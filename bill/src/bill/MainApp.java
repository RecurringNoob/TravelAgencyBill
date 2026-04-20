package bill;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Application entry point, JFrame host, and navigation controller.
 * Owns the single BookingSession shared by all panels.
 */
public class MainApp extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final CardLayout     cardLayout  = new CardLayout();
    private final JPanel         mainPanel   = new JPanel(cardLayout);
    private final BookingSession session     = new BookingSession();
    private       JPanel         navStepsPanel;

    // Screen IDs
    public static final String CUSTOMER_PANEL = "CUSTOMER";
    public static final String FLIGHT_PANEL   = "FLIGHT";
    public static final String CAR_PANEL      = "CAR";
    public static final String BILL_PANEL     = "BILL";
    public static final String SETTINGS_PANEL = "SETTINGS";
    public static final String HISTORY_PANEL  = "HISTORY";

    private BillView     billView;
    private HistoryPanel historyPanel;

    public MainApp() {
        // Apply saved settings to the new session
        AppSettings.getInstance().applyTo(session);

        setTitle("Ridhi Sidhi Tours — Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 780);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BasePanel.BACKGROUND_COLOR);

        // Top navigation bar
        container.add(createNav(), BorderLayout.NORTH);

        // Screen cards
        mainPanel.setOpaque(false);
        mainPanel.add(new CustomerForm(this, session),  CUSTOMER_PANEL);
        mainPanel.add(new FlightForm(this, session),    FLIGHT_PANEL);
        mainPanel.add(new CarForm(this, session),       CAR_PANEL);

        billView = new BillView(this, session);
        mainPanel.add(billView, BILL_PANEL);

        historyPanel = new HistoryPanel(this, session);
        mainPanel.add(historyPanel, HISTORY_PANEL);

        mainPanel.add(new SettingsPanel(this, session), SETTINGS_PANEL);

        container.add(mainPanel, BorderLayout.CENTER);
        add(container);

        showScreen(CUSTOMER_PANEL);
    }

    // -------------------------------------------------------------------------
    // Navigation Bar
    // -------------------------------------------------------------------------

    private JPanel createNav() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BasePanel.PRIMARY_COLOR);
        panel.setPreferredSize(new Dimension(getWidth(), 70));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        // Brand
        JLabel brand = new JLabel("RST  RIDHI SIDHI TOURS");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brand.setForeground(Color.WHITE);
        panel.add(brand, BorderLayout.WEST);

        // Workflow steps (centre)
        navStepsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        navStepsPanel.setOpaque(false);
        navStepsPanel.add(createStepBadge("1", "Customer", CUSTOMER_PANEL));
        navStepsPanel.add(createArrow());
        navStepsPanel.add(createStepBadge("2", "Flights",  FLIGHT_PANEL));
        navStepsPanel.add(createArrow());
        navStepsPanel.add(createStepBadge("3", "Cars",     CAR_PANEL));
        navStepsPanel.add(createArrow());
        navStepsPanel.add(createStepBadge("4", "Invoice",  BILL_PANEL));
        panel.add(navStepsPanel, BorderLayout.CENTER);

        // Utility buttons (east)
        JPanel utils = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 18));
        utils.setOpaque(false);

        JButton btnHistory  = createNavButton("History");
        JButton btnSettings = createNavButton("Settings");
        JButton btnSave     = createNavButton("Save");
        JButton btnLoad     = createNavButton("Load");

        btnHistory.addActionListener(e  -> { historyPanel.refresh(); showScreen(HISTORY_PANEL); });
        btnSettings.addActionListener(e -> showScreen(SETTINGS_PANEL));
        btnSave.addActionListener(e     -> saveSession());
        btnLoad.addActionListener(e     -> loadSession());

        utils.add(btnSave);
        utils.add(btnLoad);
        utils.add(btnHistory);
        utils.add(btnSettings);
        panel.add(utils, BorderLayout.EAST);

        return panel;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(255, 255, 255, 40));
        btn.setOpaque(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 80), 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        return btn;
    }

    private JLabel createArrow() {
        JLabel l = new JLabel("›");
        l.setForeground(new Color(255, 255, 255, 100));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        return l;
    }

    private JPanel createStepBadge(String num, String name, String id) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        p.setName(id + "_BADGE");

        JLabel numLbl = new JLabel(num);
        numLbl.setOpaque(true);
        numLbl.setBackground(new Color(255, 255, 255, 50));
        numLbl.setForeground(Color.WHITE);
        numLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        numLbl.setHorizontalAlignment(SwingConstants.CENTER);
        numLbl.setPreferredSize(new Dimension(24, 24));

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameLbl.setForeground(new Color(255, 255, 255, 150));

        p.add(numLbl);
        p.add(nameLbl);
        return p;
    }

    // -------------------------------------------------------------------------
    // Screen Navigation
    // -------------------------------------------------------------------------

    public void showScreen(String name) {
        if (name.equals(BILL_PANEL)) {
            billView.refresh();
        }
        cardLayout.show(mainPanel, name);
        updateNav(name);
    }

    private void updateNav(String activeId) {
        for (Component c : navStepsPanel.getComponents()) {
            if (!(c instanceof JPanel)) continue;
            JPanel badge = (JPanel) c;
            if (badge.getName() == null) continue;

            JLabel circle = (JLabel) badge.getComponent(0);
            JLabel text   = (JLabel) badge.getComponent(1);

            boolean active = badge.getName().startsWith(activeId);
            circle.setBackground(active ? Color.WHITE           : new Color(255, 255, 255, 50));
            circle.setForeground(active ? BasePanel.PRIMARY_COLOR : Color.WHITE);
            text.setForeground(active   ? Color.WHITE           : new Color(255, 255, 255, 150));
            text.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
        }
    }

    // -------------------------------------------------------------------------
    // Session Save / Load
    // -------------------------------------------------------------------------

    private void saveSession() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Session");
        fc.setSelectedFile(new File("session_" + session.getInvoiceNumber() + ".rss"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                session.saveTo(fc.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Session saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadSession() {
        if (session.isValidCustomer() || session.hasBookings()) {
            int choice = JOptionPane.showConfirmDialog(this,
                "Loading a session will replace current data. Continue?",
                "Load Session", JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Load Session");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                BookingSession loaded = BookingSession.loadFrom(fc.getSelectedFile());
                // Copy all loaded data into our shared session object
                session.reset();
                session.setCustomerDetails(
                    loaded.getCustomerName(), loaded.getContactNumber(),
                    loaded.getAddress(), loaded.getInvoiceNumber(), loaded.getBookingDate());
                session.setCompanyDetails(
                    loaded.getCompanyName(), loaded.getCompanyTagline(),
                    loaded.getCompanyEmail(), loaded.getCompanyPhone());
                session.setGstRate(loaded.getGstRatePercent());
                for (BookingSession.FlightBookingData f : loaded.getFlights()) {
                    session.addFlight(f.pnr, f.source, f.dest, f.fare, f.passengers);
                }
                for (BookingSession.CarBookingData c : loaded.getCars()) {
                    session.addCar(c.carNo, c.source, c.dest, c.fare);
                }
                showScreen(CUSTOMER_PANEL);
                JOptionPane.showMessageDialog(this, "Session loaded.", "Loaded", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Public API for panels
    // -------------------------------------------------------------------------

    public BookingSession getSession() { return session; }

    public void resetBooking() {
        session.reset();
        AppSettings.getInstance().applyTo(session);
        showScreen(CUSTOMER_PANEL);
    }

    // -------------------------------------------------------------------------
    // Entry Point
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                UIManager.put("Button.arc",         10);
                UIManager.put("Component.arc",      10);
                UIManager.put("TextComponent.arc",  10);
            } catch (Exception ignored) {}
            new MainApp().setVisible(true);
        });
    }
}
