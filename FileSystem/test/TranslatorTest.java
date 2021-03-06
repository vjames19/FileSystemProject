import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
public class TranslatorTest {

	private Translator translator;
	private Disk disk;
	private SuperBlock superBlock;
	private FreeSpaceManager manager;
	private Random r = new Random();
	final int n =IndirectBlock.NUMBER_OF_POINTERS;

	@Before
	public void setup() {
		superBlock = new SuperBlock();
		disk = new Disk();
		disk.format();
		writeStructures(2, 10);
		manager = new FreeSpaceManager(disk, superBlock);
		translator = new Translator(disk, manager);
	}

	@Test
	public void testEquals() {
		Inode inode = new Inode();
		inode.flags = 1;
		inode.fileSize = 10;
		inode.pointer[0] = 10;

		Inode inode2 = new Inode();
		inode2.flags = 1;
		inode2.fileSize = 10;
		inode2.pointer[0] = 10;
		assertEquals(inode, inode2);
	}

	@Test
	public void testRead() {
		InodeBlock block = new InodeBlock();
		Inode inode = getRandomInode();
		block.node[0] = inode;

		Inode inode2 = getRandomInode();
		block.node[2] = inode2;
		disk.write(1, block);

		assertEquals(inode, translator.getInodeFromDisk(1));
		assertEquals(inode2, translator.getInodeFromDisk(3));
		assertEquals(new Inode(), translator.getInodeFromDisk(2));
		assertEquals(new Inode(), translator.getInodeFromDisk(15));

	}

	@Test
	public void testWriteInode() {

		final int len = superBlock.iSize * InodeBlock.INODES_IN_BLOCK;
		List<Inode> inodes = new ArrayList<Inode>(len);
		List<Integer> references = new ArrayList<Integer>(len);
		for (int i = 1; i <= len; i++) {
			Inode inode = getRandomInode();
			inodes.add(inode);
			translator.writeInode(inode, i);
			references.add(i);
		}

		Collections.shuffle(references);
		for (Integer reference : references) {
			assertEquals(inodes.get(reference - 1),
					translator.getInodeFromDisk(reference));
		}

	}

	@Test
	public void testGetBlock() {
		Inode inode = getRandomInode();
		Inode inode2 = getRandomInode();
		InodeBlock block = new InodeBlock();
		block.node[0] = inode;
		block.node[3] = inode2;
		disk.write(1, block);

		assertEquals(block, translator.getInodeBlockFromDisk(4));

		disk.write(2, block);

		assertEquals(block,
				translator.getInodeBlockFromDisk(4 + InodeBlock.INODES_IN_BLOCK));

	}

	private Inode getRandomInode() {
		Inode inode = new Inode();
		inode.flags = r.nextInt(2);
		inode.fileSize = r.nextInt(superBlock.size - superBlock.iSize + 1);
		Arrays.fill(inode.pointer, r.nextInt());
		return inode;

	}

	private void writeStructures(int iSize, int size) {

		disk.format();
		this.superBlock = new SuperBlock();
		this.superBlock.size = size;
		this.superBlock.iSize = iSize;
		superBlock.freeList = iSize + 1;

		disk.write(0, superBlock);

		InodeBlock iblock = new InodeBlock();
		for (int i = 1; i <= iSize; i++) {
			disk.write(i, iblock);
		}

		// data blocks
		IndirectBlock idBlock = new IndirectBlock();
		for (int i = iSize + 1; i <= size; i++) {
			if (i < size - 1)
				idBlock.pointer[0] = i + 1; // link to next block
			else
				idBlock.pointer[0] = 0;// null ptr
			disk.write(i, idBlock);
		}
	}
	
	//TODO test fill holes

}
