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

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Adds etags to responses.
 * 
 * @author Karl Ostendorf
 * 
 */
public class EtagListener implements HandlerListener {

	public static final String ALGO_MD5 = "MD5";
	public static final String ALGO_SHA = "SHA";

	private static String digest(final String algorithm, final byte[] input) {

		try {
			final MessageDigest digest = MessageDigest.getInstance(algorithm);
			return toHexString(digest.digest(input));
		} catch (final NoSuchAlgorithmException e) {
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

	private final String algorithm;

	public EtagListener() {
		this(ALGO_MD5);
	}

	public EtagListener(final String algorithm) {
		this.algorithm = algorithm;
		// immediately test if the algorithm is supported by the JVM
		try {
			MessageDigest.getInstance(algorithm);
		} catch (final NoSuchAlgorithmException e) {
			throw new InternalError("JVM does not support algorithm: " + algorithm);
		}
	}

	@Override
	public void beforeSendResponse(final Response response) throws IOException {

		final HttpResponse rsp = (HttpResponse) response;

		if (rsp.getContent() == null || rsp.getContent().length == 0)
			return;

		if (!rsp.containsHeader(HttpHeader.ETAG)) {
			final StringBuilder value = new StringBuilder("\"\"");
			value.insert(1, digest(this.algorithm, rsp.getContent()));
			rsp.setHeader(HttpHeader.ETAG, value.toString());
		}

	}

}
