# mojaha: Mongrel2 Java Handler
****
Java handler for the [Mongrel2](http://mongrel2.org/) web server.
mojaha is a [ZeroMQ](http://www.zeromq.org/)-based library that can read requests and send responses to Mongrel2.
It aims for similarity but not compatibility with the Servlet API while also supporting asynchronous communications.

## Example
```java
Mongrel2Handler handler = new Mongrel2Handler(SENDER_ID, RECV_ADDR, SEND_ADDR);
handler.setActive(true);
while (handler.isActive()) {
	HttpRequest req = new HttpRequest();
	HttpResponse rsp = new HttpResponse();
	handler.takeRequest(req); // wait until next request arrives
	rsp.setContent("Hello, world!\n");
	// rsp.setContent("Hello, world!\n".getBytes("UTF-8"));
	rsp.setStatus(HttpStatus.OK);
	handler.sendResponse(rsp, req); // multiple requests may be given here
}
```

## Features
 - Supports asynchronous HTTP.
 - The mojaha JAR itself is executable and will simply print out version information to the console.

## Requirements
 - Java 1.6 JDK
 - Maven 2 +
 - [jzmq](https://github.com/zeromq/jzmq): the Java bindings for [ZeroMQ](http://www.zeromq.org/).
   As jzmq is not in the maven repositories, it will need to be built and installed locally before compiling mojaha.
   Additionally, jzmq includes a native library which must also be built and installed locally as well as referenced
   in the java.library.path at runtime.

## Building and Installing

First, download and build jzmq according to the project [readme](https://github.com/zeromq/jzmq#readme).
As an additional step, not in the readme, install the JAR into your local maven repository as follows:

				mvn install -Dmaven.test.skip=true
				
Now, build the mojaha JAR as follows:

                cd mojaha
                mvn clean install

A JAR file will be generated in the mojaha/target directory named mojaha-VERSION.jar.

## Running
When running your application with mojaha, the jzmq native library will need to be referenced in the java.library.path

                java -Djava.library.path=/path/to/jzmqlib -cp YourApp.jar:mojaha.jar your.App

## Related Projects
 - [Java Mongrel2 Handler](https://github.com/asinger/mongrel2j)

