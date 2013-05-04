/**
 * The interface of the file system to be implemented 
 */
interface FileSystem {
	 /**
     * Initialize the the disk to a state representing an empty file-system.
     * Fill in the super block, mark all inodes as "unused", and link
     * all data blocks into the free list.
     *
     * @param size the size of the disk in blocks
     * @param iSize the number of inode blocks
     * @return 0 if successful, -1 if not
     */
    public int formatDisk(int size, int iSize); 
    
    /**
     * Close all files and shut down the simulated disk.
     */
    public int shutdown(); 
    
    /**
     * Create a new (empty) file and return a file descriptor.
     * Then, open the file.
     *
     * @return the file descriptor
     */
    public int create(); 
    
    /**
     * Return the inumber of an open file
     *
     * @param fd the file descriptor of the file
     * @return the inode number of the file
     */
    public int inumber(int fd); 
    
    /**
     * Open an existing file identified by its inumber
     *
     * @param inum the inode number of the file
     * @return the file descriptor number
     */
    public int open(int iNumber); 
    
    /**
     * Read up to buffer.length bytes from the open file indicated by fd,
     * starting at the current seek pointer, and update the seek pointer.
     * Return the number of bytes read, which may be less than buffer.length
     * if the seek pointer is near the current end of the file.
     * In particular, return 0 if the seek pointer is greater than or
     * equal to the size of the file.
     *
     * @param fd the file descriptor
     * @param buffer a pre-initialized reading buffer
     * @return number of bytes read, or -1 on error
     */
    public int read(int fd, byte[] buffer); 
    
    /**
     * Transfer buffer.length bytes from the buffer to the file, starting
     * at the current seek pointer, and add buffer.length to the seek pointer.
     *
     * @param fd the file descriptor
     * @param buffer the buffer to write
     * @return number of bytes written if success, -1 if error
     */
    public int write(int fd, byte[] buffer); 
    
    /**
     * Update the seek pointer by offset, according to whence.
     * Return the new value of the seek pointer.
     * If the new seek pointer would be negative, leave it unchanged
     * and return -1.
     *
     * @param fd file descriptor
     * @param offset the offset of the seek pointer
     * @param whence either SEEK_SET, SEEK_CUR, or SEEK_END
     * @return the new value of the seek pointer, or -1 on error
     */
    public int seek(int fd, int offset, int whence); 
    
    /**
     * Write the inode back to disk and free the file table entry
     *
     * @param fd the file descriptor
     * @return 0 if successful, -1 if error
     */
    public int close(int fd); 
    
    /**
     * Delete the file with the given inumber, freeing all of its blocks.
     *
     * @param inum the inode number
     * @return 0 if successful, -1 if error
     */
    public int delete(int iNumber); 
}
