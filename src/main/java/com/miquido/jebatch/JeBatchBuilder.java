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

  public ErrorBuilder forGet(Function<Id, Out> getter) {
    Get<Out, Id> get = jeBatch.get(getter::apply);
    return new ErrorBuilder(get);
  }

  public ErrorBuilder forGetAll(Supplier<List<Out>> getter) {
    GetAll<Out> getAll = jeBatch.getAll(getter::get);
    return new ErrorBuilder(getAll);
  }

  public ErrorBuilder forPost(Function<In, Id> poster) {
    Post<In, Id> post = jeBatch.post(poster::apply);
    return new ErrorBuilder(post);
  }

  public ErrorBuilder forPut(BiFunction<In, Id, Unit> putter) {
    Put<In, Id> put = jeBatch.put((id, in) -> { putter.apply(in, id); return Unit.INSTANCE; });
    return new ErrorBuilder(put);
  }

  public ErrorBuilder forPatch(BiFunction<In, Id, Unit> patcher) {
    Patch<In, Id> patch = jeBatch.patch((id, in) -> { patcher.apply(in, id); return Unit.INSTANCE; });
    return new ErrorBuilder(patch);
  }

  public ErrorBuilder forDelete(Consumer<Id> deleter) {
    Delete<Id> delete = jeBatch.delete(id -> { deleter.accept(id); return Unit.INSTANCE; });
    return new ErrorBuilder(delete);
  }

  public JeBatch<In, Out, Id> build() {
    return jeBatch;
  }


  class ErrorBuilder {

    final RestMethod method;

    ErrorBuilder(RestMethod method) {
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
