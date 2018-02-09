package experiment.classifier;

import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import data_structures.CompressedEventTable;
import data_structures.Pair;
import data_structures.Sequence;
import data_structures.ShapeletKey;
import stife.distance.DistanceFeatureExtractor;
import stife.distance.DistanceFeatureMatrix;
import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;
import stife.shapelet.evolution.Shapelet_Size2FitnessEvaluator;
import stife.shapelet_size2.Shapelet_Size2;
import stife.shapelet_size2.ShapeletExtractor;
import stife.shapelet_size2.ShapeletFeatureMatrix;
import stife.shapelet_size2_new.ShapeletSize2;
import stife.static_metrics.StaticFeatureMatrix;
import stife.static_metrics.StaticMetricExtractor;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

public abstract class AbstractSTIFERFClassifier implements STIClassifier<Integer> {


    private final Map<ShapeletKey, Shapelet_Size2> database;
    private DistanceFeatureMatrix distanceFeatureMatrix;
    private ShapeletFeatureMatrix shapeletFeatureMatrix;
    private int epsilon;
    private int sequenceDuration;
    private int numDimensions;
    private RandomForest rf;
    private StaticMetricExtractor staticMetricFeatureExtractor;
    private FastVector allAttributes;
    private Attribute classAttribute;
    private Instances trainInstances;
    private HashSet<Integer> classIdsset;

    private final Random random;

    public AbstractSTIFERFClassifier(Random random, List<Sequence> train, List<Integer> classIds, int numDimensions, int sequenceDuration, int epsilon, int shapeletFeatureCount, ExecutorService pool) throws Exception {
        this.random = random;
        this.epsilon = epsilon;
        this.sequenceDuration = sequenceDuration;
        this.numDimensions = numDimensions;
        classIdsset = new HashSet<>(classIds);
        for (Sequence seq : train) {
            seq.sortIntervals();
        }
        //TODO: static metrics
        staticMetricFeatureExtractor = new StaticMetricExtractor();
        StaticFeatureMatrix staticFeatureMatrix = staticMetricFeatureExtractor.extractAll(train);
        //distance:
        DistanceFeatureExtractor distanceFeatureExtractor = new DistanceFeatureExtractor(train, classIds, numDimensions, sequenceDuration);
        distanceFeatureMatrix = distanceFeatureExtractor.calculateDistanceFeatureMatrix();
        //shapelets:
        shapeletFeatureMatrix = new ShapeletFeatureMatrix(train.size(), numDimensions, Sequence.NUM_RELATIONSHIPS, classIds);

        Map<ShapeletKey, List<Shapelet_Size2>> shapelets = new HashMap<>();
        for (Sequence sequence : train) {
            Map<ShapeletKey, List<Shapelet_Size2>> sequenceShapelets = sequence.getAllShapeletWithKeys(epsilon);
            for (Map.Entry<ShapeletKey, List<Shapelet_Size2>> kv : sequenceShapelets.entrySet()) {
                List<Shapelet_Size2> all = shapelets.computeIfAbsent(kv.getKey(), l -> new ArrayList<>());
                all.addAll(kv.getValue());
            }
        }
        Shapelet_Size2FitnessEvaluator fitnessEvaluator = new Shapelet_Size2FitnessEvaluator(train, classIds, epsilon);
        this.database = createShapeletDatabase(fitnessEvaluator, shapelets);

        //create all the jobs:
        int numSequencesPerJob = 10;
        int prev = 0;
        List<ShapeletExtractor> jobs = new LinkedList<>();
        for (int i = 0; i < train.size(); i += numSequencesPerJob) {
            jobs.add(new ShapeletExtractor(database, train, prev, Math.min(i + numSequencesPerJob, train.size()), shapeletFeatureMatrix, epsilon));
            prev = i + numSequencesPerJob;
        }
        //submit all jobs
        Collection<Future<?>> futures = new LinkedList<Future<?>>();
        for (ShapeletExtractor job : jobs) {
            futures.add(pool.submit(job));
        }
        for (Future<?> future : futures) {
            future.get();
        }

        shapeletFeatureMatrix.featureSelection(shapeletFeatureCount);
        trainInstances = buildInstances(train, classIds, staticFeatureMatrix, distanceFeatureMatrix.getMatrix(), shapeletFeatureMatrix.getMatrix(), "testdata" + File.separator + "stifeTrainData.csv");
        rf = new RandomForest();
        Integer numFeaturesPerTree = (int) Math.sqrt(trainInstances.numAttributes() - 1);
        rf.setOptions(new String[]{"-I", "500", "-K", numFeaturesPerTree.toString()});
        rf.buildClassifier(trainInstances);
        allAttributes = new FastVector();
        for (int col = 0; col < trainInstances.numAttributes(); col++) {
            allAttributes.addElement(trainInstances.attribute(col));
        }
        classAttribute = trainInstances.classAttribute();
    }

