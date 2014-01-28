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

public class KNN{
	public static String FileName = "project3_dataset2.txt";
	public static int k = 10;
	public static HashMap<Integer,ArrayList<Object>> dataset = new HashMap<Integer,ArrayList<Object>>();
	public static ArrayList<Boolean> type = new ArrayList<Boolean>();
	public static int numberOfRows;
	public static int numberOfColumns;
	
	public static void readData(){
		try {
			BufferedReader in = new BufferedReader(new FileReader(new File(FileName)));
			String line;
			int counter = 1;
			int set = 0;
			while((line = in.readLine())!=null){
				ArrayList<Object> vals = new ArrayList<Object>();
				String[] tokens = line.split("\t");
				double val1;
				numberOfColumns = tokens.length-1;
				for(int i = 0;i<tokens.length;i++){
					try{
						val1 = Double.parseDouble(tokens[i]);
						if(set==0)
							type.add(false);
						vals.add(val1);
					}
					catch(Exception e){
						if(set==0)
							type.add(true);
						vals.add(tokens[i]);
					}
				}
				dataset.put(counter, vals);
				counter++;
				set = 1;
			}
			in.close();
			numberOfRows = counter-1;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void Normalize(){
		double[] min = new double[numberOfColumns];
		double[] max = new double[numberOfColumns];
		for(int i = 0;i<numberOfColumns;i++){
			min[i] = Double.MAX_VALUE;
			max[i] = Double.MIN_VALUE;
		}
		for(int i:dataset.keySet()){
			ArrayList<Object> list = dataset.get(i);
			for(int j = 0;j<numberOfColumns;j++){
				if(!type.get(j)){
					if((Double)list.get(j)<min[j]){
						min[j] = (Double)list.get(j);
					}
					if((Double)list.get(j)>max[j]){
						max[j] = (Double)list.get(j);
					}
				}
			}
		}
		for(int i:dataset.keySet()){
			ArrayList<Object> list = dataset.get(i);
			ArrayList<Object> newList = new ArrayList<Object>();
			for(int j = 0;j<numberOfColumns;j++){
				if(!type.get(j)){
					double temp = ((Double)list.get(j)-min[j])/(max[j]-min[j]);
					newList.add(temp);
				}
				else
					newList.add(list.get(j));
			}
			newList.add(list.get(list.size()-1));
			dataset.put(i, newList);
		}
	}
	
	public static int[] partition(int i){
		int partitionSize = numberOfRows/10;
		int[] testingSet = new int[2];
		if(i==10){
			int remaining = numberOfRows-(9*partitionSize);
			testingSet[0] = (9*partitionSize)+1;
			testingSet[1] = remaining;
		}
		else{
			testingSet[0] = ((i-1)*partitionSize)+1;
			testingSet[1] = partitionSize;
		}
		return testingSet;
	}
	
	public static HashMap<Integer,Integer> findDistance(ArrayList<Integer> test){
		HashMap<Integer,Integer> classLabel = new HashMap<Integer,Integer>();
		if(test==null){
			System.out.println("No testing set generated.");
		}
		for(int i : test){
			//System.out.println("Point "+i+":");
			ArrayList<Object> testList = dataset.get(i);
			HashMap<Integer,Double> distances = new HashMap<Integer,Double>();
			for(int j : dataset.keySet()){
				if(!test.contains(j)){
					ArrayList<Object> trainingList = dataset.get(j);
					double dist = 0;
					for(int k = 0;k<numberOfColumns;k++){
						if(!type.get(k))
							dist+=Math.pow(((Double)testList.get(k)-(Double)trainingList.get(k)),2);
						else{
							if(!testList.get(k).equals(trainingList.get(k)))
								dist+=1;
						}
					}
					dist = Math.sqrt(dist);
					distances.put(j, dist);
				}
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
				if((Double)dataset.get(id).get(numberOfColumns)==1.0){
					one++;//=Math.pow((1/distances.get(id)),2);
				}
				else if((Double)dataset.get(id).get(numberOfColumns)==0.0){
					zero++;//=Math.pow((1/distances.get(id)),2);
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
		double[] accuracy = new double[10];
		double[] precision = new double[10];
		double[] recall = new double[10];
		double[] fmeasure = new double[10];
		double[] avgStats = new double[4];
		readData();
		Normalize();
		int [] testInfo; 
		for(int i = 0;i<10;i++){
			testInfo = partition(i+1);
			ArrayList<Integer> testerSet = new ArrayList<Integer>();
			for(int j = testInfo[0];j<testInfo[0]+testInfo[1];j++){
				testerSet.add(j);
			}
			classLabel = findDistance(testerSet);
			int[] a = new int[10];
			int[] b = new int[10];
			int[] c = new int[10];
			int[] d = new int[10];
			for(int j : classLabel.keySet()){
				if(classLabel.get(j)==1 && (Double)dataset.get(j).get(numberOfColumns)==1)
					//System.out.println("Correct");
					a[i]++;
				else if(classLabel.get(j)==0 && (Double)dataset.get(j).get(numberOfColumns)==1)
					//System.out.println("Incorrect");
					b[i]++;
				else if(classLabel.get(j)==1 && (Double)dataset.get(j).get(numberOfColumns)==0)
					c[i]++;
				else if(classLabel.get(j)==0 && (Double)dataset.get(j).get(numberOfColumns)==0)
					d[i]++;
			}
			accuracy[i] = ((a[i]+d[i])/(double)(a[i]+b[i]+c[i]+d[i]));
			precision[i] = ((a[i])/(double)(a[i]+c[i]));
			recall[i] = ((a[i])/(double)(a[i]+b[i]));
			fmeasure[i] = ((2*a[i])/(double)(2*a[i]+b[i]+c[i]));
			//System.out.println("a:"+accuracy[i]+" b: "+precision[i]+" c: "+recall[i]+" d: "+fmeasure[i]);
		}
		for(int i = 0;i<10;i++){
			avgStats[0]+=accuracy[i];
			avgStats[1]+=precision[i];
			avgStats[2]+=recall[i];
			avgStats[3]+=fmeasure[i];
		}
		for(int i = 0;i<4;i++){
			avgStats[i] = avgStats[i]/10;
		}
		System.out.println("Accuracy: "+avgStats[0]);
		System.out.println("Precision: "+avgStats[1]);
		System.out.println("Recall: "+avgStats[2]);
		System.out.println("fStat: "+avgStats[3]);
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