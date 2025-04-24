package bill;


import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.awt.print.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

public class BillGenerator extends JFrame {
 private final Bookings booking;
 private final Color primaryColor = new Color(41, 128, 185);
 private final Font headerFont = new Font("Arial", Font.BOLD, 14);
 private final Font labelFont = new Font("Arial", Font.PLAIN, 12);
 
 public BillGenerator(Bookings booking) {
     this.booking = booking;
     
     // Setup frame
     setTitle("Ridhi Sidhi Tours - Bill Preview");
     setLayout(new BorderLayout());
     setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     
     // Create header panel
     JPanel headerPanel = createHeaderPanel();
     add(headerPanel, BorderLayout.NORTH);
     
     // Create bill preview panel
     JPanel billPanel = createBillPanel();
     JScrollPane scrollPane = new JScrollPane(billPanel);
     scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
     scrollPane.setBorder(BorderFactory.createEmptyBorder());
     add(scrollPane, BorderLayout.CENTER);
     
     // Create button panel
     JPanel buttonPanel = createButtonPanel();
     add(buttonPanel, BorderLayout.SOUTH);
     
     // Final setup
     pack();
     setSize(800, 600);
     setLocationRelativeTo(null);
     setVisible(true);
 }
 
 private JPanel createHeaderPanel() {
     JPanel panel = new JPanel();
     panel.setBackground(primaryColor);
     panel.setLayout(new BorderLayout());
     
     JLabel title = new JLabel("Bill Preview", JLabel.CENTER);
     title.setForeground(Color.WHITE);
     title.setFont(new Font("Arial", Font.BOLD, 20));
     title.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
     panel.add(title, BorderLayout.CENTER);
     
     return panel;
 }
 
