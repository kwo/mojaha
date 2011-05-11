package mongrel2;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

public class HttpHandlerTest {

	@Test
	public void testActivate() throws Exception {
		final HttpHandler handler = new HttpHandler("test-sender", "ipc://requests", "ipc://responses");
		handler.setActive(true);
		handler.setActive(false);
	}

	@Test
	public void testTranaformerExecutionOrder() throws Exception {

		final HttpHandler handler = new HttpHandler("test-sender", "ipc://requests", "ipc://responses");

		final AtomicInteger index = new AtomicInteger();
		final String[] results = new String[2];

		final HandlerListener t1 = new HandlerListener() {
			@Override
			public void beforeSendResponse(final Response rsp) throws IOException {
				results[index.getAndIncrement()] = "t1";
			}
		};

		final HandlerListener t2 = new HandlerListener() {
			@Override
			public void beforeSendResponse(final Response rsp) throws IOException {
				results[index.getAndIncrement()] = "t2";
			}
		};

		handler.addHandlerListener(t1);
		handler.addHandlerListener(t2);

		handler.setActive(true);

		final HttpRequest req = new HttpRequest();
		req.setAttribute(Request.ATTR_REQUEST_ID, "1");
		req.setAttribute(Request.ATTR_SENDER_ADDR, "bogus-mongrel2");

		final HttpResponse rsp = new HttpResponse();

		handler.sendResponse(rsp, req);

		handler.setActive(false);

		// last shall be first
		Assert.assertEquals(2, index.get());
		Assert.assertEquals("t2", results[0]);
		Assert.assertEquals("t1", results[1]);

	}
}
