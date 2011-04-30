package mongrel2;

import java.util.UUID;

public class TestApp {

	private static String RECV_ADDR = "tcp://localhost:44401";
	private static String SEND_ADDR = "tcp://localhost:44402";
	private static final String SENDER_ID = UUID.randomUUID().toString();

	public static void main(final String[] args) throws Exception {

		final HttpHandler handler = new HttpHandler(SENDER_ID, RECV_ADDR, SEND_ADDR);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println();
				System.out.println("Signal caught, exiting ...");
				handler.setRunning(false);
			}
		});

		handler.setRunning(true);
		System.out.println("Running. Ctrl-c to quit.");

		while (handler.isRunning()) {

			final HttpRequest req = handler.recv();

			final HttpResponse rsp = new HttpResponse();
			rsp.setStatus(400, "Bad Request");
			rsp.setHeader("Cache-Control", "no-cache");
			rsp.setHeader("X-Handler", "TestApp");
			rsp.setDateHeader("Last-Updated", System.currentTimeMillis());

			handler.send(rsp, req);

		}

	}

}
