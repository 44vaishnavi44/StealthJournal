
package org.stealthjournal.ui.components;

import org.stealthjournal.config.ConfigManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dynamically creates and manages StealthJournal's deceptive buttons.
 *
 * Responsibilities:
 * - Load custom button labels from ConfigManager.
 * - Load the action assigned to each button.
 * - Create four buttons using the saved configuration.
 * - Dispatch button events to registered action listeners.
 * - Allow action handlers to be registered independently.
 *
 * Actual application actions are connected in later development phases.
 */
public final class DeceptiveButtonPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int BUTTON_COUNT = 4;

    private static final String[] VALID_ACTIONS = {
            "SAVE_ENCRYPT",
            "OPEN_DECRYPT",
            "PANIC_MODE",
            "UNVIEW_CANVAS"
    };

    private final ConfigManager configManager;

    private final JButton[] buttons;

    private final String[] buttonActions;

    private final Map<String, List<ActionListener>> actionHandlers;

    /**
     * Creates the deceptive button panel.
     *
     * Button labels and action mappings are loaded immediately
     * from the supplied ConfigManager.
     *
     * @param configManager configuration manager
     * @throws IllegalArgumentException if configManager is null
     * @throws IllegalStateException if the configuration is invalid
     */
    public DeceptiveButtonPanel(ConfigManager configManager) {

        if (configManager == null) {
            throw new IllegalArgumentException(
                    "ConfigManager cannot be null."
            );
        }

        this.configManager = configManager;

        this.buttons = new JButton[BUTTON_COUNT];

        this.buttonActions = new String[BUTTON_COUNT];

        this.actionHandlers =
                new LinkedHashMap<String, List<ActionListener>>();

        initializePanel();

        loadButtonsFromConfiguration();
    }

    /**
     * Initializes the panel layout and appearance.
     */
    private void initializePanel() {

        setLayout(
                new GridLayout(
                        1,
                        BUTTON_COUNT,
                        8,
                        0
                )
        );

        setBorder(
                BorderFactory.createEmptyBorder(
                        8,
                        8,
                        8,
                        8
                )
        );
    }

    /**
     * Loads all button labels and action mappings from configuration.
     *
     * Expected configuration keys:
     *
     * BTN_SLOT_1_LABEL
     * BTN_SLOT_1_ACTION
     *
     * BTN_SLOT_2_LABEL
     * BTN_SLOT_2_ACTION
     *
     * BTN_SLOT_3_LABEL
     * BTN_SLOT_3_ACTION
     *
     * BTN_SLOT_4_LABEL
     * BTN_SLOT_4_ACTION
     *
     * @throws IllegalStateException if any configuration entry is missing
     *                               or contains an invalid value
     */
    public final void loadButtonsFromConfiguration() {

        String[] loadedLabels = new String[BUTTON_COUNT];

        String[] loadedActions = new String[BUTTON_COUNT];

        /*
         * Validate the complete configuration before changing
         * the existing button layout.
         */
        for (int i = 0; i < BUTTON_COUNT; i++) {

            int slotNumber = i + 1;

            String labelKey =
                    "BTN_SLOT_" + slotNumber + "_LABEL";

            String actionKey =
                    "BTN_SLOT_" + slotNumber + "_ACTION";

            String label =
                    configManager.getSetting(labelKey);

            String action =
                    configManager.getSetting(actionKey);

            if (label == null || label.trim().isEmpty()) {

                throw new IllegalStateException(
                        "Missing or invalid button label for slot "
                        + slotNumber + "."
                );
            }

            if (action == null || action.trim().isEmpty()) {

                throw new IllegalStateException(
                        "Missing action mapping for button slot "
                        + slotNumber + "."
                );
            }

            action = action.trim();

            if (!isValidAction(action)) {

                throw new IllegalStateException(
                        "Invalid action configured for button slot "
                        + slotNumber + ": " + action
                );
            }

            loadedLabels[i] = label.trim();

            loadedActions[i] = action;
        }

        /*
         * Apply the validated configuration.
         */
        removeAll();

        for (int i = 0; i < BUTTON_COUNT; i++) {

            final int slotIndex = i;

            JButton button = new JButton(
                    loadedLabels[i]
            );

            button.setActionCommand(
                    loadedActions[i]
            );

            button.setFocusable(true);

            button.setToolTipText(
                    "Interface control"
            );

            button.addActionListener(
                    event -> dispatchAction(
                            slotIndex,
                            event
                    )
            );

            buttons[i] = button;

            buttonActions[i] = loadedActions[i];

            add(button);
        }

        revalidate();

        repaint();
    }

    /**
     * Checks whether an action code is supported.
     *
     * @param action action code
     * @return true if the action is recognized
     */
    private boolean isValidAction(String action) {

        if (action == null) {
            return false;
        }

        for (String validAction : VALID_ACTIONS) {

            if (validAction.equals(action)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Dispatches a button click to the listeners registered
     * for that button's configured action.
     *
     * If no listener has been registered yet, the click does
     * not execute an application action.
     *
     * @param slotIndex button slot index
     * @param originalEvent original Swing action event
     */
    private void dispatchAction(
            int slotIndex,
            ActionEvent originalEvent
    ) {

        if (slotIndex < 0 || slotIndex >= BUTTON_COUNT) {
            return;
        }

        String action = buttonActions[slotIndex];

        List<ActionListener> handlers =
                actionHandlers.get(action);

        if (handlers == null || handlers.isEmpty()) {
            return;
        }

        ActionEvent dispatchedEvent =
                new ActionEvent(
                        buttons[slotIndex],
                        ActionEvent.ACTION_PERFORMED,
                        action,
                        originalEvent.getWhen(),
                        originalEvent.getModifiers()
                );

        /*
         * Use a snapshot so listeners can safely modify
         * their registrations during event processing.
         */
        List<ActionListener> handlerSnapshot =
                new ArrayList<ActionListener>(handlers);

        for (ActionListener handler : handlerSnapshot) {

            handler.actionPerformed(dispatchedEvent);
        }
    }

    /**
     * Registers an action listener for a specific action code.
     *
     * Multiple buttons can share the same action code.
     * Registering a listener for an action therefore makes
     * it available to every button mapped to that action.
     *
     * @param action action code
     * @param listener action listener
     * @throws IllegalArgumentException if the action or listener is invalid
     */
    public void addActionHandler(
            String action,
            ActionListener listener
    ) {

        if (!isValidAction(action)) {

            throw new IllegalArgumentException(
                    "Unsupported action: " + action
            );
        }

        if (listener == null) {

            throw new IllegalArgumentException(
                    "Action listener cannot be null."
            );
        }

        List<ActionListener> handlers =
                actionHandlers.get(action);

        if (handlers == null) {

            handlers =
                    new ArrayList<ActionListener>();

            actionHandlers.put(action, handlers);
        }

        if (!handlers.contains(listener)) {
            handlers.add(listener);
        }
    }

    /**
     * Replaces all listeners registered for an action
     * with one supplied listener.
     *
     * @param action action code
     * @param listener replacement listener
     */
    public void setActionHandler(
            String action,
            ActionListener listener
    ) {

        if (!isValidAction(action)) {

            throw new IllegalArgumentException(
                    "Unsupported action: " + action
            );
        }

        if (listener == null) {

            throw new IllegalArgumentException(
                    "Action listener cannot be null."
            );
        }

        List<ActionListener> handlers =
                new ArrayList<ActionListener>();

        handlers.add(listener);

        actionHandlers.put(action, handlers);
    }

    /**
     * Removes a previously registered listener.
     *
     * @param action action code
     * @param listener listener to remove
     * @return true if the listener was removed
     */
    public boolean removeActionHandler(
            String action,
            ActionListener listener
    ) {

        if (!isValidAction(action) || listener == null) {
            return false;
        }

        List<ActionListener> handlers =
                actionHandlers.get(action);

        if (handlers == null) {
            return false;
        }

        boolean removed = handlers.remove(listener);

        if (handlers.isEmpty()) {
            actionHandlers.remove(action);
        }

        return removed;
    }

    /**
     * Removes every listener registered for an action.
     *
     * @param action action code
     * @return true if an action registration existed
     */
    public boolean clearActionHandlers(String action) {

        if (!isValidAction(action)) {
            return false;
        }

        return actionHandlers.remove(action) != null;
    }

    /**
     * Removes all registered action listeners.
     */
    public void clearAllActionHandlers() {

        actionHandlers.clear();
    }

    /**
     * Returns a button using its zero-based slot index.
     *
     * @param index button index from 0 to 3
     * @return corresponding JButton
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public JButton getButton(int index) {

        validateIndex(index);

        return buttons[index];
    }

    /**
     * Returns all four buttons.
     *
     * The returned array is a copy, so callers cannot
     * replace the panel's internal button references.
     *
     * @return copy of the button array
     */
    public JButton[] getButtons() {

        return buttons.clone();
    }

    /**
     * Returns the configured action for a button slot.
     *
     * @param index button index from 0 to 3
     * @return configured action code
     */
    public String getButtonAction(int index) {

        validateIndex(index);

        return buttonActions[index];
    }

    /**
     * Returns the configured label for a button slot.
     *
     * @param index button index from 0 to 3
     * @return displayed button label
     */
    public String getButtonLabel(int index) {

        validateIndex(index);

        return buttons[index].getText();
    }

    /**
     * Returns the number of buttons managed by this panel.
     *
     * @return button count
     */
    public int getButtonCount() {

        return BUTTON_COUNT;
    }

    /**
     * Checks whether an action has at least one registered listener.
     *
     * @param action action code
     * @return true if a handler is registered
     */
    public boolean hasActionHandler(String action) {

        if (!isValidAction(action)) {
            return false;
        }

        List<ActionListener> handlers =
                actionHandlers.get(action);

        return handlers != null && !handlers.isEmpty();
    }

    /**
     * Returns a snapshot of the registered action handlers.
     *
     * The returned map and its lists cannot be modified
     * to change this panel's internal registrations.
     *
     * @return unmodifiable action-handler snapshot
     */
    public Map<String, List<ActionListener>>
            getRegisteredActionHandlers() {

        Map<String, List<ActionListener>> snapshot =
                new LinkedHashMap<String, List<ActionListener>>();

        for (Map.Entry<String, List<ActionListener>> entry
                : actionHandlers.entrySet()) {

            snapshot.put(
                    entry.getKey(),
                    Collections.unmodifiableList(
                            new ArrayList<ActionListener>(
                                    entry.getValue()
                            )
                    )
            );
        }

        return Collections.unmodifiableMap(snapshot);
    }

    /**
     * Validates a button index.
     *
     * @param index button index
     */
    private void validateIndex(int index) {

        if (index < 0 || index >= BUTTON_COUNT) {

            throw new IndexOutOfBoundsException(
                    "Button index must be between 0 and "
                    + (BUTTON_COUNT - 1) + "."
            );
        }
    }
}
