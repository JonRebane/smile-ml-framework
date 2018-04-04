package experiment.classifier;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import data_structures.Sequence;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class SingleLabelSTIFERFClassifier extends AbstractSTIFERFClassifier {

    public SingleLabelSTIFERFClassifier(Random random,
                                        Function<Instances, Classifier> consumer,
                                        List<Sequence> train, List<Integer> classIds,
                                        int numDimensions,
                                        int sequenceDuration,
                                        int epsilon,
                                        int shapeletFeatureCount,
                                        int eletFeatureCount,
                                        String method,
                                        ExecutorService pool) throws Exception {
        super(random, consumer, train, classIds, numDimensions, sequenceDuration, epsilon, shapeletFeatureCount, eletFeatureCount, method, pool);
    }

}
