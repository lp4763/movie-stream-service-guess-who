import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Scanner;
import java.util.function.Function;

public class ConsoleApp {
    Connection conn;
    private enum Context { exit, login, password, createUser, loggedIn}

    public ConsoleApp(Connection nConn)
    {
        conn = nConn;
    }

    public int mainLoop() throws SQLException
    {
        System.out.println("You have been connected to the server.");
        Scanner in = new Scanner(System.in);
        Context current = Context.login;
        while ( current != Context.exit)
        {
            switch (current) {
                case login:
                    System.out.println("Please log in using by entering your username and password:");
                    break;

                case createUser:
                    System.out.println("Please create a new user by entering your username, password, firstname, lastname, and email.");
                    break;
            }
            String command = in.nextLine();
            if (command.equals("quit") || command.equals("exit"))
            {
                current = Context.exit;
            }
            else {
                switch (current) {
                    case login:
                        current = tryLogin(conn, command);
                        break;
                    case createUser:
                        current = tryCreateUser(conn, command);
                        break;
                }
            }

            //String username = userLoop(conn);
            //System.out.println("Successfully logged in as " + username);
        }

        return 0;
    }

    private Context tryCreateUser(Connection conn, String command) throws SQLException {
        if (command.equals("cancel") || command.equals("stop") || command.equals("back"))
        {
            return Context.login;
        }
        String[] input = command.split(" ");
        if (input.length != 5)
        {
            System.out.println("Your input does not match format requirements. Please input your user info as:\n<username> <password> <firstname> <lastname> <email>");
            return Context.createUser;
        }
        if (input[0].length() <= 0 || input[0].length() > 63)
        {
            System.out.println("Your input does not match format requirements. Please make sure your username is between 1 and 63 characters in length.");
            return Context.createUser;
        }
        if (input[1].length() <= 0 || input[1].length() > 63)
        {
            System.out.println("Your input does not match format requirements. Please make sure your password is between 1 and 63 characters in length.");
            return Context.createUser;
        }
        if (input[2].length() <= 0 || input[2].length() > 63)
        {
            System.out.println("Your input does not match format requirements. Please make sure your first name is between 1 and 63 characters in length.");
            return Context.createUser;
        }
        if (input[3].length() <= 0 || input[3].length() > 63)
        {
            System.out.println("Your input does not match format requirements. Please make sure your last name is between 1 and 63 characters in length.");
            return Context.createUser;
        }
        if (input[4].length() <= 0 || input[4].length() > 63)
        {
            System.out.println("Your input does not match format requirements. Please make sure your email is between 1 and 63 characters in length.");
            return Context.createUser;
        }
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(Instant.now().toEpochMilli());

        Statement createAccountStatement = conn.createStatement();
        String values = formatForInsert(new String[] {input[0], input[1], input[2], input[3], sqlDate.toString(), input[4]});
        System.out.println(values);
        createAccountStatement.execute("INSERT INTO account VALUES " + values);
        return Context.loggedIn;
    }

    private Context tryLogin(Connection conn, String command) throws SQLException
    {
        if (command.equals("create") || command.equals("new"))
        {
            return Context.createUser;
        }
        String[] input = command.split(" ");
        if (input.length != 2)
        {
            System.out.println("Your input does not match format requirements. Please input your username and password as:\n<username> <password>");
            return Context.login;
        }
        if (input[0].length() <= 0 || input[0].length() > 63)
        {
            System.out.println("Your input does not match format requirements. Please make sure your username is between 1 and 63 characters in length.");
            return Context.login;
        }
        if (input[1].length() <= 0 || input[1].length() > 63)
        {
            System.out.println("Your input does not match format requirements. Please make sure your password is between 1 and 63 characters in length.");
            return Context.login;
        }

        String username = input[0];
        String password = input[1];
        Statement loginStatement = conn.createStatement();
        // System.out.println(conn.);
        ResultSet rs = loginStatement.executeQuery("SELECT password FROM account WHERE username=\'" + username + "\' AND password=\'" + password + "\';");
        if (rs.next())
        {
            Statement updateTimeStatement = conn.createStatement();
            //updateTimeStatement.execute("UPDATE account SET lastaccessdate=current_date WHERE username='" + username + "';");
            return Context.loggedIn;
        }
        else
        {
            System.out.println("Account not found. Creating a new account.");
            return Context.createUser;
        }
    }

    private static String userLoop( Connection conn) throws SQLException {
        Scanner in = new Scanner(System.in);
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
