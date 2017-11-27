package com.miquido.jebatch

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

data class Body(val a: String, val b: Int)

class JeBatchTest {

  val testBody: Body = Body("a", 1)
  val e1: BatchRequestElement<Body, Long> = BatchRequestElement(Operation.GET)
  val e2: BatchRequestElement<Body, Long> = BatchRequestElement(Operation.POST, testBody)
  val e3: BatchRequestElement<Body, Long> = BatchRequestElement(Operation.PUT, testBody, 1)
  val e4: BatchRequestElement<Body, Long> = BatchRequestElement(Operation.PATCH, testBody, 1)
  val e5: BatchRequestElement<Body, Long> = BatchRequestElement(Operation.DELETE, testBody, 1)

  @Test
  fun allMethodsSuccess() {
    val req: BatchRequest<Body, Long> = BatchRequest(listOf(e1, e2, e3, e4, e5))

    JeBatch.builder<Body, Body, Long>()
        .forGet({ Arrays.asList(testBody) })
        .withError(RuntimeException::class.java, 400)

    val batch = batch<Body, Body, Long> {
      get ({ Arrays.asList(testBody) }) {}
      post ({1}) {}
      put ({ _, _ -> doSomething() }) {}
      patch ({ _, _ -> doSomething() }) {}
      delete ({ _ -> doSomething() }) {}
    }

    val response = batch.process("api/path", req)
    val responses = response.responses
    assertEquals(5, responses.size)
    assertEquals(200, responses[0].status)
    assertEquals(201, responses[1].status)
    assertEquals(200, responses[2].status)
    assertEquals(200, responses[3].status)
    assertEquals(200, responses[4].status)
    assertEquals(listOf(testBody), responses[0].body)
    assertEquals(null, responses[1].body)
    assertEquals(null, responses[2].body)
    assertEquals(null, responses[3].body)
    assertEquals(null, responses[4].body)
    assertEquals("api/path", responses[0].resourcePath)
    assertEquals("api/path/1", responses[1].resourcePath)
    assertEquals("api/path/1", responses[2].resourcePath)
    assertEquals("api/path/1", responses[3].resourcePath)
    assertEquals("api/path/1", responses[4].resourcePath)
  }

  @Test
  fun allMethodsErrors() {
    val req: BatchRequest<Body, Long> = BatchRequest(listOf(e1, e2, e3, e4, e5))

    val batch = batch<Body, Body, Long> {
      get({ throwException() }) {
        error(RuntimeException::class.java, 400)
      }
      post ({ throwException() }) {
        error(RuntimeException::class.java, 400)
      }
      put ({ _, _ -> throwException() }) {
        error(RuntimeException::class.java, 400)
      }
      patch ({ _, _ -> throwException() }) {
        error(RuntimeException::class.java, 400)
      }
      delete ({ _ -> throwException() }) {
        error(RuntimeException::class.java, 400)
      }
    }

    val response = batch.process("api/path", req)
    val responses = response.responses
    assertEquals(5, responses.size)
    assertEquals(400, responses[0].status)
    assertEquals(400, responses[1].status)
    assertEquals(400, responses[2].status)
    assertEquals(400, responses[3].status)
    assertEquals(400, responses[4].status)
    assertEquals(null, responses[0].body)
    assertEquals(null, responses[1].body)
    assertEquals(null, responses[2].body)
    assertEquals(null, responses[3].body)
    assertEquals(null, responses[4].body)
    assertEquals("api/path", responses[0].resourcePath)
    assertEquals("api/path", responses[1].resourcePath)
    assertEquals("api/path/1", responses[2].resourcePath)
    assertEquals("api/path/1", responses[3].resourcePath)
    assertEquals("api/path/1", responses[4].resourcePath)
  }

  @Test
  fun unhandledError() {
    val req: BatchRequest<Body, Long> = BatchRequest(listOf(e4, e5))

    val batch = batch<Body, Body, Long> {
      patch ({ _, _ -> throwException() }) {
        error(RuntimeException::class.java, 400)
      }
      delete ({ _ -> throwException() }) {}
    }

    val response = batch.process("api/path", req)
    val responses = response.responses
    assertEquals(2, responses.size)
    assertEquals(400, responses[0].status)
    assertEquals(500, responses[1].status)
  }

  @Test
  fun notAllowedMethod() {
    val req: BatchRequest<Body, Long> = BatchRequest(listOf(e1, e2))

    val batch = batch<Body, Body, Long> {
      get ({ Arrays.asList(testBody) }) {}
    }

    val response = batch.process("api/path", req)
    val responses = response.responses
    assertEquals(2, responses.size)
    assertEquals(200, responses[0].status)
    assertEquals(405, responses[1].status)
  }


  private fun doSomething() { }
  private fun throwException(): Nothing = throw RuntimeException()
}