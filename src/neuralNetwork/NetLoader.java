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


public class NetLoader {

	public static Net loadNet(File file){
		
		int layers = 0;
		int[] layerSize;
		float[][][] W;
		int shapeType = 0;
		List<Integer> lSize = new ArrayList<Integer>();
		List<List<List<Float>>> weights = new ArrayList<List<List<Float>>>();
		
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
			
			while((line = reader.readLine()) != null){
				String[] currentLine = line.split(" ");
				
				/*
				System.out.println("line: " + line);
				System.out.print("currentLine: ");
				for (int i=0 ; i<currentLine.length ; i++){
					System.out.print("("+i+")" + currentLine[i] + ", ");
				}
				System.out.println();
				*/
				
				
				if(line.startsWith("layers:")){
					layers = Integer.parseInt(currentLine[1]);
				}
				else if(line.startsWith("layerSize:")){
					for (int i=1 ; i<currentLine.length ; i++){
						lSize.add(Integer.parseInt(currentLine[i]));
					}
				}
				if(line.startsWith("shapeType:")){
					shapeType = Integer.parseInt(currentLine[1]);
				}
				
				if(line.startsWith("W[")){
					List<List<Float>> Wl = new ArrayList<List<Float>>();
					
					while ((line = reader.readLine()).startsWith("endW") == false){
						currentLine = line.split(" ");
						
						/*
						System.out.println("line: " + line);
						System.out.print("currentLine: ");
						for (int i=0 ; i<currentLine.length ; i++){
							System.out.print("("+i+")" + currentLine[i] + ", ");
						}
						System.out.println();
						*/
						
						
						if(line.startsWith("(")){
							List<Float> Wli = new ArrayList<Float>(currentLine.length-1);
							
							for (int i=1 ; i<currentLine.length ; i++){
								int value = Integer.parseInt(currentLine[i]);
								float fValue = Float.intBitsToFloat(value);
								
								Wli.add(fValue);
							}
							//System.out.println(Wli);
							Wl.add(Wli);
						}
					}
					//System.out.println("Layer (i): " + Wl);
					weights.add(Wl);
				}

			}
			
			reader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		layerSize = new int[lSize.size()];		
		for (int i=0 ; i<layerSize.length ; i++){
			layerSize[i] = lSize.get(i);
		}
		lSize.clear();
			
		
		W = new float[layers+1][][];
		for (int l=1 ; l<=layers ; l++){
			W[l] = new float[layerSize[l-1]+1][layerSize[l]+1];
			
			for (int i=0 ; i<=layerSize[l-1] ; i++){
				for (int j=1 ; j<=layerSize[l] ; j++){
					W[l][i][j] = weights.get(l-1).get(i).get(j-1);
				}
			}
		}
		weights.clear();
			
		return new Net("net", layerSize, W, shapeType);
	}
	
	
	
	public static void saveNet(Net net, File file){
		try {
			file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write("layers: " + net.getLayers() + "\n");
			
			bw.write("layerSize: ");
			for (int i=0 ; i<=net.getLayers(); i++){
				bw.write(net.getLayerSize()[i] + " ");
			}
			bw.write("\n");
			
			bw.write("shapeType: " + net.getShapeType() + "\n");
			
			for (int l=1 ; l<=net.getLayers() ; l++){
				bw.write("W[" + l + "]\n");
				for (int i=0 ; i<=net.getLayerSize()[l-1] ; i++){
					bw.write("("+i+") ");
					for (int j=1 ; j<=net.getLayerSize()[l] ; j++){
						int value = Float.floatToIntBits(net.getWeights()[l][i][j]);
						bw.write(value + " ");
					}
					bw.write("\n");
				}
				bw.write("endW\n");
			}
			
			bw.close();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
