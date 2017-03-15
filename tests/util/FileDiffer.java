package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileDiffer {

	private File f2;
	private File f1;

	public FileDiffer(File f1, File f2) {
		this.f1 = f1;
		this.f2 = f2;
	}

	public void searchFirstDiff() throws IOException {
		BufferedReader br1 = new BufferedReader(new FileReader(f1));
		BufferedReader br2 = new BufferedReader(new FileReader(f2));
		int lineCount = 1;
		String line1 = br1.readLine();
		String line2 = br2.readLine();
		while(line1!=null && line2!=null){
			if(!line1.equals(line2)){
				System.out.println("difference at line " + lineCount);
				break;
			}
			lineCount++;
			line1 = br1.readLine();
			line2 = br2.readLine();
		}
		br1.close();
		br2.close();
	}

}
