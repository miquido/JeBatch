package com.miquido.jebatch

import com.miquido.jebatch.Operation.*

@BuilderTag
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


  fun get(getter: () -> List<Out>, block: Get<Out>.() -> Unit): Get<Out> {
    get = Get(getter)
    get!!.block()
    return get as Get<Out>
  }

  fun post(poster: (In) -> Id, block: Post<In, Id>.() -> Unit): Post<In, Id> {
    post = Post(poster)
    post!!.block()
    return post as Post<In, Id>
  }

  fun put(putter: (Id, In) -> Unit, block: Put<In, Id>.() -> Unit): Put<In, Id> {
    put = Put(putter)
    put!!.block()
    return put as Put<In, Id>
  }

  fun patch(patcher: (Id, In) -> Unit, block: Patch<In, Id>.() -> Unit): Patch<In, Id> {
    patch = Patch(patcher)
    patch!!.block()
    return patch as Patch<In, Id>
  }

  fun delete(deleter: (Id) -> Unit, block: Delete<Id>.() -> Unit): Delete<Id> {
    delete = Delete(deleter)
    delete!!.block()
    return delete as Delete<Id>
  }


  fun process(basePath: String, request: BatchRequest<In, Id>): BatchResponse {
    val responses = request.requests.map { element: BatchRequestElement<In, Id> ->
      val (operation, body, id) = element
      val (status, message, result) = when (operation) {

        GET -> when {
          get == null -> notAllowedResult
          else -> get!!.perform(Unit, Unit)
        }

        POST -> when {
          post == null -> notAllowedResult
          body == null -> emptyBodyResult
          else -> post!!.perform(Unit, body)
        }

        PUT -> when {
          put == null -> notAllowedResult
          body == null -> emptyBodyResult
          id == null -> nullIdResult
          else -> put!!.perform(id, body)
        }

        PATCH -> when {
          patch == null -> notAllowedResult
          body == null -> emptyBodyResult
          id == null -> nullIdResult
          else -> patch!!.perform(id, body)
        }

        DELETE -> when {
          delete == null -> notAllowedResult
          id == null -> nullIdResult
          else -> delete!!.perform(id, Unit)
        }
      }

      when (operation) {
        POST -> BatchResponseElement(status, basePath + if (result != null) "/" + result else "", message, null)
        GET -> BatchResponseElement(status, basePath, message, result)
        else -> BatchResponseElement(status, basePath + "/" + id, message, null)
      }
    }
    return BatchResponse(responses)
  }
}


fun <In, Out, Id> batch(block: JeBatch<In, Out, Id>.() -> Unit): JeBatch<In, Out, Id> {
  val batch = JeBatch<In, Out, Id>()
  batch.block()
  return batch
}