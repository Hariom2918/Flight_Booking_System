package fbs_model;

public class FlightData {
    private String flightNumber, airline, source, destination;
    private int availableSeats;
    private final int totalSeats; // Track original capacity
    private double ticketPrice;

    public FlightData(String flightNumber, String airline, String source, String destination, int availableSeats, double ticketPrice) {
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.source = source;
        this.destination = destination;
        this.availableSeats = availableSeats;
        this.totalSeats = availableSeats; // Store original capacity
        this.ticketPrice = ticketPrice;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getAirline() {
        return airline;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public boolean bookSeat() {
        if (availableSeats > 0) {
            availableSeats--;
            return true;
        }
        return false;
    }

    public boolean cancelSeat() {
        if (availableSeats < totalSeats) {
            availableSeats++;
            return true;
        }
        System.out.println("Warning: Cannot cancel — seats already at full capacity.");
        return false;
    }

    public void displayFlightDetails() {
        System.out.println("Flight: " + flightNumber + " | Airline: " + airline
                + " | From: " + source + " To: " + destination
                + " | Seats: " + availableSeats + "/" + totalSeats
                + " | Price: Rs." + ticketPrice);
    }
}