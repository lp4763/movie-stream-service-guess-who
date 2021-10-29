package ParseData;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class PopulateDatabase {
    private String user;
    private String password;
    //private List<String> accounts;
    //private List<String> movies;
    //private List<String> people;
    public PopulateDatabase()
            throws IOException {
        File file = new File("/TxtsAccessible/loginpostgresp320_18.txt");
        Scanner scan = new Scanner(file);
        user = scan.nextLine();
        password = scan.nextLine();
        scan.close();
       // this.accounts = accounts;
       // this.movies = movies;
       // this.people = people;
    }

    public void connecttoDb() throws SQLException {

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

            Statement movie = conn.createStatement();

            Statement person = conn.createStatement();


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
    public String movieInsertString(){
        String movieInsert = "Insert into movie VALUES(";
        return movieInsert;
    }
    public String personInsertString(){
        String personInsert = "Insert into person VALUES(";
        return personInsert;
    }
}
