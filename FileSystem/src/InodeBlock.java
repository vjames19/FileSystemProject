import java.util.Arrays;


/**
 * TO DO: Add getter/setter methods so to declare the data element as private
 *
 */
class InodeBlock {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(node);
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
		InodeBlock other = (InodeBlock) obj;
		if (!Arrays.equals(node, other.node))
			return false;
		return true;
	}

	public static final int INODES_IN_BLOCK = Disk.BLOCK_SIZE/Inode.SIZE;
	Inode node[] = new Inode[INODES_IN_BLOCK];

	public InodeBlock() {
		for (int i = 0; i < INODES_IN_BLOCK; i++)
			node[i] = new Inode();
	}

	public String toString() {
		String s = "INODEBLOCK:\n";
		for (int i = 0; i < node.length; i++)
			s += node[i] + "\n";
		return s;
	}
}

