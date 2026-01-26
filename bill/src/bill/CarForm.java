package bill;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;

public class CarForm extends BasePanel {
    private JTextField txtCarNo, txtSrc, txtDest, txtFare;
    private DefaultTableModel tableModel;

    public CarForm(MainApp controller, BookingSession session) {
        super(controller, session);
        initUI();
    }

    private void initUI() {
        add(createTitlePanel("Car Rentals", "Add cab/taxi services"), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 40, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 40, 40, 40));

        // LEFT: FORM
        JPanel leftCard = createCardPanel();
        
        txtCarNo = new JTextField(); 
        txtSrc = new JTextField(); 
        txtDest = new JTextField(); 
        txtFare = new JTextField();

        addLabelAndField(leftCard, "Car Number", txtCarNo, 0);
        addLabelAndField(leftCard, "Pickup", txtSrc, 1);
        addLabelAndField(leftCard, "Drop", txtDest, 2);
        addLabelAndField(leftCard, "Cost (₹)", txtFare, 3);

        JButton btnAdd = createStyledButton("+ Add Car", SUCCESS_COLOR, true);
        btnAdd.addActionListener(e -> addCar());
        
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(20, 0, 0, 0);
        leftCard.add(btnAdd, gbc);

        // RIGHT: TABLE
        JPanel rightCard = new JPanel(new BorderLayout());
        rightCard.setOpaque(false);
        
        String[] cols = {"Car No", "Route", "Fare"};
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
        JButton btnNext = createStyledButton("Generate Bill →", PRIMARY_COLOR, true);
        
        btnBack.addActionListener(e -> controller.showScreen(MainApp.FLIGHT_PANEL));
        btnNext.addActionListener(e -> controller.showScreen(MainApp.BILL_PANEL));
        
        bottom.add(btnBack);
        bottom.add(btnNext);
        add(bottom, BorderLayout.SOUTH);
        
        refreshTable();
    }

    private void addCar() {
        try {
            BigDecimal fare = new BigDecimal(txtFare.getText());
            session.addCar(txtCarNo.getText(), txtSrc.getText(), txtDest.getText(), fare);
            txtCarNo.setText(""); txtSrc.setText(""); txtDest.setText(""); txtFare.setText("");
            refreshTable();
        } catch(Exception e) { showError("Invalid Fare"); }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for(BookingSession.CarBookingData c : session.getCars()) {
            tableModel.addRow(new Object[]{c.carNo, c.source+"-"+c.dest, c.fare});
        }
    }
}