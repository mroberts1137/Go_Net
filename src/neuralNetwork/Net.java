package neuralNetwork;

import java.io.File;
import java.util.List;
import java.util.Random;

public class Net extends VectorContainer{

	public int layers;
	public int[] layerSize;
	public int shapeType; // 0=tanh, 1=logistic
	
	private float[][][] W;
	private float[][] X, S, delta;
	private float[] target;
	
	private float stepSize = 0.01f;
	
	private Random random = new Random();
	
	public Net(String name, int[] layerSize, int shapeType){
		super.type = "net";
		super.name = name;
		this.layers = layerSize.length-1;
		this.layerSize = layerSize;
		this.shapeType = shapeType;
		
		super.inputSize = layerSize[0];
		super.outputSize = layerSize[layers];
		super.input = new float[layerSize[0]];
		super.output = new float[layerSize[layers]];
		super.deltaIn = new float[layerSize[0]];
		super.deltaOut = new float[layerSize[layers]];
		
		W = new float[layers+1][][];
		X = new float[layers+1][];
		S = new float[layers+1][];
		delta = new float[layers+1][];
		for (int l=0 ; l<=layers ; l++){
			X[l] = new float[layerSize[l]+1];
			S[l] = new float[layerSize[l]+1];
			delta[l] = new float[layerSize[l]+1];
			if (l==0){
				//W[0] = identity
				W[l] = new float[layerSize[0]+1][layerSize[0]+1];
			}
			else{
				W[l] = new float[layerSize[l-1]+1][layerSize[l]+1];
			}
		}
		target = new float[layerSize[layers]+1];
		
		randomizeWeights();
		initializeW0();
		initializeX();

	}
	
	public Net(String name, int[] layerSize, float[][][] W, int shapeType){
		super.type = "net";
		super.name = name;
		this.layers = layerSize.length-1;
		this.layerSize = layerSize;
		this.shapeType = shapeType;
		this.inputSize = layerSize[0];
		this.outputSize = layerSize[layers];
		this.W = W;

		X = new float[layers+1][];
		S = new float[layers+1][];
		delta = new float[layers+1][];
		for (int l=0 ; l<=layers ; l++){
			X[l] = new float[layerSize[l]+1];
			S[l] = new float[layerSize[l]+1];
			delta[l] = new float[layerSize[l]+1];
		}
		target = new float[layerSize[layers]+1];
		
		initializeX();

	}
	
	
	@Override
	public void forwardPropagate(){
		getContainerInput();
		for (int l = 1 ; l<=layers ; l++){
			for (int i=1 ; i<=layerSize[l] ; i++){
				S[l][i] = 0;
				for (int j=0 ; j<=layerSize[l-1] ; j++){
					S[l][i] += W[l][j][i] * X[l-1][j];
				}
				if (shapeType==0){
					X[l][i] = (float) Math.tanh(S[l][i]);
				}else if (shapeType==1){
					X[l][i] = 1/(1+(float)Math.exp(-S[l][i]));
				}
			}
		}
		setContainerOutput();
	}
	
	@Override
	public void backPropagate(){
		getContainerDeltaOut();
		for (int l=layers-1 ; l>=0 ; l--){
			for (int i=1 ; i<=layerSize[l] ; i++){
				
				delta[l][i] = 0;
				for (int j=1 ; j<=layerSize[l+1] ; j++){
					delta[l][i] += W[l+1][i][j] * delta[l+1][j];
				}
				
				//X[0] is not shaped -> delta[0] has no sigma' factor
				if (l>0){
					if (shapeType==0){
						delta[l][i] *= (1 - X[l][i]*X[l][i]);
					}else if (shapeType==1){
						delta[l][i] *= X[l][i] * (1 - X[l][i]);
					}
				}
			}
		}
		setContainerDeltaIn();
	}
	
	@Override
	public void batchGradientDescent(){
		for (int l = 1 ; l<=layers ; l++){
			for (int i=1 ; i<=layerSize[l] ; i++){
				for (int j=0 ; j<=layerSize[l-1] ; j++){
					W[l][j][i] += -stepSize * delta[l][i] * X[l-1][j];
				}
			}
		}
	}
	
	public void randomizeWeights(){
		for (int l=1; l<=layers ; l++){
			for (int i=0 ; i<=layerSize[l-1] ; i++){
				for (int j=1 ; j<=layerSize[l] ; j++){
					W[l][i][j] = (random.nextFloat()*2 - 1);
				}
			}
		}
	}
	
