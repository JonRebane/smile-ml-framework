package data_structures.eseq;

import data_structures.Sequence;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;

public class Estreams_Euclidean4 {
	
	
	
	public static void main(String[] args) {
		
		List<DefaultHashMap<String,Short>> huge = new ArrayList<DefaultHashMap<String,Short>>();
		// read e-sequences from file
		if ( args.length > 2 && args[2].startsWith("direct"))
		{ 
			System.out.println("Reading directing into vectors; file " + args[0]);
			huge = readFileIntoVectors(args[0]); 
		}
		else{
			ArrayList<Esequence> esequences = readFile(args[0]);
			System.out.println("Read file" + args[0]);
			// transform e-sequences to vector form
			for(int i=0; i < esequences.size(); i++)
			{
				ArrayList<DefaultHashMap<String,Short>> vec_seq = transform_to_vectors(esequences.get(i));
				for(int j=0; j < vec_seq.size(); j++)
				{
					DefaultHashMap<String,Short> curr_vec = vec_seq.get(j);
					if(curr_vec.size() > 0) //remove empty time points
						huge.add(curr_vec);
				}
				vec_seq = null;
			}
			esequences = null;
		}
		// Now 'huge' should contain the dataset in one big chunk of vectors
		System.out.println("Created vectors. Total time points:"+ huge.size());

		
		Random random = new Random();
		boolean random_query = args[1].startsWith("random") ? true : false;
		System.out.println("Random query:" + random_query);
		List<DefaultHashMap<String,Short>> query;
		
		System.out.println("Computing stream stats");
		HashMap<String,Double> streamStatistics = computeStreamStats(huge);
		System.out.println("Computed stream stats. Alphabet size:" + streamStatistics.size());
		
		/*
		System.out.println("ActiveQueryPoints\t"
				+ "Naive_RT_Score\t"
				+ "AB_LB(4)\t"
				//+ "AB_LB+TiDE(4)\t"
				+ "AB_LB+AiDE(4)\t"
				+ "TB_LB(4)\t"
				//+ "TB_LB+TiDE(4)\t"
				//+ "TB_LB+AiDE(4)\t"
				//+ "AB_LB+TB_LB+TiDE(4)\t"
				//+ "AB_LB+TB_LB+AiDE(4)\t"
				+ "TiDE(4)\t"
				+ "AiDE(4)\t"
				+ "0(1)LB(4)\t"
				+ "ABIDE(4)\t"
				+ "ABIDEopt(4)");
		*/
		//for(int query_length : query_lengths )
		int query_length = Integer.parseInt(args[3]);
		{
			System.out.println("Query length:" + query_length);	
			for(int k = 0; k < 10; k++)
			{
				int bubblesortInterval = 10;//Math.max(1, query_length/10);
				
				if(random_query)
				{
					//Select random time points - Query
					query = new ArrayList<DefaultHashMap<String,Short>>();
					int query_active_time_points = 0;
					for(int i=0; i< query_length; i++)
					{
						int tpoint = random.nextInt(huge.size());
						query.add( huge.get(tpoint));
						query_active_time_points += huge.get(tpoint).size();
					}
					System.out.print( query_active_time_points + "\t");
				}	
				else
				{
					// Select random segment - Query
					int start = random.nextInt( huge.size()-query_length);
					query = huge.subList(start, start+query_length);
					int query_active_time_points = 0;
					for(int i=0; i< query_length; i++)
					{
						query_active_time_points += query.get(i).size();
					}
					System.out.print( query_active_time_points + "\t");
					//System.out.println(query);
				}

				//Compute Query Alphabet
				DefaultHashMap<String,Integer> queryAlphabetMap = new DefaultHashMap<String,Integer>(0);
				for(DefaultHashMap<String,Short> vector : query)
				{
					for (Entry<String, Short> entry : vector.entrySet()) {
						String key = entry.getKey();
						queryAlphabetMap.put(key, 1); //we don't care about keeping track, just the set of labels 
					}
				}
				String [] queryAlphabet = queryAlphabetMap.keySet().toArray(new String[queryAlphabetMap.keySet().size()]); 

				//Compute Query density order
				int [] density = computeTiDEOrder(query);
		
				//Compute Query alphabet density order
				HashMap<String,Double> queryStatistics = computeStreamStats(query);
				String [] queryAlphabetDensity = computeAlphabetDenstiyOrder(queryStatistics, streamStatistics);
				// Compute total Query active time points
				int queryActivePoints = computeNumberActiveTimePoints(query);

				//Perform procedures
				float [] results;long startTime, endTime, runTime;
				Object [] output;
				// Naive, no LBs, no Densities
				//startTime = System.currentTimeMillis();
				output = euclidean(huge,query,queryAlphabet, density,queryStatistics,false, false, false, false, false,queryActivePoints, bubblesortInterval);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				//endTime = System.currentTimeMillis();
				//runTime = endTime - startTime;
				System.out.printf("%d\t%d\t" , runTime, (int)results[0]);

				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				// First Lower bound, No density
				startTime = System.currentTimeMillis();
				output  = euclidean(huge,query,queryAlphabet, density, queryStatistics, true, false, false, false, false,queryActivePoints, bubblesortInterval);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3],results[4]);
				/*
				//First lower bound, density
				startTime = System.currentTimeMillis();
				output = euclidean(huge,query,queryAlphabet, density, queryAlphabetDensity, true, false, true, false, false,queryActivePoints);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3]);
				*/
				//First lower bound, alphabet density
				//startTime = System.currentTimeMillis();
				//output = euclidean(huge,query,queryAlphabet, density, queryAlphabetDensity, true, false, false, true, false,queryActivePoints);
				//runTime = ((Long) output[0]).longValue();
				//results = (float[])output[1];
				//System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3],results[4]);
				
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//Second lower bound, no density
				startTime = System.currentTimeMillis();
				output = euclidean(huge,query,queryAlphabet, density, queryStatistics, false, true, false, false, false,queryActivePoints, bubblesortInterval);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3],results[4]);
				/*
				//Second lower bound, density
				startTime = System.currentTimeMillis();
				output = euclidean(huge,query,queryAlphabet, density, queryAlphabetDensity, false, true, true, false, false,queryActivePoints);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3]);
				
				//Second lower bound, alphabet density
				startTime = System.currentTimeMillis();
				output = euclidean(huge,query,queryAlphabet, density, queryAlphabetDensity, false, true, false, true, false,queryActivePoints);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3],results[4]);
				
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//Both lower bounds, first density
				startTime = System.currentTimeMillis();
				output = euclidean(huge,query,queryAlphabet, density, queryAlphabetDensity, true, true, true, false, false,queryActivePoints);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3],results[4]);
				
				//Both lower bounds, second density
				startTime = System.currentTimeMillis();
				output = euclidean(huge,query,queryAlphabet, density, queryAlphabetDensity, true, true, false, true, false,queryActivePoints);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
								System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3],results[4]);
				*/
				//No lower bounds, first density
				startTime = System.currentTimeMillis();
				output = euclidean(huge,query,queryAlphabet, density, queryStatistics, false, false, true, false, false, queryActivePoints, bubblesortInterval);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3],results[4]);
				
				//No lower bound, alphabet density
				startTime = System.currentTimeMillis();
				output = euclidean(huge,query,queryAlphabet, density, queryStatistics, false, false, false, true, false, queryActivePoints, bubblesortInterval);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3],results[4]);
								
				//0(1) lower bound
				startTime = System.currentTimeMillis();
				output = euclidean(huge,query,queryAlphabet, density, queryStatistics, false, false, false, false, true, queryActivePoints, bubblesortInterval);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3],results[4]);
				
				//O(1) lower bound - ABLB - AiDE  --- ABIDE !!!
				startTime = System.currentTimeMillis();
				output = euclidean(huge,query,queryAlphabet, density, queryStatistics, true, false, false, true, true,queryActivePoints, bubblesortInterval);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3],results[4]);
				
				//ABIDE optimally combined
				//startTime = System.currentTimeMillis();
				//output = optimizedAbide(huge,query,queryAlphabet, queryAlphabetDensity);
				//runTime = ((Long) output[0]).longValue();
				//results = (float[])output[1];
				//System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\n", runTime, (int)results[0], results[1], results[2], results[3], results[4]);
				/*
				//Window AiDE - 100%
				startTime = System.currentTimeMillis();
				output = benchmarkAideVariations(huge, query, queryAlphabet, queryStatistics, true,false, 1, queryAlphabetDensity);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3], results[4]);

				//Window AiDE - 10%
				startTime = System.currentTimeMillis();
				output = benchmarkAideVariations(huge, query, queryAlphabet, queryStatistics, true,false, 10, queryAlphabetDensity);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3], results[4]);
				
				//'Knowledge in advance' Aide
				startTime = System.currentTimeMillis();
				output = benchmarkAideVariations(huge, query, queryAlphabet, queryStatistics, false,false, 0, queryAlphabetDensity);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3], results[4]);
				
				//cold start AiDE
				startTime = System.currentTimeMillis();
				output = benchmarkAideVariations(huge, query, queryAlphabet, queryStatistics, false, true, 1, queryAlphabetDensity);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\n", runTime, (int)results[0], results[1], results[2], results[3], results[4]);
				*/
				
			}
		}
		
	} //END OF MAIN

	public static int ABIDE(String[] args) {
		
		List<DefaultHashMap<String,Short>> huge = new ArrayList<DefaultHashMap<String,Short>>();
		int output = 0;
		// read e-sequences from file
		if ( args.length > 2 && args[2].startsWith("direct"))
		{ 
			System.out.println("Reading directing into vectors; file " + args[0]);
			huge = readFileIntoVectors(args[0]); 
		}
		else{
			ArrayList<Esequence> esequences = readFile(args[0]);
			System.out.println("Read file" + args[0]);
			// transform e-sequences to vector form
			for(int i=0; i < esequences.size(); i++)
			{
				ArrayList<DefaultHashMap<String,Short>> vec_seq = transform_to_vectors(esequences.get(i));
				for(int j=0; j < vec_seq.size(); j++)
				{
					DefaultHashMap<String,Short> curr_vec = vec_seq.get(j);
					if(curr_vec.size() > 0) //remove empty time points
						huge.add(curr_vec);
				}
				vec_seq = null;
			}
			esequences = null;
		}
		// Now 'huge' should contain the dataset in one big chunk of vectors
		System.out.println("Created vectors. Total time points:"+ huge.size());

		
		Random random = new Random();
		boolean random_query = args[1].startsWith("random") ? true : false;
		System.out.println("Random query:" + random_query);
		List<DefaultHashMap<String,Short>> query;
		
		System.out.println("Computing stream stats");
		HashMap<String,Double> streamStatistics = computeStreamStats(huge);
		System.out.println("Computed stream stats. Alphabet size:" + streamStatistics.size());
		
		/*
		System.out.println("ActiveQueryPoints\t"
				+ "Naive_RT_Score\t"
				+ "AB_LB(4)\t"
				//+ "AB_LB+TiDE(4)\t"
				+ "AB_LB+AiDE(4)\t"
				+ "TB_LB(4)\t"
				//+ "TB_LB+TiDE(4)\t"
				//+ "TB_LB+AiDE(4)\t"
				//+ "AB_LB+TB_LB+TiDE(4)\t"
				//+ "AB_LB+TB_LB+AiDE(4)\t"
				+ "TiDE(4)\t"
				+ "AiDE(4)\t"
				+ "0(1)LB(4)\t"
				+ "ABIDE(4)\t"
				+ "ABIDEopt(4)");
		*/
		//for(int query_length : query_lengths )
		int query_length = Integer.parseInt(args[3]);
		{
			System.out.println("Query length:" + query_length);	
			for(int k = 0; k < 10; k++)
			{
				int bubblesortInterval = 10;//Math.max(1, query_length/10);
				
				if(random_query)
				{
					//Select random time points - Query
					query = new ArrayList<DefaultHashMap<String,Short>>();
					int query_active_time_points = 0;
					for(int i=0; i< query_length; i++)
					{
						int tpoint = random.nextInt(huge.size());
						query.add( huge.get(tpoint));
						query_active_time_points += huge.get(tpoint).size();
					}
					System.out.print( query_active_time_points + "\t");
				}	
				else
				{
					// Select random segment - Query
					int start = random.nextInt( huge.size()-query_length);
					query = huge.subList(start, start+query_length);
					int query_active_time_points = 0;
					for(int i=0; i< query_length; i++)
					{
						query_active_time_points += query.get(i).size();
					}
					System.out.print( query_active_time_points + "\t");
					//System.out.println(query);
				}

				//Compute Query Alphabet
				DefaultHashMap<String,Integer> queryAlphabetMap = new DefaultHashMap<String,Integer>(0);
				for(DefaultHashMap<String,Short> vector : query)
				{
					for (Entry<String, Short> entry : vector.entrySet()) {
						String key = entry.getKey();
						queryAlphabetMap.put(key, 1); //we don't care about keeping track, just the set of labels 
					}
				}
				String [] queryAlphabet = queryAlphabetMap.keySet().toArray(new String[queryAlphabetMap.keySet().size()]); 

				//Compute Query density order
				int [] density = computeTiDEOrder(query);
		
				//Compute Query alphabet density order
				HashMap<String,Double> queryStatistics = computeStreamStats(query);
				String [] queryAlphabetDensity = computeAlphabetDenstiyOrder(queryStatistics, streamStatistics);
				// Compute total Query active time points
				int queryActivePoints = computeNumberActiveTimePoints(query);

				//Perform procedures
				float [] results;long startTime, endTime, runTime;
				// Naive, no LBs, no Densities
				//startTime = System.currentTimeMillis();
				/*
				//Second lower bound, density
				startTime = System.currentTimeMillis();
				output = euclidean(huge,query,queryAlphabet, density, queryAlphabetDensity, false, true, true, false, false,queryActivePoints);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3]);
				
				//Second lower bound, alphabet density
				startTime = System.currentTimeMillis();
				output = euclidean(huge,query,queryAlphabet, density, queryAlphabetDensity, false, true, false, true, false,queryActivePoints);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3],results[4]);
				
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//Both lower bounds, first density
				startTime = System.currentTimeMillis();
				output = euclidean(huge,query,queryAlphabet, density, queryAlphabetDensity, true, true, true, false, false,queryActivePoints);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3],results[4]);
				
				//Both lower bounds, second density
				startTime = System.currentTimeMillis();
				output = euclidean(huge,query,queryAlphabet, density, queryAlphabetDensity, true, true, false, true, false,queryActivePoints);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
								System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3],results[4]);
				*/
				//No lower bounds, first density
				
				//O(1) lower bound - ABLB - AiDE  --- ABIDE !!!
				startTime = System.currentTimeMillis();
				output = euclideannew(huge,query,queryAlphabet, density, queryStatistics, true, false, false, true, true,queryActivePoints, bubblesortInterval);
				
				//ABIDE optimally combined
				//startTime = System.currentTimeMillis();
				//output = optimizedAbide(huge,query,queryAlphabet, queryAlphabetDensity);
				//runTime = ((Long) output[0]).longValue();
				//results = (float[])output[1];
				//System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\n", runTime, (int)results[0], results[1], results[2], results[3], results[4]);
				/*
				//Window AiDE - 100%
				startTime = System.currentTimeMillis();
				output = benchmarkAideVariations(huge, query, queryAlphabet, queryStatistics, true,false, 1, queryAlphabetDensity);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3], results[4]);

				//Window AiDE - 10%
				startTime = System.currentTimeMillis();
				output = benchmarkAideVariations(huge, query, queryAlphabet, queryStatistics, true,false, 10, queryAlphabetDensity);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3], results[4]);
				
				//'Knowledge in advance' Aide
				startTime = System.currentTimeMillis();
				output = benchmarkAideVariations(huge, query, queryAlphabet, queryStatistics, false,false, 0, queryAlphabetDensity);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\t", runTime, (int)results[0], results[1], results[2], results[3], results[4]);
				
				//cold start AiDE
				startTime = System.currentTimeMillis();
				output = benchmarkAideVariations(huge, query, queryAlphabet, queryStatistics, false, true, 1, queryAlphabetDensity);
				runTime = ((Long) output[0]).longValue();
				results = (float[])output[1];
				System.out.printf("%d\t%d\t%.6f\t%.6f\t%.6f\t%.6f\n", runTime, (int)results[0], results[1], results[2], results[3], results[4]);
				*/
				
			}
		}
		return output;
	} 
	
	
	public List<DefaultHashMap<String,Short>> getVectors(String[] args)
	{
			List<DefaultHashMap<String,Short>> huge = new ArrayList<DefaultHashMap<String,Short>>();
			ArrayList<Esequence> esequences = readFile(args[0]);
			System.out.println("Read file" + args[0]);
			// transform e-sequences to vector form
			for(int i=0; i < esequences.size(); i++)
			{
				ArrayList<DefaultHashMap<String,Short>> vec_seq = transform_to_vectors(esequences.get(i));
				for(int j=0; j < vec_seq.size(); j++)
				{
					DefaultHashMap<String,Short> curr_vec = vec_seq.get(j);
					if(curr_vec.size() > 0) //remove empty time points
						huge.add(curr_vec);
				}
			}
			System.out.println(huge);
			return huge;

	}
	

	public int[][] getABIDEfeatures(String args, int k)
	{
			List<DefaultHashMap<String,Short>> huge = new ArrayList<DefaultHashMap<String,Short>>();
			ArrayList<Esequence> esequences = readFile(args);
			Random random = new Random();
			List<DefaultHashMap<String,Short>> query;
			List<List<DefaultHashMap<String,Short>>> all_examples = new ArrayList<List<DefaultHashMap<String,Short>>>();
			int max_size = 10000; 

			


			System.out.println("Read file" + args);
			// transform e-sequences to vector form
			for(int i=0; i < esequences.size(); i++)
			{
				ArrayList<DefaultHashMap<String,Short>> vec_seq = transform_to_vectors(esequences.get(i)); //decided not to remove empty time points
				all_examples.add(vec_seq);
				if(vec_seq.size() < max_size) //determine max query length 
						max_size = vec_seq.size();

				for(int j=0; j < vec_seq.size(); j++)
				{
					DefaultHashMap<String,Short> curr_vec = vec_seq.get(j);
					if(curr_vec.size() > 0) //remove empty time points TODO:remove this to improve speed
						huge.add(curr_vec);
				}
			}
			System.out.print(all_examples);

		//create k random queries from given data set and create similarity matrix to every example using the ABIDE method
			int[][]newArr = new int[k][all_examples.size()];
		
			for(int j = 0; j < k; j++)
			{
				int choose_example = random.nextInt(all_examples.size());
				int query_length =  random.nextInt(max_size) + 1; // generate random length from chosen example of max size of shortest example
				int bubblesortInterval = 10;//Math.max(1, query_length/10);
				System.out.println("Max query length:" + query_length);	
				
				
	// Select random segment - Query

				int start = random.nextInt(all_examples.get(choose_example).size()-query_length);
				query = all_examples.get(choose_example).subList(start, start+query_length);
				int query_active_time_points = 0;
				for(int i=0; i< query_length; i++)
				{
					query_active_time_points += query.get(i).size();
				}
					//System.out.print( query_active_time_points + "\t");

					//System.out.println(query);
		

				//Compute Query Alphabet
				DefaultHashMap<String,Integer> queryAlphabetMap = new DefaultHashMap<String,Integer>(0);
				for(DefaultHashMap<String,Short> vector : query)
				{
					for (Entry<String, Short> entry : vector.entrySet()) {
						String key = entry.getKey();
						queryAlphabetMap.put(key, 1); //we don't care about keeping track, just the set of labels 
					}
				}
				String [] queryAlphabet = queryAlphabetMap.keySet().toArray(new String[queryAlphabetMap.keySet().size()]); 

				HashMap<String,Double> queryStatistics = computeStreamStats(query);

				//Compute Query density order
				int [] density = computeTiDEOrder(query);
		

				// Compute total Query active time points
				int queryActivePoints = computeNumberActiveTimePoints(query);

				//Perform procedures
				float [] results;long startTime, endTime, runTime;
				

					
				for(int i=0; i< all_examples.size(); i++)
				{
					System.out.println("Computing stream stats");
					HashMap<String,Double> streamStatistics = computeStreamStats(all_examples.get(i));
					System.out.println("Computed stream stats. Alphabet size:" + streamStatistics.size());

					//Compute Query alphabet density order
					//String [] queryAlphabetDensity = computeAlphabetDenstiyOrder(queryStatistics, streamStatistics);

					System.out.println(query);
					System.out.println(all_examples.get(i));
					int output = 0;
					output = euclideannew(all_examples.get(i),query,queryAlphabet, density, queryStatistics, true, false, false, true, true, queryActivePoints, bubblesortInterval);
					newArr[j][i] = output;
				}
			}


		return newArr;

	}
	
	

	public int[][] getABIDEfeaturestrain(String args, int k, int trainstart, int trainend)
	{
			//List<DefaultHashMap<String,Short>> huge = new ArrayList<DefaultHashMap<String,Short>>();
			ArrayList<Esequence> esequences = readFile(args);
			Random random = new Random();
			List<DefaultHashMap<String,Short>> query;
			List<List<DefaultHashMap<String,Short>>> all_examples = new ArrayList<List<DefaultHashMap<String,Short>>>();
			int max_size = 10000; 

			


			System.out.println("Read file" + args);
			// transform e-sequences to vector form
			for(int i=0; i < esequences.size(); i++)
			{
				ArrayList<DefaultHashMap<String,Short>> vec_seq = transform_to_vectors(esequences.get(i)); //decided not to remove empty time points
				all_examples.add(vec_seq);
				if(vec_seq.size() < max_size) //determine max query length 
						max_size = vec_seq.size();

				/*for(int j=0; j < vec_seq.size(); j++)
				{
					DefaultHashMap<String,Short> curr_vec = vec_seq.get(j);
					if(curr_vec.size() > 0) //remove empty time points TODO:remove this to improve speed
						huge.add(curr_vec);
				}*/
			}
			System.out.print(all_examples);

		//create k random queries from given data set and create similarity matrix to every example using the ABIDE method
			int[][]newArr = new int[k][all_examples.size()];
		
			for(int j = 0; j < k; j++)
			{
				int choose_example = random.nextInt((trainend - trainstart) + 1) + trainstart;
				int query_length =  random.nextInt(max_size) + 1; // generate random length from chosen example of max size of shortest example
				int bubblesortInterval = 10;//Math.max(1, query_length/10);
				System.out.println("Max query length:" + query_length);	
				
				
	// Select random segment - Query

				int start = random.nextInt(all_examples.get(choose_example).size()-query_length);
				query = all_examples.get(choose_example).subList(start, start+query_length);
				int query_active_time_points = 0;
				for(int i=0; i< query_length; i++)
				{
					query_active_time_points += query.get(i).size();
				}
					//System.out.print( query_active_time_points + "\t");

					//System.out.println(query);
		

				//Compute Query Alphabet
				DefaultHashMap<String,Integer> queryAlphabetMap = new DefaultHashMap<String,Integer>(0);
				for(DefaultHashMap<String,Short> vector : query)
				{
					for (Entry<String, Short> entry : vector.entrySet()) {
						String key = entry.getKey();
						queryAlphabetMap.put(key, 1); //we don't care about keeping track, just the set of labels 
					}
				}
				String [] queryAlphabet = queryAlphabetMap.keySet().toArray(new String[queryAlphabetMap.keySet().size()]); 

				HashMap<String,Double> queryStatistics = computeStreamStats(query);

				//Compute Query density order
				int [] density = computeTiDEOrder(query);
		

				// Compute total Query active time points
				int queryActivePoints = computeNumberActiveTimePoints(query);

				//Perform procedures
				float [] results;long startTime, endTime, runTime;
				

					
				for(int i=0; i< all_examples.size(); i++)
				{
					System.out.println("Computing stream stats");
					HashMap<String,Double> streamStatistics = computeStreamStats(all_examples.get(i));
					System.out.println("Computed stream stats. Alphabet size:" + streamStatistics.size());

					//Compute Query alphabet density order
					//String [] queryAlphabetDensity = computeAlphabetDenstiyOrder(queryStatistics, streamStatistics);

					System.out.println(query);
					System.out.println(all_examples.get(i));
					int output = 0;
					output = euclideannew(all_examples.get(i),query,queryAlphabet, density, queryStatistics, true, false, false, true, true, queryActivePoints, bubblesortInterval);
					newArr[j][i] = output;
				}
			}


		return newArr;

	}


		public int[][] getABIDEfeaturestrain2(String args, int k, int[] train)
	{
			//List<DefaultHashMap<String,Short>> huge = new ArrayList<DefaultHashMap<String,Short>>();
			ArrayList<Esequence> esequences = readFile(args);
			Random random = new Random();
			List<DefaultHashMap<String,Short>> query;
			List<List<DefaultHashMap<String,Short>>> all_examples = new ArrayList<List<DefaultHashMap<String,Short>>>();
			int max_size = 100; 
			int check = 0; 

			


			System.out.println("Read file" + args);
			// transform e-sequences to vector form
			for(int i=0; i < esequences.size(); i++)
			{
				check = check + 1;
				System.out.println(check);
				ArrayList<DefaultHashMap<String,Short>> vec_seq = transform_to_vectors(esequences.get(i)); //decided not to remove empty time points
				all_examples.add(vec_seq);
				if(vec_seq.size() < max_size) //determine max query length 
						max_size = vec_seq.size();

			}
			//System.out.print(all_examples);

		//create k random queries from given data set and create similarity matrix to every example using the ABIDE method
			int[][]newArr = new int[k][all_examples.size()];
		
			for(int j = 0; j < k; j++)
			{
				int choose_example = train[random.nextInt(train.length)] - 1; //adjust for R indexing 
				int query_length =  random.nextInt(max_size - 1) + 1; // generate random length from chosen example of max size of shortest example
				int bubblesortInterval = 10;//Math.max(1, query_length/10);
				//System.out.println("Max query length:" + query_length);	
				//System.out.println(choose_example);
				
	// Select random segment - Query

				int start = random.nextInt(all_examples.get(choose_example).size()-query_length);
				query = all_examples.get(choose_example).subList(start, start+query_length);
				int query_active_time_points = 0;
				for(int i=0; i< query_length; i++)
				{
					query_active_time_points += query.get(i).size();
				}
					//System.out.print( query_active_time_points + "\t");

					//System.out.println(query);

				//Compute Query Alphabet
				DefaultHashMap<String,Integer> queryAlphabetMap = new DefaultHashMap<String,Integer>(0);
				for(DefaultHashMap<String,Short> vector : query)
				{
					for (Entry<String, Short> entry : vector.entrySet()) {
						String key = entry.getKey();
						queryAlphabetMap.put(key, 1); //we don't care about keeping track, just the set of labels 
					}
				}
				String [] queryAlphabet = queryAlphabetMap.keySet().toArray(new String[queryAlphabetMap.keySet().size()]); 

				HashMap<String,Double> queryStatistics = computeStreamStats(query);

				//Compute Query density order
				int [] density = computeTiDEOrder(query);
		

				// Compute total Query active time points
				int queryActivePoints = computeNumberActiveTimePoints(query);

				//Perform procedures
				float [] results;long startTime, endTime, runTime;
				

					
				for(int i=0; i< all_examples.size(); i++)
				{
					//System.out.println("Computing stream stats");
					//HashMap<String,Double> streamStatistics = computeStreamStats(all_examples.get(i));
					//System.out.println("Computed stream stats. Alphabet size:" + streamStatistics.size());

					//Compute Query alphabet density order
					//String [] queryAlphabetDensity = computeAlphabetDenstiyOrder(queryStatistics, streamStatistics);

					System.out.println(query);
					//System.out.println(all_examples.get(i));
					int output = 0;
					output = euclideannew(all_examples.get(i),query,queryAlphabet, density, queryStatistics, true, false, false, true, true, queryActivePoints, bubblesortInterval);
					newArr[j][i] = output;
				}
			}


		return newArr;

	}





	public int[][] getABIDEfeaturestrainNonEmpty(String args, int k, int[] train) //k number of features, with integer array of training indices 
	{
			//List<DefaultHashMap<String,Short>> huge = new ArrayList<DefaultHashMap<String,Short>>();
			ArrayList<Esequence> esequences = readFile(args);
			Random random = new Random();
			List<DefaultHashMap<String,Short>> query;
			List<List<DefaultHashMap<String,Short>>> all_examples = new ArrayList<List<DefaultHashMap<String,Short>>>();
			int max_size = 100; 

			


			System.out.println("Read file" + args);
			// transform e-sequences to vector form
			for(int i=0; i < esequences.size(); i++)
			{
				ArrayList<DefaultHashMap<String,Short>> vec_seq = transform_to_vectors(esequences.get(i)); //decided not to remove empty time points
				List<DefaultHashMap<String,Short>> temp = new ArrayList<DefaultHashMap<String,Short>>();

				for(int j=0; j < vec_seq.size(); j++)
				{
					DefaultHashMap<String,Short> curr_vec = vec_seq.get(j);
					if(curr_vec.size() > 0) //remove empty time points
						temp.add(curr_vec);
				}

				all_examples.add(temp);

				if(temp.size() < max_size) //determine max query length 
						max_size = temp.size();

			}

		//create k random queries from given data set and create similarity matrix to every example using the ABIDE method
			int[][]newArr = new int[k][all_examples.size()];
		
			for(int j = 0; j < k; j++)
			{
				int choose_example = train[random.nextInt(train.length)] - 1; //adjust for R indexing 
				int query_length =  random.nextInt(max_size - 1) + 1; // generate random length from chosen example of max size of shortest example
				int bubblesortInterval = 10;//Math.max(1, query_length/10);
				//System.out.println("Max query length:" + query_length);	
				
	// Select random segment - Query

				int start = random.nextInt(all_examples.get(choose_example).size()-query_length);
				query = all_examples.get(choose_example).subList(start, start+query_length);
				int query_active_time_points = 0;
				for(int i=0; i< query_length; i++)
				{
					query_active_time_points += query.get(i).size();
				}
					//System.out.print( query_active_time_points + "\t");

					System.out.println(query);

				//Compute Query Alphabet
				DefaultHashMap<String,Integer> queryAlphabetMap = new DefaultHashMap<String,Integer>(0);
				for(DefaultHashMap<String,Short> vector : query)
				{
					for (Entry<String, Short> entry : vector.entrySet()) {
						String key = entry.getKey();
						queryAlphabetMap.put(key, 1); //we don't care about keeping track, just the set of labels 
					}
				}
				String [] queryAlphabet = queryAlphabetMap.keySet().toArray(new String[queryAlphabetMap.keySet().size()]); 

				HashMap<String,Double> queryStatistics = computeStreamStats(query);

				//Compute Query density order
				int [] density = computeTiDEOrder(query);
		

				// Compute total Query active time points
				int queryActivePoints = computeNumberActiveTimePoints(query);

				//Perform procedures
				//float [] results;long startTime, endTime, runTime;
				

					
				for(int i=0; i< all_examples.size(); i++)
				{
					//System.out.println("Computing stream stats");
					//HashMap<String,Double> streamStatistics = computeStreamStats(all_examples.get(i));
					//System.out.println("Computed stream stats. Alphabet size:" + streamStatistics.size());

					//Compute Query alphabet density order
					//String [] queryAlphabetDensity = computeAlphabetDenstiyOrder(queryStatistics, streamStatistics);

					//System.out.println(query);
					//System.out.println(all_examples.get(i));
					int output = 0;
					output = euclideannew(all_examples.get(i),query,queryAlphabet, density, queryStatistics, true, false, false, true, true, queryActivePoints, bubblesortInterval);
					newArr[j][i] = output;
				}
			}


		return newArr;

	}
	
	
	
		
	/* 
	 * Given a target and a query e-sequence in vector-form, the method performs 
	 * naive Euclidean distance starting from 'position' of the target eseqence.
	 * Important: we do not penalize intervals in Target that do not appear in Query,
	 * but for the rest, we penalize when in a specific time point an interval
	 * appears in Target and not in Query. 
	 */
	public static int naiveEuclideanPenalizeOffTarget(List<DefaultHashMap<String,Short>> target, List<DefaultHashMap<String,Short>> query, String [] queryAlphabet, int position)
	{
		int score = 0;
		
		// For each time-point
		//int target_size = target.size();
		int query_size = query.size();
		for( int i = 0; i < query_size; i++)
		{
			for(String key: queryAlphabet)
			{
				score += Math.abs(query.get(i).get(key)-target.get(position+i).get(key));
			}
		}
		return score;
	}
	
	/*
	 * Given the sorted density (interval count for a given time point) vector for Query, perform Euclidean Distance comparison between Query and Target at Position, 
	 * in decreasing order of query density points. 
	 */
	public static float [] densityEuclidean(List<DefaultHashMap<String,Short>> target, List<DefaultHashMap<String,Short>> query, String [] queryAlphabet, int position, int [] queryDensity, int cuttoff)
	{
		//List<DefaultHashMap<String,Short>> targetArrayList = new ArrayList<DefaultHashMap<String,Short>>(target);
		//List<DefaultHashMap<String,Short>> queryArrayList = new ArrayList<DefaultHashMap<String,Short>>(query);
		float [] result = {0,0};
		int score = 0;
		for(int i = 0; i < queryDensity.length; i++) 
		{
			int t = queryDensity[i];
			//DefaultHashMap<String,Short> query_vec = ;
			//DefaultHashMap<String,Short> target_vec = ;
			for(String key: queryAlphabet)
			{
				score += Math.abs(query.get(t).get(key)-target.get(position+t).get(key));
			}
			if (score > cuttoff) // See if it makes sense to put this inside
			{	
				if( i < queryDensity.length - 1)
				{	
					result[1] = (float)i/queryDensity.length;//early abandon
				}	
				break;
			}
		}
		result[0] = score;
		return result;
	}
	
	/*
	 * Takes a whole target vector-form sequence, and a vector-form query, and computes the rolling ED.
	 * option for FirstLowerBound
	 */
	/* Commenting out --- see new version below
	public static Object [] euclidean(List<DefaultHashMap<String,Short>> target, List<DefaultHashMap<String,Short>> query, String [] queryAlphabet, int [] queryDensity, String [] queryAlphabetDensityOrder, boolean applyFirstLowerBound,boolean applySecondLowerBound, boolean useDensity, boolean useAlphabetDensity, boolean apply01lb, int queryActivePoints)
	{
		if (useDensity && useAlphabetDensity)
			System.out.println("You are using both density approaches!! which one do you want?");
		long totalWindows = 0;
		List<DefaultHashMap<String,Short>> currentWindow = new ArrayList<DefaultHashMap<String,Short>>();
		for(int i=0; i < query.size(); i++)
			currentWindow.add( target.get(i));
		
		int totalActivePoints = 0;
		///////
		long startTime = System.nanoTime();
		///////
		
		// setup for AB_LB and O(1)-lb
		if(apply01lb)
			totalActivePoints = computeNumberActiveTimePoints(currentWindow);
		
		DefaultHashMap<String,Integer> windowStats = null;
		DefaultHashMap<String,Integer> queryStats = null;
		if (applyFirstLowerBound)
		{
			windowStats = computeWindowStats(currentWindow);
			queryStats = computeWindowStats(query);
		}
		//setup for second lower bound
		List<Integer> windowActiveIntervalCounts = null;
		List<Integer> queryActiveIntervalCounts = null;
		if(applySecondLowerBound)
		{
			windowActiveIntervalCounts = new LinkedList<Integer>();
			queryActiveIntervalCounts = new ArrayList<Integer>();
			for(DefaultHashMap<String,Short> t : currentWindow)
			{
				windowActiveIntervalCounts.add(t.size());
			}
			for(DefaultHashMap<String,Short> t : query)
			{
				queryActiveIntervalCounts.add(t.size());
			}	
		}

		
		
		// initial full comparison
		int bestScore = naiveEuclideanPenalizeOffTarget(currentWindow,query,queryAlphabet,0);
		
		
		long estimatedTime = System.nanoTime() - startTime;
		
		// Sliding window
		ListIterator<DefaultHashMap<String,Short>> it = target.listIterator(query.size()); //start from query.size() element
		int score, lowerBoundScore; float [] score_and_pruning; int secondLBdifference;
		int prunedComparisons=0; float early_abandons = 0; int secondPrunedComparisons = 0; int o1prunedComparisons = 0; //some stats
		
		while(it.hasNext())
		{
			totalWindows++;
			// remove expiring slice
			DefaultHashMap<String,Short> expiringSlice = currentWindow.remove(0);
			// add fresh slice
			DefaultHashMap<String,Short> freshSlice = it.next();
			currentWindow.add(freshSlice);
			
			
			///////
			startTime = System.nanoTime();
			///////
			
			//Update LB datastructures
			if( apply01lb)
			{
				totalActivePoints += update01Stats(freshSlice, expiringSlice);
			}
			if (applyFirstLowerBound)
			{
				windowStats = updateWindowStats(windowStats, freshSlice, expiringSlice);
			}
			if(applySecondLowerBound)
			{
				windowActiveIntervalCounts.remove(0);
				windowActiveIntervalCounts.add(freshSlice.size());
			}
			
			//Perform LB computations - they are split into two phases so that the datastructures are always up-to-date before 
			//any LB prunning
			if( apply01lb)
			{
				if( Math.abs(totalActivePoints - queryActivePoints) > bestScore)
				{o1prunedComparisons++; continue;}
			
			}
			if (applyFirstLowerBound)
			{
				lowerBoundScore = abLowerBound(windowStats,queryStats);
				if(lowerBoundScore >= bestScore)
				{ prunedComparisons++;	continue;}
			}
			if(applySecondLowerBound)
			{
				secondLBdifference = 0;
				Iterator<Integer> iterWindow = windowActiveIntervalCounts.iterator(); 
				Iterator<Integer> iterQuery = queryActiveIntervalCounts.iterator();
				while(iterWindow.hasNext())
				{
					secondLBdifference += Math.abs( iterWindow.next() - iterQuery.next()  );
					//if(secondLBdifference >= bestScore)
					//	break;
				}
				//System.out.print( "("+bestScore+","+secondLBdifference +")");
				if(secondLBdifference >= bestScore)
				{ secondPrunedComparisons++; continue;}
				
			}
			
			//Select appropriate euclidean computation for the non-pruned cases.
			if(useDensity) 
			{
				score_and_pruning = densityEuclidean(currentWindow,query,queryAlphabet,0,queryDensity, bestScore);
				score = (int)score_and_pruning[0]; early_abandons += score_and_pruning[1];
			}
			else if(useAlphabetDensity)
			{
				score_and_pruning = alphabetDensityEuclidean(currentWindow, query, queryAlphabetDensityOrder, bestScore);
				score = (int)score_and_pruning[0]; early_abandons += score_and_pruning[1];
			}
			else
			{	
				score = naiveEuclideanPenalizeOffTarget(currentWindow,query,queryAlphabet,0);
			}
			if(score < bestScore)
			{	
				bestScore = score;
				if( score == 0)// reset process
				bestScore = Integer.MAX_VALUE;
			}
			
			estimatedTime += (System.nanoTime() - startTime);
			
		}
		
		//System.out.println("Best score:" + bestScore + "    position:" + bestPosition + "   prunned comparisons speedup:" + (1.0)/(1-((double)prunedComparisons)/target.size()));
		float lbPruningPower = ((float)prunedComparisons)/totalWindows;  //(target.size()-query.size());
		float secondLBpruningPower = ((float)secondPrunedComparisons)/totalWindows; //(target.size()-query.size());
		float o1PruningPower = ((float)o1prunedComparisons)/totalWindows;
		float earlyAbandonPruningPower = (early_abandons > 0) ? 1f-(((float)early_abandons)/(totalWindows-prunedComparisons-secondPrunedComparisons)) : 0; ///(target.size()-query.size());
		//float sumPruningPower = (float)sumPrunedComparisons/totalWindows;
		float [] results = { (float)bestScore, o1PruningPower, lbPruningPower, secondLBpruningPower, earlyAbandonPruningPower};
		
		estimatedTime = estimatedTime/(long)1000; //convert to microseconds
		return new Object[]{estimatedTime, results};
	}
	*/
	//////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * Combined inital computation of O(1)- and AB_LB stats  
	 */
	public static Object [] abideComputeWindowStats(List<DefaultHashMap<String,Short>> window)
	{
		DefaultHashMap<String,Integer> stats = new DefaultHashMap<String,Integer>(0);
		int totalActiveTimePoints = 0;
		for(DefaultHashMap<String,Short> query_vec : window)
		{
			for (Entry<String, Short> entry : query_vec.entrySet()) 
			{
			    String key = entry.getKey();
			    Integer value = (int)entry.getValue();
			    stats.put(key, stats.get(key)+value);
			    totalActiveTimePoints += value;
			}
		}
		// first value is the AB_LB structure holding the active points per label
		// second value is the total active time points (for O(1)-lb)
		return new Object[]{stats,totalActiveTimePoints};
	}
	
	/*
	 * O(1) LB set-up
	 */
	public static int computeNumberActiveTimePoints(List<DefaultHashMap<String,Short>> window)
	{
		int totalActiveTimePoints = 0;
		for(DefaultHashMap<String,Short> query_vec : window)
			for (int value : query_vec.values()) 
			    totalActiveTimePoints += value;
	
		return totalActiveTimePoints;
	}
	
	public static int update01Stats(DefaultHashMap<String,Short> freshSlice, DefaultHashMap<String,Short> expiringSlice)
	{
		int timepointDiff = 0;
		// Add new stats
		for (int value : freshSlice.values()) {
		    timepointDiff += value;
		}
		// remove expring stats
		for(int value: expiringSlice.values())
		{
			timepointDiff -= value;
		}
		return timepointDiff;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * Find the total active time-points per interval-label in a window. AB_LB data-structure 
	 */
	public static DefaultHashMap<String,Integer> computeWindowStats(List<DefaultHashMap<String,Short>> window)
	{
		DefaultHashMap<String,Integer> stats = new DefaultHashMap<String,Integer>(0);
		for(DefaultHashMap<String,Short> query_vec : window)
		{
			for (Entry<String, Short> entry : query_vec.entrySet()) 
			{
			    String key = entry.getKey();
			    Integer value = (int)entry.getValue();
			    stats.put(key, stats.get(key)+value);
			}
		}
		return stats;
	}

	
	/*
	 * AB_LB: compute the number of total label-time-points in which Query and Window differ
	 */
	public static int abLowerBound( DefaultHashMap<String,Integer> targetStats, DefaultHashMap<String,Integer> queryStats)
	{
		int score = 0;
		for (Entry<String, Integer> entry : queryStats.entrySet()) {
		    String key = entry.getKey();
		    Integer value = entry.getValue();
		    score += Math.abs(value - targetStats.get(key));
		}
		return score;
	}
	
	/*
	 * Given existing window stats, an expiring slice and a new slice, compute the new window stats
	 */
	public static DefaultHashMap<String,Integer> updateWindowStats(DefaultHashMap<String,Integer> windowStats, DefaultHashMap<String,Short> freshSlice, DefaultHashMap<String,Short> expiringSlice)
	{

		// Add new stats
		for (Entry<String, Short> entry : freshSlice.entrySet()) {
		    String key = entry.getKey();
		    Integer value = (int)entry.getValue();
		    windowStats.put(key, windowStats.get(key) + value );
		}
		// remove expring stats - why iterate over window stats and not over expiringSlice?
		for(Entry<String, Short> entry : expiringSlice.entrySet())
		{
			String key = entry.getKey();
			Integer value = (int)entry.getValue();
			int freshValue = windowStats.get(key) - value;
			if(freshValue > 0)
			{windowStats.put(key, freshValue);}
			else if(freshValue == 0)
			{windowStats.remove(key);}
			else //bug-catchers
				System.out.println("Incorrect value:" + freshValue);
				
		}
		return windowStats;
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * Horizontal density (+early abandon) functions
	 * Return float [], first result is score, second is pruning power
	 */
	
	public static float [] alphabetDensityEuclidean(List<DefaultHashMap<String,Short>> window, List<DefaultHashMap<String,Short>> query, String [] alphabetDensityOrder, int threshold)
	{
		//List<DefaultHashMap<String,Short>> windowArrayList = new ArrayList<DefaultHashMap<String,Short>>(window);
		//List<DefaultHashMap<String,Short>> queryArrayList = new ArrayList<DefaultHashMap<String,Short>>(query);
		
		float [] results = new float[2];
		int score = 0; float not_pruned = 0;
		for(int i = 0; i < alphabetDensityOrder.length; i++)
		{ 
			String label = alphabetDensityOrder[i];
			for(int j = 0; j < query.size(); j++)
			{
				score += Math.abs( query.get(j).get(label) - window.get(j).get(label));
			}
			if(score > threshold)
			{not_pruned = ((float)i/alphabetDensityOrder.length);break;}
		}
		
		results[0] = (float)score;
		results[1] = not_pruned;
		return results;
	}
	
	public static HashMap<String,Double> computeStreamStats(List<DefaultHashMap<String,Short>> stream)
	{
		//TODO the first nested for-loop is duplicate code
		DefaultHashMap<String,Integer> labelCounts = new DefaultHashMap<String,Integer>(0); 
		// Count total number of active time-points per interval-label over the stream
		for(DefaultHashMap<String,Short> stream_vec : stream)
		{
			for (Entry<String, Short> entry : stream_vec.entrySet()) 
			{
			    String key = entry.getKey();
			    Integer value = (int)entry.getValue();
			    labelCounts.put(key, labelCounts.get(key)+value);
			}
		}	
		// Compute the ratio "active time points/stream length" for each interval-label
		HashMap<String,Double> streamStatistics = new HashMap<String,Double>();
		for (Entry<String, Integer> entry : labelCounts.entrySet()) 
		{
		    String key = entry.getKey();
		    Integer value = entry.getValue();
		    streamStatistics.put(key, ((double)value)/stream.size());
		}
		return streamStatistics;
	}
	
	
	public static String [] computeAlphabetDenstiyOrder(HashMap<String,Double> queryStatistics, HashMap<String,Double> streamStatistics)
	{
		// Compute absolute value of differences of appearance ratios
		List<Pair<String,Double>> pairs = new LinkedList<Pair<String,Double>>();
		for (Entry<String, Double> queryEntry : queryStatistics.entrySet())
		{
			String key = queryEntry.getKey();
			Double queryValue = queryEntry.getValue();
			Double diff = Math.abs( queryValue -streamStatistics.get(key));
			pairs.add(new Pair<String,Double>(key,diff));
		}
		
		// Sort based on absolute difference of appearance ratio
		class CustomComparator implements Comparator<Pair<String,Double>> {
			public int compare(Pair<String,Double> t1, Pair<String,Double> t2) {
		        return t2.getRight().compareTo(t1.getRight());
		    }
		}
		Collections.sort(pairs, new CustomComparator());
		String [] results = new String[queryStatistics.size()];
		for(int i = 0; i < pairs.size(); i++)
		{
			Pair<String,Double> pair = pairs.get(i); 
			results[i] = pair.getLeft();
		}
		
		return results;
	}
	

	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * Compute the density (number of active intervals) of each time point in the query. Sort time points based on density. 
	 */
	public static int [] computeTiDEOrder(List<DefaultHashMap<String,Short>> query)
	{
		Tuple [] density = new Tuple[query.size()];
		for(int i=0; i < query.size(); i++)
		{
			DefaultHashMap<String,Short> vector = query.get(i);
			int count = 0;
			for (Entry<String, Short> entry : vector.entrySet()) {
			    count += entry.getValue();
			}
			density[i] = new Tuple(i,count);
		}
		
		//Now we should sort density based on counts
		class CustomComparator implements Comparator<Tuple> {
			public int compare(Tuple t1, Tuple t2) {
		        return t2.getCount().compareTo(t1.getCount());
		    }
		}
		Arrays.sort(density, new CustomComparator());
		
		int [] density_counts = new int [query.size()];
		for(int i = 0; i < density.length; i++)
			density_counts[i] = density[i].getKey();
		return density_counts;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * Read file (intervals in form of (label,t.start,t.end), one e-sequence per line
	 */
	public static ArrayList<Esequence> readFile(String filepath)
	{
		ArrayList<Esequence> esequences = new ArrayList<Esequence>();
		try{
			FileInputStream fstream = new FileInputStream(filepath);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			//Read File Line By Line
			int counter = 0; //line counter
			while ((strLine = br.readLine()) != null)   {
		    		//for each line, split intervals
		    		String delims = "[ ,?!]+";
		    		String[] tokens = strLine.split(delims); 

		    		Esequence current = new Esequence(counter, 0,0);
		    	
			    	String label = null;
			    	int startTime = 0,endTime =0;
			    	for(int i = 0; i < tokens.length; i++)
			    	{
			    		//System.out.print(tokens[i]+" ");
			    		if( i%3 == 0)
			    			label = new String(tokens[i]);
			    		else if( i%3 == 1)
		    			startTime = Integer.parseInt(tokens[i]);
			    		else if( i%3 == 2)
			    		{	endTime = Integer.parseInt(tokens[i]);
			    			Interval temp = new Interval(startTime,endTime,label);
			    			current.addInterval(temp);
			    		}		    		
			    	}
		    	esequences.add(current);
		    	counter++;
		    }
			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
				System.err.println("Error: " + e.getMessage());
				e.printStackTrace();
		}
		return esequences;
	}
	
	/*
	 * Makes a two-pass read of the file, transforms into Vectors
	 */
	public static List<DefaultHashMap<String,Short>> readFileIntoVectors(String filepath)
	{
		// First, read file and get max interval-ending value.
		// Also get number of different labels
		HashMap<String,Short> alphabet = new HashMap<String,Short>();
		long maxEndTime = 0;		
		try{
			FileInputStream fstream = new FileInputStream(filepath);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
		    		//for each line, split intervals
		    		String delims = "[ ,?!]+";
		    		String[] tokens = strLine.split(delims); 
		    	
			    	String label = null;
			    	int startTime = 0,endTime =0;
			    	for(int i = 0; i < tokens.length; i++)
			    	{
			    		if(i%3 == 0)
			    		{
			    			label = new String(tokens[i]);
			    		}	
			    		if( i%3 == 2)
			    		{	
			    			endTime = Integer.parseInt(tokens[i]);
			    			alphabet.put(label, (short)1);
			    			if(endTime > maxEndTime)
			    				maxEndTime = endTime;
			    		}		    		
			    	}	
		    }
			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
				System.err.println("Error: " + e.getMessage());
				e.printStackTrace();
		}
		System.out.println("First pass complete. Alphabet Size:" + alphabet.size() + " maxEnd:" + maxEndTime);
		System.out.println(alphabet.keySet());
		// Create vectors
		List<DefaultHashMap<String,Short>> vectors = new ArrayList<DefaultHashMap<String,Short>>();
		for(int i= 0; i <= maxEndTime; i++)
		{
			DefaultHashMap<String,Short> map = new DefaultHashMap<String,Short>((short)0);
			vectors.add(map);
		}
		
		// Second pass to populate values
		try{
			FileInputStream fstream = new FileInputStream(filepath);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
		    		//for each line, split intervals
		    		String delims = "[ ,?!]+";
		    		String[] tokens = strLine.split(delims);
		    		String label = null;
			    	int startTime = 0,endTime =0;
			    	for(int i = 0; i < tokens.length; i++)
			    	{
			    		//System.out.print(tokens[i]+" ");
			    		if( i%3 == 0)
			    			label = new String(tokens[i]);
			    		else if( i%3 == 1)
		    			startTime = Integer.parseInt(tokens[i]);
			    		else if( i%3 == 2)
			    		{	endTime = Integer.parseInt(tokens[i]);
			    			//All 3 values have been read, populate vectors
			    			for(int j = startTime; j <= endTime; j++)
			    			{
			    				DefaultHashMap<String,Short> map = vectors.get(j);
			    				map.put( label, (short)1);	
			    			}
			    		}		    		
			    	}
		    }
			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
				System.err.println("Error: " + e.getMessage());
				e.printStackTrace();
		}
		return vectors;
	}
	
	/*
	 * Transform e-sequence from list of triples to list of vectors. One vector per time point 
	 */
	public static ArrayList<DefaultHashMap<String,Short>> transform_to_vectors(Esequence esequence)
	{
		ArrayList<DefaultHashMap<String,Short>> vectors = new ArrayList<DefaultHashMap<String,Short>>();
		int duration = esequence.getEnd() - esequence.getStart() + 1;
		// Initialize sequence of vectors
		for(int i= 0; i < duration; i++)
		{
			DefaultHashMap<String,Short> map = new DefaultHashMap<String,Short>((short)0);
			vectors.add(map);
		}
		// Populate values
		for(int i = 0; i < esequence.tracks.size(); i++)
		{
			Interval interval = esequence.tracks.get(i);
			for(int j = interval.getStart(); j <= interval.getEnd(); j++)
			{
				DefaultHashMap<String,Short> map = vectors.get(j);
				map.put( interval.getLabel(), (short)1);
			}
		}

		return vectors;
	}

	public static ArrayList<DefaultHashMap<String,Short>> transform_to_vectors(Sequence sequence)
	{
		ArrayList<DefaultHashMap<String,Short>> vectors = new ArrayList<>();
		int duration = sequence.duration() - sequence.earliestStart() + 1;
		// Initialize sequence of vectors
		for(int i= 0; i <= duration; i++)
		{
			DefaultHashMap<String,Short> map = new DefaultHashMap<>((short) 0);
			vectors.add(map);
		}
		// Populate values
		for(int i = 0; i < sequence.intervalCount(); i++)
		{
			data_structures.Interval interval = sequence.getInterval(i);
			for(int j = interval.getStart(); j <= interval.getEnd(); j++)
			{
				DefaultHashMap<String,Short> map = vectors.get(j);
				map.put( String.valueOf(interval.getDimension()), (short)1);
			}
		}

		return vectors;
	}




	/*
	 * Optimized combination of ABIDE ( O(1)lb + AB_LB + AiDE). Computing the stats for the LBs in a single scan
	 */
	public static Object [] optimizedAbide(List<DefaultHashMap<String,Short>> target, List<DefaultHashMap<String,Short>> query, String [] queryAlphabet, String [] queryAlphabetDensityOrder)
	{
		long totalWindows = 0;
		List<DefaultHashMap<String,Short>> currentWindow = new ArrayList<DefaultHashMap<String,Short>>();
		for(int i=0; i < query.size(); i++)
			currentWindow.add( target.get(i));
		
		int totalActivePoints = 0;
		///////
		long startTime = System.nanoTime();
		///////
		
		// setup for O(1)-lb and AB_LB
		Object [] lbWindowStats = abideComputeWindowStats(currentWindow);
		totalActivePoints = (Integer)lbWindowStats[1];
		DefaultHashMap<String,Integer> windowStats = (DefaultHashMap<String,Integer>)lbWindowStats[0];
		

		Object [] lbQueryStats = abideComputeWindowStats(query); 
		int queryActivePoints = (Integer)lbQueryStats[1];
		DefaultHashMap<String,Integer> queryStats = (DefaultHashMap<String,Integer>)lbQueryStats[0];
		
				
		// initial full comparison
		int bestScore = naiveEuclideanPenalizeOffTarget(currentWindow,query,queryAlphabet,0);
		
		long estimatedTime = System.nanoTime() - startTime;
		
		// Sliding window
		ListIterator<DefaultHashMap<String,Short>> it = target.listIterator(query.size()); //start from query.size() element
		int score, lowerBoundScore; float [] score_and_pruning; //int secondLBdifference;
		int prunedComparisons=0; float early_abandons = 0; int secondPrunedComparisons = 0; int o1prunedComparisons = 0; //some stats
		
		while(it.hasNext())
		{
			totalWindows++;
			// remove expiring slice
			DefaultHashMap<String,Short> expiringSlice = currentWindow.remove(0);
			// add fresh slice
			DefaultHashMap<String,Short> freshSlice = it.next();
			currentWindow.add(freshSlice);
			
			
			///////
			startTime = System.nanoTime();
			///////
			
			//Update LB datastructures 
			Object [] lbStatUpdates = abideUpdateWindowStats(windowStats, freshSlice, expiringSlice);  
			totalActivePoints += (Integer)lbStatUpdates[1];
			windowStats = (DefaultHashMap<String,Integer>)lbStatUpdates[0];

			//Perform LB computations - they are split into two phases so that the datastructures are always up-to-date before 
			//any LB prunning
			// Apply O(1)-lb
			if( Math.abs(totalActivePoints - queryActivePoints) > bestScore)
				{o1prunedComparisons++; continue;}
			// Apply AB_LB
			lowerBoundScore = abLowerBound(windowStats,queryStats);
			if(lowerBoundScore >= bestScore)
			{ prunedComparisons++;	continue;}
			
			// use AiDE
			score_and_pruning = alphabetDensityEuclidean(currentWindow, query, queryAlphabetDensityOrder, bestScore);
			score = (int)score_and_pruning[0]; early_abandons += score_and_pruning[1];
			if(score < bestScore)
			{	
				bestScore = score;
				//if( score == 0)// reset process
				//bestScore = Integer.MAX_VALUE;
			}
			
			estimatedTime += (System.nanoTime() - startTime);
			
		}
		
		//System.out.println("Best score:" + bestScore + "    position:" + bestPosition + "   prunned comparisons speedup:" + (1.0)/(1-((double)prunedComparisons)/target.size()));
		float lbPruningPower = ((float)prunedComparisons)/totalWindows;  //(target.size()-query.size());
		float secondLBpruningPower = ((float)secondPrunedComparisons)/totalWindows; //(target.size()-query.size());
		float o1PruningPower = ((float)o1prunedComparisons)/totalWindows; // 
		float earlyAbandonPruningPower = (early_abandons > 0) ? 1f-(((float)early_abandons)/(totalWindows-prunedComparisons-secondPrunedComparisons)) : 0; ///(target.size()-query.size());
		//float sumPruningPower = (float)sumPrunedComparisons/totalWindows;
		float [] results = { (float)bestScore,o1PruningPower, lbPruningPower, secondLBpruningPower, earlyAbandonPruningPower};
		
		estimatedTime = estimatedTime/(long)1000; //convert to microseconds
		return new Object[]{estimatedTime, results};
	}
	
	/*
	 * Given existing window stats, an expiring slice and a new slice, compute the new window stats
	 */
	public static Object [] abideUpdateWindowStats(DefaultHashMap<String,Integer> windowStats, DefaultHashMap<String,Short> freshSlice, DefaultHashMap<String,Short> expiringSlice)
	{
		int timepointDiff = 0;
		// Add new stats
		for (Entry<String, Short> entry : freshSlice.entrySet()) {
		    String key = entry.getKey();
		    Integer value = (int)entry.getValue();
		    windowStats.put(key, windowStats.get(key) + value );
		    timepointDiff += value;
		}
		// remove expring stats - why iterate over window stats and not over expiringSlice?
		for(Entry<String, Short> entry : expiringSlice.entrySet())
		{
			String key = entry.getKey();
			Integer value = (int)entry.getValue();
			timepointDiff -= value;
			int freshValue = windowStats.get(key) - value;
			if(freshValue > 0)
			{windowStats.put(key, freshValue);}
			else if(freshValue == 0)
			{windowStats.remove(key);}
			else //bug-catchers
				System.out.println("Incorrect value:" + freshValue);
		}
		return new Object[]{windowStats,timepointDiff};
	}
	
	/////////////////////
	public static Pair<String,Integer> [] windowAideInitialize (List<DefaultHashMap<String,Short>> currentWindow, String [] queryAlphabet){
		Pair<String,Integer> [] counts = new Pair[queryAlphabet.length];
		int i = 0;
		for(String label: queryAlphabet)
		{
			int count = 0;
			for(DefaultHashMap<String,Short> currentSlice : currentWindow)
			{
				count += currentSlice.get(label);
			}
			counts[i] = new Pair<String,Integer>(label,count);
			i++;
		}
		return counts;
	}
	public static Pair<String,Integer>[] windowAideUpdate(String [] queryAlphabet, DefaultHashMap<String,Short> expiringSlice, DefaultHashMap<String,Short> freshSlice, Pair<String,Integer>[] counts)
	{
		for(Pair<String,Integer> pair : counts)
		{
			String label = pair.getLeft();
			int newCount = pair.getRight() - expiringSlice.get(label) + freshSlice.get(label);
			pair.putRight(newCount);
		}
		return counts;
	}
	
	/////////////////////
	/*
	 * For initialization, use windowAideInitialize
	 * */
	public static Pair<String,Integer>[] coldStartAideUpdate(String [] queryAlphabet, DefaultHashMap<String,Short> freshSlice, Pair<String,Integer>[] counts)
	{
		for(Pair<String,Integer> pair : counts)
		{
			String label = pair.getLeft();
			int newCount = pair.getRight() + freshSlice.get(label);
			pair.putRight(newCount);
		}
		return counts;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * In coldStart Aide, 'windowLength' should be given the value  ('totalWindows'+queryLenght)
	 * */
	public static void bubbleSort(Pair<String,Integer>[] counts, int windowLength, HashMap<String,Double> queryStatistics )
	{
		int n = counts.length;
		do
		{
			int newN = 0;
			for(int i = 1; i < n; i++)
			{
				if( Math.abs( ((double)counts[i-1].getRight())/windowLength - queryStatistics.get(counts[i-1].getLeft()) ) < Math.abs( ((double)counts[i].getRight())/windowLength - queryStatistics.get(counts[i].getLeft())))
				{
					//swap
					Pair<String,Integer> temp = counts[i-1];
					counts[i-1] = counts[i];
					counts[i] = temp;
					// set upper limit
					newN = i;
				}
			}
			n = newN;
		}
		while(n > 0);
	}
	
	
	public static Object [] benchmarkAideVariations(List<DefaultHashMap<String,Short>> target, List<DefaultHashMap<String,Short>> query, String [] queryAlphabet, HashMap<String,Double> queryStatistics, boolean applyWindowAiDE, boolean applyColdStartAide, int bubblesortInterval, String [] queryAlphabetDensityOrder)
	{
		int totalWindows = 0;
		List<DefaultHashMap<String,Short>> currentWindow = new ArrayList<DefaultHashMap<String,Short>>();
		for(int i=0; i < query.size(); i++)
			currentWindow.add( target.get(i));
		
		int totalActivePoints = 0;
		///////
		long startTime = System.nanoTime();
		///////
				
		// initial full comparison
		int bestScore = naiveEuclideanPenalizeOffTarget(currentWindow,query,queryAlphabet,0);
		
		Pair<String,Integer> [] counts = null;
		if(applyWindowAiDE || applyColdStartAide)
		{
			counts = windowAideInitialize (currentWindow, queryAlphabet);
			bubbleSort(counts,currentWindow.size(), queryStatistics);
		}
				
		long estimatedTime = System.nanoTime() - startTime;

		
		// Sliding window
		ListIterator<DefaultHashMap<String,Short>> it = target.listIterator(query.size()); //start from query.size() element
		int score, lowerBoundScore; float [] score_and_pruning; int secondLBdifference;
		int prunedComparisons=0; float early_abandons = 0; int secondPrunedComparisons = 0; int o1prunedComparisons = 0; //some stats
	

		
		while(it.hasNext())
		{
			totalWindows++;
			// remove expiring slice
			DefaultHashMap<String,Short> expiringSlice = currentWindow.remove(0);
			// add fresh slice
			DefaultHashMap<String,Short> freshSlice = it.next();
			currentWindow.add(freshSlice);
			
			
			///////
			startTime = System.nanoTime();
			///////
			
			//Update LB/AiDE datastructures
			if(applyWindowAiDE)
			{
				counts = windowAideUpdate(queryAlphabet, expiringSlice, freshSlice, counts);
				if(totalWindows%bubblesortInterval == 0)
					bubbleSort(counts,currentWindow.size(), queryStatistics);
			}
			else if(applyColdStartAide)
			{
				counts = coldStartAideUpdate(queryAlphabet, freshSlice, counts);
				if(totalWindows%bubblesortInterval == 0)
					bubbleSort(counts,totalWindows+currentWindow.size(), queryStatistics); //TODO fix values
			}
			
			
			//Apply AiDE
			if( applyWindowAiDE || applyColdStartAide)
				score_and_pruning = aideWithCounts(currentWindow, query, counts, bestScore);
			else
				score_and_pruning = alphabetDensityEuclidean(currentWindow, query, queryAlphabetDensityOrder, bestScore);
			
			score = (int)score_and_pruning[0]; early_abandons += score_and_pruning[1];
			
			if(score < bestScore)
			{	
				bestScore = score;
				//if( score == 0)// reset process
				//bestScore = Integer.MAX_VALUE;
			}
			
			estimatedTime += (System.nanoTime() - startTime);
			
		}
		
		//System.out.println("Best score:" + bestScore + "    position:" + bestPosition + "   prunned comparisons speedup:" + (1.0)/(1-((double)prunedComparisons)/target.size()));
		float lbPruningPower = ((float)prunedComparisons)/totalWindows;  //(target.size()-query.size());
		float secondLBpruningPower = ((float)secondPrunedComparisons)/totalWindows; //(target.size()-query.size());
		float o1PruningPower = ((float)o1prunedComparisons)/totalWindows;
		float earlyAbandonPruningPower = (early_abandons > 0) ? 1f-(((float)early_abandons)/(totalWindows-prunedComparisons-secondPrunedComparisons)) : 0; ///(target.size()-query.size());
		//float sumPruningPower = (float)sumPrunedComparisons/totalWindows;
		float [] results = { (float)bestScore, o1PruningPower, lbPruningPower, secondLBpruningPower, earlyAbandonPruningPower};
		
		estimatedTime = estimatedTime/(long)1000; //convert to microseconds
		return new Object[]{estimatedTime, results};
	}

	/*
	 *  Implementation of AiDE
	 *  takes as input an ordered list with the counts, for that window, of each label
	 * */
	public static float [] aideWithCounts(List<DefaultHashMap<String,Short>> window, List<DefaultHashMap<String,Short>> query, Pair<String,Integer> [] counts, int threshold)
	{
		
		float [] results = new float[2];
		int score = 0; float not_pruned = 0;
		for(int i = 0; i < counts.length; i++)
		{ 
			String label = counts[i].getLeft();
			for(int j = 0; j < query.size(); j++)
			{
				score += Math.abs( query.get(j).get(label) - window.get(j).get(label));
			}
			if(score > threshold)
			{not_pruned = ((float)i/counts.length);break;}
		}
		
		results[0] = (float)score;
		results[1] = not_pruned;
		return results;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	public static Object [] euclidean(List<DefaultHashMap<String,Short>> target, List<DefaultHashMap<String,Short>> query, String [] queryAlphabet,
			int [] queryDensity, HashMap<String,Double> queryAlphabetStatistics, boolean applyFirstLowerBound,boolean applySecondLowerBound, 
			boolean useDensity, boolean useAlphabetDensity, boolean apply01lb, int queryActivePoints, int bubblesortInterval)
	{
		if (useDensity && useAlphabetDensity)
			System.out.println("You are using both density approaches!! which one do you want?");
		long totalWindows = 0;
		List<DefaultHashMap<String,Short>> currentWindow = new ArrayList<DefaultHashMap<String,Short>>();
		for(int i=0; i < query.size(); i++)
			currentWindow.add( target.get(i));
		
		int totalActivePoints = 0;
		///////
		long startTime = System.nanoTime();
		///////
		
		// setup for AB_LB and O(1)-lb
		if(apply01lb)
			totalActivePoints = computeNumberActiveTimePoints(currentWindow);
		
		DefaultHashMap<String,Integer> windowStats = null;
		DefaultHashMap<String,Integer> queryStats = null;
		if (applyFirstLowerBound)
		{
			windowStats = computeWindowStats(currentWindow);
			queryStats = computeWindowStats(query);
		}
		//setup for second lower bound
		List<Integer> windowActiveIntervalCounts = null;
		List<Integer> queryActiveIntervalCounts = null;
		if(applySecondLowerBound)
		{
			windowActiveIntervalCounts = new LinkedList<Integer>();
			queryActiveIntervalCounts = new ArrayList<Integer>();
			for(DefaultHashMap<String,Short> t : currentWindow)
			{
				windowActiveIntervalCounts.add(t.size());
			}
			for(DefaultHashMap<String,Short> t : query)
			{
				queryActiveIntervalCounts.add(t.size());
			}	
		}
		// initial full comparison
		int bestScore = naiveEuclideanPenalizeOffTarget(currentWindow,query,queryAlphabet,0);
		
		
		// setup AiDE
		Pair<String,Integer> [] counts = null;
		if( useAlphabetDensity)
		{
			counts = windowAideInitialize (currentWindow, queryAlphabet);
			bubbleSort(counts,currentWindow.size(), queryAlphabetStatistics);
		}//TODO remaining
		
		long estimatedTime = System.nanoTime() - startTime;
		
		// Sliding window
		ListIterator<DefaultHashMap<String,Short>> it = target.listIterator(query.size()); //start from query.size() element
		int score, lowerBoundScore; float [] score_and_pruning; int secondLBdifference;
		int prunedComparisons=0; float early_abandons = 0; int secondPrunedComparisons = 0; int o1prunedComparisons = 0; //some stats
		
		
		while(it.hasNext())
		{
			totalWindows++;
			// remove expiring slice
			DefaultHashMap<String,Short> expiringSlice = currentWindow.remove(0);
			// add fresh slice
			DefaultHashMap<String,Short> freshSlice = it.next();
			currentWindow.add(freshSlice);
			
			
			///////
			startTime = System.nanoTime();
			///////
			
			//Update LB datastructures
			if( apply01lb)
			{
				totalActivePoints += update01Stats(freshSlice, expiringSlice);
			}
			if (applyFirstLowerBound)
			{
				windowStats = updateWindowStats(windowStats, freshSlice, expiringSlice);
			}
			if(applySecondLowerBound)
			{
				windowActiveIntervalCounts.remove(0);
				windowActiveIntervalCounts.add(freshSlice.size());
			}
			if( useAlphabetDensity)
			{
				counts = windowAideUpdate(queryAlphabet, expiringSlice, freshSlice, counts);
			}
			
			//Perform LB computations - they are split into two phases so that the datastructures are always up-to-date before 
			//any LB prunning
			if( apply01lb)
			{
				if( Math.abs(totalActivePoints - queryActivePoints) > bestScore)
				{o1prunedComparisons++; continue;}
			
			}
			if (applyFirstLowerBound)
			{
				lowerBoundScore = abLowerBound(windowStats,queryStats);
				if(lowerBoundScore >= bestScore)
				{ prunedComparisons++;	continue;}
			}
			if(applySecondLowerBound)
			{
				secondLBdifference = 0;
				Iterator<Integer> iterWindow = windowActiveIntervalCounts.iterator(); 
				Iterator<Integer> iterQuery = queryActiveIntervalCounts.iterator();
				while(iterWindow.hasNext())
				{
					secondLBdifference += Math.abs( iterWindow.next() - iterQuery.next()  );
					//if(secondLBdifference >= bestScore)
					//	break;
				}
				//System.out.print( "("+bestScore+","+secondLBdifference +")");
				if(secondLBdifference >= bestScore)
				{ secondPrunedComparisons++; continue;}
				
			}
			
			//Select appropriate euclidean computation for the non-pruned cases.
			if(useDensity) 
			{
				score_and_pruning = densityEuclidean(currentWindow,query,queryAlphabet,0,queryDensity, bestScore);
				score = (int)score_and_pruning[0]; early_abandons += score_and_pruning[1];
			}
			else if(useAlphabetDensity)
			{
				score_and_pruning = aideWithCounts(currentWindow, query, counts, bestScore);
				//score_and_pruning = alphabetDensityEuclidean(currentWindow, query, queryAlphabetDensityOrder, bestScore);
				score = (int)score_and_pruning[0]; early_abandons += score_and_pruning[1];
				if(totalWindows%bubblesortInterval == 0)
					bubbleSort(counts,currentWindow.size(), queryAlphabetStatistics);
			}
			else
			{	
				score = naiveEuclideanPenalizeOffTarget(currentWindow,query,queryAlphabet,0);
			}
			
			if(score < bestScore)
			{	
				bestScore = score;
				//if( score == 0)// reset process
				//bestScore = Integer.MAX_VALUE;
			}
			
			estimatedTime += (System.nanoTime() - startTime);
		}
		
		//System.out.println("Best score:" + bestScore + "    position:" + bestPosition + "   prunned comparisons speedup:" + (1.0)/(1-((double)prunedComparisons)/target.size()));
		float lbPruningPower = ((float)prunedComparisons)/totalWindows;  //(target.size()-query.size());
		float secondLBpruningPower = ((float)secondPrunedComparisons)/totalWindows; //(target.size()-query.size());
		float o1PruningPower = ((float)o1prunedComparisons)/totalWindows;
		float earlyAbandonPruningPower = (early_abandons > 0) ? 1f-(((float)early_abandons)/(totalWindows-prunedComparisons-secondPrunedComparisons)) : 0; ///(target.size()-query.size());
		//float sumPruningPower = (float)sumPrunedComparisons/totalWindows;
		float [] results = { (float)bestScore, o1PruningPower, lbPruningPower, secondLBpruningPower, earlyAbandonPruningPower};
		
		estimatedTime = estimatedTime/(long)1000; //convert to microseconds
		return new Object[]{estimatedTime, results};
	}

	//////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////

	public static int euclideannew(List<DefaultHashMap<String,Short>> target, List<DefaultHashMap<String,Short>> query, String [] queryAlphabet,
			int [] queryDensity, HashMap<String,Double> queryAlphabetStatistics, boolean applyFirstLowerBound,boolean applySecondLowerBound, 
			boolean useDensity, boolean useAlphabetDensity, boolean apply01lb, int queryActivePoints, int bubblesortInterval)
	{
		if (useDensity && useAlphabetDensity)
			System.out.println("You are using both density approaches!! which one do you want?");
		long totalWindows = 0;
		List<DefaultHashMap<String,Short>> currentWindow = new ArrayList<DefaultHashMap<String,Short>>();
		for(int i=0; i < Math.min(query.size(), target.size()); i++)
			currentWindow.add( target.get(i));
		
		int totalActivePoints = 0;
		///////
		long startTime = System.nanoTime();
		///////
		
		// setup for AB_LB and O(1)-lb
		if(apply01lb)
			totalActivePoints = computeNumberActiveTimePoints(currentWindow);
		
		DefaultHashMap<String,Integer> windowStats = null;
		DefaultHashMap<String,Integer> queryStats = null;
		if (applyFirstLowerBound)
		{
			windowStats = computeWindowStats(currentWindow);
			queryStats = computeWindowStats(query);
		}
		//setup for second lower bound
		List<Integer> windowActiveIntervalCounts = null;
		List<Integer> queryActiveIntervalCounts = null;
		if(applySecondLowerBound)
		{
			windowActiveIntervalCounts = new LinkedList<Integer>();
			queryActiveIntervalCounts = new ArrayList<Integer>();
			for(DefaultHashMap<String,Short> t : currentWindow)
			{
				windowActiveIntervalCounts.add(t.size());
			}
			for(DefaultHashMap<String,Short> t : query)
			{
				queryActiveIntervalCounts.add(t.size());
			}	
		}
		// initial full comparison
		int bestScore = naiveEuclideanPenalizeOffTarget(currentWindow,query,queryAlphabet,0);
		
		
		// setup AiDE
		Pair<String,Integer> [] counts = null;
		if( useAlphabetDensity)
		{
			counts = windowAideInitialize (currentWindow, queryAlphabet);
			bubbleSort(counts,currentWindow.size(), queryAlphabetStatistics);
		}//TODO remaining
		
		long estimatedTime = System.nanoTime() - startTime;
		
		// Sliding window
		ListIterator<DefaultHashMap<String,Short>> it = target.listIterator(Math.min(query.size(), target.size() - 1)); //start from query.size() element
		int score, lowerBoundScore; float [] score_and_pruning; int secondLBdifference;
		int prunedComparisons=0; float early_abandons = 0; int secondPrunedComparisons = 0; int o1prunedComparisons = 0; //some stats
		
		
		while(it.hasNext())
		{
			totalWindows++;
			// remove expiring slice
			DefaultHashMap<String,Short> expiringSlice = currentWindow.remove(0);
			// add fresh slice
			DefaultHashMap<String,Short> freshSlice = it.next();
			currentWindow.add(freshSlice);
			
			
			///////
			startTime = System.nanoTime();
			///////
			
			//Update LB datastructures
			if( apply01lb)
			{
				totalActivePoints += update01Stats(freshSlice, expiringSlice);
			}
			if (applyFirstLowerBound)
			{
				windowStats = updateWindowStats(windowStats, freshSlice, expiringSlice);
			}
			if(applySecondLowerBound)
			{
				windowActiveIntervalCounts.remove(0);
				windowActiveIntervalCounts.add(freshSlice.size());
			}
			if( useAlphabetDensity)
			{
				counts = windowAideUpdate(queryAlphabet, expiringSlice, freshSlice, counts);
			}
			
			//Perform LB computations - they are split into two phases so that the datastructures are always up-to-date before 
			//any LB prunning
			if( apply01lb)
			{
				if( Math.abs(totalActivePoints - queryActivePoints) > bestScore)
				{o1prunedComparisons++; continue;}
			
			}
			if (applyFirstLowerBound)
			{
				lowerBoundScore = abLowerBound(windowStats,queryStats);
				if(lowerBoundScore >= bestScore)
				{ prunedComparisons++;	continue;}
			}
			if(applySecondLowerBound)
			{
				secondLBdifference = 0;
				Iterator<Integer> iterWindow = windowActiveIntervalCounts.iterator(); 
				Iterator<Integer> iterQuery = queryActiveIntervalCounts.iterator();
				while(iterWindow.hasNext())
				{
					secondLBdifference += Math.abs( iterWindow.next() - iterQuery.next()  );
					//if(secondLBdifference >= bestScore)
					//	break;
				}
				//System.out.print( "("+bestScore+","+secondLBdifference +")");
				if(secondLBdifference >= bestScore)
				{ secondPrunedComparisons++; continue;}
				
			}
			
			//Select appropriate euclidean computation for the non-pruned cases.
			if(useDensity) 
			{
				score_and_pruning = densityEuclidean(currentWindow,query,queryAlphabet,0,queryDensity, bestScore);
				score = (int)score_and_pruning[0]; early_abandons += score_and_pruning[1];
			}
			else if(useAlphabetDensity)
			{
				score_and_pruning = aideWithCounts(currentWindow, query, counts, bestScore);
				//score_and_pruning = alphabetDensityEuclidean(currentWindow, query, queryAlphabetDensityOrder, bestScore);
				score = (int)score_and_pruning[0]; early_abandons += score_and_pruning[1];
				if(totalWindows%bubblesortInterval == 0)
					bubbleSort(counts,currentWindow.size(), queryAlphabetStatistics);
			}
			else
			{	
				score = naiveEuclideanPenalizeOffTarget(currentWindow,query,queryAlphabet,0);
			}
			
			if(score < bestScore)
			{	
				bestScore = score;
				//if( score == 0)// reset process
				//bestScore = Integer.MAX_VALUE;
			}
			
			estimatedTime += (System.nanoTime() - startTime);
		}
		
		//System.out.println("Best score:" + bestScore + "    position:" + bestPosition + "   prunned comparisons speedup:" + (1.0)/(1-((double)prunedComparisons)/target.size()));
		float lbPruningPower = ((float)prunedComparisons)/totalWindows;  //(target.size()-query.size());
		float secondLBpruningPower = ((float)secondPrunedComparisons)/totalWindows; //(target.size()-query.size());
		float o1PruningPower = ((float)o1prunedComparisons)/totalWindows;
		float earlyAbandonPruningPower = (early_abandons > 0) ? 1f-(((float)early_abandons)/(totalWindows-prunedComparisons-secondPrunedComparisons)) : 0; ///(target.size()-query.size());
		//float sumPruningPower = (float)sumPrunedComparisons/totalWindows;
		float [] results = { (float)bestScore, o1PruningPower, lbPruningPower, secondLBpruningPower, earlyAbandonPruningPower};
		
		estimatedTime = estimatedTime/(long)1000; //convert to microseconds
		return bestScore;
	}
	
	
	
	
	
	/* 
	 * DO NOT USE !!
	 * Given a target and a query e-sequence in vector-form, the method performs 
	 * naive Euclidean distance starting from 'position' of the target eseqence.
	 * Important: we do not penalize intervals in Target that do not appear in Query,
	 * nor do we penalize interval labels - DO NOT USE!!
	 */
	public static int naiveEuclidean(List<DefaultHashMap<String,Integer>> target, List<DefaultHashMap<String,Integer>> query, int position)
	{
		System.out.println("YOU ARE USING AN INAPPROPRIATE METHOD!!!");
		int score = 0;
		
		// For each time-point
		int target_size = target.size();
		int query_size = query.size();
		for( int i = 0; i < query_size && i+position < target_size; i++)
		{
			
			DefaultHashMap<String,Integer> target_vec = target.get(position+i);
			DefaultHashMap<String,Integer> query_vec = query.get(i);
			for (Entry<String, Integer> entry : query_vec.entrySet()) {
			    String key = entry.getKey();
			    Integer value = entry.getValue();
			    score += Math.abs(value-target_vec.get(key));
			}
		}
		return score;
	}

}
