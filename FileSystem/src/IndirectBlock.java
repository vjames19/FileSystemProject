class IndirectBlock {
	public int pointer[] = new int[Disk.BLOCK_SIZE/4];

	public IndirectBlock() {
		clear();
	}

	public void clear() {
		for (int i = 0; i < Disk.BLOCK_SIZE/4; i++) {
			pointer[i] = 0;
		}
	}

	public String toString() {
		String s = new String();
		s += "INDIRECTBLOCK:\n";
		for (int i = 0; i < pointer.length; i++)
			s += pointer[i] + "|";
		return s;
	}
}
