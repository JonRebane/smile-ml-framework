package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import data_structures.Sequence;

/***
 * Class that bundles all the IO-Stuff that needs to be done, that does not fit anywhere else (does not have a class to which the IO-operation belongs)
 * @author Leon Bornemann
 *
 */
public class IOService {

	public static void createIfNotExists(String dirName) {
		File dir = new File(dirName);
		if(!dir.exists()){
			dir.mkdir();
		}
	}

	public static List<Integer> readClassData(File dataDir) throws IOException {
		String classPath = dataDir.getAbsolutePath() + File.separator +"classes.txt";
		assert(new File(classPath).exists());
		return readIntegerList(classPath);
	}

	private static List<Integer> readIntegerList(String path) throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		List<Integer> classIds = new ArrayList<>();
		String line = br.readLine();
		while(line!=null){
			classIds.add(Integer.parseInt(line));
			line = br.readLine();
		}
		br.close();
		return Collections.unmodifiableList(classIds);
	}
	
	public static List<Sequence> readSequenceData(File dataDir) throws IOException {
		String dataPath = dataDir.getAbsolutePath() + File.separator +"dataSorted.txt";
		assert(new File(dataPath).exists());
		List<Sequence> sequences = Sequence.readSequenceData(dataPath);
		return sequences;
	}

	public static void writeTrainIndices(Collection<Integer> trainIndices, String targetPath) throws IOException {
		PrintWriter pr = new PrintWriter(new FileWriter(new File(targetPath)));
		Iterator<Integer> it = trainIndices.iterator();
		while(it.hasNext()){
			Integer curElem = it.next();
			if(it.hasNext()){
				pr.println(curElem);
			} else{
				pr.print(curElem);
			}
		}
		pr.close();
	}
	
	public static List<Integer> readTrainIndices(File dir) throws FileNotFoundException, IOException{
		String dataPath = dir.getAbsolutePath() + File.separator +"trainIndices.txt";
		assert(new File(dataPath).exists());
		return readIntegerList(dataPath);
	}

	public static List<List<Integer>> readMultiLabelClassData(File dir) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(dir.getAbsolutePath()+File.separator+"classes_clean.txt")));
		List<List<Integer>> classIds = new ArrayList<>();
		String line = br.readLine();
		while(line!=null){
			if(line.equals("")){
				break;
			} else{
				ArrayList<Integer> curClassIds = new ArrayList<>(); 
				String[] tokens = line.split("\\s+");
				for(int i=0;i<tokens.length;i++){
					curClassIds.add(Integer.parseInt(tokens[i]));
				}
				classIds.add(curClassIds);
				line = br.readLine();
			}
		}
		br.close();
		return Collections.unmodifiableList(classIds);
	}

	public static List<Sequence> readMultiLabelSequenceData(File dir) throws IOException {
		String dataPath = dir.getAbsolutePath() + File.separator +"data_clean.txt";
		assert(new File(dataPath).exists());
		List<Sequence> sequences = Sequence.readSequenceData(dataPath);
		//reassign dimension labels to be ascending numbers from 1 to size.
		TreeSet<Integer> allDimensions = new TreeSet<>(Sequence.getDimensionSet(sequences));
		HashMap<Integer,Integer> newDimensionMapping = new HashMap<>();
		Iterator<Integer> it = allDimensions.iterator();
		int i=1;
		while(it.hasNext()){
			Integer curOld = it.next();
			Integer curNew = i;
			newDimensionMapping.put(curOld, curNew);
			i++;
		}
		assert(new HashSet<>(newDimensionMapping.values()).size() == allDimensions.size());
		assert(i==allDimensions.size()+1);
		for(Sequence seq:sequences){
			seq.reassignDimensionIds(newDimensionMapping);
		}
		return sequences;
	}

	public static void appendLineToFile(File file, String line) throws IOException {
		PrintWriter pr = new PrintWriter(new FileWriter(file,true));
		pr.println(line);
		pr.close();
	}
}
