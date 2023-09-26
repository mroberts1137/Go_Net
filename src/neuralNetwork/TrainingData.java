package neuralNetwork;

public class TrainingData {

	float[] data;
	float[] target;
	
	public TrainingData(float[] data, float[] target) {
		this.data = data;
		this.target = target;
	}
	
	public float[] setData() {
		return data;
	}
	public float[] setTarget() {
		return target;
	}
	
	public float[] getData() {
		return data;
	}
	public float[] getTarget() {
		return target;
	}
	
}
