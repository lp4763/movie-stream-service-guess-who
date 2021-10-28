import com.jcraft.jsch.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.Scanner;

public class PostgresSSHTest {


    public static void main(String[] args) throws SQLException {

        int lport = 5432;
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432;
        String user = "nas9053"; //change to your username
        String password = "---"; //change to your password
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
            userLoop();

            Statement testStatement = conn.createStatement();
            // System.out.println(conn.);
            ResultSet rs = testStatement.executeQuery("select * from Account");
            while (rs.next()) {
                System.out.print("Column 1 returned ");
                System.out.println(rs.getString(1));
            }


        } catch (Exception e) {
            e.printStackTrace();
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

    private static void userLoop() {
        Scanner in = new Scanner(System.in);
        System.out.println("You have been connected to the server. " +
                "Please log in using by entering your username and password, separated by a space. " +
                "If you do not have a login, please enter \"!NewUser\"");
        while (true)
        {
            String line = in.nextLine();
            if (line.toLowerCase().equals("!newuser"))
            {
                System.out.println("Creating a new user. What do you want your username to be? (This is displayed publicly, and will be used for prior logins)");
            }
            String[] split = line.split(" ");
            if (split.length != 2 || split[0].length() <= 0 || split[1].length() <= 0)
            {
                System.out.println("Your input does not match format requirements. Please input your username and password as:\n<username> <password>");
                continue;
            }
            String username = split[0];
            String password = split[1];

            break;
        }

    }
}
