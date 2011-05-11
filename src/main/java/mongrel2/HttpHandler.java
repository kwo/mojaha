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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.zeromq.ZMQ;

/**
 * A Handler for the Mongrel 2 web server. The handler connects to Mongrel2's
 * ZeroMQ socket to read requests and allows responses to be returned via the
 * separate publishing socket.
 * 
 * @author Karl Ostendorf
 * 
 */
public class HttpHandler {

	/**
	 * Internal listener that than converts the headers, contents, etc of the
	 * HttpResponse into a byte array that is the pure http response and writes
	 * it to the underlying Response's payload.
	 * 
	 */
	public class InternalListener implements HandlerListener {

		@Override
		public void beforeSendResponse(final Response response) throws IOException {

			final HttpResponse rsp = (HttpResponse) response;

			final String LINE_TERMINATOR = "\r\n";
			final char SPACE_CHAR = ' ';

			final StringBuilder responseStr = new StringBuilder();

			// body
			responseStr.append("HTTP/1.1 ");
			responseStr.append(rsp.getStatus());
			responseStr.append(SPACE_CHAR);
			responseStr.append(rsp.getStatusMessage());
			responseStr.append(LINE_TERMINATOR);

			// headers
			for (final String name : rsp.getHeaderNames()) {
				for (final String value : rsp.getHeaderValues(name)) {
					responseStr.append(name);
					responseStr.append(": ");
					responseStr.append(value);
					responseStr.append(LINE_TERMINATOR);
				}
			}

			responseStr.append(LINE_TERMINATOR);

			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			out.write(responseStr.toString().getBytes(ASCII));
			if (rsp.getContent().length > 0)
				out.write(rsp.getContent());
			out.close();

			rsp.setPayload(out.toByteArray());

		}

	}

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

	private final AtomicBoolean active;
	private ZMQ.Context context = null;
	private final List<HandlerListener> listeners;
	private final String recvAddr;
	private ZMQ.Socket requests = null;
	private ZMQ.Socket responses = null;
	private final String sendAddr;
	private final String senderId;

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
		this.senderId = senderId;
		this.recvAddr = recvAddr;
		this.sendAddr = sendAddr;
		this.active = new AtomicBoolean();
		this.listeners = new CopyOnWriteArrayList<HandlerListener>();
		this.listeners.add(new InternalListener());
	}

	/**
	 * Add a handler listener. Note that listeners are executed in LIFO order
	 * such that the last listeners added will be the first to be called in the
	 * processing chains.
	 * 
	 * @param l
	 *            listener
	 */
	public void addHandlerListener(final HandlerListener l) {
		this.listeners.add(0, l);;
	}

	/**
	 * Returns if this handler is connected to Mongrel2.
	 * 
	 * @return true if connected, otherwise, false.
	 */
	public boolean isActive() {
		return this.active.get();
	}

	/**
	 * Returns the next HTTP request from Mongrel2 or null if none are
	 * available.
	 * 
	 * @return the next HTTP request or null if none available
	 */
	public HttpRequest pollRequest() {
		final byte[] data = this.requests.recv(ZMQ.NOBLOCK);
		if (data == null)
			return null;
		final HttpRequest req = new HttpRequest();
		req.parse(data);
		return req;
	}

	/**
	 * Remove a listener.
	 * 
	 * @param l
	 *            listener
	 */
	public void removeHandlerListener(final HandlerListener l) {
		this.listeners.remove(l);
	}

	/**
	 * Send a response to one or more requests. Convenience method for the
	 * sendResponse(HttpResponse, HttpRequest[]) method.
	 * 
	 * @param response
	 *            the response to send
	 * @param recipients
	 *            one or more requests to receive the response.
	 * @throws IOException
	 */
	public void sendResponse(final HttpResponse response, final Collection<HttpRequest> recipients) throws IOException {
		final HttpRequest[] r = new HttpRequest[recipients.size()];
		recipients.toArray(r);
		sendResponse(response, r);
	}

	/**
	 * Send a response to one or more requests.
	 * 
	 * @param response
	 *            the response to send
	 * @param recipients
	 *            one or more requests to receive the response.
	 * @throws IOException
	 */
	public void sendResponse(final HttpResponse response, final HttpRequest... recipients) throws IOException {

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

		// transform response
		for (final HandlerListener l : this.listeners) {
			l.beforeSendResponse(response);
		}

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(responseStr.toString().getBytes(ASCII));
		out.write(response.getPayload());
		out.close();

		// send
		this.responses.send(out.toByteArray(), 0);

	}

	/**
	 * Sets this handler active or inactive. When switching from inactive to
	 * active the necessary ZeroMQ connections will be opened to receive
	 * requests and be able to send responses to Mongrel2. When switching from
	 * active to inactive, all connections and resources are closed.
	 * 
	 * @param active
	 *            true to activate, otherwise, false
	 */
	public void setActive(final boolean active) {

		final boolean wasActive = this.active.getAndSet(active);

		if (active && !wasActive) {

			// initialize
			this.context = ZMQ.context(1);
			this.requests = this.context.socket(ZMQ.PULL);
			this.requests.setLinger(0);
			this.responses = this.context.socket(ZMQ.PUB);
			this.responses.setIdentity(this.senderId.getBytes());
			this.responses.setLinger(0);
			this.requests.connect(this.recvAddr);
			this.responses.connect(this.sendAddr);

		} else if (!active && wasActive) {

			// shutdown
			this.requests.close();
			this.requests = null;
			this.responses.close();
			this.responses = null;
			this.context.term();
			this.context = null;

		}

	}

	/**
	 * Returns the next HTTP request from Mongrel2, blocking until one arrives.
	 * 
	 * @return next HTTP request
	 */
	public HttpRequest takeRequest() {
		final HttpRequest req = new HttpRequest();
		req.parse(this.requests.recv(0));
		return req;
	}

}
