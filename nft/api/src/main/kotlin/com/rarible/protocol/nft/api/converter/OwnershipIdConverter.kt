package com.rarible.protocol.nft.api.converter

import com.rarible.protocol.dto.parser.OwnershipIdParser
import com.rarible.protocol.nft.core.model.OwnershipId

object OwnershipIdConverter {
    fun convert(source: String): OwnershipId {
        val dto = OwnershipIdParser.parse(source)
        return  OwnershipId(dto)
    }
}
