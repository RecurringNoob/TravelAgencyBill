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
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a professional A4 PDF invoice using Apache PDFBox.
 *
 * Improvements over original:
 *  - Multi-page support: content flows to new pages when the page fills up
 *  - Unified currency symbol via BookingSession / BasePanel.CURRENCY
 *  - GST line item in the totals section
 *  - Company name/email/phone/tagline read from session (configurable)
 *  - Page numbering in footer
 */
public class PDFGeneratorService {

    // ---- PDF Colours ----
    private static final PDColor BLUE_PRIMARY  = pdfRgb(0.16f, 0.50f, 0.72f);
    private static final PDColor GREEN_SUCCESS = pdfRgb(0.15f, 0.68f, 0.38f);
    private static final PDColor DARK          = pdfRgb(0.17f, 0.24f, 0.31f);
    private static final PDColor LIGHT_BG      = pdfRgb(0.95f, 0.97f, 0.98f);
    private static final PDColor WHITE         = pdfRgb(1f,    1f,    1f);
    private static final PDColor MUTED         = pdfRgb(0.50f, 0.55f, 0.58f);

    private static final float MARGIN     = 45f;
    private static final float PAGE_W     = PDRectangle.A4.getWidth();
    private static final float PAGE_H     = PDRectangle.A4.getHeight();
    private static final float CONTENT_W  = PAGE_W - 2 * MARGIN;
    private static final float LINE_SM    = 16f;
    private static final float LINE_MD    = 20f;
    private static final float HEADER_H   = 85f;
    private static final float FOOTER_H   = 50f;
    private static final float SAFE_TOP   = PAGE_H - HEADER_H - 10f;  // First page content start y
    private static final float SAFE_BOT   = FOOTER_H + 10f;           // All pages content bottom y

    private PDDocument           doc;
    private BookingSession       session;
    private int                  pageCount;
    private List<PDPage>         pages;
    private List<PDPageContentStream> streams;

    // Current draw state
    private int   curPage;
    private float y;

    public void generatePDF(BookingSession session, File file) throws IOException {
        this.session = session;
        this.doc     = new PDDocument();
        this.pages   = new ArrayList<>();
        this.streams = new ArrayList<>();

        // First page
        newPage();
        drawHeader();
        drawInvoiceMeta();

        // Body
        if (!session.getFlights().isEmpty()) drawFlightSection();
        if (!session.getCars().isEmpty())    drawCarSection();

        drawTotals();

        // Footers on all pages
        for (int i = 0; i < pages.size(); i++) {
            drawFooter(streams.get(i), i + 1, pages.size());
        }

        // Close all streams
        for (PDPageContentStream cs : streams) cs.close();

        doc.save(file);
        doc.close();
    }

    // -------------------------------------------------------------------------
    // Page Management
    // -------------------------------------------------------------------------

    private void newPage() throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        pages.add(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        streams.add(cs);
        curPage = pages.size() - 1;
        pageCount = pages.size();

        if (curPage == 0) {
            y = SAFE_TOP;
        } else {
            // Non-first pages: draw a continuation header strip
            cs.setNonStrokingColor(BLUE_PRIMARY);
            cs.addRect(0, PAGE_H - 28, PAGE_W, 28);
            cs.fill();
            text(cs, session.getCompanyName() + "  —  Invoice " + session.getInvoiceNumber(),
                 MARGIN, PAGE_H - 18, 9, true, WHITE);
            y = PAGE_H - 45;
        }
    }

    private PDPageContentStream cs() { return streams.get(curPage); }

    /** Advances y by delta; creates a new page if we'd overflow. */
    private void advanceY(float delta) throws IOException {
        y -= delta;
        if (y < SAFE_BOT) newPage();
    }

    // -------------------------------------------------------------------------
    // Header
    // -------------------------------------------------------------------------

    private void drawHeader() throws IOException {
        PDPageContentStream cs = cs();

        // Blue band
        cs.setNonStrokingColor(BLUE_PRIMARY);
        cs.addRect(0, PAGE_H - HEADER_H, PAGE_W, HEADER_H);
        cs.fill();

        // Logo text
        text(cs, "RST", MARGIN, PAGE_H - 30, 28, true, WHITE);

        // Company name
        text(cs, session.getCompanyName(), MARGIN + 70, PAGE_H - 22, 20, true, WHITE);

        // Tagline + contact
        text(cs, session.getCompanyTagline(), MARGIN + 70, PAGE_H - 40, 10, false, pdfRgb(0.85f, 0.9f, 0.95f));
        text(cs, session.getCompanyEmail() + "   " + session.getCompanyPhone(),
             MARGIN + 70, PAGE_H - 55, 9, false, pdfRgb(0.85f, 0.9f, 0.95f));

        // INVOICE label (right)
        text(cs, "INVOICE", PAGE_W - MARGIN - 80, PAGE_H - 35, 22, true, WHITE);
    }

