/**
 * Class representing the super block.
 * 
 * TO DO: This has to be refactored so to allow
 * the data in this class to be declared as private
 * This will require getter/setter methods for each data element.
 */

class SuperBlock {
	// number of blocks in the file system.
	int size;
	//public int size;
	// number of index blocks in the file system. 
	int iSize;
	//public int iSize;
	// first block of the free list
	int freeList;
	//public int freeList;

	public String toString () {
		return
			"SUPERBLOCK:\n"
			+ "Size: " + size
			+ "  Isize: " + iSize
			+ "  freeList: " + freeList;
	}
}
