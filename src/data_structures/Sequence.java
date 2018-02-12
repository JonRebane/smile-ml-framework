package data_structures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import algorithms.Algorithms;
import stife.distance.Event;
import stife.distance.EventType;
import stife.shapelet.evolution.NShapelet;
import stife.shapelet_size2.Shapelet_Size2;
import stife.shapelet_size2_new.ShapeletSize2;
import stife.shapelet_size2.ShapeletFeatureMatrix;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

/***
 *
 * @author leon bornemann
 *
 */
public class Sequence {


    public static final int MEET = 1;
    public static final int MATCH = 2;
    public static final int OVERLAP = 3;
    public static final int LEFT_CONTAINS = 4;
    public static final int CONTAINS = 5;
    public static final int RIGHT_CONTAINS = 6;
    public static final int FOLLOWED_BY = 7;

    // 1: plain old stife
    // 2: span
    // 3: distance
    // 4: span and distance
    public static int METHOD = 1;

    public static int getMaxDuration(List<Sequence> database) {
        int maxDuration = -1;
        for (Sequence seq : database) {
            maxDuration = max(maxDuration, seq.duration());
        }
        return maxDuration;
    }

    /***
     * Reads all sequences in eventId1 file and returns them as an unmodifiable list
     * @param sequenceFilePath path to the input file.
     * @return
     * @throws IOException
     */
    public static List<Sequence> readSequenceData(String sequenceFilePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(sequenceFilePath)));
        List<Sequence> sequences = new ArrayList<Sequence>();
        List<Interval> eventsOfSameSequence = new LinkedList<>();
        int curId = -1;
        while (true) {
            String line = br.readLine();
            if (line == null || line.equals("")) {
                sequences.add(new Sequence(eventsOfSameSequence));
                break;
            }
            String[] tokens = line.split(" ");
            assert (tokens.length == 4);
            int curSeqId = Integer.parseInt(tokens[0]);
            if (curSeqId == curId || curId == -1) {
                eventsOfSameSequence.add(new Interval(line));
            } else {
                sequences.add(new Sequence(eventsOfSameSequence));
                eventsOfSameSequence.clear();
                eventsOfSameSequence.add(new Interval(line));
            }
            curId = curSeqId;
        }
        br.close();
        return Collections.unmodifiableList(sequences);
    }

    public static TreeSet<Integer> getDimensionSet(List<Sequence> sequences) {
        TreeSet<Integer> dimensions = new TreeSet<>();
        for (Sequence seq : sequences) {
            dimensions.addAll(seq.getAllDimensions());
        }
        //small consistency test of the data, if this does not hold, we have eventId1 problem with the algorithms:
        //assert(dimensions.descendingIterator().next() == dimensions.size());
        return dimensions;
    }

    /***
     * uses eventId1 table and not eventId1 list of Event-objects to optimize performance, one line in this table is basically an event object
     * first column: event dimension, second column: start-time, third column: end-time
     */

    List<Interval> intervals = new ArrayList<>();

    private boolean isSorted = false;

    //number of temporal relationships between intervals is fixed.
    public static final int NUM_RELATIONSHIPS = 7;

    public Sequence(List<Interval> intervalList) {
        assert (intervalList.size() > 0);
        fillIntervals(intervalList);
        sortIntervals();
    }

    private void fillIntervals(List<Interval> intervalList) {
        for (int i = 0; i < intervalList.size(); i++) {
            intervals.add(intervalList.get(i).deepCopy());
        }
    }

    /***
     * Creates eventId1 deep-copy of the specified sequence
     * @param seq
     */
    public Sequence(Sequence seq) {
        fillIntervals(seq.getAllIntervals());
        sortIntervals();
    }

    public Collection<Integer> getAllDimensions() {
        return intervals.stream().map(i -> i.getDimension()).collect(Collectors.toSet());
    }

    /***
     * Counts all shapelet occurences in this sequence and writes it to the shapeletFeatureMatrix
     * @param seqId the id of this sequence, aka the row in the shapeletFeatureMatrix, to which the shapelet counts will be written
     * @param shapeletFeatureMatrix
     * @param epsilon maximum time span that points of time may differ from each other to still be considered equal
     */
    public void countAllShapelets(int seqId, ShapeletFeatureMatrix shapeletFeatureMatrix, int epsilon) {
        //epsilon:
        for (int i = 0; i < intervals.size(); i++) {
            int aId = intervals.get(i).getDimension();
            int aStart = intervals.get(i).getStart();
            int aEnd = intervals.get(i).getEnd();
            //all events after A are always relevant
            for (int j = i + 1; j < intervals.size(); j++) {
                int bId = intervals.get(j).getDimension();
                int bStart = intervals.get(j).getStart();
                int bEnd = intervals.get(j).getEnd();
                int relationshipId = getRelationship(aStart, aEnd, bStart, bEnd, epsilon);
                shapeletFeatureMatrix.incAt(seqId, aId, bId, relationshipId);
            }
            //some events that start at the same time as A may be before A in the order, we need to consider those as well:
            for (int j = i - 1; j >= 0; j--) {
                int bId = intervals.get(j).getDimension();
                int bStart = intervals.get(j).getStart();
                int bEnd = intervals.get(j).getEnd();
                if (aStart - epsilon > bStart) {
                    break;
                } else {
                    int relationshipId = getRelationship(aStart, aEnd, bStart, bEnd, epsilon);
                    shapeletFeatureMatrix.incAt(seqId, aId, bId, relationshipId);
                }
            }
        }
    }

    public void countAllShapelets(Map<ShapeletKey, Shapelet_Size2> database, int seqId, ShapeletFeatureMatrix shapeletFeatureMatrix, int epsilon) {
        //epsilon:
        for (int i = 0; i < intervals.size(); i++) {
            int aId = intervals.get(i).getDimension();
            int aStart = intervals.get(i).getStart();
            int aEnd = intervals.get(i).getEnd();
            //all events after A are always relevant
            for (int j = i + 1; j < intervals.size(); j++) {
                int bId = intervals.get(j).getDimension();
                int bStart = intervals.get(j).getStart();
                int bEnd = intervals.get(j).getEnd();
                int relationshipId = getRelationship(aStart, aEnd, bStart, bEnd, epsilon);
                //Shapelet_Size2 shapelet = database.get(new ShapeletKey(relationshipId, aId, bId));
                double cost = computeModelCost(relationshipId, aStart, aEnd, bStart, bEnd);
                shapeletFeatureMatrix.incAt(seqId, aId, bId, relationshipId, cost);
            }
            //some events that start at the same time as A may be before A in the order, we need to consider those as well:
            for (int j = i - 1; j >= 0; j--) {
                int bId = intervals.get(j).getDimension();
                int bStart = intervals.get(j).getStart();
                int bEnd = intervals.get(j).getEnd();
                if (aStart - epsilon > bStart) {
                    break;
                } else {
                    int relationshipId = getRelationship(aStart, aEnd, bStart, bEnd, epsilon);
                    double cost = computeModelCost(relationshipId, aStart, aEnd, bStart, bEnd);//computeCost(relationshipId, shapelet, aStart, aEnd, bStart, bEnd);
                    shapeletFeatureMatrix.incAt(seqId, aId, bId, relationshipId, cost);
                    //shapeletFeatureMatrix.incAt(seqId, aId, bId, relationshipId);
                }
            }
        }
    }

    public Map<ShapeletKey, List<Shapelet_Size2>> getAllShapeletWithKeys(int epsilon) {
        Map<ShapeletKey, List<Shapelet_Size2>> shapelets = new HashMap<>();
        for (int i = 0; i < intervals.size(); i++) {
            int aId = intervals.get(i).getDimension();
            int aStart = intervals.get(i).getStart();
            int aEnd = intervals.get(i).getEnd();
            //all events after A are always relevant
            for (int j = i + 1; j < intervals.size(); j++) {
                int bId = intervals.get(j).getDimension();
                int bStart = intervals.get(j).getStart();
                int bEnd = intervals.get(j).getEnd();
                int relationshipId = getRelationship(aStart, aEnd, bStart, bEnd, epsilon);
                ShapeletKey key = new ShapeletKey(relationshipId, aId, bId);
                List<Shapelet_Size2> list = shapelets.computeIfAbsent(key, k -> new ArrayList<>());
                list.add(new Shapelet_Size2(aId, bId, relationshipId, aStart, bStart, aEnd, bEnd));
            }
            //some events that start at the same time as A may be before A in the order, we need to consider those as well:
            for (int j = i - 1; j >= 0; j--) {
                int bId = intervals.get(j).getDimension();
                int bStart = intervals.get(j).getStart();
                int bEnd = intervals.get(j).getEnd();
                if (aStart - epsilon > bStart) {
                    break;
                } else {
                    int relationshipId = getRelationship(aStart, aEnd, bStart, bEnd, epsilon);
                    ShapeletKey key = new ShapeletKey(relationshipId, aId, bId);
                    List<Shapelet_Size2> list = shapelets.computeIfAbsent(key, k -> new ArrayList<>());
                    list.add(new Shapelet_Size2(aId, bId, relationshipId, aStart, bStart, aEnd, bEnd));
                }
            }
        }
        return shapelets;
    }

    public List<Shapelet_Size2> getAllShapelets(int epsilon) {
        List<Shapelet_Size2> shapelets = new ArrayList<>();
        for (int i = 0; i < intervals.size(); i++) {
            int aId = intervals.get(i).getDimension();
            int aStart = intervals.get(i).getStart();
            int aEnd = intervals.get(i).getEnd();
            //all events after A are always relevant
            for (int j = i + 1; j < intervals.size(); j++) {
                int bId = intervals.get(j).getDimension();
                int bStart = intervals.get(j).getStart();
                int bEnd = intervals.get(j).getEnd();
                int relationshipId = getRelationship(aStart, aEnd, bStart, bEnd, epsilon);
                shapelets.add(new Shapelet_Size2(aId, bId, relationshipId, aStart, bStart, aEnd, bEnd));
            }
            //some events that start at the same time as A may be before A in the order, we need to consider those as well:
            for (int j = i - 1; j >= 0; j--) {
                int bId = intervals.get(j).getDimension();
                int bStart = intervals.get(j).getStart();
                int bEnd = intervals.get(j).getEnd();
                if (aStart - epsilon > bStart) {
                    break;
                } else {
                    int relationshipId = getRelationship(aStart, aEnd, bStart, bEnd, epsilon);
                    shapelets.add(new Shapelet_Size2(aId, bId, relationshipId, aStart, bStart, aEnd, bEnd));
                }
            }
        }
        return shapelets;
    }

    private static int getRelationshipCorrect(double as, double ae, double bs, double be) {
        if (as == bs) {
            // left contain
            // match
            if (ae == be) {
                return Sequence.MATCH;
            } else {
                return Sequence.LEFT_CONTAINS;
            }
        } else if (ae == be) {
            // right contains
            return Sequence.RIGHT_CONTAINS;
        } else {
            // meet
            // follows
            // contains
            // overlap
            if (ae == bs || be == as) {
                return Sequence.MEET;
            } else if (as > bs && ae < be) {
                return Sequence.CONTAINS;
            } else if (bs > as && be < ae) {
                return Sequence.CONTAINS;
            } else if (ae - bs < 0) {
                return Sequence.FOLLOWED_BY;
            } else if (be - as < 0) {
                return Sequence.FOLLOWED_BY;
            } else {
                return Sequence.OVERLAP;
            }
        }

    }

    public static double computeModelCost(int relationshipId, double as, double ae, double bs, double be) {
        double span = 1;
        switch (METHOD) {
            case 1: // good'ol stife
                return 1;
            case 2: // stife with max span
                return (length(as, ae) + length(bs, be)) / 2;//Math.min(as, bs) + max(ae, be);
            case 4: // stife with max span + distance
                span = (length(as, ae) + length(bs, be)) / 2;
            case 3: // stife with only distance
                switch (relationshipId) {
                    case Sequence.OVERLAP:
                        return overlapCost(as, ae, bs, be) * span;
                    case Sequence.CONTAINS:
                        return computeContainsCost(as, ae, bs, be) * span;
                    case Sequence.LEFT_CONTAINS:
                        return computeLeftContainCost(as, ae, bs, be) * span;
                    case Sequence.RIGHT_CONTAINS:
                        return computeRightContainsCost(as, ae, bs, be) * span;
                    case Sequence.MEET:
                    case Sequence.MATCH:
                    case Sequence.FOLLOWED_BY:
                        return Math.min(length(as, ae) / length(bs, be), length(bs, be) / length(as, ae)) * span;
                    default:
                        throw new RuntimeException("illegal relation id=" + relationshipId);
                }
            default:
                throw new RuntimeException("illegal mode");
        }
    }

    private static double computeRightContainsCost(double as, double ae, double bs, double be) {
        if (as > bs) {
            double ts = as;
            double te = ae;
            as = bs;
            ae = be;
            bs = ts;
            be = te;
        }
        double numerator = abs(bs - (ae + as) / 2);
        double denominator = length(as, ae) / 2;
        return (1.0 - (numerator == 0 ? numerator : numerator / denominator));
    }

    private static double computeLeftContainCost(double as, double ae, double bs, double be) {
        if (ae < be) {
            double ts = as;
            double te = ae;
            as = bs;
            ae = be;
            bs = ts;
            be = te;
        }

        double numerator = abs(be - (ae + as) / 2);
        double denominator = length(as, ae) / 2;
        return (1.0 - (numerator == 0 ? numerator : numerator / denominator));
    }

    private static double length(double s, double e) {
        return max(1, e - s);
    }

    private static double computeContainsCost(double as, double ae, double bs, double be) {
        if (bs < as) {
            double ts = as;
            double te = ae;
            as = bs;
            ae = be;
            bs = ts;
            be = te;
        }

        double delta = 0.25;
        double numerator = abs(bs - delta * (ae - as) - as) + abs(be - ae + delta * (ae - as));
        double denominator = 2 * delta * length(as, ae);
        return (1.0 - (numerator == 0 ? numerator : numerator / denominator));
    }

    private static double overlapCost(double as, double ae, double bs, double be) {
        if (bs < as) {
            double ts = as;
            double te = ae;
            as = bs;
            ae = be;
            bs = ts;
            be = te;
        }

        double numerator = abs(bs - (ae + as) / 2) + abs(ae - (be + bs) / 2);
        double denominator = be - bs + ae - as;
        return (1.0 - (numerator == 0 ? numerator : numerator / denominator));
    }

    public static String printRel(int rel) {
        switch (rel) {
            case Sequence.OVERLAP:
                return "overlap";
            case Sequence.CONTAINS:
                return "contains";
            case Sequence.LEFT_CONTAINS:
                return "left contains";
            case Sequence.RIGHT_CONTAINS:
                return "right_contains";
            case Sequence.MEET:
                return "meet";
            case Sequence.MATCH:
                return "match";
            case Sequence.FOLLOWED_BY:
                return "followed by";
        }
        return "?";
    }

    /***
     * Method is only public, so unit-testing is possible
     * @param as start of event A
     * @param ae end of event A
     * @param bs start of event B
     * @param be end of event B
     * @param e maximum tolerance two time values can be apart to still be considered equal
     * @return An int ranging from 1 to 7 representing the temporal relationship between Interval A and B as follows:
     * 1 - meet
     * 2 - match
     * 3 - overlap
     * 4 - leftContains
     * 5 - contains
     * 6 - rightContains
     * 7 - followedBy
     */
    //TODO
    public int getRelationship(double as, double ae, double bs, double be, int e) {
        return getRelationshipCorrect(as, ae, bs, be);
//        e = 0;
//        assert (as - e <= bs);
//
//        //Aliases to make returns more readable:
//        //results = list(meet=1,match=2,overlap=3,leftContains=4,contains=5,rightContains=6,followedBy=7)
//        boolean startsMatch = bs >= as - e && bs <= as + e;
//        boolean endsMatch = be >= ae - e && be <= ae + e;
//        if (startsMatch) {
//            // can be either left-contain, match or overlap
//            if (endsMatch) {
//                return MATCH;
//            } else if (be < ae - e) {
//                return LEFT_CONTAINS;
//            } else {
//                assert (be > ae + e);
//                return OVERLAP;
//            }
//        } else {
//            // can be either right-contain, overlap,contain, meet or follow
//            boolean endMatchesStart = bs >= ae - e && bs <= ae + e;
//            if (endsMatch) {
//                return RIGHT_CONTAINS;
//            } else if (endMatchesStart) {
//                return MEET;
//            } else if (be < ae - e) {
//                return CONTAINS;
//            } else if (bs < ae - e) {
//                assert (be > ae + e);
//                return OVERLAP;
//            } else {
//                assert (bs > ae + e);
//                return FOLLOWED_BY;
//            }
//        }
    }

    public int intervalCount() {
        return intervals.size();
    }

    public int duration() {
        return intervals.stream().mapToInt(i -> i.getEnd()).max().getAsInt();
    }

    public int earliestStart() {
        return intervals.stream().mapToInt(i -> i.getStart()).min().getAsInt();
    }

    public Interval getInterval(int i) {
        return intervals.get(i);
    }

    public List<Interval> getAllIntervals() {
        return intervals;
    }

    public void rescaleTimeAxis(int newScaleMin, int newScaleMax) {
        int xmin = earliestStart();
        int xmax = duration();
        for (int i = 0; i < intervals.size(); i++) {
            int newStart = Algorithms.linearInterpolation(xmin, xmax, intervals.get(i).getStart(), newScaleMin, newScaleMax);
            int newEnd = Algorithms.linearInterpolation(xmin, xmax, intervals.get(i).getEnd(), newScaleMin, newScaleMax);
            intervals.set(i, new Interval(intervals.get(i).getDimension(), newStart, newEnd));
        }
    }

    public double countShapeletOccurance(Shapelet_Size2 shapelet, int epsilon) {
        //TODO: Test this oh man oh man :D
        int e = epsilon;
        double count = 0;
        List<Interval> relevantIntervals = new ArrayList<>();
        for (int i = 0; i < intervals.size(); i++) {
            int eventId = intervals.get(i).getDimension();
            if (eventId == shapelet.getEventId1() || eventId == shapelet.getEventId2()) {
                relevantIntervals.add(new Interval(eventId, intervals.get(i).getStart(), intervals.get(i).getEnd()));
            }
        }
        for (int i = 0; i < relevantIntervals.size(); i++) {
            int aId = relevantIntervals.get(i).getDimension();
            if (aId != shapelet.getEventId1()) {
                continue;
            }
            //if we get here we know event A is of the right dimension
            int aStart = relevantIntervals.get(i).getStart();
            int aEnd = relevantIntervals.get(i).getEnd();
            //all intervals after A are always relevant
            for (int j = i + 1; j < relevantIntervals.size(); j++) {
                int bId = relevantIntervals.get(j).getDimension();
                if (bId != shapelet.getEventId2()) {
                    continue;
                }
                int bStart = relevantIntervals.get(j).getStart();
                int bEnd = relevantIntervals.get(j).getEnd();
                int relationshipId = getRelationship(aStart, aEnd, bStart, bEnd, e);
                if (relationshipId == shapelet.getRelationshipId()) {
                    // do distance here
                    count++;
                }
            }
            //some intervals that start at the same time as A may be before A in the order, we need to consider those as well:
            for (int j = i - 1; j >= 0; j--) {
                int bId = relevantIntervals.get(j).getDimension();
                int bStart = relevantIntervals.get(j).getStart();
                int bEnd = relevantIntervals.get(j).getEnd();
                if (aStart - e > bStart) {
                    break;
                } else if (bId != shapelet.getEventId2()) {
                    continue;
                } else {
                    int relationshipId = getRelationship(aStart, aEnd, bStart, bEnd, e);
                    if (relationshipId == shapelet.getRelationshipId()) {

                        // do distance here
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public double computeShapeletSimilarity(Shapelet_Size2 shapelet, int epsilon) {
        //TODO: Test this oh man oh man :D
        int e = epsilon;
        double count = 0;
        double matches = 0;
        List<Interval> relevantIntervals = new ArrayList<>();
        for (int i = 0; i < intervals.size(); i++) {
            int eventId = intervals.get(i).getDimension();
            if (eventId == shapelet.getEventId1() || eventId == shapelet.getEventId2()) {
                relevantIntervals.add(new Interval(eventId, intervals.get(i).getStart(), intervals.get(i).getEnd()));
            }
        }
        for (int i = 0; i < relevantIntervals.size(); i++) {
            int aId = relevantIntervals.get(i).getDimension();
            if (aId != shapelet.getEventId1()) {
                continue;
            }
            //if we get here we know event A is of the right dimension
            int aStart = relevantIntervals.get(i).getStart();
            int aEnd = relevantIntervals.get(i).getEnd();
            //all intervals after A are always relevant
            for (int j = i + 1; j < relevantIntervals.size(); j++) {
                int bId = relevantIntervals.get(j).getDimension();
                if (bId != shapelet.getEventId2()) {
                    continue;
                }
                int bStart = relevantIntervals.get(j).getStart();
                int bEnd = relevantIntervals.get(j).getEnd();
                int relationshipId = getRelationship(aStart, aEnd, bStart, bEnd, e);
                if (relationshipId == shapelet.getRelationshipId()) {
                    // do distance here
                    double cost = computeModelCost(relationshipId, aStart, aEnd, bStart, bEnd);//computeCost(relationshipId, shapelet, aStart, aEnd, bStart, bEnd);
                    /*if (cost > count) {
                        count = cost;
                    }*/
                    count += cost;
                    matches++;
                }
            }
            //some intervals that start at the same time as A may be before A in the order, we need to consider those as well:
            for (int j = i - 1; j >= 0; j--) {
                int bId = relevantIntervals.get(j).getDimension();
                int bStart = relevantIntervals.get(j).getStart();
                int bEnd = relevantIntervals.get(j).getEnd();
                if (aStart - e > bStart) {
                    break;
                } else if (bId != shapelet.getEventId2()) {
                    continue;
                } else {
                    int relationshipId = getRelationship(aStart, aEnd, bStart, bEnd, e);
                    if (relationshipId == shapelet.getRelationshipId()) {
                        double cost = computeModelCost(relationshipId, aStart, aEnd, bStart, bEnd);//computeCost(relationshipId, shapelet, aStart, aEnd, bStart, bEnd);
                       /* if (cost > count) {
                            count = cost;
                        }*/
                        count += cost;
                        matches++;
                    }
                }
            }
        }
        // return matches > 0 ? count/matches : 0;
        return count;
    }

    public double computeCost(int relationshipId, Shapelet_Size2 shapelet, int as, int ae, int bs, int be) {
        //return 1;
        double span = 1;//Math.min(as, bs) + max(ae, be);
        double numerator = 0;
        double denominator = 0;
        int alen = max(1, ae - as);
        int blen = max(1, be - bs);
        switch (relationshipId) {
            case OVERLAP:
                numerator = abs(bs - (ae + as) / 2) + abs(ae - (be + bs) / 2);
                denominator = be - bs + ae - as;
                break;
//    return overlapCost(shapelet, aEnd, bStart, aLen, bLen);
            case CONTAINS:
                double delta = 0.25;
                numerator = abs(bs - delta * ae + (delta - 1) * as) + be - delta * as * (delta - 1) * ae;
                denominator = 2 * delta * alen;
                break;
            //return containsCost(shapelet, aLen, bLen);
            case LEFT_CONTAINS:
                numerator = abs(be - (ae + as) / 2) + abs(bs - as);
                denominator = (be - bs) / 2;
                break;
            case RIGHT_CONTAINS:
                numerator = abs(bs - (ae + as) / 2) + abs(be - ae);
                denominator = (be - bs) / 2;
                break;
            case MEET:
            case MATCH:
            case FOLLOWED_BY:
                return min(abs(alen) / abs(blen), abs(blen) / abs(alen));
            default:
                throw new RuntimeException("illegal relation");
                //return defaultCost(shapelet, aLen, bLen);
        }
        return (1.0 - (numerator == 0 ? numerator : numerator / denominator)) * span;
    }

    private double defaultCost(Shapelet_Size2 shapelet, int aLen, int bLen) {
        double numerator = abs(aLen - shapelet.getALen()) + abs(bLen - shapelet.getBlen());
        double denum = max(aLen, shapelet.getALen()) + max(bLen, shapelet.getBlen());
        return 1 - (numerator / denum);
    }

    private int containsCost(Shapelet_Size2 shapelet, int aLen, int bLen) {
        return 1 - abs(bLen / aLen - shapelet.getBlen() / shapelet.getALen());
    }

    private double overlapCost(Shapelet_Size2 shapelet, int aEnd, int bStart, int aLen, int bLen) {
        double olLhs = abs(aEnd - bStart) / min(aLen, bLen);
        double olRhs = abs(shapelet.getaEnd() - shapelet.getbStart()) / min(shapelet.getALen(), shapelet.getBlen());
        return 1 - abs(olLhs - olRhs);
    }


    /***
     * Orders Intervals in this sequence according to the following scheme: startTime, (if startTime is equal: endTime), if both of these are equal, dimension
     */
    public void sortIntervals() {
        intervals.sort(new StandardIntervalComparator());
        isSorted = true;
    }

    public Map<Integer, Integer> getDimensionOccurances() {
        Map<Integer, Integer> occuranceMap = new HashMap<>();
        for (int i = 0; i < intervals.size(); i++) {
            int curDim = intervals.get(i).getDimension();
            if (occuranceMap.containsKey(curDim)) {
                occuranceMap.put(curDim, occuranceMap.get(curDim) + 1);
            } else {
                occuranceMap.put(curDim, 1);
            }
        }
        return occuranceMap;
    }

    public long getDensity() {
        long density = 0;
        for (int i = 0; i < intervals.size(); i++) {
            density += intervals.get(i).getEnd() - intervals.get(i).getStart();
        }
        return density;
    }

    public int getMaxConcurrentIntervalCount() {
        //TODO: test this
        PriorityQueue<Integer> endTimes = new PriorityQueue<>();
        for (Interval interval : intervals) {
            endTimes.add(interval.getEnd());
        }
        int searchStartRow = 0;
        int maxCount = -1;
        int ends = 0;
        while (!endTimes.isEmpty()) {
            Integer curEnd = endTimes.remove();
            int intervalCount = 0;
            for (int i = searchStartRow; i < intervals.size(); i++) {
                int curStart = intervals.get(i).getStart();
                if (curStart > curEnd || i == intervals.size() - 1) {
                    if (i == intervals.size() - 1) {
                        intervalCount = i + 1 - ends;
                    } else {
                        intervalCount = i - ends;
                        searchStartRow = i;
                    }
                    break;
                }
            }
            ends++;
            assert (intervalCount > 0 || endTimes.isEmpty());
            if (intervalCount > maxCount) {
                maxCount = intervalCount;
            }
        }
        return maxCount;
    }

    public int getConcurrentIntervalDuration(int numConcurrentIntervals) {
        // TODO test this
        List<Event> startAndStopEvents = getSortedEventList();
        int curEventsActive = 0;
        int maxDuration = 0;
        for (int i = 0; i < startAndStopEvents.size(); i++) {
            Event curEvent = startAndStopEvents.get(i);
            if (curEvent.getEventType() == EventType.Start) {
                curEventsActive++;
            } else {
                curEventsActive--;
            }
            assert (curEventsActive <= numConcurrentIntervals);
            if (curEventsActive == numConcurrentIntervals) {
                Event nextEvent = startAndStopEvents.get(i + 1);
                assert (nextEvent.getEventType() == EventType.End);
                int curDuration = nextEvent.getPointOfTime() - curEvent.getPointOfTime();
                if (curDuration > maxDuration) {
                    maxDuration = curDuration;
                }
            }
        }
        return maxDuration;
    }

    private List<Event> getSortedEventList() {
        List<Event> startAndStopEvents = new ArrayList<>();
        for (Interval interval : intervals) {
            startAndStopEvents.add(new Event(interval.getStart(), interval.getDimension(), EventType.Start));
            startAndStopEvents.add(new Event(interval.getEnd(), interval.getDimension(), EventType.End));
        }
        Collections.sort(startAndStopEvents);
        return startAndStopEvents;
    }

    public int getSummedPauseTime() {
        List<Event> startAndStopEvents = getSortedEventList();
        int curEventsActive = 0;
        int duration = 0;
        for (int i = 0; i < startAndStopEvents.size(); i++) {
            Event curEvent = startAndStopEvents.get(i);
            if (curEvent.getEventType() == EventType.Start) {
                curEventsActive++;
            } else {
                curEventsActive--;
            }
            assert (curEventsActive >= 0);
            if (curEventsActive == 0 && i != startAndStopEvents.size() - 1) {
                Event nextEvent = startAndStopEvents.get(i + 1);
                assert (nextEvent.getEventType() == EventType.Start);
                duration += nextEvent.getPointOfTime() - curEvent.getPointOfTime();
            }
        }
        return duration;
    }

    public void reassignDimensionIds(HashMap<Integer, Integer> newDimensionMapping) {
        for (int i = 0; i < intervals.size(); i++) {
            intervals.set(i, new Interval(newDimensionMapping.get(intervals.get(i).getDimension()), intervals.get(i).getStart(), intervals.get(i).getEnd()));
        }
    }

    public boolean containsNSHapelet(NShapelet shapelet, int epsilon) {
        List<Pair<Integer, Integer>> occurrences = shapelet.get2Shapelet(0).getAllOccurrences(this, epsilon);
        Set<Integer> relevantPreviousIds = occurrences.stream()
                .map(p -> p.getSecond())
                .distinct()
                .collect(Collectors.toSet());
        for (int i = 1; i < shapelet.numTwoShapelets(); i++) {
            if (relevantPreviousIds.isEmpty()) {
                return false;
            }
            ShapeletSize2 curShapelet = shapelet.get2Shapelet(i);
            Set<Integer> newPreviousIds = new HashSet<>();
            for (int intervalId : relevantPreviousIds) {
                assert (getInterval(intervalId).getDimension() == curShapelet.getEventId1());
                List<Integer> occ = curShapelet.getOccurrences(this, intervalId, epsilon);
                newPreviousIds.addAll(occ);
            }
            relevantPreviousIds = newPreviousIds;
        }
        return !relevantPreviousIds.isEmpty();
    }

    public List<List<Integer>> getAllOccurrences(NShapelet shapelet, int epsilon) {
        List<Pair<Integer, Integer>> occurrences = shapelet.get2Shapelet(0).getAllOccurrences(this, epsilon);
        List<List<Integer>> allOccurrences = occurrences.stream().map(p -> {
            ArrayList<Integer> list = new ArrayList<Integer>();
            list.add(p.getFirst());
            list.add(p.getSecond());
            return list;
        }).collect(Collectors.toList());
        for (int i = 1; i < shapelet.numTwoShapelets(); i++) {
            if (allOccurrences.isEmpty()) {
                return allOccurrences;
            }
            Map<Integer, List<List<Integer>>> occurrencesByLastIntervalId = allOccurrences.stream().collect(Collectors.groupingBy(l -> l.get(l.size() - 1)));
            ShapeletSize2 curShapelet = shapelet.get2Shapelet(i);
            List<Integer> listOfPreviousIntervalIdSorted = occurrencesByLastIntervalId.keySet().stream().sorted().collect(Collectors.toList());
            List<List<Integer>> newAllOccurrences = new ArrayList<>();
            for (int intervalId : listOfPreviousIntervalIdSorted) {
                assert (getInterval(intervalId).getDimension() == curShapelet.getEventId1());
                List<Integer> fittingIntervalIds = curShapelet.getOccurrences(this, intervalId, epsilon);
                for (Integer toAppend : fittingIntervalIds) {
                    List<List<Integer>> allOccurrencesToAppendTo = occurrencesByLastIntervalId.get(intervalId);
                    for (int j = 0; j < allOccurrencesToAppendTo.size(); j++) {
                        //for each element in fitting intervalIds, add eventId1 new list
                        List<Integer> occurrence = allOccurrencesToAppendTo.get(j);
                        ArrayList<Integer> newOccurrence = new ArrayList<>(occurrence);
                        newOccurrence.add(toAppend);
                        if (new HashSet<>(newOccurrence).size() == newOccurrence.size()) {
                            //only occurrences with each interval being used only once are allowed
                            newAllOccurrences.add(newOccurrence);
                        }
                    }
                }
            }
            allOccurrences = newAllOccurrences;
        }
        //sort all Occurrences, so that we always get the same order in each execution:
        Collections.sort(allOccurrences, (l1, l2) -> {
            int i = 0;
            while (l1.get(i).compareTo(l2.get(i)) == 0 && i < l1.size()) {
                i++;
            }
            if (i == l1.size()) {
                return 0;
            } else {
                return l1.get(i).compareTo(l2.get(i));
            }
        });
        return allOccurrences;
    }


    /***
     * Returns all intervals that are within the bounds given to the function
     * @param startTimeInclusive
     * @param endTimeInclusive
     *
     * @return
     */
    public List<Pair<Integer, Interval>> getIntervalsInTimeRange(int startTimeInclusive, int endTimeInclusive, int forbidden) {
        List<Pair<Integer, Interval>> results = new ArrayList<>();
        for (int i = 0; i < intervals.size(); i++) {
            Interval interval = intervals.get(i);
            if (interval.getStart() >= startTimeInclusive && interval.getEnd() <= endTimeInclusive && i != forbidden) {
                results.add(new Pair<>(i, interval));
            }
        }
        return results;
        //TODO: mit bin�rsuche machen (codeschnipsel unten)
//		assert(isSorted);
//		int startIndex = 0;
//		int endIndex = intervals.size();
//		int equalIndex = -1;
//		while(startIndex <=endIndex){
//			int toCheck =  startIndex + ((endIndex - startIndex) / 2);
//			int curStart = intervals.get(toCheck).getStart();
//			if(curStart == startTimeInclusive){
//				equalIndex = toCheck;
//				break;
//			} else if(curStart>startTimeInclusive){
//				endIndex = toCheck-1;
//			} else{
//				startIndex = toCheck+1;
//			}
//		}
    }

    @Override
    public String toString() {
        return intervals.toString();
    }

    public int getRelationship(int intervalId1, int intervalId2, int epsilon) {
        assert (intervalId2 > intervalId1);
        Interval a = getInterval(intervalId1);
        Interval b = getInterval(intervalId2);
        return getRelationship(a.getStart(), a.getEnd(), b.getStart(), b.getEnd(), epsilon) - 1; //-1 due to getRelationship being eventId1 different interface that stupidly starts counting at 1 (relly should fix that)
    }

}
