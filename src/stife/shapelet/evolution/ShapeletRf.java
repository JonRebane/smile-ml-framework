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

public class ShapeletRf implements STIClassifier<Integer> {
	
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
		Integer numFeaturesPerTree = new Integer((int) Math.sqrt(trainInstances.numAttributes()-1));
		rf.setOptions(new String[]{"-I","500","-K",numFeaturesPerTree.toString()});
		rf.buildClassifier(trainInstances);
		allAttributes = new FastVector();
		for(int col=0;col< trainInstances.numAttributes();col++){
			allAttributes.addElement(trainInstances.attribute(col));
		}
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
				boolean value = curSequence.containsNSHapelet(nShapelets.get(col), epsilon);
				double val = value ? 1.0 : 0.0;
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
		CSVLoader loader = new CSVLoader();
		File tempFile = new File(tempFilePath);
		loader.setSource(tempFile);
		assert(tempFile.exists());
		Instances instances = loader.getDataSet();
		instances.setClassIndex(0);
		//new stuff I am trying:
		HashSet<Integer> intersection = new HashSet<>(classIds);
		intersection.removeAll(classIds);
		for(Integer i : intersection){
			instances.classAttribute().addStringValue(i.toString());
		}
        String[] options2 = new String[2];
		options2[0]="-R";
        options2[1]="1";
		NumericToNominal convert= new NumericToNominal();
		convert.setOptions(options2);			
		convert.setInputFormat(instances);
	    instances = Filter.useFilter(instances, convert);
	    return instances;
	}
	
	@Override
	public Integer classify(Sequence sequence) throws TimeScaleException, InvalidEventTableDimensionException,
			ClassificationException, IOException, Exception {
		Sequence mySeq = new Sequence(sequence);
		mySeq.sortIntervals();
		double[] features = new double[nShapelets.size()];
		for(int col = 0;col<nShapelets.size();col++){
			boolean value = sequence.containsNSHapelet(nShapelets.get(col), epsilon);
			features[col] = value ? 1.0 : 0.0;
		}
		Instances instances = new Instances("test instances",allAttributes,1);
		instances.setClassIndex(0);
		Instance instance = createInstance(features);
		instances.add(instance);
		instance.setDataset(instances);
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
	
	private Instance createInstance(double[] features) {
		Instance curInstance = new Instance(allAttributes.size());
		//curInstance.setClassMissing();
		int instanceCol = 1;
		for(int i = 0;i<features.length;i++){
			double val = features[i];
			curInstance.setValue((Attribute)allAttributes.elementAt(instanceCol), val);
			instanceCol++;
		}
		curInstance.setMissing(classAttribute);
		return curInstance;
	}

}
