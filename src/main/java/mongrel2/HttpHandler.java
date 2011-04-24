package mongrel2;

import java.util.concurrent.atomic.AtomicBoolean;

import org.zeromq.ZMQ;

public class HttpHandler {

	private static String formatRequestIds(final String... ids) {

		final StringBuilder b = new StringBuilder();

		for (final String id : ids) {
			if (b.length() > 0)
				b.append(' ');
			b.append(id);
		}

		b.insert(0, ':');
		b.insert(0, Integer.toString(b.length() - 1));
		b.append(',');

		return b.toString();

	}

	private final ZMQ.Context context;
	private final String recvSpec;
	private final ZMQ.Socket requests;
	private final ZMQ.Socket responses;
	private final AtomicBoolean running;

	private final String sendSpec;

	public HttpHandler(final String sendSpec, final String recvSpec) {

		this.running = new AtomicBoolean();

		this.context = ZMQ.context(1);
		this.requests = this.context.socket(ZMQ.PULL);
		this.responses = this.context.socket(ZMQ.PUB);

		this.sendSpec = sendSpec;
		this.recvSpec = recvSpec;

	}

	public boolean isRunning() {
		return this.running.get();
	}

	/**
	 * Retrieves the next Request, blocking.
	 */
	public HttpRequest recv() {
		return HttpRequest.parse(HttpHandler.this.requests.recv(0));
	}

	public void send(final HttpResponse response, final HttpRequest... recipients) {
		// TODO

	}

	public void setRunning(final boolean running) {

		final boolean wasRunning = this.running.getAndSet(running);

		if (running && !wasRunning) {

			// start up
			this.requests.connect(this.sendSpec);
			this.responses.connect(this.recvSpec);

		} else if (!running && wasRunning) {
			// shutdown
			try {
				this.requests.close();
			} catch (final Exception x) {
				// ignore
			}
			try {
				this.responses.close();
			} catch (final Exception x) {
				// ignore
			}
		}

	}

}
