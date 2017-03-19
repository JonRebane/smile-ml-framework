package stife.shapelet.evolution.evolution.alterers;

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

	public SmartShapeletAppender(List<Sequence> train,Random random,int epsilon){
		this.train = train;
		this.random = random;
		this.epsilon = epsilon;
	}
	
	@Override
	public NShapelet mutate(NShapelet t) {
		NShapelet newSHaplet = t.deepCopy();
		ArrayList<Sequence> shuffled = new ArrayList<>(train);
		Collections.shuffle(shuffled,random);
		for(Sequence seq : shuffled){
			List<List<Integer>> alloccurrences = seq.getAllOccurrences(t, epsilon);
			if(!alloccurrences.isEmpty()){
				//find something that can be appended to this shapelet and occurs at least once in this sequence
				//chosenOccurrence = //TODO: chose occurrence
			}
		}
		return null; //TODO: finish implementation
	}

}
