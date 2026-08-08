import java.sql.*;
import java.util.Scanner;

public class PayrollManagement {

    static final String URL = "jdbc:mysql://localhost:3306/";
    static final String USER = "root";
    static final String PASSWORD = "12345678";

    static final String DB = "payroll_management";

    static Scanner sc = new Scanner(System.in);

    // ================== MAIN ===================

    public static void main(String[] args) {

        createDatabase();
        createTables();

        while (true) {

            System.out.println("\n========== PAYROLL MANAGEMENT ==========");
            System.out.println("1. Add Employee");
            System.out.println("2. View Employees");
            System.out.println("3. Salary Calculation");
            System.out.println("4. Generate Payslip");
            System.out.println("5. Tax Deduction");
            System.out.println("6. Salary Report");
            System.out.println("7. Exit");
            System.out.print("Enter Choice : ");

            int choice = sc.nextInt();

            switch (choice) {

                case 1:
                    addEmployee();
                    break;

                case 2:
                    viewEmployees();
                    break;

                case 3:
                    calculateSalary();
                    break;

                case 4:
                    generatePayslip();
                    break;

                case 5:
                    taxDeduction();
                    break;

                case 6:
                    salaryReport();
                    break;

                case 7:
                    System.exit(0);

                default:
                    System.out.println("Invalid Choice");
            }

        }

    }

    // ================= DATABASE =================

    static void createDatabase() {

        try {

            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);

            Statement st = con.createStatement();

            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB);

            System.out.println("Database Ready");

            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // ================= TABLES ================

