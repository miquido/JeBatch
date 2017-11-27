package com.miquido.jebatch

data class CallResult(val status: Int, val message: String = "", val result: Any? = null)