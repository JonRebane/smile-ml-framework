package stife.shapelet.evolution;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import data_structures.Sequence;
import experiment.classifier.ClassificationException;
import experiment.classifier.STIClassifier;
import stife.distance.exceptions.InvalidEventTableDimensionException;
import stife.distance.exceptions.TimeScaleException;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

public abstract class AbstractRF implements STIClassifier<Integer> {

	protected Instances buildInstancesFromFile(List<Integer> classIds, String tempFilePath) throws IOException, Exception {
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
	
	protected FastVector prepareRf(RandomForest rf, Instances trainInstances, FastVector allAttributes) throws Exception {
		Integer numFeaturesPerTree = new Integer((int) Math.sqrt(trainInstances.numAttributes()-1));
		rf.setOptions(new String[]{"-I","500","-K",numFeaturesPerTree.toString()});
		rf.buildClassifier(trainInstances);
		allAttributes = new FastVector();
		for(int col=0;col< trainInstances.numAttributes();col++){
			allAttributes.addElement(trainInstances.attribute(col));
		}
		return allAttributes;
	}
	
	protected Instance createInstance(double[] features, FastVector allAttributes, Attribute classAttribute) {
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

	protected Instance prepareInstance(double[] features, FastVector allAttributes, Attribute classAttribute) {
		Instances instances = new Instances("test instances",allAttributes,1);
		instances.setClassIndex(0);
		Instance instance = createInstance(features, allAttributes, classAttribute);
		instances.add(instance);
		instance.setDataset(instances);
		return instance;
	}

}
