package cs2110;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.Timer;


/**
 * GUI component for Click-a-Dot game board. Maintains and visualizes game state and responds to
 * mouse inputs. Allows programmatic control of game settings and property access to game state.
 */
public class GameComponent extends JPanel implements MouseListener {

    /**
     * Number of targets to show during one game.
     */
    private int maxTargets = 10;

    /**
     * Duration each target is shown [ms].
     */
    private int targetTimeMillis = 1500;

    /**
     * Encapsulates state and controls for current target.
     */
    private Target target = new Target();

    /**
     * Timer to trigger changing targets.
     */
    private Timer timer;

    /**
     * Whether a game is currently being played.
     */
    private boolean isActive = false;

    /**
     * Number of targets that have been shown during current game (including the current target). If
     * a game is not currently being played, is the number of targets shown during the previous game
     * (0 if no games have been played).
     */
    private int targetCount = 0;

    /**
     * Number of targets successfully hit by player during the current game. If a game is not
     * currently being played, is the number of targets hit during the previous game (0 if no games
     * have been played).
     */
    private int score = 0;

    /**
     * Construct a new GameComponent with default settings.
     */
    public GameComponent() {
        // Set up timer with initial delay of 0
        timer = new Timer(targetTimeMillis, (ActionEvent e) -> timeout());
        timer.setInitialDelay(0);
        timer.setCoalesce(true);

        // This component reacts to mouse events.
        addMouseListener(this);

        // Set a recommended size for the game board
        setPreferredSize(new Dimension(480, 360));
    }

    /**
     * Start a new game using current settings. Progress from any previous or ongoing game is reset.
     * Request a repaint.
     */
    public void startGame() {
        targetCount = 0;
        setScore(0);
        isActive = true;
        timer.restart();
        repaint();
    }

    /**
     * Stop current game. Takes effect immediately. Requests a repaint. All background tasks are
     * cancelled when game is stopped.
     */
    public void stopGame() {
        timer.stop();
        isActive = false;
        repaint();
    }

    /**
     * Update the game when the timer "goes off". If the game is inactive, does nothing. If the game
     * is active, and if the maximum number of targets has already been shown, stops the game.
     * Otherwise, respawns the target within the current component area, increments the target
     * count, and requests a repaint.
     */
    private void timeout() {
        if (targetCount >= maxTargets) {
            stopGame();
        } else{
            target.respawn(getWidth(),getHeight());
            targetCount++;
            repaint();
        }

        if(!isActive){
            return;
        }

    }

    /**
     * Query the current score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Change the current score to `newScore` and notify observers.
     */
    private void setScore(int newScore) {
        int oldScore = score;
        score = newScore;
        firePropertyChange("GameScore", oldScore, newScore);
    }

    /**
     * Query the radius of the target dot [px].
     */
    public int getTargetRadius() {
        return target.radius;
    }

    /**
     * Change the radius of the target dot to `r` px. Takes effect immediately (triggers a repaint
     * request).
     * <p>
     * Precondition: {@code r > 0}
     */
    public void setTargetRadius(int r) {
        target.radius = r;
        repaint();
    }

    /**
     * Query the duration each target is shown [ms].
     */
    public int getTargetTimeMillis() {
        return targetTimeMillis;
    }

    /**
     * Change the duration each target is shown to `t` ms. Does not affect the currently-shown
     * target (if any).
     * <p>
     * Precondition: {@code t >= 0}
     */
    public void setTargetTimeMillis(int t) {
        targetTimeMillis = t;
        timer.setDelay(targetTimeMillis);
    }

    /**
     * Visualize the current game state by painting on `g`. If the game is inactive, fills the
     * component area with black. If the game is active, draws the target on top of the default
     * JPanel background.
     */
    @Override
    public void paintComponent(Graphics g) {
        // Paint the default background. Keep this as the first line of the method.
        super.paintComponent(g);

        if(isActive){
            target.paintDot(g);
        }else{
            g.setColor(Color.BLACK);
            g.fillRect(0,0,getWidth(), getHeight());
        }
    }

    /**
     * Handle a mouse button being pressed. If the game is inactive, does nothing. If the game is
     * active, and if the press was a new target hit, increments score by 1, notifies observers, and
     * requests a repaint.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if(isActive){
            int mouseX = e.getX();
            int mouseY = e.getY();
            if(target.checkHit(mouseX,mouseY)){
                setScore(getScore()+1);
                repaint();
            }
        }
    }

    //unused
    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Represents a dot to be clicked. Maintains current position and size and whether it has
     * already been "hit" in its current location. Able to check clicks for hits, move to a new
     * location, and paint itself.
     */
    private static class Target {

        /**
         * x-coordinate of current center position [px].
         */
        int x;

        /**
         * y-coordinate of current center position [px].
         */
        int y;

        /**
         * Radius of circular dot representing the target [px].
         */
        int radius = 15;

        /**
         * Whether this has been "hit" in its current location.
         */
        private boolean isHit;

        /**
         * Generate random numbers to use when choosing new location.
         */
        private Random rng = new Random();

        /**
         * Paint dot on provided Graphics `g`. Dot is a circle with our current radius, centered at
         * our current position. Circle is filled red if we have been hit, otherwise blue.
         */
        void paintDot(Graphics g) {

            if(isHit){
                g.setColor(Color.RED);
            }else{
                g.setColor(Color.BLUE);
            }
            g.fillOval(x,y,2*radius,2*radius);
        }

        /**
         * Clip `coord` to be within `[radius, max - radius]`, if possible. Otherwise, return
         * `coord` unchanged.
         */
        int clip(int coord, int max) {
            if (2 * radius > max) {
                return coord;
            }
            if (coord < radius) {
                return radius;
            } else if (coord > max - radius) {
                return max - radius;
            } else {
                return coord;
            }
        }

        /**
         * Move target to a random location and reset "hit" state. xMax and yMax are the (inclusive)
         * upper bounds of the x- and y-coordinates of the bounding box of the new position [px];
         * lower bound is 0 (inclusive).
         */
        void respawn(int xMax, int yMax) {
            x = clip(rng.nextInt(xMax + 1), xMax);
            y = clip(rng.nextInt(yMax + 1), yMax);
            isHit = false;
        }

        /**
         * Return true and set state to "hit" if coordinates (cx,cy) are within this target's
         * circular area and it was not already "hit"; return false otherwise.
         */
        boolean checkHit(int cx, int cy) {
            if (!isHit) {
                int dx = cx - (x + radius); // Horizontal distance from the center
                int dy = cy - (y + radius); // Vertical distance from the center
                int distanceSquared = dx * dx + dy * dy; // Squared distance

                if (distanceSquared <= radius * radius) {
                    isHit = true;
                    return true;
                }
            }
            return false;
        }
    }
}
