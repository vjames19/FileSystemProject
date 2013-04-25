
/**
 * TO DO: Add getter/setter methods so to declare the data element as private
 *
 */
class InodeBlock {
	Inode node[] = new Inode[Disk.BLOCK_SIZE/Inode.SIZE];

	public InodeBlock() {
		for (int i = 0; i < Disk.BLOCK_SIZE/Inode.SIZE; i++)
			node[i] = new Inode();
	}

	public String toString() {
		String s = "INODEBLOCK:\n";
		for (int i = 0; i < node.length; i++)
			s += node[i] + "\n";
		return s;
	}
}

