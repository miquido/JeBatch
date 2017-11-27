package com.miquido.jebatch;

import org.junit.Test;

import java.util.Collections;

public class JeBatchTest {

  class Body {
    public String name;
    public int number;

    public Body(String name, int number) {
      this.name = name;
      this.number = number;
    }
  }

  private Body testBody = new Body("a", 1);


  @Test
  public void batchInJava() {
    JeBatch<Body, Body, Long> jeBatch = JeBatch.<Body, Body, Long>builder()
        .forGet(() -> Collections.singletonList(testBody))
        .withError(RuntimeException.class, 400)
        .and()
        .forPost(o -> 1L)
        .withError(RuntimeException.class, 400)
        .and()
        .build();
  }

}
