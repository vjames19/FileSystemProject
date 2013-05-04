/**
 * An implementation of the file system.
 *
 * Currently, all methods throw a RuntimeException.
 */

import static java.lang.Math.abs;

import java.util.Arrays;

class JavaFileSystem implements FileSystem {
	// Set up the constants for the whence field in seek
	public static final int SEEK_SET = 0;
	public static final int SEEK_CUR = 1;
	public static final int SEEK_END = 2;
	private Disk disk;
	private FileTable fileTable;
	private SuperBlock superBlock;
	private FreeSpaceManager manager;
	private Translator translator;

	JavaFileSystem() {
		this.disk = new Disk();
		this.fileTable = new FileTable();
		this.superBlock = new SuperBlock();
		translator = new Translator(disk);
		disk.read(0, superBlock);
		manager = new FreeSpaceManager(disk, superBlock, translator);
		if(disk.length() > 0){
			manager.updateFreeBlock();
		}

	}

	// Initialize the the disk to a state representing an empty file-system.
	// Fill in the super block, mark all inodes as "unused", and link
	// all data blocks into the free list.
	public int formatDisk(final int size, final int iSize) {
		if (iSize > size - 1)
			return -1;
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

		manager.updateFreeBlock();
		return 0;

	} // formatDisk

	// Close all files and shut down the simulated disk.
	public int shutdown() {

		for (int i = 0; i < fileTable.fdArray.length; i++) {
			if (fileTable.isValidAndInUse(i)) {
				close(i);
			}

		}
		disk.write(0, superBlock);
		disk.stop(false);
		return 0;
	} // shutdown

	// Create a new (empty) file and return a file descriptor.
	public int create() {
		if (fileTable.notFull()) {
			FileDescriptor fDesc = manager.getFreeInode();
			if (fDesc != null) {
				int fd =fileTable.allocate();
				fileTable.add(fd, fDesc);
				return fd;
			} else {
				System.out.println("No free inode");
				return -1;
			}
		} else {
			return -1;
		}
	} // create

	// Return the inumber of an open file
	public int inumber(final int fd) {
		if (fileTable.isValidAndInUse(fd)) {
			return fileTable.getInumber(fd);
		}

		return -1;
	}

	// Open an existing file identified by its inumber
	public int open(final int inumber) {
		if (isValidInumber(inumber)) {
			int fd = fileTable.getFDfromInumber(inumber);
			if (!fileTable.isValidAndInUseNoPrint(fd)) {// search on disk
				System.out.println("Searching on disk");
				Inode inode = translator.getInode(inumber);
				if (inode == null || !inode.isInUse()){
					System.out.println("Invalid inode");
					return -1;
				}
				fd = fileTable.allocate();
				if (fd >= 0) {
					fileTable.add(inode, inumber, fd);
					return fd;
				}
			}
			return fd;
		}
		System.out.println("invalid inumber");
		return -1;
	} // open

	// Read up to buffer.length bytes from the open file indicated by fd,
	// starting at the current seek pointer, and update the seek pointer.
	// Return the number of bytes read, which may be less than buffer.length
	// if the seek pointer is near the current end of the file.
	// In particular, return 0 if the seek pointer is greater than or
	// equal to the size of the file.
	public int read(final int fd, final byte[] buffer) {
		if (!fileTable.isValidAndInUse(fd)) {
			return -1;
		}

		Inode inode = fileTable.getInode(fd);
		int seekptr = fileTable.getSeekPointer(fd);
		if (seekptr >= inode.fileSize) {
			return 0;
		}

		int physicalBlock = -1;
		byte[] temp_buffer = new byte[Disk.BLOCK_SIZE];
		int bufferptr = 0;
		int readBytes = 0;

		while (bufferptr < buffer.length && seekptr < inode.fileSize) {
			physicalBlock = translator.getInodeBlockValue(inode, seekptr);
			if (physicalBlock <= 0){//print a hole
				Arrays.fill(buffer, (byte)0);
				fileTable.setSeekPointer(fd, seekptr + buffer.length);
				return buffer.length;
			}
			disk.read(physicalBlock, temp_buffer);

			int offset = seekptr % Disk.BLOCK_SIZE;
			int bytesRead = Math.min(abs(Disk.BLOCK_SIZE - offset),
					abs(inode.fileSize - seekptr));
			bytesRead = Math.min(bytesRead, buffer.length - bufferptr);

			for (int i = 0; i < bytesRead; i++) {
				buffer[bufferptr] = temp_buffer[offset++];
				bufferptr++;
			}
			// update counters
			seekptr += bytesRead;
			readBytes += bytesRead;
			
		}
		
		fileTable.setSeekPointer(fd, seekptr);

		return readBytes;
	} // read

