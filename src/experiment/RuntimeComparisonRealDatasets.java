package experiment;

import data_structures.Interval;
import data_structures.Sequence;

import java.io.File;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Math.abs;
import static java.lang.Math.max;

public class RuntimeComparisonRealDatasets {

	public static void main(String[] args) throws Exception {
		Sequence sequence = new Sequence(Arrays.asList(new Interval(0, 1, 10), new Interval(1, 5, 15)));

		//int rel = sequence.getRelationship(1, 15, 1, 10, 0);


		Random r = new Random(1);
		int max = 10;
		for (int i = 0; i < 5; i++) {
			int ae = r.nextInt(max)+1;
			int as = r.nextInt(ae) + 1;

			int be = r.nextInt(max)+1;
			int bs = r.nextInt(be)+1;
			System.out.print("Relation: ");
			int rel = getRelationship(as, ae, bs, be);
			System.out.println();
			printSeq(as, ae, bs, be, max);
			System.out.println("Cost: " + computeCost(rel, as, ae, bs, be));
			System.out.println();
		}


		int rel = getRelationship(1, 10, 1, 5);
		System.out.println();
		printSeq(1, 10, 1, 5, 15);
		System.out.println(computeCost(rel, 1, 10, 1, 5));





		System.out.print("Relation: ");
		getRelationship(1,53,26,76);
		System.out.println();
		printSeq(1, 53, 26, 76, 100);
		System.out.println(sequence.computeCost(Sequence.OVERLAP, null, 1, 53, 26, 76));
		System.exit(0);

		ExecutorService pool = Executors.newCachedThreadPool();
		int epsilon = 5;
		int shapeletFeatureCount = 75;
		File singleLabelDatasetPath = new File("data/singleLabelDatasets");
		File multiLabelDatasetPath = new File("data/multiLabelDatasets");
		// seed: 13
		RealDataExperiment experiment = new RealDataExperiment(pool,epsilon,shapeletFeatureCount,singleLabelDatasetPath,multiLabelDatasetPath,new Random(13),10);
		experiment.runExperiment();
		pool.shutdown();
	}

	public static double computeCost(int relationshipId, double as, double ae, double bs, double be) {
		// lets make b the smaller one
		double alen = max(1, ae - as);
		double blen = max(1, be - bs);

		if (alen < blen) {
			double ts = as;
			double te = ae;
			double tlen = alen;
			as = bs;
			ae = be;
			alen = blen;
			bs = ts;
			be = te;
			blen = tlen;
		}



		//return 1;
		double span = 1;//Math.min(as, bs) + max(ae, be);
		double numerator = 0;
		double denominator = 0;

		switch (relationshipId) {
			case Sequence.OVERLAP:
				numerator = abs(bs - (ae + as) / 2) + abs(ae - (be + bs) / 2);
				denominator = be - bs + ae - as;
				break;
//    return overlapCost(shapelet, aEnd, bStart, aLen, bLen);
			case Sequence.CONTAINS:
				double delta = 0.25;
				numerator = abs(bs - delta * (ae - as) - as) + abs(be - ae + delta * (ae - as));
				denominator = 2 * delta * alen;
				break;
			//return containsCost(shapelet, aLen, bLen);
			case Sequence.LEFT_CONTAINS:
				numerator = abs(bs - (ae + as) / 2) + abs(bs - as);
				denominator = (be - bs) / 2;
				break;
			case Sequence.RIGHT_CONTAINS:
				numerator = abs(be - (ae + as) / 2) + abs(be - ae);
				denominator = (be - bs) / 2;
				break;
			case Sequence.MEET:
			case Sequence.MATCH:
			case Sequence.FOLLOWED_BY:
				return Math.min(abs(alen) / abs(blen), abs(blen) / abs(alen));
			default:
				throw new RuntimeException("illegal relation");
				//return defaultCost(shapelet, aLen, bLen);
		}
		return (1.0 - (numerator == 0 ? numerator : numerator / denominator)) * span;
	}

	private static int getRelationship(int as, int ae, int bs, int be) {
		if (as == bs) {
			// left contain
			// match
			if (ae == be) {
				System.out.print("match");
				return Sequence.MATCH;
			}else {
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
			} else if(bs < as && be < ae) {
				System.out.println("a contains b");
				return Sequence.CONTAINS;
			}else if (ae - bs < 0) {
				System.out.print("b follows a");
				return Sequence.FOLLOWED_BY;
			} else if(be - as < 0){
				System.out.println("a follows b");
				return Sequence.FOLLOWED_BY;
			}else {
				System.out.print("overlap");
				return Sequence.OVERLAP;
			}

			// meet
			// follows
			// contains
			// overlap


		}

	}

	private static void printSeq(int as, int ae, int bs, int be, int max) {
		System.out.print("|");
		for (int j = 1; j <= max; j++) {
            if (j >= as && j <= ae) {
                System.out.print("-");
            } else {
				System.out.print(" ");
			}
        }
		System.out.printf("| a: (%d,%d)\n", as, ae);
		System.out.print("|");
		for (int j = 1; j <= max; j++) {
            if (j >= bs && j <= be) {
                System.out.print("-");
            }else{
				System.out.print(" ");
			}
        }
		System.out.printf("| b: (%d,%d)\n", bs, be);
	}
}
