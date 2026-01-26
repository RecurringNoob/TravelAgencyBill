package bill;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class PDFGeneratorService {

    private static final PDColor PRIMARY_BLUE = new PDColor(new float[]{0.16f, 0.5f, 0.72f}, PDDeviceRGB.INSTANCE);
    private static final PDColor SUCCESS_GREEN = new PDColor(new float[]{0.18f, 0.8f, 0.44f}, PDDeviceRGB.INSTANCE);
    private static final PDColor DARK_GREY = new PDColor(new float[]{0.2f, 0.29f, 0.37f}, PDDeviceRGB.INSTANCE);
    private static final PDColor LIGHT_GREY = new PDColor(new float[]{0.96f, 0.97f, 0.98f}, PDDeviceRGB.INSTANCE);

    public void generatePDF(BookingSession session, File file) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float pageWidth = page.getMediaBox().getWidth();
                float margin = 40;
                float yPosition = 750;

                // 1. Header Background
                cs.setNonStrokingColor(PRIMARY_BLUE);
                cs.addRect(0, yPosition, pageWidth, 92);
                cs.fill();

                // 2. Company Logo (REPLACED EMOJI WITH TEXT "RST")
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 36);
                cs.setNonStrokingColor(1, 1, 1);
                cs.newLineAtOffset(margin, yPosition + 50);
                cs.showText("RST"); // Changed from ✈ to RST
                cs.endText();

                // 3. Company Name
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 26);
                cs.newLineAtOffset(margin + 85, yPosition + 52); // Adjusted offset slightly
                cs.showText("RIDHI SIDHI TOURS");
                cs.endText();

                // 4. Tagline
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                cs.newLineAtOffset(margin + 85, yPosition + 35);
                cs.showText("Your Journey, Our Responsibility");
                cs.endText();

                // 5. Contact Info
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                cs.newLineAtOffset(margin + 85, yPosition + 18);
                cs.showText("Email: info@ridhisidhitours.com  |  Phone: +91-XXXXXXXXXX");
                cs.endText();

                yPosition = 720;

                // 6. Invoice Title
                cs.setNonStrokingColor(DARK_GREY);
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 22);
                cs.newLineAtOffset(margin, yPosition);
                cs.showText("INVOICE");
                cs.endText();

                // 7. Horizontal line
                yPosition -= 10;
                cs.setStrokingColor(PRIMARY_BLUE);
                cs.setLineWidth(2);
                cs.moveTo(margin, yPosition);
                cs.lineTo(pageWidth - margin, yPosition);
                cs.stroke();

                yPosition -= 25;

                // 8. Invoice Info (Left) and Customer Info (Right)
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                
                cs.setNonStrokingColor(0, 0, 0);
                writeText(cs, "Invoice No:", margin, yPosition, 11, true);
                writeText(cs, session.getInvoiceNumber(), margin + 80, yPosition, 11, false);
                
                writeText(cs, "Date:", margin, yPosition - 16, 11, true);
                writeText(cs, sdf.format(session.getBookingDate()), margin + 80, yPosition - 16, 11, false);

                // Customer Info (Right side)
                float rightColumn = pageWidth - margin - 200;
                writeText(cs, "Bill To:", rightColumn, yPosition, 11, true);
                writeText(cs, session.getCustomerName(), rightColumn, yPosition - 16, 11, false);
                
                if(!session.getContactNumber().isEmpty()) {
                    writeText(cs, session.getContactNumber(), rightColumn, yPosition - 32, 10, false);
                }
                if(!session.getAddress().isEmpty()) {
                    writeText(cs, session.getAddress(), rightColumn, yPosition - 48, 10, false);
                }

                yPosition -= 80;

                // 9. Flight Bookings Section
                if(!session.getFlights().isEmpty()) {
                    // Section header
                    cs.setNonStrokingColor(PRIMARY_BLUE);
                    cs.addRect(margin, yPosition - 5, pageWidth - 2*margin, 22);
                    cs.fill();
                    
                    cs.setNonStrokingColor(1, 1, 1);
                    writeText(cs, "FLIGHT BOOKINGS", margin + 8, yPosition + 2, 12, true);
                    
                    yPosition -= 30;

                    // Table header
                    cs.setNonStrokingColor(LIGHT_GREY);
                    cs.addRect(margin, yPosition - 3, pageWidth - 2*margin, 18);
                    cs.fill();

                    cs.setNonStrokingColor(DARK_GREY);
                    writeText(cs, "PNR", margin + 8, yPosition + 2, 10, true);
                    writeText(cs, "Route", margin + 100, yPosition + 2, 10, true);
                    writeText(cs, "Passengers", margin + 280, yPosition + 2, 10, true);
                    writeText(cs, "Fare", pageWidth - margin - 70, yPosition + 2, 10, true);

                    yPosition -= 22;

                    // Flight items
                    cs.setNonStrokingColor(0, 0, 0);
                    for (BookingSession.FlightBookingData f : session.getFlights()) {
                        writeText(cs, f.pnr, margin + 8, yPosition, 10, false);
                        writeText(cs, f.source + " -> " + f.dest, margin + 100, yPosition, 10, false);
                        writeText(cs, String.valueOf(f.passengers), margin + 295, yPosition, 10, false);
                        writeText(cs, "Rs. " + f.fare, pageWidth - margin - 70, yPosition, 10, false);
                        yPosition -= 18;
                    }

                    // Subtotal
                    yPosition -= 5;
                    cs.setStrokingColor(LIGHT_GREY);
                    cs.setLineWidth(1);
                    cs.moveTo(margin, yPosition);
                    cs.lineTo(pageWidth - margin, yPosition);
                    cs.stroke();
                    
                    yPosition -= 15;
                    writeText(cs, "Subtotal:", pageWidth - margin - 150, yPosition, 11, true);
                    writeText(cs, "Rs. " + session.getFlightTotal(), pageWidth - margin - 70, yPosition, 11, true);

                    yPosition -= 25;
                }

                // 10. Car Rentals Section
                if(!session.getCars().isEmpty()) {
                    // Section header
                    cs.setNonStrokingColor(PRIMARY_BLUE);
                    cs.addRect(margin, yPosition - 5, pageWidth - 2*margin, 22);
                    cs.fill();
                    
                    cs.setNonStrokingColor(1, 1, 1);
                    writeText(cs, "CAR RENTALS", margin + 8, yPosition + 2, 12, true);
                    
                    yPosition -= 30;

                    // Table header
                    cs.setNonStrokingColor(LIGHT_GREY);
                    cs.addRect(margin, yPosition - 3, pageWidth - 2*margin, 18);
                    cs.fill();

                    cs.setNonStrokingColor(DARK_GREY);
                    writeText(cs, "Car Number", margin + 8, yPosition + 2, 10, true);
                    writeText(cs, "Route", margin + 150, yPosition + 2, 10, true);
                    writeText(cs, "Fare", pageWidth - margin - 70, yPosition + 2, 10, true);

                    yPosition -= 22;

                    // Car items
                    cs.setNonStrokingColor(0, 0, 0);
                    for (BookingSession.CarBookingData c : session.getCars()) {
                        writeText(cs, c.carNo, margin + 8, yPosition, 10, false);
                        writeText(cs, c.source + " -> " + c.dest, margin + 150, yPosition, 10, false);
                        writeText(cs, "Rs. " + c.fare, pageWidth - margin - 70, yPosition, 10, false);
                        yPosition -= 18;
                    }

                    // Subtotal
                    yPosition -= 5;
                    cs.setStrokingColor(LIGHT_GREY);
                    cs.setLineWidth(1);
                    cs.moveTo(margin, yPosition);
                    cs.lineTo(pageWidth - margin, yPosition);
                    cs.stroke();
                    
                    yPosition -= 15;
                    writeText(cs, "Subtotal:", pageWidth - margin - 150, yPosition, 11, true);
                    writeText(cs, "Rs. " + session.getCarTotal(), pageWidth - margin - 70, yPosition, 11, true);

                    yPosition -= 25;
                }

                // 11. Grand Total
                yPosition -= 10;
                cs.setNonStrokingColor(SUCCESS_GREEN);
                cs.addRect(margin, yPosition - 8, pageWidth - 2*margin, 35);
                cs.fill();

                cs.setNonStrokingColor(1, 1, 1);
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                cs.newLineAtOffset(pageWidth - margin - 250, yPosition + 7);
                cs.showText("GRAND TOTAL:");
                cs.endText();

                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                cs.newLineAtOffset(pageWidth - margin - 100, yPosition + 7);
                cs.showText("Rs. " + session.getTotalAmount());
                cs.endText();

                // 12. Footer
                yPosition = 80;
                cs.setNonStrokingColor(DARK_GREY);
                cs.setLineWidth(1);
                cs.moveTo(margin, yPosition);
                cs.lineTo(pageWidth - margin, yPosition);
                cs.stroke();

                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                cs.newLineAtOffset(margin, yPosition - 15);
                cs.showText("Payment Terms: Please remit payment within 15 days of invoice date.");
                cs.endText();

                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                cs.newLineAtOffset(margin, yPosition - 30);
                cs.showText("Thank you for choosing Ridhi Sidhi Tours!");
                cs.endText();

                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 8);
                cs.setNonStrokingColor(0.5f, 0.5f, 0.5f);
                cs.newLineAtOffset(pageWidth - margin - 180, 30);
                cs.showText("This is a computer-generated invoice.");
                cs.endText();
            }
            
            document.save(file);
        }
    }

    private void writeText(PDPageContentStream cs, String text, float x, float y, int size, boolean bold) throws IOException {
        cs.beginText();
        if(bold) {
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), size);
        } else {
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), size);
        }
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }
}