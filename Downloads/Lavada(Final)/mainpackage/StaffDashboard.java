package mainpackage;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class StaffDashboard extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    private static final String[] LAUNDRY_STATUSES = {
        "In Queue", "Pending", "Ready to Pick Up", "Completed"
    };
    private static final String LOGO_PATH = "Images/Lavada Laundry App Logo.png";
    private static final ArrayList<Customer> CUSTOMERS = new ArrayList<Customer>();
    private static final ArrayList<LaundryOrder> ORDERS = new ArrayList<LaundryOrder>();
    private static final ArrayList<Payment> PAYMENTS = new ArrayList<Payment>();
    private static int customerSequence = 7;
    private static int orderSequence = 9;
    private static int paymentSequence = 6;

    static {
        CUSTOMERS.add(new Customer("C001", "Michael Davis Necesito", "09171234567", "Makati City"));
        CUSTOMERS.add(new Customer("C002", "Angel Mae Aquino", "09181234567", "Pasig City"));
        CUSTOMERS.add(new Customer("C003", "Marvin Ortega", "09191234567", "Quezon City"));
        CUSTOMERS.add(new Customer("C004", "Rocelyn Enderes", "09951234567", "Taguig City"));
        CUSTOMERS.add(new Customer("C005", "Ericson Binos", "09201234567", "Mandaluyo City"));
        CUSTOMERS.add(new Customer("C006", "Czaimon John", "09271234567", "San Juan City"));

        addSampleOrder("L001", "C001", "Wash and Dry", 4.5,
            1, 1, 0, "In Queue", sampleUsage(1, 0, 1, 0, 0));
        addSampleOrder("L002", "C002", "Blanket Washing", 2,
            2, 0, 0, "Ready to Pick Up", sampleUsage(1, 1, 1, 5, 0));
        addSampleOrder("L003", "C003", "Wash-Dry-Fold", 5,
            0, 1, 0, "Pending", sampleUsage(1, 1, 1, 0, 0));
        addSampleOrder("L004", "C004", "Dry Cleaning", 3,
            4, -1, 10, "Completed", sampleUsage(0, 0, 0, 10, 0));
        addSampleOrder("L005", "C001", "Ironing", 6,
            0, 1, 0, "In Queue", sampleUsage(0, 0, 0, 0, 0));
        addSampleOrder("L006", "C005", "Wash and Dry", 8,
            3, -1, 0, "Completed", sampleUsage(2, 1, 2, 0, 0));
        addSampleOrder("L007", "C006", "Dry Cleaning", 2,
            1, 2, 0, "In Queue", sampleUsage(0, 0, 1, 0, 5));
        addSampleOrder("L008", "C002", "Wash-Dry-Fold", 3,
            1, 1, 5, "Pending", sampleUsage(1, 1, 1, 0, 0));

        addSampleFullPayment("P001", "L002", 1);
        addSamplePayment("P002", "L003", 200.0, 0);
        addSampleFullPayment("P003", "L004", 1);
        addSampleFullPayment("P004", "L006", 1);
        addSamplePayment("P005", "L008", 100.0, 1);
    }

    /** Adds  realistic order example and reflects its supply use in openi stock. */
    private static void addSampleOrder(String orderId, String customerId, String serviceType,
            double quantity, int receivedDaysAgo, int dueDaysFromNow, double discount,
            String status, Map<String, Double> supplyUsage) {
        LocalDate received = LocalDate.now().minusDays(receivedDaysAgo);
        ServiceCatalog.Service service = ServiceCatalog.findByName(serviceType);
        if (service == null) {
            throw new IllegalStateException("Sample laundry service is missing: " + serviceType);
        }
        double serviceAmount = quantity * service.getRate();
        double supplyAmount = InventoryData.getUsageCost(supplyUsage);
        double originalAmount = serviceAmount + supplyAmount;
        LaundryOrder order = new LaundryOrder(orderId, customerId, serviceType, quantity,
            received, LocalDate.now().plusDays(dueDaysFromNow), originalAmount, status);
        order.setPriceBreakdown(serviceAmount, supplyAmount);
        order.setDiscountPercentage(discount);
        order.setSupplyUsage(supplyUsage);
        ORDERS.add(order);
        InventoryData.consume(supplyUsage);
    }

    /** Creates  supply quantities used by one startup sample order. */
    private static Map<String, Double> sampleUsage(double detergent, double conditioner,
            double bags, double bleach, double bleachColorSafe) {
        Map<String, Double> usage = new LinkedHashMap<String, Double>();
        usage.put("INV-001", detergent);
        usage.put("INV-002", conditioner);
        usage.put("INV-003", bags);
        usage.put("INV-004", bleach);
        usage.put("INV-005", bleachColorSafe);
        return usage;
    }

    private static void addSampleFullPayment(String paymentId, String orderId, int daysAgo) {
        LaundryOrder order = LaundryData.findOrder(orderId);
        if (order == null) {
            throw new IllegalStateException("Sample payment laundry is missing: " + orderId);
        }
        addSamplePayment(paymentId, orderId, order.getTotalAmount(), daysAgo);
    }

    private static void addSamplePayment(String paymentId, String orderId,
            double amount, int daysAgo) {
        LaundryOrder order = LaundryData.findOrder(orderId);
        if (order == null) {
            throw new IllegalStateException("Sample payment laundry is missing: " + orderId);
        }
        if (!Double.isFinite(amount) || amount <= 0 || amount > order.getTotalAmount()) {
            throw new IllegalStateException("Sample payment is invalid for laundry " + orderId);
        }
        PAYMENTS.add(new Payment(paymentId, orderId, amount,
            LocalDate.now().minusDays(daysAgo)));
    }
    private static final String[] MENU_ITEMS = {
        "Dashboard", "Customers", "New Laundry", "Laundries", "Pick Up / Release"
    };
    private static final Color CYAN = new Color(0, 176, 208);
    private static final Color DARK_CYAN = new Color(0, 64, 144);
    private static final Color SIDEBAR = new Color(45, 57, 63);
    private static final Color SIDEBAR_LIGHT = new Color(55, 69, 76);
    private static final Color BACKGROUND = new Color(245, 247, 251);
    private static final Color TEXT = new Color(39, 52, 69);
    private static final Color MUTED = new Color(133, 145, 160);
    private static final Color BORDER = new Color(231, 235, 241);
    private static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final Map<String, JButton> navigationButtons = new LinkedHashMap<String, JButton>();
    private final Map<JTable, javax.swing.table.TableRowSorter<DefaultTableModel>> tableSorters =
        new IdentityHashMap<JTable, javax.swing.table.TableRowSorter<DefaultTableModel>>();
    private final JLabel breadcrumb = new JLabel();
    private String selectedPage = "Dashboard";

    /** Builds the staff dashboard and initializes its navigation and pages. */
    public StaffDashboard() {
        setTitle("Laundry Management System - Staff Dashboard");
        setResizable(false);
        ImageIcon logo = new ImageIcon(LOGO_PATH);
        if (logo.getIconWidth() > 0) {
            setIconImage(logo.getImage());
        }
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));
        setSize(1250, 780);
        setExtendedState(MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        addWindowListener(new WindowAdapter() {
            /** Opens the logout confirmation when the dashboard is closed. */
            @Override
            public void windowClosing(WindowEvent event) {
                logout();
            }
        });

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR);
        sidebar.setPreferredSize(new Dimension(190, 0));
        JPanel menu = new JPanel();
        menu.setBackground(SIDEBAR);
        menu.setBorder(BorderFactory.createEmptyBorder(12, 0, 8, 0));
        menu.setLayout(new GridLayout(MENU_ITEMS.length, 1, 0, 8));
        for (String item : MENU_ITEMS) {
            JButton button = createNavigationButton(item);
            navigationButtons.put(item, button);
            menu.add(button);
        }
        JButton switchToAdmin = createNavigationButton("Switch to Admin (Test)");
        JButton logout = createNavigationButton("Log Out");
        JPanel logoutFooter = new JPanel();
        logoutFooter.setLayout(new javax.swing.BoxLayout(logoutFooter, javax.swing.BoxLayout.Y_AXIS));
        logoutFooter.setBackground(SIDEBAR);
        logoutFooter.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        logoutFooter.add(switchToAdmin);
        logoutFooter.add(javax.swing.Box.createVerticalStrut(8));
        logoutFooter.add(logout);
        sidebar.add(menu, BorderLayout.CENTER);
        sidebar.add(logoutFooter, BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BACKGROUND);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            BorderFactory.createEmptyBorder(0, 30, 0, 30)));
        header.setPreferredSize(new Dimension(0, 72));
        breadcrumb.setText("<html><font color='#8996A7'>WORKSPACE</font>  /  <b>DASHBOARD</b></html>");
        breadcrumb.setForeground(TEXT);
        breadcrumb.setFont(BODY);
        JPanel headerRight = new JPanel(new BorderLayout(12, 0));
        headerRight.setOpaque(false);
        JLabel date = new JLabel(LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")));
        date.setFont(BODY);
        date.setForeground(MUTED);
        JLabel avatar = new JLabel("Staff", SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(222, 244, 241));
        avatar.setForeground(DARK_CYAN);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        avatar.setPreferredSize(new Dimension(56, 36));
        headerRight.add(date, BorderLayout.CENTER);
        headerRight.add(avatar, BorderLayout.EAST);
        header.add(breadcrumb, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);
        main.add(header, BorderLayout.NORTH);

        content.setBackground(BACKGROUND);
        content.add(createDashboardPage(), "Dashboard");
        content.add(createCustomersPage(), "Customers");
        content.add(createNewOrderPage(), "New Laundry");
        content.add(createOrdersPage(), "Laundries");
        content.add(createPickupPage(), "Pick Up / Release");
        main.add(content, BorderLayout.CENTER);
        add(sidebar, BorderLayout.WEST);
        add(main, BorderLayout.CENTER);
        updateNavigationHighlight();
        refreshDashboard();
    }

    /** Exposes the live staff orders as rows for the admin oversight table. */
    static Object[][] getOrderOversightRows() {
        ArrayList<LaundryOrder> orders = LaundryData.getOrders();
        Object[][] rows = new Object[orders.size()][10];
        java.time.format.DateTimeFormatter dateFormat =
            java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy");
        for (int index = 0; index < orders.size(); index++) {
            LaundryOrder order = orders.get(index);
            Customer customer = LaundryData.findCustomer(order.getCustomerId());
            rows[index] = new Object[] {
                order.getOrderId(),
                customer == null ? "(customer deleted)" : customer.getName(),
                order.getServiceType(),
                order.getDateReceived().format(dateFormat),
                LaundryData.getPaymentStatus(order),
                String.format("P%,.2f", order.getOriginalAmount()),
                formatDiscount(order.getDiscountPercentage()),
                String.format("P%,.2f", order.getTotalAmount()),
                order.getLaundryStatus(),
                order.isAddedByAdmin() ? "ADMIN" : "STAFF"
            };
        }
        return rows;
    }

    /** Returns the customer options formatted with stable IDs for admin order creatingon. */
    static String[] getAdminCustomerOptions() {
        ArrayList<String> options = new ArrayList<String>();
        for (Customer customer : LaundryData.getCustomers()) {
            options.add(customer.getCustomerId() + " - " + customer.getName());
        }
        return options.toArray(new String[0]);
    }

    static String[] getActiveServiceNames() {
        return ServiceCatalog.getActiveServiceNames().toArray(new String[0]);
    }

    /** Creates  order from the admin oversight screen and marks its origin. */
    static String addOrderForAdmin(String customerId, String serviceName, double quantity,
            LocalDate receivedDate, LocalDate dueDate, double discountPercentage,
            Map<String, Double> usage, boolean paid) {
        if (LaundryData.findCustomer(customerId) == null) {
            throw new IllegalArgumentException("Select an available customer.");
        }
        ServiceCatalog.Service service = ServiceCatalog.findByName(serviceName);
        if (service == null || !service.isActive()) {
            throw new IllegalArgumentException("Select an active service.");
        }
        if (!Double.isFinite(quantity) || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        if (receivedDate == null || dueDate == null || dueDate.isBefore(receivedDate)) {
            throw new IllegalArgumentException("Due date cannot be earlier than date received.");
        }
        if (!Double.isFinite(discountPercentage) || discountPercentage < 0
                || discountPercentage > 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100 percent.");
        }
        Map<String, Double> supplyUsage = validateAdminSupplyUsage(usage);
        double serviceAmount = quantity * service.getRate();
        double supplyAmount = InventoryData.getUsageCost(supplyUsage);
        double originalAmount = serviceAmount + supplyAmount;
        double total = Math.round(originalAmount * (100.0 - discountPercentage)) / 100.0;
        if (!Double.isFinite(total)) {
            throw new IllegalArgumentException("The calculated laundry amount is too large.");
        }
        InventoryData.consume(supplyUsage);
        String orderId = LaundryData.createOrderId();
        LaundryOrder order = new LaundryOrder(orderId, customerId, serviceName, quantity,
            receivedDate, dueDate, originalAmount, "In Queue");
        order.setPriceBreakdown(serviceAmount, supplyAmount);
        order.setDiscountPercentage(discountPercentage);
        order.setSupplyUsage(supplyUsage);
        order.setAddedByAdmin(true);
        LaundryData.getOrders().add(order);
        if (paid && total > 0) {
            LaundryData.recordPayment(order, total);
        }
        refreshAllStaffViews();
        return orderId;
    }

    private static Map<String, Double> validateAdminSupplyUsage(Map<String, Double> usage) {
        Map<String, Double> validated = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> entry : usage.entrySet()) {
            Double amount = entry.getValue();
            if (amount == null || !Double.isFinite(amount) || amount < 0) {
                throw new IllegalArgumentException(
                    "Supply quantities must be valid non-negative numbers.");
            }
            if (InventoryData.findItem(entry.getKey()) == null) {
                throw new IllegalArgumentException("An inventory item is no longer available.");
            }
            validated.put(entry.getKey(), amount);
        }
        return validated;
    }

    static double cancelOrder(String orderId) {
        LaundryOrder order = LaundryData.findOrder(orderId);
        if (order == null) {
            throw new IllegalArgumentException("That laundry is no longer available.");
        }
        double refund = LaundryData.getCancellationRefund(order);
        LaundryData.cancelOrder(order);
        return refund;
    }

    static double getOrderCancellationRefund(String orderId) {
        LaundryOrder order = LaundryData.findOrder(orderId);
        if (order == null) {
            throw new IllegalArgumentException("That laundry is no longer available.");
        }
        return LaundryData.getCancellationRefund(order);
    }

    /** Updates the existing order from admin without changing its recorded payment history. */
    static void updateOrderForAdmin(String orderId, String customerId, String serviceName,
            double quantity, LocalDate receivedDate, LocalDate dueDate,
            double discountPercentage, String status, Map<String, Double> supplyUsage,
            boolean markPaid) {
        LaundryOrder order = LaundryData.findOrder(orderId);
        if (order == null) {
            throw new IllegalArgumentException("That laundry is no longer available.");
        }
        if ("Cancelled".equals(order.getLaundryStatus())) {
            throw new IllegalArgumentException("Cancelled laundries cannot be edited.");
        }
        if ("Completed".equals(order.getLaundryStatus()) && !"Completed".equals(status)) {
            throw new IllegalArgumentException(
                "Completed laundries cannot be moved back to another status.");
        }
        if (LaundryData.findCustomer(customerId) == null) {
            throw new IllegalArgumentException("Select an available customer.");
        }
        ServiceCatalog.Service service = ServiceCatalog.findByName(serviceName);
        if (service == null) {
            throw new IllegalArgumentException("Select a valid service.");
        }
        if (!Double.isFinite(quantity) || quantity <= 0
                || !Double.isFinite(discountPercentage)
                || discountPercentage < 0 || discountPercentage > 100) {
            throw new IllegalArgumentException("Enter a valid quantity and discount.");
        }
        if (receivedDate == null || dueDate == null || dueDate.isBefore(receivedDate)) {
            throw new IllegalArgumentException("Due date cannot be earlier than date received.");
        }
        boolean validStatus = false;
        for (String laundryStatus : LAUNDRY_STATUSES) {
            if (laundryStatus.equals(status)) {
                validStatus = true;
                break;
            }
        }
        if (!validStatus) {
            throw new IllegalArgumentException("Select a valid laundry status.");
        }
        if (statusRank(status) < statusRank(order.getLaundryStatus())) {
            throw new IllegalArgumentException("a laundry cannot move back to an earlier laundry status.");
        }
        double serviceAmount = quantity * service.getRate();
        double supplyAmount = InventoryData.getUsageCost(supplyUsage);
        double originalAmount = serviceAmount + supplyAmount;
        double total = Math.round(originalAmount * (100.0 - discountPercentage)) / 100.0;
        if (!Double.isFinite(originalAmount) || !Double.isFinite(total)) {
            throw new IllegalArgumentException("The calculated laundry amount is too large.");
        }
        double amountPaid = LaundryData.getAmountPaid(orderId);
        if (total + 0.00001 < amountPaid) {
            throw new IllegalArgumentException(
                "The updated laundry total cannot be less than the amount already paid.");
        }
        if ("Completed".equals(status)
                && !markPaid && amountPaid + 0.00001 < total) {
            throw new IllegalArgumentException(
                "a laundry must be fully paid before it can be marked completed.");
        }
        InventoryData.updateUsage(order.getSupplyUsage(), supplyUsage);
        order.setCustomerId(customerId);
        order.setServiceType(serviceName);
        order.setQuantity(quantity);
        order.setDateReceived(receivedDate);
        order.setDueDate(dueDate);
        order.setTotalAmount(originalAmount);
        order.setPriceBreakdown(serviceAmount, supplyAmount);
        order.setDiscountPercentage(discountPercentage);
        order.setSupplyUsage(supplyUsage);
        order.setLaundryStatus(status);
        if (markPaid && amountPaid + 0.00001 < total) {
            LaundryData.recordPayment(order, total - amountPaid);
        }
        refreshAllStaffViews();
    }

    private static int statusRank(String status) {
        for (int index = 0; index < LAUNDRY_STATUSES.length; index++) {
            if (LAUNDRY_STATUSES[index].equals(status)) {
                return index;
            }
        }
        return -1;
    }

    /** Deletes the admin-selected order and its payment records. */
    static void deleteOrderForAdmin(String orderId) {
        LaundryOrder order = LaundryData.findOrder(orderId);
        if (order == null) {
            throw new IllegalArgumentException("That laundry is no longer available.");
        }
        LaundryData.deleteOrder(order);
        refreshAllStaffViews();
    }

    static void deleteAllOrdersForAdmin() {
        LaundryData.deleteAllOrders();
        refreshAllStaffViews();
    }

    private static void refreshAllStaffViews() {
        for (java.awt.Window openWindow : java.awt.Window.getWindows()) {
            if (openWindow instanceof StaffDashboard) {
                ((StaffDashboard) openWindow).refreshVisiblePages();
            }
        }
    }

    /** Returns the customer rows and order counts from the shared staff data. */
    static Object[][] getCustomerManagementRows() {
        ArrayList<Customer> customers = LaundryData.getCustomers();
        Object[][] rows = new Object[customers.size()][5];
        for (int index = 0; index < customers.size(); index++) {
            Customer customer = customers.get(index);
            rows[index] = new Object[] {
                customer.getCustomerId(),
                customer.getName(),
                customer.getContactNumber(),
                customer.getAddress(),
                LaundryData.getOrderCount(customer.getCustomerId())
            };
        }
        return rows;
    }

    /** Returns the service catalog rows matching the services available to staff. */
    static Object[][] getServiceCatalogRows() {
        Object[][] rows = new Object[ServiceCatalog.getServices().size()][5];
        for (int index = 0; index < ServiceCatalog.getServices().size(); index++) {
            ServiceCatalog.Service service = ServiceCatalog.getServices().get(index);
            rows[index] = new Object[] {
                service.getId(),
                service.getName(),
                String.format("P%,.2f", service.getRate()),
                service.getUnit(),
                service.isActive() ? "Active" : "Inactive"
            };
        }
        return rows;
    }

    /** Checks if saved orders refer to the named service. */
    static boolean isServiceUsed(String serviceName) {
        for (LaundryOrder order : LaundryData.getOrders()) {
            if (order.getServiceType().equals(serviceName)) {
                return true;
            }
            for (ServiceCatalog.Service service : ServiceCatalog.getServices()) {
                if (ServiceCatalog.matchesOrderName(service.getId(), order.getServiceType())
                        && service.getName().equals(serviceName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Refreshes the all staff views that display available services or order data. */
    static void refreshServiceCatalogViews() {
        for (java.awt.Window openWindow : java.awt.Window.getWindows()) {
            if (openWindow instanceof StaffDashboard) {
                ((StaffDashboard) openWindow).refreshServiceCatalog();
            }
        }
    }

    static void refreshInventoryViews() {
        refreshAllStaffViews();
    }

    /** Updates the new-order and order-edit selectors to use only active services. */
    private void refreshServiceCatalog() {
        JPanel newOrderPage = (JPanel) content.getComponent(2);
        @SuppressWarnings("unchecked")
        JComboBox<String> newOrderChoice =
            (JComboBox<String>) newOrderPage.getClientProperty("orderService");
        refreshServiceChoice(newOrderChoice, null);
        refreshVisiblePages();
    }

    /** Replaces selector choices habthe preservi a still-active selection. */
    private void refreshServiceChoice(JComboBox<String> choice, String preferredName) {
        String selectedName = preferredName == null
            ? (String) choice.getSelectedItem() : preferredName;
        choice.removeAllItems();
        for (String serviceName : ServiceCatalog.getActiveServiceNames()) {
            choice.addItem(serviceName);
        }
        if (selectedName != null) {
            choice.setSelectedItem(selectedName);
        }
    }

    /** Summarizes the current orders and payments for the admin dashboard. */
    static Object[] getAdminDashboardSummary() {
        LocalDate today = LocalDate.now();
        int ordersToday = 0;
        int inProgress = 0;
        int inQueue = 0;
        int ready = 0;
        int completed = 0;
        int unpaidOrders = 0;
        int overdue = 0;
        double outstanding = 0;
        for (LaundryOrder order : LaundryData.getOrders()) {
            if (today.equals(order.getDateReceived())) {
                ordersToday++;
            }
            String status = order.getLaundryStatus();
            if ("In Queue".equals(status)) {
                inQueue++;
            } else if ("Pending".equals(status)) {
                inProgress++;
            } else if ("Ready to Pick Up".equals(status)) {
                ready++;
            } else if ("Completed".equals(status)) {
                completed++;
            }
            if ("Cancelled".equals(status)) {
                continue;
            }
            String paymentStatus = LaundryData.getPaymentStatus(order);
            if ("Unpaid".equals(paymentStatus)) {
                unpaidOrders++;
            }
            if ("Unpaid".equals(paymentStatus)) {
                outstanding += Math.max(0,
                    order.getTotalAmount() - LaundryData.getAmountPaid(order.getOrderId()));
            }
            if (!"Completed".equals(status)
                    && !order.getDateReceived().isAfter(today.minusDays(3))) {
                overdue++;
            }
        }

        double salesToday = 0;
        double salesThisWeek = 0;
        double salesThisMonth = 0;
        for (Payment payment : LaundryData.getPayments()) {
            LocalDate paymentDate = payment.getPaymentDate();
            if (today.equals(paymentDate)) {
                salesToday += payment.getAmount();
            }
            if (!paymentDate.isBefore(today.minusDays(6)) && !paymentDate.isAfter(today)) {
                salesThisWeek += payment.getAmount();
            }
            if (today.getYear() == paymentDate.getYear()
                    && today.getMonth() == paymentDate.getMonth()) {
                salesThisMonth += payment.getAmount();
            }
        }
        return new Object[] {
            String.valueOf(ordersToday),
            String.valueOf(inProgress),
            String.valueOf(ready),
            formatDashboardMoney(salesToday),
            String.valueOf(ready),
            String.valueOf(unpaidOrders),
            String.valueOf(overdue),
            formatDashboardMoney(salesToday),
            formatDashboardMoney(salesThisWeek),
            formatDashboardMoney(salesThisMonth),
            formatDashboardMoney(outstanding),
            String.valueOf(completed),
            formatDashboardMoney(salesToday),
            String.valueOf(inQueue)
        };
    }

    /** Totals the collected payments after subtracti recorded refunds. */
    static double getTotalSales() {
        double total = 0;
        for (Payment payment : LaundryData.getPayments()) {
            total += payment.getAmount();
        }
        return total;
    }

    /** Formats the dashboard amounts using the application's currency display. */
    private static String formatDashboardMoney(double amount) {
        return String.format("P%,.2f", amount);
    }

    /** Adds  admin-created customer to the same data used by staff orders. */
    static void addCustomerForAdmin(String name, String phone, String address) {
        String customerId = LaundryData.createAvailableCustomerId();
        LaundryData.addCustomer(new Customer(customerId, name, phone, address));
    }

    /** Updates the shared customer record by ID, returning false when it no longer exists. */
    static boolean updateCustomerForAdmin(String customerId, String name, String phone,
            String address) {
        Customer customer = LaundryData.findCustomer(customerId);
        if (customer == null) {
            return false;
        }
        customer.setName(name);
        customer.setContactNumber(phone);
        customer.setAddress(address);
        return true;
    }

    /** Deletes the shared customer record habthe preservi customers with order history. */
    static void deleteCustomerForAdmin(String customerId) {
        Customer customer = LaundryData.findCustomer(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("The selected customer no longer exists.");
        }
        LaundryData.deleteCustomer(customer);
    }

    /** Checks if any saved order has consumed a meaningful amount of this item. */
    static boolean isInventoryItemUsed(String itemId) {
        for (LaundryOrder order : LaundryData.getOrders()) {
            if (order.getSupplyUsage().getOrDefault(itemId, 0.0) > 0.00001) {
                return true;
            }
        }
        return false;
    }

    /** Builds the detailed order information for the admin's selected order. */
    static String getOrderInfoForAdmin(String orderId) {
        LaundryOrder order = LaundryData.findOrder(orderId);
        if (order == null) {
            return null;
        }
        Customer customer = LaundryData.findCustomer(order.getCustomerId());
        StringBuilder info = new StringBuilder()
            .append("Laundry ID: ").append(order.getOrderId())
            .append("\nCustomer: ").append(customer == null ? "(customer deleted)" : customer.getName())
            .append("\nService: ").append(order.getServiceType())
            .append("\nQuantity: ").append(String.format("%.2f", order.getQuantity()))
            .append("\nDate received: ").append(order.getDateReceived())
            .append("\nDue date: ").append(order.getDueDate())
            .append("\nOriginal price: ").append(String.format("P%,.2f", order.getOriginalAmount()))
            .append("\nDiscount: ").append(formatDiscount(order.getDiscountPercentage()))
            .append("\nTotal: ").append(String.format("P%,.2f", order.getTotalAmount()))
            .append("\nAmount paid: ").append(String.format("P%,.2f",
                LaundryData.getAmountPaid(order.getOrderId())))
            .append(LaundryData.getRefundedAmount(order.getOrderId()) > 0.00001
                ? "\nRefunded: " + String.format("P%,.2f",
                    LaundryData.getRefundedAmount(order.getOrderId())) : "")
            .append("\nPayment status: ").append(LaundryData.getPaymentStatus(order))
            .append("\nLaundry status: ").append(order.getLaundryStatus())
            .append(LaundryData.getRefundedAmount(order.getOrderId()) > 0.00001
                ? "\nRefunded: " + String.format("P%,.2f",
                    LaundryData.getRefundedAmount(order.getOrderId())) : "")
            .append("\n\nSupplies used:");
        boolean hasUsage = false;
        for (Map.Entry<String, Double> entry : order.getSupplyUsage().entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            InventoryData.Item item = InventoryData.findItem(entry.getKey());
            if (item != null) {
                info.append("\n-  ").append(item.getName()).append(": ")
                    .append(String.format("%.2f", entry.getValue())).append(" ")
                    .append(item.getUnit());
                hasUsage = true;
            }
        }
        if (!hasUsage) {
            info.append("\nNone recorded");
        }
        return info.toString();
    }

    /** Provides  editable order fields to the admin oversight editor. */
    static Object[] getAdminOrderEditData(String orderId) {
        LaundryOrder order = LaundryData.findOrder(orderId);
        if (order == null || "Cancelled".equals(order.getLaundryStatus())) {
            return null;
        }
        return new Object[] {
            order.getCustomerId(), order.getServiceType(), order.getQuantity(),
            order.getDateReceived(), order.getDueDate(), order.getDiscountPercentage(),
            order.getLaundryStatus(), new LinkedHashMap<String, Double>(order.getSupplyUsage()),
            LaundryData.getPaymentStatus(order)
        };
    }

    /** Creates a styled sidebar button for a staff page or logout action. */
    private JButton createNavigationButton(String text) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(Color.WHITE);
        button.setBackground(SIDEBAR_LIGHT);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 10));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setPreferredSize(new Dimension(190, 44));
        button.setActionCommand(text);
        button.addActionListener(this);
        return button;
    }

    /** Routes sidebar button events to the matching staff dashboard action. */
    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if ("Log Out".equals(command)) {
            logout();
        } else if ("Switch to Admin (Test)".equals(command)) {
            switchToAdminForTesting();
        } else {
            showPage(command);
        }
    }

    /** Selects a staff page, refreshes its data, and updates the navigation state. */
    private void showPage(String page) {
        selectedPage = page;
        breadcrumb.setText("<html><font color='#8996A7'>WORKSPACE</font>  /  <b>"
            + page.toUpperCase() + "</b></html>");
        refreshPage(page);
        cardLayout.show(content, page);
        updateNavigationHighlight();
    }

    /** Refreshes the data model for the requested staff page. */
    private void refreshPage(String page) {
        switch (page) {
            case "Dashboard":
                refreshDashboard();
                break;
            case "Customers":
                refreshCustomers();
                break;
            case "New Laundry":
                refreshNewOrderCustomers();
                refreshSupplyAvailability();
                break;
            case "Laundries":
                refreshOrders();
                break;
            case "Pick Up / Release":
                refreshPickup();
                break;
            default:
                break;
        }
    }

    /** Marks the currently selected page in the sidebar. */
    private void updateNavigationHighlight() {
        for (Map.Entry<String, JButton> entry : navigationButtons.entrySet()) {
            entry.getValue().setBackground(entry.getKey().equals(selectedPage)
                ? DARK_CYAN : SIDEBAR_LIGHT);
        }
    }

    // Dashboard summaries ay calculated from the live in-memory records.
    /** Builds the staff overview page and its live summary-card container. */
    private JPanel createDashboardPage() {
        JPanel page = pagePanel();
        page.setLayout(new BorderLayout(0, 20));
        page.add(pageHeading("Welcome back, Staff ", "Here's a quick overview of your workspace."),
            BorderLayout.NORTH);
        JPanel dashboardContent = new JPanel();
        dashboardContent.setOpaque(false);
        dashboardContent.setLayout(new javax.swing.BoxLayout(dashboardContent,
            javax.swing.BoxLayout.Y_AXIS));
        JPanel cards = new JPanel(new GridLayout(2, 3, 16, 16));
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        cards.setAlignmentX(LEFT_ALIGNMENT);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        dashboardContent.add(cards);
        dashboardContent.add(javax.swing.Box.createVerticalStrut(18));
        JPanel attention = new JPanel();
        attention.setAlignmentX(LEFT_ALIGNMENT);
        dashboardContent.add(attention);
        JScrollPane scroll = new JScrollPane(dashboardContent);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        page.add(scroll, BorderLayout.CENTER);
        page.putClientProperty("dashboardCards", cards);
        page.putClientProperty("dashboardAttention", attention);
        return page;
    }

    /** Recalculates the order, customer, and sales summaries from current records. */
    private void refreshDashboard() {
        JPanel page = (JPanel) content.getComponent(0);
        JPanel cards = (JPanel) page.getClientProperty("dashboardCards");
        cards.removeAll();
        int todayOrders = 0;
        int pendingOrders = 0;
        int queuedOrders = 0;
        int readyOrders = 0;
        int completedOrders = 0;
        int ordersWithBalance = 0;
        LocalDate today = LocalDate.now();
        double todaysSales = 0;
        for (LaundryOrder order : LaundryData.getOrders()) {
            if (today.equals(order.getDateReceived())) {
                todayOrders++;
            }
            if ("In Queue".equals(order.getLaundryStatus())) {
                queuedOrders++;
            } else if ("Pending".equals(order.getLaundryStatus())) {
                pendingOrders++;
            } else if ("Ready to Pick Up".equals(order.getLaundryStatus())) {
                readyOrders++;
            } else if ("Completed".equals(order.getLaundryStatus())) {
                completedOrders++;
            }
            if (!"Cancelled".equals(order.getLaundryStatus())
                    && order.getTotalAmount() - LaundryData.getAmountPaid(order.getOrderId())
                        > 0.00001) {
                ordersWithBalance++;
            }
        }
        for (Payment payment : LaundryData.getPayments()) {
            if (today.equals(payment.getPaymentDate())) {
                todaysSales += payment.getAmount();
            }
        }
        cards.add(summaryCard("Today's Laundries", String.valueOf(todayOrders), "Laundries received today"));
        cards.add(summaryCard("Pending Laundries", String.valueOf(pendingOrders), "Awaiting laundry processing"));
        cards.add(summaryCard("Ready to Pick Up", String.valueOf(readyOrders), "Ready to be released"));
        cards.add(summaryCard("Completed Laundries", String.valueOf(completedOrders), "Laundry completed"));
        cards.add(summaryCard("Today's Sales", money(todaysSales), "Payments recorded today"));
        cards.add(summaryCard("Customers", String.valueOf(LaundryData.getCustomers().size()),
            "Customers in this session"));
        JPanel attention = (JPanel) page.getClientProperty("dashboardAttention");
        refreshStaffAttentionPanel(attention, queuedOrders, readyOrders, ordersWithBalance);
        cards.revalidate();
        cards.repaint();
    }

    /** Rebuilds the clickable dashboard alerts from current inventory and order records. */
    private void refreshStaffAttentionPanel(JPanel panel, int queuedOrders,
            int readyOrders, int ordersWithBalance) {
        panel.removeAll();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JLabel title = new JLabel("Needs attention");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Review laundries that may need a quick follow-up");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(MUTED);
        panel.add(title);
        panel.add(javax.swing.Box.createVerticalStrut(3));
        panel.add(subtitle);
        panel.add(javax.swing.Box.createVerticalStrut(8));
        panel.add(createStaffAttentionRow("Laundries in queue",
            queuedOrders + " laundries", "Laundries", new Color(0, 176, 208)));
        panel.add(createStaffAttentionRow("Laundries ready for pickup",
            readyOrders + " laundries", "Pick Up / Release", new Color(0, 176, 208)));
        panel.add(createStaffAttentionRow("Laundries with balance",
            ordersWithBalance + " laundries", "Laundries", new Color(217, 111, 91)));
        panel.revalidate();
        panel.repaint();
    }

    /** Creates  clickable attention row that opens its related staff page. */
    private JButton createStaffAttentionRow(String title, String count, String page, Color accent) {
        JButton item = new JButton();
        item.setLayout(new BorderLayout(11, 0));
        item.setOpaque(true);
        item.setBackground(new Color(246, 248, 251));
        item.setContentAreaFilled(true);
        item.setFocusPainted(false);
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(7, 8, 7, 8)));
        item.setAlignmentX(LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        JLabel dot = new JLabel(".", SwingConstants.CENTER);
        dot.setForeground(accent);
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dot.setPreferredSize(new Dimension(15, 18));
        JLabel name = new JLabel(title);
        name.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        name.setForeground(TEXT);
        JLabel amount = new JLabel(count + "   >");
        amount.setFont(new Font("Segoe UI", Font.BOLD, 11));
        amount.setForeground(accent);
        item.add(dot, BorderLayout.WEST);
        item.add(name, BorderLayout.CENTER);
        item.add(amount, BorderLayout.EAST);
        item.addActionListener(event -> showPage(page));
        return item;
    }

    /** Creates  one dashboard summary card with a value and explanatory note. */
    private JPanel summaryCard(String label, String value, String note) {
        Color accent;
        if ("Today's Laundries".equals(label)) {
            accent = new Color(53, 132, 213);
        } else if ("Pending Laundries".equals(label)) {
            accent = new Color(218, 148, 50);
        } else if ("Ready to Pick Up".equals(label)) {
            accent = new Color(0, 176, 208);
        } else if ("Completed Laundries".equals(label)) {
            accent = new Color(72, 166, 111);
        } else if ("Today's Sales".equals(label)) {
            accent = new Color(125, 113, 202);
        } else {
            accent = DARK_CYAN;
        }
        RoundedPanel card = new RoundedPanel(15, Color.WHITE);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(15, 17, 13, 17));
        JLabel name = new JLabel(label);
        name.setFont(new Font("Segoe UI", Font.BOLD, 10));
        name.setForeground(MUTED);
        JLabel number = new JLabel(value);
        number.setFont(new Font("Segoe UI", Font.BOLD, 24));
        number.setForeground(TEXT);
        JLabel detail = new JLabel(note);
        detail.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        detail.setForeground(MUTED);
        JPanel information = new JPanel(new BorderLayout(0, 5));
        information.setOpaque(false);
        information.add(number, BorderLayout.NORTH);
        information.add(detail, BorderLayout.CENTER);
        card.add(name, BorderLayout.NORTH);
        card.add(information, BorderLayout.CENTER);
        card.add(new AccentBar(accent), BorderLayout.SOUTH);
        return card;
    }

    // Customer management: table refresh, searching, validation, and CRUD actions.
    /** Builds the customer list, search field, and customer-management actions. */
    private JPanel createCustomersPage() {
        JPanel page = pagePanel();
        JTextField search = new JTextField(22);
        JTable table = createTable(new String[] {
            "Customer ID", "Customer Name", "Contact Number", "Address"
        });
        JPanel actions = actionBar();
        JButton add = actionButton("Add Customer");
        JButton edit = actionButton("Edit Customer");
        JButton delete = actionButton("Delete Customer");
        JButton find = actionButton("Search Customer");
        add.addActionListener(event -> addCustomer());
        edit.addActionListener(event -> editCustomer(table));
        delete.addActionListener(event -> deleteCustomer(table));
        find.addActionListener(event -> filterTable(table, search.getText()));
        search.addActionListener(event -> filterTable(table, search.getText()));
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        page.add(buildTablePageHeader("Customers", "Add, update, search, and manage customer records.",
            search, find), BorderLayout.NORTH);
        page.add(tablePanel(table, actions), BorderLayout.CENTER);
        page.putClientProperty("customersTable", table);
        page.putClientProperty("customerSearch", search);
        return page;
    }

    /** Rebuilds the customer table from the current customer records. */
    private void refreshCustomers() {
        JPanel page = (JPanel) content.getComponent(1);
        JTable table = (JTable) page.getClientProperty("customersTable");
        JTextField search = (JTextField) page.getClientProperty("customerSearch");
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (Customer customer : LaundryData.getCustomers()) {
            model.addRow(new Object[] {
                customer.getCustomerId(), customer.getName(),
                customer.getContactNumber(), customer.getAddress()
            });
        }
        filterTable(table, search.getText());
    }

    /** Validates and adds a customer entered through the customer dialog. */
    private void addCustomer() {
        CustomerFields fields = showCustomerDialog(null);
        if (fields == null) {
            return;
        }
        if (fields.customerId.trim().isEmpty() || fields.name.trim().isEmpty()
                || fields.contact.trim().isEmpty() || fields.address.trim().isEmpty()) {
            showError("All customer fields are required.");
            return;
        }
        if (!InputValidation.isLettersOnly(fields.name.trim())) {
            showError("Customer name may contain letters, spaces, apostrophes, hyphens, and periods only.");
            return;
        }
        if (!InputValidation.isDigitsOnly(fields.contact.trim())) {
            showError("Contact number must contain numbers only.");
            return;
        }
        if (LaundryData.customerIdExists(fields.customerId.trim())) {
            showError("That customer ID already exists.");
            return;
        }
        LaundryData.addCustomer(new Customer(fields.customerId.trim(), fields.name.trim(),
            fields.contact.trim(), fields.address.trim()));
        refreshCustomers();
        refreshVisiblePages();
        showSuccess("Customer added successfully.");
    }

    /** Validates and applies edits to the selected customer. */
    private void editCustomer(JTable table) {
        Customer customer = selectedCustomer(table);
        if (customer == null) {
            return;
        }
        CustomerFields fields = showCustomerDialog(customer);
        if (fields == null) {
            return;
        }
        if (fields.name.trim().isEmpty() || fields.contact.trim().isEmpty()
                || fields.address.trim().isEmpty()) {
            showError("Customer name, contact number, and address are required.");
            return;
        }
        if (!InputValidation.isLettersOnly(fields.name.trim())) {
            showError("Customer name may contain letters, spaces, apostrophes, hyphens, and periods only.");
            return;
        }
        if (!InputValidation.isDigitsOnly(fields.contact.trim())) {
            showError("Contact number must contain numbers only.");
            return;
        }
        customer.setName(fields.name.trim());
        customer.setContactNumber(fields.contact.trim());
        customer.setAddress(fields.address.trim());
        refreshCustomers();
        refreshVisiblePages();
        showSuccess("Customer updated successfully.");
    }

    /** Confirms the and deletes the selected customer when order history permits. */
    private void deleteCustomer(JTable table) {
        Customer customer = selectedCustomer(table);
        if (customer == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
            "Delete " + customer.getName() + "?", "Confirm Customer Deletion",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            LaundryData.deleteCustomer(customer);
            refreshCustomers();
            refreshVisiblePages();
            showSuccess("Customer deleted successfully.");
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    /** Resolves the selected table row to its customer record. */
    private Customer selectedCustomer(JTable table) {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            showError("Select a customer first.");
            return null;
        }
        String id = String.valueOf(table.getModel().getValueAt(
            table.convertRowIndexToModel(selected), 0));
        for (Customer customer : LaundryData.getCustomers()) {
            if (customer.getCustomerId().equals(id)) {
                return customer;
            }
        }
        showError("The selected customer could not be found.");
        return null;
    }

    /** Collects the customer details for creating a customer or editing an existing one. */
    private CustomerFields showCustomerDialog(Customer customer) {
        JTextField id = new JTextField(customer == null ? LaundryData.createCustomerId()
            : customer.getCustomerId(), 18);
        JTextField name = new JTextField(customer == null ? "" : customer.getName(), 18);
        JTextField contact = new JTextField(customer == null ? "" : customer.getContactNumber(), 18);
        JTextField address = new JTextField(customer == null ? "" : customer.getAddress(), 18);
        InputValidation.lettersOnly(name);
        InputValidation.digitsOnly(contact);
        id.setEditable(customer == null);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Customer ID:"));
        form.add(id);
        form.add(new JLabel("Customer Name:"));
        form.add(name);
        form.add(new JLabel("Contact Number:"));
        form.add(contact);
        form.add(new JLabel("Address:"));
        form.add(address);
        int result = JOptionPane.showConfirmDialog(this, form,
            customer == null ? "Add Customer" : "Edit Customer",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return result == JOptionPane.OK_OPTION
            ? new CustomerFields(id.getText(), name.getText(), contact.getText(), address.getText())
            : null;
    }

    // New-order form usi the live prici and generated order identifiers.
    /** Builds the order form, includi compact supply-use and live price inputs. */
    private JPanel createNewOrderPage() {
        JPanel page = pagePanel();
        page.add(pageHeading("New Laundry", "Create a laundry for an existing customer."), BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(0, 2, 12, 12));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 230, 232)),
            BorderFactory.createEmptyBorder(24, 26, 24, 26)));
        JTextField orderId = new JTextField(LaundryData.previewOrderId());
        orderId.setEditable(false);
        JComboBox<Customer> customer = new JComboBox<Customer>();
        JComboBox<String> serviceChoice =
            new JComboBox<String>(ServiceCatalog.getActiveServiceNames().toArray(new String[0]));
        JTextField quantity = new JTextField("1");
        JSpinner discount = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 1.0));
        JTextField received = new JTextField(LocalDate.now().toString());
        JComboBox<String> paymentStatus = new JComboBox<String>(new String[] {"Unpaid", "Paid"});
        InputValidation.decimalOnly(quantity);
        InputValidation.dateCharactersOnly(received);
        SupplyUsageInput supplyUsageInput = createSupplyUsageInput(null, null);
        JLabel originalAmount = new JLabel();
        originalAmount.setFont(BODY);
        originalAmount.setForeground(MUTED);
        JLabel amount = new JLabel();
        amount.setFont(new Font("Segoe UI", Font.BOLD, 16));
        amount.setForeground(DARK_CYAN);
        for (Customer record : LaundryData.getCustomers()) {
            customer.addItem(record);
        }
        form.add(new JLabel("Laundry ID:"));
        form.add(orderId);
        form.add(new JLabel("Customer:"));
        form.add(customer);
        form.add(new JLabel("Service Type:"));
        form.add(serviceChoice);
        form.add(new JLabel("Weight:"));
        form.add(quantity);
        form.add(new JLabel("Discount (%):"));
        form.add(discount);
        form.add(new JLabel("Date Received (YYYY-MM-DD):"));
        form.add(received);
        form.add(new JLabel("Payment received:"));
        form.add(paymentStatus);
        form.add(new JLabel("Original Price (service + supplies):"));
        form.add(originalAmount);
        form.add(new JLabel("Discounted Price:"));
        form.add(amount);
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        JScrollPane supplyUsageScroll = new JScrollPane(supplyUsageInput.table);
        supplyUsageScroll.setBorder(BorderFactory.createTitledBorder(
            "Supplies used - enter amounts in the Used column"));
        supplyUsageScroll.setPreferredSize(new Dimension(650, 150));
        center.add(supplyUsageScroll, BorderLayout.CENTER);
        page.add(center, BorderLayout.CENTER);
        JPanel buttons = actionBar();
        JButton save = actionButton("Save Laundry");
        JButton clear = actionButton("Clear Form");
        JButton cancel = actionButton("Cancel");
        buttons.add(save);
        buttons.add(clear);
        buttons.add(cancel);
        page.add(buttons, BorderLayout.SOUTH);
        page.putClientProperty("orderCustomer", customer);
        page.putClientProperty("orderService", serviceChoice);
        page.putClientProperty("supplyUsageInput", supplyUsageInput);

        Runnable updateAmount = () -> {
            try {
                double value = Double.parseDouble(quantity.getText().trim());
                if (serviceChoice.getSelectedItem() == null) {
                    originalAmount.setText("No active services available");
                    amount.setText("No active services available");
                    return;
                }
                if (value <= 0) {
                    originalAmount.setText("Enter a positive quantity");
                    amount.setText("Enter a positive quantity");
                    return;
                }
                double original = value * LaundryData.getServiceRate(
                    String.valueOf(serviceChoice.getSelectedItem()))
                    + supplyUsageInput.getUsageCost();
                if (!Double.isFinite(original)) {
                    originalAmount.setText("Amount is too large");
                    amount.setText("Amount is too large");
                    return;
                }
                double percentage = ((Number) discount.getValue()).doubleValue();
                originalAmount.setText("<html><strike>" + money(original) + "</strike></html>");
                amount.setText(money(discountedAmount(original, percentage)));
            } catch (NumberFormatException exception) {
                originalAmount.setText("Enter valid quantity and supply amounts");
                amount.setText("Enter valid quantity and supply amounts");
            }
        };
        quantity.getDocument().addDocumentListener(new SimpleDocumentListener(updateAmount));
        serviceChoice.addActionListener(event -> updateAmount.run());
        discount.addChangeListener(event -> updateAmount.run());
        supplyUsageInput.model.addTableModelListener(event -> {
            if (event.getColumn() == 3
                    || event.getColumn() == javax.swing.event.TableModelEvent.ALL_COLUMNS) {
                updateAmount.run();
            }
        });
        updateAmount.run();
        save.addActionListener(event -> saveOrder(orderId, customer, serviceChoice, quantity,
            discount, received, paymentStatus, supplyUsageInput));
        clear.addActionListener(event -> {
            quantity.setText("1");
            received.setText(LocalDate.now().toString());
            if (customer.getItemCount() > 0) {
                customer.setSelectedIndex(0);
            }
            if (serviceChoice.getItemCount() > 0) {
                serviceChoice.setSelectedIndex(0);
            }
            discount.setValue(0.0);
            paymentStatus.setSelectedItem("Unpaid");
            orderId.setText(LaundryData.previewOrderId());
            supplyUsageInput.clear();
            updateAmount.run();
        });
        cancel.addActionListener(event -> showPage("Dashboard"));
        return page;
    }

    /** Refreshes the new-order customer selector habthe preservi its selection. */
    private void refreshNewOrderCustomers() {
        JPanel page = (JPanel) content.getComponent(2);
        @SuppressWarnings("unchecked")
        JComboBox<Customer> customerChoice =
            (JComboBox<Customer>) page.getClientProperty("orderCustomer");
        Customer selectedCustomer = (Customer) customerChoice.getSelectedItem();
        String selectedId = selectedCustomer == null ? null : selectedCustomer.getCustomerId();
        customerChoice.removeAllItems();
        int selectedIndex = -1;
        for (Customer customer : LaundryData.getCustomers()) {
            customerChoice.addItem(customer);
            if (customer.getCustomerId().equals(selectedId)) {
                selectedIndex = customerChoice.getItemCount() - 1;
            }
        }
        if (selectedIndex >= 0) {
            customerChoice.setSelectedIndex(selectedIndex);
        }
    }

    /** Validates an order, deducts its supplies, and stores the new record. */
    private void saveOrder(JTextField orderId, JComboBox<Customer> customer,
            JComboBox<String> service, JTextField quantity, JSpinner discount,
            JTextField received, JComboBox<String> paymentStatus,
            SupplyUsageInput supplyUsageInput) {
        Customer selectedCustomer = (Customer) customer.getSelectedItem();
        if (selectedCustomer == null) {
            showError("Add a customer before creating a laundry.");
            return;
        }
        if (!InputValidation.isDecimal(quantity.getText().trim())) {
            showError("Quantity or weight must contain numbers only and may use one decimal point.");
            return;
        }
        if (!InputValidation.isValidDate(received.getText().trim())) {
            showError("Enter the date received in YYYY-MM-DD format usi numbers only.");
            return;
        }
        try {
            double quantityValue = Double.parseDouble(quantity.getText().trim());
            if (!Double.isFinite(quantityValue) || quantityValue <= 0) {
                throw new IllegalArgumentException("Quantity or weight must be greater than zero.");
            }
            LocalDate receivedDate = LocalDate.parse(received.getText().trim());
            LocalDate dueDate = receivedDate.plusDays(2);
            String serviceType = String.valueOf(service.getSelectedItem());
            Map<String, Double> supplyUsage = readSupplyUsage(supplyUsageInput);
            double serviceAmount = quantityValue * LaundryData.getServiceRate(serviceType);
            double supplyAmount = InventoryData.getUsageCost(supplyUsage);
            double originalAmount = serviceAmount + supplyAmount;
            if (!Double.isFinite(originalAmount)) {
                throw new IllegalArgumentException("The calculated laundry amount is too large.");
            }
            double discountPercentage = ((Number) discount.getValue()).doubleValue();
            double total = discountedAmount(originalAmount, discountPercentage);
            InventoryData.consume(supplyUsage);
            String savedOrderId = LaundryData.createOrderId();
            orderId.setText(savedOrderId);
            LaundryOrder order = new LaundryOrder(savedOrderId, selectedCustomer.getCustomerId(),
                serviceType, quantityValue, receivedDate, dueDate, originalAmount, "In Queue");
            order.setPriceBreakdown(serviceAmount, supplyAmount);
            order.setDiscountPercentage(discountPercentage);
            order.setSupplyUsage(supplyUsage);
            LaundryData.getOrders().add(order);
            if ("Paid".equals(paymentStatus.getSelectedItem())) {
                LaundryData.recordPayment(order, total);
            }
            orderId.setText(LaundryData.previewOrderId());
            supplyUsageInput.clear();
            refreshVisiblePages();
            showPage("Laundries");
            showOrderReceipt(order);
        } catch (DateTimeParseException exception) {
            showError("Enter dates in YYYY-MM-DD format.");
        } catch (NumberFormatException exception) {
            showError("Enter a valid positive quantity or weight.");
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    /** Displays the receipt for an order immediately after it has been saved. */
    private void showOrderReceipt(LaundryOrder order) {
        Customer customer = LaundryData.findCustomer(order.getCustomerId());
        double amountPaid = LaundryData.getAmountPaid(order.getOrderId());
        double discountAmount = order.getOriginalAmount() - order.getTotalAmount();
        String supplies = supplyUsageText(order.getSupplyUsage());
        String receipt = "LAVADA LAUNDRY SHOP\n"
            + "                 LAUNDRY RECEIPT\n"
            + "------------------------------------------\n"
            + "Receipt / Laundry No.: " + order.getOrderId() + "\n"
            + "Date issued:        " + LocalDate.now() + "\n"
            + "Customer:           "
            + (customer == null ? "(customer unavailable)" : customer.getName()) + "\n"
            + "------------------------------------------\n"
            + "Service:            " + order.getServiceType() + "\n"
            + "Quantity / Weight:  " + quantityText(order) + "\n"
            + "Date received:      " + order.getDateReceived() + "\n"
            + "Due date:           " + order.getDueDate() + "\n"
            + "Supplies used:      " + (supplies.isEmpty() ? "None" : supplies) + "\n"
            + "------------------------------------------\n"
            + "Original price:     " + money(order.getOriginalAmount()) + "\n"
            + "Discount (" + formatDiscount(order.getDiscountPercentage()) + "):   "
            + money(discountAmount) + "\n"
            + "Total:              " + money(order.getTotalAmount()) + "\n"
            + "Amount paid:        " + money(amountPaid) + "\n"
            + "Balance:            " + money(Math.max(0, order.getTotalAmount() - amountPaid)) + "\n"
            + "Payment status:     " + LaundryData.getPaymentStatus(order) + "\n"
            + "Laundry status:       " + order.getLaundryStatus() + "\n"
            + "------------------------------------------\n"
            + "       Thank you for choosi Lavada!";
        JTextArea receiptText = new JTextArea(receipt, 20, 43);
        receiptText.setEditable(false);
        receiptText.setLineWrap(true);
        receiptText.setWrapStyleWord(true);
        receiptText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        receiptText.setBackground(Color.WHITE);
        receiptText.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JScrollPane receiptScroll = new JScrollPane(receiptText);
        receiptScroll.setBorder(BorderFactory.createLineBorder(BORDER));
        JOptionPane.showMessageDialog(this, receiptScroll, "Laundry Receipt",
            JOptionPane.INFORMATION_MESSAGE);
    }

    /** Calculates the final price after discount and rounds it to the nearest cent. */
    private double discountedAmount(double originalAmount, double percentage) {
        return Math.round(originalAmount * (100.0 - percentage)) / 100.0;
    }

    /** Formats the discount percentage for order tables and detail views. */
    private static String formatDiscount(double percentage) {
        return String.format("%.0f%%", percentage);
    }

    // Order listing, search, details, editing, and laundry-status changes.
    /** Builds the order table, search field, and order-management actions. */
    private JPanel createOrdersPage() {
        JPanel page = pagePanel();
        JTextField search = new JTextField(22);
        DefaultTableModel orderModel = new DefaultTableModel(new String[] {
            "Laundry ID", "Customer Name", "Service", "Quantity / Weight", "Date Received",
            "Original Price", "Discount", "Discounted Total", "Payment Status", "Laundry Status",
            "Added By"
        }, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(orderModel) {
            private static final long serialVersionUID = 1L;

            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,
                    int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                int originColumn = convertColumnIndexToModel(
                    getColumnModel().getColumnIndex("Added By"));
                int modelRow = convertRowIndexToModel(row);
                boolean addedByAdmin = "ADMIN".equals(
                    getModel().getValueAt(modelRow, originColumn));
                component.setFont(getFont().deriveFont(
                    addedByAdmin ? Font.BOLD : Font.PLAIN));
                component.setForeground(TEXT);
                component.setBackground(isRowSelected(row)
                    ? getSelectionBackground() : getBackground());
                return component;
            }
        };
        table.setRowHeight(34);
        table.setFont(BODY);
        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(224, 243, 241));
        table.setSelectionForeground(TEXT);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        javax.swing.table.TableRowSorter<DefaultTableModel> orderSorter =
            new javax.swing.table.TableRowSorter<DefaultTableModel>(
                (DefaultTableModel) table.getModel());
        table.setRowSorter(orderSorter);
        tableSorters.put(table, orderSorter);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setForeground(new Color(91, 105, 122));
        table.getTableHeader().setBackground(new Color(246, 248, 251));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.setFillsViewportHeight(true);
        JButton find = actionButton("Search");
        find.addActionListener(event -> filterTable(table, search.getText()));
        JPanel actions = actionBar();
        JButton details = actionButton("View Details");
        JButton edit = actionButton("Edit Laundry");
        JButton status = actionButton("Update Status");
        JButton cancel = actionButton("Cancel Laundry");
        JButton delete = actionButton("Delete Laundry");
        JButton deleteAll = actionButton("Delete All Laundries");
        details.addActionListener(event -> showOrderDetails(table));
        edit.addActionListener(event -> editOrder(table));
        status.addActionListener(event -> updateOrderStatus(table));
        cancel.addActionListener(event -> cancelOrder(table));
        delete.addActionListener(event -> deleteOrder(table));
        deleteAll.addActionListener(event -> deleteAllOrders());
        actions.add(details);
        actions.add(edit);
        actions.add(status);
        actions.add(cancel);
        actions.add(delete);
        actions.add(deleteAll);
        page.add(buildTablePageHeader("Laundries",
            "Admin-added laundries appear in bold text.", search, find), BorderLayout.NORTH);
        page.add(tablePanel(table, actions), BorderLayout.CENTER);
        page.putClientProperty("ordersTable", table);
        page.putClientProperty("orderSearch", search);
        return page;
    }

    /** Rebuilds the order table using the latest order and payment statuses. */
    private void refreshOrders() {
        JPanel page = (JPanel) content.getComponent(3);
        JTable table = (JTable) page.getClientProperty("ordersTable");
        JTextField search = (JTextField) page.getClientProperty("orderSearch");
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (LaundryOrder order : LaundryData.getOrders()) {
            Customer customer = LaundryData.findCustomer(order.getCustomerId());
            model.addRow(new Object[] {
                order.getOrderId(), customer == null ? "(customer deleted)" : customer.getName(),
                order.getServiceType(), quantityText(order), order.getDateReceived(),
                money(order.getOriginalAmount()), formatDiscount(order.getDiscountPercentage()),
                money(order.getTotalAmount()), LaundryData.getPaymentStatus(order), order.getLaundryStatus(),
                order.isAddedByAdmin() ? "ADMIN" : "STAFF"
            });
        }
        filterTable(table, search.getText());
    }

    /** Deletes the selected laundry and its linked payment history without cancelling it. */
    private void deleteOrder(JTable table) {
        LaundryOrder order = selectedOrder(table);
        if (order == null) {
            return;
        }
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete laundry " + order.getOrderId() + "?\n"
                + "Its linked payment records will also be deleted. Supplies recorded for "
                + "In Queue laundries will be returned to inventory; supplies already used "
                + "will remain deducted from stock.",
            "Confirm laundry deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            LaundryData.deleteOrder(order);
            refreshVisiblePages();
            showSuccess("Laundry " + order.getOrderId() + " deleted successfully.");
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    private void cancelOrder(JTable table) {
        LaundryOrder order = selectedOrder(table);
        if (order == null) {
            return;
        }
        String status = order.getLaundryStatus();
        if (!"In Queue".equals(status) && !"Pending".equals(status)) {
            showError("Only laundries In Queue or Pending can be cancelled.");
            return;
        }
        double refund = getOrderCancellationRefund(order.getOrderId());
        String message = "Cancel laundry " + order.getOrderId() + "?";
        if ("In Queue".equals(status)) {
            message += "\nSupplies have not been used and will be returned to inventory.";
        } else {
            message += "\nSupplies have been used and will remain charged; only the service "
                + "portion can be refunded.";
        }
        if (refund > 0.00001) {
            message += "\nRefund to record: " + money(refund);
        } else {
            message += "\nNo refund is due based on the amount collected and refundable charges.";
        }
        int result = JOptionPane.showConfirmDialog(this, message, "Confirm Laundry Cancellation",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            cancelOrder(order.getOrderId());
            refreshVisiblePages();
            showSuccess(refund > 0.00001
                ? "Laundry cancelled and refund recorded."
                : "Laundry cancelled; no refund was due.");
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    /** Confirms and removes every laundry and its linked payment records. */
    private void deleteAllOrders() {
        int orderCount = LaundryData.getOrders().size();
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
            LaundryData.deleteAllOrders();
            refreshVisiblePages();
            showSuccess(orderCount + " laundry(s) deleted.");
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    /** Resolves the selected table row to its laundry order. */
    private LaundryOrder selectedOrder(JTable table) {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            showError("Select a laundry first.");
            return null;
        }
        String id = String.valueOf(table.getModel().getValueAt(
            table.convertRowIndexToModel(selected), 0));
        LaundryOrder order = LaundryData.findOrder(id);
        if (order == null) {
            showError("The selected laundry could not be found.");
        }
        return order;
    }

    /** Displays the details and recorded supply usage for the selected order. */
    private void showOrderDetails(JTable table) {
        LaundryOrder order = selectedOrder(table);
        if (order == null) {
            return;
        }
        Customer customer = LaundryData.findCustomer(order.getCustomerId());
        double paid = LaundryData.getAmountPaid(order.getOrderId());
        String details = "Laundry ID: " + order.getOrderId()
            + "\nCustomer: " + (customer == null ? "(customer deleted)" : customer.getName())
            + "\nService: " + order.getServiceType()
            + "\nQuantity / Weight: " + quantityText(order)
            + "\nSupplies used: " + supplyUsageText(order.getSupplyUsage())
            + "\nDate Received: " + order.getDateReceived()
            + "\nDue Date: " + order.getDueDate()
            + "\nOriginal Price: " + money(order.getOriginalAmount())
            + "\nDiscount: " + formatDiscount(order.getDiscountPercentage())
            + "\nDiscounted Price: " + money(order.getTotalAmount())
            + "\nAmount Paid: " + money(paid)
            + "\nBalance: " + money(Math.max(0, order.getTotalAmount() - paid))
            + "\nPayment Status: " + LaundryData.getPaymentStatus(order)
            + "\nLaundry Status: " + order.getLaundryStatus();
        if (LaundryData.getRefundedAmount(order.getOrderId()) > 0.00001) {
            details += "\nRefunded: " + money(LaundryData.getRefundedAmount(order.getOrderId()));
        }
        JOptionPane.showMessageDialog(this, details, "Laundry Details", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Edits the order details and adjusts inventory for corrected supply usage. */
    private void editOrder(JTable table) {
        LaundryOrder order = selectedOrder(table);
        if (order == null) {
            return;
        }
        if ("Cancelled".equals(order.getLaundryStatus())) {
            showError("Cancelled laundries cannot be edited.");
            return;
        }
        ArrayList<String> editableServices =
            new ArrayList<String>(ServiceCatalog.getActiveServiceNames());
        if (!editableServices.contains(order.getServiceType())) {
            editableServices.add(order.getServiceType());
        }
        JComboBox<String> service = new JComboBox<String>(
            editableServices.toArray(new String[0]));
        service.setSelectedItem(order.getServiceType());
        JSpinner quantity = new JSpinner(new SpinnerNumberModel(order.getQuantity(), 0.1, 10000.0, 0.5));
        JSpinner discount = new JSpinner(new SpinnerNumberModel(
            order.getDiscountPercentage(), 0.0, 100.0, 1.0));
        JLabel originalPrice = new JLabel();
        originalPrice.setForeground(MUTED);
        JLabel discountedPrice = new JLabel();
        discountedPrice.setFont(new Font("Segoe UI", Font.BOLD, 14));
        discountedPrice.setForeground(DARK_CYAN);
        JComboBox<String> paymentStatus =
            new JComboBox<String>(new String[] {"Unpaid", "Paid"});
        paymentStatus.setSelectedItem(LaundryData.getPaymentStatus(order));
        JPanel orderFields = new JPanel(new GridLayout(0, 2, 8, 8));
        orderFields.add(new JLabel("Service:"));
        orderFields.add(service);
        orderFields.add(new JLabel("Quantity / Weight:"));
        orderFields.add(quantity);
        orderFields.add(new JLabel("Discount (%):"));
        orderFields.add(discount);
        orderFields.add(new JLabel("Original Price (service + supplies):"));
        orderFields.add(originalPrice);
        orderFields.add(new JLabel("Discounted Price:"));
        orderFields.add(discountedPrice);
        orderFields.add(new JLabel("Payment status:"));
        orderFields.add(paymentStatus);
        SupplyUsageInput supplyUsageInput =
            createSupplyUsageInput(order.getSupplyUsage(), order.getSupplyUsage());
        JScrollPane supplyUsageScroll = new JScrollPane(supplyUsageInput.table);
        supplyUsageScroll.setBorder(BorderFactory.createTitledBorder(
            "Supplies used - enter amounts in the Used column"));
        supplyUsageScroll.setPreferredSize(new Dimension(500, 150));
        JPanel form = new JPanel(new BorderLayout(0, 10));
        form.add(orderFields, BorderLayout.NORTH);
        form.add(supplyUsageScroll, BorderLayout.CENTER);
        Runnable updateEditPrice = () -> {
            try {
                double original = ((Number) quantity.getValue()).doubleValue()
                    * LaundryData.getServiceRate(String.valueOf(service.getSelectedItem()))
                    + supplyUsageInput.getUsageCost();
                double percentage = ((Number) discount.getValue()).doubleValue();
                originalPrice.setText("<html><strike>" + money(original) + "</strike></html>");
                discountedPrice.setText(money(discountedAmount(original, percentage)));
            } catch (NumberFormatException exception) {
                originalPrice.setText("Enter valid supply quantities");
                discountedPrice.setText("Enter valid supply quantities");
            }
        };
        quantity.addChangeListener(event -> updateEditPrice.run());
        service.addActionListener(event -> updateEditPrice.run());
        discount.addChangeListener(event -> updateEditPrice.run());
        supplyUsageInput.model.addTableModelListener(event -> {
            if (event.getColumn() == 3
                    || event.getColumn() == javax.swing.event.TableModelEvent.ALL_COLUMNS) {
                updateEditPrice.run();
            }
        });
        updateEditPrice.run();
        int result = JOptionPane.showConfirmDialog(this, form, "Edit " + order.getOrderId(),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            Map<String, Double> supplyUsage = readSupplyUsage(supplyUsageInput);
            double originalAmount = ((Number) quantity.getValue()).doubleValue()
                * LaundryData.getServiceRate(String.valueOf(service.getSelectedItem()))
                + InventoryData.getUsageCost(supplyUsage);
            double discountPercentage = ((Number) discount.getValue()).doubleValue();
            double amount = discountedAmount(originalAmount, discountPercentage);
            double amountPaid = LaundryData.getAmountPaid(order.getOrderId());
            if (amount + 0.00001 < amountPaid) {
                showError("The updated laundry total cannot be less than the amount already paid.");
                return;
            }
            InventoryData.updateUsage(order.getSupplyUsage(), supplyUsage);
            double serviceAmount = ((Number) quantity.getValue()).doubleValue()
                * LaundryData.getServiceRate(String.valueOf(service.getSelectedItem()));
            double supplyAmount = InventoryData.getUsageCost(supplyUsage);
            order.setServiceType(String.valueOf(service.getSelectedItem()));
            order.setQuantity(((Number) quantity.getValue()).doubleValue());
            order.setTotalAmount(originalAmount);
            order.setPriceBreakdown(serviceAmount, supplyAmount);
            order.setDiscountPercentage(discountPercentage);
            order.setSupplyUsage(supplyUsage);
            if ("Paid".equals(paymentStatus.getSelectedItem())
                    && amountPaid + 0.00001 < amount) {
                LaundryData.recordPayment(order, amount - amountPaid);
            }
            refreshVisiblePages();
            showSuccess("Laundry updated successfully.");
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    /** Builds the compact supply table with current availability and used amounts. */
    private SupplyUsageInput createSupplyUsageInput(Map<String, Double> currentUsage,
            Map<String, Double> reservedUsage) {
        SupplyUsageInput input = new SupplyUsageInput();
        for (InventoryData.Item item : InventoryData.getItems()) {
            double used = currentUsage == null ? 0 : currentUsage.getOrDefault(item.getId(), 0.0);
            double available = item.getStock()
                + (reservedUsage == null ? 0 : reservedUsage.getOrDefault(item.getId(), 0.0));
            input.addItem(item, available, used);
        }
        return input;
    }

    /** Updates the available-stock values shown on the new-order supply table. */
    private void refreshSupplyAvailability() {
        JPanel page = (JPanel) content.getComponent(2);
        SupplyUsageInput input = (SupplyUsageInput) page.getClientProperty("supplyUsageInput");
        input.refreshAvailability();
    }

    /** Validates and collects supply quantities entered in the compact table. */
    private Map<String, Double> readSupplyUsage(SupplyUsageInput input) {
        if (input.table.isEditing() && !input.table.getCellEditor().stopCellEditing()) {
            throw new IllegalArgumentException("Finish enteri the supply quantity before saving.");
        }
        Map<String, Double> usage = new LinkedHashMap<String, Double>();
        for (int row = 0; row < input.itemIds.size(); row++) {
            String itemId = input.itemIds.get(row);
            String value = String.valueOf(input.model.getValueAt(row, 3)).trim();
            if (value.isEmpty()) {
                value = "0";
            }
            if (!InputValidation.isDecimal(value)) {
                InventoryData.Item item = InventoryData.findItem(itemId);
                throw new IllegalArgumentException("Enter a valid quantity for "
                    + (item == null ? "the supply" : item.getName()) + ".");
            }
            double amount = Double.parseDouble(value);
            if (!Double.isFinite(amount) || amount < 0) {
                InventoryData.Item item = InventoryData.findItem(itemId);
                throw new IllegalArgumentException("Quantity for "
                    + (item == null ? "the supply" : item.getName())
                    + " must be a non-negative number.");
            }
            usage.put(itemId, amount);
        }
        return usage;
    }

    /** Formats the nonzero supply usage for display in order details. */
    private String supplyUsageText(Map<String, Double> usage) {
        ArrayList<String> amounts = new ArrayList<String>();
        for (Map.Entry<String, Double> entry : usage.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            InventoryData.Item item = InventoryData.findItem(entry.getKey());
            if (item != null) {
                amounts.add(item.getName() + ": " + formatInventoryAmount(entry.getValue())
                    + " " + item.getUnit());
            }
        }
        return amounts.isEmpty() ? "None recorded" : String.join(", ", amounts);
    }

    /** Formats the whole supply quantities without traili decimal places. */
    private String formatInventoryAmount(double amount) {
        if (amount == Math.rint(amount)) {
            return String.format("%.0f", amount);
        }
        return String.format("%.2f", amount);
    }

    /** Updates the selected order's laundry-processi status. */
    private void updateOrderStatus(JTable table) {
        LaundryOrder order = selectedOrder(table);
        if (order == null) {
            return;
        }
        if ("Cancelled".equals(order.getLaundryStatus())) {
            showError("Cancelled laundries cannot have their status changed.");
            return;
        }
        JComboBox<String> status = new JComboBox<String>(LAUNDRY_STATUSES);
        status.setSelectedItem(order.getLaundryStatus());
        int result = JOptionPane.showConfirmDialog(this, status,
            "Update Laundry Status - " + order.getOrderId(),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String selectedStatus = String.valueOf(status.getSelectedItem());
            if ("Completed".equals(order.getLaundryStatus())
                    && !"Completed".equals(selectedStatus)) {
                showError("Completed laundries cannot be moved back to another status.");
                return;
            }
            if ("Completed".equals(selectedStatus)
                    && LaundryData.getAmountPaid(order.getOrderId()) + 0.00001
                        < order.getTotalAmount()) {
                showError("a laundry must be fully paid before it can be marked completed.");
                return;
            }
            order.setLaundryStatus(selectedStatus);
            refreshVisiblePages();
            showSuccess("Laundry status updated.");
        }
    }

    // Pickup queue ay naglalaman lthe contains orders currently ready to be released.
    /** Builds the pickup queue, empty-state notice, and release action. */
    private JPanel createPickupPage() {
        JPanel page = pagePanel();
        JTextField search = new JTextField(22);
        JTable table = createTable(new String[] {
            "Laundry ID", "Customer Name", "Contact Number", "Total Amount",
            "Payment Status", "Laundry Status"
        });
        JButton find = actionButton("Search");
        find.addActionListener(event -> filterTable(table, search.getText()));
        search.addActionListener(event -> filterTable(table, search.getText()));
        page.add(buildTablePageHeader("Pick Up / Release",
            "Release laundries only when they are ready and fully paid.", search, find),
            BorderLayout.NORTH);
        JPanel actions = actionBar();
        JButton release = actionButton("Release Laundry");
        release.addActionListener(event -> releaseOrder(table));
        actions.add(release);
        JPanel pickupTablePanel = tablePanel(table, actions);
        JLabel emptyNotice = new JLabel("No laundries are currently ready for pickup.",
            SwingConstants.CENTER);
        emptyNotice.setFont(new Font("Segoe UI", Font.BOLD, 15));
        emptyNotice.setForeground(MUTED);
        emptyNotice.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        pickupTablePanel.add(emptyNotice, BorderLayout.SOUTH);
        page.add(pickupTablePanel, BorderLayout.CENTER);
        page.putClientProperty("pickupTable", table);
        page.putClientProperty("pickupSearch", search);
        page.putClientProperty("pickupEmptyNotice", emptyNotice);
        return page;
    }

    /** Shows the only ready orders and updates the queue's empty-state notice. */
    private void refreshPickup() {
        JPanel page = (JPanel) content.getComponent(4);
        JTable table = (JTable) page.getClientProperty("pickupTable");
        JTextField search = (JTextField) page.getClientProperty("pickupSearch");
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        ArrayList<LaundryOrder> readyOrders = new ArrayList<LaundryOrder>();
        for (LaundryOrder order : LaundryData.getOrders()) {
            if ("Ready to Pick Up".equals(order.getLaundryStatus())) {
                readyOrders.add(order);
            }
        }
        LaundryData.sortPickupOrdersByReceivedDate(readyOrders);
        // DSA: QUEUE - ready laundries are released from this FIFO queue.
        Queue<LaundryOrder> pickupQueue = new ArrayDeque<LaundryOrder>();
        pickupQueue.addAll(readyOrders);
        while (!pickupQueue.isEmpty()) {
            LaundryOrder order = pickupQueue.remove();
            Customer customer = LaundryData.findCustomer(order.getCustomerId());
            model.addRow(new Object[] {
                order.getOrderId(), customer == null ? "(customer deleted)" : customer.getName(),
                customer == null ? "" : customer.getContactNumber(), money(order.getTotalAmount()),
                LaundryData.getPaymentStatus(order), order.getLaundryStatus()
            });
        }
        filterTable(table, search.getText());
        JLabel emptyNotice = (JLabel) page.getClientProperty("pickupEmptyNotice");
        emptyNotice.setVisible(model.getRowCount() == 0);
    }

    /** Releases a ready, fully paid order and marks it completed. */
    private void releaseOrder(JTable table) {
        LaundryOrder order = selectedOrder(table);
        if (order == null) {
            return;
        }
        if (!"Ready to Pick Up".equals(order.getLaundryStatus())) {
            showError("Only laundries marked Ready to Pick Up can be released.");
            return;
        }
        double balance = order.getTotalAmount() - LaundryData.getAmountPaid(order.getOrderId());
        if (balance > 0.00001) {
            showError("This laundry must be fully paid before it can be released. Balance: " + money(balance));
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
            "Release laundry " + order.getOrderId() + " to the customer?",
            "Confirm Laundry Release", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            order.setLaundryStatus("Completed");
            refreshVisiblePages();
            showSuccess("Laundry released and laundry marked Completed.");
        }
    }

    /** Creates a page header with a search field for a data-table page. */
    private JPanel buildTablePageHeader(String title, String subtitle, JTextField search) {
        return buildTablePageHeader(title, subtitle, search, null);
    }

    private JPanel buildTablePageHeader(String title, String subtitle, JTextField search,
            JButton searchButton) {
        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setOpaque(false);
        header.add(pageHeading(title, subtitle), BorderLayout.CENTER);
        search.setFont(BODY);
        search.setToolTipText("Enter an ID or customer name");
        search.setPreferredSize(new Dimension(searchButton == null ? 230 : 160, 34));
        if (searchButton == null) {
            header.add(search, BorderLayout.EAST);
        } else {
            JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            searchControls.setOpaque(false);
            searchControls.add(search);
            searchControls.add(searchButton);
            header.add(searchControls, BorderLayout.EAST);
        }
        return header;
    }

    /** Wraps a data table and its actions in the dashboard's bordered white panel. */
    private JPanel tablePanel(JTable table, JPanel actions) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        if (actions != null) {
            actions.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            panel.add(actions, BorderLayout.NORTH);
        }
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    /** Creates a styled, sortable, read-only table with the supplied columns. */
    private JTable createTable(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;

            /** Keeps the table records read-only; edits go through explicit actions. */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(34);
        table.setFont(BODY);
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(224, 243, 241));
        table.setSelectionForeground(TEXT);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter =
            new javax.swing.table.TableRowSorter<DefaultTableModel>(model);
        table.setRowSorter(sorter);
        tableSorters.put(table, sorter);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setForeground(new Color(91, 105, 122));
        table.getTableHeader().setBackground(new Color(246, 248, 251));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.setFillsViewportHeight(true);
        return table;
    }

    /** Applies the literal, case-insensitive filter to a staff data table. */
    private void filterTable(JTable table, String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase();
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = tableSorters.get(table);
        if (normalized.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)"
                + java.util.regex.Pattern.quote(normalized)));
        }
    }

    /** Creates  standard content page with workspace colors and margins. */
    private JPanel pagePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(26, 30, 30, 30));
        return panel;
    }

    /** Creates a title and description block used by staff pages. */
    private JPanel pageHeading(String title, String subtitle) {
        JPanel heading = new JPanel(new GridLayout(0, 1, 0, 5));
        heading.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 23));
        titleLabel.setForeground(TEXT);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(BODY);
        subtitleLabel.setForeground(MUTED);
        heading.add(titleLabel);
        heading.add(subtitleLabel);
        return heading;
    }

    /** Creates  left-aligned container for page action buttons. */
    private JPanel actionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        bar.setOpaque(false);
        return bar;
    }

    /** Creates  consistently styled staff action button. */
    private JButton actionButton(String label) {
        JButton button = new JButton(label);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(CYAN);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        return button;
    }

    /** Refreshes all page data after a shared record changes. */
    private void refreshVisiblePages() {
        for (String page : MENU_ITEMS) {
            refreshPage(page);
        }
        refreshSupplyAvailability();
        content.revalidate();
        content.repaint();
    }

    /** Formats the order's quantity with the unit appropriate for its service. */
    private String quantityText(LaundryOrder order) {
        return String.format("%.2f %s", order.getQuantity(),
            LaundryData.getQuantityUnit(order.getServiceType()));
    }

    /** Formats the monetary amount using the application's peso display. */
    private String money(double amount) {
        return String.format("P%,.2f", amount);
    }

    /** Displays the staff-faci error dialog. */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Staff Dashboard",
            JOptionPane.ERROR_MESSAGE);
    }

    /** Displays the staff-faci success dialog. */
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Staff Dashboard",
            JOptionPane.INFORMATION_MESSAGE);
    }

    /** Confirms the logout, clears staff identity, and returns to the login screen. */
    private void logout() {
        int answer = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to exit the staff dashboard?", "Confirm logout",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            System.clearProperty("laundry.staff.username");
            System.clearProperty("laundry.staff.employeeId");
            new Login().setVisible(true);
            dispose();
        }
    }

    /** Opens the admin dashboard directly and clears the current staff identity. */
    private void switchToAdminForTesting() {
        System.clearProperty("laundry.staff.username");
        System.clearProperty("laundry.staff.employeeId");
        new AdminDashboard().showDashboard();
        dispose();
    }

    private final class SupplyUsageInput {
        private final ArrayList<String> itemIds = new ArrayList<String>();
        private final DefaultTableModel model = new DefaultTableModel(
                new Object[] {"Supply", "Available", "Cost per Unit", "Used"}, 0) {
            private static final long serialVersionUID = 1L;

            /** Allows the edits only in the column where staff enter supply usage. */
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };
        private final JTable table = new JTable(model);

        /** Configures the compact supply-usage table and its editable quantity column. */
        private SupplyUsageInput() {
            table.setFont(BODY);
            table.setRowHeight(26);
            table.setShowVerticalLines(false);
            table.setGridColor(BORDER);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
            table.getTableHeader().setForeground(new Color(91, 105, 122));
            table.getTableHeader().setBackground(new Color(246, 248, 251));
            table.getTableHeader().setPreferredSize(new Dimension(0, 28));
            table.setFillsViewportHeight(true);

            JTextField usedField = new JTextField();
            usedField.setHorizontalAlignment(SwingConstants.RIGHT);
            usedField.setToolTipText("Enter whole numbers for pack-based supplies");
            DefaultCellEditor usedEditor = new DefaultCellEditor(usedField) {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean stopCellEditing() {
                    int row = table.getEditingRow();
                    if (row >= 0 && row < itemIds.size()) {
                        InventoryData.Item item = InventoryData.findItem(itemIds.get(row));
                        String value = usedField.getText().trim();
                        if (item != null && InventoryData.usesWholeUnits(item.getId())
                                && !InputValidation.isDigitsOnly(value)) {
                            JOptionPane.showMessageDialog(usedField,
                                "This supply is measured in whole packs. Enter numbers only.",
                                "Invalid input", JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    }
                    return super.stopCellEditing();
                }
            };
            usedEditor.setClickCountToStart(1);
            table.getColumnModel().getColumn(3).setCellEditor(usedEditor);
            table.getColumnModel().getColumn(0).setPreferredWidth(230);
            table.getColumnModel().getColumn(1).setPreferredWidth(115);
            table.getColumnModel().getColumn(2).setPreferredWidth(115);
            table.getColumnModel().getColumn(3).setPreferredWidth(75);
        }

        /** Adds  inventory item and its available and used amounts to the table. */
        private void addItem(InventoryData.Item item, double available, double used) {
            itemIds.add(item.getId());
            model.addRow(new Object[] {
                item.getName() + " (" + item.getUnit() + ")",
                formatSupplyAmount(item, available) + " " + item.getUnit(),
                String.format("P%.2f / %s", item.getUnitCost(), item.getUnit()),
                formatUsedAmount(item, used)
            });
        }

        /** Totals the unit costs for the currently entered supply quantities. */
        private double getUsageCost() {
            double total = 0;
            for (int row = 0; row < itemIds.size(); row++) {
                String value = String.valueOf(model.getValueAt(row, 3)).trim();
                InventoryData.Item item = InventoryData.findItem(itemIds.get(row));
                if (!InputValidation.isDecimal(value)
                        || (item != null && InventoryData.usesWholeUnits(item.getId())
                            && !InputValidation.isDigitsOnly(value))) {
                    throw new NumberFormatException("Supply quantity is invalid or incomplete.");
                }
                double quantity = Double.parseDouble(value);
                if (item == null) {
                    throw new IllegalArgumentException(
                        "An inventory item used by this laundry is no longer available.");
                }
                total += quantity * item.getUnitCost();
                if (!Double.isFinite(total)) {
                    throw new IllegalArgumentException("The inventory usage cost is too large.");
                }
            }
            return total;
        }

        private String formatSupplyAmount(InventoryData.Item item, double amount) {
            return InventoryData.usesWholeUnits(item.getId())
                ? String.format("%.0f", amount) : formatInventoryAmount(amount);
        }

        private String formatUsedAmount(InventoryData.Item item, double amount) {
            return InventoryData.usesWholeUnits(item.getId()) ? String.format("%.0f", amount)
                : formatInventoryAmount(amount);
        }

        /** Refreshes the stock values and adds inventory items introduced since form creatingon. */
        private void refreshAvailability() {
            for (int row = itemIds.size() - 1; row >= 0; row--) {
                if (InventoryData.findItem(itemIds.get(row)) == null) {
                    itemIds.remove(row);
                    model.removeRow(row);
                }
            }
            for (InventoryData.Item item : InventoryData.getItems()) {
                int row = itemIds.indexOf(item.getId());
                if (row < 0) {
                    addItem(item, item.getStock(), 0);
                } else {
                    model.setValueAt(formatSupplyAmount(item, item.getStock()) + " " + item.getUnit(),
                        row, 1);
                    model.setValueAt(String.format("P%.2f / %s",
                        item.getUnitCost(), item.getUnit()), row, 2);
                    String used = String.valueOf(model.getValueAt(row, 3)).trim();
                    if (InputValidation.isDecimal(used)) {
                        model.setValueAt(formatUsedAmount(item, Double.parseDouble(used)), row, 3);
                    }
                }
            }
        }

        /** Resets all entered supply quantities to zero. */
        private void clear() {
            if (table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            for (int row = 0; row < model.getRowCount(); row++) {
                model.setValueAt("0", row, 3);
            }
        }
    }

    private static final class RoundedPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final int radius;
        private final Color fill;

        /** Initializes the background color and corner radius for this panel. */
        private RoundedPanel(int radius, Color fill) {
            this.radius = radius;
            this.fill = fill;
            setOpaque(false);
        }

        /** Paints the panel's rounded fill and border behind its child components. */
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

        /** Initializes the accent color and compact dimensions of the bar. */
        private AccentBar(Color accent) {
            this.accent = accent;
            setOpaque(false);
            setPreferredSize(new Dimension(0, 3));
        }

        /** Paints the colored accent strip used on dashboard cards. */
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(accent);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
            g.dispose();
        }
    }

    // In-memory models and shared data management.
    private static final class Customer {
        private final String customerId;
        private String name;
        private String contactNumber;
        private String address;

        /** Creates  customer record with its identifyi and contact details. */
        private Customer(String customerId, String name, String contactNumber, String address) {
            this.customerId = customerId;
            this.name = name;
            this.contactNumber = contactNumber;
            this.address = address;
        }

        /** Returns the customer's unique ID. */
        private String getCustomerId() {
            return customerId;
        }

        /** Returns the customer's current name. */
        private String getName() {
            return name;
        }

        /** Updates the customer's display name. */
        private void setName(String name) {
            this.name = name;
        }

        /** Returns the customer's contact number. */
        private String getContactNumber() {
            return contactNumber;
        }

        /** Updates the customer's contact number. */
        private void setContactNumber(String contactNumber) {
            this.contactNumber = contactNumber;
        }

        /** Returns the customer's address. */
        private String getAddress() {
            return address;
        }

        /** Updates the customer's address. */
        private void setAddress(String address) {
            this.address = address;
        }

        /** Formats the customer for display in selection controls. */
        @Override
        public String toString() {
            return name + " (" + customerId + ")";
        }
    }

    private static final class LaundryOrder {
        private final String orderId;
        private String customerId;
        private String serviceType;
        private double quantity;
        private LocalDate dateReceived;
        private LocalDate dueDate;
        private double originalAmount;
        private double serviceAmount;
        private double supplyAmount;
        private double discountPercentage;
        private String laundryStatus;
        private boolean addedByAdmin;
        private Map<String, Double> supplyUsage = new LinkedHashMap<String, Double>();

        /** Creates  order record with its service, schedule, price, and status. */
        private LaundryOrder(String orderId, String customerId, String serviceType, double quantity,
                LocalDate dateReceived, LocalDate dueDate, double originalAmount, String laundryStatus) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.serviceType = serviceType;
            this.quantity = quantity;
            this.dateReceived = dateReceived;
            this.dueDate = dueDate;
            this.originalAmount = originalAmount;
            this.laundryStatus = laundryStatus;
        }

        /** Returns the this order's unique ID. */
        private String getOrderId() {
            return orderId;
        }

        /** Returns the ID of the customer who owns this order. */
        private String getCustomerId() {
            return customerId;
        }

        private void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        /** Returns the service selected for this order. */
        private String getServiceType() {
            return serviceType;
        }

        /** Updates the service selected for this order. */
        private void setServiceType(String serviceType) {
            this.serviceType = serviceType;
        }

        /** Returns the order's weight or item quantity. */
        private double getQuantity() {
            return quantity;
        }

        /** Updates the order's weight or item quantity. */
        private void setQuantity(double quantity) {
            this.quantity = quantity;
        }

        /** Returns the date the laundry was received. */
        private LocalDate getDateReceived() {
            return dateReceived;
        }

        /** Updates the date the laundry was received. */
        private void setDateReceived(LocalDate dateReceived) {
            this.dateReceived = dateReceived;
        }

        /** Returns the order's promised due date. */
        private LocalDate getDueDate() {
            return dueDate;
        }

        /** Updates the order's promised due date. */
        private void setDueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
        }

        /** Returns the total charge for this order. */
        private double getTotalAmount() {
            return Math.round(originalAmount * (100.0 - discountPercentage)) / 100.0;
        }

        /** Updates the order's original price before discount. */
        private void setTotalAmount(double originalAmount) {
            this.originalAmount = originalAmount;
        }

        private void setPriceBreakdown(double serviceAmount, double supplyAmount) {
            if (!Double.isFinite(serviceAmount) || serviceAmount < 0
                    || !Double.isFinite(supplyAmount) || supplyAmount < 0) {
                throw new IllegalArgumentException("Laundry price breakdown is invalid.");
            }
            this.serviceAmount = serviceAmount;
            this.supplyAmount = supplyAmount;
            this.originalAmount = serviceAmount + supplyAmount;
            if (!Double.isFinite(this.originalAmount)) {
                throw new IllegalArgumentException("Laundry price is too large.");
            }
        }

        private double getServiceAmount() {
            return serviceAmount;
        }

        private double getSupplyAmount() {
            return supplyAmount;
        }

        /** Returns the order price before applyi its discount. */
        private double getOriginalAmount() {
            return originalAmount;
        }

        /** Returns the discount percentage applied to the original price. */
        private double getDiscountPercentage() {
            return discountPercentage;
        }

        /** Updates the percentage discount applied to this order. */
        private void setDiscountPercentage(double discountPercentage) {
            this.discountPercentage = discountPercentage;
        }

        /** Returns the current laundry-processi status. */
        private String getLaundryStatus() {
            return laundryStatus;
        }

        private boolean isAddedByAdmin() {
            return addedByAdmin;
        }

        private void setAddedByAdmin(boolean addedByAdmin) {
            this.addedByAdmin = addedByAdmin;
        }

        /** Updates the laundry-processi status. */
        private void setLaundryStatus(String laundryStatus) {
            this.laundryStatus = laundryStatus;
        }

        /** Returns the copy of the inventory quantities consumed by this order. */
        private Map<String, Double> getSupplyUsage() {
            return new LinkedHashMap<String, Double>(supplyUsage);
        }

        /** Stores a copy of the inventory quantities used by this order. */
        private void setSupplyUsage(Map<String, Double> supplyUsage) {
            this.supplyUsage = new LinkedHashMap<String, Double>(supplyUsage);
        }
    }

    private static final class Payment {
        private final String paymentId;
        private final String orderId;
        private final double amount;
        private final LocalDate paymentDate;

        /** Creates  payment record linked to an order and payment date. */
        private Payment(String paymentId, String orderId, double amount, LocalDate paymentDate) {
            this.paymentId = paymentId;
            this.orderId = orderId;
            this.amount = amount;
            this.paymentDate = paymentDate;
        }


        /** Returns the ID of the order covered by this payment. */
        private String getOrderId() {
            return orderId;
        }

        /** Returns the transaction amount; refunds are represented by negative amounts. */
        private double getAmount() {
            return amount;
        }

        /** Returns the date this payment was recorded. */
        private LocalDate getPaymentDate() {
            return paymentDate;
        }
    }

    private static final class LaundryData {
        /** Provides  access to the current in-memory customer records. */
        private static ArrayList<Customer> getCustomers() {
            return CUSTOMERS;
        }

        /** Provides  access to the current in-memory order records. */
        private static ArrayList<LaundryOrder> getOrders() {
            return ORDERS;
        }

        /** Provides  access to the current in-memory payment records. */
        private static ArrayList<Payment> getPayments() {
            return PAYMENTS;
        }

        /** Finds the customer by their unique ID. */
        private static Customer findCustomer(String customerId) {
            for (Customer customer : CUSTOMERS) {
                if (customer.getCustomerId().equals(customerId)) {
                    return customer;
                }
            }
            return null;
        }

        /** DSA: SEARCHING ALGORITHM - linear search for a laundry order by unique ID. */
        private static LaundryOrder findOrder(String orderId) {
            for (LaundryOrder order : ORDERS) {
                if (order.getOrderId().equals(orderId)) {
                    return order;
                }
            }
            return null;
        }

        /** DSA: SORTING ALGORITHM - insertion sort orders by received date, oldest first. */
        private static void sortPickupOrdersByReceivedDate(ArrayList<LaundryOrder> orders) {
            for (int index = 1; index < orders.size(); index++) {
                LaundryOrder current = orders.get(index);
                int previousIndex = index - 1;
                while (previousIndex >= 0
                        && orders.get(previousIndex).getDateReceived().isAfter(current.getDateReceived())) {
                    orders.set(previousIndex + 1, orders.get(previousIndex));
                    previousIndex--;
                }
                orders.set(previousIndex + 1, current);
            }
        }

        /** Previews the next unused customer ID without consumi it. */
        private static String createCustomerId() {
            int candidate = customerSequence;
            String customerId;
            do {
                customerId = String.format("C%03d", candidate++);
            } while (customerIdExists(customerId));
            return customerId;
        }

        /** Generates the customer ID not already used by an imported or manually entered record. */
        private static String createAvailableCustomerId() {
            return createCustomerId();
        }

        /** Generates the next laundry-order ID in the application sequence. */
        private static String createOrderId() {
            String orderId = previewOrderId();
            orderSequence = Integer.parseInt(orderId.substring(1)) + 1;
            return orderId;
        }

        /** Shows the next available ID without consumi it habthe a form is open. */
        private static String previewOrderId() {
            int candidate = orderSequence;
            String orderId;
            do {
                orderId = String.format("L%03d", candidate++);
            } while (findOrder(orderId) != null);
            return orderId;
        }

        /** Checks if a customer ID is already in use. */
        private static boolean customerIdExists(String customerId) {
            return findCustomer(customerId) != null;
        }

        /** Adds  customer after rejecti duplicate IDs. */
        private static void addCustomer(Customer customer) {
            if (customerIdExists(customer.getCustomerId())) {
                throw new IllegalArgumentException("That customer ID is already in use.");
            }
            CUSTOMERS.add(customer);
            String customerId = customer.getCustomerId();
            if (customerId.matches("C[0-9]{1,9}")) {
                int numericId = Integer.parseInt(customerId.substring(1));
                if (numericId < Integer.MAX_VALUE) {
                    customerSequence = Math.max(customerSequence, numericId + 1);
                }
            }
        }

        /** Checks if a customer's order history prevents deletingon. */
        private static boolean customerHasOrders(String customerId) {
            for (LaundryOrder order : ORDERS) {
                if (order.getCustomerId().equals(customerId)) {
                    return true;
                }
            }
            return false;
        }

        /** Counts the orders associated with a customer ID. */
        private static int getOrderCount(String customerId) {
            int count = 0;
            for (LaundryOrder order : ORDERS) {
                if (order.getCustomerId().equals(customerId)) {
                    count++;
                }
            }
            return count;
        }

        /** Deletes the customer only when they have no associated orders. */
        private static void deleteCustomer(Customer customer) {
            if (customerHasOrders(customer.getCustomerId())) {
                throw new IllegalArgumentException(
                    "This customer has laundry history and cannot be deleted.");
            }
            CUSTOMERS.remove(customer);
        }

        /** Deletes the order, restores unused queued supplies, and removes its payments. */
        private static void deleteOrder(LaundryOrder order) {
            if (!ORDERS.contains(order)) {
                throw new IllegalArgumentException("The selected laundry no longer exists.");
            }
            if ("In Queue".equals(order.getLaundryStatus())) {
                InventoryData.restore(order.getSupplyUsage());
            }
            ORDERS.remove(order);
            PAYMENTS.removeIf(payment -> payment.getOrderId().equals(order.getOrderId()));
        }

        /** Cancels the eligible order and applies the refund policy for its processi stage. */
        private static void cancelOrder(LaundryOrder order) {
            if (!ORDERS.contains(order)) {
                throw new IllegalArgumentException("The selected laundry no longer exists.");
            }
            String status = order.getLaundryStatus();
            if (!"In Queue".equals(status) && !"Pending".equals(status)) {
                throw new IllegalArgumentException(
                    "Only laundries In Queue or Pending can be cancelled.");
            }
            double amountToRefund = getCancellationRefund(order);
            if (!Double.isFinite(amountToRefund) || amountToRefund < 0) {
                throw new IllegalArgumentException("The laundry's payment total is invalid.");
            }
            if ("In Queue".equals(status)) {
                InventoryData.restore(order.getSupplyUsage());
            }
            if (amountToRefund > 0.00001) {
                recordRefund(order, amountToRefund);
            }
            order.setLaundryStatus("Cancelled");
        }

        private static double getCancellationRefund(LaundryOrder order) {
            String status = order.getLaundryStatus();
            if (!"In Queue".equals(status) && !"Pending".equals(status)) {
                throw new IllegalArgumentException(
                    "Only laundries In Queue or Pending can be cancelled.");
            }
            double netCollected = Math.max(0,
                getAmountPaid(order.getOrderId()) - getRefundedAmount(order.getOrderId()));
            if ("In Queue".equals(status)) {
                return netCollected;
            }
            double supplyCharge = Math.round(order.getSupplyAmount()
                * (100.0 - order.getDiscountPercentage())) / 100.0;
            double serviceCharge = Math.max(0, order.getTotalAmount() - supplyCharge);
            return Math.min(serviceCharge, Math.max(0, netCollected - supplyCharge));
        }

        /** Deletes every order and linked payment, restoring supplies for queued orders. */
        private static void deleteAllOrders() {
            Map<String, Double> unusedSupply = new LinkedHashMap<String, Double>();
            for (LaundryOrder order : ORDERS) {
                if ("In Queue".equals(order.getLaundryStatus())) {
                    for (Map.Entry<String, Double> entry : order.getSupplyUsage().entrySet()) {
                        Double previous = unusedSupply.get(entry.getKey());
                        unusedSupply.put(entry.getKey(),
                            (previous == null ? 0 : previous) + entry.getValue());
                    }
                }
            }
            InventoryData.restore(unusedSupply);
            ORDERS.clear();
            PAYMENTS.clear();
        }

        /** Totals the collected payment inflows for one order, excludi refund entries. */
        private static double getAmountPaid(String orderId) {
            double total = 0;
            for (Payment payment : PAYMENTS) {
                if (payment.getOrderId().equals(orderId) && payment.getAmount() > 0) {
                    total += payment.getAmount();
                }
            }
            return total;
        }

        /** Distinguishes unpaid, paid, and refunded orders. */
        private static String getPaymentStatus(LaundryOrder order) {
            if ("Cancelled".equals(order.getLaundryStatus())) {
                return getRefundedAmount(order.getOrderId()) > 0.00001 ? "Refunded" : "Cancelled";
            }
            double paid = getAmountPaid(order.getOrderId());
            if (order.getTotalAmount() <= 0.00001) {
                return "Paid";
            }
            if (paid <= 0.00001) {
                return "Unpaid";
            }
            return paid + 0.00001 >= order.getTotalAmount() ? "Paid" : "Unpaid";
        }

        private static double getRefundedAmount(String orderId) {
            double total = 0;
            for (Payment payment : PAYMENTS) {
                if (payment.getOrderId().equals(orderId) && payment.getAmount() < 0) {
                    total -= payment.getAmount();
                }
            }
            return total;
        }

        /** Validates a payment against the remaini balance and records it. */
        private static void recordPayment(LaundryOrder order, double amount) {
            if ("Cancelled".equals(order.getLaundryStatus())) {
                throw new IllegalArgumentException("Payments cannot be added to a cancelled laundry.");
            }
            double balance = order.getTotalAmount() - getAmountPaid(order.getOrderId());
            if (!Double.isFinite(amount) || amount <= 0) {
                throw new IllegalArgumentException("Payment must be greater than zero.");
            }
            if (amount > balance + 0.00001) {
                throw new IllegalArgumentException(
                    "Payment cannot be greater than the remaini balance.");
            }
            PAYMENTS.add(new Payment(String.format("P%03d", paymentSequence++),
                order.getOrderId(), amount, LocalDate.now()));
        }

        /** Records a refund as a negative payment so sales reports show the net collected amount. */
        private static void recordRefund(LaundryOrder order, double amount) {
            if (!Double.isFinite(amount) || amount <= 0
                    || getRefundedAmount(order.getOrderId()) + amount
                        > getAmountPaid(order.getOrderId()) + 0.00001) {
                throw new IllegalArgumentException("Refund amount is invalid.");
            }
            PAYMENTS.add(new Payment(String.format("P%03d", paymentSequence++),
                order.getOrderId(), -amount, LocalDate.now()));
        }

        /** Returns the configured price for a service or rejects an unknown service. */
        private static double getServiceRate(String serviceType) {
            ServiceCatalog.Service service = ServiceCatalog.findByName(serviceType);
            if (service != null) {
                return service.getRate();
            }
            throw new IllegalArgumentException("Unknown service type.");
        }

        /** Returns the weight or item unit appropriate for the selected service. */
        private static String getQuantityUnit(String serviceType) {
            ServiceCatalog.Service service = ServiceCatalog.findByName(serviceType);
            return service == null ? "item(s)" : service.getUnit().replace("per ", "");
        }
    }

    // Form support at application entry point.
    private static final class CustomerFields {
        private final String customerId;
        private final String name;
        private final String contact;
        private final String address;

        /** Holds customer form values before they are validated and saved. */
        private CustomerFields(String customerId, String name, String contact, String address) {
            this.customerId = customerId;
            this.name = name;
            this.contact = contact;
            this.address = address;
        }
    }

    private static final class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Runnable action;

        /** Creates  listener that runs the supplied action for document changes. */
        private SimpleDocumentListener(Runnable action) {
            this.action = action;
        }

        /** Recalculates the dependent values after text is inserted. */
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent event) {
            action.run();
        }

        /** Recalculates the dependent values after text is removed. */
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent event) {
            action.run();
        }

        /** Recalculates the dependent values after document attributes change. */
        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent event) {
            action.run();
        }
    }
}
