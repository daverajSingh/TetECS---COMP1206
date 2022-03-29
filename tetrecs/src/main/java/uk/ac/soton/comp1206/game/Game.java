package uk.ac.soton.comp1206.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;

import java.util.Random;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    protected GamePiece currentPiece;
    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        currentPiece = spawnPiece();
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();
        if(grid.canPlayPiece(currentPiece, x, y)) {
            grid.playPiece(currentPiece, x, y);
            nextPiece();
            afterPiece();
        }
    }
    public void afterPiece() {
        for(int x=0; x < cols; x++) {
            int count = 0;
            for(int y=0; y < rows; y++) {
                if(grid.get(x,y) == 0) break;
                count+=1;
            }
            if(count == rows) {
                clearRow(x);
            }
        }
        for(int y=0; y < rows; y++) {
            int count = 0;
            for(int x=0; x < cols; x++) {
                if(grid.get(x,y) == 0) break;
                count+=1;
            }
            if(count == cols) {
                clearCol(y);
            }
        }
    }

    public void clearRow(int row) {
        for(int col = 0; col < cols; col++) {
            grid.set(col, row, 0);
        }
    }
    public void clearCol(int col) {
        for(int row = 0; row < rows; row++) {
            grid.set(col, row, 0);
        }
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    public GamePiece spawnPiece() {
        Random random = new Random();
        int randomNum = random.nextInt(15);
        GamePiece gamePiece = GamePiece.createPiece(randomNum);
        return gamePiece;
    }

    public void nextPiece() {
        currentPiece = spawnPiece();
    }


}
