package distance.feature.extraction;

/***
 * Class that contains an interesting point of time in a sequence, which can be either the start or the end of an interval.
 * Events are ordered by their points of time, if this is equal, start occurs before end
 * @author leon bornemann
 *
 */
public class Event implements Comparable<Event>{

	private int pointOfTime;
	private EventType type;
	private int dimension;

	public Event(int pointOfTime, int dimension, EventType type) {
		this.pointOfTime = pointOfTime;
		this.dimension = dimension;
		this.type = type;
	}

	public int getPointOfTime() {
		return pointOfTime;
	}

	public int getDimension() {
		return dimension;
	}
	
	public EventType getEventType() {
		return type;
	}

	@Override
	public int compareTo(Event other) {
		if(pointOfTime < other.getPointOfTime()){
			return -1;
		} else if(pointOfTime > other.getPointOfTime()){
			return 1;
		} else {
			if(type == other.getEventType()){
				//TODO: order by dimension as well?
				return 0;
			} else if(type == EventType.Start){
				//we are start and the other one is end, we occur first
				return -1;
			} else{
				//we are end and the other one is start, therefore the other event occurs first
				return 1;
			}
		}
	}

	public boolean isStart() {
		return type == EventType.Start;
	}
	
	public boolean isEnd() {
		return type == EventType.End;
	}
}
