package mongrel2;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestApp implements Runnable {

	private static final String RECV_ADDR = "tcp://localhost:44401";
	private static final String SEND_ADDR = "tcp://localhost:44402";
	private static final int THREADS = 3;

	public static void main(final String[] args) throws Exception {

		final ExecutorService exec = Executors.newFixedThreadPool(THREADS);
		final List<TestApp> apps = new CopyOnWriteArrayList<TestApp>();
		for (int i = 0; i < THREADS; i++)
			apps.add(new TestApp());

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println();
				System.out.println("Signal caught, exiting ...");
				for (final TestApp app : apps)
					app.handler.setRunning(false);
				exec.shutdownNow();
			}
		});

		System.out.println("Running. Ctrl-c to quit.");

		for (final TestApp app : apps)
			exec.submit(app);

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

				final long now = System.currentTimeMillis();
				System.out.printf("%tH:%tM:%tS - %s %s%n", now, now, now, this.senderId, req.getRequestURL());

				final HttpResponse rsp = new HttpResponse();
				rsp.setContent("Hello, world!\n");
				rsp.setStatus(200, "OK");
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
