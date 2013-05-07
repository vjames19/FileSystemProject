/**
 * Manage a free list using linked allocation. Is to be used
 * in conjunction with {@link Translator}.
 * @author Victor J.
 *
 */
public class FreeSpaceManager {
	private SuperBlock superBlock;
	private Disk disk;
	private IndirectBlock freeBlock;
	private static final int NEXT_BLOCK = 0;
	private int allocated;//keeping a tally for testing

	public FreeSpaceManager(Disk disk, SuperBlock block) {
		this.disk = disk;
		this.superBlock = block;
	}

	/**
	 * Use it when the super block free list pointer has changed
	 * outside this context.
	 */
	public void updateFreeBlock() {
		freeBlock = new IndirectBlock();
		this.disk.read(superBlock.freeList, freeBlock);
	}

	/**
	 * Allocates a block if there is one free.
	 * @return
	 */
	public int allocateBlock() {
		if (superBlock.freeList <= 0) {
			System.out
					.println("FreeSpaceManager.allocateBlock() no more free blocks");
			return 0;
		}
		//unlink and link to next one in the free list
		int current = superBlock.freeList;
		int next = freeBlock.pointer[NEXT_BLOCK];
		if (next > 0) {
			disk.read(next, freeBlock);
		}
		superBlock.freeList = next;//links it
		allocated++;
//		System.out.println("allocated " + ++allocated);
		return current;
	}

	public void freeInode(Inode inode) {
		for (int i = 0; i < 10; i++) {
			freeBlock(inode.pointer[i]);//free all direct
		}

		for (int i = 10, j = 0; i < inode.pointer.length; i++, j++) {//free indirect
			if (inode.pointer[i] > 0) {
				freeInodeHelper(getIndirectBlock(inode.pointer[i]), j);
				freeBlock(inode.pointer[i]);
			}
		}
		inode.makeEmpty();
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
					freeBlock(i);

				}
			}
		}
	}

	public void freeBlock(int block) {
		if (block > 0) {
			freeBlock.clear();
			freeBlock.pointer[NEXT_BLOCK] = superBlock.freeList;//link
			superBlock.freeList = block;//link
			disk.write(block, freeBlock);
			allocated--;
//			System.out.println("allocated " + --allocated);
		}
	}

	/**
	 * Searches the inode blocks until it finds a free one.
	 * @return FileDescriptor or null if there is no free inode
	 */
	public FileDescriptor getFreeInode() {
		InodeBlock iblock = new InodeBlock();
		for (int i = 1; i <= superBlock.iSize; i++) {
			disk.read(i, iblock);
			for (int j = 0; j < iblock.node.length; j++) {
				if (!iblock.node[j].isInUse()) {
					Inode inode = iblock.node[j];
					inode.init();
					disk.write(i, iblock);
//					System.out.printf("i %d j %d %s", i, j, inode);
					return new FileDescriptor(iblock.node[j], (i - 1)
							* InodeBlock.INODES_IN_BLOCK + j + 1);
				}
			}
		}

		return null;
	}

	public int getAllocated() {
		return allocated;
	}

}
