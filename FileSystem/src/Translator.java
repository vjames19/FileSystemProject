public class Translator {

	private Disk disk;
	private FreeSpaceManager manager;
	private final static int N = IndirectBlock.NUMBER_OF_POINTERS;
	private static final int LEVEL0 = 10 + N;
	private static final int LEVEL1 = LEVEL0 + N * N;
	private static final int LEVEL2 = 10 + 10 * N + N * N + N * N * N;
	private static byte[] EMPTY = new byte[Disk.BLOCK_SIZE];

	public Translator(Disk disk, FreeSpaceManager manager) {
		this.disk = disk;
		this.manager = manager;
	}

	public InodeBlock getInodeBlockFromDisk(int inumber) {
		InodeBlock block = new InodeBlock();
		disk.read(getInodeBlockBlockNumberOnDisk(inumber), block);
		return block;
	}

	public Inode getInodeFromDisk(int inumber) {
		InodeBlock block = getInodeBlockFromDisk(inumber);
		int index = getInodeLogicalIndexInInodeBlock(inumber);
		return block.node[index];
	}

	public int getInodeBlockBlockNumberOnDisk(int inumber) {
		// inodes start at index 1
		return inumber / InodeBlock.INODES_IN_BLOCK + 1;
	}

	public int getInodeLogicalIndexInInodeBlock(int inumber) {
		// index in inode is 0 based, inumber >= 1
		return ((inumber - 1) % InodeBlock.INODES_IN_BLOCK);
	}

	public int getDataBlockOrFillHole(Inode inode, int inumber,
			int seekptr) {
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
			return getDataBlockOrFillHoleHelper(getIndirectBlock(ptr), ptr, index, 0);

		} else if (index < LEVEL1) {
			index -= LEVEL0;
			int ptr = fillHole(inode.pointer[11]);
			if (isNull(ptr)) {
				return 0;
			} else {
				inode.pointer[11] = ptr;
			}
			
			return getDataBlockOrFillHoleHelper(getIndirectBlock(ptr), ptr, index, 1);

		} else if (index < LEVEL2) {
			index -= LEVEL1;
			int ptr = fillHole(inode.pointer[12]);
			if (isNull(ptr)) {
				return 0;
			} else {
				inode.pointer[12] = ptr;
			}
			return getDataBlockOrFillHoleHelper(getIndirectBlock(ptr), ptr, index, 2);
		}
		return 0;
	}

	public int getDataBlockValuePointedByThisInode(Inode inode, int seekptr) {
		int index = getLogicalIndexInInode(seekptr);
		IndirectBlock iblock = new IndirectBlock();
		if (index < 10) {
			return inode.pointer[index];
		} else if (index < LEVEL0) {
			index -= 10;
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
	
	public void freeInode(int inumber){
		Inode inode = getInodeFromDisk(inumber);
		manager.freeInode(inode);
		writeInode(inode, inumber);
	}
	

	private int fillHole(int ptr) {
		if (isNull(ptr)) {
			int block = manager.allocateBlock();
			if(!isNull(block)){
				disk.write(block, EMPTY);
			}
			return block;
		} else {
			return ptr;
		}
	}

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
			return getDataBlockOrFillHoleHelper(iblock, iblockNumber, index % n,
					level - 1);
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
			if(isNull(blockNumber)){
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

	public void writeInode(Inode inode, int inumber) {
		InodeBlock iBlock = getInodeBlockFromDisk(inumber);
		int index = getInodeLogicalIndexInInodeBlock(inumber);
		iBlock.node[index] = inode;
		disk.write(getInodeBlockBlockNumberOnDisk(inumber), iBlock);
	}

	private boolean isNull(int block) {
		return block <= 0;
	}

}
