package cs2110;

import com.sun.jdi.IntegerValue;
import java.awt.BorderLayout;
import java.awt.Menu;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Main class for Click-a-Dot game. Creates window with game board, score label, start button, and
 * sliders for target size and speed.
 */
public class GameMain {

    /**
     * Start the application.
     */
    public static void main(String[] args) {
        // Creation of window must occur on Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    /**
     * Create application window.
     * <ul>
     * <li>Window title is "Click-a-Dot"
     * <li>Game board is in center of window, expands to fill window size
     * <li>Score label is at top; text is centered
     * <li>Start button is at bottom
     * <li>Size slider is at right
     * <li>Speed slider is at left
     * </ul>
     * Window should be disposed when closed, and all game tasks stopped. This should be sufficient
     * for application to shut down gracefully.
     */
    private static void createAndShowGUI() {
        // Create frame.
        JFrame frame = new JFrame("Click-a-Dot");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create and add game board.
        GameComponent game = new GameComponent();
        frame.add(game);

        // Create and add score label.
        JLabel scoreLabel = new JLabel("Score: " + game.getScore(),
                SwingConstants.CENTER);
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(24.0f));

        // TODO 1: Add `scoreLabel` to top of frame.
        frame.add(scoreLabel, BorderLayout.PAGE_START);
        // See the BorderLayout tutorial [1] for example code that you can adapt.
        // [1]: https://docs.oracle.com/javase/tutorial/uiswing/layout/border.html

        // Create and add start button.
        JButton startButton = new JButton("Start");
        startButton.setFont(startButton.getFont().deriveFont(20.0f));
        // TODO 2: Add `startButton` to bottom of frame.
        frame.add(startButton, BorderLayout.PAGE_END);

        // Create and add vertical size slider.
        // Allowed target radii are 1..50 (query game board for initial radius).
        JSlider sizeSlider = new JSlider(JSlider.VERTICAL, 1, 50,
                game.getTargetRadius());
        addSliderLabels(sizeSlider, "Small", "Large");
        // Place slider in panel with label and padding.
        frame.add(makeSliderPanel(sizeSlider, "Size"), BorderLayout.WEST);

        // Create and add vertical speed slider.
        // Allowed target durations are 250..2000 ms (query game board for
        // initial duration).
        JSlider speedSlider = new JSlider(JSlider.VERTICAL, 250, 2000,
                game.getTargetTimeMillis());
        addSliderLabels(speedSlider, "Fast", "Slow");
        speedSlider.setInverted(true);
        // Place slider in panel with label and padding.
        frame.add(makeSliderPanel(speedSlider, "Speed"), BorderLayout.EAST);

        // Add menu bar
        JMenuItem saveItem = new JMenuItem("Save score");
        JMenuItem exitItem = new JMenuItem("Exit");
        // TODO 14: Add a menu bar with a "File" menu to the frame. The
        // menu items `saveItem` and `exitItem` should be accessible under the
        // "File" menu. See the Menu tutorial [1] for example code you can adapt.
        // You do not need to add the mnemonics, keyboard shortcuts, or hover over
        // descriptions shown in that tutorial.
        // [1] https://docs.oracle.com/javase/tutorial/uiswing/components/menu.html
        JMenuBar menubar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menu.add(saveItem);
        menu.add(exitItem);
        menubar.add(menu);
        frame.setJMenuBar(menubar);
        ////////////////
        // Controller
        ////////////////

        // When the start button is clicked, start a new game.
        // TODO 3: Add an ActionListener to `startButton` that starts a game.
        startButton.addActionListener(e -> game.startGame());
        // - For how to add an ActionListener to a button using a lambda (an
        // anonymous function), see the demo code from the GUI lectures.
        // - For how to start a game, review the methods of GameComponent.

        // When the game's score changes, update the score label.
        // TODO 9: Add a PropertyChangeListener to `game` that updates
        // `scoreLabel`'s text whenever the "GameScore" property changes.
        // The label text should start with "Score: ", followed by the numerical
        // score.
        game.addPropertyChangeListener(evt -> {
            if ("GameScore".equals(evt.getPropertyName())) {
                scoreLabel.setText("Score: " + evt.getNewValue());
            }
        });

        // When size slider is adjusted, update target radius in game.
        // TODO 10: Add a ChangeListener to `sizeSlider` that sets the game's
        // target radius to the slider's current value.
        // Method `JSlider.getValue()` gets the slider's current value.
        // [1]:
        // https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/javax/swing/JSlider.html
        sizeSlider.addChangeListener(e -> {
            game.setTargetRadius(sizeSlider.getValue());
        });

        // When speed slider is adjusted, update target duration in game.
        // TODO 11: Add a ChangeListener to `speedSlider` that sets the game's
        // target duration to the slider's current value.
        speedSlider.addChangeListener(e -> {
            game.setTargetTimeMillis(speedSlider.getValue());
        });

        // When "Save" menu item is activated, open file dialog and append score
        // to chosen file.
        saveItem.addActionListener((ActionEvent ae) -> saveScore(frame, game.getScore()));

        // When "Exit" menu item is activated, dispose of the JFrame.
        exitItem.addActionListener((ActionEvent ae) -> frame.dispose());

        // Stop game when window is closed to ensure that game background tasks
        // do not hold up application shutdown.
        // Use an anonymous subclass of WindowAdapter to avoid having to handle
        // other window events.
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                game.stopGame();
            }
        });

        // Compute ideal window size and show window.
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Label `slider`'s minimum value with `minLabel` and its maximum value with `maxLabel`.
     */
    private static void addSliderLabels(JSlider slider, String minLabel,
            String maxLabel) {
        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        // TODO 12:
        labels.put(slider.getMinimum(),new JLabel(minLabel));
        labels.put(slider.getMaximum(),new JLabel(maxLabel));
        slider.setLabelTable(labels);
        slider.setPaintLabels(true);

        // 1. Put a mapping in dictionary `labels`. The key for the mapping should be the slider's
        // minimum value, which
        // can be found with `JSlider.getMinimum`. The value for the mapping should be a JLabel
        // with the text from `minLabel`.
        // 2. Put another mapping in the dictionary for the slider's maximum value, with the text
        // from `maxLabel`.
        // 3. Show the labels on the slider using `JSlider.setLabelTable` and `.setPaintLabels`. See
        // the JSlider tutorial [1]
        // for example code that you can adapt. Look under the heading "Customizing Labels on a
        // Slider".
        // [1] https://docs.oracle.com/javase/tutorial/uiswing/components/slider.html

    }

    /**
     * Place `slider` in a new padded panel with top label `title` and return the panel.
     */
    private static JComponent makeSliderPanel(JSlider slider, String title) {
        // TODO 13:
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        JLabel newTitle = new JLabel(title);
        newTitle.setFont(newTitle.getFont().deriveFont(16.0f));
        p.add(newTitle, BorderLayout.PAGE_START);
        p.add(slider,BorderLayout.LINE_END);

        // 1. Construct a new JPanel with a BorderLayout manager. See the JPanel tutorial [1] under
        // the
        // heading "Setting the Layout Manager" for example code that you can adapt.
        // 2. Add an EmptyBorder around the panel to provide at least 4px of
        // padding on all sides. Use an overload of `BorderFactory.createEmptyBorder` [2] to create
        // the border.
        // See the Border tutorial [3] for example code that you can adapt.
        // 3. Add a label to the top of the panel with the text in `title`
        // (centered). Set a font size of 16 points. For an example of setting the font size,
        // look back to the beginning of `createAndShowGUI`.
        // 4. Add `slider` to the panel to fill the remaining space below the
        // label.
        // 5. Return the panel.
        // [1]: https://docs.oracle.com/javase/tutorial/uiswing/components/panel.html
        // [2]:
        // https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/javax/swing/BorderFactory.html
        // [3]: https://docs.oracle.com/javase/tutorial/uiswing/components/border.html
        return p;  // Replace this line with one that returns your panel
    }

    /**
     * Append a line containing `score` to a user-selected file, using `frame` as the parent of any
     * dialogs. Show an error dialog if a problem occurs when writing the file.
     */
    private static void saveScore(JFrame frame, int score) {

        // TODO 15:
        // * Show a "save file" dialog [1].
        // * If the user selects a file, write the value in `score` on a new
        // line of text at the end of the file, retaining its former contents
        // (see handout).
        // * If a problem occurs when opening or writing to the file, show an
        // error dialog with the class of the exception as its title and the
        // class of the exception and exception's message as its text [2].
        // [1] https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
        // [2] https://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html

        //Create a file chooser

    }
}
