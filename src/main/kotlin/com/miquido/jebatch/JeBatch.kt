package com.miquido.jebatch

import com.miquido.jebatch.Operation.*

class JeBatch<In, Out, Id> {

  companion object {
    @JvmStatic
    fun <In, Out, Id> builder(): JeBatchBuilder<In, Out, Id> = JeBatchBuilder()
  }


  private val emptyBodyResult: CallResult = CallResult(400, "empty request body")
  private val nullIdResult: CallResult = CallResult(400, "null id")
  private val notAllowedResult: CallResult = CallResult(405)

  private var get: Get<Out>? = null
  private var post: Post<In, Id>? = null
  private var put: Put<In, Id>? = null
  private var patch: Patch<In, Id>? = null
  private var delete: Delete<Id>? = null


  fun get(getter: () -> List<Out>): Get<Out> {
    get = Get(getter)
    return get as Get<Out>
  }

  fun post(poster: (In) -> Id): Post<In, Id> {
    post = Post(poster)
    return post as Post<In, Id>
  }

  fun put(putter: (Id, In) -> Unit): Put<In, Id> {
    put = Put(putter)
    return put as Put<In, Id>
  }

  fun patch(patcher: (Id, In) -> Unit): Patch<In, Id> {
    patch = Patch(patcher)
    return patch as Patch<In, Id>
  }

  fun delete(deleter: (Id) -> Unit): Delete<Id> {
    delete = Delete(deleter)
    return delete as Delete<Id>
  }


  fun process(basePath: String, request: BatchRequest<In, Id>): BatchResponse {
    val responses = request.requests.map { element: BatchRequestElement<In, Id> ->
      val (operation, body, id) = element
      val (status, message, result) = when (operation) {

        GET -> get?.perform(Unit, Unit) ?: notAllowedResult

        POST -> when (body) {
          null -> emptyBodyResult
          else -> post?.perform(Unit, body) ?: notAllowedResult
        }

        PUT -> when {
          body == null -> emptyBodyResult
          id == null -> nullIdResult
          else -> put?.perform(id, body) ?: notAllowedResult
        }

        PATCH -> when {
          body == null -> emptyBodyResult
          id == null -> nullIdResult
          else -> patch?.perform(id, body) ?: notAllowedResult
        }

        DELETE -> when (id) {
          null -> nullIdResult
          else -> delete?.perform(id, Unit) ?: notAllowedResult
        }
      }

      when (operation) {
        POST -> BatchResponseElement(status, basePath + if (result != null) "/$result" else "", message, null)
        GET -> BatchResponseElement(status, basePath, message, result)
        else -> BatchResponseElement(status, "$basePath/$id", message, null)
      }
    }
    return BatchResponse(responses)
  }
}