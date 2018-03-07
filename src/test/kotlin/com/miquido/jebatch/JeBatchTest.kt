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

    val batch = JeBatch.builder<Body, Body, Long>()
        .forGet({ Arrays.asList(testBody) })
        .withError(RuntimeException::class.java, 400)
        .and().build()

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

    val batch = JeBatch.builder<Body, Body, Long>()
        .forGet({ throwException() })
        .withError(RuntimeException::class.java, 400)
        .and()
        .forPost( { throwException() })
        .withError(RuntimeException::class.java, 400)
        .and()
        .forPut({ _, _ -> throwException() })
        .withError(RuntimeException::class.java, 400)
        .and()
        .forPatch({ _, _ -> throwException() })
        .withError(RuntimeException::class.java, 400)
        .and()
        .forDelete({ throwException() })
        .withError(RuntimeException::class.java, 400)
        .and().build()

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

    val batch = JeBatch.builder<Body, Body, Long>()
        .forPatch({ _, _ -> throwException() })
        .withError(RuntimeException::class.java, 400)
        .and()
        .forDelete({ throwException() })
        .withError(RuntimeException::class.java, 400)
        .and().build()

    val response = batch.process("api/path", req)
    val responses = response.responses
    assertEquals(2, responses.size)
    assertEquals(400, responses[0].status)
    assertEquals(500, responses[1].status)
  }

  @Test
  fun notAllowedMethod() {
    val req: BatchRequest<Body, Long> = BatchRequest(listOf(e1, e2))

    val batch = JeBatch.builder<Body, Body, Long>()
        .forGet({ Arrays.asList(testBody) })
        .and().build()

    val response = batch.process("api/path", req)
    val responses = response.responses
    assertEquals(2, responses.size)
    assertEquals(200, responses[0].status)
    assertEquals(405, responses[1].status)
  }


  private fun doSomething() { }
  private fun throwException(): Nothing = throw RuntimeException()
}