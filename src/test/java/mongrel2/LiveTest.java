package mongrel2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.apache.commons.codec.binary.Base64;

public class LiveTest {

	private static final String HTTP_RECV = "tcp://127.0.0.1:22201";
	private static final String HTTP_SEND = "tcp://127.0.0.1:44401";
	private static final String JSON_RECV = "tcp://127.0.0.1:22202";
	private static final String JSON_SEND = "tcp://127.0.0.1:44402";
	private static final InetSocketAddress SERVER_ADDR = new InetSocketAddress("localhost", 65432);
	private static final String XML_RECV = "tcp://127.0.0.1:22203";

	private static final String XML_SEND = "tcp://127.0.0.1:44403";

	private static byte[] readInputStream(final InputStream in, final boolean keepopen) throws IOException {

		final ByteArrayOutputStream out = new ByteArrayOutputStream();

		int len = 0;
		final byte[] buf = new byte[256];
		while ((in.available() > 0) && (len = in.read(buf)) > -1)
			out.write(buf, 0, len);
		out.close();
		if (!keepopen)
			in.close();

		return out.toByteArray();

	}

	public void testHttpHandler() throws Exception {

		final ExecutorService executor = Executors.newCachedThreadPool();

		final Mongrel2Handler handler = new Mongrel2Handler(UUID.randomUUID().toString(), HTTP_RECV, HTTP_SEND);
		// handler.setLevel(Level.DEBUG);
		handler.setActive(true);

		final Runnable app = new Runnable() {
			@Override
			public void run() {

				while (handler.isActive()) {
					try {
						final HttpRequest req = new HttpRequest();
						final HttpResponse rsp = new HttpResponse();
						handler.takeRequest(req);
						rsp.setContent("Hello, world!\n");
						rsp.setStatus(HttpStatus.OK);
						handler.sendResponse(rsp, req);
					} catch (final IOException x) {
						Assert.fail(x.toString());
					}
				} // while

			}
		};

		executor.submit(app);

		final URL u = new URL("http://" + SERVER_ADDR.getHostName() + ":" + SERVER_ADDR.getPort() + "/");
		final HttpURLConnection http = (HttpURLConnection) u.openConnection();
		Assert.assertEquals(200, http.getResponseCode());

		// handler.setActive(false);
		executor.shutdownNow();

	}

	public void testJsonHandler() throws Exception {

		final byte[] contents = "{'msg': 'greetings mychat'}".getBytes();

		final Mongrel2Handler handler = new Mongrel2Handler(UUID.randomUUID().toString(), JSON_RECV, JSON_SEND);
		// handler.setLevel(Level.DEBUG);
		handler.setActive(true);

		final Socket s = new Socket(SERVER_ADDR.getAddress(), SERVER_ADDR.getPort());
		final InputStream in = s.getInputStream();
		final OutputStream out = s.getOutputStream();

		// send message
		out.write("@mychat ".getBytes());
		out.write(contents);
		out.write(0);
		out.flush();

		// wait to receive message at handler
		final Request req = new Request();
		handler.takeRequest(req);

		// verify correctness
		Assert.assertEquals("server content length unequal", contents.length, req.getContent().length);
		Assert.assertTrue("server contents do not match", Arrays.equals(contents, req.getContent()));

		// echo message back to client
		final Response rsp = new Response();
		rsp.setPayload(contents);
		Assert.assertTrue(Arrays.equals(contents, rsp.getPayload()));
		handler.sendResponse(rsp, req);

		// read out message at client
		final byte[] msg1 = readInputStream(in, true);
		Assert.assertTrue("zero-length message", msg1.length > 0);
		// decode base64 message
		final byte[] msg = Base64.decodeBase64(msg1);

		// verify correctness
		Assert.assertEquals("client content length unequal", contents.length, msg.length);
		Assert.assertTrue("client contents do not match", Arrays.equals(contents, msg));

		out.close();
		in.close();
		s.close();

		// handler.setActive(false);

	}

	public void testXmlHandler() throws Exception {

		final byte[] contents = "<myxml><msg>hello xml</msg></myxml>".getBytes();

		final Mongrel2Handler handler = new Mongrel2Handler(UUID.randomUUID().toString(), XML_RECV, XML_SEND);
		// handler.setLevel(Level.DEBUG);
		handler.setActive(true);

		final Socket s = new Socket(SERVER_ADDR.getAddress(), SERVER_ADDR.getPort());
		final InputStream in = s.getInputStream();
		final OutputStream out = s.getOutputStream();

		// send message
		// out.write("<myxml>".getBytes());
		out.write(contents);
		out.write(0);
		out.flush();

		// wait to receive message at handler
		final Request req = new Request();
		handler.takeRequest(req);

		// verify correctness
		Assert.assertEquals("server content length unequal", contents.length, req.getContent().length);
		Assert.assertTrue("server contents do not match", Arrays.equals(contents, req.getContent()));

		// echo message back to client
		final Response rsp = new Response();
		rsp.setPayload(contents);
		Assert.assertTrue(Arrays.equals(contents, rsp.getPayload()));
		handler.sendResponse(rsp, req);
		// handler.sendResponse(rsp, req);

		// read out message at client
		final byte[] msg1 = readInputStream(in, true);
		Assert.assertTrue("zero-length message", msg1.length > 0);
		// decode base64 message
		final byte[] msg = Base64.decodeBase64(msg1);

		// verify correctness
		Assert.assertEquals("client content length unequal", contents.length, msg.length);
		Assert.assertTrue("client contents do not match", Arrays.equals(contents, msg));

		out.close();
		in.close();
		s.close();

		// handler.setActive(false);

	}

}
