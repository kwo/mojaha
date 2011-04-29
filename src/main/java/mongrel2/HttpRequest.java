package mongrel2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

public class HttpRequest {

	public static final String ATTR_REQUEST_ID = "request-id";
	public static final String ATTR_SENDER_ADDR = "sender-addr";

	private static final String H_CONTENT_LENGTH = "Content-Length";
	private static final String H_CONTENT_TYPE = "Content-Type";
	private static final String H_METHOD = "METHOD";
	private static final String H_PROTOCOL = "VERSION";

	private static final String H_QUERY_STRING = "QUERY";
	private static final String H_SERVLET_PATH = "PATTERN";
	private static final String H_URI = "URI";

	public static HttpRequest parse(final byte[] raw) throws JSONException {

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
		// ignore: matching path is the whole path,
		// not the handler path definied in the mongrel2 config

		// headers
		p0 = p1;
		p1 = findNextDelimiter(raw, p0, ':');
		length = Integer.parseInt(new String(raw, p0 + 1, p1 - p0 - 1));
		final String jsonHeaders = new String(raw, p1 + 1, length);
		// System.out.println(jsonHeaders);
		// System.out.println();
		final JSONObject headers = new JSONObject(jsonHeaders);
		@SuppressWarnings("unchecked")
		final Iterator<String> keys = headers.keys();
		while (keys.hasNext()) {
			final String key = keys.next();
			final String value = headers.getString(key);
			req.addHeader(key.toUpperCase(), value);
			// System.out.printf("%s: %s%n", key, value);
		}

		// TODO: parameters

		// content
		p0 = p1 + length + 1;
		p1 = findNextDelimiter(raw, p0, ':');
		length = Integer.parseInt(new String(raw, p0 + 1, p1 - p0 - 1));
		req.setContent(Arrays.copyOfRange(raw, p1 + 1, p1 + 1 + length));

		return req;

	}

	static int findNextDelimiter(final byte[] raw, final int last, final char delimiter) {
		final int start = last + 1;
		for (int i = start; i < raw.length; i++)
			if (raw[i] == delimiter)
				return i;
		return -1;
	}

	protected byte[] content = new byte[0];

	private final Map<String, Object> attributes;

	private String cachedRequestURI = null;

	private final SimpleDateFormat df;

	private final Map<String, String[]> headers;

	public HttpRequest() {
		this.headers = new HashMap<String, String[]>();
		this.df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		this.df.setTimeZone(TimeZone.getTimeZone("GMT"));
		this.attributes = new HashMap<String, Object>();
	}

	public boolean containsHeader(final String name) {
		return this.headers.containsKey(name);
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

	public int getContentLength() {
		return getIntHeader(H_CONTENT_LENGTH);
	}

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
	 * @return
	 */
	public String getHeader(final String name) {
		final String key = name;
		if (containsHeader(key))
			return getHeaderValues(key)[0];
		return null;
	}

	public Iterable<String> getHeaderNames() {
		return this.headers.keySet();
	}

	public String[] getHeaderValues(final String name) {
		return this.headers.get(name);
	}

	public int getIntHeader(final String name) {
		if (!containsHeader(name))
			return -1;
		return Integer.parseInt(getHeader(name));
	}

	public String getMethod() {
		return getHeader(H_METHOD);
	}

	public String getPathInfo() {
		// TODO: pathinfo
		return null;
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

		if (this.cachedRequestURI == null) {
			this.cachedRequestURI = getHeader(H_URI);
			final int qm = this.cachedRequestURI.indexOf('?');
			if (qm > -1)
				this.cachedRequestURI = this.cachedRequestURI.substring(0, qm);
		}

		return this.cachedRequestURI;

	}

	public String getRequestURL() {
		// TODO: Request URL
		return null;
	}

	public String getSenderAddr() {
		return (String) getAttribute(ATTR_SENDER_ADDR);
	}

	public String getServletPath() {
		return getHeader(H_SERVLET_PATH);
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
			this.headers.put(name, newValues);
		}
	}

	void addIntHeader(final String name, final int value) {
		addHeader(name, Integer.toString(value));
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
		this.headers.put(name, new String[] { value });
	}

	void setIntHeader(final String name, final int value) {
		setHeader(name, Integer.toString(value));
	}

	void setRequestId(final String requestId) {
		setAttribute(ATTR_REQUEST_ID, requestId);
	}

	void setSenderAddr(final String senderAddr) {
		setAttribute(ATTR_SENDER_ADDR, senderAddr);
	}

}
