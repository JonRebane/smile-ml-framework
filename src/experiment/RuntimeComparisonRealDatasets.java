package experiment;

import data_structures.Interval;
import data_structures.Sequence;

import java.io.File;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Double.max;
import static java.lang.Math.abs;

public class RuntimeComparisonRealDatasets {

    public static void main(String[] args) throws Exception {
        Sequence sequence = new Sequence(Arrays.asList(new Interval(0, 1, 10), new Interval(1, 5, 15)));

        //int rel = sequence.getRelationship(1, 15, 1, 10, 0);


        Random r = new Random(1);
        int max = 30;
        for (int i = 0; i < 0; i++) {
            int ae = r.nextInt(max) + 1;
            int as = r.nextInt(ae) + 1;

            int be = r.nextInt(max) + 1;
            int bs = r.nextInt(be) + 1;
            testSeq(sequence, max, as, ae, bs, be);
        }


//		double as = 5.5;
//		double ae = 14.5;
//		double bs = 1;
//		double be = 10;
        double as = 2;
        double ae = 10;
        double bs = 4;
        double be = 8;
        //testSeq(sequence, 15, as, ae, bs, be);

        bs = 2;
        be = 10;
        as = 4;
        ae = 8;
       // testSeq(sequence, 15, as, ae, bs, be);

        testSeq(sequence, 30, 2, 24, 1, 18);
        testSeq(sequence, 30, 1, 18, 2, 24);
        testSeq(sequence, 30, 6, 27, 1, 5);
        testSeq(sequence, 30, 1, 5, 6, 27);
        testSeq(sequence, 30, 5, 27, 1, 5);
        testSeq(sequence, 30, 1, 5, 5, 27);
        /*
        a:(6,27) b:(1, 5)contains but shuld be followed by
        :(2,24) b:(1, 18)contains but shuld be overlap
a:(2,24) b:(1, 18)contains but shuld be overlap
a:(2,24) b:(1, 16)contains but shuld be overlap
a:(2,24) b:(1, 16)contains but shuld be overlap
a:(2,24) b:(1, 14)contains but shuld be overlap
a:(2,24) b:(1, 14)contains but shuld be overlap
a:(2,24) b:(1, 12)contains but shuld be overlap
a:(2,24) b:(1, 12)contains but shuld be overlap
a:(2,24) b:(1, 12)contains but shuld be overlap
a:(2,24) b:(1, 10)contains but shuld be overlap

         */

//		System.out.print("Relation: ");
//		getRelationship(1,53,26,76);
//		System.out.println();
//		printSeq(1, 53, 26, 76, 100);
//		System.out.println(sequence.computeCost(Sequence.OVERLAP, null, 1, 53, 26, 76));
//		System.exit(0);
//
      //  System.exit(0);

        Sequence.METHOD = 1;
        ExecutorService pool = Executors.newCachedThreadPool();
        int epsilon = 5;
        int shapeletFeatureCount = 75;
        File singleLabelDatasetPath = new File("data/singleLabelDatasets");
        File multiLabelDatasetPath = new File("data/multiLabelDatasets");
        // seed: 13
        RealDataExperiment experiment = new RealDataExperiment(pool, epsilon, shapeletFeatureCount, singleLabelDatasetPath, multiLabelDatasetPath, new Random(13), 10);
        experiment.runExperiment();
        pool.shutdown();
    }

    private static void testSeq(Sequence sequence, int max, double as, double ae, double bs, double be) {
        int relA = getRelationship(as, ae, bs, be);
        int relB = sequence.getRelationship(as, ae, bs, be, 0);
        System.out.print("new relation: '" + Sequence.printRel(relA));
        System.out.print("' old relation: '" + Sequence.printRel(relB));
        System.out.println("'");
        printSeq(as, ae, bs, be, max);
        System.out.println("Cost: " + computeModelCost(relA, as, ae, bs, be));
        System.out.println();
    }

    public static double computeModelCost(int relationshipId, double as, double ae, double bs, double be) {
        double span = 1;//Math.min(as, bs) + max(ae, be);

        switch (relationshipId) {
            case Sequence.OVERLAP:
                return overlapCost(as, ae, bs, be);
            case Sequence.CONTAINS:
                return computeContainsCost(as, ae, bs, be);
            case Sequence.LEFT_CONTAINS:
                return computeLeftContainCost(as, ae, bs, be);
            case Sequence.RIGHT_CONTAINS:
                return computeRightContainsCost(as, ae, bs, be);
            case Sequence.MEET:
            case Sequence.MATCH:
            case Sequence.FOLLOWED_BY:
                return Math.min(length(as, ae) / length(bs, be), length(bs, be) / length(as, ae));
            default:
                throw new RuntimeException("illegal relation");
                //return defaultCost(shapelet, aLen, bLen);
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

    private static int getRelationship(double as, double ae, double bs, double be) {
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

            // meet
            // follows
            // contains
            // overlap


        }

    }

    private static void printSeq(double as, double ae, double bs, double be, int max) {
        System.out.print("|");
        generateLine(as, ae, max);
        System.out.printf("| a: (%.2f,%.2f)\n", as, ae);
        System.out.print("|");
        generateLine(bs, be, max);
        System.out.printf("| b: (%.2f,%.2f)\n", bs, be);
    }

    private static void generateLine(double s, double e, int max) {
        for (int j = 1; j <= max; j++) {
            if (j >= s && j <= e) {
                System.out.print("-");
            } else {
                System.out.print(" ");
            }
        }
    }
}
