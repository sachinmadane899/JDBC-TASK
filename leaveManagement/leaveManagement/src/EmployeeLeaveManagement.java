import java.sql.*;
import java.util.Scanner;

public class EmployeeLeaveManagement {

    static final String URL = "jdbc:mysql://localhost:3306/";
    static final String USER = "root";
    static final String PASSWORD = "12345678";

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

    try {
        Class.forName("com.mysql.cj.jdbc.Driver");

        createDatabaseAndTables();

        while (true) {

            System.out.println("\n********** Employee Leave Management ***********");

            System.out.println("1. Register Employee");
            System.out.println("2. Apply Leave");
            System.out.println("3. Approve Leave");
            System.out.println("4. Reject Leave");
            System.out.println("5. Leave Balance");
            System.out.println("6. Employee Leave Report");
            System.out.println("7. Department Report");
            System.out.println("8. Exit");

            System.out.print("Enter Choice : ");

            int ch = sc.nextInt();

            switch (ch) {

                case 1:
                    registerEmployee();
                    break;

                case 2:
                    applyLeave();
                    break;

                case 3:
                    approveLeave();
                    break;

                case 4:
                    rejectLeave();
                    break;

                case 5:
                    leaveBalance();
                    break;

                case 6:
                    employeeReport();
                    break;

                case 7:
                    departmentReport();
                    break;

                case 8:
                    System.out.println("Thank You");
                    System.exit(0);

                default:
                    System.out.println("Invalid Choice");
            }

        }

    } catch (Exception e) {

        e.printStackTrace();
    }

}


public static void createDatabaseAndTables() throws Exception {

    Connection con = DriverManager.getConnection(URL, USER, PASSWORD);

    Statement st = con.createStatement();

    st.executeUpdate("CREATE DATABASE IF NOT EXISTS leave_db");

    con.close();

    con = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/leave_db",
            USER,
            PASSWORD);

    st = con.createStatement();

    st.executeUpdate(
            "CREATE TABLE IF NOT EXISTS employees(" +
                    "emp_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "name VARCHAR(50)," +
                    "department VARCHAR(50)," +
                    "email VARCHAR(50))");

    st.executeUpdate(
            "CREATE TABLE IF NOT EXISTS leave_balance(" +
                    "emp_id INT PRIMARY KEY," +
                    "total_leave INT DEFAULT 20," +
                    "used_leave INT DEFAULT 0," +
                    "remaining_leave INT DEFAULT 20," +
                    "FOREIGN KEY(emp_id) REFERENCES employees(emp_id))");

    st.executeUpdate(
            "CREATE TABLE IF NOT EXISTS leave_requests(" +
                    "leave_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "emp_id INT," +
                    "from_date DATE," +
                    "to_date DATE," +
                    "days INT," +
                    "reason VARCHAR(100)," +
                    "status VARCHAR(20)," +
                    "FOREIGN KEY(emp_id) REFERENCES employees(emp_id))");

    con.close();

}


public static Connection getConnection() throws Exception {

    return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/leave_db",
            USER,
            PASSWORD);

}


public static void registerEmployee() throws Exception {

    Connection con = getConnection();

    sc.nextLine();

    System.out.print("Enter Name : ");
    String name = sc.nextLine();

    System.out.print("Enter Department : ");
    String department = sc.nextLine();

    System.out.print("Enter Email : ");
    String email = sc.nextLine();

    String sql = "INSERT INTO employees(name,department,email) VALUES(?,?,?)";

    PreparedStatement ps = con.prepareStatement(
            sql,
            Statement.RETURN_GENERATED_KEYS);

    ps.setString(1, name);
    ps.setString(2, department);
    ps.setString(3, email);

    int rows = ps.executeUpdate();

    if (rows > 0) {

        ResultSet rs = ps.getGeneratedKeys();

        if (rs.next()) {

            int empId = rs.getInt(1);

            String balance =
                    "INSERT INTO leave_balance(emp_id,total_leave,used_leave,remaining_leave) VALUES(?,20,0,20)";

            PreparedStatement ps2 = con.prepareStatement(balance);

            ps2.setInt(1, empId);

            ps2.executeUpdate();

            System.out.println("\nEmployee Registered Successfully");

            System.out.println("Employee ID : " + empId);

        }

    }

    con.close();

}


public static void applyLeave() throws Exception {

    Connection con = getConnection();

    System.out.print("Enter Employee ID : ");
    int empId = sc.nextInt();

    sc.nextLine();

    System.out.print("Enter From Date (YYYY-MM-DD) : ");
    String fromDate = sc.nextLine();

    System.out.print("Enter To Date (YYYY-MM-DD) : ");
    String toDate = sc.nextLine();

    System.out.print("Enter Number of Days : ");
    int days = sc.nextInt();

    sc.nextLine();

    System.out.print("Enter Reason : ");
    String reason = sc.nextLine();

    String sql = "INSERT INTO leave_requests(emp_id,from_date,to_date,days,reason,status) VALUES(?,?,?,?,?,'Pending')";

    PreparedStatement ps = con.prepareStatement(sql);

    ps.setInt(1, empId);
    ps.setDate(2, Date.valueOf(fromDate));
    ps.setDate(3, Date.valueOf(toDate));
    ps.setInt(4, days);
    ps.setString(5, reason);

    int rows = ps.executeUpdate();

    if (rows > 0) {

        System.out.println("Leave Applied Successfully.");
    } else {

        System.out.println("Leave Application Failed.");
    }

    con.close();
}


