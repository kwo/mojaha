package mongrel2;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

public class HttpHandlerTest {

	@Test
	public void testDelimiters() throws Exception {

		// ---------------- 01234567890123456789012345678901234567890
		final byte[] raw = "uuiduuiduuid request-id pathpathpath 11:datadatadat,5:adata,".getBytes("US-ASCII");

		int p0 = -1;
		int p1 = -1;
		int length = 0;

		p1 = HttpHandler.findNextDelimiter(raw, p0, ' ');
		Assert.assertEquals(-1, p0);
		Assert.assertEquals(12, p1);
		Assert.assertEquals("uuiduuiduuid", new String(raw, p0 + 1, p1 - p0 - 1));

		p0 = p1;
		p1 = HttpHandler.findNextDelimiter(raw, p0, ' ');
		Assert.assertEquals(12, p0);
		Assert.assertEquals(23, p1);
		Assert.assertEquals("request-id", new String(raw, p0 + 1, p1 - p0 - 1));

		p0 = p1;
		p1 = HttpHandler.findNextDelimiter(raw, p0, ' ');
		Assert.assertEquals(23, p0);
		Assert.assertEquals(36, p1);
		Assert.assertEquals("pathpathpath", new String(raw, p0 + 1, p1 - p0 - 1));

		p0 = p1;
		p1 = HttpHandler.findNextDelimiter(raw, p0, ':');
		Assert.assertEquals(36, p0);
		Assert.assertEquals(39, p1);
		length = Integer.parseInt(new String(raw, p0 + 1, p1 - p0 - 1));
		Assert.assertEquals(11, length);
		Assert.assertEquals("datadatadat", new String(raw, p1 + 1, length));

		p0 = p1 + length + 1;
		p1 = HttpHandler.findNextDelimiter(raw, p0, ':');
		Assert.assertEquals(51, p0);
		Assert.assertEquals(53, p1);
		length = Integer.parseInt(new String(raw, p0 + 1, p1 - p0 - 1));
		Assert.assertEquals(5, length);
		Assert.assertTrue(Arrays.equals("adata".getBytes("US-ASCII"), Arrays.copyOfRange(raw, p1 + 1, p1 + 1 + length)));

	}

}
