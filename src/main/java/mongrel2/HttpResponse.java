package mongrel2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

	private final ByteArrayOutputStream content;
	private final SimpleDateFormat df;
	private final Map<String, String[]> headers;
	private int statusCode = 0;
	private String statusMessage = null;

	// TODO: create resource with status code messages and automate messages

	public HttpResponse() {
		this.headers = new HashMap<String, String[]>();
		this.content = new ByteArrayOutputStream();
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
			final String[] values = this.headers.get(name);
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

	public boolean containsHeader(final String name) {
		return this.headers.containsKey(name);
	}

	public byte[] getContent() {
		return this.content.toByteArray();
	}

	public String getContentType() {
		return getHeader(H_CONTENT_TYPE);
	}

	public String getHeader(final String name) {
		if (containsHeader(name))
			return getHeaderValues(name)[0];
		return null;
	}

	public Iterable<String> getHeaderNames() {
		return this.headers.keySet();
	}

	public String[] getHeaderValues(final String name) {
		return this.headers.get(name);
	}

	public OutputStream getOutputStream() throws IOException {
		return this.content;
	}

	public int getStatus() {
		return this.statusCode;
	}

	public String getStatusMessage() {
		return this.statusMessage;
	}

	public void setContentLength(final int size) {
		setIntHeader(H_CONTENT_LENGTH, size);
	}

	public void setContentType(final String mimetype) {
		setHeader(H_CONTENT_TYPE, mimetype);
	}

	public void setDate(final Object... times) {

		if (times.length == 0) {
			setDateHeader(H_DATE, System.currentTimeMillis());
		} else if (times.length == 1) {

			final Object o = times[0];
			if (o instanceof Long) {
				setDateHeader(H_DATE, (Long) o);
			} else {
				throw new IllegalArgumentException("Optional date parameter must be of type Long.");
			}

		} else {
			throw new IllegalArgumentException("Only one optional date parameter permitted.");
		}

	}

	public void setDateHeader(final String name, final long date) {
		setHeader(name, this.df.format(date));
	}

	public void setExpires(final int value, final TimeUnit unit, final Long... times) {

		long time = System.currentTimeMillis();

		switch (times.length) {
		case 0:
			// ignore
			break;
		case 1:
			time = times[0];
			break;
		default:
			throw new IllegalArgumentException("Only one optional date argument permitted.");
		}

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

		setDateHeader(H_EXPIRES, time);

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
	}

	public void setStatus(final int sc, final String sm) {
		this.statusCode = sc;
		this.statusMessage = sm;
	}

	public void setStatusMessage(final String statusMessage) {
		this.statusMessage = statusMessage;
	}

}
