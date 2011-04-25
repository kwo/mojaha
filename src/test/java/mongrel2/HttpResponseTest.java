package mongrel2;

import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class HttpResponseTest {

	@Test
	public void testContent() throws Exception {

		final HttpResponse rsp = new HttpResponse();

		final String msg = "test content";

		final OutputStream out = rsp.getOutputStream();
		out.write(msg.getBytes());
		out.close();

		final String contents = new String(rsp.getContent());
		Assert.assertEquals(msg, contents);

		rsp.setContentType("text/plain");
		rsp.setDateAndLengthHeaders();

		Assert.assertTrue(rsp.containsHeader("Date"));
		Assert.assertTrue(rsp.containsHeader("Content-Length"));
		Assert.assertEquals(msg.getBytes().length, Integer.parseInt(rsp.getHeader("Content-Length")));
		Assert.assertTrue(rsp.containsHeader("Content-Type"));
		Assert.assertEquals("text/plain", rsp.getHeader("Content-Type"));

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

		rsp.setExpires(cal.getTime());
		Assert.assertTrue(rsp.containsHeader("Expires"));
		Assert.assertEquals("Mon, 25 Apr 2011 20:07:30 GMT", rsp.getHeader("Expires"));

		cal.add(Calendar.MINUTE, 1);

		rsp.setExpires(cal.getTime().getTime());
		Assert.assertTrue(rsp.containsHeader("Expires"));
		Assert.assertEquals("Mon, 25 Apr 2011 20:08:30 GMT", rsp.getHeader("Expires"));

		try {
			rsp.setExpires(0, TimeUnit.MILLISECONDS);
			Assert.fail();
		} catch (final IllegalArgumentException x) {
			// ignore
		}

		try {
			final Date d1 = new Date();
			rsp.setExpires(0, TimeUnit.SECONDS, d1, d1, d1);
			Assert.fail();
		} catch (final IllegalArgumentException x) {
			// ignore
		}

		rsp.setExpires(10, TimeUnit.SECONDS, cal.getTime());
		Assert.assertTrue(rsp.containsHeader("Expires"));
		Assert.assertEquals("Mon, 25 Apr 2011 20:08:40 GMT", rsp.getHeader("Expires"));

		rsp.setExpires(10, TimeUnit.MINUTES, cal.getTime());
		Assert.assertTrue(rsp.containsHeader("Expires"));
		Assert.assertEquals("Mon, 25 Apr 2011 20:18:30 GMT", rsp.getHeader("Expires"));

		rsp.setExpires(10, TimeUnit.HOURS, cal.getTime());
		Assert.assertTrue(rsp.containsHeader("Expires"));
		Assert.assertEquals("Tue, 26 Apr 2011 06:08:30 GMT", rsp.getHeader("Expires"));

		rsp.setExpires(10, TimeUnit.DAYS, cal.getTime());
		Assert.assertTrue(rsp.containsHeader("Expires"));
		Assert.assertEquals("Thu, 05 May 2011 20:08:30 GMT", rsp.getHeader("Expires"));

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

		rsp.setLastModified(cal.getTime());
		Assert.assertTrue(rsp.containsHeader("Last-Modified"));
		Assert.assertEquals("Mon, 25 Apr 2011 19:46:30 GMT", rsp.getHeader("Last-Modified"));

		cal.add(Calendar.MINUTE, 1);

		rsp.setLastModified(cal.getTime().getTime());
		Assert.assertTrue(rsp.containsHeader("Last-Modified"));
		Assert.assertEquals("Mon, 25 Apr 2011 19:47:30 GMT", rsp.getHeader("Last-Modified"));

	}

	@Test
	public void testStatus() throws Exception {

		final HttpResponse rsp = new HttpResponse();

		rsp.setStatus(200);
		rsp.setStatusMessage("OK");

		Assert.assertEquals(200, rsp.getStatus());
		Assert.assertEquals("OK", rsp.getStatusMessage());

	}

}
