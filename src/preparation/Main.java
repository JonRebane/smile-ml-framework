package preparation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {

	public static void main(String[] args) throws IOException {
		File dir = new File("multiLabelDatasets");
		for(File file : dir.listFiles()){
			if(file.isDirectory()){
				File classFile = new File(file.getAbsolutePath()+File.separator+"classes.txt");
				File dataFile = new File(file.getAbsolutePath()+File.separator+"data.txt");
				BufferedReader brClass = new BufferedReader(new FileReader(classFile));
				BufferedReader brData = new BufferedReader(new FileReader(dataFile));
				PrintWriter prClass = new PrintWriter(new File(file.getAbsolutePath() +File.separator+"classes_clean.txt"));
				PrintWriter prData = new PrintWriter(new File(file.getAbsolutePath() +File.separator+"data_clean.txt"));
				String lineClass = brClass.readLine().trim();
				String lineData = brData.readLine();
				while(lineClass!=null){
					String[] tokensClass = lineClass.split("\\s+");
					String[] tokensData = lineData.split("\\s+");
					assert( equalityCondition(file,tokensClass,tokensData));
					do{
						assert(tokensData.length==4);
						 if(tokensClass.length>=2){
							prData.print(tokensData[0] + " " + tokensData[1] + " " + tokensData[2] + " " + tokensData[3]);
						 }
						 lineData = brData.readLine();
						 if(lineData!=null){
							 tokensData = lineData.split("\\s+");
						 }
						 if(lineData !=null && tokensClass.length>=2){
							 //we have more to write -> newline
							 prData.println();
						 }
					} while(lineData !=null && equalityCondition(file,tokensClass,tokensData));
					if(tokensClass.length>=2){
						for(int i=1;i<tokensClass.length;i++){
							prClass.print(tokensClass[i]);
							if(i!=tokensClass.length-1){
								prClass.print(" ");
							}
						}
					}
					lineClass = brClass.readLine();
					if(lineData==null){
						assert(lineClass==null);
					} else{
						lineClass=lineClass.trim();
						if(tokensClass.length>=2){
							prClass.println();
						}
					}
				}
				prClass.close();
				prData.close();
				brClass.close();
				brData.close();
			}
		}
	}

	private static boolean equalityCondition(File file, String[] tokensClass, String[] tokensData) {
		if(file.getName().equals("ASL-BU")){
			return Integer.parseInt(tokensClass[0])==Integer.parseInt(tokensData[0]);
		} else if(file.getName().equals("ASL-BU-2")){
			return Integer.parseInt(tokensClass[0])==Integer.parseInt(tokensData[0])+1;
		} else{
			assert(false);
			return false;
		}
	}
}
