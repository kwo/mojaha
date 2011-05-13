package mongrel2;

import org.junit.Test;

public class HttpHandlerTest {

	@Test
	public void testActivate() throws Exception {
		final Mongrel2Handler handler = new Mongrel2Handler("test-sender", "ipc://requests", "ipc://responses");
		handler.setActive(true);
		handler.setActive(false);
	}

}
