package mongrel2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
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

	public boolean containsHeader(final String name) {
		return this.headers.containsKey(name);
	}

	public byte[] getContent() {
		return this.content.toByteArray();
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

	/**
	 * Set date and content-length headers.
	 */
	public void setDateAndLengthHeaders() {

		setDateHeader(H_DATE, System.currentTimeMillis());
		setIntHeader(H_CONTENT_LENGTH, getContent().length);

	}

	public void setContentType(final String mimetype) {
		setHeader(H_CONTENT_TYPE, mimetype);
	}

	public void setDateHeader(final String name, final Date date) {
		setHeader(name, this.df.format(date));
	}

	public void setDateHeader(final String name, final long date) {
		setDateHeader(name, new Date(date));
	}

	public void setExpires(final Date date) {
		setDateHeader(H_EXPIRES, date);
	}

	public void setExpires(final int value, final TimeUnit unit, final Date... times) {

		long time = System.currentTimeMillis();

		switch (times.length) {
		case 0:
			// ignore
			break;
		case 1:
			time = times[0].getTime();
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

	public void setLastModified(final Date date) {
		setDateHeader(H_LAST_MODIFIED, date);
	}

	public void setLastModified(final long date) {
		setDateHeader(H_LAST_MODIFIED, date);
	}

	public void setStatus(final int statusCode) {
		this.statusCode = statusCode;
	}

	public void setStatusMessage(final String statusMessage) {
		this.statusMessage = statusMessage;
	}

}
