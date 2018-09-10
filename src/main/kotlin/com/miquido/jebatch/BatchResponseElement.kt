package com.miquido.jebatch

data class BatchResponseElement (val status: Int,
                                 val resourcePath: String,
                                 val message: String,
                                 val body: Any?)
