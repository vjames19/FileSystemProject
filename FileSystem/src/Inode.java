import java.util.Arrays;


/**
 * TODO: This class has to add getter/setter methods for the data elements
 * to allow the elements to be declared as private
 *
 */
class Inode {
	public final static int SIZE = 64;	// size in bytes
	int flags;
	int owner;
	int fileSize;
	int pointer[] = new int[13];

	public String toString() {
		String s = "[Flags: " + flags
		+ "  Size: " + fileSize + "  ";
		for (int i = 0; i < 13; i++) 
			s += "|" + pointer[i];
		return s + "]";
	}
	
	public void init(){
		flags =1;
		fileSize = 0;
		clear();
	}
	public void clear(){
		Arrays.fill(pointer, 0);
	}
	
	public void makeEmpty(){
		flags = 0;
		fileSize =0;
		clear();
	}
	
	public boolean isInUse(){
		return flags == 1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + fileSize;
		result = prime * result + flags;
		result = prime * result + owner;
		result = prime * result + Arrays.hashCode(pointer);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Inode other = (Inode) obj;
		if (fileSize != other.fileSize)
			return false;
		if (flags != other.flags)
			return false;
		if (owner != other.owner)
			return false;
		if (!Arrays.equals(pointer, other.pointer))
			return false;
		return true;
	}
	
	
}
