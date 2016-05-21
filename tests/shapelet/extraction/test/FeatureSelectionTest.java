package shapelet.extraction.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import shapelet.extraction.FeatureSelection;
import shapelet.extraction.IndexGainPair;

//reference implementation in R was used as a test oracle to calculate the true values
public class FeatureSelectionTest {

	private double delta = 0.0000001;
	
	@Test
	public void testOrdering(){
		assertEquals(Arrays.asList(2,0,1,4,3), Arrays.asList(FeatureSelection.getColumnSortedIndices(new short[]{20,30,1,54,43})));
	}
	
	@Test
	public void testIndexGainPairOrdering(){
		IndexGainPair a = new IndexGainPair(10, 0.5);
		IndexGainPair b = new IndexGainPair(10, 0.5);
		IndexGainPair c = new IndexGainPair(100, 0.2);
		IndexGainPair d = new IndexGainPair(15, 0.8);
		assertTrue(a.compareTo(b)==0);
		assertTrue(a.compareTo(c)>0);
		assertTrue(a.compareTo(d)<0);
		assertTrue(b.compareTo(c)>0);
		assertTrue(b.compareTo(d)<0);
	}
	
	@Test
	public void testEntropy() {
		assertEquals(0.0,FeatureSelection.calcEntropy(Arrays.asList(0,0,0,0)),delta);
		assertEquals(1.0,FeatureSelection.calcEntropy(Arrays.asList(0,0,1,1)),delta);
		assertEquals(0.8112781, FeatureSelection.calcEntropy(Arrays.asList(0,0,0,1)),delta);
		assertEquals(0.5032583, FeatureSelection.calcEntropy(Arrays.asList(0,1,1,1,1,1,1,1,1)),delta);
		assertEquals(2.0, FeatureSelection.calcEntropy(Arrays.asList(0,1,2,3)),delta);
		assertEquals(2.0, FeatureSelection.calcEntropy(Arrays.asList(0,0,1,1,2,2,3,3)),delta);
		assertEquals(1.950212, FeatureSelection.calcEntropy(Arrays.asList(0,0,1,1,2,2,3)),delta);
	}
	
	@Test
	public void testInfoAfterSplit(){
		assertEquals(0.0, FeatureSelection.infoAfterSplit(new boolean[]{true,false}, Arrays.asList(1,2)),delta);
		assertEquals(0.0, FeatureSelection.infoAfterSplit(new boolean[]{true,true,false,false}, Arrays.asList(1,1,2,2)),delta);
		assertEquals(1.0, FeatureSelection.infoAfterSplit(new boolean[]{true,false,true,false}, Arrays.asList(1,1,2,2)),delta);
		assertEquals(0.6887219, FeatureSelection.infoAfterSplit(new boolean[]{true,true,true,false}, Arrays.asList(1,1,2,2)),delta);
		assertEquals(0.5, FeatureSelection.infoAfterSplit(new boolean[]{true,true,false,false}, Arrays.asList(1,1,1,2)),delta);
		assertEquals(1.0, FeatureSelection.infoAfterSplit(new boolean[]{true,true,false,false}, Arrays.asList(1,2,3,4)),delta);
		assertEquals(0.9509775, FeatureSelection.infoAfterSplit(new boolean[]{true,true,false,false,false}, Arrays.asList(1,2,3,3,4)),delta);
	}
	
	@Test
	public void testInformationGain(){
		assertEquals(1.0, FeatureSelection.calcInfoGain(new short[]{0,1,2,3}, Arrays.asList(1,2,3,4)),delta);
		assertEquals(0.9709506, FeatureSelection.calcInfoGain(new short[]{0,1,2,3,3}, Arrays.asList(1,2,3,4,4)),delta);
		assertEquals(0.7219281, FeatureSelection.calcInfoGain(new short[]{0,1,1,1,4}, Arrays.asList(1,2,3,4,4)),delta);
		assertEquals(0.5709506, FeatureSelection.calcInfoGain(new short[]{1,2,2,2,1}, Arrays.asList(1,2,2,4,4)),delta);
		assertEquals(0.3112781, FeatureSelection.calcInfoGain(new short[]{1,2,3,4}, Arrays.asList(1,2,2,1)),delta);
		assertEquals(0.0, FeatureSelection.calcInfoGain(new short[]{1,1,1,1}, Arrays.asList(1,2,2,1)),delta);
		assertEquals(0.0, FeatureSelection.calcInfoGain(new short[]{1,2,1,2}, Arrays.asList(1,1,2,2)),delta);
	}

}
