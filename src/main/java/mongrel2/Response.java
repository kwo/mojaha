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
 * Generic response to send to Mongrel2.
 * 
 * @author Karl Ostendorf
 * 
 */
public class Response {

	protected byte[] payload = new byte[0];

	public byte[] getPayload() {
		return this.payload;
	}

	public void setPayload(final byte[] payload) {
		this.payload = payload;
	}

	/**
	 * Called before a response is dispatched back to Mongrel2.
	 */
	protected void transform() throws IOException {
		// default implementation does nothing
		// setPayload(getPayload());
	}

}
