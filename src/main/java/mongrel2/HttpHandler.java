/*
 * Copyright (C) 2011 Karl Ostendorf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mongrel2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONException;
import org.zeromq.ZMQ;

/**
 * A Handler for the Mongrel 2 web server. The handler connects to Mongrel2's
 * ZeroMQ socket to read requests and allows responses to be returned via the
 * separate publishing socket.
 * 
 * @author kwo
 * 
 */
public class HttpHandler {

	private static final Charset ASCII = Charset.forName("US-ASCII");
	private static final char SPACE_CHAR = ' ';

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
	private final String recvAddr;
	private final ZMQ.Socket requests;
	private final ZMQ.Socket responses;
	private final AtomicBoolean running;
	private final String sendAddr;

	/**
	 * Construct a new handler to communicate with Mongrel2.
	 * 
	 * @param senderId
	 *            A unique identifier for this handler.
	 * @param recvAddr
	 *            The socket on which the handler will receive messages. The
	 *            same as the send_spec in the mongrel2 handler configuration.
	 * @param sendAddr
	 *            The socket on which the handler will publish messages. The
	 *            same as the recv_spec in the mongrel2 handler configuration.
	 */
	public HttpHandler(final String senderId, final String recvAddr, final String sendAddr) {

		this.running = new AtomicBoolean();

		this.context = ZMQ.context(1);
		this.requests = this.context.socket(ZMQ.PULL);
		this.responses = this.context.socket(ZMQ.PUB);
		this.responses.setIdentity(senderId.getBytes());

		this.recvAddr = recvAddr;
		this.sendAddr = sendAddr;

	}

	public boolean isRunning() {
		return this.running.get();
	}

	/**
	 * Retrieves the next Request, blocking.
	 */
	public HttpRequest recv() {
		try {
			return HttpRequest.parse(this.requests.recv(0));
		} catch (final JSONException x) {
			throw new RuntimeException(x);
		}
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

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(responseStr.toString().getBytes(ASCII));
		out.write(response.formatBody());
		out.close();

		// send
		this.responses.send(out.toByteArray(), 0);

	}

	public void setRunning(final boolean running) {

		final boolean wasRunning = this.running.getAndSet(running);

		if (running && !wasRunning) {

			// start up
			this.requests.connect(this.recvAddr);
			this.responses.connect(this.sendAddr);

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
