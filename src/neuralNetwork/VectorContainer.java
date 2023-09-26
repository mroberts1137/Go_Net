package neuralNetwork;

import java.util.ArrayList;
import java.util.List;

public abstract class VectorContainer {

	public String type;
	public String name = null;
	
	public int outputShapeType = 0;
	
	public List<VectorContainer> inputList = new ArrayList<VectorContainer>();
	public List<VectorContainer> outputList = new ArrayList<VectorContainer>();
	
	public float[] input;
	public float[] output;
	public float[] deltaIn;
	public float[] deltaOut;
	
	public int inputSize;
	public int outputSize;
	
	public int[] connectionStartIndex;  //starting index for this vc's output into next vc input
	
	public abstract void forwardPropagate();
	public abstract void backPropagate();
	public abstract void batchGradientDescent();
	public abstract void getContainerInput();
	public abstract void setContainerInput();
	public abstract void getContainerOutput();
	public abstract void setContainerOutput();
	public abstract void getContainerDeltaIn();
	public abstract void setContainerDeltaIn();
	public abstract void getContainerDeltaOut();
	public abstract void setContainerDeltaOut();

	
	public class Net{
		public Net(String name, int[] layerSize, int shapeType) {}
	}
	public class ConvNet{}
	public class BasicVector{}
	public class LSTM{}
	
	
	public void setInput() {
		//This performs the Direct Sum of all the inputs
		int index = 0;
		for (VectorContainer vc : inputList){
			for (int i=0 ; i<vc.outputSize ; i++){
				input[index] = vc.output[i];
				index ++;
			}
		}
	}
	
	public void manualSetInput(float[] input){
		this.input = input;
	}
	
	public void setDeltaOut() {
		//deltaOut is the sum of all deltaIn's in the outputList
		for (int i=0 ; i<outputSize ; i++){
			deltaOut[i] = 0;
			for (int j=0 ; j<outputList.size() ; j++){
				deltaOut[i] += outputList.get(j).getDeltaIn()[i + connectionStartIndex[j]];
			}
				
				if (outputShapeType==0){
					deltaOut[i] *= 1 - output[i]*output[i];
				}
				if (outputShapeType==1){
					deltaOut[i] *= output[i]*(1 - output[i]);
				}
		
		}
	}
	
	public void initializeInput(int size){
		input = new float[size];
	}
	
	public  void setInputList(List<VectorContainer> in){
		inputList = in;
	}
	
	public  void setOutputList(List<VectorContainer> out){
		outputList = out;
	}
	
	public void setConnectionIndex(){
		connectionStartIndex = new int[outputList.size()];
		
		for (int j=0 ; j<outputList.size() ; j++){
			int index = 0;
			VectorContainer vc = outputList.get(j);
			for (int i=0 ; i<vc.inputList.indexOf(this) ; i++){
				index += vc.inputList.get(i).outputSize;
			}
			
			connectionStartIndex[j] = index;
		}
	}
	
	public void debugDisplay(){
		System.out.println(name + ":");
		
		if (inputList.size()>0){
			System.out.print("Input List: {");
			for (VectorContainer vc : inputList){
				System.out.print(vc.name + " ");
			}
			System.out.println("}");
		}
		if (outputList.size()>0){
			System.out.print("Output List: {");
			for (VectorContainer vc : outputList){
				System.out.print(vc.name + " ");
			}
			System.out.println("}");
			for (int j=0 ; j<outputList.size() ; j++){
				System.out.println("Connection index : " + outputList.get(j).name + ": " + connectionStartIndex[j]);
			}
		}
	}
	
	public void displayInput(){
		System.out.println("input: ");
		for (int i=0 ; i<inputSize ; i++){
			System.out.println("("+i+")" + input[i]);
		}
		System.out.println();
	}
	
	public String getType(){
		return type;
	}
	public void setType(String type){
		this.type = type;
	}
	public float[] getInput() {
		return input;
	}
	public void setOutput(float[] output) {
		this.output = output;
	}
	public float[] getOutput() {
		return output;
	}
	public float[] getDeltaIn() {
		return deltaIn;
	}
	public void setDeltaIn(float[] deltaIn) {
		this.deltaIn = deltaIn;
	}
	public float[] getDeltaOut() {
		return deltaOut;
	}
	
	public List<VectorContainer> getInputList() {
		return inputList;
	}
	public List<VectorContainer> getOutputList() {
		return outputList;
	}
	public int getInputSize() {
		return inputSize;
	}
	public void setInputSize(int inputSize) {
		this.inputSize = inputSize;
	}
	public int getOutputSize() {
		return outputSize;
	}
	public void setOutputSize(int outputSize) {
		this.outputSize = outputSize;
	}
	public int getOutputShapeType(){
		return outputShapeType;
	}
	public void setOutputShapeType(int val){
		outputShapeType = val;
	}
}
