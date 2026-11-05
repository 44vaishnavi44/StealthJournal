
package org.stealthjournal.ui;

import org.stealthjournal.auth.AuthenticationManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Arrays;

/**
 * Modal dialog for StealthJournal's secondary authentication.
 *
 * The user must provide the configured secondary password and
 * the correct answer to the configured security question.
 *
 * Authentication is performed through AuthenticationManager.
 */
public class AuthPromptDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final AuthenticationManager authenticationManager;

    private final JLabel questionLabel;
    private final JPasswordField passwordField;
    private final JTextField answerField;

    private boolean authenticated;

    /**
     * Creates the authentication dialog.
     *
     * @param owner parent window
     * @param authenticationManager authentication manager
     */
    public AuthPromptDialog(
            Window owner,
            AuthenticationManager authenticationManager) {

        super(
                owner,
                "Authentication Required",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        if (authenticationManager == null) {
            throw new IllegalArgumentException(
                    "AuthenticationManager must not be null."
            );
        }

        if (!authenticationManager.hasCredentials()) {
            throw new IllegalStateException(
                    "Authentication credentials are not configured."
            );
        }

        this.authenticationManager = authenticationManager;
        this.authenticated = false;

        this.questionLabel = new JLabel(
                authenticationManager.getSecurityQuestion()
        );

        this.passwordField = new JPasswordField(22);
        this.answerField = new JTextField(22);

        initializeDialog();
        createComponents();

        pack();
        setMinimumSize(getPreferredSize());
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /**
     * Initializes the dialog properties.
     */
    private void initializeDialog() {

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout(12, 12));

        getRootPane().setBorder(
                BorderFactory.createEmptyBorder(18, 20, 15, 20)
        );

    }

    /**
     * Creates and arranges the authentication components.
     */
    private void createComponents() {

        JLabel headingLabel = new JLabel(
                "Verify Your Identity",
                SwingConstants.CENTER
        );

        headingLabel.setFont(
                new Font("SansSerif", Font.BOLD, 17)
        );

        JLabel instructionLabel = new JLabel(
                "Enter your secondary security credentials.",
                SwingConstants.CENTER
        );

        instructionLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        JPanel headingPanel = new JPanel(
                new BorderLayout(5, 5)
        );

        headingPanel.add(
                headingLabel,
                BorderLayout.NORTH
        );

        headingPanel.add(
                instructionLabel,
                BorderLayout.SOUTH
        );

        add(
                headingPanel,
                BorderLayout.NORTH
        );

        JPanel formPanel = new JPanel(
                new GridBagLayout()
        );

        GridBagConstraints constraints =
                new GridBagConstraints();

        constraints.insets = new Insets(8, 5, 8, 5);

        constraints.anchor = GridBagConstraints.WEST;

        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.weightx = 0;

        constraints.gridx = 0;
        constraints.gridy = 0;

        JLabel passwordLabel = new JLabel(
                "Secondary Password:"
        );

        formPanel.add(
                passwordLabel,
                constraints
        );

        constraints.gridx = 1;
        constraints.weightx = 1;

        passwordField.setEchoChar('\u2022');

        passwordField.setToolTipText(
                "Enter your secondary password."
        );

        formPanel.add(
                passwordField,
                constraints
        );

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0;

        JLabel questionTitleLabel = new JLabel(
                "Security Question:"
        );

        formPanel.add(
                questionTitleLabel,
                constraints
        );

        constraints.gridx = 1;
        constraints.weightx = 1;

        questionLabel.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );

        questionLabel.setToolTipText(
                "Your configured security question."
        );

        formPanel.add(
                questionLabel,
                constraints
        );

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weightx = 0;

        JLabel answerLabel = new JLabel(
                "Security Answer:"
        );

        formPanel.add(
                answerLabel,
                constraints
        );

        constraints.gridx = 1;
        constraints.weightx = 1;

        answerField.setToolTipText(
                "Enter the answer to your security question."
        );

        formPanel.add(
                answerField,
                constraints
        );

        add(
                formPanel,
                BorderLayout.CENTER
        );

        JButton authenticateButton = new JButton(
                "Authenticate"
        );

        JButton cancelButton = new JButton(
                "Cancel"
        );

        authenticateButton.addActionListener(
                this::handleAuthentication
        );

        cancelButton.addActionListener(
                this::handleCancellation
        );

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );

        buttonPanel.add(cancelButton);
        buttonPanel.add(authenticateButton);

        add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        getRootPane().setDefaultButton(
                authenticateButton
        );

        getRootPane().registerKeyboardAction(
                this::handleEscape,
                KeyStroke.getKeyStroke("ESCAPE"),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
        );

    }

    /**
     * Handles authentication using both entered credentials.
     *
     * @param event button action event
     */
    private void handleAuthentication(ActionEvent event) {

        char[] passwordCharacters = passwordField.getPassword();

        char[] answerCharacters = answerField.getText().toCharArray();

        try {

            if (passwordCharacters.length == 0
                    || new String(answerCharacters).trim().isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter both the password and security answer.",
                        "Missing Information",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            String password = new String(passwordCharacters);

            String answer = new String(answerCharacters);

            boolean result = authenticationManager.authenticate(
                    password,
                    answer
            );

            if (result) {

                authenticated = true;

                clearInputFields();

                dispose();

            } else {

                authenticated = false;

                java.awt.Toolkit.getDefaultToolkit().beep();

                JOptionPane.showMessageDialog(
                        this,
                        "Authentication Failed.\n"
                                + "Incorrect password or security answer.",
                        "Authentication Failed",
                        JOptionPane.ERROR_MESSAGE
                );

                passwordField.setText("");
                answerField.setText("");

                passwordField.requestFocusInWindow();
            }

        } catch (RuntimeException exception) {

            authenticated = false;

            java.awt.Toolkit.getDefaultToolkit().beep();

            JOptionPane.showMessageDialog(
                    this,
                    "Authentication could not be completed.\n"
                            + "Please try again.",
                    "Authentication Error",
                    JOptionPane.ERROR_MESSAGE
            );

            passwordField.setText("");
            answerField.setText("");

            passwordField.requestFocusInWindow();

        } finally {

            Arrays.fill(passwordCharacters, '\0');
            Arrays.fill(answerCharacters, '\0');
        }
    }

    /**
     * Handles cancellation.
     *
     * Cancelling never grants access.
     *
     * @param event button action event
     */
    private void handleCancellation(ActionEvent event) {

        authenticated = false;

        clearInputFields();

        dispose();
    }

    /**
     * Handles the Escape key.
     *
     * @param event keyboard action event
     */
    private void handleEscape(ActionEvent event) {

        handleCancellation(event);
    }

    /**
     * Clears the authentication input fields.
     */
    private void clearInputFields() {

        passwordField.setText("");
        answerField.setText("");
    }

    /**
     * Returns whether authentication was successful.
     *
     * @return true if both credentials were verified
     */
    public boolean isAuthenticated() {

        return authenticated;
    }

    /**
     * Returns the password input component.
     *
     * @return password field
     */
    public JPasswordField getPasswordField() {

        return passwordField;
    }

    /**
     * Returns the security answer input component.
     *
     * @return answer field
     */
    public JTextField getAnswerField() {

        return answerField;
    }

    /**
     * Returns the displayed security question.
     *
     * @return security question label
     */
    public JLabel getQuestionLabel() {

        return questionLabel;
    }
}
