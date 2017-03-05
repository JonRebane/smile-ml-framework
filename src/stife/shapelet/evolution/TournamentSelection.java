package stife.shapelet.evolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import data_structures.Pair;

public class TournamentSelection implements SelectionAlgorithm<NShapelet> {

	
	private double p;
	private int n;
	private Random random;

	public TournamentSelection(int n,double p,Random random){
		this.n = n;
		this.p = p;
		this.random = random;
	}
	
	@Override
	public List<NShapelet> select(Map<NShapelet,Double> evaluatedPopulation) {
		ArrayList<NShapelet> population = new ArrayList<>(evaluatedPopulation.keySet());
		assert(n<=population.size());
		List<NShapelet> selected = new ArrayList<>();
		for(int i=0;i<population.size();i++){
			Collections.shuffle(population,random);
			selected.add(runTournament(population.subList(0, n),evaluatedPopulation));
		}
		return selected;
	}

	private NShapelet runTournament(List<NShapelet> chosen, Map<NShapelet, Double> evaluatedPopulation) {
		List<Pair<NShapelet, Double>> tournament = chosen.stream().map(sh -> new Pair<>(sh,evaluatedPopulation.get(sh))).collect(Collectors.toList());
		Collections.sort(tournament, (p1,p2)->p1.getSecond().compareTo(p2.getSecond()));
		boolean winnerChosen = false;
		int rank = 0;
		while(!winnerChosen && rank != tournament.size()-1){
			if(random.nextDouble()<p){
				winnerChosen = true;
			} else{
				rank++;
			}
		}
		return tournament.get(rank).getFirst();
	}
	
	

}
