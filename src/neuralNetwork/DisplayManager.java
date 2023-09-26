package neuralNetwork;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gameControls.Chain;
import gameControls.GameControl;

public class DisplayManager extends JPanel implements MouseListener, MouseMotionListener{

	private static final int WIDTH = 800;
	private static final int HEIGHT = 700;
	private static final int DRAW_WIDTH = 600;
	private static final int DRAW_HEIGHT = 350;
	private static final int DRAW_X = 80;
	private static final int DRAW_Y = 64;
	private static final int BOARD_X = 170;
	private static final int BOARD_Y = 42;
	private static final int BOARD_TOPLEFT_PIXEL = 10;
	private static final int SQUARE_WIDTH = 24;
	
	public static final String NETWORKS_PATH = "weights/";
	public static final String TRAININGDATA_PATH = "training_data/";
	public static final String IMAGES_PATH = "images/";
	
	public static JFrame frame = new JFrame("GoNet");
	JPanel panel = new JPanel(new GridBagLayout());
	Draw draw = new Draw();
	
	GridBagConstraints grid = new GridBagConstraints();
	
	JButton trainButton = new JButton("Train Network");
	JButton loadButton = new JButton("Load");
	JButton saveButton = new JButton("Save");
	JButton newButton = new JButton("New");
	JButton paintButton = new JButton("Repaint");
	JButton randomInputButton = new JButton("Forward Propagate");
	JButton switchViewButton = new JButton("Switch View");
	JButton nextButton = new JButton("Next");
	JButton groupCheckButton = new JButton("Group Check");
	
	JLabel drawContainerText = new JLabel("Display Container:");
	JTextField drawContainerField = new JTextField("1", 10);

	JPanel panelUp = new JPanel(new GridBagLayout());
	JLabel drawPick0Name = new JLabel();
	JLabel drawPick1Name = new JLabel();
	JLabel drawPick0 = new JLabel();
	JLabel drawPick1 = new JLabel();
	
	int drawContainer = 1;
	int displayMode = 1;
	int clickMode = 0;			//clickMode=0 -> place stone, clickMode=1 -> check group liberties/connection
	List<int[]> drawChain = new ArrayList<int[]>();
	List<int[]> drawLiberties = new ArrayList<int[]>();
	
	public int mouseX = 0;
	public int mouseY = 0;
	public int mouseTileX = 0;
	public int mouseTileY = 0;
	public int lastMouseTileX = 0;
	public int lastMouseTileY = 0;
	
	public static JFileChooser fileChooser = new JFileChooser(IMAGES_PATH);
	
	public NetworkManager networkManager = null;
	GameControl game;
	
	public DisplayManager(GameControl game){
		this.game = game;
		
		setupFrame();
	}
	
	public void setNetworkManager(NetworkManager net){
		networkManager = net;
	}

