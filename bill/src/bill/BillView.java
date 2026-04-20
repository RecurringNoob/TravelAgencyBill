package bill;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Step 4 — Invoice preview and PDF export.
 *
 * Improvements:
 *  - Unified currency symbol (uses BasePanel.CURRENCY)
 *  - GST line item in both text preview and grand total
 *  - Fixed-width column formatting to handle long names without breaking alignment
 *  - Sidebar shows subtotal, GST, and grand total separately
 */
public class BillView extends BasePanel {

    private JTextArea previewArea;
    private JLabel    lblSubtotal, lblGst, lblTotal;

    public BillView(MainApp controller, BookingSession session) {
        super(controller, session);
        setName(MainApp.BILL_PANEL);
        initUI();
    }

    private void initUI() {
        add(createTitlePanel("Invoice Preview", "Review and export your invoice"), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(20, 0));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(0, 40, 20, 40));

        // ---- LEFT: Paper invoice ----
        JPanel paperPanel = new JPanel(new BorderLayout());
        paperPanel.setBackground(Color.WHITE);
        paperPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(30, 30, 30, 30)));

        previewArea = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        previewArea.setBackground(Color.WHITE);
        previewArea.setForeground(TEXT_COLOR);

        JScrollPane scroll = new JScrollPane(previewArea);
        scroll.setBorder(null);
        paperPanel.add(scroll, BorderLayout.CENTER);

        // ---- RIGHT: Sidebar ----
        CardResult cr      = createCard();
        JPanel sidebar     = cr.panel;
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(260, 0));

        // Totals section
        sidebar.add(sidebarLabel("SUBTOTAL",  TEXT_MUTED));
        lblSubtotal = sidebarAmount("Rs. 0.00", TEXT_COLOR, 18);
        sidebar.add(lblSubtotal);
        sidebar.add(Box.createVerticalStrut(8));

        sidebar.add(sidebarLabel("GST (18%)", TEXT_MUTED));
        lblGst = sidebarAmount("Rs. 0.00", TEXT_MUTED, 16);
        sidebar.add(lblGst);
        sidebar.add(Box.createVerticalStrut(12));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(12));

        sidebar.add(sidebarLabel("GRAND TOTAL", TEXT_MUTED));
        lblTotal = sidebarAmount("Rs. 0.00", SUCCESS_COLOR, 28);
        sidebar.add(lblTotal);

        sidebar.add(Box.createVerticalStrut(30));

        // Action buttons
        JButton btnPDF = createStyledButton("⬇  Save as PDF", PRIMARY_COLOR, true);
        btnPDF.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPDF.addActionListener(e -> savePDF());
        sidebar.add(btnPDF);

        sidebar.add(Box.createVerticalStrut(10));

        JButton btnNew = createStyledButton("New Booking", WARNING_COLOR, false);
        btnNew.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnNew.addActionListener(e -> {
            if (confirm("Start a new booking? Current data will be lost."))
                controller.resetBooking();
        });
        sidebar.add(btnNew);

        sidebar.add(Box.createVerticalStrut(10));

        JButton btnBack = createStyledButton("← Back", TEXT_MUTED, false);
        btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBack.addActionListener(e -> controller.showScreen(MainApp.CAR_PANEL));
        sidebar.add(btnBack);

        mainContent.add(paperPanel, BorderLayout.CENTER);
        mainContent.add(sidebar, BorderLayout.EAST);
        add(mainContent, BorderLayout.CENTER);
    }

    // -------------------------------------------------------------------------
    // Refresh
    // -------------------------------------------------------------------------

    /** Called by MainApp every time this panel becomes visible. */
    public void refresh() {
        previewArea.setText(buildInvoiceText());
        previewArea.setCaretPosition(0);

        lblSubtotal.setText(CURRENCY + " " + session.getSubtotal().toPlainString());
        lblGst.setText(CURRENCY + " " + session.getGstAmount().toPlainString()
                       + "  (" + session.getGstRatePercent().toPlainString() + "%)");
        lblTotal.setText(CURRENCY + " " + session.getTotalAmount().toPlainString());
    }

    private String buildInvoiceText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String line70  = "-".repeat(70) + "\n";
        String line70b = "=".repeat(70) + "\n";

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append(centred(session.getCompanyName(), 70)).append("\n");
        sb.append(centred(session.getCompanyTagline(), 70)).append("\n");
        sb.append(centred(session.getCompanyPhone() + "  |  " + session.getCompanyEmail(), 70)).append("\n");
        sb.append(line70b);

        // Invoice meta
        sb.append(String.format(" Invoice No : %-30s Date : %s\n",
            session.getInvoiceNumber(), sdf.format(session.getBookingDate())));
        sb.append(String.format(" Client     : %-30s\n", truncate(session.getCustomerName(), 30)));
        if (!session.getContactNumber().isEmpty())
            sb.append(String.format(" Contact    : %s\n", session.getContactNumber()));
        if (!session.getAddress().isEmpty())
            sb.append(String.format(" Address    : %s\n", session.getAddress()));
        sb.append(line70);

        // Column header
        sb.append(String.format(" %-12s  %-25s  %6s  %12s\n", "REF", "ROUTE", "PAX", "AMOUNT"));
        sb.append(line70);

        // Flights
        if (!session.getFlights().isEmpty()) {
            sb.append(" FLIGHT BOOKINGS\n");
            for (BookingSession.FlightBookingData f : session.getFlights()) {
                String route = truncate(f.source + " \u2192 " + f.dest, 25);
                sb.append(String.format(" %-12s  %-25s  %6d  %12s\n",
                    truncate(f.pnr, 12), route, f.passengers,
                    CURRENCY + " " + f.fare.toPlainString()));
            }
            sb.append(String.format(" %47s  %12s\n", "Flight subtotal :",
                CURRENCY + " " + session.getFlightTotal().toPlainString()));
            sb.append("\n");
        }

        // Cars
        if (!session.getCars().isEmpty()) {
            sb.append(" CAR RENTALS\n");
            for (BookingSession.CarBookingData c : session.getCars()) {
                String route = truncate(c.source + " \u2192 " + c.dest, 25);
                sb.append(String.format(" %-12s  %-25s  %6s  %12s\n",
                    truncate(c.carNo, 12), route, "",
                    CURRENCY + " " + c.fare.toPlainString()));
            }
            sb.append(String.format(" %47s  %12s\n", "Car subtotal :",
                CURRENCY + " " + session.getCarTotal().toPlainString()));
            sb.append("\n");
        }

        // Totals
        sb.append(line70);
        sb.append(String.format(" %47s  %12s\n", "Subtotal :",
            CURRENCY + " " + session.getSubtotal().toPlainString()));
        sb.append(String.format(" %47s  %12s\n",
            "GST @ " + session.getGstRatePercent().toPlainString() + "% :",
            CURRENCY + " " + session.getGstAmount().toPlainString()));
        sb.append(line70b);
        sb.append(String.format(" %47s  %12s\n", "GRAND TOTAL :",
            CURRENCY + " " + session.getTotalAmount().toPlainString()));
        sb.append(line70b);

        // Footer
        sb.append("\n Payment Terms: Please remit within 15 days of invoice date.\n");
        sb.append(" Thank you for choosing ").append(session.getCompanyName()).append("!\n");
        sb.append("\n").append(centred("* This is a computer-generated invoice *", 70)).append("\n");

        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // PDF Export
    // -------------------------------------------------------------------------

    private void savePDF() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Invoice as PDF");
        fc.setSelectedFile(new File("Invoice_" + session.getInvoiceNumber() + ".pdf"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                new PDFGeneratorService().generatePDF(session, fc.getSelectedFile());
                showSuccess("PDF saved successfully to:\n" + fc.getSelectedFile().getAbsolutePath());
            } catch (Exception e) {
                showError("PDF generation failed:\n" + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String centred(String text, int width) {
        if (text.length() >= width) return text;
        int pad = (width - text.length()) / 2;
        return " ".repeat(pad) + text;
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 1) + "…";
    }

    private JLabel sidebarLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(LABEL_FONT);
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JLabel sidebarAmount(String text, Color color, int fontSize) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }
}
