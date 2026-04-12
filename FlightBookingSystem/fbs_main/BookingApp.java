package fbs_main;

import fbs_model.*;
import fbs_user.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;

public class BookingApp {
    private static Scanner scanner = new Scanner(System.in);
    private static ArrayList<FlightData> flights = new ArrayList<>();
    private static ArrayList<Reservation> bookings = new ArrayList<>();

    public static void main(String[] args) {
        loadFlightsFromFile();
        Manager manager = new Manager("A001", "Admin User", "admin@example.com", "0987654321", "Admin01");

        int choice = 0;
        do {
            System.out.println("\n===== Flight Booking System =====");
            System.out.println("1. Admin Menu");
            System.out.println("2. Passenger Menu");
            System.out.println("3. View Available Flights");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 4.");
                continue;
            }

            switch (choice) {
                case 1:
                    adminMenu(manager);
                    break;
                case 2:
                    Client client = createClient();
                    if (client != null) {
                        passengerMenu(client);
                    }
                    break;
                case 3:
                    viewFlights(false);
                    break;
                case 4:
                    System.out.println("Exiting system...");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1-4.");
            }
        } while (choice != 4);

        scanner.close();
    }

    // ─── Create Client with validation ───────────────────────────────────

    private static Client createClient() {
        System.out.println("\nEnter passenger details to continue:");
        System.out.print("Enter user ID: ");
        String userId = scanner.nextLine().trim();
        if (userId.isEmpty()) {
            System.out.println("User ID cannot be empty.");
            return null;
        }
        System.out.print("Enter name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return null;
        }
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter phone number: ");
        String phone = scanner.nextLine().trim();
        System.out.print("Enter passport number: ");
        String passport = scanner.nextLine().trim();
        if (passport.isEmpty()) {
            System.out.println("Passport number cannot be empty.");
            return null;
        }
        return new Client(userId, name, email, phone, passport);
    }

    // ─── Admin Menu (with try-catch on all parseInt) ─────────────────────

    private static void adminMenu(Manager manager) {
        int choice;
        do {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Add Flight");
            System.out.println("2. Remove Flight");
            System.out.println("3. View All Flights");
            System.out.println("4. View Stats");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 5.");
                continue;
            }

            switch (choice) {
                case 1:
                    addFlight();
                    break;
                case 2:
                    removeFlight();
                    break;
                case 3:
                    viewFlights(true);
                    break;
                case 4:
                    StatsHelper.showStats(flights.size(), bookings.size());
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 1-5.");
            }
        } while (true);
    }

    // ─── Add Flight (with input validation) ──────────────────────────────

    private static void addFlight() {
        System.out.print("Enter flight number: ");
        String fn = scanner.nextLine().trim();
        if (fn.isEmpty()) {
            System.out.println("Flight number cannot be empty.");
            return;
        }

        // Check for duplicate flight number
        for (FlightData f : flights) {
            if (f.getFlightNumber().equalsIgnoreCase(fn)) {
                System.out.println("A flight with this number already exists.");
                return;
            }
        }

        System.out.print("Enter airline: ");
        String al = scanner.nextLine().trim();
        System.out.print("Enter source: ");
        String src = scanner.nextLine().trim();
        System.out.print("Enter destination: ");
        String dest = scanner.nextLine().trim();

        int seats;
        double price;
        try {
            System.out.print("Enter seat count: ");
            seats = Integer.parseInt(scanner.nextLine().trim());
            if (seats <= 0) {
                System.out.println("Seat count must be a positive number.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid seat count. Please enter a valid number.");
            return;
        }

        try {
            System.out.print("Enter ticket price: ");
            price = Double.parseDouble(scanner.nextLine().trim());
            if (price <= 0) {
                System.out.println("Ticket price must be a positive number.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid price. Please enter a valid number.");
            return;
        }

        flights.add(new FlightData(fn, al, src, dest, seats, price));
        System.out.println("Flight added successfully.");
        saveFlightsToFile();
    }

    // ─── Remove Flight ───────────────────────────────────────────────────

    private static void removeFlight() {
        System.out.print("Enter flight number to remove: ");
        String flightNumberToRemove = scanner.nextLine().trim();
        if (flightNumberToRemove.isEmpty()) {
            System.out.println("Flight number cannot be empty.");
            return;
        }

        boolean found = false;
        for (int i = 0; i < flights.size(); i++) {
            if (flights.get(i).getFlightNumber().equalsIgnoreCase(flightNumberToRemove)) {
                flights.remove(i);
                found = true;
                System.out.println("Flight removed successfully.");
                saveFlightsToFile();
                break;
            }
        }
        if (!found) {
            System.out.println("Flight not found.");
        }
    }

    // ─── View Flights ────────────────────────────────────────────────────

    private static void viewFlights(boolean adminView) {
        if (flights.isEmpty()) {
            System.out.println(adminView ? "No flights scheduled." : "No flights available.");
        } else {
            System.out.println();
            for (FlightData f : flights) {
                f.displayFlightDetails();
            }
        }
    }

    // ─── Passenger Menu (with try-catch on parseInt) ─────────────────────

    private static void passengerMenu(Client client) {
        int choice;
        do {
            System.out.println("\n--- Passenger Menu ---");
            System.out.println("1. Book Flight");
            System.out.println("2. View My Bookings");
            System.out.println("3. Cancel Booking");
            System.out.println("4. Back");
            System.out.print("Enter choice: ");

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 4.");
                continue;
            }

            switch (choice) {
                case 1:
                    bookFlight(client);
                    break;
                case 2:
                    viewMyBookings(client);
                    break;
                case 3:
                    cancelBooking(client);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 1-4.");
            }
        } while (true);
    }

    // ─── Book Flight (with proper feedback) ──────────────────────────────

    private static void bookFlight(Client client) {
        viewFlights(false);

        if (flights.isEmpty()) {
            return; // viewFlights already printed "No flights available."
        }

        System.out.print("Enter flight number to book: ");
        String flightToBook = scanner.nextLine().trim();
        if (flightToBook.isEmpty()) {
            System.out.println("Flight number cannot be empty.");
            return;
        }

        boolean flightFound = false;
        for (FlightData flight : flights) {
            if (flight.getFlightNumber().equalsIgnoreCase(flightToBook)) {
                flightFound = true;
                if (flight.getAvailableSeats() > 0) {
                    Reservation res = new Reservation(client, flight);
                    bookings.add(res);
                    System.out.println("Flight booked successfully! Booking ID: " + res.getBookingId());
                } else {
                    System.out.println("Sorry, no seats available on this flight.");
                }
                break;
            }
        }

        if (!flightFound) {
            System.out.println("Flight number '" + flightToBook + "' not found. Please check and try again.");
        }
    }

    // ─── View My Bookings ────────────────────────────────────────────────

    private static void viewMyBookings(Client client) {
        boolean hasBookings = false;
        for (Reservation res : bookings) {
            if (res.getClient().getUserId().equals(client.getUserId())) {
                res.displayBookingDetails();
                System.out.println("---");
                hasBookings = true;
            }
        }
        if (!hasBookings) {
            System.out.println("No flights booked.");
        }
    }

    // ─── Cancel Booking ──────────────────────────────────────────────────

    private static void cancelBooking(Client client) {
        System.out.print("Enter Booking ID to cancel: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("Booking ID cannot be empty.");
            return;
        }

        boolean found = false;
        for (int i = 0; i < bookings.size(); i++) {
            Reservation res = bookings.get(i);
            if (res.getBookingId().equalsIgnoreCase(id)
                    && res.getClient().getUserId().equals(client.getUserId())) {
                res.cancelBooking();
                bookings.remove(i);
                System.out.println("Booking cancelled successfully.");
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("Booking not found. Please check the Booking ID and try again.");
        }
    }

    // ─── File I/O: Save Flights ──────────────────────────────────────────

    private static void saveFlightsToFile() {
        String filePath = getFlightsFilePath();
        try (FileWriter writer = new FileWriter(filePath)) {
            for (FlightData f : flights) {
                writer.write(f.getFlightNumber() + "," + f.getAirline() + ","
                        + f.getSource() + "," + f.getDestination() + ","
                        + f.getAvailableSeats() + "," + f.getTicketPrice() + "\n");
            }
            System.out.println("Flights saved to file.");
        } catch (IOException e) {
            System.out.println("Error saving flights: " + e.getMessage());
        }
    }

    // ─── File I/O: Load Flights ──────────────────────────────────────────

    private static void loadFlightsFromFile() {
        String filePath = getFlightsFilePath();
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("No saved flights file found. Starting fresh.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    try {
                        flights.add(new FlightData(
                                parts[0].trim(), parts[1].trim(),
                                parts[2].trim(), parts[3].trim(),
                                Integer.parseInt(parts[4].trim()),
                                Double.parseDouble(parts[5].trim())
                        ));
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: Skipping malformed line in flights file: " + line);
                    }
                } else {
                    System.out.println("Warning: Skipping malformed line in flights file: " + line);
                }
            }
            System.out.println("Loaded " + flights.size() + " flights from file.");
        } catch (IOException e) {
            System.out.println("Error loading flights: " + e.getMessage());
        }
    }

    // ─── Get absolute flights file path ──────────────────────────────────

    private static String getFlightsFilePath() {
        // Use the same directory as the source code for consistent file location
        String jarDir = System.getProperty("user.dir");
        return jarDir + File.separator + "flights.txt";
    }

    // ─── Stats Helper ────────────────────────────────────────────────────

    private static class StatsHelper {
        public static void showStats(int flightCount, int bookingCount) {
            System.out.println("\n--- System Stats ---");
            System.out.println("Total Flights: " + flightCount);
            System.out.println("Total Bookings: " + bookingCount);
        }
    }
}
