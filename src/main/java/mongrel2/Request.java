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

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Generic request as received from mongrel2.
 * 
 * @author Karl Ostendorf
 * 
 */
public class Request {

	public static final String ATTR_REQUEST_ID = "request-id";
	public static final String ATTR_SENDER_ADDR = "sender-addr";

	protected static final Charset ASCII = Charset.forName("US-ASCII");
	protected static final Charset UTF8 = Charset.forName("UTF-8");

	private static int findNextDelimiter(final byte[] raw, final int last, final char delimiter) {
		final int start = last + 1;
		for (int i = start; i < raw.length; i++)
			if (raw[i] == delimiter)
				return i;
		return -1;
	}

	protected final Map<String, Object> attributes;
	protected byte[] content = new byte[0];
	protected final Map<String, String[]> headers; // all keys uppercase
	protected final Map<String, String> headersOriginalKeyNames;

	public Request() {
		this.attributes = new HashMap<String, Object>();
		this.headers = new HashMap<String, String[]>();
		this.headersOriginalKeyNames = new HashMap<String, String>();
	}

	public boolean containsHeader(final String name) {
		if (name == null)
			return false;
		return this.headers.containsKey(name.toUpperCase());
	}

	public Object getAttribute(final String name) {
		return this.attributes.get(name);
	}

	public Iterable<String> getAttributeNames() {
		return this.attributes.keySet();
	}

	public byte[] getContent() {
		return this.content;
	}

	/**
	 * Returns the first value for the header. If there are multiple values for
	 * a header, use {@link #getHeaderValues(String)}
	 * 
	 * @param name
	 * @return the first value for the named header
	 */
	public String getHeader(final String name) {
		final String key = name;
		if (containsHeader(key))
			return getHeaderValues(key)[0];
		return null;
	}

	public Iterable<String> getHeaderNames() {
		return this.headersOriginalKeyNames.values();
	}

	public String[] getHeaderValues(final String name) {
		if (name == null)
			return null;
		return this.headers.get(name.toUpperCase());
	}

	public String getRequestId() {
		return (String) getAttribute(ATTR_REQUEST_ID);
	}

	public String getSenderAddr() {
		return (String) getAttribute(ATTR_SENDER_ADDR);
	}

	protected void addHeader(final String name, final String value) {
		if (!containsHeader(name)) {
			setHeader(name, value);
		} else {
			final String[] values = getHeaderValues(name);
			final String[] newValues = new String[values.length + 1];
			for (int i = 0; i < values.length; i++)
				newValues[i] = values[i];
			newValues[newValues.length - 1] = value;
			setHeader(name, newValues);
		}
	}

	protected void parse(final byte[] raw) {

		// Mongrel2 sends requests formatted as follows:
		// UUID ID PATH SIZE:HEADERS,SIZE:BODY,

		try {
			System.out.write(raw);
			System.out.println();
		} catch (final Exception x) {
			// ignore
		}

		int p0 = -1;
		int p1 = -1;
		int length = 0;

		// sender addr
		p1 = findNextDelimiter(raw, p0, ' ');
		setSenderAddr(new String(raw, p0 + 1, p1 - p0 - 1, ASCII));

		// request-id
		p0 = p1;
		p1 = findNextDelimiter(raw, p0, ' ');
		setRequestId(new String(raw, p0 + 1, p1 - p0 - 1, ASCII));

		// matching path
		p0 = p1;
		p1 = findNextDelimiter(raw, p0, ' ');
		// ignore: matching path is the whole path,
		// not the handler path definied in the mongrel2 config

		// headers
		p0 = p1;
		p1 = findNextDelimiter(raw, p0, ':');
		length = Integer.parseInt(new String(raw, p0 + 1, p1 - p0 - 1));
		final String jsonHeaders = new String(raw, p1 + 1, length, UTF8);
		try {
			final JSONObject headers = new JSONObject(jsonHeaders);
			@SuppressWarnings("unchecked")
			final Iterator<String> i = headers.keys();
			while (i.hasNext()) {
				final String key = i.next();
				final String value = headers.getString(key);
				// System.out.printf("%s: %s%n", key, value);
				addHeader(key, value);
			}
		} catch (final JSONException x) {
			throw new RuntimeException("Cannot parse Json headers: " + jsonHeaders);
		}

		// content
		p0 = p1 + length + 1;
		p1 = findNextDelimiter(raw, p0, ':');
		length = Integer.parseInt(new String(raw, p0 + 1, p1 - p0 - 1));
		setContent(Arrays.copyOfRange(raw, p1 + 1, p1 + 1 + length));

	}

	protected void removeAttribute(final String name) {
		this.attributes.remove(name);
	}

	protected void setAttribute(final String name, final Object value) {
		this.attributes.put(name, value);
	}

	protected void setContent(final byte[] content) {
		this.content = content;
	}

	protected void setHeader(final String name, final String value) {
		setHeader(name, new String[] { value });
	}

	protected void setHeader(final String name, final String[] values) {
		this.headersOriginalKeyNames.put(name.toUpperCase(), name);
		this.headers.put(name.toUpperCase(), values);
	}

	protected void setRequestId(final String requestId) {
		setAttribute(ATTR_REQUEST_ID, requestId);
	}

	protected void setSenderAddr(final String senderAddr) {
		setAttribute(ATTR_SENDER_ADDR, senderAddr);
	}

}
