package stife.shapelet.evolution;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import data_structures.CompressedEventTable;
import data_structures.Sequence;
import experiment.classifier.ClassificationException;
import experiment.classifier.STIClassifier;
import stife.distance.DistanceFeatureExtractor;
import stife.distance.DistanceFeatureMatrix;
import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;
import stife.shapelet_size2.Shapelet_Size2;
import stife.shapelet_size2_new.ShapeletSize2;
import stife.static_metrics.StaticFeatureMatrix;
import stife.static_metrics.StaticMetricExtractor;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class STIFE_2SHAPELET_RFSingleLabelClassifier extends AbstractRF implements STIClassifier<Integer> {

    private RandomForest rf;
    private FastVector allAttributes;
    private Attribute classAttribute;
    private Instances trainInstances;
    private int epsilon;
    private List<Shapelet_Size2> nShapelets;
    private DistanceFeatureMatrix distanceFeatureMatrix;
    private int sequenceDuration;
    private int numDimensions;
    private StaticMetricExtractor staticMetricFeatureExtractor;

    public STIFE_2SHAPELET_RFSingleLabelClassifier(List<Shapelet_Size2> nShapelets, List<Sequence> train, List<Integer> classIds, int numDimensions, int sequenceDuration, int epsilon) throws Exception{
        this.nShapelets = nShapelets;
        this.numDimensions = numDimensions;
        this.sequenceDuration = sequenceDuration;
        this.epsilon = epsilon;
        rf = new RandomForest();

        System.out.println(nShapelets);
        staticMetricFeatureExtractor = new StaticMetricExtractor();
        StaticFeatureMatrix staticFeatureMatrix = staticMetricFeatureExtractor.extractAll(train);
        //distance:
        DistanceFeatureExtractor distanceFeatureExtractor = new DistanceFeatureExtractor(train, classIds, numDimensions,sequenceDuration);
        distanceFeatureMatrix = distanceFeatureExtractor.calculateDistanceFeatureMatrix();
        trainInstances = buildInstances(train, classIds, nShapelets,staticFeatureMatrix,distanceFeatureMatrix.getMatrix(), "testdata" + File.separator + "stifeTrainData.csv");
        allAttributes = prepareRf(rf,trainInstances,allAttributes);
        classAttribute = trainInstances.classAttribute();
    }

    private Instances buildInstances(List<Sequence> sequences, List<Integer> classIds, List<Shapelet_Size2> nShapelets, StaticFeatureMatrix staticFeatureMatrix, double[][] distanceFeatureMatrix, String tempFilePath) throws Exception {
        PrintStream out = new PrintStream(new File(tempFilePath));
        int numFeatures = nShapelets.size() + staticFeatureMatrix.numCols() + distanceFeatureMatrix[0].length;
        for(int col = 0;col<=numFeatures;col++){
            out.print("Col_"+col);
            if(col!=numFeatures){
                out.print(",");
            } else{
                out.println();
            }
        }
        for(int row = 0;row<sequences.size();row++){
            Sequence curSequence = sequences.get(row);
            out.print(classIds.get(row)+",");
            for(int col = 0;col<staticFeatureMatrix.numCols();col++){
                double val = staticFeatureMatrix.get(row, col);
                out.print(val+",");
            }
            for(int col = 0;col<distanceFeatureMatrix[0].length;col++){
                double val = distanceFeatureMatrix[row][col];
                out.print(val+",");
            }

            double[] shapeletFeatures = new double[nShapelets.size()];
            IntStream.range(0, nShapelets.size()).parallel().forEach(i -> {
                shapeletFeatures[i] = curSequence.computeShapeletSimilarity(nShapelets.get(i), epsilon);
            });

            for(int col = 0;col<shapeletFeatures.length;col++){
                double val = shapeletFeatures[col];
                //double val = curSequence.computeShapeletSimilarity(nShapelets.get(col), epsilon);
                out.print(val);
                if(col!= nShapelets.size()-1){
                    out.print(",");
                }
            }
            if(row!=sequences.size()-1){
                out.println();
            }
        }
        out.close();
        Instances instances = buildInstancesFromFile(classIds, tempFilePath);
        return instances;
    }

    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public Integer classify(Sequence sequence) throws TimeScaleException, InvalidEventTableDimensionException,
            ClassificationException, IOException, Exception {
        Sequence mySeq = new Sequence(sequence);
        mySeq.sortIntervals();
        double[] staticFeatures = onlineStaticFeatureExtraction(mySeq);
        double[] distanceFeatures = onlineDistanceFeatureExtraction(mySeq);
        double[] shapeletFeatures = new double[nShapelets.size()];

        /*List<Future<Double>> distances = new ArrayList<>();
        for (int col = 0; col < nShapelets.size(); col++) {
            final int finalCol = col;
            Callable<Double> callable = () -> sequence.computeShapeletSimilarity(nShapelets.get(finalCol), epsilon);
            distances.add(executor.submit(callable));
        }

        for(int col = 0;col<nShapelets.size();col++){
            shapeletFeatures[col] = distances.get(col).get();
        }*/

        IntStream.range(0, nShapelets.size()).parallel().forEach(i -> {
            shapeletFeatures[i] = sequence.computeShapeletSimilarity(nShapelets.get(i), epsilon);
        });

        double[] features = buildAllFeatures(staticFeatures,distanceFeatures,shapeletFeatures);
        Instance instance = prepareInstance(features, allAttributes, classAttribute);
        int predictedClass;
        try {
            int predictedClassIndex = (int) rf.classifyInstance(instance);
            int a = Integer.parseInt(instance.classAttribute().value(predictedClassIndex));
            predictedClass = Integer.parseInt(classAttribute.value(predictedClassIndex));
            assert(predictedClass==a);
        } catch (Exception e) {
            throw new ClassificationException(e);
        }
        return predictedClass;
    }

    private double[] buildAllFeatures(double[] staticFeatures, double[] distanceFeatures,double[] shapeletFeatures) {
        //curInstance.setClassMissing();
        double[] allFeautres = new double[staticFeatures.length+distanceFeatures.length+shapeletFeatures.length];
        int instanceCol = 0;
        for(int i = 0;i<staticFeatures.length;i++){
            allFeautres[instanceCol] = staticFeatures[i];
            instanceCol++;
        }
        for(int i = 0;i<distanceFeatures.length;i++){
            allFeautres[instanceCol] = distanceFeatures[i];
            instanceCol++;
        }
        for(int i = 0;i<shapeletFeatures.length;i++){
            allFeautres[instanceCol] = shapeletFeatures[i];
            instanceCol++;
        }
        return allFeautres;
    }

    public double[] onlineStaticFeatureExtraction(Sequence mySeq) {
        return staticMetricFeatureExtractor.extract(mySeq);
    }

    public double[] onlineDistanceFeatureExtraction(Sequence sequence) throws TimeScaleException, InvalidEventTableDimensionException {
        double[] distanceFeatures = new double[distanceFeatureMatrix.numCols()];
        Sequence resizedSequence = new Sequence(sequence);
        resizedSequence.rescaleTimeAxis(1, sequenceDuration);
        CompressedEventTable table = new CompressedEventTable(resizedSequence, numDimensions);
        for(int i=0;i<distanceFeatureMatrix.numCols();i++){
            CompressedEventTable medoid = distanceFeatureMatrix.getCompressedEventTable(i);
            distanceFeatures[i] = medoid.euclidianDistance(table);
        }
        return distanceFeatures;
    }

    public static String getName() {
        return "STIFE++ framework + Weka Random Forest";
    }
}
