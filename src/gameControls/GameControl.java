package gameControls;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import neuralNetwork.DisplayManager;
import neuralNetwork.NetworkManager;

public class GameControl {

	private static final int BOARD_X = 160;
	private static final int BOARD_Y = 32;
	private static final int BOARD_TOPLEFT_PIXEL = 10;
	private static final int SQUARE_WIDTH = 24;
	
	public static final String GAMERECORDS_PATH = "res/game_records/";
	
	DisplayManager display;
	NetworkManager networkManager;
	
	public int turn = 0;				//turn = 0 = black's turn, turn = 1 = white's turn
	public int[] player = {0,0};		// player[0] = black player, player[1] = white player. 0=human,1=computer
	public Boolean humanTurn = false;
	public int turnNumber = 0;
	
	public int operationMode = 0; 				//0=regular play, 1 = training mode, 2 = run from gameRecord
	
	public int[][] board = new int[19][19];				//1=black, -1=white, 0=empty
	public int[][] koBoard = new int[19][19];			//for keeping track of which spaces are ko (black and white)
	public int[][][] validMoves = new int[2][19][19];	//first index=player(black/white), 0=valid, 1=occupied, -1=ko
	public int[][] lastBoard = new int[19][19];			//for dealing with ko
	public float[][] netOutputBoard = new float[19][19];
	
	public int[] lastMove = new int[2];
	public int[][] gameRecord = new int[400][2];
	public int gameRecordLength;
	
	public static void main(String[] args) {
		new GameControl();
	}
	
	public GameControl(){
		
		display = new DisplayManager(this);
		display.repaint();
		display.repaint();
		networkManager = new NetworkManager(display, this);
		networkManager.start();
		display.setNetworkManager(networkManager);

		gameStart();
		
	}
	
	private void gameStart(){
		if (operationMode == 0){
			if (player[0]==0){humanTurn=true;}
			else{
				getValidMoves();
				startAITurn();
			}
		}
		
		if (operationMode == 2){
			SGFLoader.loadGameRecord(new File(GAMERECORDS_PATH + "2qj8-gokifu-20170916-Li_Qincheng-Lee_Sedol" + ".sgf"));
			gameRecord = SGFLoader.getGameRecord();
			gameRecordLength = SGFLoader.getGameRecordLength();
			
			display.repaint();
			
			//turnFromGameRecord();
		}
	}
	
	public void turnEnd(){
		turn++;
		if (turn>1){turn=0;}
		turnNumber++;
		
		display.repaint();
		
		//getValidMoves();
		
		if (operationMode == 0){
			if (player[turn]==0){
				humanTurn = true;
			}
			
			if (player[turn]==1){
				humanTurn = false;
				
				startAITurn();
			}
		}
		
		if (operationMode == 2){
			if (turnNumber < gameRecordLength){
				//turnFromGameRecord();		//or wait for NEXT button to be pressed for next move
			}
		}
		
	}
	
