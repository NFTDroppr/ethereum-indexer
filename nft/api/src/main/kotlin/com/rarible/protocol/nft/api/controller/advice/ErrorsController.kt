package com.rarible.protocol.nft.api.controller.advice

import com.rarible.protocol.dto.ArgumentFormatException
import com.rarible.protocol.dto.EthereumApiErrorBadRequestDto
import com.rarible.protocol.dto.EthereumApiErrorServerErrorDto
import com.rarible.protocol.nft.api.exceptions.NftIndexerApiException
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.core.convert.ConversionFailedException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebInputException

@RestControllerAdvice(basePackages = ["com.rarible.protocol.nft.api.controller"])
class ErrorsController {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(NftIndexerApiException::class)
    fun handleIndexerApiException(ex: NftIndexerApiException) = mono {
        ResponseEntity.status(ex.status).body(ex.data)
    }

    @ExceptionHandler(
        value = [
            ServerWebInputException::class,
            ArgumentFormatException::class,
            ConversionFailedException::class
        ]
    )
    fun handleServerWebInputException(ex: Exception) = mono {
        val status = HttpStatus.BAD_REQUEST

        val error = EthereumApiErrorBadRequestDto(
            code = EthereumApiErrorBadRequestDto.Code.BAD_REQUEST,
            message = ex.cause?.cause?.message ?: ex.cause?.message ?: ex.message ?: "Missing message in error"
        )
        logger.warn("Web input error: {}", error.message)
        ResponseEntity.status(status).body(error)
    }

    @ExceptionHandler(Throwable::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handlerException(ex: Throwable) = mono {
        logger.error("System error while handling request", ex)

        EthereumApiErrorServerErrorDto(
            code = EthereumApiErrorServerErrorDto.Code.UNKNOWN,
            message = ex.message ?: "Something went wrong"
        )
    }
}
