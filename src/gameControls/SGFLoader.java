package gameControls;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import neuralNetwork.TrainingData;

public class SGFLoader {

	public static final String PATH = "res/game_records/";
	
	public static int[][] gameRecord = new int[400][2];
	public static int gameRecordLength;
	
	public static String[] letterTable = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s"};
	
	public static void loadGameRecord(File file){
		
		FileReader fr = null;
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't load file");
			e.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(fr);
		String line;
		String xCoord = null;
		String yCoord = null;
		
		try{
			
			while ((line = reader.readLine()) != null){
				String[] currentLine = line.split(";");
				

				for (int i=2 ; i<currentLine.length ; i++){
					xCoord = Character.toString(currentLine[i].charAt(2));
					yCoord = Character.toString(currentLine[i].charAt(3));
					
					for (int j=0 ; j<19 ; j++){
						if (xCoord.equals(letterTable[j])){
							gameRecord[i-2][0] = j;
						}
						if (yCoord.equals(letterTable[j])){
							gameRecord[i-2][1] = j;
						}
					}
				}
				
				gameRecordLength = currentLine.length-2;
				
				for (int i=2 ; i<currentLine.length ; i++){
					System.out.println(currentLine[i] + ", (" + gameRecord[i-2][0] + ", " + gameRecord[i-2][1] + ")");
				}
				
			}
			
			reader.close();		
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public static int[][] getGameRecord(){
		return gameRecord;
	}
	
	public static int getGameRecordLength(){
		return gameRecordLength;
	}
}
