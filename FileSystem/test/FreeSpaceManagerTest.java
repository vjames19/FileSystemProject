import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;


public class FreeSpaceManagerTest extends AbstractTest {

	FreeSpaceManager manager;
	
	@Override
	@Before
	public void setup() {
		// TODO Auto-generated method stub
		isize = 2;
		size = 4;
		super.setup();
		superBlock.freeList = isize +1;
		manager = new FreeSpaceManager(disk, superBlock, translator);
		manager.updateFreeBlock();
	}
	@Test
	public void testAllocateBlock() {
		for(int i= isize+1; i <= size; i++){
			System.out.println(i);
			assertEquals(i, manager.allocateBlock());
		}
		assertEquals(-1,manager.allocateBlock());

	}

	@Test
	public void testFreeInode() {
		int len = isize * InodeBlock.INODES_IN_BLOCK;
		List<FileDescriptor> fds = new ArrayList<FileDescriptor>(len);
		List<Integer> inumbers = new ArrayList<Integer>(len);
		for(int i=0; i < len; i++){
			FileDescriptor fd = manager.getFreeInode();
			fds.add(fd);
			inumbers.add(i+1);
			compareInumber(i+1, fd,true);
		}
		
		Collections.reverse(inumbers);
		for(Integer i: inumbers){
			Inode inode = translator.getInode(i);
			manager.freeInode(inode, i);
			FileDescriptor fd= new FileDescriptor(translator.getInode(i), i);
			compareInumber(i,fd , false);
		}

	}

	@Test
	public void testGetFreeInode() {
		int len = isize * InodeBlock.INODES_IN_BLOCK;
		for(int i=0; i < len; i++){
			compareInumber(i+1, manager.getFreeInode(), true);
		}
		
		assertEquals(null, manager.getFreeInode());
		assertEquals(null, manager.getFreeInode());
	}
	private void compareInumber(int i, FileDescriptor fd, boolean inUse) {
		assertEquals(i, fd.getInumber());
		assertTrue(fd.getInode() != null);
		assertEquals(inUse, fd.getInode().isInUse());
	}

}
