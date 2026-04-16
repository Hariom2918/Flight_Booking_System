import java.awt.Dimension;
import java.awt.FlowLayout;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.*;

public class BookingApp {
    private static final String DB_URL = System.getProperty("db.url", "jdbc:mysql://localhost:3306/flight_booking");
    private static final String DB_USER = System.getProperty("db.user", "root");
    private static final String DB_PASS = System.getProperty("db.pass", "Udaygupta@97");

    public static void main(String[] args) {
        // Verify DB connectivity BEFORE showing any UI
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            System.out.println("JDBC Connected OK");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Cannot connect to database. Please check your MySQL settings.\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Only show UI after DB is confirmed working
        SwingUtilities.invokeLater(BookingApp::showRoleSelection);
    }

    // ─── Utility: Hash password with SHA-256 ────────────────────────────

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // ─── Utility: Get database connection ────────────────────────────────

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // ─── Role Selection Screen ───────────────────────────────────────────

    public static void showRoleSelection() {
        JFrame frame = new JFrame("Login or Register");
        frame.setLayout(new FlowLayout());
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton registerButton = new JButton("Register");
        JButton loginAdminButton = new JButton("Login as Admin");
        JButton loginPassengerButton = new JButton("Login as Passenger");
        JButton exitButton = new JButton("Exit");

        registerButton.setPreferredSize(new Dimension(150, 30));
        loginAdminButton.setPreferredSize(new Dimension(150, 30));
        loginPassengerButton.setPreferredSize(new Dimension(150, 30));
        exitButton.setPreferredSize(new Dimension(150, 30));

        registerButton.addActionListener(e -> register());
        loginAdminButton.addActionListener(e -> login("admin"));
        loginPassengerButton.addActionListener(e -> login("passenger"));
        exitButton.addActionListener(e -> System.exit(0));

        frame.add(registerButton);
        frame.add(loginAdminButton);
        frame.add(loginPassengerButton);
        frame.add(exitButton);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ─── Register ────────────────────────────────────────────────────────

    private static void register() {
        JFrame registerFrame = new JFrame("Register");
        registerFrame.setLayout(new FlowLayout());
        registerFrame.setSize(300, 200);

        JTextField userIdField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JButton registerButton = new JButton("Register");

        registerButton.addActionListener(e -> {
            String userId = userIdField.getText().trim();
            String password = new String(passwordField.getPassword());
            if (userId.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(registerFrame, "Please enter all details.");
                return;
            }
            if (password.length() < 4) {
                JOptionPane.showMessageDialog(registerFrame, "Password must be at least 4 characters.");
                return;
            }

            try (Connection conn = getConnection()) {
                String sql = "INSERT INTO passengers (user_id, password) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, userId);
                    stmt.setString(2, hashPassword(password)); // Hash password before storing
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(registerFrame, "Registration successful!");
                    registerFrame.dispose();
                }
            } catch (SQLException ex) {
                if (ex.getMessage().contains("Duplicate")) {
                    JOptionPane.showMessageDialog(registerFrame, "User ID already exists. Please choose another.");
                } else {
                    JOptionPane.showMessageDialog(registerFrame, "Error: " + ex.getMessage());
                }
            }
        });

        registerFrame.add(new JLabel("User ID:"));
        registerFrame.add(userIdField);
        registerFrame.add(new JLabel("Password:"));
        registerFrame.add(passwordField);
        registerFrame.add(registerButton);

        registerFrame.setLocationRelativeTo(null);
        registerFrame.setVisible(true);
    }

    // ─── Login ───────────────────────────────────────────────────────────

