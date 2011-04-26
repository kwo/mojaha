package mongrel2;

import java.util.concurrent.TimeUnit;

public class HttpResponse extends BaseHttpObject {

	private static final String H_EXPIRES = "Expires";
	private static final String H_LAST_MODIFIED = "Last-Modified";

	private int statusCode = 0;
	private String statusMessage = null;

	public HttpResponse() {
	}

	public int getStatus() {
		return this.statusCode;
	}

	public String getStatusMessage() {
		return this.statusMessage;
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

}
