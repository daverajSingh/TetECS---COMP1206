package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.ArrayList;

/**
 * A ScoresList will hold and display a list of names and associated scores.
 * It extends VBox to show a list of scores.
 * It is bound to an observable list property, which when it is updated, will update the localScores list property in
 * ScoresList, creating a leaderboard.
 */
public class ScoresList extends VBox {

    protected SimpleListProperty<Pair<String, Integer>> localScores = new SimpleListProperty<>();

    public ScoresList() {
        this.localScores.addListener(this::updateScores); //Listener detects when there is a change to the listProperty
        localScores.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
    }

    /**
     * Listener Method
     * Renders the new scores given to the localScores list
     * @param observableValue
     * @param oldVal
     * @param newVal
     */
    protected void updateScores(ObservableValue<?  extends ObservableList<Pair<String, Integer>>> observableValue, ObservableList<Pair<String, Integer>> oldVal,  ObservableList<Pair<String, Integer>> newVal) {
        this.renderScores(newVal);
    }

    /**
     * renderScores will first clear the VBox, then iterate through the given List, creating a Text item that will
     * display the name of the player and their score, then animating the text item.
     * @param scores
     */
    protected void renderScores(ObservableList<Pair<String, Integer>> scores) {
        this.getChildren().clear();
        int x = 1;
        for(Pair pair: scores) {
            Text scoreItem = new Text(pair.getKey() + " - " + pair.getValue());
            scoreItem.getStyleClass().add("scorelist");
            this.getChildren().add(scoreItem);
            this.reveal(scoreItem);
            x++;
            if(x==11){ //Only shows the top 10 scores
                break;
            }
        }
    }

    /**
     * Animates the given Text item to fade in once, and then remain shown to the user
     * @param item
     */
    protected void reveal(Text item) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), item);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.setCycleCount(1);
        fadeTransition.setAutoReverse(false);
        fadeTransition.play();
    }

    /**
     * Returns the SimpleListProperty so that it can be bound to
     * @return
     */
    public ListProperty<Pair<String, Integer>> listProperty() {
        return this.localScores;
    }

}
