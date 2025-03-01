package com.rarible.protocol.nft.core.repository.history

import com.rarible.ethereum.domain.EthUInt256
import com.rarible.protocol.nft.core.model.ItemId
import com.rarible.protocol.nft.core.model.ItemLazyMint
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import scalether.domain.Address

class LazyNftItemHistoryRepository(
    private val mongo: ReactiveMongoOperations
) {
    fun save(lazyItemHistory: ItemLazyMint): Mono<ItemLazyMint> {
        return mongo.save(lazyItemHistory, COLLECTION)
    }

    fun remove(lazyItemHistory: ItemLazyMint): Mono<Boolean> {
        return mongo.remove(lazyItemHistory, COLLECTION).map { it.wasAcknowledged() }
    }

    fun findById(lazyMintId: ItemId): Mono<ItemLazyMint> {
        return mongo.findById(lazyMintId.stringValue, COLLECTION)
    }

    fun findItemsHistory(token: Address? = null, tokenId: EthUInt256? = null, from: ItemId? = null): Flux<ItemLazyMint> {
        val c = tokenCriteria(token, tokenId, from)
        return mongo.find(Query(c).with(LOG_SORT_ASC), ItemLazyMint::class.java, COLLECTION)
    }

    private fun tokenCriteria(token: Address?, tokenId: EthUInt256?, from: ItemId? = null): Criteria {
        return when {
            token != null && tokenId != null ->
                Criteria.where(DATA_TOKEN).`is`(token).and(DATA_TOKEN_ID).`is`(tokenId)
            token != null && from != null ->
                Criteria.where(DATA_TOKEN).`is`(token).and(DATA_TOKEN_ID).gt(from.tokenId)
            token != null ->
                Criteria.where(DATA_TOKEN).`is`(token)
            from != null ->
                Criteria().orOperator(
                    Criteria.where(DATA_TOKEN).`is`(from.token).and(DATA_TOKEN_ID).gt(from.tokenId),
                    Criteria.where(DATA_TOKEN).gt(from.token)
                )
            else ->
                Criteria()
        }
    }

    companion object {
        const val COLLECTION = "lazy_nft_item_history"

        val DATA_TOKEN = ItemLazyMint::token.name
        val DATA_TOKEN_ID = ItemLazyMint::tokenId.name

        val LOG_SORT_ASC: Sort = Sort.by(DATA_TOKEN, DATA_TOKEN_ID)
    }
}

