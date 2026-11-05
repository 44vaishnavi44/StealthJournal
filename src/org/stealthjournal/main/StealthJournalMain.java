
package org.stealthjournal.main;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import orgt.stealthjournal.util.Constants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Insets;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main entry point and primary window manager of StealthJournal.
 *
 * This class creates the initial Swing user interface using
 * a BorderLayout structure.
 *
 * The interface consists of:
 * 1. Top panel containing the entry key and stealth status.
 * 2. Centre panel containing the scrollable diary canvas.
 * 3. Bottom panel containing interface buttons and a
 *    copyright/stealth label.
 *
 * The diary canvas remains non-editable during initialisation.
 *
 * @author StealthJournal Development Team
 * @version 1.0
 */
public final class StealthJournalMain extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 600;

    private static final int MINIMUM_WIDTH = 720;
    private static final int MINIMUM_HEIGHT = 480;

    private static final Font CANVAS_FONT =
            new Font("Monospaced", Font.PLAIN, 14);

    private static final Font BUTTON_FONT =
            new Font("Arial", Font.PLAIN, 12);

    private static final Font FOOTER_FONT =
            new Font("Arial", Font.PLAIN, 10);

    private final JTextField entryKeyField;

    private final JLabel stealthStatusLabel;

    private final JTextArea diaryTextArea;

    private final JButton helpButton;

    private final JButton refreshButton;

    private final JButton deceptiveButton;

    private final JButton timestampButton;

    private final JLabel copyrightLabel;


    /**
     * Constructs the main StealthJournal window.
     */
    public StealthJournalMain() {

        super(Constants.DIARY_WINDOW_TITLE);

        entryKeyField = new JTextField(6);

        stealthStatusLabel =
                new JLabel("Stealth Status: READ-ONLY");

        diaryTextArea = new JTextArea();

        helpButton = new JButton("Help");

        refreshButton = new JButton("Refresh");

        deceptiveButton = new JButton("Click to View");

        timestampButton = new JButton("Insert Timestamp");

        copyrightLabel = new JLabel(
                "Copyright © StealthJournal. All Rights Reserved.",
                SwingConstants.CENTER
        );

        initializeWindow();

        initializeComponents();

        buildMainLayout();

    }


    /**
     * Configures the main JFrame properties.
     */
    private void initializeWindow() {

        setTitle(Constants.DIARY_WINDOW_TITLE);

        setDefaultCloseOperation(
                WindowConstants.EXIT_ON_CLOSE
        );

        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        setMinimumSize(
                new java.awt.Dimension(
                        MINIMUM_WIDTH,
                        MINIMUM_HEIGHT
                )
        );

        setResizable(true);

        setLocationRelativeTo(null);

        setLayout(
                new BorderLayout(8, 8)
        );

        getRootPane().setBorder(
                BorderFactory.createEmptyBorder(
                        8, 10, 8, 10
                )
        );

    }


    /**
     * Initializes and styles individual Swing components.
     */
    private void initializeComponents() {

        // Diary canvas configuration.

        diaryTextArea.setFont(CANVAS_FONT);

        diaryTextArea.setLineWrap(true);

        diaryTextArea.setWrapStyleWord(true);

        diaryTextArea.setEditable(false);

        diaryTextArea.setMargin(
                new Insets(12, 12, 12, 12)
        );

        diaryTextArea.setTabSize(4);

        diaryTextArea.setBackground(Color.WHITE);

        diaryTextArea.setForeground(Color.BLACK);

        diaryTextArea.setBorder(
                BorderFactory.createEmptyBorder()
        );


        // Entry key field configuration.

        entryKeyField.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        entryKeyField.setToolTipText(
                "Numeric entry key"
        );


        // Stealth status configuration.

        stealthStatusLabel.setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        stealthStatusLabel.setForeground(
                new Color(150, 40, 40)
        );


        // Button configuration.

        JButton[] buttons = {
                helpButton,
                refreshButton,
                deceptiveButton,
                timestampButton
        };

        for (JButton button : buttons) {

            button.setFont(BUTTON_FONT);

            button.setFocusPainted(false);

            button.setCursor(
                    new Cursor(Cursor.HAND_CURSOR)
            );

        }


        // Copyright label configuration.

        copyrightLabel.setFont(FOOTER_FONT);

        copyrightLabel.setForeground(
                Color.DARK_GRAY
        );

        copyrightLabel.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );


        // Timestamp button is the temporary UI test control.

        timestampButton.addActionListener(
                event -> insertTimestamp()
        );

    }


    /**
     * Constructs the main interface using BorderLayout.
     */
    private void buildMainLayout() {

        JPanel topPanel = createTopPanel();

        JPanel centerPanel = createCenterPanel();

        JPanel bottomPanel = createBottomPanel();


        add(
                topPanel,
                BorderLayout.NORTH
        );

        add(
                centerPanel,
                BorderLayout.CENTER
        );

        add(
                bottomPanel,
                BorderLayout.SOUTH
        );

    }


    /**
     * Creates the top panel containing the entry key
     * and stealth status.
     *
     * @return configured top panel
     */
    private JPanel createTopPanel() {

        JPanel topPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        6
                )
        );


        JLabel entryKeyLabel = new JLabel(
                "Entry Key:"
        );

        topPanel.add(entryKeyLabel);

        topPanel.add(entryKeyField);


        // Provides spacing between the two top controls.

        topPanel.add(Box.createHorizontalStrut(25));

        topPanel.add(stealthStatusLabel);


        topPanel.setBorder(
                BorderFactory.createEtchedBorder()
        );


        return topPanel;

    }


    /**
     * Creates the centre panel containing the diary canvas.
     *
     * @return configured centre panel
     */
    private JPanel createCenterPanel() {

        JPanel centerPanel = new JPanel(
                new BorderLayout()
        );


        JScrollPane scrollPane = new JScrollPane(
                diaryTextArea,

                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,

                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );


        scrollPane.setBorder(
                BorderFactory.createEtchedBorder()
        );


        centerPanel.add(
                scrollPane,
                BorderLayout.CENTER
        );


        return centerPanel;

    }


    /**
     * Creates the bottom panel containing interface buttons
     * and the copyright/stealth label.
     *
     * @return configured bottom panel
     */
    private JPanel createBottomPanel() {

        JPanel bottomPanel = new JPanel(
                new BorderLayout(5, 5)
        );


        JPanel buttonPanel = new JPanel(
                new GridLayout(
                        1,
                        4,
                        8,
                        0
                )
        );


        buttonPanel.add(helpButton);

        buttonPanel.add(refreshButton);

        buttonPanel.add(deceptiveButton);

        buttonPanel.add(timestampButton);


        bottomPanel.add(
                buttonPanel,
                BorderLayout.CENTER
        );


        JPanel footerPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.CENTER,
                        5,
                        4
                )
        );


        footerPanel.add(copyrightLabel);


        bottomPanel.add(
                footerPanel,
                BorderLayout.SOUTH
        );


        return bottomPanel;

    }


    /**
     * Inserts the current date and time at the
     * current cursor position in the diary canvas.
     *
     * This method is used to test timestamp insertion
     * during the initial UI development phase.
     */
    private void insertTimestamp() {

        LocalDateTime currentDateTime =
                LocalDateTime.now();


        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "EEEE, dd-MMMM-yyyy, hh:mm a"
                );


        String formattedTimestamp =
                currentDateTime.format(formatter);


        String lineSeparator =
                System.lineSeparator();


        String timestampBlock =
                lineSeparator
                + lineSeparator
                + "=================================================="
                + lineSeparator
                + "TIMESTAMP: " + formattedTimestamp
                + lineSeparator
                + "=================================================="
                + lineSeparator;


        int cursorPosition =
                diaryTextArea.getCaretPosition();


        diaryTextArea.insert(
                timestampBlock,
                cursorPosition
        );


        diaryTextArea.setCaretPosition(
                cursorPosition + timestampBlock.length()
        );

    }


    /**
     * Returns the diary text area.
     *
     * This accessor allows subsequent UI components
     * to interact with the main diary canvas.
     *
     * @return diary JTextArea
     */
    public JTextArea getDiaryTextArea() {

        return diaryTextArea;

    }


    /**
     * Returns the entry key field.
     *
     * @return entry key JTextField
     */
    public JTextField getEntryKeyField() {

        return entryKeyField;

    }


    /**
     * Returns the current stealth status label.
     *
     * @return stealth status JLabel
     */
    public JLabel getStealthStatusLabel() {

        return stealthStatusLabel;

    }


    /**
     * Application entry point.
     *
     * Initialises the Swing interface on the
     * Event Dispatch Thread.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {

        if (GraphicsEnvironment.isHeadless()) {

            System.err.println(
                    "StealthJournal requires a graphical environment."
            );

            return;

        }


        try {

            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );

        } catch (Exception exception) {

            System.err.println(
                    "Unable to apply system look and feel: "
                    + exception.getMessage()
            );

        }


        SwingUtilities.invokeLater(() -> {

            StealthJournalMain application =
                    new StealthJournalMain();

            application.setVisible(true);

        });

    }

}
