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
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * HTTP request object as sent from the Mongrel2 web server; slightly modelled
 * after HttpServletRequest of the Servlet API.
 * 
 * @author Karl Ostendorf
 * 
 */
public class HttpRequest {

	public static final String ATTR_REQUEST_ID = "request-id";
	public static final String ATTR_SENDER_ADDR = "sender-addr";

	private static final Charset ASCII = Charset.forName("US-ASCII");

	private static final String H_CONTENT_LENGTH = "Content-Length";
	private static final String H_CONTENT_TYPE = "Content-Type";
	private static final String H_METHOD = "METHOD";
	private static final String H_PATH = "PATH";
	private static final String H_PATTERN = "PATTERN";
	private static final String H_PROTOCOL = "VERSION";
	private static final String H_QUERY_STRING = "QUERY";

	private static final Charset UTF8 = Charset.forName("UTF-8");

	public static HttpRequest parse(final byte[] raw) {

		// Mongrel2 sends requests formatted as follows:
		// UUID ID PATH SIZE:HEADERS,SIZE:BODY,

		// try {
		// System.out.write(raw);
		// System.out.println();
		// } catch (final Exception x) {
		// // ignore
		// }

		final HttpRequest req = new HttpRequest();

		int p0 = -1;
		int p1 = -1;
		int length = 0;

		// sender addr
		p1 = findNextDelimiter(raw, p0, ' ');
		req.setSenderAddr(new String(raw, p0 + 1, p1 - p0 - 1, ASCII));

		// request-id
		p0 = p1;
		p1 = findNextDelimiter(raw, p0, ' ');
		req.setRequestId(new String(raw, p0 + 1, p1 - p0 - 1, ASCII));

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
				req.addHeader(key, value);
			}
		} catch (final JSONException x) {
			throw new RuntimeException("Cannot parse Json headers: " + jsonHeaders);
		}

		// content
		p0 = p1 + length + 1;
		p1 = findNextDelimiter(raw, p0, ':');
		length = Integer.parseInt(new String(raw, p0 + 1, p1 - p0 - 1));
		req.setContent(Arrays.copyOfRange(raw, p1 + 1, p1 + 1 + length));

		// calculate fields

		// scheme
		req.scheme = req.getProtocol().split("/")[0].toLowerCase();

		// secure
		req.secure = (req.getScheme().equals("https"));

		// host and port
		final String[] hostport = req.getHeader("host").split(":");
		req.serverName = hostport[0];
		req.serverPort = Integer.parseInt(hostport[1]);

		// servlet path: handler path with out the pattern
		final int posPatternStart = req.getHeader(H_PATTERN).indexOf('(');
		if (posPatternStart == -1) {
			req.servletPath = req.getHeader(H_PATTERN);
		} else {
			req.servletPath = req.getHeader(H_PATTERN).substring(0, posPatternStart);
		}

		// path info: URI - PATTERN
		req.pathinfo = req.getRequestURI().substring(req.getServletPath().length());

		// requestURL
		final StringBuilder requestURL = new StringBuilder();
		requestURL.append(req.getScheme());
		requestURL.append("://");
		requestURL.append(req.getServerName());
		if ((req.getScheme().equals("http") && req.getServerPort() != 80)
				|| (req.getScheme().equals("https") && req.getServerPort() != 443)) {
			requestURL.append(':');
			requestURL.append(req.getServerPort());
		}
		requestURL.append(req.getRequestURI());
		req.requestURL = requestURL.toString();

		// parameters
		if (req.getQueryString() != null && req.getQueryString().length() > 0) {
			try {
				final String[] paramEntries = req.getQueryString().split("&");
				for (final String entry : paramEntries) {
					final String[] kv = entry.split("=");
					final String key = URLDecoder.decode(kv[0], UTF8.name());
					final String value = URLDecoder.decode(kv[1], UTF8.name());
					req.addParameter(key, value);
				}
			} catch (final UnsupportedEncodingException x) {
				throw new InternalError("JVM does not support " + UTF8.name());
			}
		}

		return req;

	}

	private static int findNextDelimiter(final byte[] raw, final int last, final char delimiter) {
		final int start = last + 1;
		for (int i = start; i < raw.length; i++)
			if (raw[i] == delimiter)
				return i;
		return -1;
	}

	private final Map<String, Object> attributes;
	private byte[] content = new byte[0];
	private final SimpleDateFormat df;
	private final Map<String, String[]> headers; // all keys uppercase
	private final Map<String, String> headersOriginalKeyNames;
	private final Map<String, String[]> params;
	private String pathinfo = null;
	private String requestURL = null;
	private String scheme = null;
	private boolean secure = false;
	private String serverName = null;
	private int serverPort = 0;
	private String servletPath = null;

	public HttpRequest() {
		this.attributes = new HashMap<String, Object>();
		this.headers = new HashMap<String, String[]>();
		this.headersOriginalKeyNames = new HashMap<String, String>();
		this.params = new HashMap<String, String[]>();
		this.df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		this.df.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public boolean containsHeader(final String name) {
		if (name == null)
			return false;
		return this.headers.containsKey(name.toUpperCase());
	}

	public boolean containsParameter(final String name) {
		return this.params.containsKey(name);
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
	 * Returns the value of the Content-Length header, if present, otherwise
	 * zero.
	 * 
	 * @return Value of the Content-Length header
	 */
	public int getContentLength() {
		return getIntHeader(H_CONTENT_LENGTH);
	}

	/**
	 * Returns the value of the Content-Type header, if present.
	 * 
	 * @return Value of the Content-Type header
	 */
	public String getContentType() {
		return getHeader(H_CONTENT_TYPE);
	}

	public long getDateHeader(final String name) {
		if (!containsHeader(name))
			return -1;
		try {
			return this.df.parse(getHeader(name)).getTime();
		} catch (final ParseException x) {
			throw new RuntimeException(x);
		}
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

	public int getIntHeader(final String name) {
		if (!containsHeader(name))
			return 0;
		return Integer.parseInt(getHeader(name));
	}

	public String getMethod() {
		return getHeader(H_METHOD);
	}

	public String getParameter(final String name) {
		final String key = name;
		if (containsParameter(key))
			return getParameterValues(key)[0];
		return null;
	}

	public Iterable<String> getParameterNames() {
		return this.params.keySet();
	}

	public String[] getParameterValues(final String name) {
		return this.params.get(name);
	}

	public String getPathInfo() {
		return this.pathinfo;
	}

	public String getProtocol() {
		return getHeader(H_PROTOCOL);
	}

	public String getQueryString() {
		return getHeader(H_QUERY_STRING);
	}

	public String getRequestId() {
		return (String) getAttribute(ATTR_REQUEST_ID);
	}

	public String getRequestURI() {
		return getHeader(H_PATH);
	}

	public StringBuffer getRequestURL() {
		return new StringBuffer(this.requestURL);
	}

	public String getScheme() {
		return this.scheme;
	}

	public String getSenderAddr() {
		return (String) getAttribute(ATTR_SENDER_ADDR);
	}

	public String getServerName() {
		return this.serverName;
	}

	public int getServerPort() {
		return this.serverPort;
	}

	/**
	 * Returns the handler path, less its pattern if any, used to match this
	 * request.
	 * 
	 * @return the handler path used to match this request
	 */
	public String getServletPath() {
		return this.servletPath;
	}

	public boolean isSecure() {
		return this.secure;
	}

	void addDateHeader(final String name, final long value) {
		addHeader(name, this.df.format(value));
	}

	void addHeader(final String name, final String value) {
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

	void addIntHeader(final String name, final int value) {
		addHeader(name, Integer.toString(value));
	}

	void addParameter(final String name, final String value) {
		if (!containsParameter(name)) {
			setParameter(name, value);
		} else {
			final String[] values = getParameterValues(name);
			final String[] newValues = new String[values.length + 1];
			for (int i = 0; i < values.length; i++)
				newValues[i] = values[i];
			newValues[newValues.length - 1] = value;
			setParameter(name, newValues);
		}
	}

	void removeAttribute(final String name) {
		this.attributes.remove(name);
	}

	void setAttribute(final String name, final Object value) {
		this.attributes.put(name, value);
	}

	void setContent(final byte[] content) {
		this.content = content;
	}

	void setContentLength(final int size) {
		setIntHeader(H_CONTENT_LENGTH, size);
	}

	void setContentType(final String mimetype) {
		setHeader(H_CONTENT_TYPE, mimetype);
	}

	void setDateHeader(final String name, final long date) {
		setHeader(name, this.df.format(date));
	}

	void setHeader(final String name, final String value) {
		setHeader(name, new String[] { value });
	}

	void setHeader(final String name, final String[] values) {
		this.headersOriginalKeyNames.put(name.toUpperCase(), name);
		this.headers.put(name.toUpperCase(), values);
	}

	void setIntHeader(final String name, final int value) {
		setHeader(name, Integer.toString(value));
	}

	void setParameter(final String name, final String value) {
		setParameter(name, new String[] { value });
	}

	void setParameter(final String name, final String[] values) {
		this.params.put(name, values);
	}

	void setRequestId(final String requestId) {
		setAttribute(ATTR_REQUEST_ID, requestId);
	}

	void setSenderAddr(final String senderAddr) {
		setAttribute(ATTR_SENDER_ADDR, senderAddr);
	}

}
