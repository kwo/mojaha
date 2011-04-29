package mongrel2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class HttpResponse {

	private static final String H_CONTENT_LENGTH = "Content-Length";
	private static final String H_CONTENT_TYPE = "Content-Type";
	private static final String H_DATE = "Date";
	private static final String H_EXPIRES = "Expires";
	private static final String H_LAST_MODIFIED = "Last-Modified";
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

	public void setContent(final byte[] content) {
		this.content = content;
	}

	public void setContentLength(final int size) {
		setIntHeader(H_CONTENT_LENGTH, size);
	}

	public void setContentType(final String mimetype) {
		setHeader(H_CONTENT_TYPE, mimetype);
	}

	public void setDateHeader(final String name, final long date) {
		setHeader(name, this.df.format(date));
	}

	public void setExpires(final int value, final TimeUnit unit) {
		setExpires(value, unit, System.currentTimeMillis());
	}

	public void setExpires(final int value, final TimeUnit unit, final long startTime) {

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

	public void setExpires(final long date) {
		setDateHeader(H_EXPIRES, date);
	}

	public void setHeader(final String name, final String value) {
		this.headers.put(name, new String[] { value });
	}

	public void setIntHeader(final String name, final int value) {
		setHeader(name, Integer.toString(value));
	}

	public void setLastModified(final long date) {
		setDateHeader(H_LAST_MODIFIED, date);
	}

	public void setStatus(final int statusCode) {
		this.statusCode = statusCode;
		// TODO: add status message
	}

	public void setStatus(final int sc, final String sm) {
		this.statusCode = sc;
		this.statusMessage = sm;
	}

	public void setTimestampHeader() {
		setTimestampHeader(System.currentTimeMillis());
	}

	public void setTimestampHeader(final long time) {
		setDateHeader(H_DATE, time);
	}

	boolean containsHeader(final String name) {
		return this.headers.containsKey(name);
	}

	byte[] getContent() {
		return this.content;
	}

	int getContentLength() {
		return getIntHeader(H_CONTENT_LENGTH);
	}

	String getContentType() {
		return getHeader(H_CONTENT_TYPE);
	}

	long getDateHeader(final String name) {
		if (!containsHeader(name))
			return -1;
		try {
			return this.df.parse(getHeader(name)).getTime();
		} catch (final ParseException x) {
			throw new RuntimeException(x);
		}
	}

	String getHeader(final String name) {
		final String key = name;
		if (containsHeader(key))
			return getHeaderValues(key)[0];
		return null;
	}

	Iterable<String> getHeaderNames() {
		return this.headers.keySet();
	}

	String[] getHeaderValues(final String name) {
		return this.headers.get(name);
	}

	int getIntHeader(final String name) {
		if (!containsHeader(name))
			return -1;
		return Integer.parseInt(getHeader(name));
	}

	int getStatus() {
		return this.statusCode;
	}

	String getStatusMessage() {
		return this.statusMessage;
	}

}
