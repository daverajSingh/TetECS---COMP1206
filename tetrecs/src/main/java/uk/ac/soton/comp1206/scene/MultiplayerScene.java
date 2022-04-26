package uk.ac.soton.comp1206.scene;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class MultiplayerScene extends ChallengeScene{
    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);

    protected MultiplayerGame game;

    protected VBox channelBox;
    protected VBox messagesBox;

    protected Communicator communicator;

    /**
     * Create a new MultiPlayer challenge scene
     *
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("");
    }

    @Override
    public void initialise() {
        super.initialise();
        communicator = gameWindow.getCommunicator();
    }

    @Override
    public void setupGame() {
        super.setupGame();
    }

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

        var score = new Text("Score: ");
        var scoreValue = new Text("0");
        scoreValue.textProperty().bind(game.scoreProperty().asString());
        var scoreBox = new HBox(score, scoreValue);
        score.getStyleClass().add("heading");
        scoreValue.getStyleClass().add("heading");

        var level = new Text("Level: ");
        var levelValue = new Text("1");
        levelValue.textProperty().bind(game.levelProperty().asString());
        var levelBox = new HBox(level, levelValue);
        level.getStyleClass().add("heading");
        levelValue.getStyleClass().add("heading");

        var multiplier = new Text("Multiplier: ");
        var multiplierValue = new Text("1");
        multiplierValue.textProperty().bind(game.multiplierProperty().asString());
        var multiplierBox = new HBox(multiplier, multiplierValue);
        multiplier.getStyleClass().add("heading");
        multiplierValue.getStyleClass().add("heading");

        var lives = new Text("Lives: ");
        var livesValue = new Text("3");
        livesValue.textProperty().bind(game.livesProperty().asString());
        var livesBox = new HBox(lives, livesValue);
        lives.getStyleClass().add("heading");
        livesValue.getStyleClass().add("heading");

        HBox stats = new HBox(20, scoreBox, levelBox, multiplierBox, livesBox);
        challengePane.getChildren().add(stats);
        stats.setAlignment(Pos.TOP_CENTER);
        stats.setTranslateY(20);

        var highScore = new Text("Highscore: ");
        var highScoreText = new Text();
        highScoreText.textProperty().bind(highScoreValue.asString());
        var highScoreBox = new VBox(highScore, highScoreText);
        highScore.getStyleClass().add("heading");
        highScoreText.getStyleClass().add("heading");

        challengePane.getChildren().add(highScoreBox);
        highScoreBox.setAlignment(Pos.TOP_CENTER);
        highScoreBox.setTranslateY(100);
        highScoreBox.setTranslateX(285);

        pieceBoard = new GameBoard(3,3,100,100);
        pieceBoard.setAlignment(Pos.CENTER);

        followingPieceBoard = new GameBoard(3,3,75,75);
        followingPieceBoard.setAlignment(Pos.CENTER);
        pieceBoard.setTranslateY(-10);
        pieceBoard.setTranslateX(12.5);
        pieceBoard.paintCentre();

        channelBox = new VBox();
        channelBox.setSpacing(5);
        channelBox.setPadding(new Insets(0, 30, 0, 0));
        channelBox.setAlignment(Pos.CENTER_RIGHT);
        channelBox.setMaxWidth(gameWindow.getWidth() /4);
        channelBox.setMaxHeight(gameWindow.getHeight() /4);

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

        var messageEntry = new TextField();
        var messageConfirm = new Button("Send");

        messageEntry.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode() == KeyCode.ENTER) {
                    String message = messageEntry.getText();
                    if(message != null) {
                        communicator.send("MSG " + message);
                        messageEntry.clear();
                    }
                }
            }
        });

        messageConfirm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String message = messageEntry.getText();
                if(message != null) {
                    communicator.send("MSG " + message);
                    messageEntry.clear();
                }
            }
        });

        var chatBox = new HBox(messageEntry, messageConfirm);

        var sideBar = new VBox(pieceBoard, followingPieceBoard, messagesPane, chatBox);

        sideBar.setAlignment(Pos.CENTER_RIGHT);
        sideBar.setTranslateX(-75);

        timer = new Rectangle(gameWindow.getWidth(), 10);
        var timerPane = new StackPane();
        timerPane.getChildren().add(timer);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        mainPane.setRight(sideBar);
        mainPane.setTop(timerPane);
        timerPane.setAlignment(Pos.TOP_LEFT);

        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(board);

        //Handle block on Gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);

        //Setting Piece Listener
        game.setNextPieceListener(this::nextPiece);

        game.setLineClearedListener(this::lineClear);

        game.setOnGameLoop(this::gameLoop);

        game.setGameEndListener(game -> {
            gameEnd();
            gameWindow.startScores(game);
        });

        //Setting Right Clicked Listener
        board.setOnRightClicked(this::rotate);

        pieceBoard.setOnBlockClick(this::rotate);

        followingPieceBoard.setOnBlockClick(this::swapPieces);

        scene.setOnKeyPressed(this::keyboardInput);

        game.scoreProperty().addListener(this::getHighScore);
    }

    protected void listen(String s) {
        if(s.contains("MSG")) {
            s = s.replace("MSG ", "");
            String[] messageArr = s.split(":");
            if (messageArr.length > 1) {
                Text message = new Text(messageArr[0] + " : " + messageArr[1]);
                message.getStyleClass().add("messages Text");
                messagesBox.getChildren().add(message);
            }
        }
    }

}
