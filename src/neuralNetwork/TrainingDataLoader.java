package neuralNetwork;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrainingDataLoader {
	
	public static int dataSize = 0;
	public static int outputSize = 0;
	
	public static List<TrainingData> trainingData = new ArrayList<TrainingData>();

	public static void loadTrainingData(File file){
		
		float[] data;
		float[] target;
		
		FileReader fr = null;
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't load file");
			e.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(fr);
		String line;
		
		try{
			
			while ((line = reader.readLine()) != null){
				String[] currentLine = line.split(",");

				if(line.startsWith("dataSize")){
					dataSize = Integer.parseInt(currentLine[1]);
				}
				else if(line.startsWith("outputSize")){
					outputSize = Integer.parseInt(currentLine[1]);
				}
				else {
					
					List<Float> dataList = new ArrayList<Float>();
					List<Float> targetList = new ArrayList<Float>();
					
					for (int i=0 ; i<currentLine.length ; i++){
						if (i<dataSize){
							int value = Integer.parseInt(currentLine[i]);
							float fValue = Float.intBitsToFloat(value);
							
							dataList.add(fValue);
							//dataList.add(Float.parseFloat(currentLine[i]));
						}
						else{
							int value = Integer.parseInt(currentLine[i]);
							float fValue = Float.intBitsToFloat(value);
							
							targetList.add(fValue);
							//targetList.add(Float.parseFloat(currentLine[i]));
						}
					}
					
					//System.out.println(dataList);
					//System.out.println(targetList);
					
					data = new float[dataList.size()];		
					for (int i=0 ; i<data.length ; i++){
						data[i] = dataList.get(i);
					}
					dataList.clear();
					
					target = new float[outputSize];		
					for (int i=0 ; i<outputSize ; i++){
						target[i] = targetList.get(i);
					}
					targetList.clear();

					trainingData.add(new TrainingData(data, target));
				}
			}
			
			reader.close();		
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public static List<TrainingData> getTrainingData(){
		return trainingData;
	}
	
	public static int getDataSize() {
		return dataSize;
	}

	public static int getOutputSize() {
		return outputSize;
	}
	
}
