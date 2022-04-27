package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);

    protected Game game;

    protected Multimedia multimedia = new Multimedia();

    /**
     * The CurrentPiece is displayed in this GameBoard
     */
    protected GameBoard pieceBoard;

    /**
     * The FollowingPiece is displayed in this GameBoard
     */
    protected GameBoard followingPieceBoard;

    /**
     * The GameBoard for the game
     */
    protected GameBoard board;

    /**
     * The x coordinate of where a block will be placed
     */
    protected int blockX = 0;

    /**
     * The y coordinate of where a block will be placed
     */
    protected int blockY = 0;

    /**
     * The HighScore value, which is either the largest local score or the current score of the player
     */
    public SimpleIntegerProperty highScoreValue = new SimpleIntegerProperty(0);

    /**
     * The timer UI element, displaying how long the user has left to play the current piece
     */
    protected Rectangle timer;

    /**
     * The BorderPane of the current scene
     */
    protected BorderPane mainPane;

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        this.scene = gameWindow.getScene();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("challenge-background");
        root.getChildren().add(challengePane);

        //Score UI element
        var score = new Text("Score: ");
        var scoreValue = new Text("0");
        scoreValue.textProperty().bind(game.scoreProperty().asString());
        var scoreBox = new HBox(score, scoreValue);
        score.getStyleClass().add("heading");
        scoreValue.getStyleClass().add("heading");

        //Level UI element
        var level = new Text("Level: ");
        var levelValue = new Text("1");
        levelValue.textProperty().bind(game.levelProperty().asString());
        var levelBox = new HBox(level, levelValue);
        level.getStyleClass().add("heading");
        levelValue.getStyleClass().add("heading");

        //Multiplier UI element
        var multiplier = new Text("Multiplier: ");
        var multiplierValue = new Text("1");
        multiplierValue.textProperty().bind(game.multiplierProperty().asString());
        var multiplierBox = new HBox(multiplier, multiplierValue);
        multiplier.getStyleClass().add("heading");
        multiplierValue.getStyleClass().add("heading");

        //Lives UI element
        var lives = new Text("Lives: ");
        var livesValue = new Text("3");
        livesValue.textProperty().bind(game.livesProperty().asString());
        var livesBox = new HBox(lives, livesValue);
        lives.getStyleClass().add("heading");
        livesValue.getStyleClass().add("heading");

        //HBox that includes all of the Key stats for the game
        HBox stats = new HBox(20, scoreBox, levelBox, multiplierBox, livesBox);
        challengePane.getChildren().add(stats);
        stats.setAlignment(Pos.TOP_CENTER);
        stats.setTranslateY(20);

        //HighScore UI element
        var highScore = new Text("Highscore: ");
        var highScoreText = new Text();
        highScoreText.textProperty().bind(highScoreValue.asString());
        var highScoreBox = new VBox(highScore, highScoreText);
        highScore.getStyleClass().add("heading");
        highScoreText.getStyleClass().add("heading");

        //Adjusts Alignment for the HighScore element
        highScoreBox.setAlignment(Pos.TOP_CENTER);
        highScoreBox.setTranslateY(-30);
        highScoreBox.setTranslateX(22.5);

        //Current Piece preview
        pieceBoard = new GameBoard(3,3,100,100);
        pieceBoard.setAlignment(Pos.CENTER);

        //Following Piece preview
        followingPieceBoard = new GameBoard(3,3,75,75);
        followingPieceBoard.setAlignment(Pos.CENTER);
        pieceBoard.setTranslateY(-10);
        pieceBoard.setTranslateX(12.5);
        pieceBoard.paintCentre();

        //VBox of Pieces
        var pieces = new VBox(highScoreBox, pieceBoard, followingPieceBoard);
        pieces.setAlignment(Pos.CENTER_RIGHT);
        pieces.setTranslateX(-75);

        //UI timer element
        timer = new Rectangle(gameWindow.getWidth(), 10);
        var timerPane = new StackPane();
        timerPane.getChildren().add(timer);

        mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        mainPane.setRight(pieces);
        mainPane.setTop(timerPane);
        timerPane.setAlignment(Pos.TOP_LEFT);

        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(board);

        //Handle block on Gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);

        //Setting Piece Listener
        game.setNextPieceListener(this::nextPiece);

        //Setting LineClearedListener
        game.setLineClearedListener(this::lineClear);

        //Setting GameLoopListener
        game.setOnGameLoop(this::gameLoop);

        //Setting GameEndListener
        game.setGameEndListener(game -> {
            gameEnd();
            gameWindow.startScores(game);
        });

        //Setting Right Clicked Listener
        board.setOnRightClicked(this::rotate);

        //Setting BlockClickListener for the currentPiece preview
        pieceBoard.setOnBlockClick(this::rotate);

        //Setting BlockClickListener for the followingPiece preview
        followingPieceBoard.setOnBlockClick(this::swapPieces);


        //Adding a listener to the score property so that the highscore element can be changed
        game.scoreProperty().addListener(this::getHighScore);
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    protected void blockClicked(GameBlock gameBlock) {
        boolean piecePlayed = game.blockClicked(gameBlock);
        if(piecePlayed) {
            multimedia.playSound("place.wav");
            board.getBlock(blockX, blockY).paintCursor();
            game.restartLoop();
        } else {
            multimedia.playSound("fail.wav");
        }
    }

    /**
     * Set up the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();
        this.multimedia.playBackgroundMusic("game.wav");
        board.getBlock(blockX, blockY).paintCursor();
        //Handling keyboard inputs - setting on key pressed listener
        scene.setOnKeyPressed(this::keyboardInput);
        initialHighscore();
    }

    /**
     * changes the "PieceBoards" to display the correct current and following pieces
     * @param gamePiece
     * @param followingGamePiece
     */
    protected void nextPiece(GamePiece gamePiece, GamePiece followingGamePiece) {
        pieceBoard.pieceToDisplay(gamePiece);
        followingPieceBoard.pieceToDisplay(followingGamePiece);
    }

    /**
     * Rotates the given piece clockwise
     * @param gameBlock
     */
    protected void rotate(GameBlock gameBlock) {
        rotate(1);
    }

    /**
     * Rotates the given piece counter-clockwise (by calling rotate 3 times)
     */
    protected void rotateLeft() {
        rotate(3);
    }

    /**
     * Uses the game's rotate method to rotate pieces and update pieceboards
     * @param rotations
     */
    protected void rotate(int rotations) {
        for(int x = 0; x< rotations; x++) {
            game.rotateCurrentPiece();
        }
        pieceBoard.pieceToDisplay(game.getCurrentPiece());
        multimedia.playSound("rotate.wav");
    }

    /**
     * Swaps the current and following pieces
     * @param gameBlock
     */
    protected void swapPieces(GameBlock gameBlock) {
        swapPieces();
    }

    /**
     * Swaps the current and following pieces
     */
    protected void swapPieces() {
        game.swapCurrentPiece();
        pieceBoard.pieceToDisplay(game.getCurrentPiece());
        followingPieceBoard.pieceToDisplay(game.getFollowingPiece());
        multimedia.playSound("rotate.wav");
    }

    /**
     * Handles keyboard input
     * @param keyEvent
     */
    protected void keyboardInput(KeyEvent keyEvent) {
        int oldBlockX = blockX;
        int oldBlockY = blockY;
        boolean moved = false;
        if(keyEvent.getCode() == KeyCode.ESCAPE) { //Exits frame
            gameEnd();
            if(!(game instanceof MultiplayerGame)) {
                gameWindow.startMenu();
            }
            logger.info("Escape Pressed");
        } else if(keyEvent.getCode() == KeyCode.Q || keyEvent.getCode() == KeyCode.Z || keyEvent.getCode() == KeyCode.OPEN_BRACKET) {
            rotateLeft(); //Rotates the current piece left
        } else if (keyEvent.getCode() == KeyCode.E || keyEvent.getCode() == KeyCode.C || keyEvent.getCode() == KeyCode.CLOSE_BRACKET ) {
            rotate(1); // Rotates the current piece right
        } else if(keyEvent.getCode() == KeyCode.SPACE || keyEvent.getCode() == KeyCode.R) {
            swapPieces();  //Swaps the current and following pieces
        } else if(keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.X) {
            blockClicked(board.getBlock(blockX, blockY)); //Clicks piece
        } else if(keyEvent.getCode() == KeyCode.W || keyEvent.getCode() == KeyCode.UP) { // Moves cursor up
            if(blockY>0) {
                blockY-=1;
                moved = true;
            } else {
                multimedia.playSound("fail.wav");
            }
        } else if(keyEvent.getCode() == KeyCode.D || keyEvent.getCode() == KeyCode.RIGHT) { // Moves cursor right
            if(blockX<4) {
                blockX+=1;
                moved = true;
            } else {
                multimedia.playSound("fail.wav");
            }
        } else if(keyEvent.getCode() == KeyCode.S || keyEvent.getCode() == KeyCode.DOWN) { // Moves cursor down
            if(blockY<4) {
                blockY+=1;
                moved = true;
            } else {
            multimedia.playSound("fail.wav");
            }
        } else if(keyEvent.getCode() == KeyCode.A|| keyEvent.getCode() == KeyCode.LEFT) { // Moves cursor left
            if(blockX>0) {
                blockX-=1;
                moved = true;
            } else {
                multimedia.playSound("fail.wav");
            }
        }
        if(moved) {
            board.getBlock(oldBlockX, oldBlockY).resetCursor(); //Removes cursor from previous grid position
            board.getBlock(blockX, blockY).paintCursor(); //Adds cursor to current grid position
        }
    }

    /**
     * When a line has been cleared, an animation is played on a set of given GameBlockCoordinates
     * @param gameBlockCoordinates
     */
    protected void lineClear(Set<GameBlockCoordinate> gameBlockCoordinates) {
        multimedia.playSound("clear.wav");
        board.fadeOut(gameBlockCoordinates);
    }

    /**
     * Sets the timer for the next turn
     * @param delay
     */
    protected void gameLoop(int delay) {
        timer.widthProperty().set(gameWindow.getWidth());
        Timeline timerBar = createTimeLine(delay);
        timerBar.play();
    }

    /**
     * Ends the game
     */
    protected void gameEnd() {
        if(!(game instanceof MultiplayerGame)) { //Ends game only if the game is a challenge scene game
            logger.info("Game Over");
            timer.setVisible(false);
            game.endGame();
            multimedia.stopBackground();
            multimedia.playSound("transition.wav");
        }
    }

    /**
     * Creates a new timer animation, which fades from green to yellow to red depending on time left
     * @param delay
     * @return
     */
    private Timeline createTimeLine(int delay) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(timer.fillProperty(), Color.GREEN)));
        timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, new KeyValue(timer.widthProperty(), timer.getWidth())));

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay*0.5), new KeyValue(timer.fillProperty(), Color.YELLOW)));
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay*0.5), new KeyValue(timer.widthProperty(), timer.getWidth()*0.75)));

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay*0.75), new KeyValue(timer.fillProperty(), Color.RED)));
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay*0.75), new KeyValue(timer.widthProperty(), timer.getWidth()*0.5)));

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay), new KeyValue(timer.widthProperty(), 0)));

        return timeline;
    }

    /**
     * Updates highscore when the player's score has changed
     * @param observable
     * @param oldValue
     * @param newValue
     */
    protected void getHighScore(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        initialHighscore();
    }

    /**
     * Checks local file if there is a high score, otherwise it will set the highscore to be the current score if it is
     * higher
     */
    protected void initialHighscore() {
        File file = new File("scores.txt");
        int highScore = 0;
        try {
            if (file.exists()) {
                ArrayList<Pair<String, Integer>> scores = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new FileReader(file));
                Scanner scanner = new Scanner(reader);
                while (scanner.hasNext()) {
                    String[] nameScore = scanner.next().split(":");
                    var entry = new Pair<String, Integer>(nameScore[0], Integer.parseInt(nameScore[1]));
                    scores.add(entry);
                }
                scanner.close();
                scores.sort((a, b) -> b.getValue() - a.getValue());
                highScore = scores.get(0).getValue();
            } else {
                highScore = game.scoreProperty().get();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error when finding highscore");
        }
        if(game.scoreProperty().get() > highScore) {
            highScoreValue.set(game.scoreProperty().get());
        } else {
            highScoreValue.set(highScore);
        }
    }

}
