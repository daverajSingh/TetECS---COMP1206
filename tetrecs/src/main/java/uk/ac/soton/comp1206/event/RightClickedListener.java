package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;

/**
 * The RightClickedListener is used to detect when a GameBoard is RightClicked, allowing for rotation of pieces
 */
public interface RightClickedListener {
    /**
     * Handles when a GameBoard has been RightClicked
     * @param block
     */
    void rightClick(GameBlock block);
}
