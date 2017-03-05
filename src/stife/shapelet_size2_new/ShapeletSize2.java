package stife.shapelet_size2_new;

import java.util.List;

import data_structures.Pair;
import data_structures.Sequence;

public interface ShapeletSize2 {
	
	public static final int MEET = 1;
	public static final int MATCH = 2;
	public static final int OVERLAP = 3;
	public static final int LEFTCONTAINS = 4;
	public static final int CONTAINS = 5;
	public static final int RGHTCONTAINS = 6;
	public static final int FOLLOWEDBY = 7;

	public int getEventId1();
	
	public int getEventId2();
	
	public int getRelationship();
	
	public String getRelationshipName();
	
	
	/***
	 * Returns the interval ids of all intervals in the sequence that form occurrences of this 2-shapelet
	 * @param sequence
	 * @param intervalId
	 * @param epsilon
	 * @return
	 */
	public List<Pair<Integer,Integer>> getAllOccurrences(Sequence sequence,int epsilon);

	public List<Integer> getOccurrences(Sequence sequence, int firstEventIntervalId,int epsilon);

	
}
