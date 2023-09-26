package neuralNetwork;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

public class ConvNet extends VectorContainer{
	
	public static final String FILTER_PATH = "filters/";
	
	public String fileName = null;

	public BufferedImage img = null;
	
	public int imageSize = 78;	//images must be square
	public int filterSize = 10;
	//use {imageSize=78, filterSize=10}, {99,13}, {134,18}, for 3 layers with size(X[3])=1
	//Generally, 7*filterSize = imageSize-8
	
	public int layers = 3;
	public int[] layerSize = new int[4];	//layerSize is the number of filters/images per layer. layerSize[0] = 1
	public int[] filterNum = new int[4];
	//number of filters per layer = {1, n1, n2, n3}
	//use layerSize = {1, n1, n1*n2, n1*n2*n3}
	public int[] imageWidth = new int[]{78, 34, 12, 1};
	//public int[] imageWidth = new int[]{134, 58, 20, 1};
	
	public float[][][][] X, S, I, delta;		//X[layer][inputImage][filter][x][y], I=F*X, S=sub(I), X=sigma(S)
	public float[][][][] F;					//F[layer][filter][x][y]
	public float[][] b, m;						//b[layer][layerSize], bias, scale
	public float[][][][] FD;				//FD[layer][layerSize][width[l]][width[l]], F*Delta - temp calculation
	
	public float stepSize = 100f;
	public float sigmaStepSize = .1f;
	
	private Random random = new Random();
	
	public ConvNet(String name, int[] filterNum){
		super.type = "convNet";
		super.name = name;
		
		img = null; //loadImage();
		
		this.filterNum = filterNum;
		this.layerSize[0] = 1;
		this.layerSize[1] = filterNum[1];
		this.layerSize[2] = filterNum[1]*filterNum[2];
		this.layerSize[3] = filterNum[1]*filterNum[2]*filterNum[3];
		
		super.inputSize = imageSize * imageSize;
		super.outputSize = this.layerSize[layers];
		super.input = new float[super.inputSize];
		super.output = new float[super.outputSize];
		super.deltaIn = new float[super.inputSize];
		super.deltaOut = new float[super.outputSize];
		super.outputShapeType = 1;
		
		F = new float[layers+1][][][];
		X = new float[layers+1][][][];
		S = new float[layers+1][][][];
		I = new float[layers+1][][][];
		delta = new float[layers+1][][][];
		b = new float[layers+1][];
		m = new float[layers+1][];
		FD = new float[layers+1][][][];
		
		X[0] = new float[1][imageSize][imageSize];
		for (int l=1 ; l<=layers ; l++){
			
			X[l] = new float[layerSize[l]][imageWidth[l]][imageWidth[l]];
			S[l] = new float[layerSize[l]][imageWidth[l]][imageWidth[l]];
			
			I[l] = new float[layerSize[l]][imageWidth[l]*2][imageWidth[l]*2];
			delta[l] = new float[layerSize[l]][imageWidth[l]*2][imageWidth[l]*2];
			
			F[l] = new float[filterNum[l]][filterSize][filterSize];
			
			b[l] = new float[layerSize[l]];
			m[l] = new float[layerSize[l]];
			
			FD[l] = new float[layerSize[l]][imageWidth[l]][imageWidth[l]];
		}

		initializeM();
		//getInputImage();
		loadFiltersFromFile();
	}
	
	@Override
	public void forwardPropagate() {
		getContainerInput();
		
		for (int l=1 ; l<=layers ; l++){
			for (int x=0 ; x<layerSize[l-1] ; x++){
				for (int f=0 ; f<filterNum[l] ; f++){
					//convolute image x with filter f
					
					int n = layerSize[l-1]*f + x;
					
					for (int i=0 ; i<2*imageWidth[l] ; i++){
						for (int j=0 ; j<2*imageWidth[l] ; j++){
							I[l][n][i][j] = 0;
							
							for (int a=0 ; a<filterSize ; a++){
								for (int b=0 ; b<filterSize ; b++){
									I[l][n][i][j] += X[l-1][x][i+a][j+b] * F[l][f][a][b];
								}
							}
							I[l][n][i][j] /= (filterSize*filterSize);
						}
					}
					for (int i=0 ; i<imageWidth[l] ; i++){
						for (int j=0 ; j<imageWidth[l] ; j++){
							float a00 = I[l][n][2*i][2*j];
							float a01 = I[l][n][2*i][2*j+1];
							float a10 = I[l][n][2*i+1][2*j];
							float a11 = I[l][n][2*i+1][2*j+1];
							S[l][n][i][j] = Math.max(a00, Math.max(a01,  Math.max(a10,  a11)));
							float z = m[l][n] * (S[l][n][i][j] - b[l][n]);
							X[l][n][i][j] = 1/(1+(float)Math.exp(-z));
						}
					}
					
				}
			}
		}
		
		setContainerOutput();
	}
	
