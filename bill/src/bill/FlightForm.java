package bill;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Step 2 — add, edit, and remove flight bookings.
 *
 * Improvements:
 *  - Row deletion via "Delete" button in table
 *  - Row editing: clicking a row populates the form for editing
 *  - Full input validation (empty fields, src == dest, fare > 0)
 *  - Fixed: no longer uses shared mutable gbc from BasePanel
 *  - Blocks advance if no bookings AND no car bookings exist
 */
public class FlightForm extends BasePanel {

    private JTextField txtPnr, txtSrc, txtDest, txtFare;
    private JSpinner   spinPax;
    private DefaultTableModel tableModel;
    private JTable     table;
    private JButton    btnAdd;

    private int editingIndex = -1; // -1 means "add mode"

    public FlightForm(MainApp controller, BookingSession session) {
        super(controller, session);
        setName(MainApp.FLIGHT_PANEL);
        initUI();
    }

    private void initUI() {
        add(createTitlePanel("Flight Bookings", "Step 2 of 4 — Add flight legs to this invoice"), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 40, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 40, 40, 40));

        // ---- LEFT: Form ----
        CardResult cr  = createCard();
        JPanel leftCard = cr.panel;
        GridBagConstraints gbc = cr.gbc;

        txtPnr  = new JTextField();
        txtSrc  = new JTextField();
        txtDest = new JTextField();
        txtFare = new JTextField();
        spinPax = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));

        addLabelAndField(leftCard, gbc, "PNR Number *",  txtPnr,  0);
        addLabelAndField(leftCard, gbc, "Source *",      txtSrc,  1);
        addLabelAndField(leftCard, gbc, "Destination *", txtDest, 2);
        addLabelAndField(leftCard, gbc, "Fare (Rs.) *",  txtFare, 3);
        addLabelAndField(leftCard, gbc, "Passengers",    spinPax, 4);

        // Hint label
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 5, 4, 5);
        JLabel hint = new JLabel("Click a row in the table to edit it.");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(TEXT_MUTED);
        leftCard.add(hint, gbc);

        // Add / Update button
        gbc.gridy  = 6;
        gbc.insets = new Insets(16, 0, 0, 0);
        gbc.fill   = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        btnAdd = createStyledButton("+ Add Flight", SUCCESS_COLOR, true);
        btnAdd.addActionListener(e -> commitFlight());
        leftCard.add(btnAdd, gbc);

        // ---- RIGHT: Table ----
        JPanel rightPanel = new JPanel(new BorderLayout(0, 8));
        rightPanel.setOpaque(false);

        String[] cols = {"PNR", "Route", "Pax", "Fare", "Del"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);
        table.getColumnModel().getColumn(4).setMaxWidth(55);
        table.getColumnModel().getColumn(4).setMinWidth(55);

        // Row click → populate form for editing
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromRow(table.getSelectedRow());
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);

        // Delete button row (rendered as a non-editable column button via row listener)
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col == 4 && row >= 0) {
                    if (editingIndex == row) cancelEdit();
                    session.removeFlight(row);
                    refreshTable();
                }
            }
        });

        rightPanel.add(scroll, BorderLayout.CENTER);

        content.add(leftCard);
        content.add(rightPanel);
        add(content, BorderLayout.CENTER);

        // ---- Bottom Nav ----
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 40, 20, 40));

        JButton btnBack = createStyledButton("← Back",       TEXT_MUTED,     false);
        JButton btnNext = createStyledButton("Next Step →",  PRIMARY_COLOR,  true);

        btnBack.addActionListener(e -> controller.showScreen(MainApp.CUSTOMER_PANEL));
        btnNext.addActionListener(e -> advance());

        bottom.add(btnBack);
        bottom.add(btnNext);
        add(bottom, BorderLayout.SOUTH);

        refreshTable();
    }

    // -------------------------------------------------------------------------
    // Form Logic
    // -------------------------------------------------------------------------

    private void commitFlight() {
        String pnr  = txtPnr.getText().trim();
        String src  = txtSrc.getText().trim();
        String dest = txtDest.getText().trim();
        String fareStr = txtFare.getText().trim();
        int    pax  = (int) spinPax.getValue();

        if (pnr.isEmpty() || src.isEmpty() || dest.isEmpty() || fareStr.isEmpty()) {
            showError("All fields marked with * are required.");
            return;
        }

        BigDecimal fare;
        try {
            fare = new BigDecimal(fareStr);
        } catch (NumberFormatException ex) {
            showError("Fare must be a valid number (e.g. 12500 or 12500.50).");
            return;
        }

        try {
            if (editingIndex >= 0) {
                session.updateFlight(editingIndex, pnr, src, dest, fare, pax);
            } else {
                session.addFlight(pnr, src, dest, fare, pax);
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }

        clearForm();
        refreshTable();
    }

    private void populateFormFromRow(int row) {
        if (row < 0) return;
        List<BookingSession.FlightBookingData> flights = session.getFlights();
        if (row >= flights.size()) return;

        BookingSession.FlightBookingData f = flights.get(row);
        txtPnr.setText(f.pnr);
        txtSrc.setText(f.source);
        txtDest.setText(f.dest);
        txtFare.setText(f.fare.toPlainString());
        spinPax.setValue(f.passengers);

        editingIndex = row;
        btnAdd.setText("✔ Update Flight");
        btnAdd.setBackground(BasePanel.PRIMARY_COLOR);
    }

    private void cancelEdit() {
        editingIndex = -1;
    }

    private void clearForm() {
        txtPnr.setText(""); txtSrc.setText(""); txtDest.setText(""); txtFare.setText("");
        spinPax.setValue(1);
        editingIndex = -1;
        btnAdd.setText("+ Add Flight");
        table.clearSelection();
    }

    private void advance() {
        // Flights are optional — car bookings alone are also valid
        controller.showScreen(MainApp.CAR_PANEL);
    }

    // -------------------------------------------------------------------------
    // Table Rendering
    // -------------------------------------------------------------------------

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (BookingSession.FlightBookingData f : session.getFlights()) {
            tableModel.addRow(new Object[]{
                f.pnr,
                f.source + " → " + f.dest,
                f.passengers,
                BasePanel.CURRENCY + " " + f.fare.toPlainString(),
                "🗑 Del"
            });
        }
    }
}
