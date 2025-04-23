package bill;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;

public class FlightBookingForm extends JFrame {
    private final Bookings booking;
    private final ArrayList<JPanel> flightPanels = new ArrayList<>();
    private final JPanel contentPanel;
    private final Color primaryColor = new Color(41, 128, 185);
    private final Font headerFont = new Font("Arial", Font.BOLD, 14);
    private final Font labelFont = new Font("Arial", Font.PLAIN, 12);
    
    public FlightBookingForm(Bookings booking) {
        this.booking = booking;
        
        // Setup frame
        setTitle("Ridhi Sidhi Tours - Flight Bookings");
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
        
        // Add first flight panel
        addFlightPanel();
        
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
        
        JLabel title = new JLabel("Flight Booking Details", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        panel.add(title, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void addFlightPanel() {
        JPanel flightPanel = new JPanel();
        flightPanel.setLayout(new GridBagLayout());
        flightPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor),
                "Flight " + (flightPanels.size() + 1),
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
        
        String[] labels = {"PNR Number:", "Source:", "Destination:", "Fare (₹):", "Passengers:"};
        JTextField[] fields = new JTextField[labels.length];
        
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.3;
            
            JLabel label = new JLabel(labels[i]);
            label.setFont(labelFont);
            flightPanel.add(label, gbc);
            
            gbc.gridx = 1;
            gbc.weightx = 0.7;
            
            fields[i] = new JTextField(15);
            flightPanel.add(fields[i], gbc);
        }
        
        // Set default value for passengers
        fields[4].setText("1");
        
        // Store panel with its fields
        flightPanel.putClientProperty("fields", fields);
        flightPanels.add(flightPanel);
        
        // Add to scroll pane
        contentPanel.add(flightPanel);
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Refresh UI
        revalidate();
        repaint();
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        JButton addButton = new JButton("Add Another Flight");
        addButton.setFont(labelFont);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> addFlightPanel());
        
        JButton nextButton = new JButton("Continue to Car Bookings");
        nextButton.setFont(labelFont);
        nextButton.setBackground(primaryColor);
        nextButton.setForeground(Color.WHITE);
        nextButton.setFocusPainted(false);
        nextButton.addActionListener(e -> onNextButtonClick());
        
        JButton skipButton = new JButton("Skip to Car Bookings");
        skipButton.setFont(labelFont);
        skipButton.setFocusPainted(false);
        skipButton.addActionListener(e -> onSkipButtonClick());
        
        panel.add(addButton);
        panel.add(skipButton);
        panel.add(nextButton);
        
        return panel;
    }
    
    private void onNextButtonClick() {
        boolean isValid = saveFlightBookings();
        if (isValid) {
            new CarBookingForm(booking);
            dispose();
        }
    }
    
    private void onSkipButtonClick() {
        new CarBookingForm(booking);
        dispose();
    }
    
    private boolean saveFlightBookings() {
        for (JPanel panel : flightPanels) {
            JTextField[] fields = (JTextField[]) panel.getClientProperty("fields");
            
            String pnr = fields[0].getText().trim();
            String source = fields[1].getText().trim();
            String destination = fields[2].getText().trim();
            String fare = fields[3].getText().trim();
            String passengersText = fields[4].getText().trim();
            
            // Validate required fields
            if (pnr.isEmpty() || source.isEmpty() || destination.isEmpty() || fare.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please complete all flight booking fields", 
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
            
            // Validate passengers is a number
            int passengers = 1;
            try {
                passengers = Integer.parseInt(passengersText);
                if (passengers < 1) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Number of passengers must be a valid positive number", 
                    "Invalid Input", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Add flight to booking
            booking.addFlightDetails(pnr, source, destination, fare, passengers);
        }
        return true;
    }
}