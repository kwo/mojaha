package mongrel2;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LiveTest {
	
	
	// find m2sh in path
	// create uuid tmp dir in tmp dir
	// create tmp, run, logs
	
	private static final String CFG_FILE = "livetest.conf";
	private static String m2sh = null;
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
		
		// shell to m2sh, load configuration
		loadConfiguration(m2sh, workdir);
		Assert.assertTrue(new File(workdir, "config.sqlite").exists());
		
		// TODO: start mongrel2
		
	}

	@AfterClass
	public static void teardown() throws Exception {
		
		// TODO: stop/kill mongrel
		
		// remove workdir
		removeWorkingDirectory(workdir);
		Assert.assertTrue(!workdir.exists());
		
	}

	@Test
	public void testMongrel2() throws Exception {
		//Assert.fail(workdir.getPath());
	}
	
	private static void loadConfiguration(String m2sh, File workdir) throws Exception {
		
		File livetest = new File(workdir, CFG_FILE);
		extractConfiguration(livetest);
		Process p = Runtime.getRuntime().exec(new String[]{ m2sh, "load", "--db", "config.sqlite", "--config", CFG_FILE } , new String[0], workdir);
		
		while (true) {
			try {
				Thread.sleep(500);
				p.exitValue();
				break;
			} catch (IllegalThreadStateException x) {
				// wait
			}
		}
		
		Assert.assertEquals(0, p.exitValue());
	}
	
	private static void extractConfiguration(File target) throws Exception {
		
		OutputStream out = new BufferedOutputStream(new FileOutputStream(target));
		InputStream in = LiveTest.class.getResourceAsStream(CFG_FILE);
		
		int len = 0;
		byte[] buf = new byte[256];
		while ((len = in.read(buf)) > -1)
			out.write(buf, 0, len);
		out.close();
		in.close();
		
	}
	
	private static File createWorkingDirectory() throws Exception {
		
		String wd = "mongrel2-" + UUID.randomUUID().toString();
		File tmp1 = File.createTempFile("tmp", null);
		tmp1.delete();

		File workdir = new File(tmp1.getParentFile(), wd);
		File rundir = new File(workdir, "run");
		File tmpdir = new File(workdir, "tmp");
		File logdir = new File(workdir, "logs");
		
		workdir.mkdir();
		rundir.mkdir();
		tmpdir.mkdir();
		logdir.mkdir();
		
		return workdir;
		
	}
	
	private static void removeWorkingDirectory(File dir) {
		removeDirectory(dir);
	}

	private static void removeDirectory(File target) {
		File[] ff = target.listFiles();
		for (File f : ff) {
			if (f.isDirectory()) {
				removeDirectory(f);
			} else {
				f.delete();
			}
		}
		target.delete();
	}
	
	private static String findM2sh() throws Exception {
		
		String m2sh = null;

		// build directories for search
		List<String> pathdirs = new ArrayList<String>();
		
		// add search path to directories
		String path = System.getenv("PATH");
		if (path != null)
			for (String pathdir : path.split(File.pathSeparator))
				pathdirs.add(pathdir);

		// add other common locations
		pathdirs.add("/usr/local/bin");
		
		// start looking
		for (String pathdir : pathdirs) {
			File f = new File(pathdir + File.separator + "m2sh");
			if (f.exists()) {
				m2sh = f.getPath();
				break;
			}
		}
		
		return m2sh;
		
	}
	

}
