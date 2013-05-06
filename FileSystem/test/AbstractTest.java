import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
public class AbstractTest {

	protected Disk disk;
	protected SuperBlock superBlock;
	protected Random r = new Random();
	final int n =IndirectBlock.NUMBER_OF_POINTERS;
	protected int isize =2;
	protected int size = 10;

	@Before
	public void setup() {
		superBlock = new SuperBlock();
		disk = new Disk();
		writeStructures(isize, size);
	}

	protected Inode getRandomInode() {
		Inode inode = new Inode();
		inode.flags = r.nextInt(2);
		inode.fileSize = r.nextInt(superBlock.size - superBlock.iSize + 1);
		Arrays.fill(inode.pointer, r.nextInt());
		return inode;

	}

	protected void writeStructures(int iSize, int size) {

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
			if (i < size)
				idBlock.pointer[0] = i + 1; // link to next block
			else
				idBlock.pointer[0] = 0;// null ptr
			disk.write(i, idBlock);
		}
	}

	

}
