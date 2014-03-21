package btv.tests.download;

import btv.download.DLManager;
import btv.bencoding.BDecodingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.io.FileNotFoundException;

public class DLManagerTestCase {
	private DLManager downloadManager;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUpTest() {
		downloadManager = new DLManager();
	}

	@Test
	public void testAdd() throws FileNotFoundException, BDecodingException {
		String expected = "1999-12-31 18.20.09.jpg";
		String result = downloadManager.add("test.torrent");

		assertEquals(expected, result);
	}

	@Test
	public void testNotFound() throws FileNotFoundException, BDecodingException {
		thrown.expect(FileNotFoundException.class);
		downloadManager.add("nofile");
	}

	@Test
	public void testReadException() throws FileNotFoundException, 
									BDecodingException {
		thrown.expect(BDecodingException.class);
		downloadManager.add("badread.torrent");
	}

}