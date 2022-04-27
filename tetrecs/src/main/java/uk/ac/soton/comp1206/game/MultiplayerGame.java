package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;
import java.util.LinkedList;
import java.util.concurrent.Executors;

/**
 * The MultiplayerGame extends the Game class, and implements communicator to allow for multiplayer to function.
 */
public class MultiplayerGame extends Game{

    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

    /**
     * Communicator used to receive and send messages to the server
     */
    protected Communicator communicator;

    /**
     * GameWindow of the current game
     */
    protected GameWindow gameWindow;

    /**
     * Queue of pieces to be played
     */
    protected LinkedList<GamePiece> queue = new LinkedList<>();

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public MultiplayerGame(int cols, int rows, GameWindow gameWindow) {
        super(cols, rows);
        this.gameWindow = gameWindow;
    }

    /**
     * Handles what should happen when a new piece is received from the server
     * @param gamePiece
     */
    public void newPiece(GamePiece gamePiece) {
        if(currentPiece == null) {
            currentPiece = gamePiece; //First Piece
        } else if(followingPiece == null){
            followingPiece = gamePiece; //Second Piece
            nextPieceListener.nextPiece(currentPiece, followingPiece);
        } else {
            queue.add(gamePiece);//Creates Queue
        }
    }

    /**
     * Reassigns current and following pieces, and removing pieces from the queue
     */
    @Override
    public void nextPiece() {
        currentPiece = followingPiece;
        followingPiece = queue.remove();
        nextPieceListener.nextPiece(currentPiece, followingPiece);
        communicator.send("PIECE");
    }

    /**
     * Handles what should happen once a piece has been played, and sends a current description of the game board to the
     * server
     */
    @Override
    public void afterPiece() {
        super.afterPiece();
        String board = "BOARD ";
        for (int x = 0; x < this.getCols(); x++) {
            for(int y = 0; y < this.getRows(); y++) {
                board = board + grid.get(x,y) + " ";
            }
        }
        communicator.send(board);
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start, and asks the server for initial pieces
     */
    @Override
    public void initialiseGame() {
        timer = Executors.newSingleThreadScheduledExecutor();
        communicator = gameWindow.getCommunicator();
        //Listens for messages from communicator and handles the command
        communicator.addListener(message -> Platform.runLater(() -> listen(message.trim())));
        for(int x = 0; x < 5; x++) {
            communicator.send("PIECE");
        }
    }

    /**
     * Increases the Score depending on the number of lines and blocks cleared. Also increments Level every 1000 points.
     * Sends score to the server.
     * @param lines
     * @param blocks
     */
    @Override
    public void score(int lines, int blocks) {
        super.score(lines, blocks);
        communicator.send("SCORE " + this.scoreProperty().get());
    }

    /**
     * Handles messages from communicator
     * @param message
     */
    protected void listen(String message) {
        if(message.contains("PIECE")) {
            logger.info("Adding piece to queue");
            message = message.replace("PIECE ", "");
            GamePiece gamePiece = GamePiece.createPiece(Integer.parseInt(message));
            newPiece(gamePiece);
        }
    }
}
