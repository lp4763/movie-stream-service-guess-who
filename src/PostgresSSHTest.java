import com.jcraft.jsch.*;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDateTime;
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
            String username = userLoop(conn);
            System.out.println("Successfully logged in as " + username);


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

    private static String userLoop( Connection conn) throws SQLException {
        Scanner in = new Scanner(System.in);
        System.out.println("You have been connected to the server. " +
                "Please log in using by entering your username and password:");
        while (true)
        {System.out.println("Please log in using by entering your username and password:");
            String[] input = getUserInput(in, "Your input does not match format requirements. Please input your username and password as:\n<username> <password>", 2);
            String username = input[0];
            String password = input[1];

            Statement loginStatement = conn.createStatement();
            // System.out.println(conn.);
            ResultSet rs = loginStatement.executeQuery("SELECT password FROM account WHERE username=\'" + username + "\' AND password=\'" + password + "\';");
            if (rs.next())
            {
                Statement updateTimeStatement = conn.createStatement();
                updateTimeStatement.execute("UPDATE account SET lastaccessdate=current_date WHERE username='" + username + "';");
                return username;
            }
            else
            {
                System.out.println("Account not found. Would you like to create a new account? (y/n)");
                String yes = in.nextLine();
                if (yes.toLowerCase().equals("y"))
                {
                    username = createAccount(in, conn, username);
                    if (username.length() > 0)
                    {
                        Statement updateTimeStatement = conn.createStatement();
                        updateTimeStatement.execute("UPDATE account SET lastaccessdate=current_date WHERE username='" + username + "';");
                        return username;
                    }
                }
            }
        }

    }

    private static String createAccount(Scanner in, Connection conn, String username) throws SQLException{
        Statement takenStatement = conn.createStatement();
        // System.out.println(conn.);
        ResultSet rs = takenStatement.executeQuery("SELECT username FROM account WHERE username=\'" + username + "\';");
        while (rs.next())
        {
            System.out.println("Unfortunately, that username is already taken. Please input another username.");
            username = getUserInput(in, "Your input does not match format requirements. Please input your new username as\n<username>");
            rs = takenStatement.executeQuery("SELECT username FROM account WHERE username=\'" + username + "\';");
        }
        System.out.println("Please input your password:");
        String password = getUserInput(in, "Your input does not match format requirements. Please input your new password as\n<password>\"");
        System.out.println("Please input your first name and last name, separated by a space:");
        String[] name = getUserInput(in, "Your input does not match format requirements. Please input your first and last name as:\n<firstname> <lastname>", 2);
        System.out.println("Please input your email:");
        String email = getUserInput(in, "Your input does not match format requirements. Please input your email as:\n<email>");
        java.sql.Date sqlDate = new java.sql.Date(Instant.now().toEpochMilli());

        Statement createAccountStatement = conn.createStatement();
        String values = formatForInsert(new String[] {username, password, name[0], name[1], email, sqlDate.toString(), sqlDate.toString()});
        System.out.println(values);
        takenStatement.execute("INSERT INTO account VALUES " + values);
        return username;
    }

    private static String[] getUserInput(Scanner in, String invalidPrompt, int args)
    {
        boolean flag = true;
        String[] split = {""};
        while (flag)
        {
            flag = false;
            String line = in.nextLine();
            split = line.split(" ");
            if (split.length != args)
            {
                System.out.println(invalidPrompt);
                flag = true;
            }
            else {
                for (int i = 0; i < args; i++) {
                    if (split[i].length() <= 0) {
                        System.out.println(invalidPrompt);
                        flag = true;
                    }
                }
            }
        }
        return split;
    }

    private static String getUserInput(Scanner in, String invalidPrompt)
    {
        return getUserInput(in, invalidPrompt, 1)[0];
    }

    private static String formatForInsert(String[] values)
    {
        String output = "(";
        for (int i = 0; i < values.length; i++) {
            output += "'";
            output += values[i];
            output += "'";
            if (i < values.length - 1)
            {
                output += ", ";
            }
        }
        output += ")";
        return output;
    }
}
