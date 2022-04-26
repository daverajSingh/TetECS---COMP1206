package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.Game;

/**
 * The GameEndListener is used to detect when the game has ended, so that scenes can be changed
 */
public interface GameEndListener {
    /**
     * Handles when a game has ended
     * @param game
     */
    void gameEnd(Game game);
}
