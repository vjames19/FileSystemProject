class IndirectBlock {
	public static final int NUMBER_OF_POINTERS = Disk.BLOCK_SIZE/4;
	public int pointer[] = new int[NUMBER_OF_POINTERS];

	public IndirectBlock() {
		clear();
	}

	public void clear() {
		for (int i = 0; i < NUMBER_OF_POINTERS; i++) {
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
