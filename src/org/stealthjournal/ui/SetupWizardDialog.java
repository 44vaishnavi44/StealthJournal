
package org.stealthjournal.ui;

import org.stealthjournal.auth.AuthenticationManager;
import org.stealthjournal.config.ConfigManager;

import org.stealthjournal.util.Constants;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * First-time configuration wizard for StealthJournal.
 *
 * Collects:
 * - Four custom deceptive button labels.
 * - Four corresponding application actions.
 * - A valid local decoy video file.
 * - A secondary authentication password.
 * - A security question and its answer.
 *
 * Configuration is saved through ConfigManager.
 * Authentication credentials are stored as salted hashes
 * through AuthenticationManager.
 */
public final class SetupWizardDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final int BUTTON_SLOT_COUNT = 4;

    private static final String[] AVAILABLE_ACTIONS = {
            "SAVE_ENCRYPT",
            "OPEN_DECRYPT",
            "PANIC_MODE",
            "UNVIEW_CANVAS"
    };

    private static final String[] DEFAULT_BUTTON_LABELS = {
            "Help",
            "Refresh",
            "System Log",
            "Click to View"
    };

    private static final String[] ACTION_DESCRIPTIONS = {
            "Save diary content using encryption",
            "Open and decrypt a diary file",
            "Activate the panic system",
            "Hide the diary canvas"
    };

    private static final String[] VIDEO_EXTENSIONS = {
            "mp4",
            "avi",
            "mkv"
    };

    private final ConfigManager configManager;

    private final AuthenticationManager authenticationManager;

    private final JTextField[] buttonLabelFields =
            new JTextField[BUTTON_SLOT_COUNT];

    private final JComboBox<String>[] actionSelectors =
            createActionSelectors();

    private final JTextField decoyVideoPathField =
            new JTextField(25);

    private final JPasswordField secondaryPasswordField =
            new JPasswordField(25);

    private final JTextField securityQuestionField =
            new JTextField(25);

    private final JPasswordField securityAnswerField =
            new JPasswordField(25);

    private boolean setupSuccessful;

    private boolean initializationSuccessful;

    /**
     * Creates the setup wizard.
     *
     * @param owner parent window
     * @param configManager configuration manager
     */
    public SetupWizardDialog(
            Window owner,
            ConfigManager configManager
    ) {

        super(
                owner,
                Constants.SETUP_DIALOG_TITLE,
                Dialog.ModalityType.APPLICATION_MODAL
        );

        if (configManager == null) {
            throw new IllegalArgumentException(
                    "ConfigManager cannot be null."
            );
        }

        this.configManager = configManager;

        this.authenticationManager =
                new AuthenticationManager(configManager);

        this.setupSuccessful = false;

        this.initializationSuccessful =
                configManager.loadConfiguration();

        initializeDialog();
        buildInterface();

        setLocationRelativeTo(owner);
    }

    /**
     * Initializes the dialog properties.
     */
    private void initializeDialog() {

        setDefaultCloseOperation(
                JDialog.DISPOSE_ON_CLOSE
        );

        setSize(650, 720);

        setMinimumSize(
                new java.awt.Dimension(580, 620)
        );

        setResizable(true);

        setLayout(new BorderLayout(10, 10));
    }

    /**
     * Builds the complete setup wizard interface.
     */
    private void buildInterface() {

        JPanel headingPanel = createHeadingPanel();

        JPanel formPanel = createFormPanel();

        JPanel footerPanel = createFooterPanel();

        add(headingPanel, BorderLayout.NORTH);

        add(
                new JScrollPane(formPanel),
                BorderLayout.CENTER
        );

        add(footerPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the wizard heading.
     *
     * @return heading panel
     */
    private JPanel createHeadingPanel() {

        JPanel panel = new JPanel(
                new BorderLayout(5, 5)
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        15, 20, 10, 20
                )
        );

        JLabel titleLabel = new JLabel(
                "Welcome to StealthJournal",
                SwingConstants.CENTER
        );

        titleLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        22
                )
        );

        JLabel descriptionLabel = new JLabel(
                "<html><center>"
                + "Complete the following settings to "
                + "initialize your private journal."
                + "</center></html>",
                SwingConstants.CENTER
        );

        descriptionLabel.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.PLAIN,
                        13
                )
        );

        panel.add(
                titleLabel,
                BorderLayout.NORTH
        );

        panel.add(
                descriptionLabel,
                BorderLayout.CENTER
        );

        return panel;
    }

    /**
     * Creates the main form containing all configuration fields.
     *
     * @return configuration form panel
     */
    private JPanel createFormPanel() {

        JPanel panel = new JPanel(
                new GridBagLayout()
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        10, 20, 15, 20
                )
        );

        GridBagConstraints constraints =
                new GridBagConstraints();

        constraints.insets =
                new Insets(6, 5, 6, 5);

        constraints.fill =
                GridBagConstraints.HORIZONTAL;

        constraints.weightx = 1.0;

        int row = 0;

        row = addSectionHeading(
                panel,
                constraints,
                row,
                "1. Custom Interface Buttons"
        );

        for (int i = 0; i < BUTTON_SLOT_COUNT; i++) {

            buttonLabelFields[i] =
                    new JTextField(
                            DEFAULT_BUTTON_LABELS[i],
                            18
                    );

            JPanel buttonRow = createButtonSlotRow(i);

            addFullWidthComponent(
                    panel,
                    constraints,
                    row++,
                    buttonRow
            );
        }

        row = addSectionHeading(
                panel,
                constraints,
                row,
                "2. Decoy Video Configuration"
        );

        JPanel videoPanel = createVideoSelectionPanel();

        addFullWidthComponent(
                panel,
                constraints,
                row++,
                videoPanel
        );

        row = addSectionHeading(
                panel,
                constraints,
                row,
                "3. Secondary Authentication"
        );

        addLabeledField(
                panel,
                constraints,
                row++,
                "Secondary Password:",
                secondaryPasswordField
        );

        addLabeledField(
                panel,
                constraints,
                row++,
                "Security Question:",
                securityQuestionField
        );

        addLabeledField(
                panel,
                constraints,
                row++,
                "Security Answer:",
                securityAnswerField
        );

        JLabel securityNote = new JLabel(
                "<html><i>"
                + "Your password and security answer will be "
                + "stored as salted SHA-256 hashes."
                + "</i></html>"
        );

        securityNote.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.PLAIN,
                        11
                )
        );

        addFullWidthComponent(
                panel,
                constraints,
                row,
                securityNote
        );

        return panel;
    }

    /**
     * Creates one custom button configuration row.
     *
     * @param slotIndex zero-based button slot index
     * @return configured row panel
     */
    private JPanel createButtonSlotRow(int slotIndex) {

        JPanel row = new JPanel(
                new GridBagLayout()
        );

        GridBagConstraints constraints =
                new GridBagConstraints();

        constraints.insets =
                new Insets(3, 3, 3, 3);

        constraints.fill =
                GridBagConstraints.HORIZONTAL;

        JLabel slotLabel = new JLabel(
                "Button " + (slotIndex + 1) + ":"
        );

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.0;

        row.add(
                slotLabel,
                constraints
        );

        constraints.gridx = 1;
        constraints.weightx = 1.0;

        row.add(
                buttonLabelFields[slotIndex],
                constraints
        );

        constraints.gridx = 2;
        constraints.weightx = 0.0;

        row.add(
                actionSelectors[slotIndex],
                constraints
        );

        return row;
    }

    /**
     * Creates the decoy video selection controls.
     *
     * @return video selection panel
     */
    private JPanel createVideoSelectionPanel() {

        JPanel panel = new JPanel(
                new BorderLayout(8, 5)
        );

        decoyVideoPathField.setEditable(false);

        JButton browseButton =
                new JButton("Browse...");

        browseButton.addActionListener(
                event -> selectDecoyVideo()
        );

        panel.add(
                new JLabel("Video File:"),
                BorderLayout.NORTH
        );

        panel.add(
                decoyVideoPathField,
                BorderLayout.CENTER
        );

        panel.add(
                browseButton,
                BorderLayout.EAST
        );

        return panel;
    }

    /**
     * Opens a file chooser and selects a decoy video.
     */
    private void selectDecoyVideo() {

        JFileChooser fileChooser =
                new JFileChooser();

        fileChooser.setDialogTitle(
                "Select Decoy Video"
        );

        fileChooser.setFileSelectionMode(
                JFileChooser.FILES_ONLY
        );

        fileChooser.setAcceptAllFileFilterUsed(false);

        FileNameExtensionFilter videoFilter =
                new FileNameExtensionFilter(
                        "Video Files (*.mp4, *.avi, *.mkv)",
                        VIDEO_EXTENSIONS
                );

        fileChooser.setFileFilter(videoFilter);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            File selectedFile =
                    fileChooser.getSelectedFile();

            if (isValidVideoFile(selectedFile.toPath())) {

                decoyVideoPathField.setText(
                        selectedFile.getAbsolutePath()
                );

            } else {

                showError(
                        "Please select a valid video file "
                        + "(.mp4, .avi or .mkv)."
                );
            }
        }
    }

    /**
     * Checks whether a selected video file exists,
     * is readable and has a supported extension.
     *
     * @param videoPath selected video path
     * @return true if the file is valid
     */
    private boolean isValidVideoFile(Path videoPath) {

        if (videoPath == null) {
            return false;
        }

        if (!videoPath.isAbsolute()) {
            return false;
        }

        if (!Files.isRegularFile(videoPath)
                || !Files.isReadable(videoPath)) {

            return false;
        }

        String fileName =
                videoPath.getFileName().toString()
                        .toLowerCase(java.util.Locale.ROOT);

        for (String extension : VIDEO_EXTENSIONS) {

            if (fileName.endsWith("." + extension)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a section heading to the form.
     *
     * @param panel parent panel
     * @param constraints layout constraints
     * @param row grid row
     * @param heading heading text
     * @return next available row
     */
    private int addSectionHeading(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String heading
    ) {

        JLabel label = new JLabel(heading);

        label.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        15
                )
        );

        label.setBorder(
                BorderFactory.createEmptyBorder(
                        12, 0, 5, 0
                )
        );

        addFullWidthComponent(
                panel,
                constraints,
                row,
                label
        );

        return row + 1;
    }

    /**
     * Adds a labeled input field to the form.
     *
     * @param panel parent panel
     * @param constraints layout constraints
     * @param row grid row
     * @param labelText label text
     * @param inputField input component
     */
    private void addLabeledField(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String labelText,
            java.awt.Component inputField
    ) {

        JLabel label = new JLabel(labelText);

        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 1;
        constraints.weightx = 0.0;
        constraints.fill =
                GridBagConstraints.HORIZONTAL;

        panel.add(
                label,
                constraints
        );

        constraints.gridx = 1;
        constraints.gridwidth = 1;
        constraints.weightx = 1.0;

        panel.add(
                inputField,
                constraints
        );
    }

    /**
     * Adds a component across the full form width.
     *
     * @param panel parent panel
     * @param constraints layout constraints
     * @param row grid row
     * @param component component to add
     */
    private void addFullWidthComponent(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            java.awt.Component component
    ) {

        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.weightx = 1.0;
        constraints.fill =
                GridBagConstraints.HORIZONTAL;

        panel.add(
                component,
                constraints
        );

        constraints.gridwidth = 1;
    }

    /**
     * Creates the bottom action buttons.
     *
     * @return footer panel
     */
    private JPanel createFooterPanel() {

        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 10)
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        0, 15, 10, 15
                )
        );

        JButton cancelButton =
                new JButton("Cancel");

        JButton saveButton =
                new JButton("Save Configuration");

        cancelButton.addActionListener(
                event -> dispose()
        );

        saveButton.addActionListener(
                event -> saveConfiguration()
        );

        panel.add(cancelButton);
        panel.add(saveButton);

        getRootPane().setDefaultButton(saveButton);

        return panel;
    }

    /**
     * Creates the action selectors for all four button slots.
     *
     * @return array of action selectors
     */
    @SuppressWarnings("unchecked")
    private JComboBox<String>[] createActionSelectors() {

        JComboBox<String>[] selectors =
                new JComboBox[BUTTON_SLOT_COUNT];

        for (int i = 0; i < BUTTON_SLOT_COUNT; i++) {

            selectors[i] =
                    new JComboBox<String>(
                            AVAILABLE_ACTIONS
                    );

            selectors[i].setSelectedIndex(i);

            selectors[i].setToolTipText(
                    ACTION_DESCRIPTIONS[i]
            );
        }

        return selectors;
    }

    /**
     * Validates all user-provided configuration fields.
     *
     * @return true if all fields contain valid values
     */
    private boolean validateInput() {

        for (int i = 0; i < BUTTON_SLOT_COUNT; i++) {

            if (isEmpty(buttonLabelFields[i].getText())) {

                showError(
                        "All configuration fields are required. "
                        + "Please enter a label for Button "
                        + (i + 1) + "."
                );

                buttonLabelFields[i].requestFocusInWindow();

                return false;
            }

            if (actionSelectors[i].getSelectedItem() == null) {

                showError(
                        "Please select an action for every button."
                );

                actionSelectors[i].requestFocusInWindow();

                return false;
            }
        }

        if (isEmpty(decoyVideoPathField.getText())) {

            showError(
                    "Please select a decoy video file."
            );

            return false;
        }

        Path videoPath;

        try {

            videoPath = Paths.get(
                    decoyVideoPathField.getText().trim()
            );

        } catch (RuntimeException exception) {

            showError(
                    "The selected video path is invalid."
            );

            return false;
        }

        if (!isValidVideoFile(videoPath)) {

            showError(
                    "The selected video file does not exist "
                    + "or is not a supported readable video."
            );

            return false;
        }

        if (isEmpty(new String(
                secondaryPasswordField.getPassword()
        ))) {

            showError(
                    "Please enter a secondary password."
            );

            secondaryPasswordField.requestFocusInWindow();

            return false;
        }

        if (isEmpty(securityQuestionField.getText())) {

            showError(
                    "Please enter a security question."
            );

            securityQuestionField.requestFocusInWindow();

            return false;
        }

        if (isEmpty(new String(
                securityAnswerField.getPassword()
        ))) {

            showError(
                    "Please enter the answer to your security question."
            );

            securityAnswerField.requestFocusInWindow();

            return false;
        }

        return true;
    }

    /**
     * Checks whether a string is null, empty or whitespace-only.
     *
     * @param value string to check
     * @return true if the string is empty
     */
    private boolean isEmpty(String value) {

        return value == null || value.trim().isEmpty();
    }

    /**
     * Saves the complete first-time configuration.
     *
     * The setup-completed flag is saved last so that an
     * incomplete configuration is never marked as complete.
     */
    private void saveConfiguration() {

        if (!initializationSuccessful) {

            showError(
                    "The configuration system could not be initialized. "
                    + "Please check the configuration file permissions."
            );

            return;
        }

        if (!validateInput()) {
            return;
        }

        String password =
                new String(
                        secondaryPasswordField.getPassword()
                );

        String question =
                securityQuestionField.getText().trim();

        String answer =
                new String(
                        securityAnswerField.getPassword()
                );

        boolean saved = true;

        try {

            for (int i = 0; i < BUTTON_SLOT_COUNT; i++) {

                int slotNumber = i + 1;

                String labelKey =
                        "BTN_SLOT_" + slotNumber + "_LABEL";

                String actionKey =
                        "BTN_SLOT_" + slotNumber + "_ACTION";

                String label =
                        buttonLabelFields[i].getText().trim();

                String action =
                        actionSelectors[i]
                                .getSelectedItem()
                                .toString();

                saved = configManager.setSetting(
                        labelKey,
                        label
                ) && saved;

                saved = configManager.setSetting(
                        actionKey,
                        action
                ) && saved;
            }

            saved = configManager.setDecoyVideoPath(
                    decoyVideoPathField.getText().trim()
            ) && saved;

            if (saved) {

                saved = authenticationManager.setCredentials(
                        password,
                        question,
                        answer
                );
            }

            if (saved) {

                saved = configManager.setSetupCompleted(true);
            }

            if (!saved) {

                rollbackConfiguration();

                showError(
                        "Configuration could not be saved. "
                        + "Please check disk permissions and try again."
                );

                return;
            }

            setupSuccessful = true;

            JOptionPane.showMessageDialog(
                    this,
                    "StealthJournal has been configured successfully.",
                    Constants.SETUP_DIALOG_TITLE,
                    JOptionPane.INFORMATION_MESSAGE
            );

            dispose();

        } catch (RuntimeException exception) {

            rollbackConfiguration();

            showError(
                    "An unexpected error occurred while saving "
                    + "the configuration: "
                    + exception.getMessage()
            );

        } finally {

            password = null;
            answer = null;

            secondaryPasswordField.setText("");
            securityAnswerField.setText("");
        }
    }

    /**
     * Removes partially saved first-time configuration values
     * if the setup process fails.
     */
    private void rollbackConfiguration() {

        configManager.setSetupCompleted(false);

        for (int i = 1; i <= BUTTON_SLOT_COUNT; i++) {

            configManager.removeSetting(
                    "BTN_SLOT_" + i + "_LABEL"
            );

            configManager.removeSetting(
                    "BTN_SLOT_" + i + "_ACTION"
            );
        }

        authenticationManager.clearCredentials();

        configManager.setDecoyVideoPath(
                Constants.DEFAULT_DECOY_VIDEO
        );
    }

    /**
     * Displays a validation or configuration error.
     *
     * @param message error description
     */
    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Setup Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Returns whether the initial setup was completed successfully.
     *
     * The main application should check this after the dialog closes.
     *
     * @return true if configuration was successfully saved
     */
    public boolean isSetupSuccessful() {

        return setupSuccessful;
    }

    /**
     * Returns the ConfigManager used by this wizard.
     *
     * @return configuration manager
     */
    public ConfigManager getConfigManager() {

        return configManager;
    }
}
