package stife.shapelet;

public class Shapelet {
	
	private int eventId1;
	private int eventId2;
	private int relationshipId;
	
	public Shapelet(int eventId1, int eventId2, int relationshipId) {
		super();
		this.eventId1 = eventId1;
		this.eventId2 = eventId2;
		this.relationshipId = relationshipId;
	}

	public int getEventId1() {
		return eventId1;
	}

	public int getEventId2() {
		return eventId2;
	}

	public int getRelationshipId() {
		return relationshipId;
	}

}
