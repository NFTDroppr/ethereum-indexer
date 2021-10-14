package com.rarible.protocol.nft.api.converter

import com.rarible.protocol.dto.parser.ItemIdParser
import com.rarible.protocol.nft.core.model.ItemId

object ItemIdConverter {
    fun convert(source: String): ItemId {
        val dto = ItemIdParser.parse(source)
        return ItemId(dto)
    }
}

