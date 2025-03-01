package com.rarible.protocol.order.listener.service.opensea

import com.rarible.protocol.contracts.exchange.wyvern.WyvernExchange
import com.rarible.protocol.order.core.model.*
import io.daonomic.rpc.domain.Binary
import org.springframework.stereotype.Component

@Component
class OpenSeaOrderParser {
    fun parseMatchedOrders(input: String): OpenSeaMatchedOrders {
        val decoded = WyvernExchange.atomicMatch_Signature().`in`().decode(Binary.apply(input), 4)
        val addrs = decoded.value()._1()
        val uints = decoded.value()._2()
        val feeMethodsSidesKindsHowToCalls = decoded.value()._3()
        val calldataBuy = decoded.value()._4()
        val calldataSell = decoded.value()._5()
        val replacementPatternBuy = decoded.value()._6()
        val replacementPatternSell = decoded.value()._7()
        val staticExtradataBuy = decoded.value()._8()
        val staticExtradataSell = decoded.value()._9()
        val rssMetadata = decoded.value()._11()

        val buyOrder = OpenSeaTransactionOrder(
            exchange = addrs[0],
            maker = addrs[1],
            taker = addrs[2],
            makerRelayerFee = uints[0],
            takerRelayerFee = uints[1],
            makerProtocolFee = uints[2],
            takerProtocolFee = uints[3],
            feeRecipient = addrs[3],
            feeMethod = OpenSeaOrderFeeMethod.fromBigInteger(feeMethodsSidesKindsHowToCalls[0]),
            side = OpenSeaOrderSide.fromBigInteger(feeMethodsSidesKindsHowToCalls[1]),
            saleKind = OpenSeaOrderSaleKind.fromBigInteger(feeMethodsSidesKindsHowToCalls[2]),
            target = addrs[4],
            howToCall = OpenSeaOrderHowToCall.fromBigInteger(feeMethodsSidesKindsHowToCalls[3]),
            callData = Binary.apply(calldataBuy),
            replacementPattern = Binary.apply(replacementPatternBuy),
            staticTarget = addrs[5],
            staticExtraData = Binary.apply(staticExtradataBuy),
            paymentToken = addrs[6],
            basePrice = uints[4],
            extra = uints[5],
            listingTime = uints[6],
            expirationTime = uints[7],
            salt = uints[8]
        )
        val sellOrder = OpenSeaTransactionOrder(
            exchange = addrs[7],
            maker = addrs[8],
            taker = addrs[9],
            makerRelayerFee = uints[9],
            takerRelayerFee = uints[10],
            makerProtocolFee = uints[11],
            takerProtocolFee = uints[12],
            feeRecipient = addrs[10],
            feeMethod = OpenSeaOrderFeeMethod.fromBigInteger(feeMethodsSidesKindsHowToCalls[4]),
            side = OpenSeaOrderSide.fromBigInteger(feeMethodsSidesKindsHowToCalls[5]),
            saleKind = OpenSeaOrderSaleKind.fromBigInteger(feeMethodsSidesKindsHowToCalls[6]),
            target = addrs[11],
            howToCall = OpenSeaOrderHowToCall.fromBigInteger(feeMethodsSidesKindsHowToCalls[7]),
            callData = Binary.apply(calldataSell),
            replacementPattern = Binary.apply(replacementPatternSell),
            staticTarget = addrs[12],
            staticExtraData = Binary.apply(staticExtradataSell),
            paymentToken = addrs[13],
            basePrice = uints[13],
            extra = uints[14],
            listingTime = uints[15],
            expirationTime = uints[16],
            salt = uints[17]
        )
        return OpenSeaMatchedOrders(
            buyOrder = buyOrder,
            sellOrder = sellOrder,
            externalOrderExecutedOnRarible = Binary.apply(rssMetadata[4]) == Platform.RARIBLE.id
        )
    }

    fun parseCancelOrder(input: String): OpenSeaTransactionOrder {
        val decoded = WyvernExchange.cancelOrder_Signature().`in`().decode(Binary.apply(input), 4)
        val addrs = decoded.value()._1()
        val uints = decoded.value()._2()
        val feeMethod = decoded.value()._3()
        val side = decoded.value()._4()
        val saleKind = decoded.value()._5()
        val howToCall = decoded.value()._6()
        val calldata = decoded.value()._7()
        val replacementPattern = decoded.value()._8()
        val staticExtradata = decoded.value()._9()

        return OpenSeaTransactionOrder(
            exchange = addrs[0],
            maker = addrs[1],
            taker = addrs[2],
            makerRelayerFee = uints[0],
            takerRelayerFee = uints[1],
            makerProtocolFee = uints[2],
            takerProtocolFee = uints[3],
            feeRecipient = addrs[3],
            feeMethod = OpenSeaOrderFeeMethod.fromBigInteger(feeMethod),
            side = OpenSeaOrderSide.fromBigInteger(side),
            saleKind = OpenSeaOrderSaleKind.fromBigInteger(saleKind),
            target = addrs[4],
            howToCall = OpenSeaOrderHowToCall.fromBigInteger(howToCall),
            callData = Binary.apply(calldata),
            replacementPattern = Binary.apply(replacementPattern),
            staticTarget = addrs[5],
            staticExtraData = Binary.apply(staticExtradata),
            paymentToken = addrs[6],
            basePrice = uints[4],
            extra = uints[5],
            listingTime = uints[6],
            expirationTime = uints[7],
            salt = uints[8]
        )
    }
}

