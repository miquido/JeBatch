package com.miquido.jebatch

data class BatchResponseElement constructor(val status: Int,
                                            val resourcePath: String,
                                            val message: String,
                                            val body: Any?)
