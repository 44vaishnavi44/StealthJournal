package org.stealthjournal.security;

import org.stealthjournal.config.ConfigManager;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles the emergency panic functionality of StealthJournal.
 *
 * Responsibilities:
 * 1. Immediately clear the diary canvas.
 * 2. Lock the diary canvas against editing.
 * 3. Retrieve the configured decoy video path.
 * 4. Launch the video using the operating system's default application.
 * 5. Display a fallback alert if the video cannot be launched.
 *
 * Implements functional requirement FR-07.
 */
public class PanicSystemManager {

    private static final String FALLBACK_MESSAGE =
            "Media Execution Override Triggered";

    private final JTextArea diaryTextArea;
    private final ConfigManager configManager;
    private final Component parentComponent;

    /**
     * Creates a PanicSystemManager.
     *
     * The diary text area is also used as the parent component
     * for the fallback dialog.
     *
     * @param diaryTextArea the application's diary canvas
     * @param configManager the configuration manager
     */
    public PanicSystemManager(
            JTextArea diaryTextArea,
            ConfigManager configManager) {

        this(diaryTextArea, configManager, diaryTextArea);
    }

    /**
     * Creates a PanicSystemManager with a custom parent component
     * for displaying the fallback dialog.
     *
     * @param diaryTextArea the application's diary canvas
     * @param configManager the configuration manager
     * @param parentComponent component used as the dialog parent
     */
    public PanicSystemManager(
            JTextArea diaryTextArea,
            ConfigManager configManager,
            Component parentComponent) {

        if (diaryTextArea == null) {
            throw new IllegalArgumentException(
                    "Diary text area cannot be null."
            );
        }

        if (configManager == null) {
            throw new IllegalArgumentException(
                    "ConfigManager cannot be null."
            );
        }

        this.diaryTextArea = diaryTextArea;
        this.configManager = configManager;

        this.parentComponent = parentComponent != null
                ? parentComponent
                : diaryTextArea;
    }

    /**
     * Executes the panic override.
     *
     * The canvas is cleared and locked before attempting to
     * launch the configured decoy video.
     *
     * This method is safe to call from either the Swing
     * Event Dispatch Thread or another thread.
     */
    public void triggerPanic() {

        if (SwingUtilities.isEventDispatchThread()) {
            executePanicOverride();
            return;
        }

        try {
            SwingUtilities.invokeAndWait(
                    this::executePanicOverride
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            System.err.println(
                    "Panic operation was interrupted: "
                            + exception.getMessage()
            );

        } catch (InvocationTargetException exception) {

            Throwable cause = exception.getCause();

            System.err.println(
                    "Panic operation failed: "
                            + (cause != null
                            ? cause.getMessage()
                            : exception.getMessage())
            );
        }
    }

    /**
     * Performs the actual panic operation.
     *
     * This method must execute on the Swing Event Dispatch Thread.
     */
    private void executePanicOverride() {

        // Step 1: Immediately clear the visible diary content.
        diaryTextArea.setText("");

        // Step 2: Lock the canvas against further editing.
        diaryTextArea.setEditable(false);

        // Reset the caret to the beginning of the empty canvas.
        diaryTextArea.setCaretPosition(0);

        // Step 3: Retrieve the configured decoy video path.
        String videoPath;

        try {
            videoPath = configManager.getDecoyVideoPath();

        } catch (RuntimeException exception) {

            System.err.println(
                    "Unable to retrieve decoy video configuration: "
                            + exception.getMessage()
            );

            showFallbackAlert();
            return;
        }

        // Step 4: Validate the configured path.
        if (videoPath == null || videoPath.trim().isEmpty()) {
            showFallbackAlert();
            return;
        }

        try {
            Path path = Paths.get(videoPath.trim())
                    .toAbsolutePath()
                    .normalize();

            File videoFile = path.toFile();

            // Ensure the selected video exists and is accessible.
            if (!Files.isRegularFile(path)
                    || !Files.isReadable(path)) {

                showFallbackAlert();
                return;
            }

            // Step 5: Check whether Desktop integration is available.
            if (!Desktop.isDesktopSupported()) {
                showFallbackAlert();
                return;
            }

            Desktop desktop = Desktop.getDesktop();

            if (!desktop.isSupported(Desktop.Action.OPEN)) {
                showFallbackAlert();
                return;
            }

            // Step 6: Open the video using the operating
            // system's default application.
            desktop.open(videoFile);

        } catch (InvalidPathException
                 | IOException
                 | SecurityException
                 | UnsupportedOperationException exception) {

            System.err.println(
                    "Decoy video launch failed: "
                            + exception.getMessage()
            );

            showFallbackAlert();

        } catch (IllegalArgumentException exception) {

            System.err.println(
                    "Invalid decoy video configuration: "
                            + exception.getMessage()
            );

            showFallbackAlert();
        }
    }

    /**
     * Displays the fallback alert when the decoy video
     * is missing or cannot be launched.
     *
     * The diary canvas remains cleared and locked.
     */
    private void showFallbackAlert() {

        if (GraphicsEnvironment.isHeadless()) {
            System.err.println(FALLBACK_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(
                parentComponent,
                FALLBACK_MESSAGE,
                "System Notification",
                JOptionPane.WARNING_MESSAGE
        );
    }

    /**
     * Returns the diary text area managed by this class.
     *
     * @return diary JTextArea
     */
    public JTextArea getDiaryTextArea() {
        return diaryTextArea;
    }
}