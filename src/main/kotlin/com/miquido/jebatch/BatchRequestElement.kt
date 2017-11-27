package com.miquido.jebatch

data class BatchRequestElement<T, Id>(var operation: Operation,
                                      var body: T? = null,
                                      var id: Id? = null)
