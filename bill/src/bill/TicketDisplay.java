package bill;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TicketDisplay extends JFrame {
    private final Bookings booking;
    private JTextField customerNameField, addressField, invoiceField, contactField;
    private JSpinner dateSpinner;
    private final Color primaryColor = new Color(41, 128, 185);
    private final Font headerFont = new Font("Arial", Font.BOLD, 14);
    private final Font labelFont = new Font("Arial", Font.PLAIN, 12);
    
    public TicketDisplay() {
        booking = new Bookings();
        
        // Setup frame
        setTitle("Ridhi Sidhi Tours - Customer Information");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create form panel
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Final setup
        pack();
        setSize(600, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(primaryColor);
        panel.setLayout(new BorderLayout());
        
        JLabel title = new JLabel("Customer Information", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        panel.add(title, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        
        // Customer Name
        addFormRow(panel, "Customer Name:", gbc, 0);
        customerNameField = new JTextField(20);
        addFormField(panel, customerNameField, gbc, 0);
        
        // Contact
        addFormRow(panel, "Contact Number:", gbc, 1);
        contactField = new JTextField(20);
        addFormField(panel, contactField, gbc, 1);
        
        // Address
        addFormRow(panel, "Address:", gbc, 2);
        addressField = new JTextField(20);
        addFormField(panel, addressField, gbc, 2);
        
        // Date
        addFormRow(panel, "Date:", gbc, 3);
        dateSpinner = createDateSpinner();
        addFormField(panel, dateSpinner, gbc, 3);
        
        // Invoice
        addFormRow(panel, "Invoice Number:", gbc, 4);
        invoiceField = new JTextField(20);
        addFormField(panel, invoiceField, gbc, 4);
        
        return panel;
    }
    
    private void addFormRow(JPanel panel, String labelText, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        
        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        panel.add(label, gbc);
    }
    
    private void addFormField(JPanel panel, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 0.7;
        
        panel.add(field, gbc);
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));
        
        JButton nextButton = new JButton("Next");
        nextButton.setFont(labelFont);
        nextButton.setBackground(primaryColor);
        nextButton.setForeground(Color.WHITE);
        nextButton.setFocusPainted(false);
        nextButton.addActionListener(e -> onNextButtonClick());
        
        panel.add(nextButton);
        return panel;
    }
    
    private JSpinner createDateSpinner() {
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(dateModel);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        return spinner;
    }
    
    private void onNextButtonClick() {
        String customerName = customerNameField.getText().trim();
        String contactNo = contactField.getText().trim();
        String address = addressField.getText().trim();
        String invoiceNo = invoiceField.getText().trim();
        Date selectedDate = (Date) dateSpinner.getValue();
        
        // Validate inputs
        if (customerName.isEmpty() || contactNo.isEmpty() || address.isEmpty() || invoiceNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please fill in all required fields", 
                "Missing Information", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Update booking information
        booking.addCustomerDetails(customerName, contactNo, address);
        booking.setDate(selectedDate);
        booking.setInvoice(invoiceNo);
        
        // Proceed to flight booking screen
        new FlightBookingForm(booking);
        dispose();
    }
  }
    