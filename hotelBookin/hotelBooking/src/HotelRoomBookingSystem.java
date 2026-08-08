import java.sql.*;
import java.util.Scanner;

public class HotelRoomBookingSystem {

    static final String URL = "jdbc:mysql://localhost:3306/";
    static final String USER = "root";
    static final String PASS = "12345678";

    static final String DB = "hotel_booking";

    static Scanner sc = new Scanner(System.in);


    public static void main(String[] args) {

        createDatabase();
        createTables();
        insertRooms();

        while (true) {

            System.out.println("\n========= HOTEL ROOM BOOKING =========");
            System.out.println("1. Room Availability");
            System.out.println("2. Book Room");
            System.out.println("3. Check-In");
            System.out.println("4. Check-Out");
            System.out.println("5. Billing");
            System.out.println("6. Exit");
            System.out.print("Enter Choice : ");

            int ch = sc.nextInt();

            switch (ch) {

                case 1:
                    roomAvailability();
                    break;

                case 2:
                    booking();
                    break;

                case 3:
                    checkIn();
                    break;

                case 4:
                    checkOut();
                    break;

                case 5:
                    billing();
                    break;

                case 6:
                    System.out.println("Thank You");
                    System.exit(0);

                default:
                    System.out.println("Invalid Choice");
            }

        }

    }

    //================ CONNECTION ==================

    static Connection getConnection() throws Exception {

        return DriverManager.getConnection(
                URL + DB,
                USER,
                PASS);

    }

    //================ DATABASE ==================

    static void createDatabase() {

        try {

            Connection con = DriverManager.getConnection(URL, USER, PASS);

            Statement st = con.createStatement();

            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB);

            con.close();

        }

        catch (Exception e) {

            e.printStackTrace();

        }

    }

    //================ TABLES ==================

    static void createTables() {

        try {

            Connection con = getConnection();

            Statement st = con.createStatement();

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS rooms("
                            + "room_id INT PRIMARY KEY,"
                            + "room_type VARCHAR(30),"
                            + "price DOUBLE,"
                            + "status VARCHAR(20))");

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS customers("
                            + "customer_id INT AUTO_INCREMENT PRIMARY KEY,"
                            + "name VARCHAR(50),"
                            + "phone VARCHAR(20))");

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS bookings("
                            + "booking_id INT AUTO_INCREMENT PRIMARY KEY,"
                            + "customer_id INT,"
                            + "room_id INT,"
                            + "checkin DATE,"
                            + "checkout DATE,"
                            + "bill DOUBLE,"
                            + "status VARCHAR(20),"
                            + "FOREIGN KEY(customer_id) REFERENCES customers(customer_id),"
                            + "FOREIGN KEY(room_id) REFERENCES rooms(room_id))");

            con.close();

        }

        catch (Exception e) {

            e.printStackTrace();

        }

    }

    //================ INSERT SAMPLE ROOMS ==================

    static void insertRooms() {

        try {

            Connection con = getConnection();

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM rooms");

            rs.next();

            if (rs.getInt(1) == 0) {

                st.executeUpdate("INSERT INTO rooms VALUES(101,'Single',1000,'Available')");
                st.executeUpdate("INSERT INTO rooms VALUES(102,'Single',1000,'Available')");
                st.executeUpdate("INSERT INTO rooms VALUES(201,'Double',2000,'Available')");
                st.executeUpdate("INSERT INTO rooms VALUES(202,'Double',2000,'Available')");
                st.executeUpdate("INSERT INTO rooms VALUES(301,'Suite',5000,'Available')");

            }

            con.close();

        }

        catch (Exception e) {

            e.printStackTrace();

        }

    }

    //================ ROOM AVAILABILITY ==================

    static void roomAvailability() {

        try {

            Connection con = getConnection();

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery("SELECT * FROM rooms");

            System.out.println();

            System.out.println("Room\tType\tPrice\tStatus");

            while (rs.next()) {

                System.out.println(
                        rs.getInt("room_id") + "\t"
                                + rs.getString("room_type") + "\t"
                                + rs.getDouble("price") + "\t"
                                + rs.getString("status"));

            }

            con.close();

        }

        catch (Exception e) {

            e.printStackTrace();

        }

    }


    //================ BOOK ROOM ==================

