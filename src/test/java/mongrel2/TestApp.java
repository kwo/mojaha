package mongrel2;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestApp implements Runnable {

	// The socket on which the handler will receive messages. The same as the
	// send_spec in the mongrel2 handler configuration.
	private static final String RECV_ADDR = "tcp://localhost:44401";
	// The socket on which the handler will send messages. The same as the
	// recv_spec in the mongrel2 handler configuration.
	private static final String SEND_ADDR = "tcp://localhost:44402";

	// sender ids should be persistent between runs
	private static final String[] SENDERS = { "20ACBE50-DD9B-4704-9B98-F59DA590CD0E",
			"AEE93C2D-863E-4ED6-9F2E-8FF30A2BAF04", "F87B7173-9072-4330-AE32-98A4EBBECE27" };

	public static void main(final String[] args) throws Exception {

		final ExecutorService exec = Executors.newFixedThreadPool(SENDERS.length);
		final TestApp[] apps = new TestApp[SENDERS.length];
		for (int i = 0; i < SENDERS.length; i++)
			apps[i] = new TestApp(SENDERS[i]);

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

	private final Mongrel2Handler handler;
	private final String senderId;

	public TestApp(final String senderId) {
		this.senderId = senderId;
		this.handler = new Mongrel2Handler(this.senderId, RECV_ADDR, SEND_ADDR);
	}

	@Override
	public void run() {

		this.handler.setActive(true);

		System.out.printf("Started handler with sender id: %s%n", this.senderId);

		while (this.handler.isActive()) {

			try {

				final HttpRequest req = new HttpRequest();
				this.handler.takeRequest(req);

				final long now = System.currentTimeMillis();
				System.out.printf("%tH:%tM:%tS - %s %s%n", now, now, now, this.senderId, req.getRequestURL());

				final HttpResponse rsp = new HttpResponse();
				rsp.setContent("Hello, world!\n");
				rsp.setStatus(HttpStatus.OK);
				// rsp.setStatus(HttpStatus.BadRequest.code, "Nice Try");
				rsp.setHeader("Cache-Control", "public");
				rsp.setHeader("X-Handler-App", "TestApp");
				rsp.setHeader("X-Sender-Id", this.senderId);
				rsp.setDateHeader("Last-Updated", System.currentTimeMillis());

				this.handler.sendResponse(rsp, req);

			} catch (final IOException x) {
				x.printStackTrace();
			}

		} // while

		System.out.printf("Exiting handler with sender id: %s%n", this.senderId);

	}
}
