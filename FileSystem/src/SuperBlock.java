/**
 * Class representing the super block.
 * 
 * TO DO: This has to be refactored so to allow
 * the data in this class to be declared as private
 * This will require getter/setter methods for each data element.
 */

public class SuperBlock {
	// number of blocks in the file system.
	private int size;
	//public int size;
	// number of index blocks in the file system. 
	private int iSize;
	//public int iSize;
	// first block of the free list
	private int freeList;
	//public int freeList;

	public String toString () {
		return
			"SUPERBLOCK:\n"
			+ "Size: " + getSize()
			+ "  Isize: " + iSize
			+ "  freeList: " + freeList;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getiSize() {
		return iSize;
	}

	public int setiSize(int iSize) {
		this.iSize = iSize;
		return iSize;
	}

	public int getFreeList() {
		return freeList;
	}

	public int setFreeList(int freeList) {
		this.freeList = freeList;
		return freeList;
	}
}
