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

import org.zeromq.ZMQ;

/**
 * Simple App to display version information about Mojaha.
 * 
 * @author Karl Ostendorf
 * 
 */
public class App {

	/**
	 * Class to retrieve the ZeroMQ version as it isn't exposed pubicly by jzmq.
	 * 
	 */
	private static class ZmqVersion extends ZMQ {
		public static int[] version() {
			final int major = version_major();
			final int minor = version_minor();
			final int patch = version_patch();
			return new int[] { major, minor, patch };
		}
	}

	public static void main(final String[] args) throws Exception {

		final Package p = App.class.getPackage();
		final String appname = p.getSpecificationTitle();
		final String versionMaven = p.getSpecificationVersion();
		final String[] version = p.getImplementationVersion().split(" ", 2);

		final int[] v = ZmqVersion.version();

		System.out.printf("%s version:      %s.%s.%s%n", "ZeroMQ", v[0], v[1], v[2]);
		System.out.printf("%s version:      %s%n", appname, versionMaven);
		System.out.printf("%s build time:   %s%n", appname, version[1]);
		System.out.printf("%s build commit: %s%n", appname, version[0]);

	}

}
