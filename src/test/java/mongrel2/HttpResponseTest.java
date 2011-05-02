package mongrel2;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class HttpResponseTest {

	@Test
	public void testAddDateHeader() throws Exception {

		final Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, Calendar.APRIL);
		cal.set(Calendar.DATE, 25);
		cal.set(Calendar.HOUR_OF_DAY, 22);
		cal.set(Calendar.MINUTE, 07);
		cal.set(Calendar.SECOND, 30);
		cal.set(Calendar.MILLISECOND, 0);

		final HttpResponse rsp = new HttpResponse();
		rsp.setDateHeader("n1", cal.getTimeInMillis());
		rsp.addDateHeader("n1", cal.getTimeInMillis());

		Assert.assertEquals(2, rsp.getHeaderValues("n1").length);
		Assert.assertEquals("Mon, 25 Apr 2011 20:07:30 GMT", rsp.getHeaderValues("n1")[0]);
		Assert.assertEquals("Mon, 25 Apr 2011 20:07:30 GMT", rsp.getHeaderValues("n1")[1]);

	}

	@Test
	public void testAddIntHeader() throws Exception {

		final HttpResponse rsp = new HttpResponse();
		rsp.setIntHeader("n1", 1);
		rsp.addIntHeader("n1", 2);

		Assert.assertEquals(2, rsp.getHeaderValues("n1").length);
		Assert.assertEquals("1", rsp.getHeaderValues("n1")[0]);
		Assert.assertEquals("2", rsp.getHeaderValues("n1")[1]);

	}

	@Test
	public void testContent() throws Exception {

		final HttpResponse rsp = new HttpResponse();

		final String msg = "test content";

		rsp.setContent(msg.getBytes());

		final String contents = new String(rsp.getContent());
		Assert.assertEquals(msg, contents);

		rsp.setContentType("text/plain");
		rsp.setContentLength(msg.getBytes().length);

		Assert.assertTrue(rsp.containsHeader("Content-Length"));
		Assert.assertEquals(msg.getBytes().length, Integer.parseInt(rsp.getHeader("Content-Length")));
		Assert.assertTrue(rsp.containsHeader("Content-Type"));
		Assert.assertEquals("text/plain", rsp.getContentType());

	}

	@Test
	public void testDate() throws Exception {

		final HttpResponse rsp1 = new HttpResponse();
		rsp1.setTimestampHeader();
		Assert.assertTrue(rsp1.containsHeader("Date"));

		final Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, Calendar.APRIL);
		cal.set(Calendar.DATE, 25);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 42);
		cal.set(Calendar.SECOND, 30);
		cal.set(Calendar.MILLISECOND, 0);

		final HttpResponse rsp4 = new HttpResponse();
		rsp4.setTimestampHeader(cal.getTime().getTime());
		Assert.assertTrue(rsp4.containsHeader("Date"));
		Assert.assertEquals("Mon, 25 Apr 2011 21:42:30 GMT", rsp4.getHeader("Date"));
		Assert.assertEquals(cal.getTimeInMillis(), rsp4.getDateHeader("Date"));

	}

	@Test
	public void testExpires() throws Exception {

		final Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, Calendar.APRIL);
		cal.set(Calendar.DATE, 25);
		cal.set(Calendar.HOUR_OF_DAY, 22);
		cal.set(Calendar.MINUTE, 07);
		cal.set(Calendar.SECOND, 30);
		cal.set(Calendar.MILLISECOND, 0);

		final HttpResponse rsp = new HttpResponse();

		rsp.setExpires(cal.getTime().getTime());
		Assert.assertTrue(rsp.containsHeader("Expires"));
		Assert.assertEquals("Mon, 25 Apr 2011 20:07:30 GMT", rsp.getHeader("Expires"));

		rsp.setExpires(10, TimeUnit.SECONDS, cal.getTime().getTime());
		Assert.assertTrue(rsp.containsHeader("Expires"));
		Assert.assertEquals("Mon, 25 Apr 2011 20:07:40 GMT", rsp.getHeader("Expires"));

		rsp.setExpires(10, TimeUnit.MINUTES, cal.getTime().getTime());
		Assert.assertTrue(rsp.containsHeader("Expires"));
		Assert.assertEquals("Mon, 25 Apr 2011 20:17:30 GMT", rsp.getHeader("Expires"));

		rsp.setExpires(10, TimeUnit.HOURS, cal.getTime().getTime());
		Assert.assertTrue(rsp.containsHeader("Expires"));
		Assert.assertEquals("Tue, 26 Apr 2011 06:07:30 GMT", rsp.getHeader("Expires"));

		rsp.setExpires(10, TimeUnit.DAYS, cal.getTime().getTime());
		Assert.assertTrue(rsp.containsHeader("Expires"));
		Assert.assertEquals("Thu, 05 May 2011 20:07:30 GMT", rsp.getHeader("Expires"));

	}

	@Test(expected = IllegalArgumentException.class)
	public void testExpiresMilliseconds() {
		final HttpResponse rsp = new HttpResponse();
		rsp.setExpires(0, TimeUnit.MILLISECONDS);
	}

	@Test
	public void testHeaders() throws Exception {

		final HttpResponse rsp = new HttpResponse();

		// test setting a header
		rsp.setHeader("h1", "v1");
		Assert.assertEquals("v1", rsp.getHeader("h1"));
		Assert.assertEquals(1, rsp.getHeaderValues("h1").length);
		Assert.assertEquals("v1", rsp.getHeaderValues("h1")[0]);

		// test contains
		Assert.assertTrue(rsp.containsHeader("h1"));
		Assert.assertFalse(rsp.containsHeader("h0"));

		// non-existent headers are null
		Assert.assertNull(rsp.getHeader("h2"));

		// test setting a 2d header
		rsp.setHeader("h2", "v2");
		Assert.assertEquals("v2", rsp.getHeader("h2"));
		Assert.assertEquals(1, rsp.getHeaderValues("h2").length);
		Assert.assertEquals("v2", rsp.getHeaderValues("h2")[0]);
		Assert.assertTrue(rsp.containsHeader("h2"));

		// test adding to a header
		rsp.addHeader("h2", "v2-2");
		Assert.assertEquals("v2", rsp.getHeader("h2"));
		Assert.assertEquals(2, rsp.getHeaderValues("h2").length);
		Assert.assertEquals("v2", rsp.getHeaderValues("h2")[0]);
		Assert.assertEquals("v2-2", rsp.getHeaderValues("h2")[1]);

		// test overwriting a header
		rsp.setHeader("h2", "v3");
		Assert.assertEquals("v3", rsp.getHeader("h2"));
		Assert.assertEquals(1, rsp.getHeaderValues("h2").length);
		Assert.assertEquals("v3", rsp.getHeaderValues("h2")[0]);

		// test initializing a header with add
		rsp.addHeader("h3", "v4");
		Assert.assertEquals("v4", rsp.getHeader("h3"));
		Assert.assertEquals(1, rsp.getHeaderValues("h3").length);
		Assert.assertEquals("v4", rsp.getHeaderValues("h3")[0]);
		Assert.assertTrue(rsp.containsHeader("h3"));

		int counter = 0;
		for (@SuppressWarnings("unused")
		final String name : rsp.getHeaderNames()) {
			counter++;
		}
		Assert.assertEquals(3, counter);

	}

	@Test
	public void testLastModified() throws Exception {

		final Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, Calendar.APRIL);
		cal.set(Calendar.DATE, 25);
		cal.set(Calendar.HOUR_OF_DAY, 21);
		cal.set(Calendar.MINUTE, 46);
		cal.set(Calendar.SECOND, 30);
		cal.set(Calendar.MILLISECOND, 0);

		final HttpResponse rsp = new HttpResponse();

		rsp.setLastModified(cal.getTime().getTime());
		Assert.assertTrue(rsp.containsHeader("Last-Modified"));
		Assert.assertEquals("Mon, 25 Apr 2011 19:46:30 GMT", rsp.getHeader("Last-Modified"));

		cal.add(Calendar.MINUTE, 1);

		rsp.setLastModified(cal.getTime().getTime());
		Assert.assertTrue(rsp.containsHeader("Last-Modified"));
		Assert.assertEquals("Mon, 25 Apr 2011 19:47:30 GMT", rsp.getHeader("Last-Modified"));

	}

	@Test
	public void testReasonPhrases() throws Exception {

		final HttpResponse rsp = new HttpResponse();

		rsp.setStatus(100);
		Assert.assertEquals("Continue", rsp.getStatusMessage());

		rsp.setStatus(101);
		Assert.assertEquals("Switching Protocols", rsp.getStatusMessage());

		rsp.setStatus(200);
		Assert.assertEquals("OK", rsp.getStatusMessage());

		rsp.setStatus(201);
		Assert.assertEquals("Created", rsp.getStatusMessage());

		rsp.setStatus(202);
		Assert.assertEquals("Accepted", rsp.getStatusMessage());

		rsp.setStatus(203);
		Assert.assertEquals("Non-Authoritative Information", rsp.getStatusMessage());

		rsp.setStatus(204);
		Assert.assertEquals("No Content", rsp.getStatusMessage());

		rsp.setStatus(205);
		Assert.assertEquals("Reset Content", rsp.getStatusMessage());

		rsp.setStatus(206);
		Assert.assertEquals("Partial Content", rsp.getStatusMessage());

		rsp.setStatus(300);
		Assert.assertEquals("Multiple Choices", rsp.getStatusMessage());

		rsp.setStatus(301);
		Assert.assertEquals("Moved Permanently", rsp.getStatusMessage());

		rsp.setStatus(302);
		Assert.assertEquals("Found", rsp.getStatusMessage());

		rsp.setStatus(303);
		Assert.assertEquals("See Other", rsp.getStatusMessage());

		rsp.setStatus(304);
		Assert.assertEquals("Not Modified", rsp.getStatusMessage());

		rsp.setStatus(305);
		Assert.assertEquals("Use Proxy", rsp.getStatusMessage());

		rsp.setStatus(307);
		Assert.assertEquals("Temporary Redirect", rsp.getStatusMessage());

		rsp.setStatus(400);
		Assert.assertEquals("Bad Request", rsp.getStatusMessage());

		rsp.setStatus(401);
		Assert.assertEquals("Unauthorized", rsp.getStatusMessage());

		rsp.setStatus(402);
		Assert.assertEquals("Payment Required", rsp.getStatusMessage());

		rsp.setStatus(403);
		Assert.assertEquals("Forbidden", rsp.getStatusMessage());

		rsp.setStatus(404);
		Assert.assertEquals("Not Found", rsp.getStatusMessage());

		rsp.setStatus(405);
		Assert.assertEquals("Method Not Allowed", rsp.getStatusMessage());

		rsp.setStatus(406);
		Assert.assertEquals("Not Acceptable", rsp.getStatusMessage());

		rsp.setStatus(407);
		Assert.assertEquals("Proxy Authentication Required", rsp.getStatusMessage());

		rsp.setStatus(408);
		Assert.assertEquals("Request Time-out", rsp.getStatusMessage());

		rsp.setStatus(409);
		Assert.assertEquals("Conflict", rsp.getStatusMessage());

		rsp.setStatus(410);
		Assert.assertEquals("Gone", rsp.getStatusMessage());

		rsp.setStatus(411);
		Assert.assertEquals("Length Required", rsp.getStatusMessage());

		rsp.setStatus(412);
		Assert.assertEquals("Precondition Failed", rsp.getStatusMessage());

		rsp.setStatus(413);
		Assert.assertEquals("Request Entity Too Large", rsp.getStatusMessage());

		rsp.setStatus(414);
		Assert.assertEquals("Request-URI Too Large", rsp.getStatusMessage());

		rsp.setStatus(415);
		Assert.assertEquals("Unsupported Media Type", rsp.getStatusMessage());

		rsp.setStatus(416);
		Assert.assertEquals("Requested range not satisfiable", rsp.getStatusMessage());

		rsp.setStatus(417);
		Assert.assertEquals("Expectation Failed", rsp.getStatusMessage());

		rsp.setStatus(500);
		Assert.assertEquals("Internal Server Error", rsp.getStatusMessage());

		rsp.setStatus(501);
		Assert.assertEquals("Not Implemented", rsp.getStatusMessage());

		rsp.setStatus(502);
		Assert.assertEquals("Bad Gateway", rsp.getStatusMessage());

		rsp.setStatus(503);
		Assert.assertEquals("Service Unavailable", rsp.getStatusMessage());

		rsp.setStatus(504);
		Assert.assertEquals("Gateway Time-out", rsp.getStatusMessage());

		rsp.setStatus(505);
		Assert.assertEquals("HTTP Version not supported", rsp.getStatusMessage());

		rsp.setStatus(1000);
		Assert.assertEquals("Undefined", rsp.getStatusMessage());

		rsp.setStatus(400, "Nice Try Buddy");
		Assert.assertEquals("Nice Try Buddy", rsp.getStatusMessage());

	}

	@Test
	public void testStatus() throws Exception {

		final HttpResponse rsp = new HttpResponse();

		rsp.setStatus(200, "OK");

		Assert.assertEquals(200, rsp.getStatus());
		Assert.assertEquals("OK", rsp.getStatusMessage());

		rsp.setStatus(404, "Not Found");
		Assert.assertEquals(404, rsp.getStatus());
		Assert.assertEquals("Not Found", rsp.getStatusMessage());

	}

}
