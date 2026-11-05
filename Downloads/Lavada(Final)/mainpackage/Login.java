package mainpackage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;

/**
 * Inherits the Swing window behavior and implements ActionListener so the
 * login controls can use this object as their event-handler interface.
 */
public class Login extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static final Color INK = new Color(22, 42, 66);
	private static final Color MUTED = new Color(101, 122, 140);
	private static final Color BLUE = new Color(0, 64, 144);
	private static final Color CYAN = new Color(0, 176, 208);
	private static final String LOGO_PATH = "Images/Lavada Laundry App Logo.png";

	private final JTextField usernameField = new JTextField();
	private final JPasswordField passwordField = new JPasswordField();
	private final JCheckBox showPassword = new JCheckBox("Show password");
	private final JButton signIn = new JButton("Sign in");
	private final ImageIcon logoImage = new ImageIcon(LOGO_PATH);
	private final char passwordEchoChar = passwordField.getEchoChar();

	/** Builds the sign-in window and configures its close behavior. */
	public Login() {
		super("Lavada | Sign in");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			/** Confirms whether the user wants to exit the application. */
			@Override
			public void windowClosing(WindowEvent event) {
				int result = JOptionPane.showConfirmDialog(Login.this,
						"Are you sure you want to exit Lavada?", "Confirm exit",
						JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
		});
		if (logoImage.getIconWidth() > 0) {
			setIconImage(logoImage.getImage());
		}
		setMinimumSize(new Dimension(820, 560));
		setSize(960, 620);
		setLocationRelativeTo(null);
		setContentPane(buildContent());
	}

	/** Arranges the brandi panel and sign-in form side by side. */
	private JPanel buildContent() {
		JPanel content = new JPanel(new GridBagLayout());
		content.setBackground(new Color(247, 250, 252));

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weighty = 1;

		constraints.gridx = 0;
		constraints.weightx = 0.44;
		content.add(buildBrandPanel(), constraints);

		constraints.gridx = 1;
		constraints.weightx = 0.56;
		content.add(buildFormPanel(), constraints);
		return content;
	}

	/** Builds the logo and brandi area of the login screen. */
	private JPanel buildBrandPanel() {
		JPanel panel = new JPanel();
		panel.setBackground(BLUE);
		panel.setLayout(new BorderLayout());
		panel.setBorder(new EmptyBorder(25, 35, 30, 35));

		JLabel logo;
		if (logoImage.getIconWidth() > 0) {
			Image scaledLogo = logoImage.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
			logo = new JLabel(new ImageIcon(scaledLogo));
		} else {
			logo = new JLabel("LAVADA");
			logo.setFont(new Font("Segoe UI", Font.BOLD, 19));
			logo.setForeground(Color.WHITE);
		}
		logo.setPreferredSize(new Dimension(132, 132));
		panel.add(logo, BorderLayout.NORTH);

		JPanel copy = new JPanel();
		copy.setOpaque(false);
		copy.setLayout(new GridBagLayout());

		JLabel eyebrow = new JLabel("LAUNDRY SHOP MANAGEMENT");
		eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 23));
		eyebrow.setForeground(new Color(143, 231, 243));
		eyebrow.setHorizontalAlignment(SwingConstants.CENTER);
		copy.add(eyebrow);
		panel.add(copy, BorderLayout.CENTER);

		JLabel footer = new JLabel("Mabango amoy, before mag esmiring. Hoy!");
		footer.setFont(new Font("Segoe UI", Font.BOLD, 16));
		footer.setForeground(new Color(143, 231, 243));
		panel.add(footer, BorderLayout.SOUTH);
		return panel;
	}

	/** Creates the username, password, visibility, and sign-in controls. */
	private JPanel buildFormPanel() {
		JPanel outer = new JPanel(new GridBagLayout());
		outer.setBackground(new Color(247, 250, 252));
		outer.setBorder(new EmptyBorder(38, 54, 38, 54));

		JPanel form = new JPanel();
		form.setOpaque(false);
		form.setLayout(new javax.swing.BoxLayout(form, javax.swing.BoxLayout.Y_AXIS));

		JLabel heading = new JLabel("Welcome back");
		heading.setFont(new Font("Segoe UI", Font.BOLD, 30));
		heading.setForeground(INK);

		JLabel subtitle = new JLabel("Sign in to manage your laundry shop.");
		subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		subtitle.setForeground(MUTED);

		form.add(heading);
		form.add(javax.swing.Box.createVerticalStrut(8));
		form.add(subtitle);
		form.add(javax.swing.Box.createVerticalStrut(34));
		form.add(createFieldLabel("Username"));
		form.add(javax.swing.Box.createVerticalStrut(8));
		styleField(usernameField);
		form.add(usernameField);
		form.add(javax.swing.Box.createVerticalStrut(20));
		form.add(createFieldLabel("Password"));
		form.add(javax.swing.Box.createVerticalStrut(8));
		styleField(passwordField);
		form.add(passwordField);
		form.add(javax.swing.Box.createVerticalStrut(16));

		showPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		showPassword.setForeground(MUTED);
		showPassword.setOpaque(false);
		showPassword.setFocusPainted(false);
		showPassword.setBorderPainted(false);
		showPassword.setAlignmentX(LEFT_ALIGNMENT);
		showPassword.addActionListener(this);
		form.add(showPassword);
		form.add(javax.swing.Box.createVerticalStrut(20));

		signIn.setFont(new Font("Segoe UI", Font.BOLD, 15));
		signIn.setForeground(Color.WHITE);
		signIn.setBackground(BLUE);
		signIn.setFocusPainted(false);
		signIn.setBorderPainted(false);
		signIn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
		signIn.setAlignmentX(LEFT_ALIGNMENT);
		signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
		signIn.setPreferredSize(new Dimension(280, 48));
		signIn.addActionListener(this);
		passwordField.addActionListener(this);
		usernameField.addActionListener(this);
		form.add(signIn);

		JLabel systemName = new JLabel("Lavada  |  Laundry Shop Management System");
		systemName.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		systemName.setForeground(new Color(145, 158, 154));
		systemName.setAlignmentX(LEFT_ALIGNMENT);
		form.add(javax.swing.Box.createVerticalStrut(40));
		form.add(systemName);

		form.setPreferredSize(new Dimension(340, 420));
		outer.add(form);
		return outer;
	}

	/** Creates a consistently styled label for a login field. */
	private JLabel createFieldLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(new Font("Segoe UI", Font.BOLD, 13));
		label.setForeground(INK);
		label.setAlignmentX(LEFT_ALIGNMENT);
		return label;
	}

	/** Applies the shared font, colors, border, and dimensions to a text field. */
	private void styleField(JTextField field) {
		field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		field.setForeground(INK);
		field.setBackground(Color.WHITE);
		field.setCaretColor(CYAN);
		field.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(211, 226, 235)),
				new EmptyBorder(0, 12, 0, 12)));
		field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
		field.setPreferredSize(new Dimension(300, 46));
		field.setAlignmentX(LEFT_ALIGNMENT);
	}

	/** Validates credentials and opens the admin or matching staff dashboard. */
	private void submitLogin() {
		String username = usernameField.getText().trim();
		char[] password = passwordField.getPassword();

		if (username.isEmpty() || password.length == 0) {
			Arrays.fill(password, '\0');
			JOptionPane.showMessageDialog(this, "Enter your username and password to continue.",
					"Missi credentials", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (AdminAccounts.isUsername(username)) {
			boolean isAdmin = AdminAccounts.authenticate(username, password);
			Arrays.fill(password, '\0');
			passwordField.setText("");
			if (isAdmin) {
				new AdminDashboard().showDashboard();
				dispose();
			} else if (AdminAccounts.isLocked()) {
				recoverAdminAccount();
			} else {
				JOptionPane.showMessageDialog(this, "Invalid username or password. "
						+ AdminAccounts.getLoginAttemptsRemaining() + " attempt(s) remaining.",
						"Invalid credentials", JOptionPane.ERROR_MESSAGE);
			}
			return;
		}

		String employeeId;
		try {
			employeeId = StaffAccounts.authenticate(username, password);
		} finally {
			Arrays.fill(password, '\0');
		}
		passwordField.setText("");
		if (employeeId != null) {
			System.setProperty("laundry.staff.username", username);
			System.setProperty("laundry.staff.employeeId", employeeId);
			new StaffDashboard().setVisible(true);
			dispose();
		} else if (StaffAccounts.isInactive(username)) {
			JOptionPane.showMessageDialog(this, "This staff account is inactive. Contact an administrator.",
					"Account inactive", JOptionPane.ERROR_MESSAGE);
		} else if (StaffAccounts.isLocked(username)) {
			JOptionPane.showMessageDialog(this,
					"This staff account is locked. Contact an administrator to unblock it.",
					"Account locked", JOptionPane.ERROR_MESSAGE);
		} else if (!StaffAccounts.isUsernameInUse(username)) {
			AdminAccounts.recordFailedUsernameAttempt();
			if (AdminAccounts.isLocked()) {
				recoverAdminAccount();
			} else {
				JOptionPane.showMessageDialog(this, "Invalid username or password. "
						+ AdminAccounts.getLoginAttemptsRemaining() + " attempt(s) remaining.",
						"Invalid credentials", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(this, "Invalid username or password. "
					+ StaffAccounts.getLoginAttemptsRemaining(username) + " attempt(s) remaining.",
					"Invalid credentials", JOptionPane.ERROR_MESSAGE);
		}
	}

	/** Guides a locked admin through recovery code and security-question verification. */
	private void recoverAdminAccount() {
		if (AdminAccounts.isSupportRequired()) {
			showAdminSupportMessage();
			return;
		}
		if (!AdminAccounts.isRecoveryConfigured()) {
			showAdminSupportMessage();
			return;
		}

		while (AdminAccounts.getRecoveryAttemptsRemaining() > 0) {
			JPasswordField recoveryCode = new JPasswordField(18);
			int result = JOptionPane.showConfirmDialog(this, recoveryCode,
					"Admin locked - enter recovery code",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result != JOptionPane.OK_OPTION) {
				return;
			}
			char[] code = recoveryCode.getPassword();
			boolean recovered;
			try {
				recovered = AdminAccounts.checkRecoveryCode(code);
			} finally {
				Arrays.fill(code, '\0');
			}
			if (recovered) {
				showAdminRecoveredMessage();
				return;
			}
			if (AdminAccounts.getRecoveryAttemptsRemaining() > 0) {
				JOptionPane.showMessageDialog(this,
						"Incorrect recovery code. " + AdminAccounts.getRecoveryAttemptsRemaining()
								+ " attempt(s) remaining.",
						"Recovery code not accepted", JOptionPane.ERROR_MESSAGE);
			}
		}

		while (AdminAccounts.getAnswerAttemptsRemaining() > 0) {
			String[] questions = AdminAccounts.getQuestions();
			JPasswordField[] answers = new JPasswordField[3];
			JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
			for (int index = 0; index < questions.length; index++) {
				form.add(new JLabel(questions[index]));
				answers[index] = new JPasswordField(24);
				form.add(answers[index]);
			}
			int result = JOptionPane.showConfirmDialog(this, form,
					"Admin locked - verify security questions",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result != JOptionPane.OK_OPTION) {
				return;
			}
			char[][] answerValues = new char[answers.length][];
			for (int index = 0; index < answers.length; index++) {
				answerValues[index] = answers[index].getPassword();
			}
			boolean recovered = AdminAccounts.recoverWithAnswers(answerValues);
			for (char[] answer : answerValues) {
				Arrays.fill(answer, '\0');
			}
			if (recovered) {
				showAdminRecoveredMessage();
				return;
			}
			int remaining = AdminAccounts.getAnswerAttemptsRemaining();
			if (remaining > 0) {
				JOptionPane.showMessageDialog(this,
						"One or more answers were incorrect. " + remaining
								+ " attempt(s) remaining.",
						"Recovery answers not accepted", JOptionPane.ERROR_MESSAGE);
			}
		}
		showAdminSupportMessage();
	}

	private void showAdminRecoveredMessage() {
		JOptionPane.showMessageDialog(this,
				"Admin account recovered. Sign in with your current username and password.",
				"Admin account recovered", JOptionPane.INFORMATION_MESSAGE);
		passwordField.setText("");
		usernameField.setText(AdminAccounts.getUsername());
		passwordField.requestFocusInWindow();
	}

	private void showAdminSupportMessage() {
		JOptionPane.showMessageDialog(this,
				"Admin account recovery could not be verified. Please call IT support.",
				"Contact IT support", JOptionPane.ERROR_MESSAGE);
	}

	/** Implements the interface callback; this override is dispatched polymorphically for UI events. */
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == showPassword) {
			passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : passwordEchoChar);
		} else if (source == usernameField) {
			passwordField.requestFocusInWindow();
		} else if (source == signIn || source == passwordField) {
			submitLogin();
		}
	}

}
