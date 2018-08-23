package com.miquido.jebatch;

import kotlin.Unit;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class JeBatchBuilder<In, Out, Id> {

  private JeBatch<In, Out, Id> jeBatch = new JeBatch<>();

  JeBatchBuilder() {}

  /**
   * Define handling of GET requests of single resource.
   * @param getter function consuming GET request. Accepts resource id and returns the resource.
   * @return ErrorBuilder for mapping exceptions.
   */
  public ErrorBuilder forGet(Function<Id, Out> getter) {
    Get<Out, Id> get = jeBatch.get(getter::apply);
    return new ErrorBuilder(get);
  }

  /**
   * Define handling for GET request of collection.
   * @param getter function consuming GET requests. Accepts nothing and returns a collection of resources.
   * @return ErrorBuilder for mapping exceptions.
   */
  public ErrorBuilder forGetAll(Supplier<Collection<Out>> getter) {
    GetAll<Out> getAll = jeBatch.getAll(getter::get);
    return new ErrorBuilder(getAll);
  }

  /**
   * Define handling for POST requests.
   * @param poster function consuming POST requests. Accepts a body and returns an id.
   * @return ErrorBuilder for mapping exceptions.
   */
  public ErrorBuilder forPost(Function<In, Id> poster) {
    Post<In, Id> post = jeBatch.post(poster::apply);
    return new ErrorBuilder(post);
  }

  /**
   * Define handling for PUT requests.
   * @param putter function consuming PUT requests. Accepts a body and resource id and returns nothing.
   * @return ErrorBuilder for mapping exceptions.
   */
  public ErrorBuilder forPut(BiFunction<In, Id, Unit> putter) {
    Put<In, Id> put = jeBatch.put((id, in) -> { putter.apply(in, id); return Unit.INSTANCE; });
    return new ErrorBuilder(put);
  }

  /**
   * Define handling for PATCH requests.
   * @param patcher function consuming PATCH requests. Accepts a body and resource id and returns nothing.
   * @return ErrorBuilder for mapping exceptions.
   */
  public ErrorBuilder forPatch(BiFunction<In, Id, Unit> patcher) {
    Patch<In, Id> patch = jeBatch.patch((id, in) -> { patcher.apply(in, id); return Unit.INSTANCE; });
    return new ErrorBuilder(patch);
  }

  /**
   * Define handling for DELETE requests.
   * @param deleter function consuming DELETE requests. Accepts a resource id and returns nothing.
   * @return ErrorBuilder for mapping exceptions.
   */
  public ErrorBuilder forDelete(Consumer<Id> deleter) {
    Delete<Id> delete = jeBatch.delete(id -> { deleter.accept(id); return Unit.INSTANCE; });
    return new ErrorBuilder(delete);
  }

  /**
   * @return JeBatch instance.
   */
  public JeBatch<In, Out, Id> build() {
    return jeBatch;
  }


  class ErrorBuilder {

    final RestMethod method;

    ErrorBuilder(RestMethod method) {
      this.method = method;
    }

    /**
     * Defines mapping of exception to HTTP status code.
     * @param exceptionClass class of exception to map.
     * @param status status code to map to.
     * @param <E> type of exception.
     * @return this builder.
     */
    public <E extends Exception> ErrorBuilder withError(Class<E> exceptionClass, int status) {
      method.error(exceptionClass, status);
      return this;
    }

    /**
     * Closes error translations definitions for this method.
     * @return top level builder, allowing defining other methods handling.
     */
    public JeBatchBuilder<In, Out, Id> and() {
      return JeBatchBuilder.this;
    }
  }
}
