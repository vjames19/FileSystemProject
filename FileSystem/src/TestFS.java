/** Basic driver program to be used with the Java file system project.
 **    It can be run in two modes:
 **        Interactive:         java TestFS 
 **        With a test file:    java TestFS ./tests/testfile    
 **     
 **    To get a list of supported commands, type 'help' at the command line.
 **
 **    The testfile consists of commands to the driver program (one per line)
 **      as well as comments.  Comments beginning with /* will be ignored
 **      completely by the driver.  Comments beginning with // will be echoed
 **      to the output.  See the sample testfile for an example.
 **/

import java.io.*;
import java.util.*;

class TestFS 
{
    // File System object to be used for the function calls
    private static FileSystem fs;

    // Table mapping variables to values
    private static Hashtable vars = new Hashtable();

    public static void main(String [] args){

        // Check for correct number of arguments
        if (args.length > 1) System.err.println ("Usage: TestFS [filename]");

        // Is the input coming from a file
        boolean fromFile = (args.length==1);

        // Create our test fileSystem
        fs = new JavaFileSystem();

        // Create a stream for input
        BufferedReader data = null;

        // Open our input stream
        if (fromFile) {
            try {
                data = new BufferedReader (new FileReader(new File(args[0])));
            }
            catch (FileNotFoundException e) {
                System.err.println("Error: File " + args[0] + " not found.");
                System.exit(1);
            }
        }
        else data = new BufferedReader (new InputStreamReader(System.in));

        // Cycle through user or file input
        for (;;) {
            try {
                // Print out the prompt for the user
                if (!fromFile) {
                    System.out.print("--> ");
                    System.out.flush();
                }

                // Read in a line
                String line = data.readLine();
                //System.out.println(line);

                // Check for various conditions
                if (line == null) System.exit(0);  // Ctrl-D check
                line = line.trim();                // Trim off extra whitespace
                if (line.length() == 0) {          // Is anything left?
                    System.out.println();
                    continue;
                }

                // Handle comments and file input
                if (line.startsWith("//")) {
                    if (fromFile)
                        System.out.println(line);
                    continue;
                }
                if (line.startsWith("/*")) continue;
                if (fromFile) System.out.println("--> " + line);

                // See if the command has the format of an assignment
                String target = null;
                int equals = line.indexOf('=');
                if (equals > 0) {
                    target = line.substring(0,equals).trim();
                    line = line.substring(equals+1).trim();
                }

                // Tokenize command line
                StringTokenizer cmds = new StringTokenizer (line);
                String cmd = cmds.nextToken();

                // Call the function that corresponds to the command
                int result = 0;
                if (cmd.equalsIgnoreCase("formatDisk")
                        || cmd.equalsIgnoreCase("format"))
                {
                    int arg1 = nextValue(cmds);
                    int arg2 = nextValue(cmds);
                    result = fs.formatDisk(arg1,arg2);
                }
                else if (cmd.equalsIgnoreCase("shutdown")) {
                    result = fs.shutdown();
                }
                else if (cmd.equalsIgnoreCase("create")) {
                    result = fs.create();
                }
                else if (cmd.equalsIgnoreCase("open")) {
                    result = fs.open(nextValue(cmds));
                } 
                else if (cmd.equalsIgnoreCase("inumber")) {
                    result = fs.inumber(nextValue(cmds));
                } 
                else if (cmd.equalsIgnoreCase("read")) {
                    int arg1 = nextValue(cmds);
                    int arg2 = nextValue(cmds);
                    result = readTest(arg1,arg2);
                } 
                else if (cmd.equalsIgnoreCase("write")) {
                    int arg1 = nextValue(cmds);
                    String arg2 = cmds.nextToken();
                    int arg3 = nextValue(cmds);
                    result = writeTest(arg1,arg2,arg3);
                } 
                else if (cmd.equalsIgnoreCase("seek")) {
                    int arg1 = nextValue(cmds);
                    int arg2 = nextValue(cmds);
                    int arg3 = nextValue(cmds);
                    result = fs.seek(arg1,arg2,arg3);
                } 
                else if (cmd.equalsIgnoreCase("close")) {
                    result = fs.close(nextValue(cmds));
                } 
                else if (cmd.equalsIgnoreCase("delete")) {
                    result = fs.delete(nextValue(cmds));
                } 
                else if (cmd.equalsIgnoreCase("quit")) {
                    System.exit(0);
                } 
                else if (cmd.equalsIgnoreCase("vars")) {
                    for (Enumeration e = vars.keys(); e.hasMoreElements(); ) {
                        Object key = e.nextElement();
                        Object val = vars.get(key);
                        System.out.println("\t" + key + " = " + val);
                    }
                    continue;
                }
                else if (cmd.equalsIgnoreCase("help")) {
                    help();
                    continue;
                } 
                else {
                    System.out.println("unknown command");
                    continue;
                }

                // Print out the result of the function call
                if (target == null)
                    System.out.println("    Result is " + result);
                else {
                    vars.put(target,new Integer(result));
                    System.out.println("    " + target + " = " + result);
                }
            }
            // Handler for Integer.parseInt(...)
            catch (NumberFormatException e) {
                System.out.println("Incorrect argument type");
            }
            // Handler for nextToken()
            catch (NoSuchElementException e) {
                System.out.println("Incorrect number of elements");
            }
            catch (IOException e) {
                System.err.println(e);
            }
        }
    } // main

