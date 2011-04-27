package mongrel2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public abstract class BaseHttpObject {

	protected static final String H_CONTENT_LENGTH = "Content-Length";
	protected static final String H_CONTENT_TYPE = "Content-Type";
	protected static final String H_DATE = "Date";

	protected byte[] content = new byte[0];
	private final SimpleDateFormat df;
	private final Map<String, String[]> headers;

	protected BaseHttpObject() {
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

	public boolean containsHeader(final String name) {
		return this.headers.containsKey(name);
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

	public void setHeader(final String name, final String value) {
		this.headers.put(name, new String[] { value });
	}

	public void setIntHeader(final String name, final int value) {
		setHeader(name, Integer.toString(value));
	}

}
