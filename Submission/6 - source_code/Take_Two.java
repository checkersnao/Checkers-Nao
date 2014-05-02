import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Take_Two extends JPanel {
 
 public static final int SIZE = 1200; // size of the display window
 /**
  * Main routine makes it possible to run Take-Two as a stand-alone
  * application.  Opens a window showing a Take-Two panel; the program
  * ends when the user closes the window.
  * @throws IOException 
  */
 private static String fileName;

 public static void main(String[] args) throws IOException {
  
  if (args.length < 1) {
    System.err.println("Please specify participant's ID");
    System.exit(1);
  }


  fileName = args[0];
  System.out.println(fileName);

  JFrame window = new JFrame("Take Two");
  Take_Two content = new Take_Two();
  window.setContentPane(content);
  window.pack();
  window.setLocation(0,0);
  window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  window.setResizable(false);  
  window.setVisible(true);

 }
       
 private JButton newGameButton;  // button for starting a new game
 private JButton nextMoveButton; // button to allow next player to move                             
 private JLabel message;  // Label for displaying messages to the user.
 
    
 /**
  * The constructor creates the Board (which in turn creates and manages
  * the buttons and message label), adds all the components, and sets
  * the bounds of the components.  A null layout is used.  (This is
  * the only thing that is done in the main Take_Two class.)
  * @throws IOException 
  */
 public Take_Two() throws IOException {
  
  setLayout(null); 
  setPreferredSize(new Dimension(SIZE + SIZE/2 ,SIZE + SIZE/20 + 10));
       
  setBackground(Color.LIGHT_GRAY);  // light gray background
    
  /* Create the components and add them to the applet. */
       
  Board board = new Board();  // Note: The constructor for the
                                   //   board also creates the button
               //   and label.
  add(board);
  add(nextMoveButton);
  add(newGameButton);
  add(message);
  /* Set the position and size of each component by calling
   * its setBounds() method. */
       
  board.setBounds(0,SIZE/20 + 10,SIZE + SIZE/2,SIZE);
     nextMoveButton.setBounds((int) (SIZE/10), 10, SIZE/5, SIZE/20);
     newGameButton.setBounds(SIZE + SIZE/5, 10, SIZE/5, SIZE/20);
     message.setBounds(SIZE/4, 10, SIZE, SIZE/20);
       
    } // end constructor
 
//*********************************GAME DATA**********************************//
 
 public static class GameData {
  
  /*  The following constants represent the values of the blocks, the
   * number of blocks, and the number of moves each player can make. */
  
  static final int
   PLAYER_1 = 1,
   PLAYER_2 = 2;
  
  static final int
   EMPTY = 0,
   BLUE = 2,
   WHITE = 5,
   GREEN = 10,
   RED = 25;
     
     
  
  static final int
   b_count = 5,
   w_count = 5,
   r_count = 4,
   g_count = 4;
     
  
  static final int hesitation = 1000; // hesitation of opponent in ms
  static final int numMoves = 2;
  static int numPieces = b_count + g_count + r_count + w_count;
  
  ArrayList<Integer> ramp;  // ramp containing all blocks
      
  /* Constructor */
  GameData() {}
      
  /**
   * Set up the ramp with blocks in position along the ramp Given 5
   * different block values, we will try and have an equal number of
   * each block on our ramp.
   */
  void setUpGame() {
   
   ramp = new ArrayList<Integer>();
     
   int[] counts = new int[4]; // number of each block
   Random r = new Random();
 
      // fill in the blocks on the ramp
      for (int i = 0; i < numPieces; i++) {
        
       //if (curr_number >= max_number)
       // maxPiece = numPieces / 4;
        
       // generate a random number up to the value of WHITE
       int number = r.nextInt(numPieces) + 1;
       int type_count = -1;
       int index = -1; 
       // check if that number equals a block value
       if (number <= b_count) {
        number = BLUE;
        index = 0;
        type_count = b_count;
       }
       else if (number <= w_count+b_count) {
        number = WHITE;
        index = 1;
        type_count = w_count;
       }
       else if (number <= g_count+w_count+b_count) {
        number = GREEN;
        index = 2;
        type_count = g_count;
       }
       else if (number <= r_count+g_count+b_count+w_count) {
        number = RED;
        index = 3;
        type_count = r_count;
       }
      
        
       // update count of blocks
       if (counts[index] < type_count) {
        ramp.add(number);
        counts[index]++;
        //if (counts[index] == maxPiece)
        // curr_number++;
       }
       else {
        i--; // retry if too many blocks
        continue;
       }
      }
  }  // end setUpGame()
      
  // get rampPiece and any value i
  int pieceAt(int i) {
   if (i >= ramp.size()) 
    return -1;
   else
    return ramp.get(i);
  }
  
  // get number of blocks remaining
  int getSize() {
   return ramp.size();
  }
      
  // return blocks on ramp as an int[]
  int[] getBlocks() {
   int[] blocks = new int[ramp.size()];
   for (int i = 0; i < ramp.size(); i++)
       blocks[i] = ramp.get(i); 
      return blocks;
  }
      
  /**
   * Called when a player makes a move. Removes the first block from the
   * ramp, and returns the value of that block.
   */
  int makeMove() {
   int value = ramp.get(0);
   ramp.remove(0);
   return value;
  }
 } // end class GameData
 
//*******************************BOARD****************************************//
 private class Board extends JPanel implements ActionListener, MouseListener {
  
  GameData game; // the data for the game is kept here.

     boolean gameInProgress; // is a game currently in progress?

     private Timer timer = new Timer(GameData.hesitation, this);

     File file;
     BufferedWriter output;
        
     long currentTime;
     int currentPlayer; // whose turn is it now?
                            
     int player2_moves; // number of moves player has made this turn
     int player1_moves;
     
     int player1_score; // score each player so far
     int player2_score;
     
     ArrayList<Integer> player1; // blocks player has taken
     ArrayList<Integer> player2;
     ArrayList<Long> moveTimes;
     
     /**
      * Constructor.  Create the buttons and label.  Listens for mouse
      * clicks and for clicks on the buttons.  Create the board and
      * start the first game.
      * @throws IOException 
      */
     Board() throws IOException {
        setBackground(Color.BLACK);
        addMouseListener(this);
        nextMoveButton = new JButton("Done");
        nextMoveButton.addActionListener(this);
        newGameButton = new JButton("New Game");
        newGameButton.addActionListener(this);
        message = new JLabel("",JLabel.CENTER);
        message.setFont(new  Font("Dialog", Font.BOLD, SIZE/40));
        message.setForeground(Color.BLACK);
        currentTime = 0;
        game = new GameData();
        doNewGame();
     }
      
     /**
      * Start a new game
      * @throws IOException 
      */
     void doNewGame() throws IOException {
      
      game.setUpGame();   // Set up the pieces.
      
      // initialize all values
      timer.stop();
      player1 = new ArrayList<Integer>();
         player2 = new ArrayList<Integer>();
         player1_score = 0;
         player2_score = 0;
         player1_moves = 0;
         player2_moves = 0;
         
         
         boolean success = new File("SimulationData").mkdirs();
         
         file = new File("SimulationData/" + fileName + ".txt");
         output = new BufferedWriter(new FileWriter(file, true));
         moveTimes = new ArrayList<Long>();
  
         currentPlayer = GameData.PLAYER_1;   // player 1 moves first.
         nextMoveButton.setEnabled(true);
         newGameButton.setEnabled(false);
         message.setText("Your Move!");
         gameInProgress = true;
         repaint();
     }
     
     void gameOver() throws IOException {
      
      int sum = 0;
     
      for (int i = 0; i < moveTimes.size(); i++)
       sum += moveTimes.get(i);
      
      output.write(moveTimes.size() + " Moves");
      output.newLine();
      output.write(sum / moveTimes.size() + "");
   output.close();
   gameInProgress = false;
   newGameButton.setEnabled(true);
      message.setText("Game Over!");
      return;
     }
    
     /**
      * Called when the player clicks on the board, x and y represent
      * the coordinates of the click.
      * @throws IOException 
      */
     void doClickSquare(int x, int y) throws IOException {
      // if it is player 2's turn, do nothing
      if (currentPlayer == GameData.PLAYER_2)
       return;
      
      // find size and center of block at bottom of ramp
      int size = SIZE / (int) (GameData.numPieces) / 2;
      int x_center = SIZE / 5 + size;
      int y_center = SIZE - SIZE / 5 + size;
      
      // check if this square was clicked, if yes, make a mocve
      if (x < x_center + size && x > x_center - size &&
       y < y_center + size && y > y_center - size) {
       doMakeMove();
       repaint();
       return;
      }
   
      // otherwise do nothing
      return;
     } // end doClickSquare()
       
     /**
      * Makes a move using the makeMove method from GameData. Called 
      * when player 1 clicks, and called recursively on player 2's turn
      * until all desired blocks have been choosen.
      * @throws IOException 
      */
     void doMakeMove() throws IOException {
      
      // check if any blocks remain
      if (game.getSize() <= 0) {
       gameOver();
       return;
      }
         
      //moves for player 1
      if (currentPlayer == GameData.PLAYER_1) {
       // check if already used all moves
       if (player1_moves >= GameData.numMoves) {
        message.setText("You can only take " + GameData.numMoves + " blocks!");
        return;
       }
       // otherwise take block and add to score
       player1_score += game.pieceAt(0);
       player1.add(game.makeMove());
       player1_moves++;
       
       if (game.getSize() <= 0)
        gameOver();
      }
      
      //moves for opponent
         else {
          //check if opponent has made all moves
          if (player2_moves <= 0) {
           
           if (game.getSize() <= 0)
            gameOver();
           else {
            currentTime = System.currentTimeMillis();
            currentPlayer = GameData.PLAYER_1;
            message.setText("Your Move!");
            nextMoveButton.setEnabled(true);
           }
           return;
          }
          // otherwise take block and add to score
          player2_score += game.pieceAt(0);
             player2.add(game.makeMove());
             player2_moves--;
             // pause and repeat
          timer.start();
         }
       repaint();
     } // end doMakeMove();
     
//*******************************PAINTING*************************************//
     /**
      * This is called by mousePressed() when a player clicks on the
      * square in the specified row and col.  It has already been checked
      * that a game is, in fact, in progress.
      */
     
     public void paintComponent(Graphics g) {  
      
      int size = SIZE / (int) (GameData.numPieces);
     
      Graphics2D g2d = (Graphics2D)g;
     
      // clear last iteration
      g2d.clearRect(0, 0, SIZE + SIZE/2, SIZE);
      
      // redraw background
      g2d.setColor(Color.LIGHT_GRAY);
     g2d.fillRect(0, 0, SIZE + SIZE/2, SIZE);
      
     // draw block value key
     drawKey(g2d);
    
      Font f = new Font("Dialog", Font.PLAIN, size);
      g2d.setFont(f);
       
      int x_offset = SIZE / 5;
      int y_offset = SIZE - SIZE / 5;
      int buffer = 3;
            
      for (int i = 0; i < game.getSize(); i++) {
      
       int x = x_offset + (size + buffer) * i;    
       int y = y_offset;
       int toCorner = (int) Math.sqrt(2*size*size) / 2 + buffer;
       Rectangle rect = new Rectangle(x, y, size, size);
       g2d.setColor(Color.WHITE);
           
       if (i >= 2) {
        x = x_offset + toCorner * (i+1);   
           y = y_offset - toCorner * (i-2);
           g2d.translate(x, y);
           g2d.rotate(Math.toRadians(-45));
           rect = new Rectangle(0, 0, size, size);
           }
           
           drawBlock(g2d, game.pieceAt(i), rect);
        
           if (i >= 2) {
            g2d.rotate(Math.toRadians(45));
          g2d.translate(-x, -y);
           }
      } 

      int y = SIZE - SIZE / 10;
      g2d.setColor(Color.BLACK);
      g2d.drawString(scoreToString(player1_score), 0, y + size);
       
      for (int i = 0; i < player1.size(); i++) {
         
       int x = x_offset + (size + buffer) * i;    
         
       Rectangle rect = new Rectangle(x, y, size, size);
       drawBlock(g2d, player1.get(i), rect); 
      }

      y = SIZE / 10;
      g2d.setColor(Color.BLACK);
      g2d.drawString(scoreToString(player2_score), 0, y + size);
        
      for (int i = 0; i < player2.size(); i++) {
         
         int x = x_offset + (size + buffer) * i;    
     
         Rectangle rect = new Rectangle(x, y, size, size);
         drawBlock(g2d, player2.get(i), rect); 
      } 
        
      return;
     } // end paintComponent()
     
     void drawKey (Graphics2D g2d) {
    
      int key_x = SIZE + SIZE/8;
      int key_y = SIZE/ 7;
      int key_size = SIZE/10;
         
      Font f = new Font("Dialog", Font.PLAIN, key_size / 2);
   g2d.setFont(f);
       
   for (int i = 0; i < 4; i++) {
          
    int x = key_x;
    int y = key_y + (key_size) * i;
    Rectangle rect = new Rectangle(x + 2*key_size, y, key_size, key_size);
    String value = "";
    
    switch (i) {
    case 0:
     drawBlock(g2d, GameData.BLUE, rect);
     value = scoreToString(GameData.BLUE);
     break;
    case 1:
     drawBlock(g2d, GameData.WHITE, rect);
     value = scoreToString(GameData.WHITE);
     break;          
    case 2:
     drawBlock(g2d, GameData.GREEN, rect);
     value = scoreToString(GameData.GREEN);
     break;
    case 3:
     drawBlock(g2d, GameData.RED, rect);
     value = scoreToString(GameData.RED);
     break;
    /*case 3:
     drawBlock(g2d, GameData.PURPLE, rect);
     value = scoreToString(GameData.PURPLE);
     break;*/
    }
    
          g2d.setColor(Color.BLACK);
          g2d.drawString(value, x+key_size, y + (int)(key_size/1.5));
       }
     }
     
     void drawBlock(Graphics2D g2d, int color, Rectangle rect) {
      
      switch (color) {
            case GameData.RED:
             g2d.setColor(Color.RED);
          g2d.draw(rect);
          g2d.fill(rect);
          break;
            case GameData.GREEN:
             g2d.setColor(Color.GREEN);
          g2d.draw(rect);
          g2d.fill(rect);
          break;
            case GameData.BLUE:
             g2d.setColor(Color.BLUE);
                g2d.draw(rect);
                g2d.fill(rect);
                break;  
            /*case GameData.PURPLE:
             g2d.setColor(Color.MAGENTA);
                g2d.draw(rect);
                g2d.fill(rect);
                break;*/  
            case GameData.WHITE:
             g2d.setColor(Color.WHITE);
                g2d.draw(rect);
                g2d.fill(rect);
           break;
            }
     }
     
//*********************************ACTIONS************************************//
     /**
      * Respond to a user click on the board.  If no game is in progress, 
      * show an error message.  Otherwise, find the row and column that the 
      * user clicked and call doClickSquare() to handle it.
      */
     public void mousePressed(MouseEvent evt) {
      if (gameInProgress == false)
       message.setText("Click \"New Game\" to start a new game.");
      else {
       try {
     doClickSquare(evt.getX(),evt.getY());
    } catch (IOException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
    }
      }
     }
     
     public void mouseReleased(MouseEvent evt) { }
     public void mouseClicked(MouseEvent evt) { }
     public void mouseEntered(MouseEvent evt) { }
     public void mouseExited(MouseEvent evt) { }
     
     /**
      * Respond to user's click on one of the two buttons.
      */
     public void actionPerformed(ActionEvent evt) {
      Object src = evt.getSource();
        
      // start a new game
      if (src == newGameButton)
    try {
     doNewGame();
    } catch (IOException e1) {
     e1.printStackTrace();
    }
      
   else if (src == nextMoveButton) {
       try {
        if (currentTime != 0) {
         long moveTime = System.currentTimeMillis() - currentTime;
         output.write(moveTime + "");
         output.newLine();
         moveTimes.add(moveTime);
        }
    } catch (IOException e) {
     e.printStackTrace();
    }
       // for player 1
       if (currentPlayer == GameData.PLAYER_1) {
        //ensures player has made a move
        if (player1_moves == 0) {
         message.setText("You must make a move!");
         return;
        }
        message.setText("");
        currentPlayer = GameData.PLAYER_2;
        nextMoveButton.setEnabled(false);
              player1_moves = 0;
              //get best moves for player two
              player2_moves = findNextMove();
              timer.start();
       }
      }
        
      //wait before making the next move
      else if (src == timer) {
       timer.stop(); 
       try {
     doMakeMove();
    } catch (IOException e) {
     e.printStackTrace();
    }
      }
     }
     
//********************************ALGORITHM***********************************// 
     public int findNextMove() {
 
      int moves = GameData.numMoves;
      int[] cards = game.getBlocks();
      
      int size = cards.length;
       
      int best_taken = moves;
      int best_value = 0;
      int worst_taken = moves;
      int worst_value = sum(cards, 1, moves);
         
      int k;   
      boolean found = false;

      for (k = 1; k <= moves; k++) {
          
       int their_sum, my_sum;
       their_sum = my_sum = 0;
          
       for (int j = k; j < size && j < k + moves; j++)
        their_sum += cards[j];
           
       for (int j = k + moves; j < size && j < k + 2*moves; j++)
        my_sum += cards[j];
       
       // maximize sum if better than opponent
       if (my_sum >= their_sum) {
        if (my_sum + sum(cards, 0, k) >= best_value) {
         best_taken = k;
         best_value = my_sum + sum(cards, 0, k); 
        }
        found = true;
       }
           
       // minimize opponent's sum if can't beat them
       else {
        if (their_sum <= worst_value) {
         worst_taken = k;
         worst_value = their_sum;
        }
       }
      }
         
      if (found)
       k = best_taken;

      if (!found)
       k = worst_taken;
         
      return k;
     }
       
     public int sum(int[] cards, int i, int taken) {
      int size = cards.length;
      int value = 0;
        
      for (int j = i; j < size && j < i + taken; j++)
       value += cards[j];  
      
      return value;
     }
     
     public String scoreToString(int score) {
      String score_string;
      if (score / 100 >= 1) {
       score_string = "" + score;
      }
      else if (score / 10 >= 1) {
       score_string = " " + score;
      }
      else {
       score_string = "  " + score;
      }
      return score_string;
     }
  } // end class Board
}