    static void createTables() {

        try {

            Connection con = DriverManager.getConnection(URL + DB, USER, PASSWORD);

            Statement st = con.createStatement();

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS employees("
                            + "emp_id INT PRIMARY KEY AUTO_INCREMENT,"
                            + "name VARCHAR(100),"
                            + "department VARCHAR(100),"
                            + "designation VARCHAR(100),"
                            + "basic_salary DOUBLE)");

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS salaries("
                            + "salary_id INT PRIMARY KEY AUTO_INCREMENT,"
                            + "emp_id INT,"
                            + "basic DOUBLE,"
                            + "hra DOUBLE,"
                            + "da DOUBLE,"
                            + "tax DOUBLE,"
                            + "net_salary DOUBLE,"
                            + "FOREIGN KEY(emp_id) REFERENCES employees(emp_id))");

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS payroll("
                            + "payroll_id INT PRIMARY KEY AUTO_INCREMENT,"
                            + "emp_id INT,"
                            + "pay_month VARCHAR(20),"
                            + "pay_year INT,"
                            + "payment_date DATE,"
                            + "FOREIGN KEY(emp_id) REFERENCES employees(emp_id))");

            System.out.println("Tables Ready");

            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // ================= CONNECTION ==================

    static Connection getConnection() throws Exception {

        return DriverManager.getConnection(URL + DB, USER, PASSWORD);

    }

    // ================== ADD EMPLOYEE =================

    static void addEmployee() {

        try {

            Connection con = getConnection();

            System.out.print("Employee Name : ");
            sc.nextLine();
            String name = sc.nextLine();

            System.out.print("Department : ");
            String dept = sc.nextLine();

            System.out.print("Designation : ");
            String des = sc.nextLine();

            System.out.print("Basic Salary : ");
            double salary = sc.nextDouble();

            String sql = "INSERT INTO employees(name,department,designation,basic_salary) VALUES(?,?,?,?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, dept);
            ps.setString(3, des);
            ps.setDouble(4, salary);

            int rows = ps.executeUpdate();

            if (rows > 0)
                System.out.println("Employee Added Successfully");

            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // -------------------- VIEW EMPLOYEES --------------------

    static void viewEmployees() {

        try {

            Connection con = getConnection();

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery("SELECT * FROM employees");

            System.out.println("\n------------------------------------------------------------");
            System.out.println("ID\tNAME\tDEPARTMENT\tDESIGNATION\tSALARY");
            System.out.println("------------------------------------------------------------");

            while (rs.next()) {

                System.out.println(
                        rs.getInt("emp_id") + "\t"
                                + rs.getString("name") + "\t"
                                + rs.getString("department") + "\t"
                                + rs.getString("designation") + "\t"
                                + rs.getDouble("basic_salary"));

            }

            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }


    // ==================== SALARY CALCULATION ==================

static void calculateSalary() {

    try {

        Connection con = getConnection();

        System.out.print("Enter Employee ID : ");
        int id = sc.nextInt();

        String sql = "SELECT basic_salary FROM employees WHERE emp_id=?";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (!rs.next()) {

            System.out.println("Employee Not Found");
            return;

        }

        double basic = rs.getDouble("basic_salary");

        double hra = basic * 0.20;      // 20%
        double da = basic * 0.10;       // 10%
        double tax = basic * 0.05;      // 5%

        double net = basic + hra + da - tax;

        String insert = "INSERT INTO salaries(emp_id,basic,hra,da,tax,net_salary) VALUES(?,?,?,?,?,?)";

        PreparedStatement ps2 = con.prepareStatement(insert);

        ps2.setInt(1, id);
        ps2.setDouble(2, basic);
        ps2.setDouble(3, hra);
        ps2.setDouble(4, da);
        ps2.setDouble(5, tax);
        ps2.setDouble(6, net);

        ps2.executeUpdate();

        System.out.println("Salary Calculated Successfully");
        System.out.println("Net Salary : " + net);

        con.close();

    } catch (Exception e) {

        e.printStackTrace();

    }

}

// =================== TAX DEDUCTION ===================

static void taxDeduction() {

    try {

        Connection con = getConnection();

        System.out.print("Enter Employee ID : ");
        int id = sc.nextInt();

        String sql = "SELECT tax FROM salaries WHERE emp_id=? ORDER BY salary_id DESC LIMIT 1";

        PreparedStatement ps = con.prepareStatement(sql);

        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            System.out.println("Tax Deduction : " + rs.getDouble("tax"));

        } else {

            System.out.println("Salary Not Calculated Yet");

        }

        con.close();

    } catch (Exception e) {

        e.printStackTrace();

    }

}

// =================== BATCH PROCESSING ===================

static void batchPayroll() {

    try {

        Connection con = getConnection();

        con.setAutoCommit(false);

        Statement st = con.createStatement();

        st.addBatch(
            "INSERT INTO payroll(emp_id,pay_month,pay_year,payment_date) " +
            "VALUES(1,'August',2026,CURDATE())");

        st.addBatch(
            "INSERT INTO payroll(emp_id,pay_month,pay_year,payment_date) " +
            "VALUES(2,'August',2026,CURDATE())");

        st.addBatch(
            "INSERT INTO payroll(emp_id,pay_month,pay_year,payment_date) " +
            "VALUES(3,'August',2026,CURDATE())");

        st.executeBatch();

        con.commit();

        System.out.println("Batch Processing Completed");

        con.close();

    } catch (Exception e) {

        e.printStackTrace();

    }

}


// =================== GENERATE PAYSLIP ===================

static void generatePayslip() {

    try {

        Connection con = getConnection();

        System.out.print("Enter Employee ID : ");
        int id = sc.nextInt();

        String sql = "SELECT e.name,e.department,e.designation,"
                + "s.basic,s.hra,s.da,s.tax,s.net_salary "
                + "FROM employees e "
                + "JOIN salaries s ON e.emp_id=s.emp_id "
                + "WHERE e.emp_id=? "
                + "ORDER BY s.salary_id DESC LIMIT 1";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            System.out.println("\n========== PAYSLIP ==========");
            System.out.println("Employee ID   : " + id);
            System.out.println("Name          : " + rs.getString("name"));
            System.out.println("Department    : " + rs.getString("department"));
            System.out.println("Designation   : " + rs.getString("designation"));
            System.out.println("-----------------------------");
            System.out.println("Basic Salary  : " + rs.getDouble("basic"));
            System.out.println("HRA           : " + rs.getDouble("hra"));
            System.out.println("DA            : " + rs.getDouble("da"));
            System.out.println("Tax           : " + rs.getDouble("tax"));
            System.out.println("-----------------------------");
            System.out.println("Net Salary    : " + rs.getDouble("net_salary"));
            System.out.println("=============================");

        } else {

            System.out.println("Salary Record Not Found");

        }

        con.close();

    } catch (Exception e) {

        e.printStackTrace();

    }

}

// =================== SALARY REPORT ===================

static void salaryReport() {

    try {

        Connection con = getConnection();

        Statement st = con.createStatement();

        ResultSet rs = st.executeQuery(
                "SELECT COUNT(*) totalEmployees,"
                        + "SUM(net_salary) totalSalary,"
                        + "AVG(net_salary) averageSalary "
                        + "FROM salaries");

        if (rs.next()) {

            System.out.println("\n========== SALARY REPORT ==========");
            System.out.println("Employees Paid : " + rs.getInt("totalEmployees"));
            System.out.println("Total Salary   : " + rs.getDouble("totalSalary"));
            System.out.println("Average Salary : " + rs.getDouble("averageSalary"));
            System.out.println("===================================");

        }

        con.close();

    } catch (Exception e) {

        e.printStackTrace();

    }

  }

}