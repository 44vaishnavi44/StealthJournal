
package org.stealthjournal.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

/**
 * Provides the main diary writing canvas for StealthJournal.
 *
 * Responsibilities:
 * - Display the diary text area.
 * - Provide scrolling for lengthy diary entries.
 * - Keep the canvas read-only by default.
 * - Insert automatically formatted timestamps.
 * - Provide controlled access to diary text and editing state.
 *
 * Timestamp format follows the StealthJournal SRS:
 * EEEE, dd-MMMM-yyyy, hh:mm a
 */
public class DiaryCanvasPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "EEEE, dd-MMMM-yyyy, hh:mm a",
                    Locale.ENGLISH
            );

    private static final String TIMESTAMP_PREFIX = "TIMESTAMP: ";

    private final JTextArea diaryTextArea;

    private final JScrollPane scrollPane;

    /**
     * Creates the diary canvas.
     *
     * The canvas starts in read-only mode.
     */
    public DiaryCanvasPanel() {

        setLayout(new BorderLayout());

        diaryTextArea = new JTextArea();

        diaryTextArea.setFont(
                new Font(Font.MONOSPACED, Font.PLAIN, 14)
        );

        diaryTextArea.setLineWrap(true);
        diaryTextArea.setWrapStyleWord(true);

        diaryTextArea.setEditable(false);

        diaryTextArea.setTabSize(4);

        diaryTextArea.setMargin(
                new java.awt.Insets(12, 12, 12, 12)
        );

        diaryTextArea.setCaretPosition(0);

        diaryTextArea.getAccessibleContext()
                .setAccessibleName("StealthJournal Diary Canvas");

        diaryTextArea.getAccessibleContext()
                .setAccessibleDescription(
                        "Main text area for writing diary entries."
                );

        scrollPane = new JScrollPane(
                diaryTextArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder()
        );

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Returns the JTextArea used by the diary canvas.
     *
     * This allows other application components to access
     * the text area when necessary.
     *
     * @return diary JTextArea
     */
    public JTextArea getTextArea() {
        return diaryTextArea;
    }

    /**
     * Returns the complete diary text.
     *
     * @return current diary content
     */
    public String getText() {
        return diaryTextArea.getText();
    }

    /**
     * Replaces the existing diary content.
     *
     * @param text new diary content
     * @throws IllegalArgumentException if text is null
     */
    public void setText(String text) {

        if (text == null) {
            throw new IllegalArgumentException(
                    "Diary text cannot be null."
            );
        }

        diaryTextArea.setText(text);
        diaryTextArea.setCaretPosition(
                diaryTextArea.getDocument().getLength()
        );
    }

    /**
     * Changes the editing permission of the diary canvas.
     *
     * The authentication system will use this method
     * to unlock or lock the diary.
     *
     * @param editable true to enable editing,
     *                 false to make the canvas read-only
     */
    public void setEditable(boolean editable) {
        diaryTextArea.setEditable(editable);
    }

    /**
     * Checks whether the diary canvas is currently editable.
     *
     * @return true if editing is enabled
     */
    public boolean isEditable() {
        return diaryTextArea.isEditable();
    }

    /**
     * Inserts a chronological timestamp header at the
     * current caret position.
     *
     * Timestamp format:
     * TIMESTAMP: Wednesday, 29-July-2026, 05:15 PM
     *
     * The method uses the system's current date and time.
     *
     * @return the generated timestamp header
     */
    public String insertTimestamp() {

        LocalDateTime currentDateTime =
                LocalDateTime.now();

        String formattedDateTime =
                currentDateTime.format(TIMESTAMP_FORMATTER);

        String timestampHeader =
                TIMESTAMP_PREFIX + formattedDateTime;

        String insertionText =
                timestampHeader
                + System.lineSeparator()
                + System.lineSeparator();

        int caretPosition =
                diaryTextArea.getCaretPosition();

        diaryTextArea.insert(
                insertionText,
                caretPosition
        );

        diaryTextArea.setCaretPosition(
                caretPosition + insertionText.length()
        );

        return timestampHeader;
    }

    /**
     * Clears all text from the diary canvas.
     *
     * This method is useful for future security and
     * workspace-management operations.
     */
    public void clearCanvas() {

        diaryTextArea.setText("");
        diaryTextArea.setCaretPosition(0);
    }

    /**
     * Moves the text cursor to the end of the diary.
     */
    public void moveCaretToEnd() {

        diaryTextArea.setCaretPosition(
                diaryTextArea.getDocument().getLength()
        );
    }

    /**
     * Returns the scroll pane containing the diary canvas.
     *
     * @return diary scroll pane
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }
}
