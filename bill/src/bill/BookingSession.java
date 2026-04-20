package bill;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Central data model for a single booking session.
 * Shared by reference across all panels.
 * Supports serialization for session persistence.
 */
public class BookingSession implements Serializable {
    private static final long serialVersionUID = 1L;

    // Company info (configurable)
    private String companyName    = "Ridhi Sidhi Tours";
    private String companyTagline = "Your Journey, Our Responsibility";
    private String companyEmail   = "info@ridhisidhitours.com";
    private String companyPhone   = "+91-9800000000";

    // Customer info
    private String customerName   = "";
    private String contactNumber  = "";
    private String address        = "";
    private String invoiceNumber  = "";
    private Date   bookingDate    = new Date();

    // Tax
    private BigDecimal gstRatePercent = new BigDecimal("18"); // 18% GST by default

    private final List<FlightBookingData> flights = new ArrayList<>();
    private final List<CarBookingData>    cars    = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Inner Data Classes
    // -------------------------------------------------------------------------

    public static class FlightBookingData implements Serializable {
        private static final long serialVersionUID = 1L;
        public String     pnr, source, dest;
        public BigDecimal fare;
        public int        passengers;

        public FlightBookingData(String pnr, String src, String dest, BigDecimal fare, int passengers) {
            this.pnr        = pnr;
            this.source     = src;
            this.dest       = dest;
            this.fare       = fare;
            this.passengers = passengers;
        }
    }

    public static class CarBookingData implements Serializable {
        private static final long serialVersionUID = 1L;
        public String     carNo, source, dest;
        public BigDecimal fare;

        public CarBookingData(String carNo, String src, String dest, BigDecimal fare) {
            this.carNo  = carNo;
            this.source = src;
            this.dest   = dest;
            this.fare   = fare;
        }
    }

    // -------------------------------------------------------------------------
    // Mutation Methods
    // -------------------------------------------------------------------------

    public void setCustomerDetails(String name, String contact, String addr, String inv, Date date) {
        this.customerName  = name.trim();
        this.contactNumber = contact.trim();
        this.address       = addr.trim();
        this.invoiceNumber = inv.trim();
        this.bookingDate   = date;
    }

    public void setCompanyDetails(String name, String tagline, String email, String phone) {
        this.companyName    = name;
        this.companyTagline = tagline;
        this.companyEmail   = email;
        this.companyPhone   = phone;
    }

    public void setGstRate(BigDecimal ratePercent) {
        this.gstRatePercent = ratePercent;
    }

    /** Adds a flight after validation. Throws IllegalArgumentException on invalid input. */
    public void addFlight(String pnr, String src, String dest, BigDecimal fare, int passengers) {
        if (pnr.isBlank())           throw new IllegalArgumentException("PNR cannot be empty.");
        if (src.isBlank())           throw new IllegalArgumentException("Source cannot be empty.");
        if (dest.isBlank())          throw new IllegalArgumentException("Destination cannot be empty.");
        if (src.equalsIgnoreCase(dest)) throw new IllegalArgumentException("Source and destination cannot be the same.");
        if (fare.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Fare must be greater than zero.");
        if (passengers < 1)          throw new IllegalArgumentException("At least 1 passenger required.");
        flights.add(new FlightBookingData(pnr.trim(), src.trim(), dest.trim(), fare, passengers));
    }

    /** Updates an existing flight entry by index. */
    public void updateFlight(int index, String pnr, String src, String dest, BigDecimal fare, int passengers) {
        removeFlight(index);
        flights.add(index, new FlightBookingData(pnr.trim(), src.trim(), dest.trim(), fare, passengers));
    }

    /** Adds a car rental after validation. Throws IllegalArgumentException on invalid input. */
    public void addCar(String carNo, String src, String dest, BigDecimal fare) {
        if (carNo.isBlank()) throw new IllegalArgumentException("Car number cannot be empty.");
        if (src.isBlank())   throw new IllegalArgumentException("Pickup location cannot be empty.");
        if (dest.isBlank())  throw new IllegalArgumentException("Drop location cannot be empty.");
        if (src.equalsIgnoreCase(dest)) throw new IllegalArgumentException("Pickup and drop cannot be the same.");
        if (fare.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Fare must be greater than zero.");
        cars.add(new CarBookingData(carNo.trim(), src.trim(), dest.trim(), fare));
    }

    /** Updates an existing car entry by index. */
    public void updateCar(int index, String carNo, String src, String dest, BigDecimal fare) {
        removeCar(index);
        cars.add(index, new CarBookingData(carNo.trim(), src.trim(), dest.trim(), fare));
    }

    public void removeFlight(int index) {
        if (index >= 0 && index < flights.size()) flights.remove(index);
    }

    public void removeCar(int index) {
        if (index >= 0 && index < cars.size()) cars.remove(index);
    }

    public void reset() {
        customerName  = "";
        contactNumber = "";
        address       = "";
        invoiceNumber = "";
        bookingDate   = new Date();
        flights.clear();
        cars.clear();
        // Company details and GST rate are intentionally preserved across sessions
    }

    // -------------------------------------------------------------------------
    // Calculations
    // -------------------------------------------------------------------------

    public BigDecimal getSubtotal() {
        return getFlightTotal().add(getCarTotal());
    }

    public BigDecimal getGstAmount() {
        return getSubtotal()
               .multiply(gstRatePercent)
               .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalAmount() {
        return getSubtotal().add(getGstAmount());
    }

    public BigDecimal getFlightTotal() {
        return flights.stream()
                      .map(f -> f.fare)
                      .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getCarTotal() {
        return cars.stream()
                   .map(c -> c.fare)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalFlights()    { return flights.size(); }
    public int getTotalCars()       { return cars.size(); }
    public int getTotalPassengers() { return flights.stream().mapToInt(f -> f.passengers).sum(); }

    // -------------------------------------------------------------------------
    // Validation  (single, consistent method)
    // -------------------------------------------------------------------------

    /** Returns true if name and invoice number are non-blank (trimmed). */
    public boolean isValidCustomer() {
        return customerName  != null && !customerName.isBlank()
            && invoiceNumber != null && !invoiceNumber.isBlank();
    }

    public boolean hasBookings() {
        return !flights.isEmpty() || !cars.isEmpty();
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    /** Saves this session to a file. */
    public void saveTo(File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        }
    }

    /** Loads a previously saved session from a file. */
    public static BookingSession loadFrom(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (BookingSession) ois.readObject();
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getCompanyName()    { return companyName; }
    public String getCompanyTagline() { return companyTagline; }
    public String getCompanyEmail()   { return companyEmail; }
    public String getCompanyPhone()   { return companyPhone; }
    public BigDecimal getGstRatePercent() { return gstRatePercent; }

    public String getCustomerName()   { return customerName; }
    public String getContactNumber()  { return contactNumber; }
    public String getAddress()        { return address; }
    public String getInvoiceNumber()  { return invoiceNumber; }
    public Date   getBookingDate()    { return bookingDate; }

    public List<FlightBookingData> getFlights() { return new ArrayList<>(flights); }
    public List<CarBookingData>    getCars()    { return new ArrayList<>(cars); }
}
