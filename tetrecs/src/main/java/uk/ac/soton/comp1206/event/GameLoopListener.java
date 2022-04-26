package uk.ac.soton.comp1206.event;

/**
 * The GameLoopListener is used to link the timer in game to a UI timer element.
 */
public interface GameLoopListener {
    /**
     * Sets the Delay for the next Timer UI element, linking both the game and UI timers
     * @param delay
     */
    void gameLoop(int delay);
}
