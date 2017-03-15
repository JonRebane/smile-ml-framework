package shapelet.evolution.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyRandom extends Random {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	List<List<Object>> generatedValues = new ArrayList<>();
	List<Integer> nextDoubleCalls = new ArrayList<>();
	List<Integer> nextIntCalls = new ArrayList<>();
	List<Integer> nextBooleanCalls = new ArrayList<>();
	int index = 0;

	private long seed;
	
	public MyRandom(long seed){
		super(seed);
		this.seed = seed;
		generatedValues.add(new ArrayList<>());
		nextDoubleCalls.add(0);
		nextIntCalls.add(0);
		nextBooleanCalls.add(0);
	}
	
	@Override
	public int nextInt(int bound){
		int val = super.nextInt(bound);
		generatedValues.get(index).add(val);
		nextIntCalls.set(index, nextIntCalls.get(index)+1);
		if(index != 0){
			int elementIndex = generatedValues.get(index).size()-1;
			boolean mustHold = generatedValues.get(index-1).get(elementIndex).equals(val);
			assert mustHold : "error at index " + elementIndex + " " + generatedValues.get(index-1).get(elementIndex) + " is not equal to the new " + val;
		}
		return val;
	}
	
	@Override
	public double nextDouble(){
		double val = super.nextDouble();
		generatedValues.get(index).add(val);
		nextDoubleCalls.set(index, nextDoubleCalls.get(index)+1);
		return val;
	}
	
	@Override
	public boolean nextBoolean(){
		boolean val = super.nextBoolean();
		generatedValues.get(index).add(val);
		nextBooleanCalls.set(index, nextBooleanCalls.get(index)+1);
		return val;
	}

	public List<Object> getGeneratedValues(int index) {
		return generatedValues.get(index);
	}

	public void restart() {
		setSeed(seed);
		index++;
		generatedValues.add(new ArrayList<>());
		nextDoubleCalls.add(0);
		nextIntCalls.add(0);
		nextBooleanCalls.add(0);
	}
	
	
}
