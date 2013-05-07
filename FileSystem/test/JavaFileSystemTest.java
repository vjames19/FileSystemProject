import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;


public class JavaFileSystemTest {

	JavaFileSystem fs;
	int isize;
	int size;
	@Before
	public void setup(){
		fs = new JavaFileSystem();
		isize = 2;
		size = 4;
		fs.formatDisk(size, isize);
	}


	@Test
	public void testShutdown() {
		fs.shutdown();
	}

	@Test
	public void testCreate() {
		int len = Math.min(FileTable.MAX_FILES, isize * InodeBlock.INODES_IN_BLOCK);
		for(int i=0; i < len; i++){
			assertEquals(i,fs.create());
		}
		
		for(int i=0; i < len-1; i++){
			assertTrue(fs.create() < 0);
			fs.delete(i+1);
			assertEquals(i, fs.create());
		}
	}

	@Test
	public void testInumber() {
		
	}

	@Test
	public void testOpen() {
		int fd = fs.create();
		int inumber = fs.inumber(fd);
		assertEquals(fd, fs.open(inumber));
		assertEquals(-1, fs.open(10));
		
		fs.close(fd);
		assertTrue(fs.open(inumber) >=0);
		
	}

	@Test
	public void testRead() {
		fail("Not yet implemented");
	}

	@Test
	public void testWrite() {
		fail("Not yet implemented");
	}

	@Test
	public void testSeek() {
		fail("Not yet implemented");
	}

	@Test
	public void testClose() {
		fail("Not yet implemented");
	}

	@Test
	public void testDelete() {
		fail("Not yet implemented");
	}

}
