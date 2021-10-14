package com.rarible.protocol.nft.api.controller

import com.rarible.core.common.convert
import com.rarible.protocol.dto.NftCollectionDto
import com.rarible.protocol.dto.NftCollectionsDto
import com.rarible.protocol.dto.NftTokenIdDto
import com.rarible.protocol.dto.parser.AddressParser
import com.rarible.protocol.nft.api.service.colllection.CollectionService
import com.rarible.protocol.nft.core.model.TokenFilter
import org.springframework.core.convert.ConversionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.lang.Integer.min

@RestController
class CollectionController(
    private val collectionService: CollectionService,
    private val conversionService: ConversionService
) : NftCollectionControllerApi {

    override suspend fun getNftCollectionById(
        collection: String
    ): ResponseEntity<NftCollectionDto> {
        val result = collectionService
            .get(AddressParser.parse(collection))
            .let { conversionService.convert<NftCollectionDto>(it) }
        return ResponseEntity.ok(result)
    }

    override suspend fun searchNftAllCollections(
        continuation: String?,
        size: Int?
    ): ResponseEntity<NftCollectionsDto> {
        val filter = TokenFilter.All(continuation, size.limit())
        val result = searchCollections(filter)
        return ResponseEntity.ok(result)
    }

    override suspend fun searchNftCollectionsByOwner(
        owner: String,
        continuation: String?,
        size: Int?
    ): ResponseEntity<NftCollectionsDto> {
        val filter = TokenFilter.ByOwner(
            AddressParser.parse(owner),
            continuation,
            size.limit()
        )
        val result = searchCollections(filter)
        return ResponseEntity.ok(result)
    }

    override suspend fun generateNftTokenId(
        collection: String,
        minter: String
    ): ResponseEntity<NftTokenIdDto> {
        val collectionAddress = AddressParser.parse(collection)
        val minterAddress = AddressParser.parse(minter)
        val nextTokenId = collectionService.generateId(collectionAddress, minterAddress)
        val result = conversionService.convert<NftTokenIdDto>(nextTokenId)
        return ResponseEntity.ok(result)
    }

    private suspend fun searchCollections(filter: TokenFilter): NftCollectionsDto {
        val collections = collectionService.search(filter)
        val continuation =
            if (collections.isEmpty() || collections.size < filter.size) null else collections.last().id.toString()

        return NftCollectionsDto(
            total = collections.size.toLong(),
            collections = collections.map { conversionService.convert<NftCollectionDto>(it) },
            continuation = continuation
        )
    }

    companion object {
        fun Int?.limit(): Int = min(this ?: DEFAULT_MAX_SIZE, DEFAULT_MAX_SIZE)
        private const val DEFAULT_MAX_SIZE = 1_000
    }
}
