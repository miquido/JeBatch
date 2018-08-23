package com.miquido.jebatch

import org.junit.Assert.*
import org.junit.Test
import java.util.Arrays

data class Body(val a: String, val b: Int)

class JeBatchTest {

  val testBody: Body = Body("a", 1)
  val e1: BatchRequestElement<Body, Long> = BatchRequestElement(Operation.GET)
  val e2: BatchRequestElement<Body, Long> = BatchRequestElement(Operation.GET, null, 1)
  val e3: BatchRequestElement<Body, Long> = BatchRequestElement(Operation.POST, testBody)
  val e4: BatchRequestElement<Body, Long> = BatchRequestElement(Operation.PUT, testBody, 1)
  val e5: BatchRequestElement<Body, Long> = BatchRequestElement(Operation.PATCH, testBody, 1)
  val e6: BatchRequestElement<Body, Long> = BatchRequestElement(Operation.DELETE, testBody, 1)

  @Test
  fun allMethodsSuccess() {
    val req: BatchRequest<Body, Long> = BatchRequest(listOf(e1, e2, e3, e4, e5, e6))

    val batch = JeBatch.builder<Body, Body, Long>()
        .forGetAll { Arrays.asList(testBody) }
        .and().forGet { _ -> testBody }
        .and().forPost { 1 }
        .and().forPut { _, _ -> doSomething() }
        .and().forPatch { _, _ -> doSomething() }
        .and().forDelete { _ -> doSomething() }
        .and().build()

    val response = batch.process("api/path", req)
    val responses = response.responses
    assertEquals(6, responses.size)
    assertEquals(200, responses[0].status)
    assertEquals(200, responses[1].status)
    assertEquals(201, responses[2].status)
    assertEquals(200, responses[3].status)
    assertEquals(200, responses[4].status)
    assertEquals(200, responses[5].status)
    assertEquals(listOf(testBody), responses[0].body)
    assertEquals(testBody, responses[1].body)
    assertEquals(null, responses[2].body)
    assertEquals(null, responses[3].body)
    assertEquals(null, responses[4].body)
    assertEquals(null, responses[5].body)
    assertEquals("api/path", responses[0].resourcePath)
    assertEquals("api/path/1", responses[1].resourcePath)
    assertEquals("api/path/1", responses[2].resourcePath)
    assertEquals("api/path/1", responses[3].resourcePath)
    assertEquals("api/path/1", responses[4].resourcePath)
    assertEquals("api/path/1", responses[5].resourcePath)
  }

  @Test
  fun allMethodsErrors() {
    val req: BatchRequest<Body, Long> = BatchRequest(listOf(e1, e2, e3, e4, e5, e6))

    val batch = JeBatch.builder<Body, Body, Long>()
        .forGetAll { throwException() }
        .withError(RuntimeException::class.java, 400)
        .and()
        .forGet { throwException() }
        .withError(RuntimeException::class.java, 400)
        .and()
        .forPost { throwException() }
        .withError(RuntimeException::class.java, 400)
        .and()
        .forPut { _, _ -> throwException() }
        .withError(RuntimeException::class.java, 400)
        .and()
        .forPatch { _, _ -> throwException() }
        .withError(RuntimeException::class.java, 400)
        .and()
        .forDelete { throwException() }
        .withError(RuntimeException::class.java, 400)
        .and().build()

    val response = batch.process("api/path", req)
    val responses = response.responses
    assertEquals(6, responses.size)
    assertEquals(400, responses[0].status)
    assertEquals(400, responses[1].status)
    assertEquals(400, responses[2].status)
    assertEquals(400, responses[3].status)
    assertEquals(400, responses[4].status)
    assertEquals(400, responses[5].status)
    assertEquals(null, responses[0].body)
    assertEquals(null, responses[1].body)
    assertEquals(null, responses[2].body)
    assertEquals(null, responses[3].body)
    assertEquals(null, responses[4].body)
    assertEquals(null, responses[5].body)
    assertEquals("api/path", responses[0].resourcePath)
    assertEquals("api/path/1", responses[1].resourcePath)
    assertEquals("api/path", responses[2].resourcePath)
    assertEquals("api/path/1", responses[3].resourcePath)
    assertEquals("api/path/1", responses[4].resourcePath)
    assertEquals("api/path/1", responses[5].resourcePath)
  }

  @Test
  fun unhandledError() {
    val req: BatchRequest<Body, Long> = BatchRequest(listOf(e5, e6))

    val batch = JeBatch.builder<Body, Body, Long>()
        .forPatch { _, _ -> throwException() }
        .withError(RuntimeException::class.java, 400)
        .and()
        .forDelete { throwException() }
        .and().build()

    val response = batch.process("api/path", req)
    val responses = response.responses
    assertEquals(2, responses.size)
    assertEquals(400, responses[0].status)
    assertEquals(500, responses[1].status)
  }

  @Test
  fun notAllowedMethod() {
    val req: BatchRequest<Body, Long> = BatchRequest(listOf(e1, e3))

    val batch = JeBatch.builder<Body, Body, Long>()
        .forGetAll { Arrays.asList(testBody) }
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