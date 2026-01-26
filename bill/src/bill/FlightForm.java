package bill;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;

public class FlightForm extends BasePanel {
    private JTextField txtPnr, txtSrc, txtDest, txtFare;
    private JSpinner spinPax;
    private DefaultTableModel tableModel;

    public FlightForm(MainApp controller, BookingSession session) {
        super(controller, session);
        initUI();
    }

    private void initUI() {
        add(createTitlePanel("Flight Bookings", "Add flight legs to this invoice"), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 40, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 40, 40, 40));

        // LEFT: FORM
        JPanel leftCard = createCardPanel();
        
        txtPnr = new JTextField(); 
        txtSrc = new JTextField(); 
        txtDest = new JTextField(); 
        txtFare = new JTextField();
        spinPax = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));

        addLabelAndField(leftCard, "PNR Number", txtPnr, 0);
        addLabelAndField(leftCard, "Source", txtSrc, 1);
        addLabelAndField(leftCard, "Destination", txtDest, 2);
        addLabelAndField(leftCard, "Cost (₹)", txtFare, 3);
        addLabelAndField(leftCard, "Passengers", spinPax, 4);

        JButton btnAdd = createStyledButton("+ Add Flight", SUCCESS_COLOR, true);
        btnAdd.addActionListener(e -> addFlight());
        
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(20, 0, 0, 0);
        leftCard.add(btnAdd, gbc);

        // RIGHT: TABLE
        JPanel rightCard = new JPanel(new BorderLayout());
        rightCard.setOpaque(false);
        
        String[] cols = {"PNR", "Route", "Pax", "Fare"};
        tableModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tableModel);
        styleTable(table);
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);
        
        rightCard.add(scroll, BorderLayout.CENTER);

        content.add(leftCard);
        content.add(rightCard);
        add(content, BorderLayout.CENTER);

        // BOTTOM NAV
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 40, 20, 40));
        
        JButton btnBack = createStyledButton("Back", TEXT_MUTED, false);
        JButton btnNext = createStyledButton("Next Step →", PRIMARY_COLOR, true);
        
        btnBack.addActionListener(e -> controller.showScreen(MainApp.CUSTOMER_PANEL));
        btnNext.addActionListener(e -> controller.showScreen(MainApp.CAR_PANEL));
        
        bottom.add(btnBack);
        bottom.add(btnNext);
        add(bottom, BorderLayout.SOUTH);
        
        refreshTable();
    }

    private void addFlight() {
        try {
            BigDecimal fare = new BigDecimal(txtFare.getText());
            session.addFlight(txtPnr.getText(), txtSrc.getText(), txtDest.getText(), fare, (int)spinPax.getValue());
            txtPnr.setText(""); txtSrc.setText(""); txtDest.setText(""); txtFare.setText("");
            refreshTable();
        } catch(Exception e) { showError("Invalid Fare"); }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for(BookingSession.FlightBookingData f : session.getFlights()) {
            tableModel.addRow(new Object[]{f.pnr, f.source+"-"+f.dest, f.passengers, f.fare});
        }
    }
}