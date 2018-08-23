package com.miquido.jebatch

import java.util.HashMap


abstract class RestMethod<in Req, Res, in Id>(private var handler: ((Id, Req) -> Res),
                                              private val successStatus: Int = 200) {

  private val errors: MutableMap<Class<out Exception>, Int> = HashMap()

  fun <E : Exception> error(exceptionClass: Class<E>, status: Int) {
    errors[exceptionClass] = status
  }

  fun perform(id: Id, req: Req): CallResult {
    return try {
      val result = handler.invoke(id, req)
      CallResult(successStatus, "", result)
    } catch (e: Exception) {
      CallResult(errors.getOrDefault(e::class.java, 500), e.message ?: "")
    }
  }
}


class Get<Out, Id>(getter: (Id) -> Out) : RestMethod<Unit, Out, Id>({ id, _ -> getter.invoke(id) })


class GetAll<Out>(getter: () -> Collection<Out>) : RestMethod<Unit, Collection<Out>, Unit>({ _, _ -> getter.invoke() })


class Post<in In, Id>(poster: (In) -> Id) : RestMethod<In, Id, Unit>({ _, t -> poster.invoke(t) }, 201)


class Put<in In, in Id>(putter: (Id, In) -> Unit) : RestMethod<In, Unit, Id>(putter)


class Patch<in In, in Id>(patcher: (Id, In) -> Unit) : RestMethod<In, Unit, Id>(patcher)


class Delete<in Id>(deleter: (Id) -> Unit) : RestMethod<Unit, Unit, Id>({ id, _ -> deleter.invoke(id) })