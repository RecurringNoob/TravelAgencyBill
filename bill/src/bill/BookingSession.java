package bill;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookingSession {
    private String customerName = "";
    private String contactNumber = "";
    private String address = "";
    private String invoiceNumber = "";
    private Date bookingDate = new Date();

    private final List<FlightBookingData> flights = new ArrayList<>();
    private final List<CarBookingData> cars = new ArrayList<>();

    // --- Inner Classes for Data ---
    public static class FlightBookingData {
        public String pnr, source, dest;
        public BigDecimal fare;
        public int passengers;

        public FlightBookingData(String pnr, String src, String dest, BigDecimal fare, int passengers) {
            this.pnr = pnr; 
            this.source = src; 
            this.dest = dest;
            this.fare = fare; 
            this.passengers = passengers;
        }
    }

    public static class CarBookingData {
        public String carNo, source, dest;
        public BigDecimal fare;

        public CarBookingData(String carNo, String src, String dest, BigDecimal fare) {
            this.carNo = carNo; 
            this.source = src; 
            this.dest = dest;
            this.fare = fare;
        }
    }

    // --- Setters ---
    public void setCustomerDetails(String name, String contact, String addr, String inv, Date date) {
        this.customerName = name; 
        this.contactNumber = contact;
        this.address = addr; 
        this.invoiceNumber = inv; 
        this.bookingDate = date;
    }

    public void addFlight(String pnr, String src, String dest, BigDecimal fare, int passengers) {
        flights.add(new FlightBookingData(pnr, src, dest, fare, passengers));
    }

    public void addCar(String carNo, String src, String dest, BigDecimal fare) {
        cars.add(new CarBookingData(carNo, src, dest, fare));
    }

    public void removeFlight(int index) {
        if(index >= 0 && index < flights.size()) {
            flights.remove(index);
        }
    }

    public void removeCar(int index) {
        if(index >= 0 && index < cars.size()) {
            cars.remove(index);
        }
    }

    public void reset() {
        customerName = "";
        contactNumber = "";
        address = "";
        invoiceNumber = "";
        bookingDate = new Date();
        flights.clear();
        cars.clear();
    }

    // --- Getters ---
    public BigDecimal getTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (FlightBookingData f : flights) {
            total = total.add(f.fare);
        }
        for (CarBookingData c : cars) {
            total = total.add(c.fare);
        }
        return total;
    }

    public BigDecimal getFlightTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (FlightBookingData f : flights) {
            total = total.add(f.fare);
        }
        return total;
    }

    public BigDecimal getCarTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CarBookingData c : cars) {
            total = total.add(c.fare);
        }
        return total;
    }

    public int getTotalFlights() {
        return flights.size();
    }

    public int getTotalCars() {
        return cars.size();
    }

    public int getTotalPassengers() {
        int total = 0;
        for (FlightBookingData f : flights) {
            total += f.passengers;
        }
        return total;
    }

    public boolean hasCustomerDetails() {
        return !customerName.isEmpty() && !invoiceNumber.isEmpty();
    }

    public boolean hasBookings() {
        return !flights.isEmpty() || !cars.isEmpty();
    }

    // Validation methods
    public boolean isValidCustomer() {
        return customerName != null && !customerName.trim().isEmpty() 
            && invoiceNumber != null && !invoiceNumber.trim().isEmpty();
    }

    public String getCustomerName() { return customerName; }
    public String getContactNumber() { return contactNumber; }
    public String getAddress() { return address; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public Date getBookingDate() { return bookingDate; }
    public List<FlightBookingData> getFlights() { return new ArrayList<>(flights); }
    public List<CarBookingData> getCars() { return new ArrayList<>(cars); }
}