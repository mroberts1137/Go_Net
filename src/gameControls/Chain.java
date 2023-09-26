package gameControls;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Chain {
	
	public int[][] board = new int[19][19];
	public int[][] chainCoords = new int[19][19];
	public int[][] libertyCoords = new int[19][19];
	public int color = 1;
	
	public List<int[]> stones = new ArrayList<int[]>();
	public List<int[]> liberties = new ArrayList<int[]>();
	
	public Chain(int[][] board, int x, int y){
		this.board = board;
		this.color = board[x][y];
		
		stones.add(new int[]{x,y});
		chainCoords[x][y] = 1;
		
		buildChain();
		
		System.out.println("Chain Size: " + stones.size() + ", Liberties: " + getLibertyCount());
	}
	
	private void buildChain(){
		
		int i=0;
		while (i < stones.size()){
			int[] stone = stones.get(i);
			int x = stone[0];
			int y = stone[1];
			
			// {x+1, y}
			
			if ((x+1>=0) && (x+1<19) && (y>=0) && (y<19)){
				//If stone is same color, add to chain
				if ((board[x+1][y] == color) && (chainCoords[x+1][y]==0)){
					stones.add(new int[]{x+1,y});
					chainCoords[x+1][y] = 1;
				}
				//add liberties
				if ((board[x+1][y] == 0) && (libertyCoords[x+1][y]==0)){
					liberties.add(new int[]{x+1,y});
					libertyCoords[x+1][y] = 1;
				}
			}
			
			// {x, y-1}
			
			if ((x>=0) && (x<19) && (y-1>=0) && (y-1<19)){
				//If stone is same color, add to chain
				if ((board[x][y-1] == color) && (chainCoords[x][y-1]==0)){
					stones.add(new int[]{x,y-1});
					chainCoords[x][y-1] = 1;
				}
				//add liberties
				if ((board[x][y-1] == 0) && (libertyCoords[x][y-1]==0)){
					liberties.add(new int[]{x,y-1});
					libertyCoords[x][y-1] = 1;
				}
			}
			
			// {x-1, y}
			
			if ((x-1>=0) && (x-1<19) && (y>=0) && (y<19)){
				//If stone is same color, add to chain
				if ((board[x-1][y] == color) && (chainCoords[x-1][y]==0)){
					stones.add(new int[]{x-1,y});
					chainCoords[x-1][y] = 1;
				}
				//add liberties
				if ((board[x-1][y] == 0) && (libertyCoords[x-1][y]==0)){
					liberties.add(new int[]{x-1,y});
					libertyCoords[x-1][y] = 1;
				}
			}
			
			// {x, y+1}
			
			if ((x>=0) && (x<19) && (y+1>=0) && (y+1<19)){
				//If stone is same color, add to chain
				if ((board[x][y+1] == color) && (chainCoords[x][y+1]==0)){
					stones.add(new int[]{x,y+1});
					chainCoords[x][y+1] = 1;
				}
				//add liberties
				if ((board[x][y+1] == 0) && (libertyCoords[x][y+1]==0)){
					liberties.add(new int[]{x,y+1});
					libertyCoords[x][y+1] = 1;
				}
			}
			
			i++;
		}
		
		
	}
	
	public int getLibertyCount(){
		return liberties.size();
	}
	
	public List<int[]> getChain(){
		return stones;
	}
	
	public List<int[]> getLiberties(){
		return liberties;
	}
	
}
