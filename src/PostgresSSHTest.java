import com.jcraft.jsch.*;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

public class PostgresSSHTest {

    private String user;
    private String password;

    public PostgresSSHTest() throws IOException {
        File file = new File("/TxtsAccessible/loginpostgresp320_18.txt");
        Scanner scan = new Scanner(file);
        user = scan.nextLine();
        password = scan.nextLine();
        scan.close();
    }
    public static void main(String[] args) {

        try {
            PostgresSSHTest postgresSSHTest = new PostgresSSHTest();
            postgresSSHTest.dbConnect();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public void dbConnect() throws SQLException {

        int lport = 5432;
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432;
        String databaseName = "p320_18"; //change to your database name

        String driverName = "org.postgresql.Driver";
        Connection conn = null;
        Session session = null;
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            session = jsch.getSession(user, rhost, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.setConfig("PreferredAuthentications","publickey,keyboard-interactive,password");
            session.connect();
            System.out.println("Connected");
            int assigned_port = session.setPortForwardingL(lport, "localhost", rport);
            System.out.println("Port Forwarded");

            // Assigned port could be different from 5432 but rarely happens
            String url = "jdbc:postgresql://localhost:"+ assigned_port + "/" + databaseName;

            System.out.println("database Url: " + url);
            Properties props = new Properties();
            props.put("user", user);
            props.put("password", password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);
            System.out.println("Database connection established");

            // Do something with the database....



            ConsoleApp applicationHandler = new ConsoleApp(conn);
            applicationHandler.mainLoop();



        } catch (Exception e) {
            if (e.toString().equals("org.postgresql.util.PSQLException: FATAL: remaining connection slots are" +
                    " reserved for non-replication superuser connections"))
            {
                System.out.println("Unfortunately, the server is full. Please try again later.");
            } else
            {
                e.printStackTrace();
            }
        } finally {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Closing Database Connection");
                conn.close();
            }
            if (session != null && session.isConnected()) {
                System.out.println("Closing SSH Connection");
                session.disconnect();
            }
        }
    }

}
