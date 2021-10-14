package com.rarible.protocol.nft.core.model

import com.rarible.ethereum.domain.EthUInt256
import com.rarible.protocol.dto.OwnershipIdDto
import scalether.domain.Address

data class OwnershipId(
    val token: Address,
    val tokenId: EthUInt256,
    val owner: Address
) {
    constructor(dto: OwnershipIdDto): this(
        token = dto.token,
        tokenId = EthUInt256.of(dto.tokenId),
        owner = dto.owner
    )

    val stringValue
        get() = "$token:$tokenId:$owner"

    val decimalStringValue
        get() = "$token:${tokenId.value}:$owner"

    override fun toString(): String {
        return stringValue
    }

    companion object {
        fun parseId(ownershipId: String): OwnershipId {
            return Ownership.parseId(ownershipId)
        }
    }
}
