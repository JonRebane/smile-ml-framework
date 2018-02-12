package experiment;

import data_structures.Interval;
import data_structures.Sequence;

import java.util.Arrays;
import java.util.Random;

import static java.lang.Double.max;
import static java.lang.Math.abs;

public class RuntimeComparisonRealDatasets {

    public static void main(String[] args) throws Exception {
        Sequence sequence = new Sequence(Arrays.asList(new Interval(0, 1, 10), new Interval(1, 5, 15)));

        //int rel = sequence.getRelationship(1, 15, 1, 10, 0);


        Random r = new Random(1);
        int max = 10;
        for (int i = 0; i < 10; i++) {
            int ae = r.nextInt(max) + 1;
            int as = r.nextInt(ae) + 1;

            int be = r.nextInt(max) + 1;
            int bs = r.nextInt(be) + 1;
            System.out.print("Relation: ");
            int rel = getRelationship(as, ae, bs, be);
            System.out.println();
            printSeq(as, ae, bs, be, max);
            System.out.println("Cost: " + computeModelCost(rel, as, ae, bs, be));
            System.out.println();
        }


//		double as = 5.5;
//		double ae = 14.5;
//		double bs = 1;
//		double be = 10;
        double bs = 15;
        double be = 15;
        double as = 1;
        double ae = 15;
        int rel = getRelationship(as, ae, bs, be);
        System.out.println();
        printSeq(as, ae, bs, be, 30);
        System.out.println(computeModelCost(rel, as, ae, bs, be));


//		System.out.print("Relation: ");
//		getRelationship(1,53,26,76);
//		System.out.println();
//		printSeq(1, 53, 26, 76, 100);
//		System.out.println(sequence.computeCost(Sequence.OVERLAP, null, 1, 53, 26, 76));
//		System.exit(0);
//
//		ExecutorService pool = Executors.newCachedThreadPool();
//		int epsilon = 5;
//		int shapeletFeatureCount = 75;
//		File singleLabelDatasetPath = new File("data/singleLabelDatasets");
//		File multiLabelDatasetPath = new File("data/multiLabelDatasets");
//		 seed: 13
//		RealDataExperiment experiment = new RealDataExperiment(pool,epsilon,shapeletFeatureCount,singleLabelDatasetPath,multiLabelDatasetPath,new Random(13),10);
//		experiment.runExperiment();
//		pool.shutdown();
    }

    public static double computeModelCost(int relationshipId, double as, double ae, double bs, double be) {
        double span = 1;//Math.min(as, bs) + max(ae, be);

        switch (relationshipId) {
            case Sequence.OVERLAP:
                return overlapCost(as, ae, bs, be);
            case Sequence.CONTAINS:
//      case Sequence.LEFT_CONTAINS:
//      case Sequence.RIGHT_CONTAINS:
                return computeContainsCost(as, ae, bs, be);
            //	break;
            //return containsCost(shapelet, aLen, bLen);
            case Sequence.LEFT_CONTAINS:
                return computeLeftContainCost(as, ae, bs, be);
            case Sequence.RIGHT_CONTAINS:
                return computeRightContainsCost(as, ae, bs, be);
            case Sequence.MEET:
            case Sequence.MATCH:
            case Sequence.FOLLOWED_BY:
                return Math.min(abs(ae - as) / abs(be - bs), abs(be - bs) / abs(ae - as));
            default:
                throw new RuntimeException("illegal relation");
                //return defaultCost(shapelet, aLen, bLen);
        }

    }

    private static double computeRightContainsCost(double as, double ae, double bs, double be) {
        if (bs > as) {
            double ts = as;
            double te = ae;
            as = bs;
            ae = be;
            bs = ts;
            be = te;
        }
        double numerator = abs(be - (ae + as) / 2) + abs(be - ae);
        double denominator = (be - bs) / 2;
        return (1.0 - (numerator == 0 ? numerator : numerator / denominator));
    }

    private static double computeLeftContainCost(double as, double ae, double bs, double be) {
        if (be < ae) {
            double ts = as;
            double te = ae;
            as = bs;
            ae = be;
            bs = ts;
            be = te;
        }

        double numerator = abs(bs - (ae + as) / 2) + abs(bs - as);
        double denominator = length(bs, be) / 2;
        return (1.0 - (numerator == 0 ? numerator : numerator / denominator));
    }

    private static double length(double bs, double be) {
        return max(1, be - bs);
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
        double denominator = 2 * delta * length(ae, as);
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
                System.out.print("match");
                return Sequence.MATCH;
            } else {
                System.out.print("left contains");
                return Sequence.LEFT_CONTAINS;
            }
        } else if (ae == be) {
            // right contains
            System.out.print("right contains");
            return Sequence.RIGHT_CONTAINS;
        } else {
            if (ae == bs || be == as) {
                System.out.print("meet");
                return Sequence.MEET;
            } else if (as > bs && ae < be) {
                System.out.print("b contains a");
                return Sequence.CONTAINS;
            } else if (bs > as && be < ae) {
                System.out.println("a contains b");
                return Sequence.CONTAINS;
            } else if (ae - bs < 0) {
                System.out.print("b follows a");
                return Sequence.FOLLOWED_BY;
            } else if (be - as < 0) {
                System.out.println("a follows b");
                return Sequence.FOLLOWED_BY;
            } else {
                System.out.print("overlap");
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
