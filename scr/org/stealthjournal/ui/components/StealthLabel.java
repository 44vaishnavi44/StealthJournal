
package org.stealthjournal.ui.components;

import org.stealthjournal.auth.AuthenticationManager;
import org.stealthjournal.ui.AuthPromptDialog;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.Cursor;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Provides a hidden text-based authentication gate for StealthJournal.
 *
 * The label looks like an ordinary copyright or informational label.
 * Clicking it opens the secondary authentication dialog.
 *
 * The diary canvas remains non-editable until authentication succeeds.
 */
public class StealthLabel extends JLabel {

    private static final long serialVersionUID = 1L;

    private final Window parentWindow;
    private final AuthenticationManager authenticationManager;
    private final JTextArea diaryTextArea;

    private boolean authenticationInProgress;

    /**
     * Creates a StealthLabel with the default copyright text.
     *
     * @param parentWindow parent window of the authentication dialog
     * @param authenticationManager authentication manager
     * @param diaryTextArea diary text area to unlock
     */
    public StealthLabel(
            Window parentWindow,
            AuthenticationManager authenticationManager,
            JTextArea diaryTextArea) {

        this(
                "© StealthJournal",
                parentWindow,
                authenticationManager,
                diaryTextArea
        );
    }

    /**
     * Creates a StealthLabel with custom display text.
     *
     * @param text ordinary text displayed by the label
     * @param parentWindow parent window of the authentication dialog
     * @param authenticationManager authentication manager
     * @param diaryTextArea diary text area to unlock
     */
    public StealthLabel(
            String text,
            Window parentWindow,
            AuthenticationManager authenticationManager,
            JTextArea diaryTextArea) {

        super(text);

        if (parentWindow == null) {
            throw new IllegalArgumentException(
                    "Parent window must not be null."
            );
        }

        if (authenticationManager == null) {
            throw new IllegalArgumentException(
                    "AuthenticationManager must not be null."
            );
        }

        if (diaryTextArea == null) {
            throw new IllegalArgumentException(
                    "Diary text area must not be null."
            );
        }

        this.parentWindow = parentWindow;
        this.authenticationManager = authenticationManager;
        this.diaryTextArea = diaryTextArea;

        initializeLabel();
        registerMouseListener();
    }

    /**
     * Configures the appearance of the label.
     */
    private void initializeLabel() {

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        setFocusable(true);

        setToolTipText(null);

        setOpaque(false);

        getAccessibleContext().setAccessibleDescription(
                "Application information label."
        );

        diaryTextArea.setEditable(false);
    }

    /**
     * Registers mouse and keyboard interaction for the hidden gate.
     */
    private void registerMouseListener() {

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent event) {

                if (SwingUtilities.isLeftMouseButton(event)
                        && event.getClickCount() == 1) {

                    openAuthenticationDialog();
                }
            }
        });

        addKeyListener(new java.awt.event.KeyAdapter() {

            @Override
            public void keyPressed(java.awt.event.KeyEvent event) {

                if (event.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER
                        || event.getKeyCode()
                        == java.awt.event.KeyEvent.VK_SPACE) {

                    openAuthenticationDialog();
                }
            }
        });
    }

    /**
     * Opens the modal authentication dialog.
     *
     * Successful authentication unlocks the diary canvas.
     * Failed or cancelled authentication keeps the canvas locked.
     */
    private void openAuthenticationDialog() {

        if (authenticationInProgress) {
            return;
        }

        if (diaryTextArea.isEditable()) {
            return;
        }

        authenticationInProgress = true;

        try {

            if (!authenticationManager.hasCredentials()) {

                JOptionPane.showMessageDialog(
                        parentWindow,
                        "Authentication credentials are not configured.",
                        "Authentication Unavailable",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            AuthPromptDialog authenticationDialog =
                    new AuthPromptDialog(
                            parentWindow,
                            authenticationManager
                    );

            authenticationDialog.setVisible(true);

            if (authenticationDialog.isAuthenticated()) {

                unlockDiaryCanvas();

            } else {

                keepDiaryCanvasLocked();
            }

            authenticationDialog.dispose();

        } catch (RuntimeException exception) {

            keepDiaryCanvasLocked();

            JOptionPane.showMessageDialog(
                    parentWindow,
                    "Unable to complete authentication.\n"
                            + "Please try again.",
                    "Authentication Error",
                    JOptionPane.ERROR_MESSAGE
            );

        } finally {

            authenticationInProgress = false;
        }
    }

    /**
     * Unlocks the diary canvas after successful authentication.
     */
    private void unlockDiaryCanvas() {

        diaryTextArea.setEditable(true);

        diaryTextArea.requestFocusInWindow();

        JOptionPane.showMessageDialog(
                parentWindow,
                "Access Granted",
                "Authentication Successful",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Ensures that the diary canvas remains locked.
     */
    private void keepDiaryCanvasLocked() {

        diaryTextArea.setEditable(false);
    }

    /**
     * Returns whether the diary canvas is currently unlocked.
     *
     * @return true if the canvas is editable
     */
    public boolean isDiaryUnlocked() {

        return diaryTextArea.isEditable();
    }

    /**
     * Locks the diary canvas again.
     */
    public void lockDiaryCanvas() {

        diaryTextArea.setEditable(false);
    }
}
