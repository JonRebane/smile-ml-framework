package stife.shapelet.evolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import data_structures.Pair;

public class TournamentSelection implements SelectionAlgorithm<NShapelet> {

	
	protected double p;
	protected int n;
	protected Random random;

	public TournamentSelection(int n,double p,Random random){
		this.n = n;
		this.p = p;
		this.random = random;
	}
	
	@Override
	public List<NShapelet> select(Map<NShapelet,Double> evaluatedPopulation) {
		//System.out.println("starting selection");
		List<NShapelet> population = evaluatedPopulation.keySet().stream().sorted(NShapelet.nShapeletComparator).collect(Collectors.toList());
		assert(n<=population.size());
		List<NShapelet> selected = new ArrayList<>();
		//System.out.println("population before loop: " + population);
		for(int i=0;i<population.size();i++){
			Collections.shuffle(population,random);
			//System.out.println("population shuffle result in tournament " + i + ": " + population);
			selected.add(runTournament(population.subList(0, n),evaluatedPopulation).deepCopy());
			//System.out.println("selected after tournament " + i + ":  " + selected);
		}
		//System.out.println("population after loop: " + population);
		//System.out.println("selected right before return: " + selected);
		return selected;
	}

	protected NShapelet runTournament(List<NShapelet> chosen, Map<NShapelet, Double> evaluatedPopulation) {
		//System.out.println("beginning tournament");
		List<Pair<NShapelet, Double>> tournament = chosen.stream().map(sh -> new Pair<>(sh,evaluatedPopulation.get(sh))).collect(Collectors.toList());
		Collections.sort(tournament, (p1,p2)->{
			if(!p1.getSecond().equals(p2.getSecond())){
				return p1.getSecond().compareTo(p2.getSecond());
			} else{
				return NShapelet.nShapeletComparator.compare(p1.getFirst(), p2.getFirst());
			}
		});
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
