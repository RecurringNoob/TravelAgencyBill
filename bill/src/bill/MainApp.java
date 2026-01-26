package bill;

import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private final BookingSession session = new BookingSession();
    private JPanel navigationPanel;

    // Screen IDs
    public static final String CUSTOMER_PANEL = "CUSTOMER";
    public static final String FLIGHT_PANEL = "FLIGHT";
    public static final String CAR_PANEL = "CAR";
    public static final String BILL_PANEL = "BILL";

    public MainApp() {
        setTitle("Ridhi Sidhi Tours - Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        
        // Main Container
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(236, 240, 241));

        // Top Navigation
        navigationPanel = createModernNav();
        container.add(navigationPanel, BorderLayout.NORTH);

        // Content Area
        mainPanel.setOpaque(false);
        mainPanel.add(new CustomerForm(this, session), CUSTOMER_PANEL);
        mainPanel.add(new FlightForm(this, session), FLIGHT_PANEL);
        mainPanel.add(new CarForm(this, session), CAR_PANEL);
        mainPanel.add(new BillView(this, session), BILL_PANEL);

        container.add(mainPanel, BorderLayout.CENTER);
        add(container);
        
        showScreen(CUSTOMER_PANEL);
    }

    private JPanel createModernNav() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(41, 128, 185));
        panel.setPreferredSize(new Dimension(getWidth(), 70));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        // Brand
        JLabel brand = new JLabel("✈ RIDHI SIDHI TOURS");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brand.setForeground(Color.WHITE);
        panel.add(brand, BorderLayout.WEST);

        // Steps Container
        JPanel steps = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 20));
        steps.setOpaque(false);
        
        steps.add(createStepBadge("1", "Customer", CUSTOMER_PANEL));
        steps.add(createArrow());
        steps.add(createStepBadge("2", "Flights", FLIGHT_PANEL));
        steps.add(createArrow());
        steps.add(createStepBadge("3", "Cars", CAR_PANEL));
        steps.add(createArrow());
        steps.add(createStepBadge("4", "Invoice", BILL_PANEL));

        panel.add(steps, BorderLayout.EAST);
        return panel;
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
        // Circular border handled by basic rendering for now, or use custom paint
        
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameLbl.setForeground(new Color(255, 255, 255, 150));

        p.add(numLbl);
        p.add(nameLbl);
        return p;
    }

    public void showScreen(String name) {
        if(name.equals(BILL_PANEL)) {
            // Refresh bill view
             for(Component c : mainPanel.getComponents()) {
                if(c.getName() != null && c.getName().equals(BILL_PANEL)) {
                    ((BillView)c).refresh();
                }
            }
        }
        cardLayout.show(mainPanel, name);
        updateNav(name);
    }

    private void updateNav(String activeId) {
        Component[] comps = ((JPanel)navigationPanel.getComponent(1)).getComponents();
        for(Component c : comps) {
            if(c instanceof JPanel && c.getName() != null) {
                JPanel badge = (JPanel)c;
                JLabel circle = (JLabel)badge.getComponent(0);
                JLabel text = (JLabel)badge.getComponent(1);
                
                if(c.getName().startsWith(activeId)) {
                    circle.setBackground(Color.WHITE);
                    circle.setForeground(new Color(41, 128, 185));
                    text.setForeground(Color.WHITE);
                    text.setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else {
                    circle.setBackground(new Color(255, 255, 255, 50));
                    circle.setForeground(Color.WHITE);
                    text.setForeground(new Color(255, 255, 255, 150));
                    text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }
            }
        }
    }

    public BookingSession getSession() { return session; }
    public void resetBooking() { session.reset(); showScreen(CUSTOMER_PANEL); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                // Global UI Polish
                UIManager.put("Button.arc", 10);
                UIManager.put("Component.arc", 10);
                UIManager.put("TextComponent.arc", 10);
            } catch (Exception e) {}
            new MainApp().setVisible(true);
        });
    }
}