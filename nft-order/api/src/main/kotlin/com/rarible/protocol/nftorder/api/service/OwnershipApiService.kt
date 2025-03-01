package com.rarible.protocol.nftorder.api.service

import com.rarible.protocol.dto.NftOrderOwnershipDto
import com.rarible.protocol.dto.NftOrderOwnershipsPageDto
import com.rarible.protocol.dto.NftOwnershipsDto
import com.rarible.protocol.nft.api.client.NftOwnershipControllerApi
import com.rarible.protocol.nftorder.core.converter.NftOwnershipDtoToNftOrderOwnershipDtoConverter
import com.rarible.protocol.nftorder.core.converter.OwnershipToDtoConverter
import com.rarible.protocol.nftorder.core.model.Ownership
import com.rarible.protocol.nftorder.core.model.OwnershipId
import com.rarible.protocol.nftorder.core.service.OrderService
import com.rarible.protocol.nftorder.core.service.OwnershipService
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class OwnershipApiService(
    private val nftOwnershipControllerApi: NftOwnershipControllerApi,
    private val ownershipService: OwnershipService,
    private val orderService: OrderService
) {

    private val logger = LoggerFactory.getLogger(OwnershipApiService::class.java)

    suspend fun getOwnershipById(id: OwnershipId): NftOrderOwnershipDto {
        logger.debug("Get Ownership: [{}]", id)
        val ownership = ownershipService.getOrFetchOwnershipById(id).entity
        return ownershipService.enrichOwnership(ownership)
    }

    suspend fun getAllOwnerships(continuation: String?, size: Int?): NftOrderOwnershipsPageDto {
        logger.debug("Get all Ownerships with params: continuation={}, size={}", continuation, size)
        return ownershipsResponse(nftOwnershipControllerApi.getNftAllOwnerships(continuation, size))
    }

    suspend fun getOwnershipsByItem(
        contract: String,
        tokenId: String,
        continuation: String?,
        size: Int?
    ): NftOrderOwnershipsPageDto {
        logger.debug(
            "Get Ownerships by item with params: contract=[{}], tokenId=[{}], continuation={}, size={}",
            contract, tokenId, continuation, size
        )
        return ownershipsResponse(
            nftOwnershipControllerApi.getNftOwnershipsByItem(contract, tokenId, continuation, size)
        )
    }

    private suspend fun ownershipsResponse(apiResponse: Mono<NftOwnershipsDto>): NftOrderOwnershipsPageDto {
        val response = apiResponse.awaitFirst()
        val ownerships = response.ownerships

        return if (ownerships.isEmpty()) {
            logger.debug("No Ownerships found")
            NftOrderOwnershipsPageDto(null, emptyList())
        } else {
            val existingOwnerships: Map<OwnershipId, Ownership> = ownershipService
                .findAll(ownerships.map { OwnershipId.parseId(it.id) })
                .associateBy { it.id }
            logger.debug("{} enriched of {} Items found in DB", existingOwnerships.size, ownerships.size)

            // Looking for full orders for existing items in order-indexer
            val shortOrderIds = existingOwnerships.values
                .mapNotNull { it.bestSellOrder?.hash }

            val orders = orderService.getByIds(shortOrderIds).associateBy { it.hash }

            val result = ownerships.map {
                val ownershipId = OwnershipId.parseId(it.id)
                val existingOwnership = existingOwnerships[ownershipId]
                if (existingOwnership == null) {
                    // No enrichment data found, ownership proxied "as is"
                    NftOwnershipDtoToNftOrderOwnershipDtoConverter.convert(it)
                } else {
                    // Enriched item found, using it for response
                    OwnershipToDtoConverter.convert(existingOwnership, orders)
                }
            }

            NftOrderOwnershipsPageDto(response.continuation, result)
        }
    }
}