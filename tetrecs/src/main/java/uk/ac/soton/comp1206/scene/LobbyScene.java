package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.media.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.*;


public class LobbyScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    private Multimedia multimedia = new Multimedia();

    protected Timer timer;

    protected Communicator communicator;

    protected VBox channelNames = new VBox();

    protected Button nickName;
    protected Button leaveChannel;
    protected Button startGame;

    protected VBox channelBox;
    protected VBox messagesBox;
    protected GridPane players;

    protected BorderPane borderPane;

    protected Text channelText;


    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        this.scene = gameWindow.getScene();
    }

    @Override
    public void initialise() {
        timer = new Timer();
        this.scene.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ESCAPE) {
                gameWindow.startMenu();
                logger.info("Escape Pressed");
                communicator.send("PART");
            }
        });
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> communicator.send("LIST"));
            }
        },1000, 3000);
        communicator = gameWindow.getCommunicator();
        communicator.addListener(message -> Platform.runLater(() -> listen(message.trim())));
    }


    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var lobbyPane = new StackPane();
        lobbyPane.setMaxWidth(gameWindow.getWidth());
        lobbyPane.setMaxHeight(gameWindow.getHeight());
        lobbyPane.getStyleClass().add("lobby-background");
        root.getChildren().add(lobbyPane);

        borderPane = new BorderPane();
        lobbyPane.getChildren().add(borderPane);
        borderPane.setMaxSize(lobbyPane.getMaxWidth(), lobbyPane.getMaxHeight());

        var channelUI = new VBox();
        borderPane.setLeft(channelUI);

        var startChannel = new Button("Start Channel");
        startChannel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                var dialog = new TextInputDialog();
                dialog.setTitle("New Channel");
                dialog.setContentText("Enter Name For New Channel");
                Optional<String> result = dialog.showAndWait();
                if(result.isPresent()) {
                    communicator.send("CREATE " + result.get());
                } else {
                    communicator.send("CREATE channel");
                }
            }
        });

        nickName = new Button("Edit NickName");
        nickName.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                var dialog = new TextInputDialog();
                dialog.setTitle("Enter Nickname");
                dialog.setContentText("Enter new Nickname: ");
                Optional<String> result = dialog.showAndWait();
                if(result.isPresent()) {
                    communicator.send("NICK " + result.get());
                } else {
                    communicator.send("NICK player");
                }
            }
        });

        leaveChannel = new Button("Leave");
        leaveChannel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                communicator.send("PART");
                leaveChannel.setVisible(false);
                nickName.setVisible(false);
                channelBox.setVisible(false);
                channelText.setText(" ");
                messagesBox.getChildren().clear();
            }
        });

        channelBox = new VBox();
        channelBox.setSpacing(5);
        channelBox.setPadding(new Insets(0, 30, 0, 0));
        channelBox.setAlignment(Pos.CENTER_RIGHT);
        channelBox.setMaxWidth(gameWindow.getWidth());
        channelBox.setMaxHeight(gameWindow.getHeight());

        var messagesPane = new BorderPane();
        messagesPane.setPrefSize(gameWindow.getWidth()/2, gameWindow.getHeight()/2);

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

        startGame = new Button("Start Game");
        startGame.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                communicator.send("START");
            }
        });

        players = new GridPane();
        players.setPrefWidth(currentMessages.getPrefWidth());

        var chatBox = new HBox(messageEntry, messageConfirm);
        var buttonsHBox = new HBox(nickName, leaveChannel);

        channelText = new Text();
        channelText.getStyleClass().add("heading");

        channelBox.getChildren().addAll(channelText, buttonsHBox, messagesPane, chatBox, startGame, players);

        this.borderPane.setRight(channelBox);
        channelBox.setVisible(false);

        Button[] buttons = new Button[]{startChannel, nickName, leaveChannel, startGame};
        for (Button node: buttons) {
            node.hoverProperty().addListener((ov, oldValue, newValue) -> {
                if (newValue) {
                    node.setStyle("-fx-text-fill: yellow");
                } else {
                    node.setStyle("-fx-text-fill: white");
                }
            });
            node.setStyle("-fx-text-fill: white");
            node.getStyleClass().add("menuItem");
            if(node == startGame) {
                node.getStyleClass().clear();
                node.getStyleClass().add("smallMenuItem");
            }
            node.setBackground(null);
        }

        channelUI.getChildren().addAll(startChannel, channelNames);
    }

    protected void listen(String s) {
        if (s.contains("CHANNELS")) {
            channelNames.getChildren().clear();
            s = s.replace("CHANNELS ", "");
            String[] channelArray = s.split("\n");
            for (String channel: channelArray) {
                Text textChannel = new Text(channel);

                textChannel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        communicator.send("JOIN " + channel);
                    }
                });

                textChannel.hoverProperty().addListener((ov, oldValue, newValue) -> {
                    if (newValue) {
                        textChannel.setStyle("-fx-text-fill: yellow");
                    } else {
                        textChannel.setStyle("-fx-text-fill: white");
                    }
                });
                textChannel.getStyleClass().add("channelItem");
                channelNames.getChildren().add(textChannel);
            }
        } else if (s.contains("JOIN")) {
            String[] channelName = s.split(" ");
            channelJoin(channelName[1]);
        } else if(s.contains("MSG")) {
            s = s.replace("MSG ", "");
            String[] messageArr = s.split(":");
            if(messageArr.length > 1) {
                Text message = new Text(messageArr[0] + " : " + messageArr[1]);
                message.getStyleClass().add("messages Text");
                messagesBox.getChildren().add(message);
            }
        } else if (s.contains("HOST")) {
            startGame.setVisible(true);
        } else if (s.contains("USERS")){
            s=s.replace("USERS ", "");
            setPlayers(s);
        }
    }

    protected void channelJoin(String channelName) {
        nickName.setVisible(true);
        leaveChannel.setVisible(true);
        channelBox.setVisible(true);
        startGame.setVisible(false);
        channelText.setText("Current Channel: " + channelName);
    }

    protected void setPlayers(String players){
        this.players.getChildren().clear();
        String[] playerArr = players.split("\n");
        for (int x=0; x<playerArr.length; x++) {
            Text text = new Text(playerArr[x]);
            text.getStyleClass().add("heading");
            if(x < 3) {
                this.players.add(text, x, 0);
            } else if(x < 5){
                this.players.add(text, x-3, 1);
            } else {
                this.players.add(text, x-6, 2);
            }
        }
        this.players.setVgap(10);
        this.players.setHgap(10);
    }

}
