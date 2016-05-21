package experiment;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import data_structures.Sequence;

public class ExperimentUtil {


	public static long getCpuTime( ) {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
	    return bean.getCurrentThreadCpuTime();
	}
	
	public static List<Integer> getShuffledIndices(List<Sequence> sequences, Random random) {
		List<Integer> shuffledIndices = new ArrayList<>();
		for(int i = 0;i<sequences.size();i++){
			shuffledIndices.add(i);
		}
		Collections.shuffle(shuffledIndices, random);
		assert(shuffledIndices.size()==sequences.size());
		return shuffledIndices;
	}
	
	public static List<Integer> getTestIndices(int size, List<Integer> trainIndices) {
		return getTestIndices(getAllIndices(size),trainIndices);
	}
	
	private static List<Integer> getAllIndices(int size) {
		List<Integer> list = new ArrayList<>();
		for(int i=0;i<size;i++){
			list.add(i);
		}
		return list;
	}

	public static List<Integer> getTestIndices(List<Integer> allIndices, List<Integer> trainIndices) {
		Set<Integer> testSet = new LinkedHashSet<>(allIndices);
		testSet.removeAll(trainIndices);
		return new ArrayList<>(testSet);
	}

	public static <E> List<E> getAll(List<E> database, List<Integer> indices) {
		List<E> subList = new ArrayList<>();
		for(Integer i : indices){
			subList.add(database.get(i));
		}
		return subList;
	}

	public static List<Integer> getTrainingIndices(List<Integer> allIndices, int i,int k) {
		List<Integer> trainIndices = new ArrayList<>();
		trainIndices.addAll(allIndices.subList(0, i*allIndices.size()/k));
		trainIndices.addAll(allIndices.subList((i+1)*allIndices.size()/k, allIndices.size()));
		trainIndices = Collections.unmodifiableList(trainIndices);
		return trainIndices;
	}
	
	public static List<Integer> generateRandomBinaryClassIds(int size, Random random) {
		List<Integer> classIds = new ArrayList<>(size);
		for(int i=0;i<size;i++){
			classIds.add(random.nextInt(2)+1);
		}
		return classIds;
	}
}
