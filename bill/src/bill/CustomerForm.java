package bill;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

/**
 * Step 1 — collects customer and invoice metadata.
 */
public class CustomerForm extends BasePanel {

    private JTextField txtName, txtContact, txtAddress, txtInvoice;
    private JSpinner   dateSpinner;

    public CustomerForm(MainApp controller, BookingSession session) {
        super(controller, session);
        setName(MainApp.CUSTOMER_PANEL);
        initUI();
    }

    private void initUI() {
        add(createTitlePanel("Customer Details", "Step 1 of 4 — Enter booking information"), BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        CardResult cr = createCard();
        JPanel card   = cr.panel;
        GridBagConstraints gbc = cr.gbc;

        txtName     = new JTextField(20);
        txtContact  = new JTextField(20);
        txtAddress  = new JTextField(20);
        txtInvoice  = new JTextField(20);
        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd-MM-yyyy"));

        addLabelAndField(card, gbc, "Full Name *",       txtName,    0);
        addLabelAndField(card, gbc, "Contact Number",    txtContact, 1);
        addLabelAndField(card, gbc, "Billing Address",   txtAddress, 2);
        addLabelAndField(card, gbc, "Invoice Number *",  txtInvoice, 3);
        addLabelAndField(card, gbc, "Booking Date",      dateSpinner,4);

        // Next button
        gbc.gridx     = 0; gbc.gridy    = 5;
        gbc.gridwidth = 2; gbc.weightx  = 1.0;
        gbc.insets    = new Insets(30, 0, 0, 0);
        gbc.anchor    = GridBagConstraints.CENTER;
        gbc.fill      = GridBagConstraints.NONE;

        JButton btnNext = createStyledButton("Next Step →", PRIMARY_COLOR, true);
        btnNext.addActionListener(e -> advance());
        card.add(btnNext, gbc);

        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        populateFromSession();
    }

    /** Pre-fills fields from the session (supports round-trip edit and load-session). */
    public void populateFromSession() {
        if (session.isValidCustomer()) {
            txtName.setText(session.getCustomerName());
            txtContact.setText(session.getContactNumber());
            txtAddress.setText(session.getAddress());
            txtInvoice.setText(session.getInvoiceNumber());
            if (session.getBookingDate() != null) {
                dateSpinner.setValue(session.getBookingDate());
            }
        } else {
            // Auto-generate an invoice number for new sessions
            txtInvoice.setText("INV-" + System.currentTimeMillis() / 1000);
        }
    }

    private void advance() {
        String name = txtName.getText().trim();
        String inv  = txtInvoice.getText().trim();

        if (name.isEmpty() || inv.isEmpty()) {
            showWarning("Please enter Full Name and Invoice Number (marked with *).");
            return;
        }

        session.setCustomerDetails(
            name,
            txtContact.getText().trim(),
            txtAddress.getText().trim(),
            inv,
            (Date) dateSpinner.getValue()
        );
        controller.showScreen(MainApp.FLIGHT_PANEL);
    }
}
