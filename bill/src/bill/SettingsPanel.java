package bill;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Settings screen — lets the user configure company details and GST rate.
 * Changes are persisted via AppSettings and immediately applied to the session.
 */
public class SettingsPanel extends BasePanel {

    private JTextField txtName, txtTagline, txtEmail, txtPhone;
    private JTextField txtGst;

    public SettingsPanel(MainApp controller, BookingSession session) {
        super(controller, session);
        setName(MainApp.SETTINGS_PANEL);
        initUI();
    }

    private void initUI() {
        add(createTitlePanel("Settings", "Configure company details and tax rate"), BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        CardResult cr  = createCard();
        JPanel card    = cr.panel;
        GridBagConstraints gbc = cr.gbc;

        AppSettings s = AppSettings.getInstance();
        txtName    = new JTextField(s.getCompanyName());
        txtTagline = new JTextField(s.getCompanyTagline());
        txtEmail   = new JTextField(s.getCompanyEmail());
        txtPhone   = new JTextField(s.getCompanyPhone());
        txtGst     = new JTextField(s.getGstRate().toPlainString());

        addLabelAndField(card, gbc, "Company Name",    txtName,    0);
        addLabelAndField(card, gbc, "Tagline",         txtTagline, 1);
        addLabelAndField(card, gbc, "Email",           txtEmail,   2);
        addLabelAndField(card, gbc, "Phone",           txtPhone,   3);
        addLabelAndField(card, gbc, "GST Rate (%)",    txtGst,     4);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 0, 0, 0);
        gbc.fill   = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnRow.setOpaque(false);

        JButton btnSave = createStyledButton("Save Settings", SUCCESS_COLOR, true);
        JButton btnBack = createStyledButton("← Back",        TEXT_MUTED,   false);

        btnSave.addActionListener(e -> saveSettings());
        btnBack.addActionListener(e -> controller.showScreen(MainApp.CUSTOMER_PANEL));

        btnRow.add(btnBack);
        btnRow.add(btnSave);
        card.add(btnRow, gbc);

        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);
    }

    private void saveSettings() {
        BigDecimal gst;
        try {
            gst = new BigDecimal(txtGst.getText().trim());
            if (gst.compareTo(BigDecimal.ZERO) < 0 || gst.compareTo(new BigDecimal("100")) > 0) {
                showError("GST rate must be between 0 and 100.");
                return;
            }
        } catch (NumberFormatException ex) {
            showError("GST rate must be a valid number.");
            return;
        }

        AppSettings s = AppSettings.getInstance();
        s.setCompanyName(txtName.getText().trim());
        s.setCompanyTagline(txtTagline.getText().trim());
        s.setCompanyEmail(txtEmail.getText().trim());
        s.setCompanyPhone(txtPhone.getText().trim());
        s.setGstRate(gst);
        s.save();
        s.applyTo(session); // Immediately reflect in current session

        showSuccess("Settings saved successfully.");
        controller.showScreen(MainApp.CUSTOMER_PANEL);
    }
}