	public void initializeW0(){
		for (int i=1 ; i<=inputSize ; i++){
			W[0][i][i] = 1;
		}
	}
	
	public void initializeX(){
		for (int l=0; l<=layers ; l++){
			X[l][0] = 1;
		}
	}
	
	public void randomInput(){
		for (int i=1 ; i<=layerSize[0] ; i++){
			X[0][i] = (random.nextFloat()*2)-1;
		}
		forwardPropagate();
	}
	

	
	@Override
	public void getContainerInput(){
		for (int i=0 ; i<layerSize[0] ; i++){
			//X[0] is indexed from 1 to layerSize[0]+1
			X[0][i+1] = super.input[i];
		}
	}
	
	@Override
	public void setContainerInput(){
		for (int i=0 ; i<layerSize[0] ; i++){
			super.input[i] = X[0][i+1];
		}
	}
	
	@Override
	public void getContainerOutput(){
		for (int i=0 ; i<layerSize[layers] ; i++){
			X[layers][i+1] = super.output[i];
		}
	}
	
	@Override
	public void setContainerOutput(){
		for (int i=0 ; i<layerSize[layers] ; i++){
			super.output[i] = X[layers][i+1];
		}
	}
	
	@Override
	public void getContainerDeltaIn(){
		for (int i=0 ; i<layerSize[0] ; i++){
			delta[0][i+1] = super.deltaIn[i];
		}
	}
	
	@Override
	public void setContainerDeltaIn(){
		for (int i=0 ; i<layerSize[0] ; i++){
			super.deltaIn[i] = delta[0][i+1];
		}
	}
	
	@Override
	public void getContainerDeltaOut(){
		for (int i=0 ; i<layerSize[layers] ; i++){
			delta[layers][i+1] = super.deltaOut[i];
		}
	}
	
	@Override
	public void setContainerDeltaOut(){
		for (int i=0 ; i<layerSize[layers] ; i++){
			super.deltaOut[i] = delta[layers][i+1];
		}
	}
	
	public void displayX(){
		System.out.println("X[l][i]");
		for (int l=0 ; l<=layers ; l++){
			System.out.print("Layer " + l + ": ");
			for (int i=0 ; i<=layerSize[l] ; i++){
				System.out.print("("+i+")" + X[l][i] + ", ");
			}
			System.out.println();
		}
	}
	
	public void displayS(){
		System.out.println("S[l][i]");
		for (int l=1 ; l<=layers ; l++){
			System.out.print("Layer " + l + ": ");
			for (int i=1 ; i<=layerSize[l] ; i++){
				System.out.print("("+i+")" + S[l][i] + ", ");
			}
			System.out.println();
		}
	}
	public void displayDelta(){
		System.out.println("delta[l][i]");
		for (int l=0 ; l<=layers ; l++){
			System.out.print("Layer " + l + ": ");
			for (int i=1 ; i<=layerSize[l] ; i++){
				System.out.print("("+i+")" + delta[l][i] + ", ");
			}
			System.out.println();
		}
	}
	
	public void displayW(){
		System.out.println("W[l][i][j]");
		
		System.out.println("Layer 0:");
		for (int i=1 ; i<=inputSize ; i++){
			for (int j=1 ; j<=inputSize ; j++){
				System.out.print("("+i+","+j+")" + W[0][i][j] + ", ");
			}
			System.out.println();
		}
		System.out.println();
		
		for (int l=1 ; l<=layers ; l++){
			System.out.println("Layer " + l + ":");
			for (int i=0 ; i<=layerSize[l-1] ; i++){
				for (int j=1 ; j<=layerSize[l] ; j++){
					System.out.print("("+i+","+j+")" + W[l][i][j] + ", ");
				}
				System.out.println();
			}
			System.out.println();
		}
	}

	public int getLayers() {
		return layers;
	}

	public int[] getLayerSize() {
		return layerSize;
	}
	
	public float[][][] getWeights(){
		return W;
	}
	
	public float[][] getX(){
		return X;
	}
	
	public int getShapeType(){
		return shapeType;
	}

	public int getInputSize() {
		return inputSize;
	}

	public int getOutputSize() {
		return outputSize;
	}	
	
}
