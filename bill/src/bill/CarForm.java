package bill;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Step 3 — add, edit, and remove car rental bookings.
 *
 * Improvements:
 *  - Row deletion via click on Del column
 *  - Row editing: clicking a row populates the form
 *  - Full input validation (empty fields, src == dest, fare > 0)
 *  - Blocks advance if session has zero bookings total
 *  - Fixed: uses card-local GBC, not shared BasePanel field
 */
public class CarForm extends BasePanel {

    private JTextField txtCarNo, txtSrc, txtDest, txtFare;
    private DefaultTableModel tableModel;
    private JTable     table;
    private JButton    btnAdd;
    private int        editingIndex = -1;

    public CarForm(MainApp controller, BookingSession session) {
        super(controller, session);
        setName(MainApp.CAR_PANEL);
        initUI();
    }

    private void initUI() {
        add(createTitlePanel("Car Rentals", "Step 3 of 4 — Add cab / taxi services"), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 40, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 40, 40, 40));

        // ---- LEFT: Form ----
        CardResult cr  = createCard();
        JPanel leftCard = cr.panel;
        GridBagConstraints gbc = cr.gbc;

        txtCarNo = new JTextField();
        txtSrc   = new JTextField();
        txtDest  = new JTextField();
        txtFare  = new JTextField();

        addLabelAndField(leftCard, gbc, "Car Number *",  txtCarNo, 0);
        addLabelAndField(leftCard, gbc, "Pickup *",      txtSrc,   1);
        addLabelAndField(leftCard, gbc, "Drop *",        txtDest,  2);
        addLabelAndField(leftCard, gbc, "Cost (Rs.) *",  txtFare,  3);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 5, 4, 5);
        JLabel hint = new JLabel("Click a row in the table to edit it.");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(TEXT_MUTED);
        leftCard.add(hint, gbc);

        gbc.gridy  = 5;
        gbc.insets = new Insets(16, 0, 0, 0);
        gbc.fill   = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        btnAdd = createStyledButton("+ Add Car", SUCCESS_COLOR, true);
        btnAdd.addActionListener(e -> commitCar());
        leftCard.add(btnAdd, gbc);

        // ---- RIGHT: Table ----
        JPanel rightPanel = new JPanel(new BorderLayout(0, 8));
        rightPanel.setOpaque(false);

        String[] cols = {"Car No", "Route", "Fare", "Del"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);
        table.getColumnModel().getColumn(3).setMaxWidth(55);
        table.getColumnModel().getColumn(3).setMinWidth(55);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromRow(table.getSelectedRow());
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col == 3 && row >= 0) {
                    if (editingIndex == row) cancelEdit();
                    session.removeCar(row);
                    refreshTable();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);
        rightPanel.add(scroll, BorderLayout.CENTER);

        content.add(leftCard);
        content.add(rightPanel);
        add(content, BorderLayout.CENTER);

        // ---- Bottom Nav ----
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 40, 20, 40));

        JButton btnBack = createStyledButton("← Back",          TEXT_MUTED,    false);
        JButton btnNext = createStyledButton("Generate Bill →", PRIMARY_COLOR, true);

        btnBack.addActionListener(e -> controller.showScreen(MainApp.FLIGHT_PANEL));
        btnNext.addActionListener(e -> advance());

        bottom.add(btnBack);
        bottom.add(btnNext);
        add(bottom, BorderLayout.SOUTH);

        refreshTable();
    }

    // -------------------------------------------------------------------------
    // Form Logic
    // -------------------------------------------------------------------------

    private void commitCar() {
        String carNo   = txtCarNo.getText().trim();
        String src     = txtSrc.getText().trim();
        String dest    = txtDest.getText().trim();
        String fareStr = txtFare.getText().trim();

        if (carNo.isEmpty() || src.isEmpty() || dest.isEmpty() || fareStr.isEmpty()) {
            showError("All fields marked with * are required.");
            return;
        }

        BigDecimal fare;
        try {
            fare = new BigDecimal(fareStr);
        } catch (NumberFormatException ex) {
            showError("Fare must be a valid number (e.g. 2500 or 2500.00).");
            return;
        }

        try {
            if (editingIndex >= 0) {
                session.updateCar(editingIndex, carNo, src, dest, fare);
            } else {
                session.addCar(carNo, src, dest, fare);
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
        List<BookingSession.CarBookingData> cars = session.getCars();
        if (row >= cars.size()) return;

        BookingSession.CarBookingData c = cars.get(row);
        txtCarNo.setText(c.carNo);
        txtSrc.setText(c.source);
        txtDest.setText(c.dest);
        txtFare.setText(c.fare.toPlainString());

        editingIndex = row;
        btnAdd.setText("✔ Update Car");
    }

    private void cancelEdit() {
        editingIndex = -1;
    }

    private void clearForm() {
        txtCarNo.setText(""); txtSrc.setText(""); txtDest.setText(""); txtFare.setText("");
        editingIndex = -1;
        btnAdd.setText("+ Add Car");
        table.clearSelection();
    }

    private void advance() {
        if (!session.hasBookings()) {
            showWarning("Please add at least one flight or car booking before generating the bill.");
            return;
        }
        controller.showScreen(MainApp.BILL_PANEL);
    }

    // -------------------------------------------------------------------------
    // Table
    // -------------------------------------------------------------------------

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (BookingSession.CarBookingData c : session.getCars()) {
            tableModel.addRow(new Object[]{
                c.carNo,
                c.source + " → " + c.dest,
                BasePanel.CURRENCY + " " + c.fare.toPlainString(),
                "🗑 Del"
            });
        }
    }
}
