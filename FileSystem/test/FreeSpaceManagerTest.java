import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
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
		manager = new FreeSpaceManager(disk, superBlock);
		manager.updateFreeBlock();
	}
	@Test
	public void testAllocateBlock() {
		for(int i= isize+1; i <= size; i++){
			System.out.println(i);
			assertEquals(i, manager.allocateBlock());
		}
		assertEquals(0,manager.allocateBlock());
		assertEquals(0,manager.allocateBlock());
		assertEquals(0,manager.allocateBlock());


	}

	@Test
	public void testFreeInode() {
		assertEquals(0, manager.getAllocated());
		Inode inode = new Inode();
		int len = Math.min(inode.pointer.length, size - isize);
		for(int i=0; i <len; i++){
			inode.pointer[i] = manager.allocateBlock();
			assertTrue(inode.pointer[i] >0);
		}
		
		assertEquals(len, manager.getAllocated());
		
		manager.freeInode(inode);
		assertEquals(false, inode.isInUse());
		assertEquals(0, manager.getAllocated());
		
		int ptr = superBlock.freeList;
		int count =0;
		while(ptr > 0){
			IndirectBlock iBlock = new IndirectBlock();
			disk.read(ptr, iBlock);
			ptr = iBlock.pointer[0];
			count++;
		}
		
		assertEquals(size -isize, count);

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
	
	@After
	public void after(){
		System.out.println("next test:");
	}

}