	@Override
	public void backPropagate() {
		getContainerDeltaOut();		//gets delta[layers]. delta[L][n] is 2x2.
		for (int l=layers-1 ; l>=1 ; l--){
			for (int n=0 ; n<layerSize[l] ; n++){
				
				for (int i=0 ; i<imageWidth[l] ; i++){
					for (int j=0 ; j<imageWidth[l] ; j++){
						
						FD[l][n][i][j] = 0;
						for (int f=0 ; f<filterNum[l+1] ; f++){
							int N = f*layerSize[l] + n;
							
							for (int a=0 ; a<filterSize ; a++){
								for (int b=0 ; b<filterSize ; b++){
									if ((i-a < 2*imageWidth[l+1]) & (j-b < 2*imageWidth[l+1]) & (i-a >= 0) & (j-b >= 0)){
										FD[l][n][i][j] += F[l+1][f][a][b] * delta[l+1][N][i-a][j-b];
									}
								}
							}
						}
						FD[l][n][i][j] /= (filterSize*filterSize);
						
						float a00 = I[l][n][2*i][2*j];
						float a01 = I[l][n][2*i][2*j+1];
						float a10 = I[l][n][2*i+1][2*j];
						float a11 = I[l][n][2*i+1][2*j+1];
						float max = Math.max(a00, Math.max(a01,  Math.max(a10,  a11)));
						
						int di=0;
						int dj=0;
						if (max==a01){
							dj=1;
						}else if(max==a10){
							di=1;
						}else if(max==a11){
							di=1;
							dj=1;
						}
						
						delta[l][n][2*i][2*j] = 0;
						delta[l][n][2*i][2*j+1] = 0;
						delta[l][n][2*i+1][2*j] = 0;
						delta[l][n][2*i+1][2*j+1] = 0;
						
						float x = X[l][n][i][j];
						delta[l][n][2*i+di][2*j+dj] = max * x*(1-x)*m[l][n] * FD[l][n][i][j];
					}
				}
				
				
			}
		}
		setContainerDeltaIn();
		
		//displayDelta();
		//displayF();
		//displayM();
	}
	
	@Override
	public void batchGradientDescent() {
		for (int l = 1 ; l<=layers ; l++){
			for (int f=0 ; f<filterNum[l] ; f++){
				
				for (int i=0 ; i<filterSize ; i++){
					for (int j=0 ; j<filterSize ; j++){
						float deltaF = 0;
						
						for (int n=0 ; n<layerSize[l-1] ; n++){
							int N = f*layerSize[l-1] + n;
							
							for (int a=0 ; a<2*imageWidth[l] ; a++){
								for (int b=0 ; b<2*imageWidth[l] ; b++){
									deltaF += X[l-1][n][i+a][j+b] * delta[l][N][a][b];
								}
							}
							
						}
						
						F[l][f][i][j] += -stepSize * deltaF/(filterSize*filterSize);
						
						if (F[l][f][i][j] < 0){
							F[l][f][i][j] = 0;
						}
						if (F[l][f][i][j] > 1){
							F[l][f][i][j] = 1;
						}
					}
				}
				
			}
			
			for (int n=0 ; n<layerSize[l] ; n++){
				float deltaB = 0;
				float deltaM = 0;
				for (int i=0 ; i<imageWidth[l] ; i++){
					for (int j=0 ; j<imageWidth[l] ; j++){
						float x = X[l][n][i][j];
						deltaB += -x*(1-x)*m[l][n] * FD[l][n][i][j];
						deltaM += x*(1-x)*(S[l][n][i][j] - b[l][n]) * FD[l][n][i][j];
					}
				}
				b[l][n] += -sigmaStepSize * deltaB;
				m[l][n] += -sigmaStepSize * deltaM;
				if (m[l][n] < 0){
					m[l][n] = 0;
				}
			}
		}
	}
	
	public void initializeM(){
		for (int l = 1 ; l<=layers ; l++){
			for (int n=0 ; n<layerSize[l] ; n++){
				b[l][n] = .3f;
				m[l][n] = 10;
			}
		}
	}

