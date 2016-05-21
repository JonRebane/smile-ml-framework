package data_structures;

import java.util.Comparator;

public class StandardIntervalComparator implements Comparator<Interval> {

	@Override
	public int compare(Interval i1, Interval i2) {
		if(i1.getStart() < i2.getStart()){
			return -1;
		} else if(i1.getStart() > i2.getStart()){
			return 1;
		} else{
			if(i1.getEnd() < i2.getEnd()){
				return -1;
			} else if(i1.getEnd() > i2.getEnd()){
				return 1;
			} else{
				if(i1.getDimension() < i2.getDimension()){
					return -1;
				} else if(i1.getDimension() > i2.getDimension()){
					return 1;
				} else{
					return 0;
				}
			}
		}
	}

}
