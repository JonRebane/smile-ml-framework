package stife.shapelet_size2;

/***
 * Class used by the feature selection to sort the column indices of the shapeletFeatureMatrix by their column's information gain.
 * @author leon bornemann
 *
 */
public class IndexGainPair implements Comparable<IndexGainPair> {

	private int index;
	private double informationGain;
	
	public IndexGainPair(int index, double informationGain) {
		super();
		this.index = index;
		this.informationGain = informationGain;
	}

	public int getIndex() {
		return index;
	}

	public double getInformationGain() {
		return informationGain;
	}

	@Override
	public int compareTo(IndexGainPair other) {
		if(informationGain < other.getInformationGain()){
			return -1;
		} else if(informationGain == other.getInformationGain()){
			if(index < other.getIndex()){
				return -1;
			} else if( index == other.getIndex()){
				return 0;
			} else{
				return 1;
			}
		} else{
			return 1;
		}
	}	
}
