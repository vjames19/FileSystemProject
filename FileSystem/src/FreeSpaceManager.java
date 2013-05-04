public class FreeSpaceManager {
	private SuperBlock superBlock;
	private Disk disk;
	private IndirectBlock freeBlock;
	private static final int NEXT_BLOCK = 0;
	private Translator translator;

	public FreeSpaceManager(Disk disk, SuperBlock block, Translator translator) {
		// if (block.freeList == 0) {
		// throw new IllegalArgumentException(
		// "Invalid free block pointing to superblock");
		// }
		this.disk = disk;
		this.superBlock = block;
		this.translator = translator;

		// updateFreeBlock();
	}

	public void updateFreeBlock() {
		freeBlock = new IndirectBlock();
		this.disk.read(superBlock.freeList, freeBlock);
	}

	public int allocateBlock() {
		if (superBlock.freeList <= 0) {
			System.out
					.println("FreeSpaceManager.allocateBlock() no more free blocks");
			return -1;
		}

		int current = superBlock.freeList;
		int next = freeBlock.pointer[NEXT_BLOCK];
		if (next > 0) {
			disk.read(next, freeBlock);
		}
		superBlock.freeList = next;
		return current;
	}

	public void freeInode(Inode inode, int inumber) {
		for (int i = 0; i < 10; i++) {
			freeBlock(inode.pointer[i]);
		}

		for (int i = 10, j = 0; i < inode.pointer.length; i++, j++) {
			if (inode.pointer[i] > 0) {
				freeInodeHelper(getIndirectBlock(inode.pointer[i]), j);
			}
		}

		inode.makeEmpty();
		translator.updateInode(inode, inumber);

	}

	private IndirectBlock getIndirectBlock(int blockNumber) {
		IndirectBlock block = new IndirectBlock();
		disk.read(blockNumber, block);
		return block;
	}

	private void freeInodeHelper(IndirectBlock block, int level) {
		if (level == 0) {
			for (int i = 0; i < block.pointer.length; i++) {
				freeBlock(block.pointer[i]);
			}
		} else {
			IndirectBlock iblock = new IndirectBlock();
			for (Integer i : block.pointer) {
				if (i > 0) {
					disk.read(i, iblock);
					freeInodeHelper(iblock, level - 1);

				}
			}
		}
	}

	private void freeBlock(int i) {
		if (i > 0) {
			freeBlock.clear();
			freeBlock.pointer[NEXT_BLOCK] = superBlock.freeList;
			superBlock.freeList = i;
			disk.write(i, freeBlock);
		}
	}

	public FileDescriptor getFreeInode() {
		InodeBlock iblock = new InodeBlock();
		for (int i = 1; i <= superBlock.iSize; i++) {
			disk.read(i, iblock);
			for (int j = 0; j < iblock.node.length; j++) {
				if (!iblock.node[j].isInUse()) {
					Inode inode = iblock.node[j];
					inode.init();
					disk.write(i, iblock);
					System.out.printf("i %d j %d %s", i, j, inode);
					return new FileDescriptor(iblock.node[j], (i - 1)
							* InodeBlock.INODES_IN_BLOCK + j + 1);
				}
			}
		}

		return null;
	}

	private void checkForNull(int i) {
		if (i == 0) {
			throw new IllegalArgumentException("Null pointer");
		}
	}

}
