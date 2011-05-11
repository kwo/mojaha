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

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * HTTP response object used to return responses to the Mongrel2 web server;
 * slightly modelled after HttpServletResponse of the Servlet API.
 * 
 * @author Karl Ostendorf
 * 
 */
public class HttpResponse extends Response {

	private static final String DEFAULT_REASON_PHRASE = "Undefined";

	private byte[] content = new byte[0];
	private final SimpleDateFormat df;
	private final Map<String, String[]> headers;
	private int statusCode = 0;
	private String statusMessage = null;

	public HttpResponse() {
		this.headers = new HashMap<String, String[]>();
		this.df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		this.df.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public void addDateHeader(final String name, final long value) {
		addHeader(name, this.df.format(value));
	}

	public void addHeader(final String name, final String value) {
		if (!containsHeader(name)) {
			setHeader(name, value);
		} else {
			final String[] values = getHeaderValues(name);
			final String[] newValues = new String[values.length + 1];
			for (int i = 0; i < values.length; i++)
				newValues[i] = values[i];
			newValues[newValues.length - 1] = value;
			this.headers.put(name, newValues);
		}
	}

	public void addIntHeader(final String name, final int value) {
		addHeader(name, Integer.toString(value));
	}

	/**
	 * Send a simple plain text response back to the requester, using the http
	 * reason phrase as the body contents.
	 * 
	 * @param status
	 */
	public void sendError(final HttpStatus status) {

		setContent(status.msg + "\n");
		setHeader("Cache-Control", "no-cache");
		setDateHeader(HttpHeader.LAST_MODIFIED, System.currentTimeMillis());

	}

	public void setContent(final byte[] content) {
		this.content = content;
	}

	/**
	 * Set the content as a string. The string will be encoded as UTF8 and the
	 * content-type will also be set to "text/plain; charset=utf-8".
	 * 
	 * @param content
	 */
	public void setContent(final String content) {
		try {
			setContent(content.getBytes("UTF-8"));
			setContentType("text/plain; charset=utf-8");
		} catch (final UnsupportedEncodingException x) {
			throw new InternalError("JVM does not support UTF8 encoding.");
		}
	}

	public void setContentLength(final int size) {
		setIntHeader(HttpHeader.CONTENT_LENGTH, size);
	}

	public void setContentType(final String mimetype) {
		setHeader(HttpHeader.CONTENT_TYPE, mimetype);
	}

	public void setDateHeader(final String name, final long date) {
		setHeader(name, this.df.format(date));
	}

	public void setExpires(final int value, final TimeUnit unit) {
		setExpires(value, unit, System.currentTimeMillis());
	}

	public void setExpires(final long date) {
		setDateHeader(HttpHeader.EXPIRES, date);
	}

	public void setExpires(final long value, final TimeUnit unit, final long startTime) {

		long time = startTime;

		switch (unit) {

		case SECONDS:
			time += (value * 1000);
			break;

		case MINUTES:
			time += (value * 1000 * 60);
			break;

		case HOURS:
			time += (value * 1000 * 60 * 60);
			break;

		case DAYS:
			time += (value * 1000 * 60 * 60 * 24);
			break;

		default:
			throw new IllegalArgumentException("Only days, hours, minutes and seconds supported.");

		}

		setExpires(time);

	}

	public void setHeader(final String name, final String value) {
		this.headers.put(name, new String[] { value });
	}

	public void setIntHeader(final String name, final int value) {
		setHeader(name, Integer.toString(value));
	}

	public void setLastModified(final long date) {
		setDateHeader(HttpHeader.LAST_MODIFIED, date);
	}

	/**
	 * Convenience method that calls {@link #setStatus(int, String)}.
	 * 
	 * @param status
	 */
	public void setStatus(final HttpStatus status) {
		setStatus(status.code, status.msg);
	}

	/**
	 * Convenience method that calls {@link #setStatus(int, String)}. The reason
	 * phrase will be set automatically for know HTTP status codes.
	 * 
	 * 
	 * @param statusCode
	 */
	public void setStatus(final int statusCode) {
		try {
			final HttpStatus status = HttpStatus.findByCode(statusCode);
			setStatus(statusCode, status.msg);
		} catch (final IllegalArgumentException x) {
			setStatus(statusCode, DEFAULT_REASON_PHRASE);
		}
	}

	/**
	 * Set the HTTP Status Code and corresponding resaon phrase.
	 * 
	 * @param sc
	 * @param sm
	 */
	public void setStatus(final int sc, final String sm) {
		this.statusCode = sc;
		this.statusMessage = sm;
	}

	protected boolean containsHeader(final String name) {
		return this.headers.containsKey(name);
	}

	protected byte[] getContent() {
		return this.content;
	}

	protected int getContentLength() {
		return getIntHeader(HttpHeader.CONTENT_LENGTH);
	}

	protected String getContentType() {
		return getHeader(HttpHeader.CONTENT_TYPE);
	}

	protected long getDateHeader(final String name) {
		if (!containsHeader(name))
			return -1;
		try {
			return this.df.parse(getHeader(name)).getTime();
		} catch (final ParseException x) {
			throw new RuntimeException(x);
		}
	}

	protected String getHeader(final String name) {
		final String key = name;
		if (containsHeader(key))
			return getHeaderValues(key)[0];
		return null;
	}

	protected Iterable<String> getHeaderNames() {
		return this.headers.keySet();
	}

	protected String[] getHeaderValues(final String name) {
		return this.headers.get(name);
	}

	protected int getIntHeader(final String name) {
		if (!containsHeader(name))
			return -1;
		return Integer.parseInt(getHeader(name));
	}

	protected int getStatus() {
		return this.statusCode;
	}

	protected String getStatusMessage() {
		return this.statusMessage;
	}

	protected void setTimestampHeader() {
		setTimestampHeader(System.currentTimeMillis());
	}

	protected void setTimestampHeader(final long time) {
		setDateHeader(HttpHeader.DATE, time);
	}

}