	public void startAITurn(){
		float[] input = new float[361];
		float[] output = new float[361];
		int moveX = 0;
		int moveY = 0;
		float bestValue = 0;
		int color = 1-2*turn;		//maps turn (0,1) -> (1,-1) -> (black,white)
		
		for (int i=0 ; i<19 ; i++){
			for (int j=0 ; j<19 ; j++){
				input[i + 19*j] = board[i][j];
			}
		}
		
		networkManager.setGlobalInput(input);
		networkManager.globalForwardPropagate();
		output = networkManager.getGlobalOutput();
		
		
		for (int i=0 ; i<19 ; i++){
			//System.out.println();
			for (int j=0 ; j<19 ; j++){
				netOutputBoard[i][j] = output[i+19*j];
				//System.out.print(output[i+19*j] + ", ");
				
				if (color * netOutputBoard[i][j] > bestValue){
					if ((validMoves[turn][i][j]==0) && (checkMoveIsLegal(board, i, j, color))){
						bestValue = color * netOutputBoard[i][j];
						moveX = i;
						moveY = j;
					}
				}
				
			}
		}
		
		makeMove(moveX, moveY, color);
		removeDeadStones(moveX, moveY, color);
		
		try {
			TimeUnit.MILLISECONDS.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		turnEnd();
		
	}
	
	public void turnFromGameRecord(){
		int x = gameRecord[turnNumber][0];
		int y = gameRecord[turnNumber][1];
		int color = 1-2*turn;
		
		makeMove(x, y, color);
		removeDeadStones(x, y, color);
		
		display.repaint();
		
		turnEnd();
	}
	
	public void humanMove(int x, int y){
		int color = 1-2*turn;
		if ((validMoves[turn][x][y] == 0) && (checkMoveIsLegal(board, x, y, color))){
			
    		makeMove(x, y, color);
    		removeDeadStones(x, y, color);
    				
    		display.repaint();
    		
    		turnEnd();
    	}
	}
	
	public void makeMove(int x, int y, int color){
		board[x][y] = color;
		validMoves[0][x][y] = 1;
		validMoves[1][x][y] = 1;
		lastMove[0] = x;
		lastMove[1] = y;
		System.out.println("Move: " + turnNumber + ", Player: " + turn + ", (" + x + ", " + y + ")");
	}
	
	public void removeDeadStones(int x, int y, int color){
		//"color" is color being played. When checking opponent captures, check -color.
		
		List<int[]> deadStones = new ArrayList<int[]>();
		
		if ((x+1>=0) && (x+1<19) && (y>=0) && (y<19)){
			if (board[x+1][y] == -color){
				deadStones.addAll(checkCaptured(board, x+1, y));
			}
		}
		if ((x>=0) && (x<19) && (y-1>=0) && (y-1<19)){
			if (board[x][y-1] == -color){
				deadStones.addAll(checkCaptured(board, x, y-1));
			}
		}
		if ((x-1>=0) && (x-1<19) && (y>=0) && (y<19)){
			if (board[x-1][y] == -color){
				deadStones.addAll(checkCaptured(board, x-1, y));
			}
		}
		if ((x>=0) && (x<19) && (y+1>=0) && (y+1<19)){
			if (board[x][y+1] == -color){
				deadStones.addAll(checkCaptured(board, x, y+1));
			}
		}
		
		//clear out all captured stones
		
		System.out.println("Removed stones: " + deadStones.size());
		
		for (int[] stone : deadStones){
			board[stone[0]][stone[1]] = 0;
			validMoves[0][stone[0]][stone[1]] = 0;
			validMoves[1][stone[0]][stone[1]] = 0;
			System.out.println("Removed stone: (" + stone[0] + ", " + stone[1] + ")");
		}
		deadStones.clear();
		
	}
	
	public List<int[]> checkCaptured(int[][] board, int x, int y){
		List<int[]> deadStones = new ArrayList<int[]>();
		Chain chain = new Chain(board, x, y);
		
		if (chain.getLibertyCount()==0){
			deadStones = chain.getChain();
		}
		
		return deadStones;
	}
	
	public Boolean checkMoveIsLegal(int[][] board, int x, int y, int color){
		//"color" is color being played. When checking opponent captures, check -color.
		
		List<int[]> deadStones = new ArrayList<int[]>();
		
		board[x][y] = color;		//place stone, then check if it captures any stones
		
		if ((x+1>=0) && (x+1<19) && (y>=0) && (y<19)){
			if (board[x+1][y] == -color){
				deadStones.addAll(checkCaptured(board, x+1, y));
			}
		}
		if ((x>=0) && (x<19) && (y-1>=0) && (y-1<19)){
			if (board[x][y-1] == -color){
				deadStones.addAll(checkCaptured(board, x, y-1));
			}
		}
		if ((x-1>=0) && (x-1<19) && (y>=0) && (y<19)){
			if (board[x-1][y] == -color){
				deadStones.addAll(checkCaptured(board, x-1, y));
			}
		}
		if ((x>=0) && (x<19) && (y+1>=0) && (y+1<19)){
			if (board[x][y+1] == -color){
				deadStones.addAll(checkCaptured(board, x, y+1));
			}
		}
		
		//clear out all captured stones first
		
		for (int[] stone : deadStones){
			board[stone[0]][stone[1]] = 0;
		}
		deadStones.clear();
		
		//then check whether placed stone is captured itself
		
		
		Chain self = new Chain(board, x, y);
		if (self.getLibertyCount()==0){
			return false;
		}else{
			return true;
		}
		
	}
	
	public void getValidMoves(){
		
		//Remove ko (When stone is captured creating ko, validMoves is set to -2. Add 1 to negative values every turn until = 0
		// This means opponent's turn begins with validMoves = -1. At end of their turn, it equals 0 and space is valid again.)
		/*
		for (int p=0 ; p<=1 ; p++){
			for (int i=0 ; i<19 ; i++){
				for (int j=0 ; j<19 ; j++){
					if (validMoves[p][i][j] < 0){
						validMoves[p][i][j]++;
					}
				}
			}
		}
		*/
		
		//Can't play into position that captures yourself unless it captures opponent in the process.
		
	}
	

}
