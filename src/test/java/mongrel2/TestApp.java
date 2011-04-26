package mongrel2;

public class TestApp {

	private static String RECV_SPEC = "tcp://localhost:44401";
	private static String SEND_SPEC = "tcp://localhost:44402";

	public static void main(final String[] args) throws Exception {

		final HttpHandler handler = new HttpHandler(RECV_SPEC, SEND_SPEC);
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
			rsp.setDate();
			rsp.setContentLength(0);

			handler.send(rsp, req);

		}

	}

}
