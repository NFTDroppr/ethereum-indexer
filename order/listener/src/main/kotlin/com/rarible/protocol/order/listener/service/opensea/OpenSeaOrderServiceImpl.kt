package com.rarible.protocol.order.listener.service.opensea

import com.rarible.opensea.client.OpenSeaClient
import com.rarible.opensea.client.model.*
import com.rarible.protocol.order.listener.configuration.OrderListenerProperties
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class OpenSeaOrderServiceImpl(
    private val openSeaClient: OpenSeaClient,
    properties: OrderListenerProperties
) : OpenSeaOrderService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val loadOpenSeaPeriod = properties.loadOpenSeaPeriod.seconds
    private val loadOpenSeaOrderSide = convert(properties.openSeaOrderSide)

    override suspend fun getNextOrdersBatch(listedAfter: Long, listedBefore: Long): List<OpenSeaOrder> =
        coroutineScope {
            val batches = (listedBefore - listedAfter) / loadOpenSeaPeriod
            assert(batches >= 0) { "OpenSea batch count must be positive" }

            (1..batches).map {
                async {
                    val nextListedAfter = listedAfter + ((it - 1) * loadOpenSeaPeriod)
                    val nextListedBefore = java.lang.Long.min(listedAfter + (it * loadOpenSeaPeriod), listedBefore)
                    getNextOrders(nextListedAfter, nextListedBefore)
                }
            }.awaitAll().flatten()
        }

    private suspend fun getNextOrders(listedAfter: Long, listedBefore: Long): List<OpenSeaOrder> {
        val orders = mutableListOf<OpenSeaOrder>()
        logger.info("[OpenSea] Load from $listedAfter to $listedBefore")

        do {
            val request = OrdersRequest(
                listedAfter = Instant.ofEpochSecond(listedAfter),
                listedBefore = Instant.ofEpochSecond(listedBefore),
                offset = orders.size,
                sortBy = SortBy.CREATED_DATE,
                sortDirection = SortDirection.ASC,
                side = loadOpenSeaOrderSide,
                limit = null
            )
            val result = getOrdersWithLogIfException(request)

            orders.addAll(result)
        } while (result.isNotEmpty() && orders.size <= MAX_OFFSET)

        return orders
    }

    private suspend fun getOrders(request: OrdersRequest): List<OpenSeaOrder> {
        var lastError: OpenSeaError? = null
        var retries = 0

        while (retries++ < MAX_RETRIES) {
            when (val result = openSeaClient.getOrders(request)) {
                is OperationResult.Success -> return result.result.orders
                is OperationResult.Fail -> lastError = result.error
            }
        }
        throw IllegalStateException("Can't fetch OpenSea orders, number of attempts exceeded, last error: $lastError")
    }

    private suspend fun getOrdersWithLogIfException(request: OrdersRequest): List<OpenSeaOrder> {
        return try {
            getOrders(request)
        } catch (ex: Exception) {
            logger.error("Exception while get OpenSea orders with request: listedAfter=${request.listedAfter?.epochSecond}, listedBefore=${request.listedBefore?.epochSecond}, offset=${request.offset}, side=${request.side}, ex=${ex.javaClass.simpleName}")
            throw ex
        }
    }

    private fun convert(side: OrderListenerProperties.OrderSide?): OrderSide? {
        return when (side ?: OrderListenerProperties.OrderSide.ALL) {
            OrderListenerProperties.OrderSide.SELL -> OrderSide.SELL
            OrderListenerProperties.OrderSide.BID -> OrderSide.BUY
            OrderListenerProperties.OrderSide.ALL -> null
        }
    }

    companion object {
        const val MAX_OFFSET = 10000
        const val MAX_RETRIES = 5
    }
}
