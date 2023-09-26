package neuralNetwork;

import java.util.ArrayList;
import java.util.List;

public class LSTM extends VectorContainer{
	
	List<Net> netList = new ArrayList<Net>();
	
	
	Net cellNet = newNetwork("cell", new int[]{super.inputSize, 5, 6, super.outputSize}, 0);
	Net ignoreNet = newNetwork("ignore", new int[]{super.inputSize, 5, 6, super.outputSize}, 1);
	Net forgetNet = newNetwork("forget", new int[]{super.inputSize, 5, 6, super.outputSize}, 1);
	Net outputNet = newNetwork("output", new int[]{super.inputSize, 5, 6, super.outputSize}, 1);
	
	public void initiate(){
		netList.add(cellNet);
		netList.add(ignoreNet);
		netList.add(forgetNet);
		netList.add(outputNet);
	}
	
	
	
	
	
	
	
	
	
	public Net newNetwork(String name, int[] layerSize, int shapeType){
		Net net = new Net(name, layerSize, shapeType);

		return net;
	}
	
	public void setConnectionArrays(){
		for (Net vc : netList){
			//vc.setConnectionIndex();
		}
	}
	
	
	
	

	@Override
	public void forwardPropagate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void backPropagate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void batchGradientDescent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getContainerInput() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContainerInput() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getContainerOutput() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContainerOutput() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getContainerDeltaIn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContainerDeltaIn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getContainerDeltaOut() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContainerDeltaOut() {
		// TODO Auto-generated method stub
		
	}

}
