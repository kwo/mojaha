package mongrel2;

import java.io.IOException;
import java.util.UUID;

public class TestApp implements Runnable {

	private static String RECV_ADDR = "tcp://localhost:44401";
	private static String SEND_ADDR = "tcp://localhost:44402";

	public static void main(final String[] args) throws Exception {

		final TestApp app = new TestApp();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println();
				System.out.println("Signal caught, exiting ...");
				app.handler.setRunning(false);
			}
		});

		System.out.println("Running. Ctrl-c to quit.");

		app.run(); // start in same thread although Runnable

	}

	private final HttpHandler handler;;
	private final String senderId;;

	public TestApp() {
		this.senderId = UUID.randomUUID().toString();
		this.handler = new HttpHandler(this.senderId, RECV_ADDR, SEND_ADDR);
	}

	@Override
	public void run() {

		this.handler.setRunning(true);

		System.out.printf("Started handler with sender id: %s%n", this.senderId);

		while (this.handler.isRunning()) {

			try {

				final HttpRequest req = this.handler.recv();

				final HttpResponse rsp = new HttpResponse();
				rsp.setStatus(400, "Bad Request");
				rsp.setHeader("Cache-Control", "no-cache");
				rsp.setHeader("X-Handler", "TestApp");
				rsp.setDateHeader("Last-Updated", System.currentTimeMillis());

				this.handler.send(rsp, req);

			} catch (final IOException x) {
				x.printStackTrace();
			}

		}

	}

}