	public void getInputImage(){
		for (int i=0 ; i<imageSize ; i++){
			for (int j=0 ; j<imageSize ; j++){
				Color c = new Color(img.getRGB(i,j));
				int g = (c.getRed() + c.getBlue() + c.getGreen())/3;
				
				X[0][0][i][j] = ((float) g)/256;
			}
		}
	}
	
	public BufferedImage loadImage(){
		BufferedImage image = null;
		int fileReturn = DisplayManager.fileChooser.showOpenDialog(DisplayManager.frame);
		if (fileReturn == JFileChooser.APPROVE_OPTION){
			File file = DisplayManager.fileChooser.getSelectedFile();
			try {
				image = ImageIO.read(file);
				System.out.println("Image Name: " + file.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return image;	
	}
	
	public void loadFiltersFromFile(){
		for (int l=1 ; l<=layers ; l++){
			for (int f=0 ; f<filterNum[l] ; f++){
				F[l][f] = loadFilter("filter-" + Integer.toString(l) + "-" + Integer.toString(f));
			}
		}
	}
	
	public float[][] loadFilter(String file){
		BufferedImage image = null;
		float[][] filter = new float[filterSize][filterSize];
		
		try {
			image = ImageIO.read(new File(FILTER_PATH + file + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("File not found.");
		}
		for (int i=0 ; i<image.getWidth() ; i++){
			for (int j=0 ; j<image.getHeight() ; j++){
				Color c = new Color(image.getRGB(i,j));
				int g = (c.getRed() + c.getBlue() + c.getGreen())/3;
				
				filter[i][j] = ((float) g)/256;
			}
		}
		
		return filter;
	}
	
	public void displayDelta(){
		for (int l=1 ; l<=layers ; l++){
			System.out.println("Layer: " + l);
			for (int n=0 ; n<layerSize[l] ; n++){
				System.out.println("Node: " + n);
				
				for (int i=0 ; i<2*imageWidth[l] ; i++){
					for (int j=0 ; j<2*imageWidth[l] ; j++){
						System.out.print("("+i+","+j+") " + delta[l][n][i][j] + ", ");
					}
					System.out.println();
				}
			}
		}
	}
	
	public void displayF(){
		for (int l=1 ; l<=layers ; l++){
			System.out.println("Layer: " + l);
			for (int f=0 ; f<filterNum[l] ; f++){
				System.out.println("Filter: " + f);
				
				for (int i=0 ; i<filterSize ; i++){
					for (int j=0 ; j<filterSize ; j++){
						System.out.print("("+i+","+j+") " + F[l][f][i][j] + ", ");
					}
					System.out.println();
				}
			}
		}
	}
	
	public void displayM(){
		for (int l = 1 ; l<=layers ; l++){
			System.out.println("Layer: " + l);
			for (int n=0 ; n<layerSize[l] ; n++){
				System.out.print("("+n+") " + b[l][n] + ", " + m[l][n]);
			}
			System.out.println();
		}
		System.out.println();
	}

	@Override
	public void getContainerInput() {
		int width = (int)Math.sqrt(super.inputSize);
		for (int i=0 ; i<width ; i++){
			for (int j=0 ; j<width ; j++){
				X[0][0][i][j] = super.input[i*width + j];
			}
		}
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
		for (int n=0 ; n<layerSize[layers] ; n++){
			super.output[n] = X[layers][n][0][0];
		}
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
		for (int n=0 ; n<layerSize[layers] ; n++){
			float a00 = I[layers][n][0][0];
			float a01 = I[layers][n][0][1];
			float a10 = I[layers][n][1][0];
			float a11 = I[layers][n][1][1];
			float max = Math.max(a00, Math.max(a01,  Math.max(a10,  a11)));
			
			int i=0;
			int j=0;
			if (max==a01){
				j=1;
			}else if(max==a10){
				i=1;
			}else if(max==a11){
				i=1;
				j=1;
			}
			
			delta[layers][n][0][0] = 0;
			delta[layers][n][0][1] = 0;
			delta[layers][n][1][0] = 0;
			delta[layers][n][1][1] = 0;
			
			delta[layers][n][i][j] = I[layers][n][i][j] * super.deltaOut[n];
		}
	}

	@Override
	public void setContainerDeltaOut() {
		// TODO Auto-generated method stub
		
	}
	
	
	public BufferedImage getImg() {
		return img;
	}
	
	public String getFileName() {
		return fileName;
	}

	
}
