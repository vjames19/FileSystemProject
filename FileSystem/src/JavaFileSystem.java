/**
 * An implementation of the file system.
 *
 * Currently, all methods throw a RuntimeException.
 */

import java.util.*;
import java.io.*;

class JavaFileSystem implements FileSystem {
    // Set up the constants for the whence field in seek
    public static final int SEEK_SET    = 0;
    public static final int SEEK_CUR    = 1;
    public static final int SEEK_END    = 2;

    JavaFileSystem() {
        //throw new RuntimeException("not implemented");
    }

    // Initialize the the disk to a state representing an empty file-system.
    // Fill in the super block, mark all inodes as "unused", and link
    // all data blocks into the free list.
    public int formatDisk(int size, int iSize) {
        throw new RuntimeException("not implemented");
    } // formatDisk

    // Close all files and shut down the simulated disk.
    public int shutdown() {
        throw new RuntimeException("not implemented");
    } // shutdown

    // Create a new (empty) file and return a file descriptor.
    public int create() {
        throw new RuntimeException("not implemented");
    } // create

    // Return the inumber of an open file
    public int inumber(int fd) {
        throw new RuntimeException("not implemented");
    }

    // Open an existing file identified by its inumber
    public int open(int iNumber) {
        throw new RuntimeException("not implemented");
    } // open

    // Read up to buffer.length bytes from the open file indicated by fd,
    // starting at the current seek pointer, and update the seek pointer.
    // Return the number of bytes read, which may be less than buffer.length
    // if the seek pointer is near the current end of the file.
    // In particular, return 0 if the seek pointer is greater than or
    // equal to the size of the file.
    public int read(int fd, byte[] buffer) {
        throw new RuntimeException("not implemented");
    } // read

    // Transfer buffer.length bytes from the buffer to the file, starting
    // at the current seek pointer, and add buffer.length to the seek pointer.
    public int write(int fd, byte[] buffer) {
        throw new RuntimeException("not implemented");
    } // write

    // Update the seek pointer by offset, according to whence.
    // Return the new value of the seek pointer.
    // If the new seek pointer would be negative, leave it unchanged
    // and return -1.
    public int seek(int fd, int offset, int whence) {
        throw new RuntimeException("not implemented");
    } // seek

    // Write the inode back to disk and free the file table entry
    public int close(int fd) {
        throw new RuntimeException("not implemented");
    } // close

    // Delete the file with the given inumber, freeing all of its blocks.
    public int delete(int iNumber) {
        throw new RuntimeException("not implemented");
    } // delete

    public String toString() {
        throw new RuntimeException("not implemented");
    }
}
