package com.rarible.protocol.nft.api.controller

import com.rarible.protocol.dto.LazyNftDto
import com.rarible.protocol.dto.NftItemDto
import com.rarible.protocol.nft.api.service.mint.LazyNftValidator
import com.rarible.protocol.nft.api.service.mint.MintService
import com.rarible.protocol.nft.core.converters.dto.ExtendedItemDtoConverter
import com.rarible.protocol.nft.core.converters.model.LazyNftDtoToLazyItemHistoryConverter
import com.rarible.protocol.nft.core.model.ExtendedItem
import com.rarible.protocol.nft.core.service.item.meta.ItemMetaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class LazyMintController(
    private val mintService: MintService,
    private val lazyNftValidator: LazyNftValidator,
    private val itemMetaService: ItemMetaService
) : NftLazyMintControllerApi {

    override suspend fun mintNftAsset(lazyNftDto: LazyNftDto): ResponseEntity<NftItemDto> {
        val result = lazyNftDto
            .apply { lazyNftValidator.validate(this) }
            .let { lazeNftDto -> LazyNftDtoToLazyItemHistoryConverter.convert(lazeNftDto) }
            .let { lazyItemHistory -> mintService.createLazyNft(lazyItemHistory) }
            .let { item ->
                val meta = itemMetaService.getItemMetadata(item.id)
                ExtendedItem(item, meta)
            }
            .let { ExtendedItemDtoConverter.convert(it) }
        return ResponseEntity.ok(result)
    }
}
