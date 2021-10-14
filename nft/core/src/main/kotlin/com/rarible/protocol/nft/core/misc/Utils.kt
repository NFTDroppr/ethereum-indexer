package com.rarible.protocol.nft.core.misc

fun <T : Any> List<T>.ifNotEmpty(): List<T>? {
    return ifEmpty { null }
}