    private Map<ShapeletKey, Shapelet_Size2> createShapeletDatabase(Shapelet_Size2FitnessEvaluator fitnessEvaluator, Map<ShapeletKey, List<Shapelet_Size2>> shapelets) {
        Map<ShapeletKey, Shapelet_Size2> database = new HashMap<>();
        for (Map.Entry<ShapeletKey, List<Shapelet_Size2>> kv : shapelets.entrySet()) {
        /*    Shapelet_Size2 bestShapelet = null;
            double bestSoFar = Double.NEGATIVE_INFINITY;
            List<Shapelet_Size2> value = kv.getValue();
            Collections.shuffle(value);
            int end = (int) Math.round(value.size() * 0.1)+1;
            //System.out.println(end);
            for (Shapelet_Size2 s : value.subList(0, end)) {
                double fitness = fitnessEvaluator.getFitness(s);
                if (fitness > bestSoFar) {
                    bestShapelet = s;
                    bestSoFar = fitness;
                }
            }
            database.put(kv.getKey(), bestShapelet);*/


            //System.out.println("best shapelet " + bestShapelet + " with fitness " + bestSoFar);
            database.put(kv.getKey(), kv.getValue().get(random.nextInt(kv.getValue().size())));
        }
        return database;
    }

    private Instances buildInstances(List<Sequence> sequences, List<Integer> classIds, StaticFeatureMatrix staticFeatureMatrix, double[][] distanceFeatureMatrix, double[][] shapeletFeatureMatrix, String tempFilePath) throws Exception {
        PrintStream out = new PrintStream(new File(tempFilePath));
        int numTotalColsWithoutClass = staticFeatureMatrix.numCols() + distanceFeatureMatrix[0].length + shapeletFeatureMatrix[0].length;
        for (int col = 0; col <= numTotalColsWithoutClass; col++) {
            out.print("Col_" + col);
            if (col != numTotalColsWithoutClass) {
                out.print(",");
            } else {
                out.println();
            }
        }
        for (int row = 0; row < sequences.size(); row++) {
            out.print(classIds.get(row) + ",");
            for (int col = 0; col < staticFeatureMatrix.numCols(); col++) {
                double val = staticFeatureMatrix.get(row, col);
                out.print(val + ",");
            }
            for (int col = 0; col < shapeletFeatureMatrix[0].length; col++) {
                double val = shapeletFeatureMatrix[row][col];
                out.print(val + ",");
            }
            for (int col = 0; col < distanceFeatureMatrix[0].length; col++) {
                double val = distanceFeatureMatrix[row][col];
                out.print(val);
                if (col != distanceFeatureMatrix[0].length - 1) {
                    out.print(",");
                }
            }
            if (row != sequences.size() - 1) {
                out.println();
            }
        }
        out.close();
        CSVLoader loader = new CSVLoader();
        File tempFile = new File(tempFilePath);
        loader.setSource(tempFile);
        assert (tempFile.exists());
        Instances instances = loader.getDataSet();
        instances.setClassIndex(0);
        //new stuff I am trying:
        HashSet<Integer> intersection = new HashSet<>(classIdsset);
        intersection.removeAll(classIds);
        for (Integer i : intersection) {
            instances.classAttribute().addStringValue(i.toString());
        }
        String[] options2 = new String[2];
        options2[0] = "-R";
        options2[1] = "1";
        NumericToNominal convert = new NumericToNominal();
        convert.setOptions(options2);
        convert.setInputFormat(instances);
        instances = Filter.useFilter(instances, convert);
        return instances;
    }

