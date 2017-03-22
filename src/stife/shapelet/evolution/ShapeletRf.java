package stife.shapelet.evolution;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;

import data_structures.Sequence;
import experiment.classifier.ClassificationException;
import experiment.classifier.STIClassifier;
import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;
import stife.static_metrics.StaticFeatureMatrix;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

public class ShapeletRf extends AbstractRF implements STIClassifier<Integer> {
	
	private RandomForest rf;
	private FastVector allAttributes;
	private Attribute classAttribute;
	private Instances trainInstances;
	private int epsilon;
	private List<NShapelet> nShapelets;

	public ShapeletRf(List<NShapelet> nShapelets,List<Sequence> train,List<Integer> classIds,int epsilon) throws Exception{
		this.nShapelets = nShapelets;
		this.epsilon = epsilon;
		rf = new RandomForest();
		trainInstances = buildInstances(train, classIds, nShapelets, "testdata" + File.separator + "stifeTrainData.csv");
		allAttributes = prepareRf(rf,trainInstances,allAttributes);
		classAttribute = trainInstances.classAttribute();
	}
	
	private Instances buildInstances(List<Sequence> sequences, List<Integer> classIds, List<NShapelet> nShapelets, String tempFilePath) throws Exception {
		PrintStream out = new PrintStream(new File(tempFilePath));
		for(int col = 0;col<=nShapelets.size();col++){
			out.print("Col_"+col);
			if(col!=nShapelets.size()){
				out.print(",");
			} else{
				out.println();
			}
		}
		for(int row = 0;row<sequences.size();row++){
			Sequence curSequence = sequences.get(row);
			out.print(classIds.get(row)+",");
			for(int col = 0;col<nShapelets.size();col++){
				double val = curSequence.getAllOccurrences(nShapelets.get(col), epsilon).size();
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
	
	@Override
	public Integer classify(Sequence sequence) throws TimeScaleException, InvalidEventTableDimensionException,
			ClassificationException, IOException, Exception {
		Sequence mySeq = new Sequence(sequence);
		mySeq.sortIntervals();
		double[] features = new double[nShapelets.size()];
		for(int col = 0;col<nShapelets.size();col++){
			double val = sequence.getAllOccurrences(nShapelets.get(col), epsilon).size();
			features[col] = val;
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
