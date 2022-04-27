package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * The MultiplayerScene. Holds the UI for the Multiplayer mode in the game.
 */
public class MultiplayerScene extends ChallengeScene{
    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

    /**
     * Shows the chat to the user
     */
    protected VBox messagesBox;

    /**
     * Communicator used to send messages to the server, and receive messages from the server
     */
    protected Communicator communicator;

    /**
     * TextField used as a chat input
     */
    protected TextField textField = new TextField();


    /**
     * Holds all scores of all players in the game
     */
    protected SimpleListProperty<Pair<String, Integer>> multiplayerScores = new SimpleListProperty<>();

    /**
     * Shows the scores of players in the game
     */
    protected ScoresList leaderboard;

    /**
     * Extension - Holds previews of GameBoards of all players in the game
     */
    protected VBox boardSideBar = new VBox();

    /**
     * A Set of all players in the game
     */
    protected Set<String> players;

    /**
     * A HashMap of players to their respective GameBoard
     */
    protected HashMap<String, GameBoard> playerToGameboard;

    /**
     * Create a new MultiPlayer challenge scene
     *
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow, Set<String> playerSet) {
        super(gameWindow);
        this.multiplayerScores.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
        this.players = playerSet;
    }

    /**
     * Initialise the scene and starts the multiplayer game.
     * Asks the server for scores, and initialises the sidebar of all gameboards
     */
    @Override
    public void initialise() {
        super.initialise();
        communicator = gameWindow.getCommunicator();
        //Listens for messages from communicator and handles the command
        communicator.addListener(message -> Platform.runLater(() -> listen(message.trim())));
        communicator.send("SCORES");
        initialisePlayerBoards();
    }

    /**
     * Handles Keyboard input
     * @param keyEvent
     */
    @Override
    protected void keyboardInput(KeyEvent keyEvent) {
        super.keyboardInput(keyEvent);
        if (keyEvent.getCode() == KeyCode.T) { //Opens Chat
            if (!textField.isVisible()) {
                textField.setVisible(true);
                String message = textField.getText();
                if (message != null) {
                    communicator.send("MSG " + message);
                    textField.clear();
                }
            } else {
                textField.setVisible(false);
                textField.clear();
            }
        }
        if (keyEvent.getCode() == KeyCode.ESCAPE) { //Exits scene
            if (textField.isVisible()) {
                textField.setVisible(false);
                textField.clear();
            } else {
                multimedia.stopBackground();
                game.endGame();
                multimedia.playSound("transition.wav");
                gameEnd();
                gameWindow.startMenu();
                communicator.send("DIE");
                logger.info("Escape Pressed");
            }
        }
        if (keyEvent.getCode() == KeyCode.ENTER) {
            if (textField.isVisible()) {
                String message = textField.getText();
                if (message != null) {
                    communicator.send("MSG " + message);
                    textField.clear();
                }
                textField.setVisible(false);
            }
        }
    }

    /**
     * Set up the game object and model
     */
    @Override
    public void setupGame() {
        logger.info("Starting a new multiplayer game");

        game = new MultiplayerGame(5,  5, this.gameWindow);
    }

