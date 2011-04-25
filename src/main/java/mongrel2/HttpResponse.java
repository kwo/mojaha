package mongrel2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

	private final ByteArrayOutputStream content;
	private final Map<String, String[]> headers;
	private int statusCode = 0;
	private String statusMessage = null;

	// TODO: create resource with status code messages and automate messages

	public HttpResponse() {
		this.headers = new HashMap<String, String[]>();
		this.content = new ByteArrayOutputStream();
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

	public void setHeader(final String name, final String value) {
		this.headers.put(name, new String[] { value });
	}

	public void setStatus(final int statusCode) {
		this.statusCode = statusCode;
	}

	public void setStatusMessage(final String statusMessage) {
		this.statusMessage = statusMessage;
	}

}
