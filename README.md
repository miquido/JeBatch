JeBatch is a simple library for handling lightweight batch requests on serverside.
It allows handling collections of requests of mixed types with single call.
Designed for handling operations on collection of resources.

For use with Kotlin and Java.


# Use cases and limitations
JeBatch is designed for handling REST-compliant batch requests on collection of resources: getting collection with GET, creating a resource with POST, replacing it with PUT, updating with PATCH or deleting with DELETE.

Supported HTTP methods are GET, POST, PUT, PATCH and DELETE.

GET works for returning whole collection, not single resources identified by id.

POST does not allow returning response body to the caller.

# Usage
Creating a JeBatch instance:
```java
val jeBatch = JeBatch.builder<REQUEST_BODY_TYPE, RESPONSE_BODY_TYPE, ID_TYPE>()
        .forGet { getResourcesList() }
        .and().forPost { requestBody -> createResourceAndReturnId(requestBody) }
        .and().forPut { requestBody, id -> putResource(requestBody, id) }
        .and().forPatch { requestBody, id -> patchResource(requestBody, id) }
        .and().forDelete { id -> deleteResource(id) }
        .and().build()
```
REQUEST_BODY_TYPE is the type that is expected to come as body in POST, PUT and PATCH requests. 

RESPONSE_BODY_TYPE is the type of items in collection returned by GET.

ID_TYPE is the type of identifiers of resources that are appended to path for PUT, PATCH and DELETE requests and returned in path in result of POST request.

JeBatch can translate Exceptions thrown from your methods into HTTP response statuses. To define error translating add withError calls on builder after defining methods:
```java
val jeBatch = JeBatch.builder<REQUEST_BODY_TYPE, RESPONSE_BODY_TYPE, ID_TYPE>()
        .forPatch { requestBody, id -> patchResource(requestBody, id) }
        .withError(NotFoundException::class.java, 404)
        .withError(BadRequestException::class.java, 400)
        .withError(ValidationException::class.java, 422)
        .and()
        .forPut { requestBody, id -> putResource(requestBody, id) }
        .withError(BadRequestException::class.java, 400)
        .withError(ValidationException::class.java, 422)
        .and()
        .build()
```

Methods that are not defined in builder will cause a 405 response to be returned upon being called. Message of exceptions is returned in message of the response.

JeBatch accepts request of type BatchRequest. These contain list of BatchRequestElements, each with defined HTTP method (operation), optional (depending on method) body and id.

Analogously, it returns BatchResponse containg a list of BatchResponseElements, each with status, resourcePath (except for GET), message (if there was an error) and body (if it was a GET).

Ideally, your service should accept requests and respond in these formats. Then, usage of JeBatch is trivial:
```java
val response = batch.process("PATH_TO_RESOURCES_COLLECTION", batchRequest)
```
Since base path is passed on each process() invocation you can use one instance of JeBatch for handling multiple paths, as long as request and response bodies and id remain the same type.

# Further development
* Add GET for single resource.
* Support returning body from any method. Not very RESTy but useful.
