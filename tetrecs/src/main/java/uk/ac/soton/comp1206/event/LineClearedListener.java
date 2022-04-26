package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import java.util.Set;

/**
 * The LineClearedListener is used to link the Game and UI, and is triggered when a line has been cleared so that
 * animations can be played
 */
public interface LineClearedListener {
    /**
     * Takes in a set of GameBlockCoordinates and animates each individual block
     * @param gameBlockCoordinateSet
     */
    void lineClear(Set<GameBlockCoordinate> gameBlockCoordinateSet);
}
