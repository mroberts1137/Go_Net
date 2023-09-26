package neuralNetwork;

public class BasicVector extends VectorContainer{

	public BasicVector(String name, int size){
		super.type = "vector";
		super.name = name;
		super.inputSize = size;
		super.outputSize = size;
		super.initializeInput(size);
		super.setOutput(new float[size]);
	}
	
	@Override
	public void forwardPropagate() {
		super.setOutput(super.getInput());
	}

	@Override
	public void backPropagate() {
		super.setDeltaIn(super.getDeltaOut());
	}
	
	@Override
	public void batchGradientDescent() {
		
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