	public void setupFrame(){
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		panel.setBackground(Color.GRAY);
		frame.setVisible(true);
		
		frame.addMouseListener(this);
        frame.addMouseMotionListener(this);
		
		grid.insets = new Insets(10, 10, 10, 10);
		
		grid.gridx = 0;
		grid.gridy = 0;
		panel.add(newButton, grid);
		newButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				//networkManager.newNetwork(new int[]{7, 5, 6, 3});
			}
		});
		
		grid.gridx = 1;
		grid.gridy = 0;
		panel.add(trainButton, grid);
		trainButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				networkManager.trainNetworkFromFile();	
			}
		});
		
		grid.gridx = 2;
		grid.gridy = 0;
		panel.add(loadButton, grid);
		loadButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				networkManager.loadNetwork();	
			}
		});
		
		grid.gridx = 3;
		grid.gridy = 0;
		panel.add(saveButton, grid);
		saveButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				networkManager.saveNetwork();
			}
		});
		
		grid.gridx = 3;
		grid.gridy = 1;
		panel.add(paintButton, grid);
		paintButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				draw.repaint();	
			}
		});
		
		grid.gridx = 2;
		grid.gridy = 1;
		panel.add(randomInputButton, grid);
		randomInputButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				networkManager.randomInput();
				draw.repaint();
			}
		});
		
		grid.gridx = 0;
		grid.gridy = 1;
		panel.add(drawContainerText, grid);
		
		grid.gridx = 1;
		grid.gridy = 1;
		panel.add(drawContainerField, grid);
		drawContainerField.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int value = Integer.parseInt(drawContainerField.getText());
				if ((value>=0) & (value < networkManager.netList.size())){
					drawContainer = value;
					draw.repaint();
				}
			}
		});
		
		grid.gridx = 4;
		grid.gridy = 1;
		panel.add(switchViewButton, grid);
		switchViewButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				displayMode ++;
				if (displayMode > 1){
					displayMode = 0;
				}
				draw.repaint();
			}
		});
		
		grid.gridx = 4;
		grid.gridy = 2;
		panel.add(nextButton, grid);
		nextButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				
				if (game.turnNumber < game.gameRecordLength){
					game.turnFromGameRecord();
					draw.repaint();
				}
			}
		});
		
		grid.gridx = 5;
		grid.gridy = 2;
		panel.add(groupCheckButton, grid);
		groupCheckButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				
				clickMode++;
				if (clickMode>1){clickMode=0;}
			}
		});
		
		grid.gridx = 0;
		grid.gridy = 3;
		panelUp.add(drawPick0, grid);
		
		grid.gridx = 1;
		grid.gridy = 3;
		panelUp.add(drawPick1, grid);
		
		frame.add(panel, BorderLayout.SOUTH);
		frame.add(panelUp, BorderLayout.NORTH);
		frame.add(draw, BorderLayout.CENTER);
		
	}
	
	public int returnValue(int value){
		return value;
	}

	
	
	////////////////////////////////////// PAINT /////////////////////////////////////////////
	
	public class Draw extends JComponent{
		public void paint(Graphics g){
			Graphics2D g2 = (Graphics2D) g;
			
			BufferedImage boardImage = null;
			try {
				boardImage = ImageIO.read(new File(IMAGES_PATH + "GoBoard.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			g2.drawImage(boardImage, BOARD_X-BOARD_TOPLEFT_PIXEL, BOARD_Y-BOARD_TOPLEFT_PIXEL, this);
			
			//displayMode=1 - Game Board view
			
			//if (displayMode==1){
				BufferedImage blackStone = null;
				BufferedImage whiteStone = null;
				BufferedImage blackStoneTransparent = null;
				BufferedImage whiteStoneTransparent = null;
				try {
					blackStone = ImageIO.read(new File(IMAGES_PATH + "blackStone.png"));
					whiteStone = ImageIO.read(new File(IMAGES_PATH + "whiteStone.png"));
					blackStoneTransparent = ImageIO.read(new File(IMAGES_PATH + "blackStoneTransparent.png"));
					whiteStoneTransparent = ImageIO.read(new File(IMAGES_PATH + "whiteStoneTransparent.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if (game.turn == 0){
					g2.drawImage(blackStone, 32, 32 , this);
				} else if (game.turn == 1){
					g2.drawImage(whiteStone, 32, 32 , this);
				}
				
				for (int i=0 ; i<19 ; i++){
					for (int j=0 ; j<19 ; j++){
						if (game.board[i][j] == 1){
							g2.drawImage(blackStone, BOARD_X+i*SQUARE_WIDTH-SQUARE_WIDTH/2, BOARD_Y+j*SQUARE_WIDTH-SQUARE_WIDTH/2, this);
						} else if (game.board[i][j] == -1){
							g2.drawImage(whiteStone, BOARD_X+i*SQUARE_WIDTH-SQUARE_WIDTH/2, BOARD_Y+j*SQUARE_WIDTH-SQUARE_WIDTH/2, this);
						}
					}
				}
				
				if ((mouseTileX >= 0) && (mouseTileX < 19) && (mouseTileY >= 0) && (mouseTileY < 19) && (clickMode==0)){
					if (game.validMoves[game.turn][mouseTileX][mouseTileY] == 0){
						if ((game.player[game.turn]==0) && (game.operationMode==0)){
							if (game.turn==0){
								g2.drawImage(blackStoneTransparent, BOARD_X+mouseTileX*SQUARE_WIDTH-SQUARE_WIDTH/2, BOARD_Y+mouseTileY*SQUARE_WIDTH-SQUARE_WIDTH/2, this);
							} else if (game.turn==1){
								g2.drawImage(whiteStoneTransparent, BOARD_X+mouseTileX*SQUARE_WIDTH-SQUARE_WIDTH/2, BOARD_Y+mouseTileY*SQUARE_WIDTH-SQUARE_WIDTH/2, this);
							}
						}
					}
				}
				
				
				if(game.turnNumber > 0){
					if (game.turn==1){
						g2.setColor(Color.WHITE);	//last turn was white
					} else{
						g2.setColor(Color.BLACK);	//last turn was black
					}
					g2.drawOval(BOARD_X+game.lastMove[0]*SQUARE_WIDTH-SQUARE_WIDTH/2+5, BOARD_Y+game.lastMove[1]*SQUARE_WIDTH-SQUARE_WIDTH/2+5, 12, 12);
					g2.drawOval(BOARD_X+game.lastMove[0]*SQUARE_WIDTH-SQUARE_WIDTH/2+4, BOARD_Y+game.lastMove[1]*SQUARE_WIDTH-SQUARE_WIDTH/2+4, 14, 14);
				}
				
				if ((clickMode==1) && (drawChain.size()>0)){
					g2.setColor(Color.GREEN);
					for (int[] stone : drawChain){
						g2.drawOval(BOARD_X+stone[0]*SQUARE_WIDTH-SQUARE_WIDTH/2+2, BOARD_Y+stone[1]*SQUARE_WIDTH-SQUARE_WIDTH/2+2, 20, 20);
						g2.drawOval(BOARD_X+stone[0]*SQUARE_WIDTH-SQUARE_WIDTH/2+3, BOARD_Y+stone[1]*SQUARE_WIDTH-SQUARE_WIDTH/2+3, 19, 19);
					}
					g2.setColor(Color.BLUE);
					for (int[] liberty : drawLiberties){
						g2.drawOval(BOARD_X+liberty[0]*SQUARE_WIDTH-SQUARE_WIDTH/2+2, BOARD_Y+liberty[1]*SQUARE_WIDTH-SQUARE_WIDTH/2+2, 20, 20);
						g2.drawOval(BOARD_X+liberty[0]*SQUARE_WIDTH-SQUARE_WIDTH/2+3, BOARD_Y+liberty[1]*SQUARE_WIDTH-SQUARE_WIDTH/2+3, 19, 19);
					}
				}
				
				
				
				
			//} //end displayMode=1
			
			////////////////////////////////////////////////////////////////////
			//displayMode=0 - Neural Net view
			////////////////////////////////////////////////////////////////////
			
			if (displayMode==0){
				
				for (int i=0 ; i<19 ; i++){
					for (int j=0 ; j<19 ; j++){
						if (game.validMoves[game.turn][i][j]==0){
							Color c;
							float val = 1/(1+(float)Math.exp(game.netOutputBoard[i][j]));
							c = new Color(0, 0, 1,val);
							g2.setColor(c);
							g2.fillOval(BOARD_X+i*SQUARE_WIDTH-SQUARE_WIDTH/2+4, BOARD_Y+j*SQUARE_WIDTH-SQUARE_WIDTH/2+4, 16, 16);
						}
					}
				}
				
				
			} //End displayMode==0
			
			
		}
	}
	////////////////////////////////////////////////end Draw class



	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (game.humanTurn){
			mouseX = e.getX()-8-BOARD_X+SQUARE_WIDTH/2;
			mouseY = e.getY()-50-BOARD_Y+SQUARE_WIDTH/2;
			lastMouseTileX = mouseTileX;
			lastMouseTileY = mouseTileY;
			mouseTileX = mouseX/SQUARE_WIDTH;
			mouseTileY = mouseY/SQUARE_WIDTH;
	    	
	    	draw.repaint();
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if ((game.humanTurn) && (clickMode==0)){
			mouseX = e.getX()-8-BOARD_X+SQUARE_WIDTH/2;
			mouseY = e.getY()-50-BOARD_Y+SQUARE_WIDTH/2;
			lastMouseTileX = mouseTileX;
			lastMouseTileY = mouseTileY;
			mouseTileX = mouseX/SQUARE_WIDTH;
	    	mouseTileY = mouseY/SQUARE_WIDTH;
	    	
	    	if ((mouseTileX >= 0) && (mouseTileX < 19) && (mouseTileY >= 0) && (mouseTileY < 19)){
	    		game.humanMove(mouseTileX,  mouseTileY);
	    	}
	    	
		}
		
		if (clickMode==1){
			mouseX = e.getX()-8-BOARD_X+SQUARE_WIDTH/2;
			mouseY = e.getY()-50-BOARD_Y+SQUARE_WIDTH/2;
			lastMouseTileX = mouseTileX;
			lastMouseTileY = mouseTileY;
			mouseTileX = mouseX/SQUARE_WIDTH;
	    	mouseTileY = mouseY/SQUARE_WIDTH;
	    	
	    	if ((mouseTileX >= 0) && (mouseTileX < 19) && (mouseTileY >= 0) && (mouseTileY < 19)){
	    		if (!(game.board[mouseTileX][mouseTileY] == 0)){
	    			Chain chain = new Chain(game.board, mouseTileX, mouseTileY);
		    		drawChain.clear();
		    		drawLiberties.clear();
		    		drawChain = chain.getChain();
		    		drawLiberties = chain.getLiberties();
		    		
		    		draw.repaint();
	    		}
	    	}
	    	
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
