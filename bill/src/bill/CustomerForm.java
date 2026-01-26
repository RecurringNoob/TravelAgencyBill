package bill;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class CustomerForm extends BasePanel {
    private JTextField txtName, txtContact, txtAddress, txtInvoice;
    private JSpinner dateSpinner;

    public CustomerForm(MainApp controller, BookingSession session) {
        super(controller, session);
        initUI();
    }

    private void initUI() {
        add(createTitlePanel("Customer Details", "Step 1 of 4"), BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        // The Card
        JPanel card = createCardPanel();
        
        txtName = new JTextField(20);
        txtContact = new JTextField(20);
        txtAddress = new JTextField(20);
        txtInvoice = new JTextField(20);
        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd-MM-yyyy"));

        addLabelAndField(card, "Full Name", txtName, 0);
        addLabelAndField(card, "Contact Number", txtContact, 1);
        addLabelAndField(card, "Billing Address", txtAddress, 2);
        addLabelAndField(card, "Invoice Number", txtInvoice, 3);
        addLabelAndField(card, "Booking Date", dateSpinner, 4);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.insets = new Insets(30, 0, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        
        JButton btnNext = createStyledButton("Next Step →", PRIMARY_COLOR, true);
        btnNext.addActionListener(e -> {
            if(txtName.getText().isEmpty() || txtInvoice.getText().isEmpty()) {
                showWarning("Please enter Name and Invoice Number.");
                return;
            }
            session.setCustomerDetails(txtName.getText(), txtContact.getText(), 
                txtAddress.getText(), txtInvoice.getText(), (Date)dateSpinner.getValue());
            controller.showScreen(MainApp.FLIGHT_PANEL);
        });
        
        card.add(btnNext, gbc);

        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);
        
        // Auto-fill for testing/defaults
        if(!session.hasCustomerDetails()) {
             txtInvoice.setText("INV-" + System.currentTimeMillis() / 1000);
        }
    }
}