public static void leaveBalance() throws Exception {

    Connection con = getConnection();

    System.out.print("Enter Employee ID : ");
    int empId = sc.nextInt();

    String sql = "SELECT * FROM leave_balance WHERE emp_id=?";

    PreparedStatement ps = con.prepareStatement(sql);

    ps.setInt(1, empId);

    ResultSet rs = ps.executeQuery();

    if (rs.next()) {

        System.out.println("\n----- Leave Balance -----");

        System.out.println("Employee ID      : " + rs.getInt("emp_id"));
        System.out.println("Total Leave      : " + rs.getInt("total_leave"));
        System.out.println("Used Leave       : " + rs.getInt("used_leave"));
        System.out.println("Remaining Leave  : " + rs.getInt("remaining_leave"));

    } else {

        System.out.println("Employee Not Found.");
    }

    con.close();
}


public static void approveLeave() throws Exception {

    Connection con = getConnection();

    con.setAutoCommit(false);

    try {

        System.out.print("Enter Leave ID : ");
        int leaveId = sc.nextInt();

        String selectQuery =
                "SELECT emp_id,days FROM leave_requests " +
                "WHERE leave_id=? AND status='Pending'";

        PreparedStatement ps1 =
                con.prepareStatement(selectQuery);

        ps1.setInt(1, leaveId);

        ResultSet rs = ps1.executeQuery();

        if (rs.next()) {

            int empId = rs.getInt("emp_id");
            int days = rs.getInt("days");

            String balanceQuery =
                    "SELECT remaining_leave FROM leave_balance WHERE emp_id=?";

            PreparedStatement ps2 =
                    con.prepareStatement(balanceQuery);

            ps2.setInt(1, empId);

            ResultSet rs2 = ps2.executeQuery();

            if (rs2.next()) {

                int remaining = rs2.getInt("remaining_leave");

                if (remaining >= days) {

                    String approve =
                            "UPDATE leave_requests SET status='Approved' WHERE leave_id=?";

                    PreparedStatement ps3 =
                            con.prepareStatement(approve);

                    ps3.setInt(1, leaveId);

                    ps3.executeUpdate();

                    String updateBalance =
                            "UPDATE leave_balance " +
                            "SET used_leave=used_leave+?, " +
                            "remaining_leave=remaining_leave-? " +
                            "WHERE emp_id=?";

                    PreparedStatement ps4 =
                            con.prepareStatement(updateBalance);

                    ps4.setInt(1, days);
                    ps4.setInt(2, days);
                    ps4.setInt(3, empId);

                    ps4.executeUpdate();

                    con.commit();

                    System.out.println("Leave Approved Successfully.");

                } else {

                    System.out.println("Insufficient Leave Balance.");

                    con.rollback();

                }

            }

        } else {

            System.out.println("Leave Request Not Found.");

            con.rollback();

        }

    } catch (Exception e) {

        con.rollback();

        e.printStackTrace();

    }

    con.setAutoCommit(true);

    con.close();

}


public static void rejectLeave() throws Exception {

    Connection con = getConnection();

    System.out.print("Enter Leave ID : ");

    int leaveId = sc.nextInt();

    String sql =
            "UPDATE leave_requests SET status='Rejected' WHERE leave_id=?";

    PreparedStatement ps =
            con.prepareStatement(sql);

    ps.setInt(1, leaveId);

    int rows = ps.executeUpdate();

    if (rows > 0) {

        System.out.println("Leave Rejected Successfully.");

    } else {

        System.out.println("Leave Request Not Found.");

    }

    con.close();

}


public static void employeeReport() throws Exception {

    Connection con = getConnection();

    String sql =
            "SELECT e.emp_id,e.name,e.department," +
            "l.leave_id,l.from_date,l.to_date,l.days,l.reason,l.status " +
            "FROM employees e " +
            "JOIN leave_requests l " +
            "ON e.emp_id=l.emp_id";

    Statement st = con.createStatement();

    ResultSet rs = st.executeQuery(sql);

    System.out.println("\n================ Employee Leave Report ================");

    while (rs.next()) {

        System.out.println("------------------------------------------");

        System.out.println("Employee ID : " + rs.getInt("emp_id"));
        System.out.println("Name        : " + rs.getString("name"));
        System.out.println("Department  : " + rs.getString("department"));
        System.out.println("Leave ID    : " + rs.getInt("leave_id"));
        System.out.println("From Date   : " + rs.getDate("from_date"));
        System.out.println("To Date     : " + rs.getDate("to_date"));
        System.out.println("Days        : " + rs.getInt("days"));
        System.out.println("Reason      : " + rs.getString("reason"));
        System.out.println("Status      : " + rs.getString("status"));

    }

    con.close();

}


public static void departmentReport() throws Exception {

    Connection con = getConnection();

    String sql =
            "SELECT e.department," +
            "SUM(l.days) AS total_days " +
            "FROM employees e " +
            "JOIN leave_requests l " +
            "ON e.emp_id=l.emp_id " +
            "WHERE l.status='Approved' " +
            "GROUP BY e.department";

    Statement st = con.createStatement();

    ResultSet rs = st.executeQuery(sql);

    System.out.println("\n========== Department Report ==========");

    while (rs.next()) {

        System.out.println(
                rs.getString("department")
                        + " --> "
                        + rs.getInt("total_days")
                        + " Days");

    }

    con.close();

}
}