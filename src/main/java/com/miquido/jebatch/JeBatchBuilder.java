package com.miquido.jebatch;

import kotlin.Unit;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class JeBatchBuilder<In, Out, Id> {

  private JeBatch<In, Out, Id> jeBatch = new JeBatch<>();

  JeBatchBuilder() {}

  public ErrorBuilder forGet(Supplier<List<Out>> getter) {
    Get<Out> get = jeBatch.get(getter::get, receiver -> Unit.INSTANCE);
    return new ErrorBuilder(get);
  }

  public ErrorBuilder forPost(Function<In, Id> poster) {
    Post<In, Id> post = jeBatch.post(poster::apply, receiver -> Unit.INSTANCE);
    return new ErrorBuilder(post);
  }

  public ErrorBuilder forPut(BiFunction<In, Id, Void> putter) {
    Put<In, Id> put = jeBatch.put(putter::apply, receiver -> Unit.INSTANCE);
    return new ErrorBuilder(put);
  }

  public ErrorBuilder forPatch(BiFunction<In, Id, Void> patcher) {
    Patch<In, Id> patch = jeBatch.patch(patcher::apply, receiver -> Unit.INSTANCE);
    return new ErrorBuilder(patch);
  }

  public ErrorBuilder forDelete(Consumer<Id> deleter) {
    Delete<Id> delete = jeBatch.delete(deleter::accept, receiver -> Unit.INSTANCE);
    return new ErrorBuilder(delete);
  }

  public JeBatch<In, Out, Id> build() {
    return jeBatch;
  }


  class ErrorBuilder {

    private final RestMethod method;

    private ErrorBuilder(RestMethod method) {
      this.method = method;
    }

    public <E extends Exception> ErrorBuilder withError(Class<E> exceptionClass, int status) {
      method.error(exceptionClass, status);
      return this;
    }

    public JeBatchBuilder<In, Out, Id> and() {
      return JeBatchBuilder.this;
    }

  }
}
