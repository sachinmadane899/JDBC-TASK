import java.sql.*;
import java.util.Scanner;

public class ComplaintManagement {

    static final String URL = "jdbc:mysql://localhost:3306/";
    static final String USER = "root";
    static final String PASS = "12345678";

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(URL, USER, PASS);
            Statement st = con.createStatement();

            st.executeUpdate("CREATE DATABASE IF NOT EXISTS complaintManagement");
            st.execute("USE complaintManagement");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS users("
                    + "id INT PRIMARY KEY AUTO_INCREMENT,"
                    + "name VARCHAR(100),"
                    + "phone VARCHAR(15))");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS officers("
                    + "id INT PRIMARY KEY AUTO_INCREMENT,"
                    + "name VARCHAR(100),"
                    + "department VARCHAR(100))");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS complaints("
                    + "id INT PRIMARY KEY AUTO_INCREMENT,"
                    + "user_id INT,"
                    + "officer_id INT,"
                    + "complaint VARCHAR(255),"
                    + "status VARCHAR(50),"
                    + "resolution VARCHAR(255))");

            while (true) {

                System.out.println("\n===== Complaint Management =====");
                System.out.println("1. Register Complaint");
                System.out.println("2. Assign Officer");
                System.out.println("3. Update Status");
                System.out.println("4. View Resolution");
                System.out.println("5. Exit");

                System.out.print("Enter Choice : ");
                int ch = sc.nextInt();
                sc.nextLine();

                switch (ch) {

                    case 1:

                        System.out.print("Citizen Name : ");
                        String name = sc.nextLine();

                        System.out.print("Phone : ");
                        String phone = sc.nextLine();

                        PreparedStatement ps1 = con.prepareStatement(
                                "INSERT INTO users(name,phone) VALUES(?,?)",
                                Statement.RETURN_GENERATED_KEYS);

                        ps1.setString(1, name);
                        ps1.setString(2, phone);
                        ps1.executeUpdate();

                        ResultSet rs = ps1.getGeneratedKeys();
                        rs.next();
                        int userId = rs.getInt(1);

                        System.out.print("Complaint : ");
                        String comp = sc.nextLine();

                        PreparedStatement ps2 = con.prepareStatement(
                                "INSERT INTO complaints(user_id,complaint,status) VALUES(?,?,?)");

                        ps2.setInt(1, userId);
                        ps2.setString(2, comp);
                        ps2.setString(3, "Pending");
                        ps2.executeUpdate();

                        System.out.println("Complaint Registered.");
                        break;

                    case 2:

                        System.out.print("Officer Name : ");
                        String oname = sc.nextLine();

                        System.out.print("Department : ");
                        String dept = sc.nextLine();

                        PreparedStatement ps3 = con.prepareStatement(
                                "INSERT INTO officers(name,department) VALUES(?,?)",
                                Statement.RETURN_GENERATED_KEYS);

                        ps3.setString(1, oname);
                        ps3.setString(2, dept);
                        ps3.executeUpdate();

                        ResultSet rs2 = ps3.getGeneratedKeys();
                        rs2.next();
                        int officerId = rs2.getInt(1);

                        System.out.print("Complaint ID : ");
                        int cid = sc.nextInt();

                        PreparedStatement ps4 = con.prepareStatement(
                                "UPDATE complaints SET officer_id=? WHERE id=?");

                        ps4.setInt(1, officerId);
                        ps4.setInt(2, cid);
                        ps4.executeUpdate();

                        System.out.println("Officer Assigned.");
                        break;

                    case 3:

                        System.out.print("Complaint ID : ");
                        int id = sc.nextInt();
                        sc.nextLine();

                        System.out.print("Status : ");
                        String status = sc.nextLine();

                        System.out.print("Resolution : ");
                        String res = sc.nextLine();

                        PreparedStatement ps5 = con.prepareStatement(
                                "UPDATE complaints SET status=?,resolution=? WHERE id=?");

                        ps5.setString(1, status);
                        ps5.setString(2, res);
                        ps5.setInt(3, id);

                        ps5.executeUpdate();

                        System.out.println("Status Updated.");
                        break;

                    case 4:

                        Statement s = con.createStatement();

                        ResultSet r = s.executeQuery(
                                "SELECT c.id,u.name,o.name,c.complaint,c.status,c.resolution "
                                + "FROM complaints c "
                                + "LEFT JOIN users u ON c.user_id=u.id "
                                + "LEFT JOIN officers o ON c.officer_id=o.id");

                        while (r.next()) {

                            System.out.println("-------------------------");
                            System.out.println("Complaint ID : " + r.getInt(1));
                            System.out.println("Citizen      : " + r.getString(2));
                            System.out.println("Officer      : " + r.getString(3));
                            System.out.println("Complaint    : " + r.getString(4));
                            System.out.println("Status       : " + r.getString(5));
                            System.out.println("Resolution   : " + r.getString(6));
                        }

                        break;

                    case 5:
                        con.close();
                        System.exit(0);

                    default:
                        System.out.println("Invalid Choice");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}