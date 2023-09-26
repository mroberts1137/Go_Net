package neuralNetwork;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import gameControls.GameControl;

public class NetworkManager {
	
	List<VectorContainer> netList = new ArrayList<VectorContainer>();
	int networkSize;
	
	VectorContainer inputVector;
	VectorContainer outputVector;
	
	public int inputSize;
	public int outputSize;
	
	public int activeNet = 1;
	public boolean hasNet = false;
	
	DisplayManager display;
	GameControl game;
	
	Random random = new Random();
	
	public NetworkManager(DisplayManager display, GameControl game){
		this.display = display;
		this.game = game;
	}
	
	public void start(){
		inputVector = new BasicVector("input", 361);
		Net net1 = newNetwork("net1", new int[]{361, 361, 361});
		outputVector = new BasicVector("output", 361);
		
		netList.add(inputVector);
		netList.add(net1);
		netList.add(outputVector);
		
		
		inputVector.setOutputList(new ArrayList<VectorContainer>(Arrays.asList(new VectorContainer[]{net1})));
		
		net1.setInputList(new ArrayList<VectorContainer>(Arrays.asList(new VectorContainer[]{inputVector})));
		net1.setOutputList(new ArrayList<VectorContainer>(Arrays.asList(new VectorContainer[]{outputVector})));
		
		outputVector.setInputList(new ArrayList<VectorContainer>(Arrays.asList(new VectorContainer[]{net1})));
		
		
		setConnectionArrays();
		
		inputSize = inputVector.inputSize;
		outputSize = outputVector.outputSize;
		
		displayNetList();
	}

	public void globalForwardPropagate(){
		inputVector.forwardPropagate();
		for (int i=1 ; i<netList.size() ; i++){
			netList.get(i).setInput();
			netList.get(i).forwardPropagate();
		}
	}
	
	public void globalBackPropagate(){
		for (int i=netList.size()-2 ; i>0 ; i--){
			netList.get(i).setDeltaOut();
			netList.get(i).backPropagate();
		}
	}
	
	public void globalBatchGradientDescent(){
		for (int i=1 ; i<netList.size()-1 ; i++){
			netList.get(i).batchGradientDescent();
		}
	}
	
	public void setTargetDelta(float[] target){
		
		float[] delta = new float[outputVector.outputSize];
		for (int i=0 ; i<outputVector.outputSize ; i++){
			float x = outputVector.getOutput()[i];
			delta[i] = x - target[i];
			//Don't shape here, since setDeltaOut shapes when it grabs delta
		}
		outputVector.setDeltaIn(delta);
	}
	
	public void trainNetworkFromFile(){
		//display.fileChooser.setCurrentDirectory(display.TRAININGDATA_PATH);
		int fileReturn = DisplayManager.fileChooser.showOpenDialog(DisplayManager.frame);
		if (fileReturn == JFileChooser.APPROVE_OPTION){
			File file = DisplayManager.fileChooser.getSelectedFile();
			
			TrainingDataLoader.loadTrainingData(file);
			
			System.out.println("Training Data input size: " + TrainingDataLoader.getDataSize());
			System.out.println("Training Data output size: " + TrainingDataLoader.getOutputSize());
				
			if (hasNet){
				if((inputSize == TrainingDataLoader.getDataSize()) & (outputSize == TrainingDataLoader.getOutputSize())){
					
					
					List<TrainingData> trainingData = TrainingDataLoader.getTrainingData();
					for (int i=0 ; i<100 ; i++){		//just to repeat training 100 times.
					for (TrainingData td : trainingData){
						
						inputVector.manualSetInput(td.getData());
						globalForwardPropagate();
						
						System.out.print("Cost: (Before) " + getError(td.getTarget()));
						
						setTargetDelta(td.getTarget());
						globalBackPropagate();
						
						globalBatchGradientDescent();

						////////////////debug test:
						globalForwardPropagate();
						System.out.println(", (after) " + getError(td.getTarget()));
						
						display.draw.repaint();
						///////////////
					}
					}
					
				}
				else{
					System.out.println("ERROR: Network size does not match training data size");
				}
			}
			else{
				System.out.println("ERROR: There is no active network.");
			}
		}
	}
	
