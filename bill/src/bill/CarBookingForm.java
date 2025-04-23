package bill;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;

public class CarBookingForm extends JFrame {
    private final Bookings booking;
    private final ArrayList<JPanel> carPanels = new ArrayList<>();
    private final JPanel contentPanel;
    private final Color primaryColor = new Color(41, 128, 185);
    private final Font headerFont = new Font("Arial", Font.BOLD, 14);
    private final Font labelFont = new Font("Arial", Font.PLAIN, 12);
    
    public CarBookingForm(Bookings booking) {
        this.booking = booking;
        
        // Setup frame
        setTitle("Ridhi Sidhi Tours - Car Bookings");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create scrollable content panel
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
        
        // Add first car panel
        addCarPanel();
        
        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Final setup
        pack();
        setSize(600, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(primaryColor);
        panel.setLayout(new BorderLayout());
        
        JLabel title = new JLabel("Car Booking Details", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        panel.add(title, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void addCarPanel() {
        JPanel carPanel = new JPanel();
        carPanel.setLayout(new GridBagLayout());
        carPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor),
                "Car " + (carPanels.size() + 1),
                TitledBorder.LEFT,
                TitledBorder.TOP,
                headerFont,
                primaryColor
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        String[] labels = {"Car Number:", "Source:", "Destination:", "Fare (₹):"};
        JTextField[] fields = new JTextField[labels.length];
        
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.3;
            
            JLabel label = new JLabel(labels[i]);
            label.setFont(labelFont);
            carPanel.add(label, gbc);
            
            gbc.gridx = 1;
            gbc.weightx = 0.7;
            
            fields[i] = new JTextField(15);
            carPanel.add(fields[i], gbc);
        }
        
        // Store panel with its fields
        carPanel.putClientProperty("fields", fields);
        carPanels.add(carPanel);
        
        // Add to scroll pane
        contentPanel.add(carPanel);
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Refresh UI
        revalidate();
        repaint();
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        JButton addButton = new JButton("Add Another Car");
        addButton.setFont(labelFont);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> addCarPanel());
        
        JButton finishButton = new JButton("Generate Bill");
        finishButton.setFont(labelFont);
        finishButton.setBackground(primaryColor);
        finishButton.setForeground(Color.WHITE);
        finishButton.setFocusPainted(false);
        finishButton.addActionListener(e -> onFinishButtonClick());
        
        JButton skipButton = new JButton("Skip to Bill");
        skipButton.setFont(labelFont);
        skipButton.setFocusPainted(false);
        skipButton.addActionListener(e -> onSkipButtonClick());
        
        panel.add(addButton);
        panel.add(skipButton);
        panel.add(finishButton);
        
        return panel;
    }
    
    private void onFinishButtonClick() {
        boolean isValid = saveCarBookings();
        if (isValid) {
            generateBill();
        }
    }
    
    private void onSkipButtonClick() {
        generateBill();
    }
    
    private boolean saveCarBookings() {
        for (JPanel panel : carPanels) {
            JTextField[] fields = (JTextField[]) panel.getClientProperty("fields");
            
            String carNo = fields[0].getText().trim();
            String source = fields[1].getText().trim();
            String destination = fields[2].getText().trim();
            String fare = fields[3].getText().trim();
            
            // Validate required fields
            if (carNo.isEmpty() || source.isEmpty() || destination.isEmpty() || fare.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please complete all car booking fields", 
                    "Missing Information", 
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            // Validate fare is a number
            try {
                Integer.parseInt(fare);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Fare must be a valid number", 
                    "Invalid Input", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Add car to booking
            booking.addCarDetails(carNo, source, destination, fare);
        }
        return true;
    }
    
    private void generateBill() {
        try {
            booking.print(); // Print to console for debugging
            new BillGenerator(booking);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error generating bill: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}