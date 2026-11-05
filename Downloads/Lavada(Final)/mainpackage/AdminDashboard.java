package mainpackage;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.RowFilter;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

/**
 * Builds the admin user interface from reusable Swing components and delegates
 * business data operations to the inventory, service, payroll, and staff models.
 */
public class AdminDashboard extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;

    private static final Color NAVY = new Color(45, 57, 63);
    private static final Color NAVY_LIGHT = new Color(55, 69, 76);
    private static final Color BLUE = new Color(0, 64, 144);
    private static final Color TEAL = new Color(0, 176, 208);
    private static final Color CANVAS = new Color(245, 247, 251);
    private static final Color TEXT = new Color(39, 52, 69);
    private static final Color MUTED = new Color(133, 145, 160);
    private static final Color BORDER = new Color(231, 235, 241);
    private static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final String DASHBOARD = "Dashboard Home";
    private static final String ADMIN_ACCOUNT = "Manage Admin Account";
    private static final String LOGO_PATH = "Images/Lavada Laundry App Logo.png";

    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageContainer = new JPanel(pageLayout);
    private final Map<String, JButton> navigationButtons = new LinkedHashMap<>();
    private final Map<String, DashboardPage> dashboardPages = new LinkedHashMap<>();
    private final JLabel breadcrumbLabel = new JLabel();
    private JLabel lowStockCountLabel;
    private JLabel financeSalesValue;
    private JLabel financeExpensesValue;
    private JLabel financeProfitValue;
    private JLabel financePayrollValue;
    private JButton selectedNavigationButton;
    private JButton manageAdminButton;
    private DefaultTableModel inventoryControlModel;

    /** Builds the admin window and installs its sidebar and page content. */
    public AdminDashboard() {
        super("Lavada | Admin Dashboard");
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        ImageIcon logo = new ImageIcon(LOGO_PATH);
        if (logo.getIconWidth() > 0) {
            setIconImage(logo.getImage());
        }
        // Inheritance and runtime polymorphism: this adapter specializes the close callback.
        addWindowListener(new WindowAdapter() {
            /** Confirms closing the admin dashboard and returning to login. */
            @Override
            public void windowClosing(WindowEvent event) {
                logout();
            }
        });
        setMinimumSize(new Dimension(940, 650));
        setSize(1180, 790);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CANVAS);
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createMainArea(), BorderLayout.CENTER);
        setContentPane(root);
    }

    /** Displays the completed admin window. */
    public void showDashboard() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
        if (!AdminAccounts.isRecoveryConfigured()) {
            SwingUtilities.invokeLater(this::showAdminAccountPage);
        }
    }

    /** Creates section navigation and the logout control. */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(190, 0));
        sidebar.setBackground(NAVY);

        JPanel navigation = new JPanel();
        navigation.setBackground(NAVY);
        navigation.setBorder(BorderFactory.createEmptyBorder(12, 0, 8, 0));

        String[] sections = {
            DASHBOARD, "Customer Management", "Laundry Oversight",
            "Service Catalog", "Inventory Control", "Employee Management",
            "Expenses & Profit"
        };
        navigation.setLayout(new GridLayout(sections.length, 1, 0, 8));
        for (String section : sections) {
            JButton button = createNavigationButton(section);
            navigationButtons.put(section, button);
            navigation.add(button);
        }
        sidebar.add(navigation, BorderLayout.CENTER);

        manageAdminButton = createNavigationButton(ADMIN_ACCOUNT);
        JButton switchToStaff = createNavigationButton("Switch to Staff (Test)");
        JButton logout = createNavigationButton("Logout");
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(NAVY);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        footer.add(manageAdminButton);
        footer.add(Box.createVerticalStrut(8));
        footer.add(switchToStaff);
        footer.add(Box.createVerticalStrut(8));
        footer.add(logout);
        sidebar.add(footer, BorderLayout.SOUTH);
        selectNavigationButton(navigationButtons.get(DASHBOARD));
        return sidebar;
    }

    /** Styles a sidebar button and connects it to its matching dashboard section. */
    private JButton createNavigationButton(String title) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setFont(BODY);
        button.setForeground(Color.WHITE);
        button.setBackground(NAVY_LIGHT);
        button.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 10));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setPreferredSize(new Dimension(190, 44));
        JLabel name = new JLabel(title);
        name.setForeground(Color.WHITE);
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.add(name, BorderLayout.CENTER);
        button.setActionCommand(title);
        button.addActionListener(this);
        return button;
    }

    /** Routes sidebar button events to the matching dashboard action. */
    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if ("Logout".equals(command)) {
            logout();
        } else if ("Switch to Staff (Test)".equals(command)) {
            switchToStaffForTesting();
        } else if (event.getSource() instanceof JButton button) {
            showSection(command, button);
        }
    }

    /** Switches pages and synchronizes the selected navigation and breadcrumb. */
    private void showSection(String section, JButton button) {
        selectNavigationButton(button);
        breadcrumbLabel.setText("<html><font color='#8996A7'>WORKSPACE</font>  /  <b>"
            + section.toUpperCase() + "</b></html>");
        pageLayout.show(pageContainer, section);
        DashboardPage page = dashboardPages.get(section);
        if (page != null) {
            // Interface dispatch lets each page perform its own activation work.
            page.onPageShown();
        }
    }

    /** Applies the selected appearance to one sidebar button. */
    private void selectNavigationButton(JButton button) {
        if (selectedNavigationButton != null) {
            selectedNavigationButton.setBackground(NAVY_LIGHT);
        }
        selectedNavigationButton = button;
        selectedNavigationButton.setBackground(BLUE);
        for (Component child : selectedNavigationButton.getComponents()) {
            if (child instanceof JLabel label) {
                label.setForeground(Color.WHITE);
            }
        }
    }

    /** Confirms logout before closing the admin view and returning to login. */
    private void logout() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit the admin dashboard?",
            "Confirm logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            dispose();
            new Login().setVisible(true);
        }
    }

    /** Opens the staff dashboard directly using the default test employee identity. */
    private void switchToStaffForTesting() {
        System.setProperty("laundry.staff.username", StaffAccounts.getUsername("EMP-001"));
        System.setProperty("laundry.staff.employeeId", "EMP-001");
        dispose();
        new StaffDashboard().setVisible(true);
    }

    /** Assembles the header and scrollable area that hosts dashboard pages. */
    private JPanel createMainArea() {
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(CANVAS);
        mainArea.add(createHeader(), BorderLayout.NORTH);

        pageContainer.setBackground(CANVAS);
        pageContainer.add(createDashboardPage(), DASHBOARD);
        for (String section : navigationButtons.keySet()) {
            if (!DASHBOARD.equals(section)) {
                AbstractDashboardPage page = "Expenses & Profit".equals(section)
                    ? createFinancePage() : createModulePage(section);
                pageContainer.add(page, section);
                dashboardPages.put(section, page);
            }
        }
        AbstractDashboardPage accountPage = createAdminAccountPage();
        pageContainer.add(accountPage, ADMIN_ACCOUNT);
        dashboardPages.put(ADMIN_ACCOUNT, accountPage);

        JScrollPane scrollPane = new JScrollPane(pageContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CANVAS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(9, 0));
        mainArea.add(scrollPane, BorderLayout.CENTER);
        return mainArea;
    }

    /** Creates the workspace breadcrumb, current date, and admin avatar. */
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            BorderFactory.createEmptyBorder(0, 30, 0, 30)));
        header.setPreferredSize(new Dimension(0, 72));

        breadcrumbLabel.setText("<html><font color='#8996A7'>WORKSPACE</font>  /  <b>DASHBOARD</b></html>");
        breadcrumbLabel.setForeground(TEXT);
        breadcrumbLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JPanel right = new JPanel(new BorderLayout(12, 0));
        right.setOpaque(false);
        JLabel date = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")));
        date.setForeground(MUTED);
        date.setFont(BODY);
        JLabel avatar = new JLabel("Admin", SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(222, 244, 241));
        avatar.setForeground(TEAL);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        avatar.setPreferredSize(new Dimension(64, 36));
        right.add(date, BorderLayout.CENTER);
        right.add(avatar, BorderLayout.EAST);
        header.add(breadcrumbLabel, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    /** Lays out the admin overview, summary cards, and income panels. */
    private JPanel createDashboardPage() {
        JPanel page = new JPanel();
        page.setBackground(CANVAS);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(BorderFactory.createEmptyBorder(26, 30, 30, 30));

        JLabel title = heading("Welcome back, Admin", 25);
        JLabel subtitle = text("Here's what's happening at your laundry shop today.", MUTED);
        subtitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 20, 0));
        page.add(title);
        page.add(subtitle);
        page.add(createSummaryCards());
        page.add(Box.createVerticalStrut(18));
        page.add(createOverviewPanels());
        page.add(Box.createVerticalStrut(18));
        page.add(createIncomeReport());
        return page;
    }

    /** Arranges the four top-level business summary cards. */
    private JPanel createSummaryCards() {
        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 119));
        Object[] summary = StaffDashboard.getAdminDashboardSummary();
        cards.add(createStatCard("LAUNDRIES TODAY", String.valueOf(summary[0]), "ORD",
            new Color(53, 132, 213), new Color(233, 243, 255)));
        cards.add(createStatCard("IN PROGRESS", String.valueOf(summary[1]), "WIP",
            new Color(218, 148, 50), new Color(255, 246, 229)));
        cards.add(createStatCard("READY FOR PICKUP", String.valueOf(summary[2]), "RDY",
            TEAL, new Color(227, 247, 244)));
        cards.add(createStatCard("SALES TODAY", String.valueOf(summary[3]), "P",
            new Color(72, 166, 111), new Color(233, 248, 238)));
        return cards;
    }

    /** Builds a single overview card with its value, badge, and accent color. */
    private JPanel createStatCard(String label, String value, String badge, Color accent, Color badgeBackground) {
        RoundedPanel card = new RoundedPanel(15, Color.WHITE);
        card.setLayout(new BorderLayout(10, 9));
        card.setBorder(BorderFactory.createEmptyBorder(16, 17, 14, 17));
        JLabel title = text(label, MUTED);
        title.setFont(new Font("Segoe UI", Font.BOLD, 10));
        JLabel amount = heading(value, 25);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.CENTER);
        JLabel mark = new JLabel(badge, SwingConstants.CENTER);
        mark.setOpaque(true);
        mark.setBackground(badgeBackground);
        mark.setForeground(accent);
        mark.setFont(new Font("Segoe UI", Font.BOLD, 9));
        mark.setPreferredSize(new Dimension(38, 30));
        top.add(mark, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);
        card.add(amount, BorderLayout.CENTER);
        card.add(new AccentBar(accent), BorderLayout.SOUTH);
        return card;
    }

    /** Combines attention links and sales summaries into the dashboard overview. */
    private JPanel createOverviewPanels() {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 276));

        RoundedPanel attention = createSectionCard("Needs attention", "Review items that may need a quick follow-up");
        attention.setLayout(new BoxLayout(attention, BoxLayout.Y_AXIS));
        Object[] summary = StaffDashboard.getAdminDashboardSummary();
        JButton lowStockRow = createAttentionRow("Low-stock supplies",
            InventoryData.getLowStockCount() + " items", "Inventory Control",
            new Color(222, 153, 54));
        lowStockCountLabel = (JLabel) lowStockRow.getClientProperty("countLabel");
        attention.add(lowStockRow);
        attention.add(createAttentionRow("Laundries in queue", summary[13] + " laundries",
            "Laundry Oversight", new Color(0, 176, 208)));
        attention.add(createAttentionRow("Laundries ready for pickup", summary[4] + " laundries",
            "Laundry Oversight", TEAL));
        attention.add(createAttentionRow("Unpaid laundries", summary[5] + " laundries",
            "Laundry Oversight", new Color(217, 111, 91)));

        RoundedPanel sales = createSectionCard("Sales overview", "Collected payments by period");
        sales.setLayout(new BoxLayout(sales, BoxLayout.Y_AXIS));
        JPanel salesGrid = new JPanel(new GridLayout(0, 2, 10, 10));
        salesGrid.setOpaque(false);
        salesGrid.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        salesGrid.add(createSalesTile("TODAY", String.valueOf(summary[7])));
        salesGrid.add(createSalesTile("THIS WEEK", String.valueOf(summary[8])));
        salesGrid.add(createSalesTile("THIS MONTH", String.valueOf(summary[9])));
        salesGrid.add(createSalesTile("COMPLETED LAUNDRIES", summary[11] + " laundries"));
        sales.add(salesGrid);
        row.add(attention);
        row.add(sales);
        return row;
    }

    /** Creates a reusable white card with a title and descriptive subtitle. */
    private RoundedPanel createSectionCard(String title, String subtitle) {
        RoundedPanel card = new RoundedPanel(15, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(17, 18, 15, 18));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel caption = text(subtitle, MUTED);
        caption.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(heading(title, 16));
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(caption);
        card.add(titleBlock, BorderLayout.NORTH);
        return card;
    }

    /** Builds a clickable dashboard alert that navigates to its related section. */
    private JButton createAttentionRow(String title, String count, String section, Color accent) {
        JButton item = new JButton();
        item.setLayout(new BorderLayout(11, 0));
        item.setOpaque(true);
        item.setBackground(CANVAS);
        item.setContentAreaFilled(true);
        item.setFocusPainted(false);
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        JLabel dot = new JLabel(".", SwingConstants.CENTER);
        dot.setForeground(accent);
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dot.setPreferredSize(new Dimension(15, 18));
        JLabel name = text(title, TEXT);
        name.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel amount = text(count + "   >", accent);
        amount.setFont(new Font("Segoe UI", Font.BOLD, 11));
        item.add(dot, BorderLayout.WEST);
        item.add(name, BorderLayout.CENTER);
        item.add(amount, BorderLayout.EAST);
        item.putClientProperty("countLabel", amount);
        item.addActionListener(event -> showSection(section, navigationButtons.get(section)));
        return item;
    }

    /** Creates a compact labeled value tile for the sales overview. */
    private JPanel createSalesTile(String label, String value) {
        RoundedPanel tile = new RoundedPanel(10, new Color(247, 249, 252));
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBorder(BorderFactory.createEmptyBorder(10, 11, 9, 8));
        JLabel title = text(label, MUTED);
        title.setFont(new Font("Segoe UI", Font.BOLD, 9));
        JLabel amount = heading(value, 16);
        amount.setForeground(TEAL);
        tile.add(title);
        tile.add(Box.createVerticalStrut(5));
        tile.add(amount);
        return tile;
    }

    /** Builds the daily, weekly, and monthly income report panel. */
    private JPanel createIncomeReport() {
        RoundedPanel report = createSectionCard("Income report", "A quick snapshot of your shop's income");
        report.setLayout(new BorderLayout(0, 14));
        report.setBorder(BorderFactory.createEmptyBorder(17, 18, 17, 18));
        LocalDate today = LocalDate.now();
        String date = today.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        String weekStart = today.minusDays(6).format(DateTimeFormatter.ofPattern("MMM d"));
        String monthStart = today.withDayOfMonth(1).format(DateTimeFormatter.ofPattern("MMM d"));
        Object[] summary = StaffDashboard.getAdminDashboardSummary();
        JPanel periods = new JPanel(new GridLayout(1, 3, 11, 0));
        periods.setOpaque(false);
        periods.add(createIncomeTile("DAILY INCOME", date, String.valueOf(summary[12])));
        periods.add(createIncomeTile("WEEKLY INCOME", weekStart + " - " + date,
            String.valueOf(summary[8])));
        periods.add(createIncomeTile("MONTHLY INCOME", monthStart + " - " + date,
            String.valueOf(summary[9])));
        report.add(periods, BorderLayout.CENTER);
        report.setAlignmentX(Component.LEFT_ALIGNMENT);
        report.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        return report;
    }

    /** Creates one income-period tile showi its date range and amount. */
    private JPanel createIncomeTile(String title, String period, String amountText) {
        RoundedPanel tile = new RoundedPanel(10, new Color(247, 249, 252));
        tile.setLayout(new BorderLayout(0, 12));
        tile.setBorder(BorderFactory.createEmptyBorder(12, 13, 12, 13));
        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        JLabel caption = text(title, TEXT);
        caption.setFont(new Font("Segoe UI", Font.BOLD, 10));
        JLabel dates = text(period, MUTED);
        dates.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        labels.add(caption);
        labels.add(Box.createVerticalStrut(4));
        labels.add(dates);
        JLabel amount = heading(amountText, 19);
        amount.setForeground(TEAL);
        tile.add(labels, BorderLayout.NORTH);
        tile.add(amount, BorderLayout.SOUTH);
        return tile;
    }

    /** Assembles a module page with metrics and its searchable data table. */
    private ModulePage createModulePage(String section) {
        ModulePage page = new ModulePage();

        JLabel title = heading(section, 25);
        JLabel subtitle = text(getModuleSubtitle(section), MUTED);
        subtitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 18, 0));
        page.add(title);
        page.add(subtitle);

        DefaultTableModel model = createModuleModel(section);
        JLabel totalValue = heading(String.valueOf(model.getRowCount()), 20);
        JLabel activeValue = heading(getActiveCount(model), 20);
        model.addTableModelListener(event -> {
            totalValue.setText(String.valueOf(model.getRowCount()));
            activeValue.setText(getActiveCount(model));
        });
        page.add(createModuleMetrics(totalValue, activeValue));
        page.add(Box.createVerticalStrut(16));
        page.add(createDataTablePanel(section, model));
        return page;
    }

    /** Builds the expense editor and collected-sales profit summary. */
    private FinancePage createFinancePage() {
        FinancePage page = new FinancePage();
        page.add(heading("Expenses & Profit", 25));
        JLabel subtitle = text("Net profit subtracts current bills and monthly staff salaries from collected sales.",
            MUTED);
        subtitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 18, 0));
        page.add(subtitle);

        financeSalesValue = heading("", 20);
        financeExpensesValue = heading("", 20);
        financeProfitValue = heading("", 20);
        JPanel metrics = new JPanel(new GridLayout(1, 3, 12, 0));
        metrics.setOpaque(false);
        metrics.setAlignmentX(Component.LEFT_ALIGNMENT);
        metrics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        metrics.add(createMetricCard("COLLECTED SALES", financeSalesValue, TEAL));
        metrics.add(createMetricCard("TOTAL EXPENSES (WITH PAYROLL)", financeExpensesValue,
            new Color(53, 132, 213)));
        metrics.add(createMetricCard("NET PROFIT", financeProfitValue, new Color(125, 113, 202)));
        page.add(metrics);
        page.add(Box.createVerticalStrut(16));

        RoundedPanel billCard = createSectionCard("Shop bills",
            "Fixed and additional utility bills are deducted from recorded payments.");
        billCard.setBorder(BorderFactory.createEmptyBorder(17, 18, 17, 18));
        billCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        JTextField electricity = new JTextField(String.format("%.2f", ShopExpenses.getElectricityBill()), 12);
        JTextField water = new JTextField(String.format("%.2f", ShopExpenses.getWaterBill()), 12);
        JTextField inventory = new JTextField(String.format("%.2f", ShopExpenses.getInventoryBill()), 12);
        financePayrollValue = new JLabel();
        InputValidation.decimalOnly(electricity);
        InputValidation.decimalOnly(water);
        InputValidation.decimalOnly(inventory);
        form.add(createBillInputRow("Electricity bill (P)", electricity));
        form.add(createBillInputRow("Water bill (P)", water));
        form.add(createBillInputRow("Inventory bill (P)", inventory));
        JPanel payrollRow = createBillInputRow("Monthly staff salaries", financePayrollValue);
        payrollRow.setBorder(BorderFactory.createEmptyBorder(4, 0, 10, 0));
        form.add(payrollRow);

        JPanel utilitySection = new JPanel();
        utilitySection.setOpaque(false);
        utilitySection.setLayout(new BoxLayout(utilitySection, BoxLayout.Y_AXIS));
        JPanel utilityHeading = new JPanel(new BorderLayout(12, 0));
        utilityHeading.setOpaque(false);
        JLabel utilityLabel = new JLabel("Additional utility bills");
        utilityLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        utilityLabel.setForeground(TEXT);
        JButton addUtility = createSecondaryActionButton("+ Add bill");
        JPanel headingContents = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headingContents.setOpaque(false);
        headingContents.add(utilityLabel);
        headingContents.add(Box.createHorizontalStrut(12));
        headingContents.add(addUtility);
        utilityHeading.add(headingContents, BorderLayout.WEST);
        utilitySection.add(utilityHeading);

        JPanel utilityRows = new JPanel();
        utilityRows.setOpaque(false);
        utilityRows.setLayout(new BoxLayout(utilityRows, BoxLayout.Y_AXIS));
        utilitySection.add(utilityRows);
        ArrayList<UtilityBillFields> utilityFields = new ArrayList<UtilityBillFields>();
        for (ShopExpenses.UtilityBill bill : ShopExpenses.getAdditionalUtilityBills()) {
            addUtilityBillRow(bill.getName(), String.format("%.2f", bill.getAmount()),
                utilityRows, utilityFields, billCard, page);
        }
        addUtility.addActionListener(event ->
            addUtilityBillRow("", "", utilityRows, utilityFields, billCard, page));
        form.add(utilitySection);
        JPanel billContent = new JPanel(new BorderLayout());
        billContent.setOpaque(false);
        billContent.add(form, BorderLayout.NORTH);
        billCard.add(billContent, BorderLayout.CENTER);

        JButton save = createSecondaryActionButton("Save Bills");
        save.addActionListener(event -> {
            String[] values = {electricity.getText().trim(), water.getText().trim(),
                inventory.getText().trim()};
            for (String value : values) {
                if (!InputValidation.isDecimal(value)) {
                    showInputError("Enter a valid non-negative amount for each bill.");
                    return;
                }
            }
            try {
                ArrayList<ShopExpenses.UtilityBill> otherBills =
                    new ArrayList<ShopExpenses.UtilityBill>();
                for (UtilityBillFields fields : utilityFields) {
                    String name = fields.name.getText().trim();
                    String amount = fields.amount.getText().trim();
                    if (name.isEmpty()) {
                        showInputError("Enter a name for each additional utility bill.");
                        return;
                    }
                    if (!InputValidation.isDecimal(amount)) {
                        showInputError("Enter a valid amount for " + name + ".");
                        return;
                    }
                    otherBills.add(new ShopExpenses.UtilityBill(name, Double.parseDouble(amount)));
                }
                ShopExpenses.update(Double.parseDouble(values[0]), Double.parseDouble(values[1]),
                    Double.parseDouble(values[2]), otherBills);
                refreshFinanceMetrics();
                JOptionPane.showMessageDialog(this, "Shop bills saved.",
                    "Bills updated", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException exception) {
                showInputError(exception.getMessage());
            }
        });
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.add(save);
        billCard.add(actions, BorderLayout.SOUTH);
        billCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,
            billCard.getPreferredSize().height));
        page.add(billCard);
        page.onPageShown();
        return page;
    }

    /** Creates a compact, left-aligned row for a fixed bill or payroll amount. */
    private JPanel createBillInputRow(String title, Component value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel(title + ":");
        label.setPreferredSize(new Dimension(190, 25));
        label.setFont(BODY);
        label.setForeground(TEXT);
        row.add(label);
        row.add(value);
        return row;
    }

    /** Adds a removable named utility-bill row to the editor. */
    private void addUtilityBillRow(String name, String amount, JPanel rows,
            ArrayList<UtilityBillFields> utilityFields, RoundedPanel billCard, JPanel page) {
        JTextField nameField = new JTextField(name, 18);
        JTextField amountField = new JTextField(amount, 12);
        InputValidation.decimalOnly(amountField);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel nameLabel = new JLabel("Name");
        nameLabel.setPreferredSize(new Dimension(48, 24));
        JLabel amountLabel = new JLabel("Amount (P)");
        amountLabel.setPreferredSize(new Dimension(80, 24));
        row.add(nameLabel);
        row.add(nameField);
        row.add(amountLabel);
        row.add(amountField);
        JButton remove = createSecondaryActionButton("Remove");
        remove.addActionListener(event -> {
            rows.remove(row);
            utilityFields.removeIf(fields -> fields.row == row);
            rows.revalidate();
            rows.repaint();
            resizeBillCard(billCard, page);
        });
        row.add(remove);
        rows.add(row);
        utilityFields.add(new UtilityBillFields(nameField, amountField, row));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        rows.revalidate();
        rows.repaint();
        resizeBillCard(billCard, page);
    }

    /** Keeps the bill editor's height fitted to its utility rows. */
    private void resizeBillCard(RoundedPanel billCard, JPanel page) {
        billCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,
            billCard.getPreferredSize().height));
        page.revalidate();
    }

    private static final class UtilityBillFields {
        private final JTextField name;
        private final JTextField amount;
        private final JPanel row;

        private UtilityBillFields(JTextField name, JTextField amount, JPanel row) {
            this.name = name;
            this.amount = amount;
            this.row = row;
        }
    }

    /** Refreshes the finance summary using recorded payments and saved bills. */
    private void refreshFinanceMetrics() {
        double sales = StaffDashboard.getTotalSales();
        double expenses = ShopExpenses.getTotalExpenses() + StaffPayroll.getTotalMonthlySalary();
        financeSalesValue.setText(String.format("P%,.2f", sales));
        financeExpensesValue.setText(String.format("P%,.2f", expenses));
        financeProfitValue.setText(String.format("P%,.2f", sales - expenses));
        financePayrollValue.setText(String.format("P%,.2f / month",
            StaffPayroll.getTotalMonthlySalary()));
    }

    /** Returns the explanatory text associated with a dashboard module. */
    private String getModuleSubtitle(String section) {
        switch (section) {
            case "Customer Management":
                return "Manage customer contact details and their laundry history.";
            case "Laundry Oversight":
                return "Track incoming laundry, laundry progress, and pickup status.";
            case "Service Catalog":
                return "Manage laundry services, pricing, and turnaround times.";
            case "Inventory Control":
                return "Track stock, reorder levels, and the cost per unit used on laundries.";
            case "Employee Management":
                return "Manage employee details, login credentials, and account access.";
            default:
                return "Manage your laundry shop records.";
        }
    }

    /** Supplies a module's columns and initial records in a read-only table model. */
    private DefaultTableModel createModuleModel(String section) {
        String[] columns;
        Object[][] rows;
        switch (section) {
            case "Customer Management":
                columns = new String[] {"Customer ID", "Full name", "Phone", "Address", "Total laundries"};
                rows = StaffDashboard.getCustomerManagementRows();
                break;
            case "Laundry Oversight":
                columns = new String[] {"Laundry ID", "Customer", "Service", "Date received",
                    "Payment Status", "Original Price", "Discount", "Discounted Total", "Status",
                    "Added By"};
                rows = StaffDashboard.getOrderOversightRows();
                break;
            case "Service Catalog":
                columns = new String[] {"Service ID", "Service name", "Price", "Unit", "Status"};
                rows = StaffDashboard.getServiceCatalogRows();
                break;
            case "Inventory Control":
                columns = new String[] {"Item ID", "Supply item", "Category", "Stock",
                    "Reorder level", "Unit", "Cost per Unit", "Status"};
                rows = new Object[InventoryData.getItems().size()][columns.length];
                for (int index = 0; index < InventoryData.getItems().size(); index++) {
                    InventoryData.Item item = InventoryData.getItems().get(index);
                    rows[index] = new Object[] {
                        item.getId(), item.getName(), item.getCategory(),
                        formatInventoryStock(item, item.getStock()),
                        formatInventoryStock(item, item.getReorderLevel()),
                        item.getUnit(), String.format("%.2f", item.getUnitCost()),
                        item.isLowStock() ? "Low Stock" : "In Stock"
                    };
                }
                break;
            case "Employee Management":
                columns = new String[] {"Employee ID", "Full name", "Role", "Phone", "Email",
                    "Shift", "Employment Status", "Monthly Salary", "Username", "Account Status"};
                rows = new Object[][] {
                    {"EMP-001", "Ana Garcia", "Laundry Attendant", "0917 555 0211", "ana.g@email.com",
                        "Morning", "Active", formatSalary("EMP-001"), StaffAccounts.getUsername("EMP-001"),
                        StaffAccounts.getStatus("EMP-001")},
                    {"EMP-002", "Mark Flores", "Cashier", "0918 555 0142", "mark.f@email.com",
                        "Afternoon", "Active", formatSalary("EMP-002"), StaffAccounts.getUsername("EMP-002"),
                        StaffAccounts.getStatus("EMP-002")},
                    {"EMP-003", "Liza Navarro", "Laundry Attendant", "0920 555 0283", "liza.n@email.com",
                        "Morning", "Active", formatSalary("EMP-003"), StaffAccounts.getUsername("EMP-003"),
                        StaffAccounts.getStatus("EMP-003")},
                    {"EMP-004", "Rico Bautista", "Delivery Staff", "0995 555 0294", "rico.b@email.com",
                        "Afternoon", "Inactive", formatSalary("EMP-004"), StaffAccounts.getUsername("EMP-004"),
                        StaffAccounts.getStatus("EMP-004")}
                };
                break;
            default:
                columns = new String[] {"ID", "Name", "Status"};
                rows = new Object[0][columns.length];
        }

        DefaultTableModel model = new DefaultTableModel(rows, columns) {
            private static final long serialVersionUID = 1L;

            /** Keeps module rows read-only; changes are made through their action dialogs. */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        if ("Inventory Control".equals(section)) {
            inventoryControlModel = model;
        }
        return model;
    }

    /** Arranges record totals, active counts, and the last-updated date. */
    private JPanel createModuleMetrics(JLabel totalValue, JLabel activeValue) {
        JPanel metrics = new JPanel(new GridLayout(1, 3, 12, 0));
        metrics.setOpaque(false);
        metrics.setAlignmentX(Component.LEFT_ALIGNMENT);
        metrics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        metrics.add(createMetricCard("TOTAL RECORDS", totalValue, TEAL));
        metrics.add(createMetricCard("ACTIVE / CURRENT", activeValue, new Color(53, 132, 213)));
        metrics.add(createMetricCard("LAST UPDATED", LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
            new Color(125, 113, 202)));
        return metrics;
    }

    /** Counts rows that are not marked inactive or on leave. */
    private String getActiveCount(DefaultTableModel model) {
        int statusColumn = model.findColumn("Status");
        if (statusColumn < 0) {
            statusColumn = model.findColumn("Employment Status");
        }
        if (statusColumn < 0) {
            return String.valueOf(model.getRowCount());
        }
        int count = 0;
        for (int row = 0; row < model.getRowCount(); row++) {
            String status = String.valueOf(model.getValueAt(row, statusColumn));
            if (!"Inactive".equalsIgnoreCase(status)) {
                count++;
            }
        }
        return String.valueOf(count);
    }

    /**
     * Converts a metric stri into a label for the shared card builder.
     * This overload and the JLabel overload below demonstrate method overloading.
     */
    private JPanel createMetricCard(String title, String value, Color accent) {
        return createMetricCard(title, heading(value, 20), accent);
    }

    /** Overloaded card builder that accepts an existing value label. */
    private JPanel createMetricCard(String title, JLabel amount, Color accent) {
        RoundedPanel card = new RoundedPanel(13, Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(12, 15, 10, 12));
        JLabel label = text(title, MUTED);
        label.setFont(new Font("Segoe UI", Font.BOLD, 9));
        amount.setForeground(accent);
        card.add(label);
        card.add(Box.createVerticalStrut(4));
        card.add(amount);
        return card;
    }

    /** Builds a styled module table with search and section-specific actions. */
    private JPanel createDataTablePanel(String section, DefaultTableModel model) {
        RoundedPanel panel = new RoundedPanel(15, Color.WHITE);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 17, 16, 17));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 460));

        JLabel tableTitle = heading(section + " list", 16);
        JButton addButton = new JButton("+  Add " + getSingularName(section));
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addButton.setForeground(Color.WHITE);
        addButton.setBackground(TEAL);
        addButton.setFocusPainted(false);
        addButton.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        addButton.addActionListener(event -> {
            if ("Laundry Oversight".equals(section)) {
                showAddOrderDialog(model);
            } else {
                showAddRecordDialog(section, model);
            }
        });
        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionButtons.setOpaque(false);
        if ("Employee Management".equals(section)) {
            JButton salaryButton = createSecondaryActionButton("Set Salary");
            salaryButton.addActionListener(event ->
                setEmployeeSalary((JTable) panel.getClientProperty("dataTable"), model));
            actionButtons.add(salaryButton);
            JButton employeeInfoButton = createSecondaryActionButton("Manage Employee Info");
            employeeInfoButton.addActionListener(event ->
                editEmployeeInformation((JTable) panel.getClientProperty("dataTable"), model));
            actionButtons.add(employeeInfoButton);
            JButton accountButton = new JButton("Manage Account");
            accountButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            accountButton.setForeground(TEXT);
            accountButton.setBackground(new Color(246, 248, 251));
            accountButton.setFocusPainted(false);
            accountButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            accountButton.addActionListener(event ->
                manageEmployeeAccount((JTable) panel.getClientProperty("dataTable"), model));
            actionButtons.add(accountButton);
        }
        if ("Customer Management".equals(section)) {
            JButton editButton = createSecondaryActionButton("Edit Customer");
            editButton.addActionListener(event ->
                editCustomer((JTable) panel.getClientProperty("dataTable"), model));
            JButton deleteButton = createSecondaryActionButton("Delete Customer");
            deleteButton.addActionListener(event ->
                deleteCustomer((JTable) panel.getClientProperty("dataTable"), model));
            actionButtons.add(editButton);
            actionButtons.add(deleteButton);
        }
        if ("Inventory Control".equals(section)) {
            JButton editButton = createSecondaryActionButton("Edit Item");
            editButton.addActionListener(event ->
                editInventoryItem((JTable) panel.getClientProperty("dataTable"), model));
            JButton deleteButton = createSecondaryActionButton("Delete Item");
            deleteButton.addActionListener(event ->
                deleteInventoryItem((JTable) panel.getClientProperty("dataTable"), model));
            actionButtons.add(editButton);
            actionButtons.add(deleteButton);
        }
        if ("Laundry Oversight".equals(section)) {
            JButton infoButton = new JButton("View Info");
            infoButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            infoButton.setForeground(TEXT);
            infoButton.setBackground(new Color(246, 248, 251));
            infoButton.setFocusPainted(false);
            infoButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            infoButton.addActionListener(event ->
                showOrderInfo((JTable) panel.getClientProperty("dataTable"), model));
            actionButtons.add(infoButton);
            JButton editButton = createSecondaryActionButton("Edit Laundry");
            editButton.addActionListener(event ->
                editOrder((JTable) panel.getClientProperty("dataTable"), model));
            JButton cancelButton = createSecondaryActionButton("Cancel Laundry");
            cancelButton.addActionListener(event ->
                cancelOrder((JTable) panel.getClientProperty("dataTable"), model));
            JButton deleteButton = createSecondaryActionButton("Delete Laundry");
            deleteButton.addActionListener(event ->
                deleteOrder((JTable) panel.getClientProperty("dataTable"), model));
            JButton deleteAllButton = createSecondaryActionButton("Delete All Laundries");
            deleteAllButton.addActionListener(event -> deleteAllOrders(model));
            actionButtons.add(editButton);
            actionButtons.add(cancelButton);
            actionButtons.add(deleteButton);
            actionButtons.add(deleteAllButton);
        }
        if ("Service Catalog".equals(section)) {
            JButton editButton = createSecondaryActionButton("Edit Service");
            editButton.addActionListener(event ->
                editService((JTable) panel.getClientProperty("dataTable"), model));
            JButton deleteButton = createSecondaryActionButton("Delete Service");
            deleteButton.addActionListener(event ->
                deleteService((JTable) panel.getClientProperty("dataTable"), model));
            actionButtons.add(editButton);
            actionButtons.add(deleteButton);
        }
        actionButtons.add(addButton);

        JTextField searchField = new JTextField();
        searchField.setFont(BODY);
        searchField.setToolTipText("Type a word and press Enter to search");
        boolean hasAdjacentSearchButton = "Laundry Oversight".equals(section)
            || "Service Catalog".equals(section)
            || "Inventory Control".equals(section)
            || "Employee Management".equals(section);
        searchField.setPreferredSize(new Dimension(hasAdjacentSearchButton ? 160 : 220, 36));
        searchField.addActionListener(event ->
            applyTableSearch(searchField, (JTable) panel.getClientProperty("dataTable")));
        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchControls.setOpaque(false);
        searchControls.add(searchField);
        if (hasAdjacentSearchButton) {
            JButton searchButton = createSecondaryActionButton("Search");
            searchButton.addActionListener(event ->
                applyTableSearch(searchField, (JTable) panel.getClientProperty("dataTable")));
            searchControls.add(searchButton);
        }
        if ("Customer Management".equals(section)) {
            JButton searchButton = createSecondaryActionButton("Search");
            searchButton.addActionListener(event ->
                applyTableSearch(searchField, (JTable) panel.getClientProperty("dataTable")));
            actionButtons.add(searchButton, 0);
        }
        JPanel controls = new JPanel(new BorderLayout(12, 0));
        controls.setOpaque(false);
        controls.add(tableTitle, BorderLayout.WEST);
        JPanel actions = new JPanel(new BorderLayout(8, 0));
        actions.setOpaque(false);
        actions.add(hasAdjacentSearchButton ? searchControls : searchField, BorderLayout.CENTER);
        actions.add(actionButtons, BorderLayout.EAST);
        controls.add(actions, BorderLayout.EAST);
        panel.add(controls, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setFont(BODY);
        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(224, 243, 241));
        table.setSelectionForeground(TEXT);
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setForeground(new Color(91, 105, 122));
        header.setBackground(new Color(246, 248, 251));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 39));
        panel.putClientProperty("dataTable", table);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(BORDER));
        tableScroll.getViewport().setBackground(Color.WHITE);
        panel.add(tableScroll, BorderLayout.CENTER);
        return panel;
    }

    /** Creates a selected customer's editable details and applies validated changes. */
    private void editCustomer(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a customer first.", "No customer selected",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        JTextField[] fields = new JTextField[3];
        JPanel form = new JPanel(new GridLayout(fields.length, 2, 10, 9));
        for (int column = 1; column <= fields.length; column++) {
            String columnName = model.getColumnName(column);
            JTextField field = new JTextField(
                String.valueOf(model.getValueAt(modelRow, column)), 18);
            configureFieldValidation(field, columnName);
            fields[column - 1] = field;
            form.add(new JLabel(columnName + ":"));
            form.add(field);
        }

        int result = JOptionPane.showConfirmDialog(this, form, "Edit Customer",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        String[] values = new String[fields.length];
        for (int index = 0; index < fields.length; index++) {
            String value = fields[index].getText().trim();
            if (value.isEmpty()) {
                showInputError("Please fill in every customer field.");
                return;
            }
            String columnName = model.getColumnName(index + 1);
            String fieldType = getFieldType(columnName);
            if ("letters".equals(fieldType) && !InputValidation.isLettersOnly(value)) {
                showInputError(columnName + " must contain letters only.");
                return;
            }
            if ("digits".equals(fieldType) && !InputValidation.isDigitsOnly(value)) {
                showInputError(columnName + " must contain numbers only.");
                return;
            }
            values[index] = value;
        }
        String customerId = String.valueOf(model.getValueAt(modelRow, 0));
        if (!StaffDashboard.updateCustomerForAdmin(customerId, values[0], values[1], values[2])) {
            showInputError("The selected customer no longer exists. Refresh the page and try again.");
            return;
        }
        refreshCustomerModel(model);
    }

    /** Confirms removal of the selected customer from the customer table. */
    private void deleteCustomer(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a customer first.", "No customer selected",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String customerName = String.valueOf(model.getValueAt(modelRow, model.findColumn("Full name")));
        int result = JOptionPane.showConfirmDialog(this,
            "Delete customer " + customerName + "?", "Confirm customer deletingon",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            String customerId = String.valueOf(model.getValueAt(modelRow, 0));
            try {
                StaffDashboard.deleteCustomerForAdmin(customerId);
                refreshCustomerModel(model);
            } catch (IllegalArgumentException exception) {
                JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Customer cannot be deleted", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /** Rebuilds the admin table from the authoritative customer records. */
    private void refreshCustomerModel(DefaultTableModel model) {
        model.setRowCount(0);
        for (Object[] row : StaffDashboard.getCustomerManagementRows()) {
            model.addRow(row);
        }
    }

    /** Edits the selected inventory item's details while preservi its ID. */
    private void editInventoryItem(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an inventory item first.",
                "No item selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String itemId = String.valueOf(model.getValueAt(modelRow, 0));
        boolean wholeUnitItem = InventoryData.usesWholeUnits(itemId);
        int fieldCount = model.getColumnCount() - 2;
        JTextField[] fields = new JTextField[fieldCount];
        JPanel form = new JPanel(new GridLayout(fieldCount, 2, 10, 9));
        for (int column = 1; column < model.getColumnCount() - 1; column++) {
            String columnName = model.getColumnName(column);
            JTextField field = new JTextField(String.valueOf(model.getValueAt(modelRow, column)), 18);
            if (wholeUnitItem && ("Stock".equals(columnName) || "Reorder level".equals(columnName))) {
                InputValidation.digitsOnly(field);
            } else {
                configureFieldValidation(field, columnName);
            }
            if ("Unit".equals(columnName)) {
                field.setToolTipText("Enter the amount represented by one inventory unit, e.g. pack or 20ml.");
            } else if ("Cost per Unit".equals(columnName)) {
                field.setToolTipText("Cost for one amount of the unit above.");
            }
            fields[column - 1] = field;
            form.add(new JLabel("Unit".equals(columnName)
                ? "Unit (e.g. pack, 20ml):" : columnName + ":"));
            form.add(field);
        }

        int result = JOptionPane.showConfirmDialog(this, form, "Edit Inventory Item",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        String[] values = new String[fieldCount];
        for (int index = 0; index < fields.length; index++) {
            String value = fields[index].getText().trim();
            String columnName = model.getColumnName(index + 1);
            if (value.isEmpty()) {
                showInputError("Please fill in every inventory item field.");
                return;
            }
            if ("decimal".equals(getFieldType(columnName)) && !InputValidation.isDecimal(value)) {
                showInputError(columnName + " must contain a valid number.");
                return;
            }
            values[index] = value;
        }

        try {
            InventoryData.updateItem(itemId, values[0], values[1],
                Double.parseDouble(values[2]), Double.parseDouble(values[3]), values[4],
                Double.parseDouble(values[5]));
            refreshInventoryModel(model);
            StaffDashboard.refreshInventoryViews();
        } catch (IllegalArgumentException exception) {
            showInputError(exception.getMessage());
        }
    }

    /** Confirms deletingon and preserves items referenced by saved orders. */
    private void deleteInventoryItem(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an inventory item first.",
                "No item selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String itemId = String.valueOf(model.getValueAt(modelRow, 0));
        String itemName = String.valueOf(model.getValueAt(modelRow, 1));
        if (StaffDashboard.isInventoryItemUsed(itemId)) {
            JOptionPane.showMessageDialog(this,
                "This item cannot be deleted because it is recorded in saved laundries.",
                "Item in use", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int result = JOptionPane.showConfirmDialog(this,
            "Delete inventory item " + itemName + " (" + itemId + ")?",
            "Confirm item deletingon", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            if (InventoryData.removeItem(itemId)) {
                refreshInventoryModel(model);
                StaffDashboard.refreshInventoryViews();
            } else {
                JOptionPane.showMessageDialog(this,
                    "The selected inventory item is no longer available. Refresh and try again.",
                    "Item not found", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /** Rebuilds inventory rows and refreshes the dashboard's low-stock count. */
    private void refreshInventoryModel(DefaultTableModel model) {
        model.setRowCount(0);
        for (InventoryData.Item item : InventoryData.getItems()) {
            model.addRow(new Object[] {
                item.getId(), item.getName(), item.getCategory(),
                formatInventoryStock(item, item.getStock()),
                formatInventoryStock(item, item.getReorderLevel()),
                item.getUnit(), String.format("%.2f", item.getUnitCost()),
                item.isLowStock() ? "Low Stock" : "In Stock"
            });
        }
        lowStockCountLabel.setText(InventoryData.getLowStockCount() + " items   >");
    }

    /** Creates a consistently styled secondary action button for module controls. */
    private JButton createSecondaryActionButton(String title) {
        JButton button = new JButton(title);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(TEXT);
        button.setBackground(new Color(246, 248, 251));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return button;
    }

    /** Displays the selected live staff order, includi its recorded supplies. */
    private void showOrderInfo(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a laundry first.", "No laundry selected",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String orderId = String.valueOf(model.getValueAt(modelRow, model.findColumn("Laundry ID")));
        String details = StaffDashboard.getOrderInfoForAdmin(orderId);
        if (details == null) {
            JOptionPane.showMessageDialog(this, "The selected laundry is no longer available.",
                "Laundry not found", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, details, "Laundry Information",
            JOptionPane.INFORMATION_MESSAGE);
    }

    /** Creates an admin-originated order using the shared staff order records. */
    private void showAddOrderDialog(DefaultTableModel model) {
        String[] customers = StaffDashboard.getAdminCustomerOptions();
        String[] services = StaffDashboard.getActiveServiceNames();
        if (customers.length == 0 || services.length == 0) {
            showInputError(customers.length == 0
                ? "Add a customer before creating a laundry."
                : "Add or activate a service before creating a laundry.");
            return;
        }
        JComboBox<String> customer = new JComboBox<String>(customers);
        JComboBox<String> service = new JComboBox<String>(services);
        JTextField quantity = new JTextField("1", 12);
        JTextField received = new JTextField(LocalDate.now().toString(), 12);
        JSpinner discount = new JSpinner(new javax.swing.SpinnerNumberModel(0.0, 0.0, 100.0, 1.0));
        JComboBox<String> paymentStatus = new JComboBox<String>(new String[] {"Unpaid", "Paid"});
        InputValidation.decimalOnly(quantity);
        InputValidation.dateCharactersOnly(received);
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        form.add(new JLabel("Customer:"));
        form.add(customer);
        form.add(new JLabel("Service:"));
        form.add(service);
        form.add(new JLabel("Quantity / weight:"));
        form.add(quantity);
        form.add(new JLabel("Discount (%):"));
        form.add(discount);
        form.add(new JLabel("Date received (YYYY-MM-DD):"));
        form.add(received);
        form.add(new JLabel("Payment received:"));
        form.add(paymentStatus);
        JLabel originalAmount = new JLabel();
        JLabel discountedAmount = new JLabel();
        originalAmount.setFont(BODY);
        discountedAmount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        discountedAmount.setForeground(BLUE);
        form.add(new JLabel("Original Price (service + supplies):"));
        form.add(originalAmount);
        form.add(new JLabel("Discounted Price:"));
        form.add(discountedAmount);
        AdminSupplyUsageInput supplies = new AdminSupplyUsageInput();
        JScrollPane supplyScroll = new JScrollPane(supplies.table);
        supplyScroll.setBorder(BorderFactory.createTitledBorder(
            "Supplies used - enter amounts in the Used column"));
        supplyScroll.setPreferredSize(new Dimension(650, 190));
        JPanel dialogContent = new JPanel(new BorderLayout(0, 10));
        dialogContent.add(form, BorderLayout.NORTH);
        dialogContent.add(supplyScroll, BorderLayout.CENTER);

        Runnable updatePrice = () -> {
            try {
                double amount = Double.parseDouble(quantity.getText().trim());
                ServiceCatalog.Service selectedService =
                    ServiceCatalog.findByName(String.valueOf(service.getSelectedItem()));
                if (amount <= 0 || selectedService == null) {
                    originalAmount.setText("Enter a positive quantity");
                    discountedAmount.setText(" - ");
                    return;
                }
                double original = amount * selectedService.getRate()
                    + InventoryData.getUsageCost(supplies.readUsage());
                if (!Double.isFinite(original)) {
                    originalAmount.setText("Amount is too large");
                    discountedAmount.setText(" - ");
                    return;
                }
                double percentage = ((Number) discount.getValue()).doubleValue();
                originalAmount.setText(String.format("P%.2f", original));
                discountedAmount.setText(String.format("P%.2f",
                    Math.round(original * (100.0 - percentage)) / 100.0));
            } catch (IllegalArgumentException exception) {
                originalAmount.setText("Enter valid quantity and supply amounts");
                discountedAmount.setText(" - ");
            }
        };
        quantity.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent event) {
                updatePrice.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent event) {
                updatePrice.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent event) {
                updatePrice.run();
            }
        });
        service.addActionListener(event -> updatePrice.run());
        discount.addChangeListener(event -> updatePrice.run());
        supplies.model.addTableModelListener(event -> {
            if (event.getColumn() == 3
                    || event.getColumn() == javax.swing.event.TableModelEvent.ALL_COLUMNS) {
                updatePrice.run();
            }
        });
        updatePrice.run();
        int result = JOptionPane.showConfirmDialog(this, dialogContent, "Add Laundry",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        if (!InputValidation.isDecimal(quantity.getText().trim())
                || !InputValidation.isValidDate(received.getText().trim())) {
            showInputError("Enter a valid quantity and date received in YYYY-MM-DD format.");
            return;
        }
        try {
            String customerId = selectedCustomerId(String.valueOf(customer.getSelectedItem()));
            LocalDate receivedDate = LocalDate.parse(received.getText().trim());
            String orderId = StaffDashboard.addOrderForAdmin(customerId,
                String.valueOf(service.getSelectedItem()),
                Double.parseDouble(quantity.getText().trim()),
                receivedDate, receivedDate.plusDays(2),
                ((Number) discount.getValue()).doubleValue(), supplies.readUsage(),
                "Paid".equals(paymentStatus.getSelectedItem()));
            refreshOrderModel(model);
            JOptionPane.showMessageDialog(this, "Laundry " + orderId
                + " was added and marked as ADMIN for staff.",
                "Admin laundry added", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | java.time.DateTimeException exception) {
            showInputError(exception.getMessage());
        }
    }

    /** Edits the selected order while preservi its payment and supply records. */
    private void editOrder(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a laundry first.",
                "No laundry selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String orderId = String.valueOf(model.getValueAt(modelRow, model.findColumn("Laundry ID")));
        Object[] orderData = StaffDashboard.getAdminOrderEditData(orderId);
        if (orderData == null) {
            showInputError("The selected laundry is no longer available.");
            return;
        }
        String[] customers = StaffDashboard.getAdminCustomerOptions();
        if (customers.length == 0) {
            showInputError("Add a customer before editing this laundry.");
            return;
        }
        JComboBox<String> customer = new JComboBox<String>(customers);
        String currentCustomer = String.valueOf(orderData[0]);
        for (int index = 0; index < customers.length; index++) {
            if (customers[index].startsWith(currentCustomer + " - ")) {
                customer.setSelectedIndex(index);
                break;
            }
        }
        String currentService = String.valueOf(orderData[1]);
        ArrayList<String> serviceOptions = new ArrayList<String>(
            java.util.Arrays.asList(StaffDashboard.getActiveServiceNames()));
        if (!serviceOptions.contains(currentService)) {
            serviceOptions.add(currentService);
        }
        JComboBox<String> service = new JComboBox<String>(serviceOptions.toArray(new String[0]));
        service.setSelectedItem(currentService);
        JTextField quantity = new JTextField(String.valueOf(orderData[2]), 12);
        JTextField received = new JTextField(String.valueOf(orderData[3]), 12);
        JSpinner discount = new JSpinner(new javax.swing.SpinnerNumberModel(
            ((Double) orderData[5]).doubleValue(), 0.0, 100.0, 1.0));
        JComboBox<String> status = new JComboBox<String>(
            new String[] {"In Queue", "Pending", "Ready to Pick Up", "Completed"});
        status.setSelectedItem(String.valueOf(orderData[6]));
        JComboBox<String> paymentStatus =
            new JComboBox<String>(new String[] {"Unpaid", "Paid"});
        paymentStatus.setSelectedItem(String.valueOf(orderData[8]));
        InputValidation.decimalOnly(quantity);
        InputValidation.dateCharactersOnly(received);
        @SuppressWarnings("unchecked")
        Map<String, Double> currentSupplyUsage = (Map<String, Double>) orderData[7];
        AdminSupplyUsageInput supplies =
            new AdminSupplyUsageInput(currentSupplyUsage, currentSupplyUsage);
        JPanel orderFields = new JPanel(new GridLayout(0, 2, 10, 9));
        orderFields.add(new JLabel("Customer:"));
        orderFields.add(customer);
        orderFields.add(new JLabel("Service:"));
        orderFields.add(service);
        orderFields.add(new JLabel("Quantity / weight:"));
        orderFields.add(quantity);
        orderFields.add(new JLabel("Discount (%):"));
        orderFields.add(discount);
        orderFields.add(new JLabel("Date received (YYYY-MM-DD):"));
        orderFields.add(received);
        orderFields.add(new JLabel("Laundry status:"));
        orderFields.add(status);
        orderFields.add(new JLabel("Payment status:"));
        orderFields.add(paymentStatus);
        JScrollPane supplyScroll = new JScrollPane(supplies.table);
        supplyScroll.setBorder(BorderFactory.createTitledBorder(
            "Supplies used - enter amounts in the Used column"));
        supplyScroll.setPreferredSize(new Dimension(560, 150));
        JPanel form = new JPanel(new BorderLayout(0, 10));
        form.add(orderFields, BorderLayout.NORTH);
        form.add(supplyScroll, BorderLayout.CENTER);
        int result = JOptionPane.showConfirmDialog(this, form, "Edit Laundry " + orderId,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        if (!InputValidation.isDecimal(quantity.getText().trim())
                || !InputValidation.isValidDate(received.getText().trim())) {
            showInputError("Enter a valid quantity and date received in YYYY-MM-DD format.");
            return;
        }
        try {
            Map<String, Double> supplyUsage = supplies.readUsage();
            LocalDate receivedDate = LocalDate.parse(received.getText().trim());
            StaffDashboard.updateOrderForAdmin(orderId,
                selectedCustomerId(String.valueOf(customer.getSelectedItem())),
                String.valueOf(service.getSelectedItem()),
                Double.parseDouble(quantity.getText().trim()),
                receivedDate, receivedDate.plusDays(2),
                ((Number) discount.getValue()).doubleValue(), String.valueOf(status.getSelectedItem()),
                supplyUsage, "Paid".equals(paymentStatus.getSelectedItem()));
            refreshOrderModel(model);
            if (inventoryControlModel != null) {
                refreshInventoryModel(inventoryControlModel);
            }
            refreshFinanceMetrics();
            JOptionPane.showMessageDialog(this, "Laundry " + orderId + " updated.",
                "Laundry updated", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | java.time.DateTimeException exception) {
            showInputError(exception.getMessage());
        }
    }

    private String selectedCustomerId(String option) {
        int separator = option.indexOf(" - ");
        if (separator <= 0) {
            throw new IllegalArgumentException("Select a valid customer.");
        }
        return option.substring(0, separator);
    }

    /** Encapsulates the supply-usage form and its table model for one admin order dialog. */
    private final class AdminSupplyUsageInput {
            private final ArrayList<String> itemIds = new ArrayList<String>();
            private final DefaultTableModel model = new DefaultTableModel(
                    new Object[] {"Supply", "Available", "Cost per Unit", "Used"}, 0) {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 3;
                }
            };
            private final JTable table = new JTable(model);

            private AdminSupplyUsageInput() {
                this(null, null);
            }

            private AdminSupplyUsageInput(Map<String, Double> currentUsage,
                    Map<String, Double> reservedUsage) {
                table.setFont(BODY);
                table.setRowHeight(26);
                table.setShowVerticalLines(false);
                table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
                table.getColumnModel().getColumn(0).setPreferredWidth(250);
                table.getColumnModel().getColumn(1).setPreferredWidth(120);
                table.getColumnModel().getColumn(2).setPreferredWidth(125);
                table.getColumnModel().getColumn(3).setPreferredWidth(80);
                JTextField usedField = new JTextField();
                usedField.setHorizontalAlignment(SwingConstants.RIGHT);
                javax.swing.DefaultCellEditor usedEditor =
                    new javax.swing.DefaultCellEditor(usedField) {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public boolean stopCellEditing() {
                            int row = table.getEditingRow();
                            if (row >= 0 && row < itemIds.size()
                                    && InventoryData.usesWholeUnits(itemIds.get(row))
                                    && !InputValidation.isDigitsOnly(usedField.getText().trim())) {
                                JOptionPane.showMessageDialog(usedField,
                                    "This supply is measured in whole packs. Enter numbers only.",
                                    "Invalid input", JOptionPane.ERROR_MESSAGE);
                                return false;
                            }
                            return super.stopCellEditing();
                        }
                    };
                table.getColumnModel().getColumn(3).setCellEditor(usedEditor);
                for (InventoryData.Item item : InventoryData.getItems()) {
                    itemIds.add(item.getId());
                    double used = currentUsage == null ? 0
                        : currentUsage.getOrDefault(item.getId(), 0.0);
                    double available = item.getStock() + (reservedUsage == null ? 0
                        : reservedUsage.getOrDefault(item.getId(), 0.0));
                    model.addRow(new Object[] {
                        item.getName() + " (" + item.getUnit() + ")",
                        formatInventoryStock(item, available) + " " + item.getUnit(),
                        String.format("P%.2f / %s", item.getUnitCost(), item.getUnit()),
                        String.valueOf(used)
                    });
                }
            }

            private Map<String, Double> readUsage() {
                if (table.isEditing() && !table.getCellEditor().stopCellEditing()) {
                    throw new IllegalArgumentException(
                        "Finish enteri the supply quantity before saving.");
                }
                Map<String, Double> usage = new LinkedHashMap<String, Double>();
                for (int row = 0; row < itemIds.size(); row++) {
                    String value = String.valueOf(model.getValueAt(row, 3)).trim();
                    if (value.isEmpty()) {
                        value = "0";
                    }
                    if (!InputValidation.isDecimal(value)) {
                        throw new IllegalArgumentException("Enter a valid supply quantity.");
                    }
                    double amount = Double.parseDouble(value);
                    if (!Double.isFinite(amount) || amount < 0) {
                        throw new IllegalArgumentException(
                            "Supply quantities must be valid non-negative numbers.");
                    }
                    if (InventoryData.usesWholeUnits(itemIds.get(row))
                            && !InputValidation.isDigitsOnly(value)) {
                        throw new IllegalArgumentException(
                            "Laundry detergent and fabric conditioner quantities must be whole packs.");
                    }
                    usage.put(itemIds.get(row), amount);
                }
                return usage;
            }
    }

    /** Cancels an eligible order usi the shared queue-aware refund and stock rules. */
    private void cancelOrder(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a laundry first.",
                "No laundry selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String orderId = String.valueOf(model.getValueAt(modelRow, model.findColumn("Laundry ID")));
        String status = String.valueOf(model.getValueAt(modelRow, model.findColumn("Status")));
        if (!"In Queue".equals(status) && !"Pending".equals(status)) {
            JOptionPane.showMessageDialog(this,
                "Only laundries that are In Queue or Pendi can be cancelled.",
                "Laundry cannot be cancelled", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double refund = StaffDashboard.getOrderCancellationRefund(orderId);
            String stockMessage = "In Queue".equals(status)
                ? "Recorded supplies will be returned to inventory."
                : "Supplies already used will remain deducted fromventory.";
            int result = JOptionPane.showConfirmDialog(this,
                "Cancel laundry " + orderId + "?\n" + stockMessage
                    + "\nRefund: " + String.format("P%,.2f", refund),
                "Confirm laundry cancellation", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
            StaffDashboard.cancelOrder(orderId);
            refreshOrderModel(model);
            if (inventoryControlModel != null) {
                refreshInventoryModel(inventoryControlModel);
            }
            refreshFinanceMetrics();
        } catch (IllegalArgumentException exception) {
            showInputError(exception.getMessage());
        }
    }

    /** Deletes the selected laundry and its linked payment history without cancelling it. */
    private void deleteOrder(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a laundry first.",
                "No laundry selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String orderId = String.valueOf(model.getValueAt(modelRow, model.findColumn("Laundry ID")));
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete laundry " + orderId + "?\n"
                + "Its linked payment records will also be deleted. Supplies recorded for "
                + "In Queue laundries will be returned to inventory; supplies already used "
                + "will remain deducted from stock.",
            "Confirm laundry deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            StaffDashboard.deleteOrderForAdmin(orderId);
            refreshOrderModel(model);
            if (inventoryControlModel != null) {
                refreshInventoryModel(inventoryControlModel);
            }
            refreshFinanceMetrics();
        } catch (IllegalArgumentException exception) {
            showInputError(exception.getMessage());
        }
    }

    /** Confirms and removes every laundry and its linked payment records. */
    private void deleteAllOrders(DefaultTableModel model) {
        int orderCount = StaffDashboard.getOrderOversightRows().length;
        if (orderCount == 0) {
            JOptionPane.showMessageDialog(this, "There are no laundries to delete.",
                "No laundries", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete all " + orderCount + " laundries?\n"
                + "All linked payment records will also be deleted. Supplies recorded for "
                + "In Queue laundries will be returned to inventory; supplies already used "
                + "will remain deducted from stock.",
            "Confirm deletion of all laundries", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            StaffDashboard.deleteAllOrdersForAdmin();
            refreshOrderModel(model);
            if (inventoryControlModel != null) {
                refreshInventoryModel(inventoryControlModel);
            }
            refreshFinanceMetrics();
        } catch (IllegalArgumentException exception) {
            showInputError(exception.getMessage());
        }
    }

    /** Rebuilds order-oversight rows from the shared staff order records. */
    private void refreshOrderModel(DefaultTableModel model) {
        model.setRowCount(0);
        for (Object[] row : StaffDashboard.getOrderOversightRows()) {
            model.addRow(row);
        }
    }

    /** Applies a literal, case-insensitive search across the table's columns. */
    private void applyTableSearch(JTextField searchField, JTable table) {
        @SuppressWarnings("unchecked")
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) table.getRowSorter();
        String query = searchField.getText().trim();
        sorter.setRowFilter(query.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(query)));
    }

    /** Chooses the singular record name used by each module's add action. */
    private String getSingularName(String section) {
        switch (section) {
            case "Laundry Oversight":
                return "laundry";
            case "Customer Management":
                return "customer";
            case "Service Catalog":
                return "service";
            case "Inventory Control":
                return "item";
            case "Employee Management":
                return "employee";
            default:
                return "record";
        }
    }

    /** Collects, validates, and adds a record to the selected module. */
    private void showAddRecordDialog(String section, DefaultTableModel model) {
        if ("Service Catalog".equals(section)) {
            showAddServiceDialog(model);
            return;
        }
        int lastInputColumn = "Employee Management".equals(section)
            ? model.findColumn("Employment Status")
            : "Customer Management".equals(section)
                ? model.findColumn("Address")
                : "Inventory Control".equals(section)
                    ? model.findColumn("Cost per Unit") : model.getColumnCount() - 1;
        int fieldCount = lastInputColumn;
        JPanel form = new JPanel(new GridLayout(fieldCount, 2, 10, 9));
        java.awt.Component[] inputs = new java.awt.Component[fieldCount];
        for (int column = 1; column <= lastInputColumn; column++) {
            String columnName = model.getColumnName(column);
            if ("Employee Management".equals(section) && "Shift".equals(columnName)) {
                inputs[column - 1] = new JComboBox<String>(
                    new String[] {"Morning", "Afternoon", "Night"});
            } else if ("Employee Management".equals(section)
                    && "Employment Status".equals(columnName)) {
                inputs[column - 1] = new JComboBox<String>(
                    new String[] {"Active", "Not Set", "Inactive"});
            } else {
                JTextField field = new JTextField(18);
                configureFieldValidation(field, columnName);
                inputs[column - 1] = field;
            }
            if ("Unit".equals(columnName)) {
                ((JTextField) inputs[column - 1]).setToolTipText(
                    "Enter the amount represented by one inventory unit, e.g. pack or 20ml.");
            } else if ("Cost per Unit".equals(columnName)) {
                ((JTextField) inputs[column - 1]).setToolTipText("Cost for one amount of the unit above.");
            }
            String label = "date".equals(getFieldType(columnName))
                ? columnName + " (YYYY-MM-DD):"
                : "Unit".equals(columnName) ? "Unit (e.g. pack, 20ml):" : columnName + ":";
            form.add(new JLabel(label));
            form.add(inputs[column - 1]);
        }

        int result = JOptionPane.showConfirmDialog(this, form, "Add " + getSingularName(section),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        Object[] row = new Object[model.getColumnCount()];
        if (!"Customer Management".equals(section)) {
            row[0] = createNextId(section, model);
        }
        for (int index = 0; index < inputs.length; index++) {
            String value = inputs[index] instanceof JTextField
                ? ((JTextField) inputs[index]).getText().trim()
                : String.valueOf(((JComboBox<?>) inputs[index]).getSelectedItem());
            if (value.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in every field.", "Missi information",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            String columnName = model.getColumnName(index + 1);
            String fieldType = getFieldType(columnName);
            if ("letters".equals(fieldType) && !InputValidation.isLettersOnly(value)) {
                showInputError(columnName + " must contain letters only.");
                return;
            }
            if ("digits".equals(fieldType) && !InputValidation.isDigitsOnly(value)) {
                showInputError(columnName + " must contain numbers only.");
                return;
            }
            if ("decimal".equals(fieldType) && !InputValidation.isDecimal(value)) {
                showInputError(columnName + " must contain a valid number.");
                return;
            }
            if ("email".equals(fieldType) && !InputValidation.isValidEmail(value)) {
                showInputError("Enter a valid email address for " + columnName + ".");
                return;
            }
            if ("date".equals(fieldType) && !InputValidation.isValidDate(value)) {
                showInputError("Enter " + columnName + " as a valid YYYY-MM-DD date.");
                return;
            }
            row[index + 1] = value;
        }
        if ("Customer Management".equals(section)) {
            StaffDashboard.addCustomerForAdmin(
                String.valueOf(row[1]), String.valueOf(row[2]), String.valueOf(row[3]));
            refreshCustomerModel(model);
            return;
        } else if ("Employee Management".equals(section)) {
            String employeeId = String.valueOf(row[0]);
            StaffPayroll.setMonthlySalary(employeeId, 0);
            row[model.findColumn("Monthly Salary")] = formatSalary(employeeId);
            row[model.findColumn("Username")] = StaffAccounts.getUsername(String.valueOf(row[0]));
            row[model.findColumn("Account Status")] = StaffAccounts.getStatus(String.valueOf(row[0]));
        } else if ("Inventory Control".equals(section)) {
            if (!addInventoryItem(row)) {
                return;
            }
        } else if ("Service Catalog".equals(section)) {
            if (!addService(row)) {
                return;
            }
            model.addRow(row);
            StaffDashboard.refreshServiceCatalogViews();
            return;
        }
        model.addRow(row);
    }

    /** Adds a service without requiring a unit; new services default to per item. */
    private void showAddServiceDialog(DefaultTableModel model) {
        JTextField name = new JTextField(18);
        JTextField price = new JTextField(18);
        JComboBox<String> status = new JComboBox<String>(new String[] {"Active", "Inactive"});
        InputValidation.lettersOnly(name);
        InputValidation.decimalOnly(price);
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 9));
        form.add(new JLabel("Service name:"));
        form.add(name);
        form.add(new JLabel("Price (P):"));
        form.add(price);
        form.add(new JLabel("Status:"));
        form.add(status);

        int result = JOptionPane.showConfirmDialog(this, form, "Add service",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        String serviceName = name.getText().trim();
        String servicePrice = price.getText().trim();
        if (serviceName.isEmpty() || servicePrice.isEmpty()) {
            showInputError("Please fill in every field.");
            return;
        }
        if (!InputValidation.isLettersOnly(serviceName)) {
            showInputError("Service name must contain letters and supported punctuation.");
            return;
        }
        if (!InputValidation.isDecimal(servicePrice)) {
            showInputError("Service price must be a valid non-negative amount.");
            return;
        }

        Object[] row = new Object[] {
            createNextId("Service Catalog", model),
            serviceName,
            servicePrice,
            "per item",
            String.valueOf(status.getSelectedItem())
        };
        if (addService(row)) {
            model.addRow(row);
            StaffDashboard.refreshServiceCatalogViews();
        }
    }

    /** Adds a validated service to the shared catalog and prepares its table row. */
    private boolean addService(Object[] row) {
        String name = String.valueOf(row[1]).trim();
        String rate = String.valueOf(row[2]).trim();
        String unit = String.valueOf(row[3]).trim();
        String status = String.valueOf(row[4]).trim();
        if (!InputValidation.isLettersOnly(name)) {
            showInputError("Service name must contain letters and supported punctuation.");
            return false;
        }
        if (!InputValidation.isDecimal(rate)) {
            showInputError("Service price must be a valid non-negative amount.");
            return false;
        }
        if (unit.isEmpty() || !InputValidation.isLettersOnly(unit)) {
            showInputError("Enter a valid service unit, such as 'per kg' or 'per item'.");
            return false;
        }
        if (!"Active".equalsIgnoreCase(status) && !"Inactive".equalsIgnoreCase(status)) {
            showInputError("Service status must be Active or Inactive.");
            return false;
        }
        try {
            ServiceCatalog.Service service = ServiceCatalog.add(String.valueOf(row[0]),
                name, Double.parseDouble(rate), unit,
                "Active".equalsIgnoreCase(status));
            row[1] = service.getName();
            row[2] = String.format("P%,.2f", service.getRate());
            row[3] = service.getUnit();
            row[4] = service.isActive() ? "Active" : "Inactive";
            return true;
        } catch (IllegalArgumentException exception) {
            showInputError(exception.getMessage());
            return false;
        }
    }

    /** Edits the selected service's name, price, unit, and active state. */
    private void editService(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a service first.",
                "No service selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String serviceId = String.valueOf(model.getValueAt(modelRow, 0));
        ServiceCatalog.Service service = ServiceCatalog.findById(serviceId);
        if (service == null) {
            showInputError("The selected service is no longer available.");
            return;
        }
        JTextField name = new JTextField(service.getName(), 18);
        JTextField price = new JTextField(String.format("%.2f", service.getRate()), 18);
        JTextField unit = new JTextField(service.getUnit(), 18);
        JComboBox<String> status = new JComboBox<String>(new String[] {"Active", "Inactive"});
        status.setSelectedItem(service.isActive() ? "Active" : "Inactive");
        InputValidation.lettersOnly(name);
        InputValidation.decimalOnly(price);
        InputValidation.lettersOnly(unit);
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 9));
        form.add(new JLabel("Service name:"));
        form.add(name);
        form.add(new JLabel("Price (P):"));
        form.add(price);
        form.add(new JLabel("Unit:"));
        form.add(unit);
        form.add(new JLabel("Status:"));
        form.add(status);
        int result = JOptionPane.showConfirmDialog(this, form, "Edit Service",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        if (!InputValidation.isLettersOnly(name.getText().trim())
                || !InputValidation.isLettersOnly(unit.getText().trim())) {
            showInputError("Enter a valid service name and unit.");
            return;
        }
        if (!InputValidation.isDecimal(price.getText().trim())) {
            showInputError("Service price must be a valid non-negative amount.");
            return;
        }
        try {
            ServiceCatalog.update(serviceId, name.getText(), Double.parseDouble(price.getText().trim()),
                unit.getText(), "Active".equals(status.getSelectedItem()));
            refreshServiceModel(model);
            StaffDashboard.refreshServiceCatalogViews();
        } catch (IllegalArgumentException exception) {
            showInputError(exception.getMessage());
        }
    }

    /** Deletes the selected service only when it has no order history. */
    private void deleteService(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a service first.",
                "No service selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String serviceId = String.valueOf(model.getValueAt(modelRow, 0));
        String serviceName = String.valueOf(model.getValueAt(modelRow, 1));
        if (StaffDashboard.isServiceUsed(serviceName)) {
            JOptionPane.showMessageDialog(this,
                "This service cannot be deleted because it is recorded in saved laundries. "
                    + "Set it to Inactive to hide it from new laundries.",
                "Service in use", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int result = JOptionPane.showConfirmDialog(this,
            "Delete service " + serviceName + " (" + serviceId + ")?",
            "Confirm service deletingon", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            if (ServiceCatalog.remove(serviceId)) {
                refreshServiceModel(model);
                StaffDashboard.refreshServiceCatalogViews();
            } else {
                showInputError("The selected service is no longer available.");
            }
        }
    }

    /** Rebuilds service rows from the shared catalog. */
    private void refreshServiceModel(DefaultTableModel model) {
        model.setRowCount(0);
        for (Object[] row : StaffDashboard.getServiceCatalogRows()) {
            model.addRow(row);
        }
    }

    /** Registers an inventory item and formats its initial stock values. */
    private boolean addInventoryItem(Object[] row) {
        try {
            double stock = Double.parseDouble(String.valueOf(row[3]));
            double reorderLevel = Double.parseDouble(String.valueOf(row[4]));
            double unitCost = Double.parseDouble(String.valueOf(row[6]));
            InventoryData.addItem(String.valueOf(row[0]), String.valueOf(row[1]),
                String.valueOf(row[2]), stock, reorderLevel, String.valueOf(row[5]), unitCost);
            InventoryData.Item item = InventoryData.findItem(String.valueOf(row[0]));
            row[3] = formatInventoryStock(item, stock);
            row[4] = formatInventoryStock(item, reorderLevel);
            row[6] = String.format("%.2f", unitCost);
            row[7] = stock <= reorderLevel ? "Low Stock" : "In Stock";
            lowStockCountLabel.setText(InventoryData.getLowStockCount() + " items   >");
            StaffDashboard.refreshInventoryViews();
            return true;
        } catch (NumberFormatException exception) {
            showInputError("Stock, reorder level, and unit cost must be valid numbers.");
            return false;
        } catch (IllegalArgumentException exception) {
            showInputError(exception.getMessage());
            return false;
        }
    }

    /** Formats whole inventory amounts without unnecessary decimal places. */
    private String formatInventoryAmount(double amount) {
        if (amount == Math.rint(amount)) {
            return String.format("%.0f", amount);
        }
        return String.format("%.2f", amount);
    }

    private String formatInventoryStock(InventoryData.Item item, double amount) {
        return formatInventoryAmount(amount);
    }

    /** Creates or updates login credentials and active status for the selected employee. */
    private void manageEmployeeAccount(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an employee first.", "No employee selected",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String employeeId = String.valueOf(model.getValueAt(modelRow, model.findColumn("Employee ID")));
        String employeeName = String.valueOf(model.getValueAt(modelRow, model.findColumn("Full name")));
        JTextField username = new JTextField(18);
        String currentUsername = StaffAccounts.getUsername(employeeId);
        if (!" - ".equals(currentUsername)) {
            username.setText(currentUsername);
        }
        JPasswordField password = new JPasswordField(18);
        char passwordEchoChar = password.getEchoChar();
        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setOpaque(false);
        showPassword.addActionListener(event ->
            password.setEchoChar(showPassword.isSelected() ? (char) 0 : passwordEchoChar));
        JComboBox<String> status = new JComboBox<>(new String[] {"Active", "Inactive"});
        String accountStatus = StaffAccounts.getStatus(employeeId);
        status.setSelectedItem("Locked".equals(accountStatus) ? "Inactive" : accountStatus);
        if ("Not Set".equals(accountStatus)) {
            status.setSelectedItem("Active");
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 9));
        form.add(new JLabel("Employee:"));
        form.add(new JLabel(employeeName + " (" + employeeId + ")"));
        form.add(new JLabel("Username:"));
        form.add(username);
        form.add(new JLabel("New password (8+ characters):"));
        JPanel passwordFieldPanel = new JPanel();
        passwordFieldPanel.setOpaque(false);
        passwordFieldPanel.setLayout(new BoxLayout(passwordFieldPanel, BoxLayout.Y_AXIS));
        passwordFieldPanel.add(password);
        passwordFieldPanel.add(showPassword);
        form.add(passwordFieldPanel);
        form.add(new JLabel("Account status:"));
        form.add(status);
        form.add(new JLabel(""));
        String statusHelp = "Locked".equals(accountStatus)
            ? "Account is locked. Choose Active to unblock it."
            : "Leave password blank to keep it.";
        form.add(new JLabel(statusHelp));

        int result = JOptionPane.showConfirmDialog(this, form, "Manage Employee Account",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        char[] passwordValue = password.getPassword();
        try {
            StaffAccounts.save(employeeId, username.getText(), passwordValue,
                "Active".equals(status.getSelectedItem()));
        } catch (IllegalArgumentException exception) {
            showInputError(exception.getMessage());
            return;
        } finally {
            java.util.Arrays.fill(passwordValue, '\0');
        }
        model.setValueAt(StaffAccounts.getUsername(employeeId), modelRow,
            model.findColumn("Username"));
        model.setValueAt(StaffAccounts.getStatus(employeeId), modelRow,
            model.findColumn("Account Status"));
        JOptionPane.showMessageDialog(this, "Account updated for " + employeeName + ".",
            "Account updated", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Opens the admin account settings in the dashboard workspace. */
    private void showAdminAccountPage() {
        showSection(ADMIN_ACCOUNT, manageAdminButton);
    }

    /** Builds the admin username, password, recovery code, and security-question form. */
    private AbstractDashboardPage createAdminAccountPage() {
        AdminAccountPage page = new AdminAccountPage();
        page.add(heading(ADMIN_ACCOUNT, 25));
        JLabel subtitle = text("Update your sign-in credentials and account recovery details.", MUTED);
        subtitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 18, 0));
        page.add(subtitle);

        JTextField username = new JTextField(AdminAccounts.getUsername(), 20);
        JPasswordField currentPassword = new JPasswordField(20);
        JPasswordField newPassword = new JPasswordField(20);
        JPasswordField recoveryCode = new JPasswordField(20);
        JCheckBox showPasswords = new JCheckBox("Show password");
        JCheckBox showRecoveryCode = new JCheckBox("Show new recovery code");
        char passwordEchoChar = newPassword.getEchoChar();
        char recoveryEchoChar = recoveryCode.getEchoChar();
        showPasswords.setOpaque(false);
        showRecoveryCode.setOpaque(false);
        showPasswords.addActionListener(event -> {
            char echoChar = showPasswords.isSelected() ? (char) 0 : passwordEchoChar;
            currentPassword.setEchoChar(echoChar);
            newPassword.setEchoChar(echoChar);
        });
        showRecoveryCode.addActionListener(event ->
            recoveryCode.setEchoChar(showRecoveryCode.isSelected() ? (char) 0 : recoveryEchoChar));
        String[] savedQuestions = AdminAccounts.getQuestions();
        JTextField[] questions = new JTextField[3];
        JTextField[] answers = new JTextField[3];

        JPanel form = new JPanel(new GridLayout(0, 2, 14, 12));
        form.setOpaque(false);
        form.add(createAccountFieldLabel("Current password"));
        form.add(createAccountField(currentPassword, null));
        form.add(createAccountFieldLabel("Admin username"));
        form.add(createAccountField(username));
        form.add(createAccountFieldLabel("New password (8+ characters)"));
        form.add(createAccountField(newPassword, showPasswords));
        form.add(createAccountFieldLabel("New recovery code (8+ characters)"));
        form.add(createAccountField(recoveryCode, showRecoveryCode));
        for (int index = 0; index < 3; index++) {
            questions[index] = new JTextField(savedQuestions[index], 20);
            answers[index] = new JTextField(20);
            form.add(createAccountFieldLabel("Recovery question " + (index + 1)));
            form.add(createAccountField(questions[index]));
            form.add(createAccountFieldLabel("Answer " + (index + 1)));
            form.add(createAccountField(answers[index]));
        }
        form.add(new JLabel(""));
        form.add(text("<html>Leave optional fields blank to keep them.<br>"
            + "To change questions, provide all three new questions and answers.</html>", MUTED));

        RoundedPanel accountCard = createSectionCard("Admin credentials and recovery",
            "Current password is required to save account changes. Password and recovery code are hidden by default.");
        accountCard.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        accountCard.add(form, BorderLayout.CENTER);
        accountCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(accountCard);
        page.add(Box.createVerticalStrut(18));

        JLabel feedback = text(" ", MUTED);
        feedback.setAlignmentX(Component.LEFT_ALIGNMENT);
        feedback.setFont(new Font("Segoe UI", Font.BOLD, 13));
        page.add(feedback);
        JButton save = new JButton("Save Changes");
        save.setFont(new Font("Segoe UI", Font.BOLD, 14));
        save.setForeground(Color.WHITE);
        save.setBackground(BLUE);
        save.setFocusPainted(false);
        save.setPreferredSize(new Dimension(150, 42));
        save.addActionListener(event -> updateAdminAccount(username, currentPassword, newPassword,
            recoveryCode, questions, answers, feedback));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        actions.add(save);
        page.add(actions);
        return page;
    }

    /** Creates a consistent label and field style for the admin account form. */
    private JLabel createAccountFieldLabel(String value) {
        JLabel label = new JLabel(value + ":");
        label.setForeground(TEXT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }

    private JPanel createAccountField(JTextField field) {
        return createAccountField(field, null);
    }

    private JPanel createAccountField(JTextField field, JCheckBox visibilityToggle) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(280, 34));
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(field);
        if (visibilityToggle != null) {
            visibilityToggle.setFont(BODY);
            visibilityToggle.setForeground(MUTED);
            container.add(Box.createVerticalStrut(3));
            container.add(visibilityToggle);
        }
        return container;
    }

    /** Validates and saves account settings, then clears the submitted secrets. */
    private void updateAdminAccount(JTextField username, JPasswordField currentPassword,
            JPasswordField newPassword, JPasswordField recoveryCode, JTextField[] questions,
            JTextField[] answers, JLabel feedback) {
        char[] currentPasswordValue = currentPassword.getPassword();
        char[] newPasswordValue = newPassword.getPassword();
        char[] recoveryCodeValue = recoveryCode.getPassword();
        char[][] answerValues = new char[3][];
        String[] questionValues = new String[3];
        for (int index = 0; index < 3; index++) {
            questionValues[index] = questions[index].getText();
            answerValues[index] = answers[index].getText().toCharArray();
        }
        try {
            AdminAccounts.update(username.getText(), currentPasswordValue, newPasswordValue,
                recoveryCodeValue, questionValues, answerValues);
            feedback.setForeground(new Color(42, 139, 93));
            feedback.setText("Admin account settings updated.");
        } catch (IllegalArgumentException exception) {
            feedback.setForeground(new Color(190, 56, 56));
            feedback.setText(exception.getMessage());
        } finally {
            java.util.Arrays.fill(currentPasswordValue, '\0');
            java.util.Arrays.fill(newPasswordValue, '\0');
            java.util.Arrays.fill(recoveryCodeValue, '\0');
            for (char[] answer : answerValues) {
                java.util.Arrays.fill(answer, '\0');
            }
            currentPassword.setText("");
            newPassword.setText("");
            recoveryCode.setText("");
            for (JTextField answer : answers) {
                answer.setText("");
            }
        }
    }

    /** Edits the selected employee's contact, role, shift, and employment details. */
    private void editEmployeeInformation(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an employee first.",
                "No employee selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        JTextField name = new JTextField(String.valueOf(model.getValueAt(modelRow,
            model.findColumn("Full name"))), 18);
        JTextField role = new JTextField(String.valueOf(model.getValueAt(modelRow,
            model.findColumn("Role"))), 18);
        JTextField phone = new JTextField(String.valueOf(model.getValueAt(modelRow,
            model.findColumn("Phone"))), 18);
        JTextField email = new JTextField(String.valueOf(model.getValueAt(modelRow,
            model.findColumn("Email"))), 18);
        JComboBox<String> shift = new JComboBox<String>(new String[] {"Morning", "Afternoon", "Evening"});
        shift.setSelectedItem(model.getValueAt(modelRow, model.findColumn("Shift")));
        JComboBox<String> employmentStatus =
            new JComboBox<String>(new String[] {"Active", "Inactive"});
        employmentStatus.setSelectedItem(
            model.getValueAt(modelRow, model.findColumn("Employment Status")));
        InputValidation.lettersOnly(name);
        InputValidation.lettersOnly(role);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 9));
        form.add(new JLabel("Full name:"));
        form.add(name);
        form.add(new JLabel("Role:"));
        form.add(role);
        form.add(new JLabel("Phone:"));
        form.add(phone);
        form.add(new JLabel("Email:"));
        form.add(email);
        form.add(new JLabel("Shift:"));
        form.add(shift);
        form.add(new JLabel("Employment status:"));
        form.add(employmentStatus);

        int result = JOptionPane.showConfirmDialog(this, form, "Manage Employee Information",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        String nameValue = name.getText().trim();
        String roleValue = role.getText().trim();
        String phoneValue = phone.getText().trim();
        String emailValue = email.getText().trim();
        if (!InputValidation.isLettersOnly(nameValue)) {
            showInputError("Employee name must contain letters and supported punctuation.");
            return;
        }
        if (!InputValidation.isLettersOnly(roleValue)) {
            showInputError("Employee role must contain letters and supported punctuation.");
            return;
        }
        if (!phoneValue.matches("[0-9+() -]{7,20}")
                || phoneValue.replaceAll("[^0-9]", "").length() < 7) {
            showInputError("Enter a valid employee phone number.");
            return;
        }
        if (!InputValidation.isValidEmail(emailValue)) {
            showInputError("Enter a valid employee email address.");
            return;
        }
        model.setValueAt(nameValue, modelRow, model.findColumn("Full name"));
        model.setValueAt(roleValue, modelRow, model.findColumn("Role"));
        model.setValueAt(phoneValue, modelRow, model.findColumn("Phone"));
        model.setValueAt(emailValue, modelRow, model.findColumn("Email"));
        model.setValueAt(shift.getSelectedItem(), modelRow, model.findColumn("Shift"));
        model.setValueAt(employmentStatus.getSelectedItem(), modelRow,
            model.findColumn("Employment Status"));
        JOptionPane.showMessageDialog(this, "Employee information updated.",
            "Information updated", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Sets an employee's monthly salary and refreshes the payroll totals. */
    private void setEmployeeSalary(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an employee first.",
                "No employee selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        String employeeId = String.valueOf(model.getValueAt(modelRow,
            model.findColumn("Employee ID")));
        String employeeName = String.valueOf(model.getValueAt(modelRow,
            model.findColumn("Full name")));
        JTextField salary = new JTextField(String.format("%.2f",
            StaffPayroll.getMonthlySalary(employeeId)), 12);
        InputValidation.decimalOnly(salary);
        JPanel form = new JPanel(new GridLayout(1, 2, 10, 9));
        form.add(new JLabel("Monthly salary (P):"));
        form.add(salary);
        int result = JOptionPane.showConfirmDialog(this, form,
            "Set Salary - " + employeeName, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        String value = salary.getText().trim();
        if (!InputValidation.isDecimal(value)) {
            showInputError("Enter a valid non-negative monthly salary.");
            return;
        }
        try {
            StaffPayroll.setMonthlySalary(employeeId, Double.parseDouble(value));
            model.setValueAt(formatSalary(employeeId), modelRow, model.findColumn("Monthly Salary"));
            refreshFinanceMetrics();
            JOptionPane.showMessageDialog(this, "Monthly salary updated for " + employeeName + ".",
                "Salary updated", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException exception) {
            showInputError(exception.getMessage());
        }
    }

    /** Formats one employee's configured monthly salary as currency. */
    private String formatSalary(String employeeId) {
        return String.format("P%,.2f", StaffPayroll.getMonthlySalary(employeeId));
    }

    /** Installs the appropriate input filter for a module field. */
    private void configureFieldValidation(JTextField field, String columnName) {
        switch (getFieldType(columnName)) {
            case "letters":
                InputValidation.lettersOnly(field);
                break;
            case "digits":
                InputValidation.digitsOnly(field);
                break;
            case "decimal":
                InputValidation.decimalOnly(field);
                break;
            case "date":
                InputValidation.dateCharactersOnly(field);
                break;
            default:
                break;
        }
    }

    /** Maps a column label to the validation rule used for its input. */
    private String getFieldType(String columnName) {
        String name = columnName.toLowerCase();
        if (name.contains("email")) {
            return "email";
        }
        if (name.contains("date")) {
            return "date";
        }
        if (name.contains("phone")) {
            return "digits";
        }
        if (name.equals("stock") || name.equals("reorder level")) {
            return "decimal";
        }
        if (name.contains("price") || name.contains("amount") || name.contains("cost")) {
            return "decimal";
        }
        if (name.contains("stock") || name.contains("reorder level")
                || name.contains("total laundries")) {
            return "digits";
        }
        if (name.contains("total")) {
            return "decimal";
        }
        if (name.contains("name") || name.equals("role") || name.equals("category")
                || name.equals("unit") || name.equals("status") || name.equals("method")
                || name.equals("shift")) {
            return "letters";
        }
        return "text";
    }

    /** Shows a validation error to the admin. */
    private void showInputError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid input", JOptionPane.ERROR_MESSAGE);
    }

    /** Generates the next prefixed record ID based on the current row count. */
    private String createNextId(String section, DefaultTableModel model) {
        String prefix;
        switch (section) {
            case "Customer Management":
                prefix = "CUS";
                break;
            case "Laundry Oversight":
                prefix = "ORD";
                break;
            case "Service Catalog":
                prefix = "SRV";
                break;
            case "Inventory Control":
                prefix = "INV";
                break;
            case "Employee Management":
                prefix = "EMP";
                break;
            default:
                prefix = "REC";
        }
        int highestId = 0;
        for (int row = 0; row < model.getRowCount(); row++) {
            String id = String.valueOf(model.getValueAt(row, 0));
            if (!id.startsWith(prefix + "-")) {
                continue;
            }
            try {
                highestId = Math.max(highestId,
                    Integer.parseInt(id.substring(prefix.length() + 1)));
            } catch (NumberFormatException exception) {
                // Ignore IDs that do not use this module's numeric suffix.
            }
        }
        return prefix + "-" + String.format("%03d", highestId + 1);
    }

    /** Creates a primary header label using the dashboard's text style. */
    private JLabel heading(String value, int size) {
        JLabel label = new JLabel(value);
        label.setForeground(TEXT);
        label.setFont(new Font("Segoe UI", Font.BOLD, size));
        return label;
    }

    /** Creates a standard body label with the requested foreground color. */
    private JLabel text(String value, Color color) {
        JLabel label = new JLabel(value);
        label.setForeground(color);
        label.setFont(BODY);
        return label;
    }

    /** A module page uses the common lifecycle and layout supplied by its abstract base. */
    private static final class ModulePage extends AbstractDashboardPage {
        private static final long serialVersionUID = 1L;

        private ModulePage() {
            super(CANVAS, 26, 30, 30, 30);
        }
    }

    /** Holds admin account settings as a regular dashboard workspace page. */
    private static final class AdminAccountPage extends AbstractDashboardPage {
        private static final long serialVersionUID = 1L;

        private AdminAccountPage() {
            super(CANVAS, 26, 30, 30, 30);
        }
    }

    /** The finance page specializes page activation to refresh its live totals. */
    private final class FinancePage extends AbstractDashboardPage {
        private static final long serialVersionUID = 1L;

        private FinancePage() {
            super(CANVAS, 26, 30, 30, 30);
        }

        @Override
        public void onPageShown() {
            super.onPageShown();
            refreshFinanceMetrics();
        }
    }

    /**
     * Inherits the JPanel behavior and overrides painting to provide rounded cards;
     * Tinatawag  Swing the override polymorphically duri rendering.
     */
    private static final class RoundedPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final int radius;
        private final Color fill;

        /** Sets the fill and corner radius used to paint this card. */
        private RoundedPanel(int radius, Color fill) {
            this.radius = radius;
            this.fill = fill;
            setOpaque(false);
        }

        /** Paints the rounded background and border before drawi child components. */
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.setColor(BORDER);
            g.setStroke(new BasicStroke(1f));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class AccentBar extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Color accent;

        /** Stores the accent color used by this card's narrow decorative bar. */
        private AccentBar(Color accent) {
            this.accent = accent;
            setOpaque(false);
            setPreferredSize(new Dimension(0, 3));
        }

        /** Paints the rounded accent strip at the bottom of its containi card. */
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(accent);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
            g.dispose();
        }
    }
}
