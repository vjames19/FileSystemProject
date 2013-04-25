import java.util.*;
import java.io.*;

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
    
    public FileTable(){
	fdArray = new FileDescriptor[MAX_FILES];
	bitmap = new int[MAX_FILES];
	for(int i = 0; i < MAX_FILES; i++){
	    bitmap[i] = 0;
	}
    }
    
    //returns the index in the file table
    public int allocate(){
	for(int i = 0; i < MAX_FILES; i++){
	    if(bitmap[i] == 0)
		return i;
	}
	System.out.println("Cannot open file... filetable is full.");
	return -1; //filetable is full already.
    }
    
    //add will overwrite. **********!!!!!!!!!
    public int add(Inode inode, int inumber, int fd){
	if(bitmap[fd] != 0)
	    return -1;
	bitmap[fd] = 1;
	fdArray[fd] = new FileDescriptor(inode, inumber);
	return 0;  //SUCCESS this time.
    }
    

    public void free(int fd){
	bitmap[fd] = 0;
    }

    //returns true if it is valid, false if not.
    public boolean isValid(int fd){
	if (fd < 0 || fd >= MAX_FILES) {
	    System.out.println("ERROR: Invalid file descriptor (must be 0 <= fd <= "+ MAX_FILES + ") : " + fd);
	    return false;
	}
	else if (bitmap[fd] > 0){
	    return true;
	}
	else{
	    return false;
	}
    }

    public Inode getInode(int fd){
	if(bitmap[fd] == 0){
	    return null;
	}
	else{
	    return fdArray[fd].getInode();
	}
    }

    public int getInumber(int fd){
	if(bitmap[fd] == 0){
	    return 0;   //ERROR, if invalid
	}
	else{
	    return fdArray[fd].getInumber();
	}
    }

    public int getSeekPointer(int fd){
	if(bitmap[fd] == 0){
	    return 0;  //invalid
	}
	else{
	    return fdArray[fd].getSeekPointer();
	}
    }

    public int setSeekPointer(int fd, int newPointer) {
	if(bitmap[fd] == 0){
	    return 0;  //invalid
	}
	else{
	    fdArray[fd].setSeekPointer(newPointer);
	    return 1;  //valid
	}
    }

    public int setFileSize(int fd, int newFileSize){
	if(bitmap[fd] == 0){
	    return 0; //invalid
	}
	else{
	    fdArray[fd].setFileSize(newFileSize);
	    return 1;
	}
    }

    public int getFDfromInumber(int inumber){       
	for(int i = 0; i < MAX_FILES; i++){
	    if(bitmap[i] == 1){
		if (fdArray[i].getInumber() == inumber){
		    return i;
		}
	    }	    
	}   
	return -1;
    }
}


