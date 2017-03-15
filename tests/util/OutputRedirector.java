package util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class OutputRedirector {

	private PrintStream outStream;
	private PrintStream consoleStream;

	public OutputRedirector(File outFile) throws FileNotFoundException {
		super();
		consoleStream = System.out;
		outStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)));
	}

	public void redirectSysout(){
		System.setOut(outStream);
	}
	
	public void terminateRedirection(){
		outStream.flush();
		outStream.close();
		System.setOut(consoleStream);
	}
}
