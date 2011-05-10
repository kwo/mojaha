package mongrel2;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestApp implements Runnable {

	// The socket on which the handler will receive messages. The same as the
	// send_spec in the mongrel2 handler configuration.
	private static final String RECV_ADDR = "tcp://localhost:44401";
	// The socket on which the handler will send messages. The same as the
	// recv_spec in the mongrel2 handler configuration.
	private static final String SEND_ADDR = "tcp://localhost:44402";
	private static final int THREADS = 3;

	public static void main(final String[] args) throws Exception {

		final ExecutorService exec = Executors.newFixedThreadPool(THREADS);
		final TestApp[] apps = new TestApp[THREADS];
		for (int i = 0; i < THREADS; i++)
			apps[i] = new TestApp();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println();
				System.out.println("Signal caught, exiting ...");
				for (final TestApp app : apps)
					app.handler.setActive(false);
				exec.shutdownNow();
			}
		});

		System.out.println("Running. Ctrl-c to quit.");

		for (final TestApp app : apps)
			exec.submit(app);

	}

	private final HttpHandler handler;
	private final String senderId;

	public TestApp() {
		this.senderId = UUID.randomUUID().toString();
		this.handler = new HttpHandler(this.senderId, RECV_ADDR, SEND_ADDR);
	}

	@Override
	public void run() {

		this.handler.setActive(true);

		System.out.printf("Started handler with sender id: %s%n", this.senderId);

		while (this.handler.isActive()) {

			try {

				final HttpRequest req = this.handler.recv();

				final long now = System.currentTimeMillis();
				System.out.printf("%tH:%tM:%tS - %s %s%n", now, now, now, this.senderId, req.getRequestURL());

				final HttpResponse rsp = new HttpResponse();
				rsp.setContent("Hello, world!\n");
				rsp.setStatus(HttpStatus.OK);
				// rsp.setStatus(HttpStatus.BadRequest.code, "Nice Try");
				rsp.setHeader("Cache-Control", "no-cache");
				rsp.setHeader("X-Handler-App", "TestApp");
				rsp.setHeader("X-Sender-Id", this.senderId);
				rsp.setDateHeader("Last-Updated", System.currentTimeMillis());

				this.handler.send(rsp, req);

			} catch (final IOException x) {
				x.printStackTrace();
			}

		} // while

		System.out.printf("Exiting handler with sender id: %s%n", this.senderId);

	}
}
