import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;
import javax.swing.*;

public class BookingApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> showRoleSelection());
    }

    // ─── Role Selection Screen ───────────────────────────────────────────

    public static void showRoleSelection() {
        JFrame frame = new JFrame("Flight Booking System");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(3, 1));

        JLabel label = new JLabel("Select Role to Login", SwingConstants.CENTER);
        frame.add(label);

        JButton adminButton = new JButton("Login as Admin");
        adminButton.addActionListener(e -> {
            frame.dispose();
            showLoginForm("admin");
        });
        frame.add(adminButton);

        JButton passengerButton = new JButton("Login as Passenger");
        passengerButton.addActionListener(e -> {
            frame.dispose();
            showLoginForm("passenger");
        });
        frame.add(passengerButton);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ─── Login Form ──────────────────────────────────────────────────────

    public static void showLoginForm(String role) {
        JFrame frame = new JFrame(role.equals("admin") ? "Admin Login" : "Passenger Login");
        frame.setSize(400, 300);
        frame.setLayout(new GridLayout(5, 2));

        JTextField userIdField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();

        frame.add(new JLabel("User ID:"));
        frame.add(userIdField);
        frame.add(new JLabel(role.equals("admin") ? "Admin Name:" : "Passenger Name:"));
        frame.add(nameField);
        frame.add(new JLabel("Email:"));
        frame.add(emailField);
        frame.add(new JLabel("Phone:"));
        frame.add(phoneField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
            String userId = userIdField.getText().trim();
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            // Input validation
            if (userId.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields are required. Please fill in every field.");
                return;
            }
            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid email address.");
                return;
            }
            if (!phone.matches("\\d{7,15}")) {
                JOptionPane.showMessageDialog(frame, "Phone number must be 7-15 digits.");
                return;
            }

            frame.dispose();
            if (role.equals("admin")) {
                Manager manager = new Manager(userId, name, email, phone);
                showAdminMenu(manager);
            } else {
                Client client = new Client(userId, name, email, phone);
                showPassengerMenu(client);
            }
        });

        frame.add(new JLabel());
        frame.add(loginButton);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ─── Admin Menu ──────────────────────────────────────────────────────

    public static void showAdminMenu(Manager manager) {
        JFrame frame = new JFrame("Admin Menu");
        frame.setSize(400, 300);
        frame.setLayout(new GridLayout(6, 1));

        JButton addFlightButton = new JButton("Add Flight");
        addFlightButton.addActionListener(e -> addFlight());

        JButton removeFlightButton = new JButton("Remove Flight");
        removeFlightButton.addActionListener(e -> removeFlight());

        JButton viewFlightsButton = new JButton("View All Flights");
        viewFlightsButton.addActionListener(e -> viewFlights());

        JButton showStatsButton = new JButton("Show Stats");
        showStatsButton.addActionListener(e -> showStats());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            frame.dispose();
            showRoleSelection();
        });

        frame.add(addFlightButton);
        frame.add(removeFlightButton);
        frame.add(viewFlightsButton);
        frame.add(showStatsButton);
        frame.add(logoutButton);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ─── Passenger Menu ──────────────────────────────────────────────────

    public static void showPassengerMenu(Client client) {
        JFrame frame = new JFrame("Passenger Menu");
        frame.setSize(400, 300);
        frame.setLayout(new GridLayout(5, 1));

        JButton bookFlightButton = new JButton("Book Flight");
        bookFlightButton.addActionListener(e -> bookFlight(client));

        JButton viewBookingsButton = new JButton("View My Bookings");
        viewBookingsButton.addActionListener(e -> viewBookings(client));

        JButton cancelBookingButton = new JButton("Cancel Booking");
        cancelBookingButton.addActionListener(e -> cancelBooking(client));

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            frame.dispose();
            showRoleSelection();
        });

        frame.add(bookFlightButton);
        frame.add(viewBookingsButton);
        frame.add(cancelBookingButton);
        frame.add(logoutButton);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ─── Add Flight (DB-backed) ──────────────────────────────────────────

    public static void addFlight() {
        JTextField flightNoField = new JTextField();
        JTextField airlineField = new JTextField();
        JTextField srcField = new JTextField();
        JTextField destField = new JTextField();
        JTextField seatsField = new JTextField();
        JTextField priceField = new JTextField();

        Object[] fields = {
                "Flight Number:", flightNoField,
                "Airline:", airlineField,
                "Source:", srcField,
                "Destination:", destField,
                "Available Seats:", seatsField,
                "Ticket Price:", priceField
        };

        int option = JOptionPane.showConfirmDialog(null, fields, "Add Flight", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String flightNo = flightNoField.getText().trim();
            String airline = airlineField.getText().trim();
            String src = srcField.getText().trim();
            String dest = destField.getText().trim();
            String seatsText = seatsField.getText().trim();
            String priceText = priceField.getText().trim();

            if (flightNo.isEmpty() || airline.isEmpty() || src.isEmpty() || dest.isEmpty()
                    || seatsText.isEmpty() || priceText.isEmpty()) {
                JOptionPane.showMessageDialog(null, "All fields are required.");
                return;
            }

            try {
                int seats = Integer.parseInt(seatsText);
                double price = Double.parseDouble(priceText);

                if (seats <= 0 || price <= 0) {
                    JOptionPane.showMessageDialog(null, "Seats and price must be positive numbers.");
                    return;
                }

                try (Connection conn = DBConnection.getConnection()) {
                    String sql = "INSERT INTO flights (flight_number, airline, source, destination, available_seats, ticket_price) "
                            + "VALUES (?, ?, ?, ?, ?, ?)";

                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, flightNo);
                        stmt.setString(2, airline);
                        stmt.setString(3, src);
                        stmt.setString(4, dest);
                        stmt.setInt(5, seats);
                        stmt.setDouble(6, price);

                        stmt.executeUpdate();
                        JOptionPane.showMessageDialog(null, "Flight added successfully!");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error adding flight to the database: " + ex.getMessage());
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input for seats or price. Please enter numbers only.");
            }
        }
    }

    // ─── Remove Flight (DB-backed) ───────────────────────────────────────

    public static void removeFlight() {
        String flightNo = JOptionPane.showInputDialog("Enter Flight Number to remove:");
        if (flightNo == null || flightNo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter a Flight Number.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM flights WHERE flight_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, flightNo.trim());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(null, "Flight removed successfully.");
                } else {
                    JOptionPane.showMessageDialog(null, "Flight not found.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error removing flight from the database: " + ex.getMessage());
        }
    }

    // ─── View Flights (DB-backed) ────────────────────────────────────────

    public static void viewFlights() {
        StringBuilder sb = new StringBuilder();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM flights";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet resultSet = stmt.executeQuery()) {

                boolean hasData = false;
                while (resultSet.next()) {
                    hasData = true;
                    sb.append("Flight ").append(resultSet.getString("flight_number"))
                            .append(" (").append(resultSet.getString("airline")).append(") ")
                            .append(resultSet.getString("source")).append(" -> ").append(resultSet.getString("destination"))
                            .append(" | Seats: ").append(resultSet.getInt("available_seats"))
                            .append(" | Price: Rs.").append(resultSet.getDouble("ticket_price")).append("\n");
                }

                if (!hasData) {
                    sb.append("No flights available.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            sb.append("Error fetching flights from the database: ").append(ex.getMessage());
        }

        JOptionPane.showMessageDialog(null, sb.toString());
    }

    // ─── Show Stats (DB-backed) ──────────────────────────────────────────

    public static void showStats() {
        try (Connection conn = DBConnection.getConnection()) {
            int flightCount = 0;
            int bookingCount = 0;

            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM flights");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    flightCount = rs.getInt(1);
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM bookings");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    bookingCount = rs.getInt(1);
                }
            }

            JOptionPane.showMessageDialog(null,
                    "Total Flights: " + flightCount + "\nTotal Bookings: " + bookingCount);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching stats: " + ex.getMessage());
        }
    }

    // ─── Book Flight (DB-backed) ─────────────────────────────────────────

    public static void bookFlight(Client client) {
        ArrayList<FlightData> availableFlights = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM flights WHERE available_seats > 0";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    availableFlights.add(new FlightData(
                            rs.getString("flight_number"),
                            rs.getString("airline"),
                            rs.getString("source"),
                            rs.getString("destination"),
                            rs.getInt("available_seats"),
                            rs.getDouble("ticket_price")
                    ));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching available flights: " + ex.getMessage());
            return;
        }

        if (availableFlights.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No flights available for booking.");
            return;
        }

        String[] options = new String[availableFlights.size()];
        for (int i = 0; i < availableFlights.size(); i++) {
            FlightData f = availableFlights.get(i);
            options[i] = f.getFlightNumber() + " - " + f.getSource() + " to " + f.getDestination()
                    + " (Rs." + f.getTicketPrice() + ")";
        }

        String selected = (String) JOptionPane.showInputDialog(null, "Select a flight to book:",
                "Book Flight", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (selected != null) {
            for (FlightData flight : availableFlights) {
                if (selected.startsWith(flight.getFlightNumber())) {
                    String bookingId = UUID.randomUUID().toString();

                    // Use a transaction to ensure atomicity
                    Connection conn = null;
                    try {
                        conn = DBConnection.getConnection();
                        conn.setAutoCommit(false);

                        // Insert booking into database
                        String insertSql = "INSERT INTO bookings (booking_id, user_id, user_name, user_email, user_phone, flight_number) "
                                + "VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setString(1, bookingId);
                            insertStmt.setString(2, client.getUserId());
                            insertStmt.setString(3, client.getName());
                            insertStmt.setString(4, client.getEmail());
                            insertStmt.setString(5, client.getPhone());
                            insertStmt.setString(6, flight.getFlightNumber());
                            insertStmt.executeUpdate();
                        }

                        // Update available seats
                        String updateSql = "UPDATE flights SET available_seats = available_seats - 1 WHERE flight_number = ? AND available_seats > 0";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, flight.getFlightNumber());
                            int rowsAffected = updateStmt.executeUpdate();
                            if (rowsAffected == 0) {
                                conn.rollback();
                                JOptionPane.showMessageDialog(null, "No seats available. Booking failed.");
                                return;
                            }
                        }

                        conn.commit();
                        JOptionPane.showMessageDialog(null, "Booking confirmed!\nBooking ID: " + bookingId);
                    } catch (SQLException ex) {
                        if (conn != null) {
                            try { conn.rollback(); } catch (SQLException ignored) {}
                        }
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Database error during booking: " + ex.getMessage());
                    } finally {
                        if (conn != null) {
                            try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
                        }
                    }
                    return;
                }
            }
            JOptionPane.showMessageDialog(null, "Selected flight not found.");
        }
    }

    // ─── View Bookings (DB-backed) ───────────────────────────────────────

    public static void viewBookings(Client client) {
        StringBuilder sb = new StringBuilder();
        boolean foundBooking = false;

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT b.booking_id, b.flight_number, f.source, f.destination, f.airline, f.ticket_price "
                    + "FROM bookings b JOIN flights f ON b.flight_number = f.flight_number "
                    + "WHERE b.user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, client.getUserId());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        foundBooking = true;
                        sb.append("Booking ID: ").append(rs.getString("booking_id")).append("\n")
                                .append("Flight: ").append(rs.getString("flight_number"))
                                .append(" (").append(rs.getString("airline")).append(")")
                                .append(" | ").append(rs.getString("source"))
                                .append(" to ").append(rs.getString("destination"))
                                .append(" | Price: Rs.").append(rs.getDouble("ticket_price"))
                                .append("\n\n");
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching bookings: " + ex.getMessage());
            return;
        }

        if (!foundBooking) {
            sb.append("No bookings found for you.");
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    // ─── Cancel Booking (DB-backed with transaction) ─────────────────────

    public static void cancelBooking(Client client) {
        String bookingIdToCancel = JOptionPane.showInputDialog("Enter Booking ID to cancel:");
        if (bookingIdToCancel == null || bookingIdToCancel.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter a Booking ID.");
            return;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Find the booking and its flight number
            String flightNumber = null;
            String findSql = "SELECT flight_number FROM bookings WHERE booking_id = ? AND user_id = ?";
            try (PreparedStatement findStmt = conn.prepareStatement(findSql)) {
                findStmt.setString(1, bookingIdToCancel.trim());
                findStmt.setString(2, client.getUserId());
                try (ResultSet rs = findStmt.executeQuery()) {
                    if (rs.next()) {
                        flightNumber = rs.getString("flight_number");
                    }
                }
            }

            if (flightNumber == null) {
                conn.rollback();
                JOptionPane.showMessageDialog(null, "Booking ID not found for your account.");
                return;
            }

            // Delete the booking
            String deleteSql = "DELETE FROM bookings WHERE booking_id = ? AND user_id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setString(1, bookingIdToCancel.trim());
                deleteStmt.setString(2, client.getUserId());
                deleteStmt.executeUpdate();
            }

            // Restore the seat
            String updateSql = "UPDATE flights SET available_seats = available_seats + 1 WHERE flight_number = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, flightNumber);
                updateStmt.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(null, "Booking cancelled successfully.");
        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error cancelling booking: " + ex.getMessage());
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    // ─── DB Connection (throws SQLException instead of returning null) ───

    public static class DBConnection {

        private static final String URL = System.getProperty("db.url", "jdbc:mysql://localhost:3306/flight_booking");
        private static final String USER = System.getProperty("db.user", "root");
        private static final String PASS = System.getProperty("db.pass", "devang1234");

        public static Connection getConnection() throws SQLException {
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Database connected successfully.");
            return conn;
        }
    }

    // ─── Supporting Classes (with proper encapsulation) ──────────────────

    static class Manager {
        private final String userId;
        private final String name;
        private final String email;
        private final String phone;

        Manager(String userId, String name, String email, String phone) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.phone = phone;
        }

        public String getUserId() { return userId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
    }

    static class Client {
        private final String userId;
        private final String name;
        private final String email;
        private final String phone;

        Client(String userId, String name, String email, String phone) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.phone = phone;
        }

        public String getUserId() { return userId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
    }

    static class FlightData {
        private final String flightNumber;
        private final String airline;
        private final String source;
        private final String destination;
        private int availableSeats;
        private double ticketPrice;

        FlightData(String flightNumber, String airline, String source, String destination, int availableSeats, double ticketPrice) {
            this.flightNumber = flightNumber;
            this.airline = airline;
            this.source = source;
            this.destination = destination;
            this.availableSeats = availableSeats;
            this.ticketPrice = ticketPrice;
        }

        public String getFlightNumber() { return flightNumber; }
        public String getAirline() { return airline; }
        public String getSource() { return source; }
        public String getDestination() { return destination; }
        public int getAvailableSeats() { return availableSeats; }
        public double getTicketPrice() { return ticketPrice; }
    }

    static class Reservation {
        private final String bookingId;
        private final Client client;
        private final FlightData flight;

        Reservation(String bookingId, Client client, FlightData flight) {
            this.bookingId = bookingId;
            this.client = client;
            this.flight = flight;
        }

        public String getBookingId() { return bookingId; }
        public Client getClient() { return client; }
        public FlightData getFlight() { return flight; }
    }
}