	// Transfer buffer.length bytes from the buffer to the file, starting
	// at the current seek pointer, and add buffer.length to the seek pointer.
	public int write(final int fd, final byte[] buffer) {
		if (!fileTable.isValidAndInUse(fd)) {
			return -1;
		}
		// TODO 8 TEST 3
		int seekptr = fileTable.getSeekPointer(fd);
		byte[] temp = new byte[Disk.BLOCK_SIZE];
		Inode inode = fileTable.getInode(fd);
		int inumber = fileTable.getInumber(fd);

		int writtenBytes = 0;
		int bufferptr = 0;

		while (bufferptr < buffer.length) {
			int physical = translator.getInodeBlockValue(inode, seekptr);

			if (physical <= 0) {
				int allocated = manager.allocateBlock();
				if (allocated <= 0) {
					System.out.println("no free block to write");
					return -1;
				}
				translator.setInodeBlockValue(inode,inumber, seekptr, allocated);
				physical = allocated;
			}

			int offset = seekptr % Disk.BLOCK_SIZE;

			disk.read(physical, temp);
			int writeNum = Math.min(abs(Disk.BLOCK_SIZE - offset),
					abs(buffer.length - bufferptr));
			for (int i = 0; i < writeNum; i++) {
				temp[offset++] = buffer[bufferptr++];
			}

			disk.write(physical, temp);

			writtenBytes += writeNum;
			seekptr += writeNum;

			inode.fileSize = seekptr;
			
		}
		
		fileTable.setSeekPointer(fd, seekptr);

		return writtenBytes;

	} // write

	// Update the seek pointer by offset, according to whence.
	// Return the new value of the seek pointer.
	// If the new seek pointer would be negative, leave it unchanged
	// and return -1.
	public int seek(final int fd, final int offset, final int whence) {
		if (!fileTable.isValidAndInUse(fd)) {
			return -1;
		}
		int seekptr = fileTable.getSeekPointer(fd);

		switch (whence) {
		case SEEK_SET:
			seekptr = offset;
			break;
		case SEEK_CUR:
			seekptr += offset;
			break;
		case SEEK_END:
			seekptr = fileTable.getInode(fd).fileSize + offset;
			break;
		}
		if (seekptr >= 0) {
			fileTable.setSeekPointer(fd, seekptr);
			return seekptr;
		} else {
			return -1;
		}

	} // seek

	// Write the inode back to disk and free the file table entry
	public int close(final int fd) {
		if (!fileTable.isValidAndInUse(fd))
			return -1;
		Inode inode = fileTable.getInode(fd);
		int inumber = fileTable.getInumber(fd);		
		translator.updateInode(inode, inumber);
		fileTable.free(fd);
		return 0;
	} // close

	// Delete the file with the given inumber, freeing all of its blocks.
	public int delete(final int inumber) {
		if (!isValidInumber(inumber)) {
			return -1;
		}
		int fd = fileTable.getFDfromInumber(inumber);
		if (fileTable.isValidAndInUseNoPrint(fd)) {// close if still open
			close(fd);
		}

		Inode inode = translator.getInode(inumber);
		manager.freeInode(inode, inumber);
		return 0;

	} // delete

	public String toString() {
		String s = disk.generateStats();
		s.concat(superBlock.toString());
		return s;
	}

	private int blockNum(int iNumber) {
		return 1 + (iNumber / InodeBlock.INODES_IN_BLOCK);
	}

	private int inodeInBlock(int iNumber) {
		return (iNumber - 1) % (InodeBlock.INODES_IN_BLOCK);
	}

	private boolean isValidInumber(int inumber) {
		return inumber > 0
				&& inumber <= (superBlock.iSize * InodeBlock.INODES_IN_BLOCK);
	}
}
