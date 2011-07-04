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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * 
 * BaseHttpRequest which does nothing at the moment.
 * 
 * @author Karl Ostendorf
 * 
 */
public class BaseHttpRequest extends Request {

	private static final String H_METHOD = "METHOD";
	private static final String H_PATH = "PATH";
	private static final String H_PATTERN = "PATTERN";
	private static final String H_PROTOCOL = "VERSION";
	private static final String H_QUERY_STRING = "QUERY";

	private final SimpleDateFormat df;
	private final Map<String, String[]> params;
	private String pathinfo = null;
	private String requestURL = null;
	private String scheme = null;
	private boolean secure = false;
	private String serverName = null;
	private int serverPort = 0;
	private String servletPath = null;

	public BaseHttpRequest() {
		this.params = new HashMap<String, String[]>();
		this.df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		this.df.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public boolean containsParameter(final String name) {
		return this.params.containsKey(name);
	}

	/**
	 * Returns the value of the Content-Length header, if present, otherwise
	 * zero.
	 * 
	 * @return Value of the Content-Length header
	 */
	public int getContentLength() {
		return getIntHeader(HttpHeader.CONTENT_LENGTH);
	}

	/**
	 * Returns the value of the Content-Type header, if present.
	 * 
	 * @return Value of the Content-Type header
	 */
	public String getContentType() {
		return getHeader(HttpHeader.CONTENT_TYPE);
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

	public String getRequestURI() {
		return getHeader(H_PATH);
	}

	public StringBuffer getRequestURL() {
		return new StringBuffer(this.requestURL);
	}

	public String getScheme() {
		return this.scheme;
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

	protected void addDateHeader(final String name, final long value) {
		addHeader(name, this.df.format(value));
	}

	protected void addIntHeader(final String name, final int value) {
		addHeader(name, Integer.toString(value));
	}

	protected void addParameter(final String name, final String value) {
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

	@Override
	protected void parse(final byte[] raw) {

		super.parse(raw);

		// calculate fields

		// scheme
		this.scheme = getProtocol().split("/")[0].toLowerCase();

		// secure
		this.secure = (getScheme().equals("https"));

		// host and port
		final String[] hostport = getHeader("host").split(":");
		this.serverName = hostport[0];
		this.serverPort = Integer.parseInt(hostport[1]);

		// servlet path: handler path with out the pattern
		final int posPatternStart = getHeader(H_PATTERN).indexOf('(');
		if (posPatternStart == -1) {
			this.servletPath = getHeader(H_PATTERN);
		} else {
			this.servletPath = getHeader(H_PATTERN).substring(0, posPatternStart);
		}

		// path info: URI - PATTERN
		this.pathinfo = getRequestURI().substring(getServletPath().length());

		// requestURL
		final StringBuilder requestURL = new StringBuilder();
		requestURL.append(getScheme());
		requestURL.append("://");
		requestURL.append(getServerName());
		if ((getScheme().equals("http") && getServerPort() != 80)
				|| (getScheme().equals("https") && getServerPort() != 443)) {
			requestURL.append(':');
			requestURL.append(getServerPort());
		}
		requestURL.append(getRequestURI());
		this.requestURL = requestURL.toString();

		// parameters
		if (getQueryString() != null && getQueryString().length() > 0) {
			try {
				final String[] paramEntries = getQueryString().split("&");
				for (final String entry : paramEntries) {
					final String[] kv = entry.split("=");
					final String key = URLDecoder.decode(kv[0], UTF8.name());
					final String value = URLDecoder.decode(kv[1], UTF8.name());
					addParameter(key, value);
				}
			} catch (final UnsupportedEncodingException x) {
				throw new InternalError("JVM does not support " + UTF8.name());
			}
		}

	}

	protected void setContentLength(final int size) {
		setIntHeader(HttpHeader.CONTENT_LENGTH, size);
	}

	protected void setContentType(final String mimetype) {
		setHeader(HttpHeader.CONTENT_TYPE, mimetype);
	}

	protected void setDateHeader(final String name, final long date) {
		setHeader(name, this.df.format(date));
	}

	protected void setIntHeader(final String name, final int value) {
		setHeader(name, Integer.toString(value));
	}

	protected void setParameter(final String name, final String value) {
		setParameter(name, new String[] { value });
	}

	protected void setParameter(final String name, final String[] values) {
		this.params.put(name, values);
	}

}
