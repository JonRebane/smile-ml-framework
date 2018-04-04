package experiment.classifier;

import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import data_structures.CompressedEventTable;
import data_structures.Sequence;
import data_structures.ShapeletKey;
import data_structures.eseq.*;
import stife.distance.DistanceFeatureExtractor;
import stife.distance.DistanceFeatureMatrix;
import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;
import stife.shapelet_size2.Shapelet_Size2;
import stife.shapelet_size2.ShapeletExtractor;
import stife.shapelet_size2.ShapeletFeatureMatrix;
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
    private List<Elet> eletFeatures;
    private double[][] eletFeatureMatrix;
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

        int noElets = 100;
        eletFeatures = new ArrayList<>();
        eletFeatureMatrix = extractEletFeatureMatrix(train, noElets);

        this.database = null;

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
        trainInstances = buildInstances(train, classIds, staticFeatureMatrix, distanceFeatureMatrix.getMatrix(), shapeletFeatureMatrix.getMatrix(), eletFeatureMatrix,"testdata" + File.separator + "stifeTrainData.csv");
        rf = new RandomForest();
        Integer numFeaturesPerTree = (int) Math.sqrt(trainInstances.numAttributes() - 1);
        rf.setOptions(new String[]{"-I", "500", "-K", numFeaturesPerTree.toString(), "-S", "123"});
        rf.buildClassifier(trainInstances);
        allAttributes = new FastVector();
        for (int col = 0; col < trainInstances.numAttributes(); col++) {
            allAttributes.addElement(trainInstances.attribute(col));
        }
        classAttribute = trainInstances.classAttribute();
    }

    private double[][] extractEletFeatureMatrix(List<Sequence> train, int noElets) {
        double[][] distances = new double[train.size()][noElets];
        List<List<DefaultHashMap<String, Short>>> allExamples = new ArrayList<>();
        int maxSize = 100;
        int check = 0;
        for (Sequence sequence : train) {
            check = check + 1;
            List<DefaultHashMap<String, Short>> temp = convertToVectors(sequence);
            allExamples.add(temp);
            if (temp.size() < maxSize)
                maxSize = temp.size();
        }

        for (int i = 0; i < noElets; i++) {
            int index = this.random.nextInt(train.size());
            List<DefaultHashMap<String, Short>> vecSeq = allExamples.get(index);

            int query_length = this.random.nextInt(maxSize - 1) + 1;
            int start = this.random.nextInt(vecSeq.size() - query_length);

            List<DefaultHashMap<String, Short>> query = vecSeq.subList(start, start + query_length);
            int query_active_time_points = 0;
            for (int j = 0; j < query_length; j++) {
                query_active_time_points += query.get(j).size();
            }

            DefaultHashMap<String, Integer> queryAlphabetMap = new DefaultHashMap<String, Integer>(0);
            for (DefaultHashMap<String, Short> vector : query) {
                for (Map.Entry<String, Short> entry : vector.entrySet()) {
                    String key = entry.getKey();
                    queryAlphabetMap.put(key, 1);
                }
            }
            String[] queryAlphabet = queryAlphabetMap.keySet().toArray(new String[queryAlphabetMap.keySet().size()]);
            HashMap<String, Double> queryStatistics = Estreams_Euclidean4.computeStreamStats(query);
            int[] density = Estreams_Euclidean4.computeTiDEOrder(query);
            int queryActivePoints = Estreams_Euclidean4.computeNumberActiveTimePoints(query);
            Elet elet = new Elet(query, query_active_time_points, queryAlphabetMap, queryAlphabet, queryStatistics, density, queryActivePoints);
            eletFeatures.add(elet);
        }

        for (int i = 0; i < train.size(); i++) {
            List<DefaultHashMap<String, Short>> a = allExamples.get(i);
            int finalI = i;
            IntStream.range(0, noElets).parallel().forEach(j -> {
                Elet b = eletFeatures.get(j);
                double dist = Estreams_Euclidean4.euclideannew(a, b.query, b.queryAlphabet, b.density, b.queryStatistics,
                        true, false, false,
                        true, true, b.queryActivePoints, 10);
                distances[finalI][j] = dist;

            });
        }
        return distances;
    }

    private List<DefaultHashMap<String, Short>> convertToVectors(Sequence sequence) {
        ArrayList<DefaultHashMap<String, Short>> vec_seq = Estreams_Euclidean4.transform_to_vectors(sequence);
        List<DefaultHashMap<String, Short>> temp = new ArrayList<>();
        for (DefaultHashMap<String, Short> curr_vec : vec_seq) {
            if (curr_vec.size() > 0) //remove empty time points
                temp.add(curr_vec);
        }
        return temp;
    }

    private Instances buildInstances(List<Sequence> sequences, List<Integer> classIds, StaticFeatureMatrix staticFeatureMatrix, double[][] distanceFeatureMatrix, double[][] shapeletFeatureMatrix, double[][] eletFeatureMatrix, String tempFilePath) throws Exception {
        PrintStream out = new PrintStream(new File(tempFilePath));
        int numTotalColsWithoutClass = staticFeatureMatrix.numCols() + distanceFeatureMatrix[0].length + shapeletFeatureMatrix[0].length + eletFeatureMatrix[0].length;
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
                out.print(val + ",");
            }
            for (int col = 0; col < eletFeatureMatrix[0].length; col++) {
                double val = eletFeatureMatrix[row][col];
                out.print(val);
                if (col != eletFeatureMatrix[0].length - 1) {
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
        double[] eletFeatures = onlineEletFeatures(mySeq);
        Instances instances = new Instances("test instances", allAttributes, 1);
        instances.setClassIndex(0);
        Instance instance = createInstance(staticFeatures, shapeletFeatures, distanceFeatures, eletFeatures);
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

    private double[] onlineEletFeatures(Sequence sequence) {
        double[] values = new double[eletFeatures.size()];
        List<DefaultHashMap<String, Short>> a = convertToVectors(sequence);

        IntStream.range(0, eletFeatures.size()).parallel().forEach(i -> {
            Elet b = eletFeatures.get(i);
            double dist = Estreams_Euclidean4.euclideannew(a, b.query, b.queryAlphabet, b.density, b.queryStatistics,
                    true, false, false,
                    true, true, b.queryActivePoints, 10);
            values[i] = dist;
        });
        return values;
    }

    private Instance createInstance(double[] staticFeatures, double[] shapeletFeatures, double[] distanceFeatures, double[] eletFeatures) {
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
        for (int i = 0; i < eletFeatures.length; i++) {
            double val = eletFeatures[i];
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
            Shapelet_Size2 curShapelet = shapeletFeatureMatrix.getShapeletOfColumn(i);
            if (this.database != null) {
                curShapelet = this.database.get(key);
            }
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
