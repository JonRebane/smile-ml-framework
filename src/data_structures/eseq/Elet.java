package data_structures.eseq;

import java.util.HashMap;
import java.util.List;

public class Elet {
    public final List<DefaultHashMap<String, Short>> query;
    public final int query_active_time_points;
    public final DefaultHashMap<String, Integer> queryAlphabetMap;
    public final String[] queryAlphabet;
    public final HashMap<String, Double> queryStatistics;
    public final int[] density;
    public final int queryActivePoints;

    public Elet(List<DefaultHashMap<String, Short>> query, int query_active_time_points, DefaultHashMap<String, Integer> queryAlphabetMap, String[] queryAlphabet, HashMap<String, Double> queryStatistics, int[] density, int queryActivePoints) {
        this.query = query;
        this.query_active_time_points = query_active_time_points;
        this.queryAlphabetMap = queryAlphabetMap;
        this.queryAlphabet = queryAlphabet;
        this.queryStatistics = queryStatistics;
        this.density = density;
        this.queryActivePoints = queryActivePoints;
    }
}