    // -------------------------------------------------------------------------
    // Invoice Metadata
    // -------------------------------------------------------------------------

    private void drawInvoiceMeta() throws IOException {
        PDPageContentStream cs = cs();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        // Blue rule under header
        hline(cs, y + 5, BLUE_PRIMARY, 1.5f);

        // Left: invoice info
        float leftX  = MARGIN;
        float rightX = PAGE_W - MARGIN - 160;

        text(cs, "Invoice No:", leftX, y, 10, true, DARK);
        text(cs, session.getInvoiceNumber(), leftX + 75, y, 10, false, DARK);
        y -= LINE_SM;

        text(cs, "Date:", leftX, y, 10, true, DARK);
        text(cs, sdf.format(session.getBookingDate()), leftX + 75, y, 10, false, DARK);
        y -= LINE_SM;

        // Right: customer info
        float metaTop = y + LINE_SM * 2;
        text(cs, "Bill To:", rightX, metaTop, 10, true, DARK);
        text(cs, session.getCustomerName(), rightX, metaTop - LINE_SM, 10, false, DARK);
        if (!session.getContactNumber().isEmpty())
            text(cs, session.getContactNumber(), rightX, metaTop - LINE_SM * 2, 9, false, MUTED);
        if (!session.getAddress().isEmpty())
            text(cs, truncate(session.getAddress(), 32), rightX, metaTop - LINE_SM * 3, 9, false, MUTED);

        advanceY(LINE_MD);
        hline(cs(), y + 5, DARK, 0.5f);
        advanceY(LINE_SM);
    }

    // -------------------------------------------------------------------------
    // Flight Section
    // -------------------------------------------------------------------------

    private void drawFlightSection() throws IOException {
        drawSectionHeader("FLIGHT BOOKINGS");
        drawTableHeader(new String[]{"PNR", "Route", "Pax", "Fare"},
                        new float[]  {MARGIN, MARGIN+100, MARGIN+290, MARGIN+360});

        for (BookingSession.FlightBookingData f : session.getFlights()) {
            ensureSpace(LINE_SM);
            PDPageContentStream cs = cs();
            text(cs, truncate(f.pnr, 14),                       MARGIN,       y, 10, false, DARK);
            text(cs, truncate(f.source + " \u2192 " + f.dest, 24), MARGIN+100, y, 10, false, DARK);
            text(cs, String.valueOf(f.passengers),               MARGIN+290,   y, 10, false, DARK);
            text(cs, BasePanel.CURRENCY + " " + f.fare.toPlainString(), MARGIN+360, y, 10, false, DARK);
            advanceY(LINE_SM);
        }

        // Subtotal row
        PDPageContentStream cs = cs();
        hline(cs, y + 4, MUTED, 0.5f);
        advanceY(LINE_SM);
        text(cs(), "Flight Subtotal:", MARGIN + 250, y, 10, true, DARK);
        text(cs(), BasePanel.CURRENCY + " " + session.getFlightTotal().toPlainString(), MARGIN + 360, y, 10, true, BLUE_PRIMARY);
        advanceY(LINE_MD);
    }

    // -------------------------------------------------------------------------
    // Car Section
    // -------------------------------------------------------------------------

    private void drawCarSection() throws IOException {
        drawSectionHeader("CAR RENTALS");
        drawTableHeader(new String[]{"Vehicle No", "Route", "Fare"},
                        new float[]  {MARGIN, MARGIN+130, MARGIN+360});

        for (BookingSession.CarBookingData c : session.getCars()) {
            ensureSpace(LINE_SM);
            PDPageContentStream cs = cs();
            text(cs, truncate(c.carNo, 18),                            MARGIN,     y, 10, false, DARK);
            text(cs, truncate(c.source + " \u2192 " + c.dest, 26),    MARGIN+130, y, 10, false, DARK);
            text(cs, BasePanel.CURRENCY + " " + c.fare.toPlainString(), MARGIN+360, y, 10, false, DARK);
            advanceY(LINE_SM);
        }

        PDPageContentStream cs = cs();
        hline(cs, y + 4, MUTED, 0.5f);
        advanceY(LINE_SM);
        text(cs(), "Car Subtotal:", MARGIN + 280, y, 10, true, DARK);
        text(cs(), BasePanel.CURRENCY + " " + session.getCarTotal().toPlainString(), MARGIN + 360, y, 10, true, BLUE_PRIMARY);
        advanceY(LINE_MD);
    }

    // -------------------------------------------------------------------------
    // Totals
    // -------------------------------------------------------------------------

