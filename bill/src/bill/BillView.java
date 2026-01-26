package bill;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;

public class BillView extends BasePanel {
    private JTextArea previewArea;
    private JLabel totalLabel;

    public BillView(MainApp controller, BookingSession session) {
        super(controller, session);
        setName(MainApp.BILL_PANEL); 
        initUI();
    }

    private void initUI() {
        add(createTitlePanel("Invoice Preview", "Review and export"), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(20, 0));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(0, 40, 20, 40));

        // LEFT: The "Paper" Invoice
        JPanel paperPanel = new JPanel(new BorderLayout());
        paperPanel.setBackground(Color.WHITE);
        paperPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(30, 30, 30, 30)
        ));

        previewArea = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        previewArea.setBackground(Color.WHITE);
        
        JScrollPane scroll = new JScrollPane(previewArea);
        scroll.setBorder(null);
        paperPanel.add(scroll, BorderLayout.CENTER);

        // RIGHT: Actions & Summary
        JPanel sidebar = createCardPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(300, 0));

        JLabel lblTotalHeader = new JLabel("GRAND TOTAL");
        lblTotalHeader.setFont(LABEL_FONT);
        lblTotalHeader.setForeground(TEXT_MUTED);
        lblTotalHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        totalLabel = new JLabel("₹ 0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        totalLabel.setForeground(SUCCESS_COLOR);
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnPDF = createStyledButton("Save PDF", PRIMARY_COLOR, true);
        btnPDF.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPDF.addActionListener(e -> savePDF());

        JButton btnNew = createStyledButton("New Booking", WARNING_COLOR, false);
        btnNew.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnNew.addActionListener(e -> {
            if(confirm("Start new booking? Current data will be lost.")) controller.resetBooking();
        });

        sidebar.add(lblTotalHeader);
        sidebar.add(totalLabel);
        sidebar.add(Box.createVerticalStrut(30));
        sidebar.add(btnPDF);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnNew);

        mainContent.add(paperPanel, BorderLayout.CENTER);
        mainContent.add(sidebar, BorderLayout.EAST);
        add(mainContent, BorderLayout.CENTER);
    }

    public void refresh() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        StringBuilder sb = new StringBuilder();
        String line = "----------------------------------------------------------------------\n";
        
        sb.append("                       RIDHI SIDHI TOURS                        \n");
        sb.append("                  Your Journey, Our Responsibility              \n\n");
        sb.append(" INVOICE: ").append(session.getInvoiceNumber()).append("\n");
        sb.append(" DATE:    ").append(sdf.format(session.getBookingDate())).append("\n");
        sb.append(" CLIENT:  ").append(session.getCustomerName()).append("\n");
        sb.append(line);
        sb.append(String.format(" %-40s | %15s\n", "DESCRIPTION", "AMOUNT"));
        sb.append(line);
        
        for(BookingSession.FlightBookingData f : session.getFlights()) {
            sb.append(String.format(" FLIGHT: %-32s | %15s\n", f.source+"-"+f.dest, f.fare));
        }
        for(BookingSession.CarBookingData c : session.getCars()) {
            sb.append(String.format(" CAR:    %-32s | %15s\n", c.source+"-"+c.dest, c.fare));
        }
        sb.append(line);
        sb.append(String.format(" TOTAL: %58s\n", session.getTotalAmount()));
        
        previewArea.setText(sb.toString());
        totalLabel.setText("₹ " + session.getTotalAmount());
    }

    private void savePDF() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("Invoice_" + session.getInvoiceNumber() + ".pdf"));
        if(fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                new PDFGeneratorService().generatePDF(session, fc.getSelectedFile());
                showSuccess("PDF Saved Successfully!");
            } catch(Exception e) { showError(e.getMessage()); }
        }
    }
}