static void booking() {

    try {

        Connection con = getConnection();

        con.setAutoCommit(false);

        System.out.print("Customer Name : ");
        sc.nextLine();
        String name = sc.nextLine();

        System.out.print("Phone : ");
        String phone = sc.nextLine();

        System.out.print("Room ID : ");
        int roomId = sc.nextInt();

        System.out.print("Check-In Date (YYYY-MM-DD) : ");
        Date checkIn = Date.valueOf(sc.next());

        System.out.print("Check-Out Date (YYYY-MM-DD) : ");
        Date checkOut = Date.valueOf(sc.next());

        // Check Room Availability
        PreparedStatement ps1 = con.prepareStatement(
                "SELECT status FROM rooms WHERE room_id=?");

        ps1.setInt(1, roomId);

        ResultSet rs = ps1.executeQuery();

        if (!rs.next()) {

            System.out.println("Room Not Found.");

            con.rollback();
            con.close();
            return;
        }

        if (!rs.getString("status").equalsIgnoreCase("Available")) {

            System.out.println("Room Already Booked.");

            con.rollback();
            con.close();
            return;
        }

        // Insert Customer
        PreparedStatement ps2 = con.prepareStatement(
                "INSERT INTO customers(name,phone) VALUES(?,?)",
                Statement.RETURN_GENERATED_KEYS);

        ps2.setString(1, name);
        ps2.setString(2, phone);

        ps2.executeUpdate();

        ResultSet key = ps2.getGeneratedKeys();

        key.next();

        int customerId = key.getInt(1);

        // Room Price
        PreparedStatement ps3 = con.prepareStatement(
                "SELECT price FROM rooms WHERE room_id=?");

        ps3.setInt(1, roomId);

        ResultSet rsPrice = ps3.executeQuery();

        rsPrice.next();

        double price = rsPrice.getDouble("price");

        long days = (checkOut.getTime() - checkIn.getTime())
                / (1000 * 60 * 60 * 24);

        if (days <= 0)
            days = 1;

        double bill = days * price;

        // Booking
        PreparedStatement ps4 = con.prepareStatement(
                "INSERT INTO bookings(customer_id,room_id,checkin,checkout,bill,status)"
                        + " VALUES(?,?,?,?,?,?)");

        ps4.setInt(1, customerId);
        ps4.setInt(2, roomId);
        ps4.setDate(3, checkIn);
        ps4.setDate(4, checkOut);
        ps4.setDouble(5, bill);
        ps4.setString(6, "Booked");

        ps4.executeUpdate();

        // Update Room Status
        PreparedStatement ps5 = con.prepareStatement(
                "UPDATE rooms SET status='Booked' WHERE room_id=?");

        ps5.setInt(1, roomId);

        ps5.executeUpdate();

        con.commit();

        System.out.println("\nRoom Booked Successfully.");
        System.out.println("Customer ID : " + customerId);
        System.out.println("Total Bill : " + bill);

        con.close();

    }

    catch (Exception e) {

        e.printStackTrace();

    }

}


//================ CHECK IN ==================

static void checkIn() {

    try {

        Connection con = getConnection();

        System.out.print("Booking ID : ");
        int bookingId = sc.nextInt();

        PreparedStatement ps = con.prepareStatement(
                "UPDATE bookings SET status='Checked-In' WHERE booking_id=?");

        ps.setInt(1, bookingId);

        int i = ps.executeUpdate();

        if (i > 0)
            System.out.println("Customer Checked-In Successfully.");
        else
            System.out.println("Booking Not Found.");

        con.close();

    }

    catch (Exception e) {

        e.printStackTrace();

    }

}

//================ CHECK OUT ==================

static void checkOut() {

    try {

        Connection con = getConnection();

        System.out.print("Booking ID : ");
        int bookingId = sc.nextInt();

        // Get Room ID
        PreparedStatement ps1 = con.prepareStatement(
                "SELECT room_id FROM bookings WHERE booking_id=?");

        ps1.setInt(1, bookingId);

        ResultSet rs = ps1.executeQuery();

        if (!rs.next()) {

            System.out.println("Booking Not Found.");
            con.close();
            return;

        }

        int roomId = rs.getInt("room_id");

        // Update Booking Status
        PreparedStatement ps2 = con.prepareStatement(
                "UPDATE bookings SET status='Checked-Out' WHERE booking_id=?");

        ps2.setInt(1, bookingId);
        ps2.executeUpdate();

        // Make Room Available Again
        PreparedStatement ps3 = con.prepareStatement(
                "UPDATE rooms SET status='Available' WHERE room_id=?");

        ps3.setInt(1, roomId);
        ps3.executeUpdate();

        System.out.println("Customer Checked-Out Successfully.");

        con.close();

    }

    catch (Exception e) {

        e.printStackTrace();

    }

}

//================ BILLING ==================

static void billing() {

    try {

        Connection con = getConnection();

        System.out.print("Booking ID : ");
        int bookingId = sc.nextInt();

        PreparedStatement ps = con.prepareStatement(

                "SELECT b.booking_id,"
                        + "c.name,"
                        + "r.room_id,"
                        + "r.room_type,"
                        + "b.checkin,"
                        + "b.checkout,"
                        + "b.bill,"
                        + "b.status "
                        + "FROM bookings b "
                        + "JOIN customers c ON b.customer_id=c.customer_id "
                        + "JOIN rooms r ON b.room_id=r.room_id "
                        + "WHERE b.booking_id=?");

        ps.setInt(1, bookingId);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            System.out.println("\n========== BILL ==========");

            System.out.println("Booking ID : " + rs.getInt("booking_id"));
            System.out.println("Customer   : " + rs.getString("name"));
            System.out.println("Room ID    : " + rs.getInt("room_id"));
            System.out.println("Room Type  : " + rs.getString("room_type"));
            System.out.println("Check-In   : " + rs.getDate("checkin"));
            System.out.println("Check-Out  : " + rs.getDate("checkout"));
            System.out.println("Status     : " + rs.getString("status"));
            System.out.println("Total Bill : " + rs.getDouble("bill"));

            System.out.println("==========================");

        } else {

            System.out.println("Booking Not Found.");

        }

        con.close();

    }

    catch (Exception e) {

        e.printStackTrace();

    }

  }

}