    private void drawTotals() throws IOException {
        ensureSpace(90);
        PDPageContentStream cs = cs();

        advanceY(10);
        hline(cs, y + 5, DARK, 1f);
        advanceY(LINE_MD);

        // Subtotal
        text(cs, "Subtotal:", MARGIN + 280, y, 11, true, DARK);
        text(cs, BasePanel.CURRENCY + " " + session.getSubtotal().toPlainString(), MARGIN + 360, y, 11, false, DARK);
        advanceY(LINE_MD);

        // GST
        text(cs, "GST (" + session.getGstRatePercent().toPlainString() + "%):", MARGIN + 280, y, 11, true, DARK);
        text(cs, BasePanel.CURRENCY + " " + session.getGstAmount().toPlainString(), MARGIN + 360, y, 11, false, DARK);
        advanceY(LINE_MD);

        // Grand total band
        cs.setNonStrokingColor(GREEN_SUCCESS);
        cs.addRect(MARGIN, y - 10, CONTENT_W, 32);
        cs.fill();

        text(cs, "GRAND TOTAL:", MARGIN + 230, y + 7, 14, true, WHITE);
        text(cs, BasePanel.CURRENCY + " " + session.getTotalAmount().toPlainString(), MARGIN + 360, y + 7, 16, true, WHITE);
        advanceY(40);
    }

    // -------------------------------------------------------------------------
    // Footer
    // -------------------------------------------------------------------------

    private void drawFooter(PDPageContentStream cs, int pageNo, int totalPages) throws IOException {
        hline(cs, FOOTER_H + 20, MUTED, 0.5f);

        text(cs, "Payment Terms: Please remit within 15 days of invoice date.",
             MARGIN, FOOTER_H + 10, 8, false, MUTED);
        text(cs, "Thank you for choosing " + session.getCompanyName() + "!",
             MARGIN, FOOTER_H - 3, 9, true, DARK);

        // Page number (right-aligned)
        String pageStr = "Page " + pageNo + " of " + totalPages;
        text(cs, pageStr, PAGE_W - MARGIN - 60, FOOTER_H + 5, 8, false, MUTED);

        text(cs, "* Computer-generated invoice — no signature required *",
             MARGIN, 15, 7, false, MUTED);
    }

    // -------------------------------------------------------------------------
    // Section Helpers
    // -------------------------------------------------------------------------

    private void drawSectionHeader(String title) throws IOException {
        ensureSpace(40);
        PDPageContentStream cs = cs();
        cs.setNonStrokingColor(BLUE_PRIMARY);
        cs.addRect(MARGIN, y - 6, CONTENT_W, 22);
        cs.fill();
        text(cs, title, MARGIN + 8, y + 2, 11, true, WHITE);
        advanceY(LINE_MD + 6);
    }

    private void drawTableHeader(String[] labels, float[] xs) throws IOException {
        ensureSpace(LINE_SM + 6);
        PDPageContentStream cs = cs();
        cs.setNonStrokingColor(LIGHT_BG);
        cs.addRect(MARGIN, y - 4, CONTENT_W, LINE_SM + 2);
        cs.fill();
        for (int i = 0; i < labels.length; i++) {
            text(cs, labels[i], xs[i] + 4, y, 9, true, DARK);
        }
        advanceY(LINE_SM + 4);
    }

    private void ensureSpace(float needed) throws IOException {
        if (y - needed < SAFE_BOT) newPage();
    }

    // -------------------------------------------------------------------------
    // Low-level Drawing Helpers
    // -------------------------------------------------------------------------

    private void hline(PDPageContentStream cs, float atY, PDColor color, float width) throws IOException {
        cs.setStrokingColor(color);
        cs.setLineWidth(width);
        cs.moveTo(MARGIN, atY);
        cs.lineTo(PAGE_W - MARGIN, atY);
        cs.stroke();
    }

    private void text(PDPageContentStream cs, String str, float x, float atY,
                      int size, boolean bold, PDColor color) throws IOException {
        if (str == null || str.isEmpty()) return;
        cs.setNonStrokingColor(color);
        cs.beginText();
        cs.setFont(
            bold ? new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
                 : new PDType1Font(Standard14Fonts.FontName.HELVETICA),
            size);
        cs.newLineAtOffset(x, atY);
        // Replace characters not in PDFBox's WinAnsiEncoding
        cs.showText(sanitise(str));
        cs.endText();
    }

    /** Replace non-Latin characters (e.g. → arrow, ₹) with safe ASCII equivalents. */
    private String sanitise(String s) {
        return s.replace('\u2192', '>')   // → arrow
                .replace("\u20B9", "Rs.") // ₹ rupee sign
                .replace("\u2026", "...") // … ellipsis
                .replaceAll("[^\\x20-\\x7E\\xA0-\\xFF]", "?");
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "...";
    }

    private static PDColor pdfRgb(float r, float g, float b) {
        return new PDColor(new float[]{r, g, b}, PDDeviceRGB.INSTANCE);
    }
}
