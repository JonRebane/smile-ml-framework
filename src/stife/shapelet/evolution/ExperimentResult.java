package stife.shapelet.evolution;

public class ExperimentResult {
	
	private double evolvedAccuracy;
	private double exhaustiveAccuracy;
	private int numSize2ShapeletsInEvolved;
	private long trainingTime;
	private long exhaustiveTrainingTime;
	
	
	public ExperimentResult(double evolvedAccuracy, double exhaustiveAccuracy, int numSize2ShapeletsInEvolved, long trainingTime,long exhaustiveTrainingTime) {
		super();
		this.evolvedAccuracy = evolvedAccuracy;
		this.exhaustiveAccuracy = exhaustiveAccuracy;
		this.numSize2ShapeletsInEvolved = numSize2ShapeletsInEvolved;
		this.trainingTime = trainingTime;
		this.exhaustiveTrainingTime = exhaustiveTrainingTime;
	}
	
	public double getEvolvedAccuracy() {
		return evolvedAccuracy;
	}
	
	public void setEvolvedAccuracy(double evolvedAccuracy) {
		this.evolvedAccuracy = evolvedAccuracy;
	}
	
	public double getExhaustiveAccuracy() {
		return exhaustiveAccuracy;
	}
	
	public void setExhaustiveAccuracy(double exhaustiveAccuracy) {
		this.exhaustiveAccuracy = exhaustiveAccuracy;
	}
	
	public int getNumSize2ShapeletsInEvolved() {
		return numSize2ShapeletsInEvolved;
	}
	
	public void setNumSize2ShapeletsInEvolved(int numSize2ShapeletsInEvolved) {
		this.numSize2ShapeletsInEvolved = numSize2ShapeletsInEvolved;
	}

	public long getTrainingTime() {
		return trainingTime;
	}

	public void setTrainingTime(long trainingTime) {
		this.trainingTime = trainingTime;
	}

	public long getExhaustiveTrainingTime() {
		return exhaustiveTrainingTime;
	}

	public void setExhaustiveTrainingTime(long exhaustiveTrainingTime) {
		this.exhaustiveTrainingTime = exhaustiveTrainingTime;
	}	
}
