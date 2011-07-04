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

## Building and Installing

First, download jzmq and build it as follows:

	cd jzmq
	./autogen.sh
	./configure
	make
	mvn clean install

If you run into trouble, check the jzmq project [readme](https://github.com/zeromq/jzmq#readme).
				
Now, build the mojaha JAR as follows:

	cd mojaha
	mvn clean install

A JAR file will be generated in the mojaha/target directory named mojaha-VERSION.jar.

## Running
Run your application with mojaha as follows:

	java -cp YourApp.jar:mojaha-VERSION.jar your.App

## Related Projects
 - [Java Mongrel2 Handler](https://github.com/asinger/mongrel2j)

