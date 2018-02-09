package stife.shapelet_size2;

public class Shapelet_Size2 {
	
	private int eventId1;
	private int eventId2;
	private int relationshipId;
	private int aStart;
	private int bStart;
	private int aEnd;
	private int bEnd;

	public Shapelet_Size2(int eventId1, int eventId2, int relationshipId) {
		this(eventId1, eventId2, relationshipId, -1, -1, -1, -1);
	}

	public Shapelet_Size2(int eventId1, int eventId2, int relationshipId, int aStart, int bStart, int aEnd, int bEnd) {
		super();
		this.eventId1 = eventId1;
		this.eventId2 = eventId2;
		this.relationshipId = relationshipId;
		this.aStart = aStart;
		this.bStart = bStart;
		this.aEnd = aEnd;
		this.bEnd = bEnd;
	}

	public int getALen() {
		return Math.max(1, aEnd - aStart);
	}

	public int getBlen() {
		return Math.max(1, bEnd - bStart);
	}

	public int getaStart() {
		return aStart;
	}

	public int getbStart() {
		return bStart;
	}

	public int getaEnd() {
		return aEnd;
	}

	public int getbEnd() {
		return bEnd;
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
