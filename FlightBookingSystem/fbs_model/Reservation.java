package fbs_model;

import fbs_user.Client;
import java.util.UUID;

public class Reservation {
    private String bookingId;
    private Client client;
    private FlightData flight;

    public Reservation(Client client, FlightData flight) {
        this.bookingId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.client = client;
        this.flight = flight;
        flight.bookSeat();
    }

    public String getBookingId() {
        return bookingId;
    }

    public Client getClient() {
        return client;
    }

    public FlightData getFlight() {
        return flight;
    }

    public void cancelBooking() {
        flight.cancelSeat();
    }

    public void displayBookingDetails() {
        System.out.println("Booking ID: " + bookingId);
        client.displayDetails();
        flight.displayFlightDetails();
    }
}