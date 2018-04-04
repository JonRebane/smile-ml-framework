package data_structures.eseq;

import java.util.ArrayList;

public class Esequence {

	//import "interval";


	public ArrayList<Interval> tracks;
	public ArrayList<Integer> uniqueIDs;
	public ArrayList<String> uniqueLabels;
	private int start;
	private int end;
	private int id;
	private String phrase;
		
	//Create new utterance
	public Esequence( int ID, int begin, int finish)
	{
		tracks = new ArrayList<Interval>();
		uniqueIDs = new ArrayList<Integer>();
		uniqueLabels = new ArrayList<String>();
		start = begin;
		end = finish;
		id = ID;
		phrase = null;
	}
		
		//Getters
	public int getStart(){ return start;}
	public int getEnd(){ return end;}
	public int getId(){ return id;}
	public void addInterval(Interval interval)
	{
		tracks.add(interval);
		if( ! uniqueIDs.contains(interval.getUniqueId()))
				uniqueIDs.add(interval.getUniqueId());
		if( ! uniqueLabels.contains(interval.getLabel()))
				uniqueLabels.add( interval.getLabel());
		if( interval.getEnd() > end)
				end = interval.getEnd();
		if( interval.getStart() < start)
				start = interval.getStart();
	}
	public Interval getInterval(int i){return tracks.get(i);}
	public void setPhrase(String p){phrase = p;}
	public String getPhrase(){return phrase;}
	
}
