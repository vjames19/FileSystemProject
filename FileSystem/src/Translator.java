public class Translator {

	private Disk disk;
	private final static int N = IndirectBlock.NUMBER_OF_POINTERS;

	public Translator(Disk disk) {
		this.disk = disk;
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
		return inumber / InodeBlock.INODES_IN_BLOCK + 1;
	}

	public int getInodeLogicalIndexInInodeBlock(int inumber) {
		return ((inumber -1) % InodeBlock.INODES_IN_BLOCK);
	}

	public void changedDataBlockPointedByTheInode(Inode inode,int inumber, int seekptr, int value) {
		int index = getLogicalIndexInInode(seekptr);
		IndirectBlock iblock = new IndirectBlock();
		if (index < 10) {
			inode.pointer[index] = value;
			//disk.write(inode.pointer[index], iblock);
		} else if (index < 10 + N) {
			index -= 10;
			disk.read(inode.pointer[10], iblock);
			changeDataBlockValueHelper(iblock, inode.pointer[10], index, 0, value);

		} else if (index < 10 + N + N * N) {
			index -= (10 + N);
			disk.read(inode.pointer[11], iblock);
			changeDataBlockValueHelper(iblock, inode.pointer[11], index, 1, value);

		} else if (index < 10 + 10 * N + N * N + N * N * N) {
			index -= (10 + N + N * N);
			disk.read(inode.pointer[12], iblock);
			changeDataBlockValueHelper(iblock, inode.pointer[11], index, 2, value);
		}
	}

	public int getDataBlockValuePointedByThisInode(Inode inode, int seekptr) {
		int index = getLogicalIndexInInode(seekptr);
		IndirectBlock iblock = new IndirectBlock();
		if (index < 10) {
			return inode.pointer[index];
		} else if (index < 10 + N) {
			index -= 10;
			disk.read(inode.pointer[10], iblock);
			return getDataBlockValueHelper(iblock, index, 0);
		} else if (index < 10 + 10 * N + N * N) {
			index -= (10 + N);
			disk.read(inode.pointer[11], iblock);
			return getDataBlockValueHelper(iblock, index, 1);
		} else if (index < 10 + 10 * N + N * N + N * N * N) {
			index -= (10 + 10 * N + N * N);
			disk.read(inode.pointer[12], iblock);
			return getDataBlockValueHelper(iblock, index, 2);
		}

		return -1;

	}

	private int getLogicalIndexInInode(int seekptr) {
		return seekptr / Disk.BLOCK_SIZE;
	}

	private int getDataBlockValueHelper(IndirectBlock block, int index,
			int level) {

		if (level == 0) {
			return block.pointer[index];
		} else {
			final int n = (int) Math.pow(N, level);
			int i = index / n;
			int blockNumber = block.pointer[i];
			IndirectBlock iblock = new IndirectBlock();
			disk.read(blockNumber, iblock);
			return getDataBlockValueHelper(iblock, index % n, level - 1);

		}
	}

	private void changeDataBlockValueHelper(IndirectBlock block, int blocknumber,
			int index, int level, int value) {
		if (level == 0) {
			block.pointer[index] = value;
			disk.write(blocknumber, block);
		} else {
			final int n = (int) Math.pow(N, level);
			int i = index / n;
			int blockNumber = block.pointer[i];
			IndirectBlock iblock = new IndirectBlock();
			disk.read(blockNumber, iblock);
			changeDataBlockValueHelper(iblock, blockNumber, index % n, level - 1,
					value);

		}
	}
	
	public void updateInode(Inode inode, int inumber){
		InodeBlock iBlock = getInodeBlockFromDisk(inumber);
		int index = getInodeLogicalIndexInInodeBlock(inumber);
		iBlock.node[index] = inode;
		disk.write(getInodeBlockBlockNumberOnDisk(inumber), iBlock);
	}

}