	public Net newNetwork(String name, int[] layerSize){
		Net net = new Net(name, layerSize, 0);
		
		hasNet = true;
		display.draw.repaint();

		return net;
	}
	
	public ConvNet newConvNet(String name, int[] filterNum){
		ConvNet cn = new ConvNet(name, filterNum);
		
		hasNet = true;
		display.draw.repaint();
		
		return cn;
	}
	
	public void loadNetwork(){
		JOptionPane.showMessageDialog(null,  "Loading a network will erase current network.");
		int fileReturn = DisplayManager.fileChooser.showOpenDialog(DisplayManager.frame);
		if (fileReturn == JFileChooser.APPROVE_OPTION){
			File file =  DisplayManager.fileChooser.getSelectedFile();
			
			System.out.println("File: " + file);
			
			Net net = NetLoader.loadNet(file);
			netList.add(net);
			hasNet = true;
			display.draw.repaint();
			
			System.out.println("Net input size: " + net.getInputSize());
			System.out.println("Net output size: " + net.getOutputSize());
		}
	}
	
	public void saveNetwork(){
		int fileReturn = DisplayManager.fileChooser.showSaveDialog(DisplayManager.frame);
		if (fileReturn == JFileChooser.APPROVE_OPTION){
			File file = DisplayManager.fileChooser.getSelectedFile();
			
			NetLoader.saveNet(getActiveNet(), file);
		}
	}
	
	public Net getActiveNet(){
		if (netList.get(activeNet).type == "net"){
			return (Net) netList.get(activeNet);
		}
		else{
			return null;
		}
	}
	
	public float getError(float[] target){
		float error = 0;
		for (int i=0 ; i<outputSize ; i++){
			error += (float) Math.pow(outputVector.getOutput()[i] - target[i], 2)/2;
		}
		return error;
	}
	
	public void setConnectionArrays(){
		for (VectorContainer vc : netList){
			vc.setConnectionIndex();
		}
	}
	
	public void randomInput(){
		if (hasNet){
			/*
			for (int i=0 ; i<inputVector.inputSize ; i++){
				inputVector.input[i] = (random.nextFloat()*2)-1;
			}
			*/
			globalForwardPropagate();
			display.draw.repaint();
		}
	}
	
	public void loadImage(){
		BufferedImage image = null;
		float[] imageData = new float[inputSize];
		
		int fileReturn = DisplayManager.fileChooser.showOpenDialog(DisplayManager.frame);
		if (fileReturn == JFileChooser.APPROVE_OPTION){
			File file = DisplayManager.fileChooser.getSelectedFile();
			try {
				image = ImageIO.read(file);
				System.out.println("File: " + file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (image.getWidth() * image.getHeight() == inputSize){
			for (int i=0 ; i<image.getHeight(); i++){
				for (int j=0 ; j<image.getHeight(); j++){
					Color c = new Color(image.getRGB(i,j));
					int g = (c.getRed() + c.getBlue() + c.getGreen())/3;
					
					imageData[i*image.getWidth() + j] = ((float) g)/256;
				}
			}
		}
		else{
			System.out.println("Image size doesn't match input vector size");
		}

		inputVector.manualSetInput(imageData);
	}
	
	public void displayNetList(){
		System.out.print("Net List: {");
		for (int i=0 ; i<netList.size() ; i++){
			System.out.print(netList.get(i).name);
			if (i<netList.size()-1){
				System.out.print(", "); 
			}
		}
		System.out.println("}");
		
		System.out.println("Input Size: " + inputSize);
		System.out.println("Output Size: " + outputSize);
		System.out.println();
		
		for (VectorContainer vc : netList){
			vc.debugDisplay();
			if (vc.type=="net"){
				Net net = (Net) vc;
				System.out.print("Layer Size: {");
				for (int l=0 ; l<=net.layers ; l++){
					System.out.print(net.layerSize[l]);
					if (l<net.layers){
						System.out.print(", "); 
					}
				}
				System.out.println("}");
			}
			System.out.println();
		}
	}

	public void setGlobalInput(float[] input){
		inputVector.manualSetInput(input);
	}
	
	public float[] getGlobalOutput(){
		return outputVector.getOutput();
	}
	

}
