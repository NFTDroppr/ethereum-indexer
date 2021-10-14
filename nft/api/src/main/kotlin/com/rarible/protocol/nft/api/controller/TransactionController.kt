package com.rarible.protocol.nft.api.controller

import com.rarible.core.common.convert
import com.rarible.core.logging.RaribleMDCContext
import com.rarible.protocol.dto.CreateTransactionRequestDto
import com.rarible.protocol.dto.LogEventDto
import com.rarible.protocol.nft.api.service.pending.PendingTransactionService
import com.rarible.protocol.nft.core.converters.model.ListenerTransactionConverter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.springframework.core.convert.ConversionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@ExperimentalCoroutinesApi
@RestController
class TransactionController(
    private val conversionService: ConversionService,
    private val pendingTransactionService: PendingTransactionService
) : NftTransactionControllerApi {

    override fun createNftPendingTransaction(request: CreateTransactionRequestDto): ResponseEntity<Flow<LogEventDto>> {
        val transaction = ListenerTransactionConverter.convert(request)

        val result = flow<LogEventDto> {
            pendingTransactionService
                .process(transaction)
                .forEach { emit(conversionService.convert(it)) }
        }.flowOn(RaribleMDCContext())

        return ResponseEntity.ok(result)
    }
}
