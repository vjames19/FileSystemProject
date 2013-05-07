/**
 * This class used to interface with the {@link Disk} acting as a low level API
 * for the most used operations within the File System.
 * 
 * <p>
 * All inumber used with this class must be >=1
 * 
 * @author Victor J.
 * 
 */
public class Translator {

	private Disk disk;
	private FreeSpaceManager manager;
	private final static int N = IndirectBlock.NUMBER_OF_POINTERS;

	// levels of indirection. They are 0-based
	private static final int LEVEL0 = 10 + N;
	private static final int LEVEL1 = LEVEL0 + N * N;
	private static final int LEVEL2 = 10 + 10 * N + N * N + N * N * N;

	// When a new block is allocated, it needs to be sanitized.
	private static byte[] EMPTY = new byte[Disk.BLOCK_SIZE];

	public Translator(Disk disk, FreeSpaceManager manager) {
		this.disk = disk;
		this.manager = manager;
	}

	public InodeBlock getInodeBlockFromDisk(int inumber) {
		InodeBlock block = new InodeBlock();
		disk.read(getPhysicalBlockNumberOfInodeBlock(inumber), block);
		return block;
	}

	public Inode getInodeFromDisk(int inumber) {
		InodeBlock block = getInodeBlockFromDisk(inumber);
		int index = getInodeLogicalIndexInInodeBlock(inumber);
		return block.node[index];
	}

	public int getPhysicalBlockNumberOfInodeBlock(int inumber) {
		// inodes start at index 1
		return inumber / InodeBlock.INODES_IN_BLOCK + 1;
	}

	public int getInodeLogicalIndexInInodeBlock(int inumber) {
		// index in inode is 0 based, inumber >= 1
		return ((inumber - 1) % InodeBlock.INODES_IN_BLOCK);
	}

	/**
	 * Gets the data pointed by this inode, specified by the seekptr.
	 * If there are holes, this holes will be allocated with new blocks.
	 * If there are free ones.<br>
	 * If there a no free blocks left it will return a 0
	 * @param inode
	 * @param inumber
	 * @param seekptr
	 * @return dataBlock pointed by this inode or 0 if there are
	 * no sufficient free blocks to allocate
	 */
	public int getDataBlockOrFillHole(Inode inode, int inumber, int seekptr) {
		int index = getLogicalIndexInInode(seekptr);
		if (index < 10) {
			int ptr = fillHole(inode.pointer[index]);
			if (isNull(ptr)) {
				return 0;
			} else {
				inode.pointer[index] = ptr;
			}

			return ptr;
			// disk.write(inode.pointer[index], iblock);
		} else if (index < LEVEL0) {
			index -= 10;
			int ptr = fillHole(inode.pointer[10]);
			if (isNull(ptr)) {
				return 0;
			} else {
				inode.pointer[10] = ptr;
			}
			return getDataBlockOrFillHoleHelper(getIndirectBlock(ptr), ptr,
					index, 0);

		} else if (index < LEVEL1) {
			index -= LEVEL0;
			int ptr = fillHole(inode.pointer[11]);
			if (isNull(ptr)) {
				return 0;
			} else {
				inode.pointer[11] = ptr;
			}

			return getDataBlockOrFillHoleHelper(getIndirectBlock(ptr), ptr,
					index, 1);

		} else if (index < LEVEL2) {
			index -= LEVEL1;
			int ptr = fillHole(inode.pointer[12]);
			if (isNull(ptr)) {
				return 0;
			} else {
				inode.pointer[12] = ptr;
			}
			return getDataBlockOrFillHoleHelper(getIndirectBlock(ptr), ptr,
					index, 2);
		}
		return 0;
	}

	/**
	 * Returns the datablock pointed by this inode or 0 if there are holes
	 * @param inode
	 * @param seekptr
	 * @return the datablock pointed by this inode or 0 if there are holes
	 */
	public int getDataBlockValuePointedByThisInode(Inode inode, int seekptr) {
		int index = getLogicalIndexInInode(seekptr);
		IndirectBlock iblock = new IndirectBlock();
		if (index < 10) {
			return inode.pointer[index];
		} else if (index < LEVEL0) {
			index -= 10;//normalize the index
			disk.read(inode.pointer[10], iblock);
			return getDataBlockValueHelper(iblock, index, 0);
		} else if (index < LEVEL1) {
			index -= LEVEL0;
			disk.read(inode.pointer[11], iblock);
			return getDataBlockValueHelper(iblock, index, 1);
		} else if (index < LEVEL2) {
			index -= LEVEL1;
			disk.read(inode.pointer[12], iblock);
			return getDataBlockValueHelper(iblock, index, 2);
		}

		return -1;

	}

	/**
	 * Frees this inode's datablocks, clears it and writes it back to disk 
	 * @param inumber inumber of the inode that needs to be freed
	 */
	public void freeInode(int inumber) {
		Inode inode = getInodeFromDisk(inumber);
		manager.freeInode(inode);
		writeInode(inode, inumber);
	}

	/**
	 * Writes the inode back to disk.
	 * @param inode inode to be written to disk
	 * @param inumber inumber of this inode
	 */
	public void writeInode(Inode inode, int inumber) {
		InodeBlock iBlock = getInodeBlockFromDisk(inumber);
		int index = getInodeLogicalIndexInInodeBlock(inumber);
		iBlock.node[index] = inode;
		disk.write(getPhysicalBlockNumberOfInodeBlock(inumber), iBlock);
	}

	/**
	 * If ptr is null then it returns a new allocated one, else the one supplied
	 * @param ptr
	 * @return
	 */
	private int fillHole(int ptr) {
		if (isNull(ptr)) {
			int block = manager.allocateBlock();
			if (!isNull(block)) {
				disk.write(block, EMPTY);
			}
			return block;
		} else {
			return ptr;
		}
	}

	//Gets the value or fills the hole using recursion. Its like accesing a tree
	private int getDataBlockOrFillHoleHelper(IndirectBlock block,
			int blockNumber, int index, int level) {
		if (level == 0) {
			int next = block.pointer[index];
			if (isNull(next)) {
				next = manager.allocateBlock();
				block.pointer[index] = next;
			}
			disk.write(blockNumber, block);
			return next;
		} else {
			final int n = (int) Math.pow(N, level);
			int i = index / n;
			int iblockNumber = fillHole(block.pointer[i]);
			if (isNull(iblockNumber)) {
				return 0;
			} else {
				block.pointer[i] = iblockNumber;
				disk.write(blockNumber, block);
			}

			IndirectBlock iblock = getIndirectBlock(iblockNumber);
			return getDataBlockOrFillHoleHelper(iblock, iblockNumber,
					index % n, level - 1);
		}
	}

	private int getDataBlockValueHelper(IndirectBlock block, int index,
			int level) {

		if (level == 0) {
			return block.pointer[index];
		} else {
			final int n = (int) Math.pow(N, level);
			int i = index / n;
			int blockNumber = block.pointer[i];
			if (isNull(blockNumber)) {
				return 0;
			}
			IndirectBlock iblock = getIndirectBlock(blockNumber);
			return getDataBlockValueHelper(iblock, index % n, level - 1);

		}
	}

	private int getLogicalIndexInInode(int seekptr) {
		return seekptr / Disk.BLOCK_SIZE;
	}

	private IndirectBlock getIndirectBlock(int blockNumber) {
		IndirectBlock iblock = new IndirectBlock();
		disk.read(blockNumber, iblock);
		return iblock;
	}

	/**
	 * True if block is null ( block <= 0)
	 * @param block
	 * @return
	 */
	private boolean isNull(int block) {
		return block <= 0;
	}

}
