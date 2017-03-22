package stife.shapelet.evolution.evolution.alterers;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import data_structures.Sequence;
import stife.shapelet.evolution.NShapelet;

public class SmartShapeletAppender implements MutationStrategy<NShapelet> {

	private List<Sequence> train;
	private Random random;
	private int epsilon;
	private ShapeletAppender shapeletAppender;

	public SmartShapeletAppender(List<Sequence> train,Random random,int maxEventId,int epsilon){
		this.train = train;
		this.random = random;
		this.epsilon = epsilon;
		this.shapeletAppender = new ShapeletAppender(random,maxEventId);
	}
	
	@Override
	public NShapelet mutate(NShapelet t) {
		NShapelet newShaplet = t.deepCopy();
		ArrayList<Sequence> shuffled = new ArrayList<>(train);
		Collections.shuffle(shuffled,random);
		for(Sequence seq : shuffled){
			List<List<Integer>> alloccurrences = seq.getAllOccurrences(newShaplet, epsilon);
			if(!alloccurrences.isEmpty()){
				//find something that can be appended to this shapelet and occurs at least once in this sequence
				List<Integer> chosenOccurrence = alloccurrences.get(random.nextInt(alloccurrences.size()));
				int lastIntervalId = chosenOccurrence.get(chosenOccurrence.size()-1);
				if(lastIntervalId == seq.intervalCount()-1){
					return shapeletAppender.mutate(newShaplet);
				}
				int newIntervalId = getRandom(lastIntervalId+1,seq.intervalCount());
				int relationship = seq.getRelationship(lastIntervalId, newIntervalId,epsilon);
				int dimension = seq.getInterval(newIntervalId).getDimension();
				newShaplet.append(dimension, relationship);
				return newShaplet;
			}
		}
		return shapeletAppender.mutate(newShaplet);
	}

	private int getRandom(int lowInclusive, int highExclusive) {
		return random.nextInt(highExclusive-lowInclusive) + lowInclusive;
	}

}
