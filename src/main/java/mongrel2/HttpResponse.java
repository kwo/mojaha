package mongrel2;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Response implementation that adds common http headers during the
 * transformation.
 * 
 * @author Karl Ostendorf
 * 
 */
public class HttpResponse extends BareHttpResponse {

	private static final String DEFAULT_CONTENT_TYPE = "text/plain";
	private static final String DEFAULT_TEXT_CHARSET = "utf-8";
	private static final String ETAG_DIGEST_ALGORITHM = "MD5";

	private static String digest(final String algorithm, final byte[] input) {
		try {
			final MessageDigest digest = MessageDigest.getInstance(algorithm);
			return toHexString(digest.digest(input));
		} catch (final NoSuchAlgorithmException x) {
			throw new InternalError("JVM does not support algorithm: " + algorithm);
		}
	}

	private static String toHexString(final byte[] bytes) {
		final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		final char[] hexChars = new char[bytes.length * 2];
		for (int i = 0; i < bytes.length; i++) {
			final int n = bytes[i] & 0xFF;
			hexChars[i * 2] = hexArray[n / 16];
			hexChars[i * 2 + 1] = hexArray[n % 16];
		}
		return new String(hexChars);
	}

	@Override
	protected void transform() throws IOException {

		// add DATE header if not already set
		if (!containsHeader(HttpHeader.DATE))
			setTimestampHeader();

		// if content is null, set it to zero-length byte array
		if (getContent() == null)
			setContent(new byte[0]);

		// set content-length header
		setIntHeader(HttpHeader.CONTENT_LENGTH, getContent().length);

		// add the default content type, if missing
		if (!containsHeader(HttpHeader.CONTENT_TYPE))
			setContentType(DEFAULT_CONTENT_TYPE);

		// if content type is text/* and no charset, assign default charset
		if (getContentType().startsWith("text/") && getContentType().indexOf("charset") == -1)
			setContentType(getContentType() + "; charset=" + DEFAULT_TEXT_CHARSET);

		// add etag
		if (!containsHeader(HttpHeader.ETAG)) {
			final StringBuilder value = new StringBuilder("\"\"");
			value.insert(1, digest(ETAG_DIGEST_ALGORITHM, getContent()));
			setHeader(HttpHeader.ETAG, value.toString());
		}

		super.transform();

	}

}
