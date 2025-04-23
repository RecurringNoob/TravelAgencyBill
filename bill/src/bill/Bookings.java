package bill;
import java.util.Date;
import java.util.ArrayList;


public class Bookings {
    private Customer cust;
    private String invoice;
    private Date date;
    private int airfareTotal = 0;
    private int carfareTotal = 0;
    private ArrayList<FlightBooking> flights;
    private ArrayList<CarBooking> cars;
    
    static class FlightBooking {
        private String pnr;
        private String src;
        private String dest;
        private int fare;
        private int passengerNumber;
        private String[] passengerNames;
        
        FlightBooking(String pnr, String src, String dest, String fare, int passengerNumber) {
            this.pnr = pnr;
            this.src = src;
            this.dest = dest;
            this.fare = Integer.parseInt(fare);
            this.passengerNumber = passengerNumber;
            this.passengerNames = new String[passengerNumber];
        }
        
        public String getPnr() { return pnr; }
        public String getSrc() { return src; }
        public String getDest() { return dest; }
        public int getFare() { return fare; }
        public int getPassengerNumber() { return passengerNumber; }
    }
    
    static class CarBooking {
        private String carNo;
        private String src;
        private String dest;
        private int fare;
        private String farePerKm;
        private String seatCapacity;
        
        CarBooking(String carNo, String src, String dest, String fare) {
            this.carNo = carNo;
            this.src = src;
            this.dest = dest;
            this.fare = Integer.parseInt(fare);
        }
        
        public String getCarNo() { return carNo; }
        public String getSrc() { return src; }
        public String getDest() { return dest; }
        public int getFare() { return fare; }
    }
    
    static class Customer {
        private String name;
        private String contactNo;
        private String address;
        
        public String getName() { return name; }
        public String getContactNo() { return contactNo; }
        public String getAddress() { return address; }
    }
    
    public Bookings() {
        cust = new Customer();
        date = new Date();
        flights = new ArrayList<FlightBooking>();
        cars = new ArrayList<CarBooking>();
    }
    
    public void addCustomerDetails(String name, String contactNo, String address) {
        this.cust.name = name;
        this.cust.address = address;
        this.cust.contactNo = contactNo;
    }
    
    public void addFlightDetails(String pnr, String src, String dest, String fare, int passengerNumber) {
        try {
            int fareValue = Integer.parseInt(fare);
            this.flights.add(new FlightBooking(pnr, src, dest, fare, passengerNumber));
            this.airfareTotal += fareValue;
        } catch (NumberFormatException e) {
            System.err.println("Invalid fare format: " + fare);
        }
    }
    
    public void addFlightDetails(String pnr, String src, String dest, String fare) {
        addFlightDetails(pnr, src, dest, fare, 1); // Default to 1 passenger
    }
    
    public void addCarDetails(String carNo, String src, String dest, String fare) {
        try {
            int fareValue = Integer.parseInt(fare);
            this.cars.add(new CarBooking(carNo, src, dest, fare));
            this.carfareTotal += fareValue;
        } catch (NumberFormatException e) {
            System.err.println("Invalid fare format: " + fare);
        }
    }
    
    public Customer getCustomer() { return cust; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getInvoice() { return invoice; }
    public void setInvoice(String invoice) { this.invoice = invoice; }
    public int getAirfareTotal() { return airfareTotal; }
    public int getCarfareTotal() { return carfareTotal; }
    public int getTotalFare() { return airfareTotal + carfareTotal; }
    public ArrayList<FlightBooking> getFlights() { return flights; }
    public ArrayList<CarBooking> getCars() { return cars; }
    
    public void print() {
        System.out.println("--- Booking Summary ---");
        System.out.println("Customer Name: " + cust.name + "\tContact: " + cust.contactNo);
        System.out.println("Address: " + cust.address);
        System.out.println("Invoice: " + invoice + "\tDate: " + date);
        
        System.out.println("\nFlight Bookings:");
        for (FlightBooking flight : flights) {
            System.out.println("PNR: " + flight.pnr + 
                               " | From: " + flight.src + 
                               " | To: " + flight.dest + 
                               " | Fare: ₹" + flight.fare);
        }
        
        System.out.println("\nCar Bookings:");
        for (CarBooking car : cars) {
            System.out.println("Car No: " + car.carNo + 
                               " | From: " + car.src + 
                               " | To: " + car.dest + 
                               " | Fare: ₹" + car.fare);
        }
        
        System.out.println("\nTotal Airfare: ₹" + airfareTotal);
        System.out.println("Total Carfare: ₹" + carfareTotal);
        System.out.println("Grand Total: ₹" + getTotalFare());
    }
}