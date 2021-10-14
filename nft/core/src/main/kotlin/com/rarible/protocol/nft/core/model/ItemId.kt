package com.rarible.protocol.nft.core.model

import com.rarible.ethereum.domain.EthUInt256
import com.rarible.protocol.dto.ItemIdDto
import scalether.domain.Address

data class ItemId(
    val token: Address,
    val tokenId: EthUInt256
) {
    constructor(dto: ItemIdDto): this(
        token = dto.token,
        tokenId = EthUInt256.of(dto.tokenId)
    )

    val stringValue: String
        get() = "$token:$tokenId"

    val decimalStringValue: String
        get() = "$token:${tokenId.value}"

    override fun toString(): String {
        return stringValue
    }

    companion object {
        fun parseId(itemId: String): ItemId {
            return Item.parseId(itemId)
        }

        val MAX_ID: ItemId = ItemId(
            Address.apply("0xffffffffffffffffffffffffffffffffffffffff"),
            EthUInt256.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
        )
    }
}
