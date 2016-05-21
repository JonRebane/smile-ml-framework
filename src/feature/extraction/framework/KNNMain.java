package feature.extraction.framework;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import distance.feature.extraction.exceptions.InvalidEventTableDimensionException;
import distance.feature.extraction.exceptions.TimeScaleException;
import representations.CompressedEventTable;
import representations.Sequence;

public class KNNMain {

	public static void main(String[] args) throws IOException, TimeScaleException, InvalidEventTableDimensionException{
		String pathToData = args[0];
		File superDir = new File(pathToData);
		Map<String,List<Double>> results = new HashMap<>();
		for(File dir : superDir.listFiles()){
			if(dir.isDirectory() && dir.getName().equals("AUSLAN2")){
				long dirBegin = System.currentTimeMillis();
				System.out.println("-------------------------------------------------------------------------------------------------");
				System.out.println("-------------------------------------------------------------------------------------------------");
				System.out.println("Beginning evaluation of "+dir.getName());
				List<Double> accuracies = new ArrayList<>();
				List<Integer> classIds = IOService.readClassData(dir);
				List<Sequence> sequences = IOService.readSequenceData(dir);
				int numDimensions = Sequence.getDimensionSet(sequences).size();
				List<CompressedEventTable> eventTables = getResizedEventTables(sequences, numDimensions);
				ArrayList<File> fileList = new ArrayList<>();
				for(File k_dir : dir.listFiles()){
					fileList.add(k_dir);
				}
				Collections.sort(fileList);
				for(File k_dir : fileList){
					if(k_dir.isDirectory()){
						System.out.println(k_dir);
						List<Integer> train = IOService.readTrainIndices(k_dir);
						List<Integer> test = getTestIndices(sequences,train);
						double curAccuracy = 0;
						for(Integer i :test){
							int minDistIndex = -1;
							double minDist = Double.MAX_VALUE;
							for(Integer j : train){
								double curDist = eventTables.get(i).euclidianDistance(eventTables.get(j));
								if(minDistIndex==-1 || curDist < minDist){
									minDistIndex = j;
									minDist = curDist;
								}
							}
							if(classIds.get(i) == classIds.get(minDistIndex)){
								curAccuracy++;
							}
						}
						curAccuracy = curAccuracy/test.size();
						System.out.println("current Accuracy " + curAccuracy);
						accuracies.add(curAccuracy);
					}
				}
				results.put(dir.getName(), accuracies);
				long dirEnd = System.currentTimeMillis();
				System.out.println("Finished Evaluation of "+dir.getName() + "in " + (dirEnd-dirBegin)/1000 + "s");
			}
		}
		printResults(results);
	}

	private static void printResults(Map<String, List<Double>> results) {
		for(String key : results.keySet()){
			System.out.println("-------------------------------------------------------------------");
			System.out.println(key);
			System.out.println("Exact accuracies");
			results.get(key).forEach(e -> System.out.print(e + " , "));
			System.out.println("mean accuracy");
			double meanAccuracy = 0;
			for(Double acc : results.get(key)){
				meanAccuracy +=acc;
			}
			System.out.println(meanAccuracy/results.get(key).size());
		}
	}

	private static List<CompressedEventTable> getResizedEventTables(List<Sequence> sequences, int numDimensions) {
		List<CompressedEventTable> resizedSequenceTables = new ArrayList<>();
		int maxDuration = -1;
		for(Sequence seq : sequences){
			maxDuration = Math.max(maxDuration,seq.duration());
		}
		for(Sequence seq : sequences){
			Sequence resizedSeq = new Sequence(seq);
			resizedSeq.rescaleTimeAxis(1, maxDuration);
			resizedSequenceTables.add(new CompressedEventTable(resizedSeq, numDimensions));
		}
		return resizedSequenceTables;
	}

	private static List<Integer> getTestIndices(List<Sequence> sequences, List<Integer> train) {
		List<Integer> test = new ArrayList<>();
		for(int i=0;i<sequences.size();i++){
			if(!train.contains(i)){
				test.add(i);
			}
		}
		return test;
	}
	
	
}
