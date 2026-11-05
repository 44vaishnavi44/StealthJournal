package org.stealthjournal.security;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import orgt.stealthjournal.util.Constants;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

/**
 * Handles the deceptive "Click to View" trap mechanism.
 *
 * Responsibilities:
 * 1. Intercept clicks on the deceptive button.
 * 2. Display a fake system error dialog.
 * 3. Terminate the application process.
 *
 * Implements functional requirement FR-08.
 */
public class TrapHandler implements ActionListener {

    private static final String ERROR_MESSAGE =
            "Critical System Exception 0x00084F: "
            + "Application Data File Corrupted. "
            + "The system will now close to prevent "
            + "local memory corruption.";

    private final Component parentComponent;

    /**
     * Creates a TrapHandler without a parent component.
     */
    public TrapHandler() {
        this(null);
    }

    /**
     * Creates a TrapHandler with a parent component.
     *
     * @param parentComponent component used as the dialog parent
     */
    public TrapHandler(Component parentComponent) {
        this.parentComponent = parentComponent;
    }

    /**
     * Handles the deceptive button's click event.
     *
     * This method is automatically invoked when this object
     * is registered as an ActionListener.
     *
     * @param event button click event
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        handleTrapClick();
    }

    /**
     * Executes the fake system error sequence.
     *
     * Displays the error dialog and terminates the application.
     */
    public void handleTrapClick() {

        if (SwingUtilities.isEventDispatchThread()) {
            executeTrapSequence();
            return;
        }

        try {
            SwingUtilities.invokeAndWait(
                    this::executeTrapSequence
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            System.err.println(
                    "Trap execution interrupted: "
                            + exception.getMessage()
            );

            System.exit(0);

        } catch (InvocationTargetException exception) {

            Throwable cause = exception.getCause();

            System.err.println(
                    "Trap execution failed: "
                            + (cause != null
                            ? cause.getMessage()
                            : exception.getMessage())
            );

            System.exit(0);
        }
    }

    /**
     * Displays the deceptive error dialog and exits the process.
     *
     * This method executes on the Swing Event Dispatch Thread.
     */
    private void executeTrapSequence() {

        // Display the fake system error dialog.
        if (!GraphicsEnvironment.isHeadless()) {

            JOptionPane.showMessageDialog(
                    parentComponent,
                    ERROR_MESSAGE,
                    Constants.CORRUPTION_ERROR_TITLE,
                    JOptionPane.ERROR_MESSAGE
            );
        }

        // Terminate the entire application process.
        System.exit(0);
    }
}