package stife.shapelet.evolution;

public class ExperimentResult {
	
	private double evolvedAccuracy;
	private double exhaustiveAccuracy;
	private int numSize2ShapeletsInEvolved;
	
	public ExperimentResult(double evolvedAccuracy, double exhaustiveAccuracy, int numSize2ShapeletsInEvolved) {
		super();
		this.evolvedAccuracy = evolvedAccuracy;
		this.exhaustiveAccuracy = exhaustiveAccuracy;
		this.numSize2ShapeletsInEvolved = numSize2ShapeletsInEvolved;
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
}
