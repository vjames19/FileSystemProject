// This class keeps track of all files currently open.
// In a real Unix system, this information is split into three levels:
// There is a table of "in-core inodes" with one entry for each file that
// has been opened by any process, there is another table with one entry
// for each "instance" of an open file (allowing multiple seek pointers
// into the same file), and there is a per-process table
// mapping instances to file descriptors.

/*
 *Our implementation:
 *A byte array called bitmap tells whether the specified index has a file open.
 *A fileDescriptor array fdArray contains FD objects.
 *Each FD object is an instance of the class FileDescriptor (below) that maintains
 *information about the specified file.
 */
class FileTable {
	public static final int MAX_FILES = 21;
	public int bitmap[];
	public FileDescriptor[] fdArray;

	public FileTable() {
		fdArray = new FileDescriptor[MAX_FILES];
		bitmap = new int[MAX_FILES];
		for (int i = 0; i < MAX_FILES; i++) {
			bitmap[i] = 0;
		}
	}

	/**
	 * Returns an index of a free space in on the table
	 * 
	 * @return index or -1 if there is no space
	 */
	public int allocate() {
		for (int i = 0; i < MAX_FILES; i++) {
			if (bitmap[i] == 0)
				return i;
		}
		System.out.println("Cannot open file... filetable is full.");
		return -1; // filetable is full already.
	}

	/**
	 * Adds the arguments to the file table. It overwrites if the file
	 * descriptor already is being used
	 * 
	 * @param inode
	 * @param inumber
	 * @param fd
	 * @return
	 */
	public int add(Inode inode, int inumber, int fd) {
		if (bitmap[fd] != 0)
			return -1;
		bitmap[fd] = 1;
		fdArray[fd] = new FileDescriptor(inode, inumber);
		return 0; // SUCCESS this time.
	}

	/**
	 * Adds the arguments to the file table. It overwrites if the file
	 * descriptor already is being used
	 * 
	 * @param fd
	 * @param fileDescriptor
	 * @return
	 */
	public int add(int fd, FileDescriptor fileDescriptor) {
		if (bitmap[fd] != 0)
			return -1;
		bitmap[fd] = 1;
		fdArray[fd] = fileDescriptor;
		return 0;
	}

	/**
	 * Free this file descriptor entry.
	 * 
	 * @param fd
	 */
	public void free(int fd) {
		bitmap[fd] = 0;
		fdArray[fd] = null;
	}

	/**
	 * Returns true if the fd is being used and its valid
	 * 
	 * @param fd
	 *            file descriptor
	 * @return
	 */
	public boolean isValidAndInUse(int fd) {
		if (fd < 0 || fd >= MAX_FILES) {
			System.out
					.println("ERROR: Invalid file descriptor (must be 0 <= fd <= "
							+ MAX_FILES + ") : " + fd);
			return false;
		} else if (bitmap[fd] > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns true if the fd is being used and its valid. It doesn't print an
	 * error if its not valid
	 * 
	 * @param fd
	 *            file descriptor
	 * @return
	 */
	public boolean isValidAndInUseNoPrint(int fd) {
		if (fd < 0 || fd >= MAX_FILES) {
			return false;
		} else if (bitmap[fd] > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * True if the fd is in the valid range
	 * 
	 * @param fd
	 * @return
	 */
	public boolean isInRange(int fd) {
		return fd >= 0 && fd < MAX_FILES;
	}

	/**
	 * Returns the inode associated with this file descriptor
	 * 
	 * @param fd
	 * @return
	 */
	public Inode getInode(int fd) {
		if (bitmap[fd] == 0) {
			return null;
		} else {
			return fdArray[fd].getInode();
		}
	}

	/**
	 * Returns the inumber associated with this file descriptor
	 * 
	 * @param fd
	 * @return
	 */
	public int getInumber(int fd) {
		if (bitmap[fd] == 0) {
			return 0; // ERROR, if invalid
		} else {
			return fdArray[fd].getInumber();
		}
	}

	public int getSeekPointer(int fd) {
		if (bitmap[fd] == 0) {
			return 0; // invalid
		} else {
			return fdArray[fd].getSeekPointer();
		}
	}

	public int setSeekPointer(int fd, int newPointer) {
		if (bitmap[fd] == 0) {
			return 0; // invalid
		} else {
			fdArray[fd].setSeekPointer(newPointer);
			return 1; // valid
		}
	}

	public int setFileSize(int fd, int newFileSize) {
		if (bitmap[fd] == 0) {
			return 0; // invalid
		} else {
			fdArray[fd].setFileSize(newFileSize);
			return 1;
		}
	}

	public int getFDfromInumber(int inumber) {
		for (int i = 0; i < MAX_FILES; i++) {
			if (bitmap[i] == 1) {
				if (fdArray[i].getInumber() == inumber) {
					return i;
				}
			}
		}
		return -1;
	}

	public boolean notFull() {
		for (int i = 0; i < MAX_FILES; i++) {
			if (bitmap[i] == 0)
				return true;
		}

		return false;
	}
}
