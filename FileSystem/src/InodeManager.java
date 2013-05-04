public class InodeManager {

	private Disk disk;
	private InodeBlock free;
	private int inumberFree;
	private Translator translator;
	private SuperBlock superBlock;

	public InodeManager(SuperBlock block, Translator translator, Disk disk) {
		this.translator = translator;
		this.superBlock = block;
		this.disk = disk;
	}
	
	



}
