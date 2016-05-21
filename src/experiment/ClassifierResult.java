package experiment;

import java.util.LinkedList;
import java.util.List;

public class ClassifierResult {
	
	private String classifierName;
	private List<Double> accuracyValues = new LinkedList<>();
	private List<Long> classificationTimes = new LinkedList<>(); //unit is nanoseconds
	
	public ClassifierResult(String classifierName) {
		super();
		this.classifierName = classifierName;
	}
	
	public void addAccuracyValue(double accuracy){
		accuracyValues.add(accuracy);
	}
	
	public void addClassificationTime(long time){
		classificationTimes.add(time);
	}

	public String getClassifierName() {
		return classifierName;
	}

	public double meanAccuracy() {
		double accuracySum = 0;
		for(Double acc : accuracyValues){
			accuracySum += acc;
		}
		return accuracySum / accuracyValues.size();
	}

	public double meanClassificationTimeMS() {
		long timeSum = 0;
		for(Long time : classificationTimes){
			timeSum += time;
		}
		return timeSum / (classificationTimes.size() * 1000000.0);
	}

}
