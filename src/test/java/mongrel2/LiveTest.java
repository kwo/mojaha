package mongrel2;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LiveTest {

	private static final String CFG_FILE = "livetest.conf";
	private static ExecutorService executor = null;
	private static String m2sh = null;
	private static final String RECV_ADDR = "tcp://127.0.0.1:22201";
	private static final String SEND_ADDR = "tcp://127.0.0.1:22202";
	private static Process server = null;
	private static File workdir = null;

	@BeforeClass
	public static void setup() throws Exception {

		// find m2sh
		m2sh = findM2sh();
		Assert.assertNotNull(m2sh);

		// create workdir
		workdir = createWorkingDirectory();
		Assert.assertTrue(workdir.exists());
		Assert.assertTrue(workdir.isDirectory());

		executor = Executors.newCachedThreadPool();

		// shell to m2sh, load configuration
		loadConfiguration(m2sh, workdir);
		final File cfg = new File(workdir, "config.sqlite");
		Assert.assertTrue(cfg.exists());
		Assert.assertTrue("zero-length db", cfg.length() > 0);

		serverStart(m2sh, workdir);

	}

	@AfterClass
	public static void teardown() throws Exception {

		serverStop(m2sh, workdir);

		if (executor != null)
			executor.shutdownNow();
		executor = null;

		// remove workdir
		removeWorkingDirectory(workdir);
		Assert.assertTrue(!workdir.exists());

	}

	private static File createWorkingDirectory() throws Exception {

		final String wd = "mongrel2-" + UUID.randomUUID().toString();
		final File tmp1 = File.createTempFile("tmp", null);
		tmp1.delete();

		final File workdir = new File(tmp1.getParentFile(), wd);
		final File rundir = new File(workdir, "run");
		final File tmpdir = new File(workdir, "tmp");
		final File logdir = new File(workdir, "logs");

		workdir.mkdir();
		rundir.mkdir();
		tmpdir.mkdir();
		logdir.mkdir();

		return workdir;

	}

	private static void extractConfiguration(final File target) throws IOException {
		final OutputStream out = new FileOutputStream(target);
		out.write(readInputStream(LiveTest.class.getResourceAsStream(CFG_FILE)));
		out.close();
	}

	private static String findM2sh() throws Exception {

		String m2sh = null;

		// build directories for search
		final List<String> pathdirs = new ArrayList<String>();

		// add search path to directories
		final String path = System.getenv("PATH");
		if (path != null)
			for (final String pathdir : path.split(File.pathSeparator))
				pathdirs.add(pathdir);

		// add other common locations
		pathdirs.add("/usr/local/bin");

		// start looking
		for (final String pathdir : pathdirs) {
			final File f = new File(pathdir + File.separator + "m2sh");
			if (f.exists()) {
				m2sh = f.getPath();
				break;
			}
		}

		return m2sh;

	}

	private static void loadConfiguration(final String m2sh, final File workdir) throws Exception {

		final File livetest = new File(workdir, CFG_FILE);
		extractConfiguration(livetest);
		final Process p = Runtime.getRuntime().exec(
				new String[] { m2sh, "load", "--db", "config.sqlite", "--config", CFG_FILE }, new String[0], workdir);

		final Future<?> stderr = executor.submit(new StreamMonitor(p.getErrorStream()));
		final Future<?> stdout = executor.submit(new StreamMonitor(p.getInputStream()));

		while (true) {
			try {
				Thread.sleep(500);
				p.exitValue();
				break;
			} catch (final IllegalThreadStateException x) {
				// wait
			}
		}

		stderr.cancel(true);
		stdout.cancel(true);

		Assert.assertEquals(0, p.exitValue());

	}

	private static byte[] readFile(final File f) throws IOException {
		final InputStream in = new FileInputStream(f);
		try {
			return readInputStream(in);
		} finally {
			in.close();
		}
	}

	private static String readFileAsString(final File f) throws IOException {
		return new String(readFile(f));
	}

	private static byte[] readInputStream(final InputStream in) throws IOException {

		final ByteArrayOutputStream out = new ByteArrayOutputStream();

		int len = 0;
		final byte[] buf = new byte[256];
		while ((len = in.read(buf)) > -1)
			out.write(buf, 0, len);
		out.close();
		in.close();

		return out.toByteArray();

	}

	private static void removeDirectory(final File target) {
		final File[] ff = target.listFiles();
		for (final File f : ff) {
			if (f.isDirectory()) {
				removeDirectory(f);
			} else {
				f.delete();
			}
		}
		target.delete();
	}

	private static void removeWorkingDirectory(final File dir) {
		removeDirectory(dir);
	}

	private static void serverStart(final String m2sh, final File workdir) throws Exception {

		server = Runtime.getRuntime().exec(new String[] { m2sh, "start", "--name", "test" }, new String[0], workdir);
		Assert.assertNotNull(server);

		executor.execute(new StreamMonitor(server.getErrorStream()));
		executor.execute(new StreamMonitor(server.getInputStream()));

		// System.out.printf("Server running in: %s%n", workdir.getPath());

		Thread.sleep(500);

		final File pidFile = new File(workdir, "run" + File.separator + "mongrel2.pid");
		Assert.assertNotNull(pidFile);
		Assert.assertTrue("pid file does not exist", pidFile.exists());

		// final String pid = new String(readFile(pidFile));
		// System.out.printf("Server running with pid: %s%n", pid);

	}

	private static void serverStop(final String m2sh, final File workdir) throws Exception {
		final File pidFile = new File(workdir, "run" + File.separator + "mongrel2.pid");
		final String pid = readFileAsString(pidFile);
		if (server != null)
			server.destroy();
		server = null;
		// old fashioned hardcoded kill
		final String[] command = new String[] { "/bin/kill", "-9", pid };
		Runtime.getRuntime().exec(command);
	}

	@Test
	public void testMongrel2() throws Exception {

		final Mongrel2Handler handler = new Mongrel2Handler(UUID.randomUUID().toString(), RECV_ADDR, SEND_ADDR);
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

		final URL u = new URL("http://localhost:6767/");
		final HttpURLConnection http = (HttpURLConnection) u.openConnection();
		Assert.assertEquals(200, http.getResponseCode());

		handler.setActive(false);

	}

}

class StreamMonitor implements Runnable {

	private final BufferedReader in;

	StreamMonitor(final InputStream in) {
		this.in = new BufferedReader(new InputStreamReader(in));
	}

	@Override
	public void run() {

		try {
			String line = null;
			while ((line = this.in.readLine()) != null) {
				if (line.startsWith("[ERROR]"))
					System.out.println(line);
			}
		} catch (final IOException x) {
			// ignore
		} finally {
			try {
				if (this.in != null)
					this.in.close();
			} catch (final IOException x) {
				// ignore
			}
		}

	}

}
