package bill;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * History screen — lists previously saved session (.rss) files from the most
 * recently used directory. Users can open a session to view its invoice.
 *
 * Since the app stores sessions as serialized files, history is directory-based:
 * the user picks a folder and all .rss files within are listed.
 */
public class HistoryPanel extends BasePanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private final List<File> sessionFiles = new ArrayList<>();
    private JLabel lblDir;

    public HistoryPanel(MainApp controller, BookingSession session) {
        super(controller, session);
        setName(MainApp.HISTORY_PANEL);
        initUI();
    }

    private void initUI() {
        add(createTitlePanel("Invoice History", "Browse and reopen saved sessions"), BorderLayout.NORTH);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 40, 0, 40));

        lblDir = new JLabel("No folder selected");
        lblDir.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblDir.setForeground(TEXT_MUTED);

        JButton btnBrowse = createStyledButton("Browse Folder", PRIMARY_COLOR, false);
        btnBrowse.addActionListener(e -> browseFolder());

        JButton btnBack = createStyledButton("← Back", TEXT_MUTED, false);
        btnBack.addActionListener(e -> controller.showScreen(MainApp.CUSTOMER_PANEL));

        toolbar.add(btnBrowse);
        toolbar.add(lblDir);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(btnBack);
        add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Invoice No", "Client", "Date", "Amount", "File"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);
        table.getColumnModel().getColumn(4).setMinWidth(180);

        // Double-click to open
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openSelectedSession();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setOpaque(false);
        tableWrapper.setBorder(new EmptyBorder(10, 40, 10, 40));
        tableWrapper.add(scroll, BorderLayout.CENTER);

        // Bottom open button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 40, 20, 40));

        JButton btnOpen = createStyledButton("Open Selected", SUCCESS_COLOR, true);
        btnOpen.addActionListener(e -> openSelectedSession());
        bottom.add(btnOpen);

        add(tableWrapper, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    // Called by MainApp when navigating to this screen
    public void refresh() {
        // No-op; user must browse a folder explicitly
    }

    private void browseFolder() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Select folder containing saved sessions");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File dir = fc.getSelectedFile();
        lblDir.setText(dir.getAbsolutePath());
        loadSessionFiles(dir);
    }

    private void loadSessionFiles(File dir) {
        sessionFiles.clear();
        tableModel.setRowCount(0);

        File[] files = dir.listFiles((d, name) -> name.endsWith(".rss"));
        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(this,
                "No saved sessions (.rss files) found in this folder.",
                "No Sessions", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        for (File f : files) {
            try {
                BookingSession s = BookingSession.loadFrom(f);
                sessionFiles.add(f);
                tableModel.addRow(new Object[]{
                    s.getInvoiceNumber(),
                    s.getCustomerName(),
                    sdf.format(s.getBookingDate()),
                    CURRENCY + " " + s.getTotalAmount().toPlainString(),
                    f.getName()
                });
            } catch (Exception ignored) {
                // Skip corrupt or incompatible files
            }
        }
    }

    private void openSelectedSession() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarning("Please select a session to open.");
            return;
        }
        File file = sessionFiles.get(row);
        try {
            BookingSession loaded = BookingSession.loadFrom(file);
            // Copy into shared session
            session.reset();
            session.setCustomerDetails(
                loaded.getCustomerName(), loaded.getContactNumber(),
                loaded.getAddress(), loaded.getInvoiceNumber(), loaded.getBookingDate());
            session.setCompanyDetails(
                loaded.getCompanyName(), loaded.getCompanyTagline(),
                loaded.getCompanyEmail(), loaded.getCompanyPhone());
            session.setGstRate(loaded.getGstRatePercent());
            for (BookingSession.FlightBookingData f : loaded.getFlights())
                session.addFlight(f.pnr, f.source, f.dest, f.fare, f.passengers);
            for (BookingSession.CarBookingData c : loaded.getCars())
                session.addCar(c.carNo, c.source, c.dest, c.fare);

            controller.showScreen(MainApp.BILL_PANEL);
        } catch (Exception ex) {
            showError("Could not open session: " + ex.getMessage());
        }
    }
}
