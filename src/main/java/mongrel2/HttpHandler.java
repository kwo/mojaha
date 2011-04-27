package mongrel2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONException;
import org.json.JSONObject;
import org.zeromq.ZMQ;

public class HttpHandler {

	private static final Charset ASCII = Charset.forName("US-ASCII");
	private static final String LINE_TERMINATOR = "\r\n";
	private static final char SPACE_CHAR = ' ';

	static int findNextDelimiter(final byte[] raw, final int last, final char delimiter) {
		final int start = last + 1;
		for (int i = start; i < raw.length; i++)
			if (raw[i] == delimiter)
				return i;
		return -1;
	}

	static String formatNetString(final HttpRequest[] requests) {
		final String[] requestIds = new String[requests.length];
		for (int i = 0; i < requests.length; i++)
			requestIds[i] = requests[i].getRequestId();
		return formatNetString(requestIds);
	}

	static String formatNetString(final String[] values) {

		final StringBuilder b = new StringBuilder();

		for (final String value : values) {
			if (b.length() > 0)
				b.append(SPACE_CHAR);
			b.append(value);
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

	public HttpHandler(final String recvSpec, final String sendSpec) {

		this.running = new AtomicBoolean();

		this.context = ZMQ.context(1);
		this.requests = this.context.socket(ZMQ.PULL);
		this.responses = this.context.socket(ZMQ.PUB);

		this.recvSpec = recvSpec;
		this.sendSpec = sendSpec;

	}

	public boolean isRunning() {
		return this.running.get();
	}

	/**
	 * Retrieves the next Request, blocking.
	 */
	public HttpRequest recv() {

		// TODO

		final byte[] raw = this.requests.recv(0);

		try {
			System.out.write(raw);
			System.out.println();
		} catch (final IOException e) {
		}

		final HttpRequest req = new HttpRequest();

		int p0 = -1;
		int p1 = -1;
		int length = 0;

		// sender addr
		p1 = findNextDelimiter(raw, p0, ' ');
		req.setSenderAddr(new String(raw, p0 + 1, p1 - p0 - 1));

		// request-id
		p0 = p1;
		p1 = findNextDelimiter(raw, p0, ' ');
		req.setRequestId(new String(raw, p0 + 1, p1 - p0 - 1));

		// matching path
		p0 = p1;
		p1 = findNextDelimiter(raw, p0, ' ');
		req.setServletPath(new String(raw, p0 + 1, p1 - p0 - 1));

		p0 = p1;
		p1 = findNextDelimiter(raw, p0, ':');
		length = Integer.parseInt(new String(raw, p0 + 1, p1 - p0 - 1));
		final String jsonHeaders = new String(raw, p1 + 1, length);
		try {
			final JSONObject headers = new JSONObject(jsonHeaders);
			@SuppressWarnings("unchecked")
			final Iterator<String> keys = headers.keys();
			while (keys.hasNext()) {
				final String key = keys.next();
				final String value = headers.getString(key);

				if (key.equalsIgnoreCase("method")) {

				} else {
					req.setHeader(key, value);
				}

			}
		} catch (final JSONException e) {
		}

		p0 = p1 + length + 1;
		p1 = findNextDelimiter(raw, p0, ':');
		length = Integer.parseInt(new String(raw, p0 + 1, p1 - p0 - 1));
		req.setContent(Arrays.copyOfRange(raw, p1 + 1, p1 + 1 + length));

		return req;

	}

	public void send(final HttpResponse response, final HttpRequest... recipients) throws IOException {

		if (recipients == null || recipients.length == 0)
			throw new IllegalArgumentException();

		// use sender addr of first request
		final String senderAddr = recipients[0].getSenderAddr();
		final String recipientNetString = formatNetString(recipients);

		// construct mongrel2 response
		final StringBuilder responseStr = new StringBuilder();
		responseStr.append(senderAddr);
		responseStr.append(SPACE_CHAR);
		responseStr.append(recipientNetString);
		responseStr.append(SPACE_CHAR);

		// body

		responseStr.append("HTTP/1.1 ");
		responseStr.append(response.getStatus());
		responseStr.append(SPACE_CHAR);
		responseStr.append(response.getStatusMessage());
		responseStr.append(LINE_TERMINATOR);

		// headers
		for (final String name : response.getHeaderNames()) {
			responseStr.append(name);
			responseStr.append(": ");
			final String[] values = response.getHeaderValues(name);
			for (int i = 0; i < values.length; i++) {
				if (i > 0)
					responseStr.append(',');
				responseStr.append(values[i]);
			}
			responseStr.append(LINE_TERMINATOR);
		}

		responseStr.append(LINE_TERMINATOR);

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(responseStr.toString().getBytes(ASCII));
		out.write(response.getContent());

		// send
		this.responses.send(out.toByteArray(), 0);

	}

	public void setRunning(final boolean running) {

		final boolean wasRunning = this.running.getAndSet(running);

		if (running && !wasRunning) {

			// start up
			this.requests.connect(this.recvSpec);
			this.responses.connect(this.sendSpec);

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