 private JPanel createBillPanel() {
     JPanel panel = new JPanel();
     panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
     panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
     
     // Company Header
     JPanel companyPanel = new JPanel(new BorderLayout());
     companyPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
     
     JLabel companyName = new JLabel("ABC Tours & Travels", JLabel.CENTER);
     companyName.setFont(new Font("Arial", Font.BOLD, 24));
     companyName.setForeground(primaryColor);
     
     JLabel tagline = new JLabel("Your Journey, Our Responsibility", JLabel.CENTER);
     tagline.setFont(new Font("Arial", Font.ITALIC, 14));
     
     JPanel companyTextPanel = new JPanel();
     companyTextPanel.setLayout(new BoxLayout(companyTextPanel, BoxLayout.Y_AXIS));
     companyTextPanel.add(companyName);
     companyTextPanel.add(Box.createVerticalStrut(5));
     companyTextPanel.add(tagline);
     
     companyPanel.add(companyTextPanel, BorderLayout.CENTER);
     panel.add(companyPanel);
     panel.add(Box.createVerticalStrut(20));
     
     // Add separator
     JSeparator separator = new JSeparator();
     separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
     panel.add(separator);
     panel.add(Box.createVerticalStrut(20));
     
     // Customer Information Section
     JPanel customerPanel = createSectionPanel("Customer Information");
     
     addLabelValueRow(customerPanel, "Customer Name:", booking.getCustomer().getName());
     addLabelValueRow(customerPanel, "Contact Number:", booking.getCustomer().getContactNo());
     addLabelValueRow(customerPanel, "Address:", booking.getCustomer().getAddress());
     
     SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
     addLabelValueRow(customerPanel, "Invoice Number:", booking.getInvoice());
     addLabelValueRow(customerPanel, "Date:", dateFormat.format(booking.getDate()));
     
     panel.add(customerPanel);
     panel.add(Box.createVerticalStrut(20));
     
     // Flight Bookings Section
     if (!booking.getFlights().isEmpty()) {
         JPanel flightPanel = createSectionPanel("Flight Bookings");
         
         // Headers
         JPanel headerRow = new JPanel(new GridLayout(1, 5));
         headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
         
         String[] headers = {"PNR", "From", "To", "Passengers", "Fare (Rs.)"};
         for (String header : headers) {
             JLabel label = new JLabel(header, JLabel.CENTER);
             label.setFont(headerFont);
             headerRow.add(label);
         }
         
         flightPanel.add(headerRow);
         flightPanel.add(Box.createVerticalStrut(10));
         
         // Flight entries
         for (Bookings.FlightBooking flight : booking.getFlights()) {
             JPanel flightRow = new JPanel(new GridLayout(1, 5));
             flightRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
             
             JLabel pnrLabel = new JLabel(flight.getPnr(), JLabel.CENTER);
             JLabel srcLabel = new JLabel(flight.getSrc(), JLabel.CENTER);
             JLabel destLabel = new JLabel(flight.getDest(), JLabel.CENTER);
             JLabel passengersLabel = new JLabel(String.valueOf(flight.getPassengerNumber()), JLabel.CENTER);
             JLabel fareLabel = new JLabel(String.valueOf(flight.getFare()), JLabel.CENTER);
             
             flightRow.add(pnrLabel);
             flightRow.add(srcLabel);
             flightRow.add(destLabel);
             flightRow.add(passengersLabel);
             flightRow.add(fareLabel);
             
             flightPanel.add(flightRow);
             flightPanel.add(Box.createVerticalStrut(5));
         }
         
         // Total airfare
         JPanel airTotalRow = new JPanel(new GridLayout(1, 5));
         airTotalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
         
         JLabel totalLabel = new JLabel("Total Airfare:", JLabel.RIGHT);
         totalLabel.setFont(headerFont);
         
         JLabel totalValue = new JLabel("Rs." + booking.getAirfareTotal(), JLabel.CENTER);
         totalValue.setFont(headerFont);
         
         airTotalRow.add(new JLabel());
         airTotalRow.add(new JLabel());
         airTotalRow.add(new JLabel());
         airTotalRow.add(totalLabel);
         airTotalRow.add(totalValue);
         
         flightPanel.add(Box.createVerticalStrut(10));
         flightPanel.add(airTotalRow);
         
         panel.add(flightPanel);
         panel.add(Box.createVerticalStrut(20));
     }
     
     // Car Bookings Section
     if (!booking.getCars().isEmpty()) {
         JPanel carPanel = createSectionPanel("Car Bookings");
         
         // Headers
         JPanel headerRow = new JPanel(new GridLayout(1, 4));
         headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
         
         String[] headers = {"Car Number", "From", "To", "Fare (Rs.)"};
         for (String header : headers) {
             JLabel label = new JLabel(header, JLabel.CENTER);
             label.setFont(headerFont);
             headerRow.add(label);
         }
         
         carPanel.add(headerRow);
         carPanel.add(Box.createVerticalStrut(10));
         
         // Car entries
         for (Bookings.CarBooking car : booking.getCars()) {
             JPanel carRow = new JPanel(new GridLayout(1, 4));
             carRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
             
             JLabel carNoLabel = new JLabel(car.getCarNo(), JLabel.CENTER);
             JLabel srcLabel = new JLabel(car.getSrc(), JLabel.CENTER);
             JLabel destLabel = new JLabel(car.getDest(), JLabel.CENTER);
             JLabel fareLabel = new JLabel(String.valueOf(car.getFare()), JLabel.CENTER);
             
             carRow.add(carNoLabel);
             carRow.add(srcLabel);
             carRow.add(destLabel);
             carRow.add(fareLabel);
             
             carPanel.add(carRow);
             carPanel.add(Box.createVerticalStrut(5));
         }
         
         // Total carfare
         JPanel carTotalRow = new JPanel(new GridLayout(1, 4));
         carTotalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
         
         JLabel totalLabel = new JLabel("Total Carfare:", JLabel.RIGHT);
         totalLabel.setFont(headerFont);
         
         JLabel totalValue = new JLabel("Rs." + booking.getCarfareTotal(), JLabel.CENTER);
         totalValue.setFont(headerFont);
         
         carTotalRow.add(new JLabel());
         carTotalRow.add(new JLabel());
         carTotalRow.add(totalLabel);
         carTotalRow.add(totalValue);
         
         carPanel.add(Box.createVerticalStrut(10));
         carPanel.add(carTotalRow);
         
         panel.add(carPanel);
         panel.add(Box.createVerticalStrut(20));
     }
     
     // Grand Total Section
     JPanel totalPanel = new JPanel();
     totalPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
     totalPanel.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createMatteBorder(2, 0, 0, 0, Color.BLACK),
         BorderFactory.createEmptyBorder(10, 10, 10, 10)
     ));
     totalPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
     
     JLabel grandTotalLabel = new JLabel("Grand Total: ");
     grandTotalLabel.setFont(new Font("Arial", Font.BOLD, 18));
     
     JLabel grandTotalValue = new JLabel("Rs" + booking.getTotalFare());
     grandTotalValue.setFont(new Font("Arial", Font.BOLD, 18));
     grandTotalValue.setForeground(new Color(0, 100, 0));
     
     totalPanel.add(grandTotalLabel);
     totalPanel.add(grandTotalValue);
     
     panel.add(totalPanel);
     
     // Footer
     JPanel footerPanel = new JPanel();
     footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
     footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
     footerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
     
     JLabel thanksLabel = new JLabel("Thank you for choosing abc Tours & Travels!");
     thanksLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
     thanksLabel.setFont(new Font("Arial", Font.ITALIC, 14));
     
     JLabel addressLabel = new JLabel("123 Travel Street, Tourism City - 400001");
     addressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
     addressLabel.setFont(new Font("Arial", Font.PLAIN, 12));
     
     JLabel contactLabel = new JLabel("Contact: +91-1234567890 | Email:abc@gmail.com");
     contactLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
     contactLabel.setFont(new Font("Arial", Font.PLAIN, 12));
     
     footerPanel.add(thanksLabel);
     footerPanel.add(Box.createVerticalStrut(5));
     footerPanel.add(addressLabel);
     footerPanel.add(Box.createVerticalStrut(3));
     footerPanel.add(contactLabel);
     
     panel.add(Box.createVerticalStrut(20));
     panel.add(footerPanel);
     
     return panel;
 }
 
 private JPanel createSectionPanel(String title) {
     JPanel panel = new JPanel();
     panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
     panel.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createTitledBorder(
             BorderFactory.createLineBorder(primaryColor),
             title,
             TitledBorder.LEFT,
             TitledBorder.TOP,
             headerFont,
             primaryColor
         ),
         BorderFactory.createEmptyBorder(10, 10, 10, 10)
     ));
     panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getMaximumSize().height));
     
     return panel;
 }
 
 private void addLabelValueRow(JPanel panel, String labelText, String value) {
     JPanel row = new JPanel(new BorderLayout(10, 0));
     row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
     
     JLabel label = new JLabel(labelText);
     label.setFont(labelFont);
     label.setPreferredSize(new Dimension(150, label.getPreferredSize().height));
     
     JLabel valueLabel = new JLabel(value);
     valueLabel.setFont(labelFont);
     
     row.add(label, BorderLayout.WEST);
     row.add(valueLabel, BorderLayout.CENTER);
     
     panel.add(row);
     panel.add(Box.createVerticalStrut(5));
 }
 
 private JPanel createButtonPanel() {
     JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
     panel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));
     
     JButton printButton = new JButton("Print");
     printButton.setFont(labelFont);
     printButton.setFocusPainted(false);
     printButton.addActionListener(e -> printBill());
     
     JButton saveButton = new JButton("Save PDF");
     saveButton.setFont(labelFont);
     saveButton.setFocusPainted(false);
     saveButton.addActionListener(e -> generatePDF());
     
     JButton newBookingButton = new JButton("New Booking");
     newBookingButton.setFont(labelFont);
     newBookingButton.setBackground(primaryColor);
     newBookingButton.setForeground(Color.WHITE);
     newBookingButton.setFocusPainted(false);
     newBookingButton.addActionListener(e -> startNewBooking());
     
     panel.add(printButton);
     panel.add(saveButton);
     panel.add(newBookingButton);
     
     return panel;
 }
 
 private void generatePDF() {
	    try {
	        JFileChooser fileChooser = new JFileChooser();
	        fileChooser.setDialogTitle("Save PDF File");
	        fileChooser.setSelectedFile(new File("Invoice_" + booking.getInvoice() + ".pdf"));
	        
	        int userSelection = fileChooser.showSaveDialog(this);
	        
	        if (userSelection == JFileChooser.APPROVE_OPTION) {
	            File fileToSave = fileChooser.getSelectedFile();
	            
	            // Create PDF document
	            PDDocument document = new PDDocument();
	            PDPage page = new PDPage(PDRectangle.A4);
	            document.addPage(page);
	            
	            PDPageContentStream contentStream = new PDPageContentStream(document, page);
	            
	            // Define colors
	            PDColor primaryBlue = new PDColor(new float[]{0.27f, 0.51f, 0.71f}, PDDeviceRGB.INSTANCE);
	            PDColor accentOrange = new PDColor(new float[]{0.99f, 0.65f, 0.0f}, PDDeviceRGB.INSTANCE);
	            PDColor lightPeach = new PDColor(new float[]{0.99f, 0.87f, 0.75f}, PDDeviceRGB.INSTANCE);
	            
	            // Draw background
	            contentStream.setNonStrokingColor(lightPeach);
	            contentStream.addRect(0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
	            contentStream.fill();
	            
	            // Add header image - note: in a real app, you'd use PDImageXObject to load your logo
	            // For this example we'll draw a simple decorative element
	            contentStream.setNonStrokingColor(primaryBlue);
	            contentStream.addRect(30, 750, 70, 70);
	            contentStream.fill();
	            
	            contentStream.setNonStrokingColor(accentOrange);
	            contentStream.addRect(60, 730, 70, 30);
	            contentStream.fill();
	            
	            // Add company name
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
	            contentStream.setNonStrokingColor(accentOrange);
	            contentStream.newLineAtOffset(160, 780);
	            contentStream.showText("TRAVEL AGENCY");
	            contentStream.endText();
	            
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
	            contentStream.setNonStrokingColor(primaryBlue);
	            contentStream.newLineAtOffset(160, 755);
	            contentStream.showText("Ridhi Sidhi Tours & Travels");
	            contentStream.endText();
	            
	            // Add invoice header
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
	            contentStream.setNonStrokingColor(primaryBlue);
	            contentStream.newLineAtOffset(60, 700);
	            contentStream.showText("INVOICE TO");
	            contentStream.endText();
	            
	            // Invoice details section
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
	            contentStream.setNonStrokingColor(0, 0, 0);
	            contentStream.newLineAtOffset(60, 680);
	            contentStream.showText("Name");
	            contentStream.newLineAtOffset(0, -20);
	            contentStream.showText("Address");
	            contentStream.endText();
	            
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
	            contentStream.newLineAtOffset(120, 680);
	            contentStream.showText(": " + booking.getCustomer().getName());
	            contentStream.newLineAtOffset(0, -20);
	            contentStream.showText(": " + booking.getCustomer().getAddress());
	            contentStream.endText();
	            
	            // Invoice number and date
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
	            contentStream.setNonStrokingColor(primaryBlue);
	            contentStream.newLineAtOffset(400, 680);
	            contentStream.showText("Invoice No.");
	            contentStream.newLineAtOffset(0, -20);
	            contentStream.showText("Date");
	            contentStream.endText();
	            
	            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
	            
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
	            contentStream.setNonStrokingColor(0, 0, 0);
	            contentStream.newLineAtOffset(480, 680);
	            contentStream.showText(": " + booking.getInvoice());
	            contentStream.newLineAtOffset(0, -20);
	            contentStream.showText(": " + dateFormat.format(booking.getDate()));
	            contentStream.endText();
	            
	            // Draw table headers
	            float yStart = 600;
	            float tableWidth = 500;
	            float rowHeight = 30;
	            float margin = 60;
	            String[] headers = {"ITEM DESCRIPTION", "PRICE", "QTY.", "TOTAL"};
	            float[] columnWidths = {tableWidth * 0.5f, tableWidth * 0.16f, tableWidth * 0.16f, tableWidth * 0.18f};
	            
	            // Draw table header background
	            contentStream.setNonStrokingColor(primaryBlue);
	            contentStream.addRect(margin, yStart - rowHeight, tableWidth, rowHeight);
	            contentStream.fill();
	            
	            // Add table headers
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
	            contentStream.setNonStrokingColor(1, 1, 1); // White text
	            
	            float xPosition = margin + 10;
	            for (int i = 0; i < headers.length; i++) {
	                contentStream.newLineAtOffset(i == 0 ? xPosition : columnWidths[i-1], i == 0 ? yStart - 20 : 0);
	                contentStream.showText(headers[i]);
	                xPosition = 0; // Reset for relative positioning
	            }
	            contentStream.endText();
	            
	            // Calculate available space for rows and determine if we need to add flight and car bookings
	            float yPosition = yStart - rowHeight;
	            
	            // Flight bookings
	            if (!booking.getFlights().isEmpty()) {
	                float flightYStart = yPosition;
	                
	                // Add flight items to the table
	                for (Bookings.FlightBooking flight : booking.getFlights()) {
	                    // Draw alternating row background
	                    if ((flightYStart - yPosition) / rowHeight % 2 == 1) {
	                        contentStream.setNonStrokingColor(0.9f, 0.9f, 0.9f); // Light gray
	                        contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
	                        contentStream.fill();
	                    }
	                    
	                    // Add row data
	                    contentStream.beginText();
	                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
	                    contentStream.setNonStrokingColor(0, 0, 0);
	                    
	                    // Description column
	                    String description = "Flight: " + flight.getSrc() + " to " + flight.getDest() + " (PNR: " + flight.getPnr() + ")";
	                    contentStream.newLineAtOffset(margin + 10, yPosition - 20);
	                    contentStream.showText(description);
	                    
	                    // Price column
	                    float unitPrice = (float)flight.getFare() / flight.getPassengerNumber();
	                    contentStream.newLineAtOffset(columnWidths[0] - 10, 0);
	                    contentStream.showText(String.format("Rs. %.2f", unitPrice));
	                    
	                    // Quantity column
	                    contentStream.newLineAtOffset(columnWidths[1], 0);
	                    contentStream.showText(String.valueOf(flight.getPassengerNumber()));
	                    
	                    // Total column
	                    contentStream.newLineAtOffset(columnWidths[2], 0);
	                    contentStream.showText(String.format("Rs. %.2f", (float)flight.getFare())); 
	                    
	                    contentStream.endText();
	                    
	                    yPosition -= rowHeight;
	                }
	            }
	            
	            // Car bookings
	            if (!booking.getCars().isEmpty()) {
	                float carYStart = yPosition;
	                
	                // Add car items to the table
	                for (Bookings.CarBooking car : booking.getCars()) {
	                    // Draw alternating row background
	                    if ((carYStart - yPosition) / rowHeight % 2 == 1) {
	                        contentStream.setNonStrokingColor(0.9f, 0.9f, 0.9f); // Light gray
	                        contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
	                        contentStream.fill();
	                    }
	                    
	                    // Add row data
	                    contentStream.beginText();
	                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
	                    contentStream.setNonStrokingColor(0, 0, 0);
	                    
	                    // Description column
	                    String description = "Car: " + car.getSrc() + " to " + car.getDest() + " (Car No: " + car.getCarNo() + ")";
	                    contentStream.newLineAtOffset(margin + 10, yPosition - 20);
	                    contentStream.showText(description);
	                    
	                    // Price column
	                    contentStream.newLineAtOffset(columnWidths[0] - 10, 0);
	                    contentStream.showText(String.format("Rs. %.2f", (float)car.getFare())); 
	                    
	                    // Quantity column
	                    contentStream.newLineAtOffset(columnWidths[1], 0);
	                    contentStream.showText("1");
	                    
	                    // Total column
	                    contentStream.newLineAtOffset(columnWidths[2], 0);
	                    contentStream.showText(String.format("Rs. %.2f", (float)car.getFare())); 
	                    
	                    contentStream.endText();
	                    
	                    yPosition -= rowHeight;
	                }
	            }
	            
	            // Draw total section
	            float totalSectionY = yPosition - 20;
	            
	            // Subtotal
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
	            contentStream.setNonStrokingColor(0, 0, 0);
	            contentStream.newLineAtOffset(margin + columnWidths[0] + columnWidths[1], totalSectionY);
	            contentStream.showText("Subtotal");
	            contentStream.endText();
	            
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
	            contentStream.setNonStrokingColor(0, 0, 0);
	            contentStream.newLineAtOffset(margin + columnWidths[0] + columnWidths[1] + columnWidths[2], totalSectionY);
	            String subtotalValue = String.format("Rs. %.2f", (float)booking.getTotalFare());
	            contentStream.showText(subtotalValue);
	            contentStream.endText();
	            
	            // Tax (if applicable)
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
	            contentStream.setNonStrokingColor(0, 0, 0);
	            contentStream.newLineAtOffset(margin + columnWidths[0] + columnWidths[1], totalSectionY - 20);
	            contentStream.showText("Tax Rate");
	            contentStream.endText();
	            
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
	            contentStream.setNonStrokingColor(0, 0, 0);
	            contentStream.newLineAtOffset(margin + columnWidths[0] + columnWidths[1] + columnWidths[2], totalSectionY - 20);
	            contentStream.showText("0%");
	            contentStream.endText();
	            
	            // Draw orange background for total
	            contentStream.setNonStrokingColor(accentOrange);
	            contentStream.addRect(margin + columnWidths[0], totalSectionY - 60, columnWidths[1] + columnWidths[2] + columnWidths[3] - columnWidths[0], 30);
	            contentStream.fill();
	            
	            // Total
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
	            contentStream.setNonStrokingColor(1, 1, 1); // White text
	            contentStream.newLineAtOffset(margin + columnWidths[0] + columnWidths[1] - 20, totalSectionY - 50);
	            contentStream.showText("TOTAL");
	            contentStream.endText();
	            
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
	            contentStream.setNonStrokingColor(1, 1, 1); // White text
	            contentStream.newLineAtOffset(margin + columnWidths[0] + columnWidths[1] + columnWidths[2] - 15, totalSectionY - 50);
	            contentStream.showText(String.format("Rs. %.2f", (float)booking.getTotalFare()));
	            contentStream.endText();
	            
	            // Payment information
	            float paymentInfoY = totalSectionY - 100;
	            
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
	            contentStream.setNonStrokingColor(primaryBlue);
	            contentStream.newLineAtOffset(60, paymentInfoY);
	            contentStream.showText("PAYMENT INFORMATION :");
	            contentStream.endText();
	            
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
	            contentStream.setNonStrokingColor(0, 0, 0);
	            contentStream.newLineAtOffset(60, paymentInfoY - 25);
	            contentStream.showText("Account No");
	            contentStream.newLineAtOffset(0, -20);
	            contentStream.showText("Name");
	            contentStream.newLineAtOffset(0, -20);
	            contentStream.showText("Bank Detail");
	            contentStream.endText();
	            
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
	            contentStream.setNonStrokingColor(0, 0, 0);
	            contentStream.newLineAtOffset(150, paymentInfoY - 25);
	            contentStream.showText(": " + booking.getInvoice());  // Using invoice as account
	            contentStream.newLineAtOffset(0, -20);
	            contentStream.showText(": ABC Tours & Travels");
	            contentStream.newLineAtOffset(0, -20);
	            contentStream.showText(": XYZ Bank");
	            contentStream.endText();
	            
	            // Add signature line
	            contentStream.setStrokingColor(0, 0, 0);
	            contentStream.setLineWidth(1);
	            contentStream.moveTo(400, paymentInfoY - 65);
	            contentStream.lineTo(530, paymentInfoY - 65);
	            contentStream.stroke();
	            
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
	            contentStream.setNonStrokingColor(primaryBlue);
	            contentStream.newLineAtOffset(430, paymentInfoY - 80);
	            contentStream.showText("Authorised Sign");
	            contentStream.endText();
	            
	            // Add footer
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
	            contentStream.setNonStrokingColor(primaryBlue);
	            contentStream.newLineAtOffset(180, 60);
	            contentStream.showText("Thank you for choosing Ridhi Sidhi Tours & Travels!");
	            contentStream.endText();
	            
	            contentStream.beginText();
	            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
	            contentStream.setNonStrokingColor(0, 0, 0);
	            contentStream.newLineAtOffset(150, 40);
	            contentStream.showText("123 Travel Street, Tourism City - 400001");
	            contentStream.newLineAtOffset(10, -15);
	            contentStream.showText("Contact: +91-1234567890 | Email: info@ridhisidhitours.com");
	            contentStream.endText();
	            
	            // Close the content stream and save
	            contentStream.close();
	            document.save(fileToSave);
	            document.close();
	            
	            JOptionPane.showMessageDialog(this,
	                "PDF saved successfully to: " + fileToSave.getAbsolutePath(),
	                "PDF Saved",
	                JOptionPane.INFORMATION_MESSAGE);
	        }
	    } catch (IOException e) {
	        JOptionPane.showMessageDialog(this,
	            "Error saving PDF: " + e.getMessage(),
	            "Error",
	            JOptionPane.ERROR_MESSAGE);
	        e.printStackTrace();
	    }
	}
 private String generateBillText() {
     // This method would generate the bill content in a text format
     // In a real application, this would generate PDF content
     StringBuilder sb = new StringBuilder();
     SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
     
     sb.append("Ridhi Sidhi Tours & Travels\n");
     sb.append("Your Journey, Our Responsibility\n\n");
     
     sb.append("Customer Information:\n");
     sb.append("Customer Name: ").append(booking.getCustomer().getName()).append("\n");
     sb.append("Contact Number: ").append(booking.getCustomer().getContactNo()).append("\n");
     sb.append("Address: ").append(booking.getCustomer().getAddress()).append("\n");
     sb.append("Invoice Number: ").append(booking.getInvoice()).append("\n");
     sb.append("Date: ").append(dateFormat.format(booking.getDate())).append("\n\n");
     
     if (!booking.getFlights().isEmpty()) {
         sb.append("Flight Bookings:\n");
         sb.append("PNR\tFrom\tTo\tPassengers\tFare (Rs.)\n");
         
         for (Bookings.FlightBooking flight : booking.getFlights()) {
             sb.append(flight.getPnr()).append("\t");
             sb.append(flight.getSrc()).append("\t");
             sb.append(flight.getDest()).append("\t");
             sb.append(flight.getPassengerNumber()).append("\t");
             sb.append(flight.getFare()).append("\n");
         }
         
         sb.append("Total Airfare: Rs.").append(booking.getAirfareTotal()).append("\n\n");
     }
     
     if (!booking.getCars().isEmpty()) {
         sb.append("Car Bookings:\n");
         sb.append("Car Number\tFrom\tTo\tFare (Rs.)\n");
         
         for (Bookings.CarBooking car : booking.getCars()) {
             sb.append(car.getCarNo()).append("\t");
             sb.append(car.getSrc()).append("\t");
             sb.append(car.getDest()).append("\t");
             sb.append(car.getFare()).append("\n");
         }
         
         sb.append("Total Carfare: Rs.").append(booking.getCarfareTotal()).append("\n\n");
     }
     
     sb.append("Grand Total: Rs.").append(booking.getTotalFare()).append("\n\n");
     
     sb.append("Thank you for choosing Ridhi Sidhi Tours & Travels!\n");
     sb.append("123 Travel Street, Tourism City - 400001\n");
     sb.append("Contact: +91-1234567890 | Email: info@ridhisidhitours.com");
     
     return sb.toString();
 }
 
 private void printBill() {
     try {
         // Set up Printable
         Printable printable = new Printable() {
             @Override
             public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
                 if (pageIndex > 0) {
                     return Printable.NO_SUCH_PAGE;
                 }
                 
                 Graphics2D g2d = (Graphics2D) g;
                 g2d.translate(pf.getImageableX(), pf.getImageableY());
                 
                 // Scale to fit the page
                 double pageWidth = pf.getImageableWidth();
                 double pageHeight = pf.getImageableHeight();
                 double componentWidth = getContentPane().getWidth();
                 double componentHeight = getContentPane().getHeight();
                 double scaleX = pageWidth / componentWidth;
                 double scaleY = pageHeight / componentHeight;
                 double scale = Math.min(scaleX, scaleY);
                 
                 g2d.scale(scale, scale);
                 
                 // Print component
                 getContentPane().print(g2d);
                 
                 return Printable.PAGE_EXISTS;
             }
         };
         
         // Show print dialog
         PrinterJob job = PrinterJob.getPrinterJob();
         job.setPrintable(printable);
         
         if (job.printDialog()) {
             job.print();
         }
     } catch (PrinterException ex) {
         JOptionPane.showMessageDialog(this,
             "Error printing: " + ex.getMessage(),
             "Print Error",
             JOptionPane.ERROR_MESSAGE);
         ex.printStackTrace();
     }
 }
 
 private void startNewBooking() {
     new TicketDisplay();
     dispose();
 }
}
