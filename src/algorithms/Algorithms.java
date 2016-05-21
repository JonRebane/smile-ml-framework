package algorithms;

public class Algorithms {

	public static int linearInterpolation(int oldScaleMin, int oldScaleMax, int oldVal, int newScaleMin, int newScaleMax) {
		int factor = newScaleMax-newScaleMin;
		double numenator = (double) (oldVal - oldScaleMin);
		double denomenator = (double) (oldScaleMax-oldScaleMin);
		double newVal = newScaleMin + factor* numenator / denomenator;
		//since we use integers as our time scale we need to round, this can cause some minor mistakes, but these will be consistent for all sequences, so no great harm should be done by it
		return (int) Math.round(newVal);
	}

	public static void vectorDivision(double[] values, double denumenator) {
		for(int i=0;i<values.length;i++){
			values[i] = values[i] / denumenator;
		}
	}

}