    private static void login(String role) {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setLayout(new FlowLayout());
        loginFrame.setSize(300, 150);

        JTextField userIdField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JButton loginButton = new JButton("Login");

        loginButton.addActionListener(e -> {
            String userId = userIdField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (userId.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(loginFrame, "Please enter both User ID and Password.");
                return;
            }

            if (validateLogin(role, userId, password)) {
                JOptionPane.showMessageDialog(loginFrame,
                        (role.equals("admin") ? "Admin" : "Passenger") + " Login Successful");
                loginFrame.dispose();
                if (role.equals("admin")) {
                    showAdminMenu();
                } else {
                    showPassengerMenu(userId);
                }
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Invalid Credentials");
            }
        });

        loginFrame.add(new JLabel("User ID:"));
        loginFrame.add(userIdField);
        loginFrame.add(new JLabel("Password:"));
        loginFrame.add(passwordField);
        loginFrame.add(loginButton);

        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);
    }

    // ─── Validate Login (unified for admin and passenger) ──────────────────

    private static boolean validateLogin(String role, String userId, String password) {
        String sql = role.equals("admin")
                ? "SELECT 1 FROM admins WHERE admin_id = ? AND password = ?"
                : "SELECT 1 FROM passengers WHERE user_id = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, hashPassword(password));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ─── Admin Menu (with working ActionListeners) ───────────────────────

    private static void showAdminMenu() {
        JFrame adminMenu = new JFrame("Admin Menu");
        adminMenu.setLayout(new FlowLayout());
        adminMenu.setSize(400, 250);
        adminMenu.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JButton addFlight = new JButton("Add Flight");
        JButton viewFlights = new JButton("View Flights");
        JButton removeFlight = new JButton("Remove Flight");
        JButton showStats = new JButton("Show Stats");
        JButton logout = new JButton("Logout");

        addFlight.setPreferredSize(new Dimension(150, 30));
        viewFlights.setPreferredSize(new Dimension(150, 30));
        removeFlight.setPreferredSize(new Dimension(150, 30));
        showStats.setPreferredSize(new Dimension(150, 30));
        logout.setPreferredSize(new Dimension(150, 30));

        addFlight.addActionListener(e -> addFlight());
        viewFlights.addActionListener(e -> viewAllFlights());
        removeFlight.addActionListener(e -> removeFlightAdmin());
        showStats.addActionListener(e -> showStatsDialog());
        logout.addActionListener(e -> {
            adminMenu.dispose();
            showRoleSelection();
        });

        adminMenu.add(addFlight);
        adminMenu.add(viewFlights);
        adminMenu.add(removeFlight);
        adminMenu.add(showStats);
        adminMenu.add(logout);

        adminMenu.setLocationRelativeTo(null);
        adminMenu.setVisible(true);
    }

    // ─── Passenger Menu (with working ActionListeners) ───────────────────

    private static void showPassengerMenu(String userId) {
        JFrame passengerMenu = new JFrame("Passenger Menu");
        passengerMenu.setLayout(new FlowLayout());
        passengerMenu.setSize(400, 250);
        passengerMenu.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JButton bookFlight = new JButton("Book Flight");
        JButton viewBookings = new JButton("View Bookings");
        JButton cancelBooking = new JButton("Cancel Booking");
        JButton logout = new JButton("Logout");

        bookFlight.setPreferredSize(new Dimension(150, 30));
        viewBookings.setPreferredSize(new Dimension(150, 30));
        cancelBooking.setPreferredSize(new Dimension(150, 30));
        logout.setPreferredSize(new Dimension(150, 30));

        bookFlight.addActionListener(e -> bookFlightForPassenger(userId));
        viewBookings.addActionListener(e -> viewPassengerBookings(userId));
        cancelBooking.addActionListener(e -> cancelPassengerBooking(userId));
        logout.addActionListener(e -> {
            passengerMenu.dispose();
            showRoleSelection();
        });

        passengerMenu.add(bookFlight);
        passengerMenu.add(viewBookings);
        passengerMenu.add(cancelBooking);
        passengerMenu.add(logout);

        passengerMenu.setLocationRelativeTo(null);
        passengerMenu.setVisible(true);
    }

    // ─── Admin: Add Flight ───────────────────────────────────────────────

    private static void addFlight() {
        JTextField flightNoField = new JTextField(15);
        JTextField airlineField = new JTextField(15);
        JTextField srcField = new JTextField(15);
        JTextField destField = new JTextField(15);
        JTextField seatsField = new JTextField(15);
        JTextField priceField = new JTextField(15);

        Object[] fields = {
                "Flight Number:", flightNoField,
                "Airline:", airlineField,
                "Source:", srcField,
                "Destination:", destField,
                "Available Seats:", seatsField,
                "Ticket Price:", priceField
        };

        int option = JOptionPane.showConfirmDialog(null, fields, "Add Flight", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;

        String flightNo = flightNoField.getText().trim();
        String airline = airlineField.getText().trim();
        String src = srcField.getText().trim();
        String dest = destField.getText().trim();

        if (flightNo.isEmpty() || airline.isEmpty() || src.isEmpty() || dest.isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields are required.");
            return;
        }

        try {
            int seats = Integer.parseInt(seatsField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());

            if (seats <= 0 || price <= 0) {
                JOptionPane.showMessageDialog(null, "Seats and price must be positive.");
                return;
            }

            try (Connection conn = getConnection()) {
                String sql = "INSERT INTO flights (flight_number, airline, source, destination, available_seats, ticket_price) VALUES (?, ?, ?, ?, ?, ?)";
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
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Seats and price must be valid numbers.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "DB Error: " + ex.getMessage());
        }
    }

    // ─── Admin: View All Flights ─────────────────────────────────────────

    private static void viewAllFlights() {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM flights";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    sb.append(rs.getString("flight_number"))
                            .append(" | ").append(rs.getString("airline"))
                            .append(" | ").append(rs.getString("source"))
                            .append(" -> ").append(rs.getString("destination"))
                            .append(" | Seats: ").append(rs.getInt("available_seats"))
                            .append(" | Price: Rs.").append(rs.getDouble("ticket_price"))
                            .append("\n");
                }
                if (!hasData) {
                    sb.append("No flights available.");
                }
            }
        } catch (SQLException ex) {
            sb.append("Error: ").append(ex.getMessage());
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    // ─── Admin: Remove Flight ────────────────────────────────────────────

    private static void removeFlightAdmin() {
        String flightNo = JOptionPane.showInputDialog("Enter Flight Number to remove:");
        if (flightNo == null || flightNo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter a Flight Number.");
            return;
        }
        try (Connection conn = getConnection()) {
            String sql = "DELETE FROM flights WHERE flight_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, flightNo.trim());
                int rows = stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, rows > 0 ? "Flight removed." : "Flight not found.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    // ─── Admin: Show Stats ───────────────────────────────────────────────

    private static void showStatsDialog() {
        try (Connection conn = getConnection()) {
            int flightCount = 0, bookingCount = 0;
            try (PreparedStatement s = conn.prepareStatement("SELECT COUNT(*) FROM flights");
                 ResultSet r = s.executeQuery()) {
                if (r.next()) flightCount = r.getInt(1);
            }
            try (PreparedStatement s = conn.prepareStatement("SELECT COUNT(*) FROM bookings");
                 ResultSet r = s.executeQuery()) {
                if (r.next()) bookingCount = r.getInt(1);
            }
            JOptionPane.showMessageDialog(null,
                    "Total Flights: " + flightCount + "\nTotal Bookings: " + bookingCount);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    // ─── Passenger: Book Flight ──────────────────────────────────────────

    private static void bookFlightForPassenger(String userId) {
        List<String[]> flightList = new ArrayList<>();
        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM flights WHERE available_seats > 0";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    flightList.add(new String[]{
                            rs.getString("flight_number"),
                            rs.getString("airline"),
                            rs.getString("source"),
                            rs.getString("destination"),
                            String.valueOf(rs.getInt("available_seats")),
                            String.valueOf(rs.getDouble("ticket_price"))
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            return;
        }

        if (flightList.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No flights available for booking.");
            return;
        }

        String[] options = new String[flightList.size()];
        for (int i = 0; i < flightList.size(); i++) {
            String[] f = flightList.get(i);
            options[i] = f[0] + " - " + f[2] + " to " + f[3] + " (Rs." + f[5] + ")";
        }

        String selected = (String) JOptionPane.showInputDialog(null, "Select a flight:",
                "Book Flight", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (selected == null) return;

        String selectedFlightNo = selected.split(" - ")[0];
        String bookingId = UUID.randomUUID().toString();

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String insertSql = "INSERT INTO bookings (booking_id, user_id, flight_number) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, bookingId);
                stmt.setString(2, userId);
                stmt.setString(3, selectedFlightNo);
                stmt.executeUpdate();
            }

            String updateSql = "UPDATE flights SET available_seats = available_seats - 1 WHERE flight_number = ? AND available_seats > 0";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, selectedFlightNo);
                int rows = stmt.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(null, "No seats available. Booking failed.");
                    return;
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(null, "Booking confirmed!\nBooking ID: " + bookingId);
        } catch (SQLException ex) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ignored) {} }
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {} }
        }
    }

    // ─── Passenger: View Bookings ────────────────────────────────────────

    private static void viewPassengerBookings(String userId) {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = getConnection()) {
            String sql = "SELECT b.booking_id, b.flight_number, f.airline, f.source, f.destination, f.ticket_price "
                    + "FROM bookings b JOIN flights f ON b.flight_number = f.flight_number WHERE b.user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        sb.append("Booking: ").append(rs.getString("booking_id"))
                                .append(" | Flight: ").append(rs.getString("flight_number"))
                                .append(" (").append(rs.getString("airline")).append(")")
                                .append(" | ").append(rs.getString("source"))
                                .append(" -> ").append(rs.getString("destination"))
                                .append(" | Rs.").append(rs.getDouble("ticket_price")).append("\n");
                    }
                    if (!found) sb.append("No bookings found.");
                }
            }
        } catch (SQLException ex) {
            sb.append("Error: ").append(ex.getMessage());
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    // ─── Passenger: Cancel Booking ───────────────────────────────────────

    private static void cancelPassengerBooking(String userId) {
        String bookingId = JOptionPane.showInputDialog("Enter Booking ID to cancel:");
        if (bookingId == null || bookingId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter a Booking ID.");
            return;
        }

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Find the flight number
            String flightNumber = null;
            String findSql = "SELECT flight_number FROM bookings WHERE booking_id = ? AND user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(findSql)) {
                stmt.setString(1, bookingId.trim());
                stmt.setString(2, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) flightNumber = rs.getString("flight_number");
                }
            }

            if (flightNumber == null) {
                conn.rollback();
                JOptionPane.showMessageDialog(null, "Booking not found for your account.");
                return;
            }

            // Delete booking
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM bookings WHERE booking_id = ? AND user_id = ?")) {
                stmt.setString(1, bookingId.trim());
                stmt.setString(2, userId);
                stmt.executeUpdate();
            }

            // Restore seat
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE flights SET available_seats = available_seats + 1 WHERE flight_number = ?")) {
                stmt.setString(1, flightNumber);
                stmt.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(null, "Booking cancelled successfully.");
        } catch (SQLException ex) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ignored) {} }
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {} }
        }
    }
}