    @Override
    public Integer classify(Sequence sequence) throws Exception {
        Sequence mySeq = new Sequence(sequence);
        mySeq.sortIntervals();
        double[] staticFeatures = onlineStaticFeatureExtraction(mySeq);
        double[] shapeletFeatures = onlineShapeletFeatureExtraction(mySeq);
        double[] distanceFeatures = onlineDistanceFeatureExtraction(mySeq);
        Instances instances = new Instances("test instances", allAttributes, 1);
        instances.setClassIndex(0);
        Instance instance = createInstance(staticFeatures, shapeletFeatures, distanceFeatures);
        instances.add(instance);
        instance.setDataset(instances);
        int predictedClass;
        try {
            int predictedClassIndex = (int) rf.classifyInstance(instance);
            int a = Integer.parseInt(instance.classAttribute().value(predictedClassIndex));
            predictedClass = Integer.parseInt(classAttribute.value(predictedClassIndex));
            assert (predictedClass == a);
        } catch (Exception e) {
            throw new ClassificationException(e);
        }
        return predictedClass;
    }

    private Instance createInstance(double[] staticFeatures, double[] shapeletFeatures, double[] distanceFeatures) {
        Instance curInstance = new Instance(allAttributes.size());
        //curInstance.setClassMissing();
        int instanceCol = 1;
        for (int i = 0; i < staticFeatures.length; i++) {
            double val = staticFeatures[i];
            curInstance.setValue((Attribute) allAttributes.elementAt(instanceCol), val);
            instanceCol++;
        }
        for (int i = 0; i < shapeletFeatures.length; i++) {
            double val = shapeletFeatures[i];
            curInstance.setValue((Attribute) allAttributes.elementAt(instanceCol), val);
            instanceCol++;
        }
        for (int i = 0; i < distanceFeatures.length; i++) {
            double val = distanceFeatures[i];
            curInstance.setValue((Attribute) allAttributes.elementAt(instanceCol), val);
            instanceCol++;
        }
        curInstance.setMissing(classAttribute);
        return curInstance;
    }

    public double[] onlineStaticFeatureExtraction(Sequence mySeq) {
        return staticMetricFeatureExtractor.extract(mySeq);
    }

    public double[] onlineDistanceFeatureExtraction(Sequence sequence) throws TimeScaleException, InvalidEventTableDimensionException {
        double[] distanceFeatures = new double[distanceFeatureMatrix.numCols()];
        Sequence resizedSequence = new Sequence(sequence);
        resizedSequence.rescaleTimeAxis(1, sequenceDuration);
        CompressedEventTable table = new CompressedEventTable(resizedSequence, numDimensions);
        for (int i = 0; i < distanceFeatureMatrix.numCols(); i++) {
            CompressedEventTable medoid = distanceFeatureMatrix.getCompressedEventTable(i);
            distanceFeatures[i] = medoid.euclidianDistance(table);
        }
        return distanceFeatures;
    }

    public double[] onlineShapeletFeatureExtraction(Sequence sequence) {
        double[] shapeletFeatures = new double[shapeletFeatureMatrix.numCols()];
        for (int i = 0; i < shapeletFeatureMatrix.numCols(); i++) {
            ShapeletKey key = new ShapeletKey(shapeletFeatureMatrix.getShapeletOfColumn(i));
            Shapelet_Size2 curShapelet = this.database.get(key);
            shapeletFeatures[i] = sequence.computeShapeletSimilarity(curShapelet, epsilon);
        }
        return shapeletFeatures;
    }

    public static String getName() {
        return "STIFE framework + Weka Random Forest";
    }

    //only for test purposes!!!!
    public ShapeletFeatureMatrix getShapeletFeatureMatrix() {
        return shapeletFeatureMatrix;
    }

}
