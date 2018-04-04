package data_structures.eseq;

public class Tuple {
	    private Integer[] data;
	    public Tuple(int key, int count){this.data = new Integer[2]; this.data[0]=key; this.data[1]=count; }
	    public int get(int index) { return this.data[index]; }
	    public int getKey(){return this.data[0];}
	    public Integer getCount(){return this.data[1];}
	    public String toString(){return "("+data[0].toString()+","+data[1].toString()+")"; }
}