    /** Helper function for main, to interpret a command argument */
    static private int nextValue(StringTokenizer cmds)
    {
        String arg = cmds.nextToken();
        Object val = vars.get(arg);
        return
            (val == null) ?  Integer.parseInt(arg) : ((Integer)val).intValue();
    }

    /** help will just print out a listing of the commands available on
     ** the system.
     **/
    private static void help() {
        System.out.println ("\tformatDisk size iSize");
        System.out.println ("\tshutdown");
        System.out.println ("\tcreate");
        System.out.println ("\topen inum");
        System.out.println ("\tinumber fd");
        System.out.println ("\tread fd size");
        System.out.println ("\twrite fd pattern size");
        System.out.println ("\tseek fd offset whence");
        System.out.println ("\tclose fd");
        System.out.println ("\tdelete inum");
        System.out.println ("\tquit");
        System.out.println ("\tvars");
        System.out.println ("\thelp");
    }

    /** readTest will read in a buffer from the disk and print the contents
     ** to the screen.  It will return the number of bytes read or a -1 on
     ** error.
     **/
    private static int readTest(int fd, int size) {
        byte[] buffer = new byte[size];
        int length;

        // Fill buffer with junk first
        for (int i = 0; i < size; i++)
            buffer[i] = (byte) '*';
        length = fs.read(fd, buffer);
        for (int i = 0; i < length; i++) 
            showchar(buffer[i]);
        if (length != -1) System.out.println();
        return length;
    }

    /** writeTest will create a buffer and fill it with repeating 
     ** copies of str.  This buffer will then be written to disk.  
     ** It will return the number of bytes written or a -1 on error.
     **/
    private static int writeTest (int fd, String str, int size) {
        byte[] buffer = new byte[size];

        for (int i = 0; i < buffer.length; i++) 
            buffer[i] = (byte)str.charAt(i % str.length());

        return fs.write(fd, buffer);
    }
    
    /** showchar will print an ASCII representation of the byte, b, to
     ** the screen.
     **/
    private static void showchar(byte b) {
        if (b < 0) {
            System.out.print("M-");
            b += 0x80;                // Make b positive
        }
        if (b >= ' ' && b <= '~') {
            System.out.print((char)b);
            return;
        }
        switch (b) {
            case '\0': System.out.print("\\0"); return;
            case '\n': System.out.print("\\n"); return;
            case '\r': System.out.print("\\r"); return;
            case '\b': System.out.print("\\b"); return;
            case 0x7f: System.out.print("\\?"); return;
            default:   System.out.print("^" + (char)(b + '@')); return;
        }
    }
}
