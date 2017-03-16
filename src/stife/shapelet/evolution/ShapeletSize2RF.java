package stife.shapelet.evolution;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import data_structures.Sequence;
import experiment.classifier.ClassificationException;
import experiment.classifier.STIClassifier;
import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;
import stife.shapelet_size2.ShapeletExtractor;
import stife.shapelet_size2.ShapeletFeatureMatrix;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class ShapeletSize2RF extends AbstractRF implements STIClassifier<Integer> {

	private int epsilon;
	private RandomForest rf;
	private FastVector allAttributes;
	private Instances trainInstances;
	private Attribute classAttribute;
	ShapeletFeatureMatrix matrix;


	public ShapeletSize2RF(List<Sequence> train,List<Integer> classIds, int numDimensions,int epsilon,int numFeatures) throws Exception {
		ExhaustiveShapeletSize2FeatureExtractor extractor = new ExhaustiveShapeletSize2FeatureExtractor();
		matrix = extractor.extract(train,classIds,numDimensions,epsilon,numFeatures);
		short[][] features = matrix.getMatrix();
		this.epsilon = epsilon;
		rf = new RandomForest();
		trainInstances = buildInstances(train, classIds, features, "testdata" + File.separator + "stifeTrainData.csv");
		allAttributes = prepareRf(rf,trainInstances,allAttributes);
		classAttribute = trainInstances.classAttribute();
	}
	
	private Instances buildInstances(List<Sequence> sequences, List<Integer> classIds, short[][] features, String tempFilePath) throws Exception {
		PrintStream out = new PrintStream(new File(tempFilePath));
		for(int col = 0;col<=features[0].length;col++){
			out.print("Col_"+col);
			if(col!=features[0].length){
				out.print(",");
			} else{
				out.println();
			}
		}
		for(int row = 0;row<features.length;row++){
			out.print(classIds.get(row)+",");
			for(int col = 0;col<features[0].length;col++){
				double val = features[row][col];
				out.print(val);
				if(col!= features[0].length-1){
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
	
	
	@Override
	public Integer classify(Sequence sequence) throws TimeScaleException, InvalidEventTableDimensionException,
			ClassificationException, IOException, Exception {
		Sequence mySeq = new Sequence(sequence);
		mySeq.sortIntervals();
		int numCols = matrix.getMatrix()[0].length;
		double[] features = new double[numCols];
		for(int col = 0;col<numCols;col++){
			double value = sequence.countShapeletOccurance(matrix.getShapeletOfColumn(col), epsilon);
			features[col] = value;
		}
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

}