    /**
     * Build the Multiplayer window
     */
    @Override
    public void build() {
        super.build();

        //Chat Window within the multiplayer window
        var messagesPane = new BorderPane();
        messagesPane.setPrefSize(gameWindow.getWidth()/8, gameWindow.getHeight()/8);

        var currentMessages = new ScrollPane();
        currentMessages.setBackground(null);
        currentMessages.setPrefSize(messagesPane.getWidth(), messagesPane.getHeight()-100);
        currentMessages.needsLayoutProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                currentMessages.setVvalue(1.0);
            }
        });

        messagesBox = new VBox();
        messagesBox.getStyleClass().add("messages");
        messagesBox.setPrefSize(currentMessages.getPrefWidth(), currentMessages.getPrefHeight());
        VBox.setVgrow(currentMessages, Priority.ALWAYS);

        currentMessages.setContent(messagesBox);
        messagesPane.setCenter(currentMessages);

        var chatHeading = new Text("Chat: <Press T to Chat>");
        chatHeading.setTextAlignment(TextAlignment.CENTER);
        chatHeading.getStyleClass().add("heading");
        currentMessages.getStyleClass().add("scroller");

        var chatBox = new HBox(chatHeading, textField);
        chatBox.setMaxWidth(gameWindow.getWidth());
        textField.setPrefWidth(gameWindow.getWidth() - chatHeading.getLayoutBounds().getWidth());
        textField.setAlignment(Pos.CENTER);
        textField.setVisible(false);

        var chat = new VBox(chatBox, messagesPane);

        //Shows the score of players in the game
        leaderboard = new ScoresList();
        leaderboard.setAlignment(Pos.CENTER);
        leaderboard.setTranslateY(-50);
        leaderboard.setTranslateX(25);
        this.multiplayerScores.bind(leaderboard.listProperty());

        var sideBar = new VBox(leaderboard, pieceBoard, followingPieceBoard);

        sideBar.setAlignment(Pos.CENTER_RIGHT);
        sideBar.setTranslateX(-75);

        mainPane.setRight(sideBar);
        mainPane.setBottom(chat);

        //Setting GameEndListener
        game.setGameEndListener(game -> {
            gameEnd();
            gameWindow.loadScene(new ScoresScene(gameWindow, game, this.multiplayerScores));
        });
    }

    /**
     * Changes the style of a player's name on the leaderboard when a player gets a game over or leaves
     * @param userName
     */
    protected void endUser(String userName) {
        leaderboard.strikeThrough(userName);
    }

    /**
     * Handles messages from communicator
     * @param s
     */
    protected void listen(String s) {
        if(s.contains("MSG")) { //chat message
            s = s.replace("MSG ", "");
            String[] messageArr = s.split(":");
            if (messageArr.length > 1) {
                Text message = new Text(messageArr[0] + " : " + messageArr[1]);
                message.getStyleClass().add("messages Text");
                messagesBox.getChildren().add(message);
            }
        } else if (s.contains("SCORES")) { //Scores of all players in the game
            s = s.replace("SCORES ", "");
            String[] playerScoreLives = s.split("\n");
            this.multiplayerScores.clear();
            for (String item: playerScoreLives) {
                String[] stats = item.split(":");
                var entry = new Pair<String, Integer>(stats[0], Integer.parseInt(stats[1]));
                this.multiplayerScores.add(entry);
            }
        } else if(s.contains("DIE")) { //A player has lost or left
            s = s.replace("DIE ", "");
            endUser(s);
        } else if(s.contains(("BOARD"))) { //A representation of a player's GameBoard
            s=s.replace("BOARD ", "");
            updatePlayerBoard(s);
        }
    }

    /**
     * Initialises the sidebar which contains previews of all player's GameBoards
     */
    public void initialisePlayerBoards() {
        playerToGameboard = new HashMap<>();
        for (String player: players) {
            GameBoard gameBoard = new GameBoard(5,5, 75,75);
            Text name = new Text(player);
            name.getStyleClass().add("heading");
            name.setTextAlignment(TextAlignment.CENTER);
            boardSideBar.getChildren().addAll(name, gameBoard);
            playerToGameboard.put(player, gameBoard);
        }
        mainPane.setLeft(boardSideBar);
        boardSideBar.setAlignment(Pos.CENTER_LEFT);
        boardSideBar.setMaxHeight(this.gameWindow.getHeight());
    }

    /**
     * Updates GameBoards when a message is received
     * @param board
     */
    public void updatePlayerBoard(String board) {
        String player = board.split(":")[0];
        String[] values = board.split(":")[1].split(" ");
        if(players.contains(player)) {
            GameBoard gameBoard = playerToGameboard.get(player);
            int i = 0;
            for (int x = 0; x < this.game.getCols(); x++) {
                for (int y = 0; y < this.game.getRows(); y++) {
                    gameBoard.getGrid().set(x, y, Integer.parseInt(values[i]));
                    i++;
                }
            }
        }
    }
}
