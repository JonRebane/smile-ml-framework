package data_structures.eseq;

public class Interval {
	
	private int start;
	private int end;
	private int field_id;
	private int value_id;
	private int unique_id;
	private String id;
	
	public Interval(int begin, int finish, int fid, int vid){
	 		start = begin;
	 		end = finish;
	 		field_id = fid;
	 		value_id = vid;
	 		
	 		if( fid > 100 || value_id > 10000)
	 			unique_id = -1;
	 		else
	 			unique_id = field_id*10000 + value_id;
	}

	public Interval(int begin, int finish, String Id){
 		start = begin;
 		end = finish;
 		this.id = new String(Id);
 		unique_id = Id.hashCode();
	}
	
	public int getStart(){return start;}
	public void setStart(int value){start = value;}
	public int getEnd(){return end;}
	public void setEnd(int value){end = value;}
	public int getFid(){return field_id;}
	public int getVid(){return value_id;}
	public int getUniqueId(){return unique_id;}
	public String getLabel(){return id;}
}

