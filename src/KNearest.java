import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class KNearest{
	public static String FileNameTest = "testing.txt";
	public static String FileNameTraining = "training.txt";
	public static int k = 1;
	public static HashMap<Integer,ArrayList<Double>> datasetTest = new HashMap<Integer,ArrayList<Double>>();
	public static HashMap<Integer,ArrayList<Double>> datasetTraining = new HashMap<Integer,ArrayList<Double>>();
	public static int numberOfRows;
	public static int numberOfColumns;

	public static void readData(){
		try {
			BufferedReader in = new BufferedReader(new FileReader(new File(FileNameTest)));
			String line;
			int counter = 1;
			while((line = in.readLine())!=null){
				ArrayList<Double> vals = new ArrayList<Double>();
				String[] tokens = line.split("\t");
				double val1;
				numberOfColumns = tokens.length-1;
				for(int i = 0;i<tokens.length;i++){
					val1 = Double.parseDouble(tokens[i]);
					vals.add(val1);
				}
				datasetTest.put(counter, vals);
				counter++;
			}
			in.close();
			numberOfRows = counter-1;
			counter = 1;
			BufferedReader in1 = new BufferedReader(new FileReader(new File(FileNameTraining)));
			while((line = in1.readLine())!=null){
				ArrayList<Double> vals = new ArrayList<Double>();
				String[] tokens = line.split("\t");
				double val1;
				for(int i = 0;i<tokens.length;i++){
					val1 = Double.parseDouble(tokens[i]);
					vals.add(val1);
				}
				datasetTraining.put(counter, vals);
				counter++;
			}
			in1.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void Normalize(HashMap<Integer, ArrayList<Double>> dataset){
		double[] min = new double[numberOfColumns];
		double[] max = new double[numberOfColumns];
		for(int i = 0;i<numberOfColumns;i++){
			min[i] = Double.MAX_VALUE;
			max[i] = Double.MIN_VALUE;
		}
		for(int i:dataset.keySet()){
			ArrayList<Double> list = dataset.get(i);
			for(int j = 0;j<numberOfColumns;j++){
				if((Double)list.get(j)<min[j]){
					min[j] = (Double)list.get(j);
				}
				if((Double)list.get(j)>max[j]){
					max[j] = (Double)list.get(j);
				}
			}
		}
		for(int i:dataset.keySet()){
			ArrayList<Double> list = dataset.get(i);
			ArrayList<Double> newList = new ArrayList<Double>();
			for(int j = 0;j<numberOfColumns;j++){
				double temp = ((Double)list.get(j)-min[j])/(max[j]-min[j]);
				newList.add(temp);
			}
			newList.add(list.get(list.size()-1));
			dataset.put(i, newList);
		}
	}

	public static HashMap<Integer,Integer> findDistance(){
		HashMap<Integer,Integer> classLabel = new HashMap<Integer,Integer>();
		for(int i : datasetTest.keySet()){
			//System.out.println("Point "+i+":");
			ArrayList<Double> testList = datasetTest.get(i);
			HashMap<Integer,Double> distances = new HashMap<Integer,Double>();
			for(int j : datasetTraining.keySet()){
					ArrayList<Double> trainingList = datasetTraining.get(j);
					double dist = 0;
					for(int k = 0;k<numberOfColumns;k++){
						dist+=Math.pow(((Double)testList.get(k)-(Double)trainingList.get(k)),2);
					}
					dist = Math.sqrt(dist);
					distances.put(j, dist);
			}
//			for(int x:distances.keySet()){
//				System.out.println("Point "+x+" : "+distances.get(x));
//			}
			LinkedHashMap<Integer,Double> sortedMap = sortByComparator(distances);
			int counter = 0;
//			for(int x:sortedMap.keySet()){
//				System.out.println("Point "+x+" : "+sortedMap.get(x));
//			}
			Iterator<Integer> it = sortedMap.keySet().iterator();
			double one = 0, zero = 0;
			while(counter<k){
				int id = it.next();
				//System.out.println("This Point "+id+" : "+distances.get(id));
				if((Double)datasetTraining.get(id).get(numberOfColumns)==1.0){
					one+=Math.pow((1/distances.get(id)),2);
				}
				else if((Double)datasetTraining.get(id).get(numberOfColumns)==0.0){
					zero+=Math.pow((1/distances.get(id)),2);
				}
				else
					System.out.println("Unknown Class");
				counter++;
			}
			if(one>zero)
				classLabel.put(i,1);
			else
				classLabel.put(i,0);
			
		}
		return classLabel;
	}

	public static void main(String[] args){
		HashMap<Integer,Integer> classLabel = new HashMap<Integer,Integer>();
		double accuracy = 0;
		double precision = 0;
		double recall = 0;
		double fmeasure = 0;
		readData();
		Normalize(datasetTest);
		Normalize(datasetTraining);
//		System.out.println("Testing:");
//		for(int x:datasetTest.keySet()){
//			System.out.println("Point "+x+" : "+datasetTest.get(x));
//		}
//		System.out.println("Training:");
//		for(int x:datasetTraining.keySet()){
//			System.out.println("Point "+x+" : "+datasetTraining.get(x));
//		}
		classLabel = findDistance();
		int a = 0;
		int b = 0;
		int c = 0;
		int d = 0;
		for(int j : classLabel.keySet()){
			if(classLabel.get(j)==1 && (Double)datasetTest.get(j).get(numberOfColumns)==1){
				//System.out.println("Correct");
				a++;
			}
			else if(classLabel.get(j)==0 && (Double)datasetTest.get(j).get(numberOfColumns)==1){
				//System.out.println("Incorrect");
				b++;
			}
			else if(classLabel.get(j)==1 && (Double)datasetTest.get(j).get(numberOfColumns)==0){
				//System.out.println("Incorrect");
				c++;
			}
			else if(classLabel.get(j)==0 && (Double)datasetTest.get(j).get(numberOfColumns)==0){
				d++;
				//System.out.println("Correct");
			}
		}
		accuracy = ((a+d)/(double)(a+b+c+d));
		precision = ((a)/(double)(a+c));
		recall = ((a)/(double)(a+b));
		fmeasure = ((2*a)/(double)(2*a+b+c));
		//System.out.println("a:"+accuracy[i]+" b: "+precision[i]+" c: "+recall[i]+" d: "+fmeasure[i])
		System.out.println("Accuracy: "+accuracy);
		System.out.println("Precision: "+precision);
		System.out.println("Recall: "+recall);
		System.out.println("fStat: "+fmeasure);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static LinkedHashMap<Integer,Double> sortByComparator(HashMap<Integer,Double> unsortMap) {

		List<Map.Entry<Integer, Double>> list = new LinkedList<Map.Entry<Integer, Double>>(unsortMap.entrySet());

		// sort list based on comparator
		Collections.sort(list, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry<Integer,Double>) (o1)).getValue())
						.compareTo(((Map.Entry<Integer,Double>) (o2)).getValue());
			}
		});

		LinkedHashMap sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

}