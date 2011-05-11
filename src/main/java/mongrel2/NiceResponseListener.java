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

/**
 * Listener that cleans up responses before being dispatched by the handler.
 * Adds date header, if missing; sets the content-length; adds content-type and
 * encoding, if missing.
 * 
 * @author Karl Ostendorf
 * 
 */
public class NiceResponseListener implements HandlerListener {

	private static final String DEFAULT_CONTENT_TYPE = "text/plain";
	private static final String DEFAULT_TEXT_CHARSET = "utf-8";

	@Override
	public void beforeSendResponse(final Response response) throws IOException {

		final HttpResponse rsp = (HttpResponse) response;

		// add DATE header if not already set
		if (!rsp.containsHeader(HttpHeader.DATE))
			rsp.setTimestampHeader();

		// if content is null for some reason, set it to zero-length byte array
		if (rsp.getContent() == null)
			rsp.setContent(new byte[0]);

		// set content-length header
		rsp.setIntHeader(HttpHeader.CONTENT_LENGTH, rsp.getContent().length);

		// add the default content type, if missing
		if (!rsp.containsHeader(HttpHeader.CONTENT_TYPE))
			rsp.setContentType(DEFAULT_CONTENT_TYPE);

		// if content type is text/* and missing charset, assign default charset
		if (rsp.getContentType().startsWith("text/") && rsp.getContentType().indexOf("charset") == -1)
			rsp.setContentType(rsp.getContentType() + "; charset=" + DEFAULT_TEXT_CHARSET);

	}

}
