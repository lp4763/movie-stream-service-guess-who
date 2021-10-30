import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Scanner;
import java.util.function.Function;

public class ConsoleApp {
    private Connection conn;
    private enum Context { exit, login, createUser, loggedIn,
        collection, createCollection, listCollections, deleteCollection, renameCollection, editCollection, openCollection}

    // TODO update this to use the account object, rather than hard coding the username.
    private String username;

    public ConsoleApp(Connection nConn)
    {
        conn = nConn;
    }

    public void mainLoop() throws SQLException
    {
        try {
            System.out.println("You have been connected to the server.");
            Scanner in = new Scanner(System.in);
            Context current = Context.login;
            while (current != Context.exit) {
                switch (current) {
                    case login:
                        System.out.println("Please log in using by entering your username and password:");
                        break;
                    case createUser:
                        System.out.println("Please create a new user by entering your username, password, firstname, lastname, and email.");
                        break;
                    case loggedIn:
                        System.out.println("You are logged in. Please enter a command, such as search, collection, or follow. Enter help for more options.");
                        break;
                    case collection:
                        System.out.println("Please input a collection specific command such as create, list, edit, delete, rename, open, or play.");
                        break;
                    case createCollection:
                        System.out.println("Please input the name of the collection you want to create.");
                        break;
                    case editCollection:
                        System.out.println("Please input the name of the collection you want to edit.");
                        break;
                    case deleteCollection:
                        System.out.println("Please input the name of the collection you want to delete.");
                        break;
                    case renameCollection:
                        System.out.println("Please input the name of the collection you want to rename, followed by the new name.");
                        break;
                    case openCollection:
                        System.out.println("Please input the name of the collection you want to open and see the movies in.");
                        break;
                }
                String command = in.nextLine();
                if (command.toLowerCase().equals("quit") || command.toLowerCase().equals("exit")) {
                    current = Context.exit;
                } else {
                    if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
                    {
                        System.out.println("At any time, you may enter quit or exit to exit the application; cancel, stop, or back to return to a prior operation, or help or ? to get information about what to enter in a given context.");
                    }
                    switch (current) {
                        case login:
                            current = tryLogin(conn, command);
                            break;
                        case createUser:
                            current = tryCreateUser(conn, command);
                            break;
                        case loggedIn:
                            current = tryCommand(conn, command);
                            break;
                        case collection:
                            current = tryCollection(conn, command);
                            break;
                        case createCollection:
                            current = tryCreateCollection(conn, command);
                            break;
                        case listCollections:
                            current = tryListCollections(conn);
                            break;
                        case editCollection:
                            current = tryEditCollection(conn, command);
                            break;
                        case deleteCollection:
                            current = tryDeleteCollection(conn, command);
                            break;
                        case renameCollection:
                            current = tryRenameCollection(conn, command);
                            break;
                        case openCollection:
                            current = tryOpenCollection(conn, command);
                            break;
                    }
                }

                //String username = userLoop(conn);
                //System.out.println("Successfully logged in as " + username);
            }
        }
        catch (SQLException e)
        {
            throw e;
        }
    }

    private Context tryOpenCollection(Connection conn, String command) throws SQLException {
        // TODO Make sure this is sorting in accordance with requirements
        // TODO test this once we have actual movies
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop"))
        {
            return Context.collection;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("Please input your collection name as:\n<collectionName>");
            return Context.openCollection;
        }
        String[] input = command.split(" ");
        if (input.length == 1)
        {
            if (input[0].length() < 1 || input[0].length() > 63)
            {
                System.out.println("Your input does not match format requirements. Please make sure your collection name is between 1 and 63 characters in length.");
                return Context.openCollection;
            } else
            {
                // TODO update this to use the moviecollection object
                Statement foundStatement = conn.createStatement();
                ResultSet rs = foundStatement.executeQuery("SELECT username FROM collection WHERE username=\'" + username + "\' AND name=\'" + input[0] + "\';");
                if (rs.next())
                {

                    // TODO Fix this to instead use movieCollection objects, rather than hard coded SQL
                    Statement listStatement = conn.createStatement();
                    rs = listStatement.executeQuery("SELECT * FROM contains WHERE username=\'" + username + "\' AND collectionname=\'" + input[0]+ "\';");
                    System.out.println("Collection " + input[0] + " contains:");
                    while (rs.next())
                    {
                        System.out.println(rs.getString("moviename"));
                    }
                    return Context.collection;
                } else
                {
                    System.out.println("Unable to find a collection to delete with the given name.");
                    return Context.openCollection;
                }
            }
        } else
        {
            System.out.println("Please input your collection name as:\n<collectionName>");
            return Context.openCollection;
        }
    }

    private Context tryEditCollection(Connection conn, String command) throws SQLException {
        // TODO test this once we have actual movies to add/remove

        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop"))
        {
            return Context.collection;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("Please input your collection name, add or remove, and the movie name as:\n<collectionName> [add/remove] <movie name>");
            return Context.renameCollection;
        }
        String[] input = command.split(" ");
        if (input.length == 3)
        {
            if (input[0].length() < 1 || input[0].length() > 63)
            {
                System.out.println("Your input does not match format requirements. Please make sure your collection name is between 1 and 63 characters in length.");
                return Context.editCollection;
            } else if (!(input[1].toLowerCase().equals("add") || input[1].toLowerCase().equals("remove")))
            {
                System.out.println("Your input does not match format requirements. Please make sure you input whether you want to add or remove the movie.");
                return Context.editCollection;
            } else if (input[2].length() < 1 || input[1].length() > 63)
            {
                System.out.println("Your input does not match format requirements. Please make sure your movie name is between 1 and 63 characters in length.");
                return Context.editCollection;
            } else
            {
                // TODO update this to use the moviecollection object
                Statement foundCStatement = conn.createStatement();
                ResultSet rs = foundCStatement.executeQuery("SELECT username FROM collection WHERE username=\'" + username + "\' AND name=\'" + input[0] + "\';");
                if (rs.next())
                {
                    // TODO update this to use the movie object
                    Statement foundMStatement = conn.createStatement();
                    rs = foundMStatement.executeQuery("SELECT name FROM movie WHERE name=\'" + input[2] + "\';");
                    if (rs.next()) {
                        Statement foundRStatement = conn.createStatement();
                        rs = foundRStatement.executeQuery("SELECT name FROM contains WHERE username=\'" + username + "\' AND collectionname=\'" + input[0]+ "\' AND moviename=\'" + input[2] + "\';");
                        if (input[1].toLowerCase().equals("add"))
                        {
                            if (rs.next())
                            {
                                System.out.println("The collection already contains that movie");
                                return Context.editCollection;
                            } else
                            {
                                // TODO update this to use objects, rather than a hard coded insert
                                Statement addContainsStatement = conn.createStatement();
                                String values = formatForInsert(new String[] {input[0], username, input[2]});
                                addContainsStatement.execute("INSERT INTO contains VALUES " + values);
                                return Context.collection;
                            }

                        } else
                        {
                            if (rs.next())
                            {
                                // TODO update this to use objects, rather than a hard coded delete
                                Statement deleteContainsStatement = conn.createStatement();
                                deleteContainsStatement.execute("DELETE FROM contains WHERE username=\'" + username + "\' AND collectionname=\'" + input[0]+ "\' AND moviename=\'" + input[2] + "\';");
                                return Context.collection;
                            } else
                            {
                                System.out.println("The collection does not contain that movie");
                                return Context.editCollection;
                            }
                        }
                    } else
                    {
                        System.out.println("Unable to find a movie with the given name.");
                        return Context.editCollection;
                    }
                } else
                {
                    System.out.println("Unable to find a collection with the given name.");
                    return Context.editCollection;
                }
            }
        } else
        {
            System.out.println("Please input your collection name, add or remove, and the movie name as:\n<collectionName> [add/remove] <movie name>");
            return Context.editCollection;
        }

    }

    private Context tryRenameCollection(Connection conn, String command) throws SQLException {
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop"))
        {
            return Context.collection;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("Please input your collection name and new name as:\n<collectionName> <new name>");
            return Context.renameCollection;
        }
        String[] input = command.split(" ");
        if (input.length == 2)
        {
            if (input[0].length() < 1 || input[0].length() > 63)
            {
                System.out.println("Your input does not match format requirements. Please make sure your collection name is between 1 and 63 characters in length.");
                return Context.deleteCollection;
            } else if (input[1].length() < 1 || input[1].length() > 63)
            {
                System.out.println("Your input does not match format requirements. Please make sure your new collection name is between 1 and 63 characters in length.");
                return Context.deleteCollection;
            } else
            {
                // TODO update this to use the moviecollection object
                Statement foundStatement = conn.createStatement();
                ResultSet rs = foundStatement.executeQuery("SELECT username FROM collection WHERE username=\'" + username + "\' AND name=\'" + input[0] + "\';");
                if (rs.next())
                {
                    // TODO update this to use the moviecollection object, rather than a hard coded insert
                    Statement deleteCollectionStatement = conn.createStatement();
                    deleteCollectionStatement.execute("UPDATE collection SET name=\'"+ input[1] + "\' WHERE username=\'" + username + "\' AND name=\'" + input[0] + "\';");
                    return Context.collection;
                } else
                {
                    System.out.println("Unable to find a collection to rename with the given name.");
                    return Context.renameCollection;
                }
            }
        } else
        {
            System.out.println("Please input your collection name and new name as:\n<collectionName> <new name>");
            return Context.renameCollection;
        }
    }

    private Context tryDeleteCollection(Connection conn, String command) throws SQLException {
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop"))
        {
            return Context.collection;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("Please input your collection name as:\n<collectionName>");
            return Context.deleteCollection;
        }
        String[] input = command.split(" ");
        if (input.length == 1)
        {
            if (input[0].length() < 1 || input[0].length() > 63)
            {
                System.out.println("Your input does not match format requirements. Please make sure your collection name is between 1 and 63 characters in length.");
                return Context.deleteCollection;
            } else
            {
                // TODO update this to use the moviecollection object
                Statement foundStatement = conn.createStatement();
                ResultSet rs = foundStatement.executeQuery("SELECT username FROM collection WHERE username=\'" + username + "\' AND name=\'" + input[0] + "\';");
                if (rs.next())
                {
                    // TODO update this to use the moviecollection object, rather than a hard coded insert
                    Statement deleteCollectionStatement = conn.createStatement();
                    deleteCollectionStatement.execute("DELETE FROM collection WHERE username=\'" + username + "\' AND name=\'" + input[0] + "\';");
                    return Context.collection;
                } else
                {
                    System.out.println("Unable to find a collection to delete with the given name.");
                    return Context.deleteCollection;
                }
            }
        } else
        {
            System.out.println("Please input your collection name as:\n<collectionName>");
            return Context.deleteCollection;
        }
    }

    private Context tryListCollections(Connection conn) throws SQLException {
        // TODO Make sure this is sorting in accordance with requirements

        // TODO Fix this to instead use movieCollection objects, rather than hard coded SQL
        Statement listStatement = conn.createStatement();
        ResultSet rs = listStatement.executeQuery("SELECT * FROM collection WHERE username=\'" + username + "\';");
        while (rs.next())
        {
            System.out.println("Collection " + rs.getString("name") + " contains " + rs.getInt("movieNumber") + " movies, with a total length of " + rs.getInt("totalLength") + " minutes");
        }
        return Context.collection;
    }

    private Context tryCreateCollection(Connection conn, String command) throws SQLException {
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop"))
        {
            return Context.collection;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("Please input your collection name as:\n<collectionName>");
            return Context.createCollection;
        }
        String[] input = command.split(" ");
        if (input.length == 1)
        {
            if (input[0].length() < 1 || input[0].length() > 63)
            {
                System.out.println("Your input does not match format requirements. Please make sure your collection name is between 1 and 63 characters in length.");
                return Context.createCollection;
            } else
            {
                // TODO update this to use the moviecollection object
                Statement foundStatement = conn.createStatement();
                ResultSet rs = foundStatement.executeQuery("SELECT username FROM collection WHERE username=\'" + username + "\' AND name=\'" + input[0] + "\';");
                if (rs.next())
                {
                    System.out.println("You may only have one collection with a given name. You may want to edit instead, or create a collection with a different name.");
                    return Context.createCollection;
                }

                // TODO update this to use the moviecollection object, rather than a hard coded insert
                Statement createCollectionStatement = conn.createStatement();
                String values = formatForInsert(new String[] {input[0], username, "0", "0"});
                createCollectionStatement.execute("INSERT INTO collection VALUES " + values);

                return Context.collection;
            }
        } else
        {
            System.out.println("Please input your collection name as:\n<collectionName>");
            return Context.createCollection;
        }
    }

    private Context tryCollection(Connection conn, String command) throws SQLException {
        // TODO add play code, once the framework for playing a specific movie exists
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop"))
        {
            return Context.loggedIn;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("Please input a collection specific command such as create, list, edit, delete, rename, open, or play.");
            return Context.collection;
        }
        String[] input = command.split(" ");
        if (input.length == 1)
        {
            if (input[0].toLowerCase().equals("create"))
            {
                return Context.createCollection;
            } else if (input[0].toLowerCase().equals("list"))
            {
                tryListCollections(conn);
                return Context.collection;
            } else if (input[0].toLowerCase().equals("edit"))
            {
                return Context.collection;
            } else if (input[0].toLowerCase().equals("delete"))
            {
                return Context.deleteCollection;
            } else if (input[0].toLowerCase().equals("rename"))
            {
                return Context.renameCollection;
            } else if (input[0].toLowerCase().equals("open"))
            {
                return Context.openCollection;
            }
        } else if (input.length > 1)
        {
            String recreate = "";
            for (int i = 0; i < input.length - 1; i++)
            {
                recreate += input[i+1];
                if (i < input.length - 1)
                {
                    recreate += " ";
                }
            }
            System.out.println(recreate);
            if (input[0].toLowerCase().equals("create"))
            {
                return tryCreateCollection(conn, recreate);
            } else if (input[0].toLowerCase().equals("edit"))
            {
                return tryEditCollection(conn, recreate);
            } else if (input[0].toLowerCase().equals("delete"))
            {
                return tryDeleteCollection(conn, recreate);
            } else if (input[0].toLowerCase().equals("rename"))
            {
                return tryRenameCollection(conn, recreate);
            } else if (input[0].toLowerCase().equals("open"))
            {
                return tryOpenCollection(conn, recreate);
            }
            return Context.collection;
        }
        return Context.collection;
    }

    private Context tryCommand(Connection conn, String command) throws SQLException {
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop") || command.toLowerCase().equals("back") || command.toLowerCase().equals("logout") || command.toLowerCase().equals("log out"))
        {
            System.out.println("Logging out.");
            return Context.login;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("You can type collection to access collection commands, search to access search commands, follow to access follow commands, or play to access play commands.");
            return Context.loggedIn;
        }
        String[] input = command.split(" ");
        if (input.length == 1)
        {
            if (input[0].toLowerCase().equals("collection"))
            {
                return Context.collection;
            }

        } else if (input.length > 1)
        {
            String recreate = "";
            for (int i = 0; i < input.length - 1; i++)
            {
                recreate += input[i+1];
                if (i < input.length - 1)
                {
                    recreate += " ";
                }
            }
            if (input[0].toLowerCase().equals("collection"))
            {
                return tryCollection(conn, recreate);
            }
        }

        System.out.println("Your input does not match any commands. Please input your command as:\n<command>, from 'collection', 'search', 'follow', 'play'");
        return Context.loggedIn;
    }

    private Context tryCreateUser(Connection conn, String command) throws SQLException {
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop") || command.toLowerCase().equals("back"))
        {
            return Context.login;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("Please input your new user info as:\n<username> <password> <firstname> <lastname> <email>");
            return Context.createUser;
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

        // TODO update this to use the account object
        Statement foundStatement = conn.createStatement();
        ResultSet rs = foundStatement.executeQuery("SELECT password FROM account WHERE username=\'" + username + "\';");
        if (rs.next()) {
            System.out.println("An account with this username already exists. Please select a different username.");
            return Context.createUser;
        }


        java.sql.Timestamp sqlDate = new java.sql.Timestamp(Instant.now().toEpochMilli());

        // TODO update this to use the account object, rather than hard coding the username.
        username = input[0];

        // TODO update this to use the account object, rather than a hard coded insert
        Statement createAccountStatement = conn.createStatement();
        String values = formatForInsert(new String[] {input[0], input[1], input[2], input[3], sqlDate.toString(), input[4]});
        System.out.println(values);
        createAccountStatement.execute("INSERT INTO account VALUES " + values);
        // TODO re-add the code to update access dates on login, using the access date table
        return Context.loggedIn;
    }

    private Context tryLogin(Connection conn, String command) throws SQLException
    {
        if (command.toLowerCase().equals("create") || command.toLowerCase().equals("new"))
        {
            return Context.createUser;
        }
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop") || command.toLowerCase().equals("back"))
        {
            System.out.println("Unable to cancel. Logging in is necessary to use additional features.");
            return Context.login;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("Please input your username and password as:\n<username> <password>");
            return Context.login;
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

        // TODO update this to use the account object, rather than a hard coded select
        String usernameIn = input[0];
        String password = input[1];
        Statement loginStatement = conn.createStatement();
        ResultSet rs = loginStatement.executeQuery("SELECT password FROM account WHERE username=\'" + usernameIn + "\' AND password=\'" + password + "\';");
        if (rs.next())
        {
            // TODO update this to use the account object, rather than hard coding the username.
            this.username = input[0];
            Statement updateTimeStatement = conn.createStatement();
            //updateTimeStatement.execute("UPDATE account SET lastaccessdate=current_date WHERE username='" + username + "';");
            // TODO re-add the code to update access dates on login, using the access date table
            return Context.loggedIn;
        }
        else
        {
            // TODO update this to use the account object
            Statement foundStatement = conn.createStatement();
            rs = foundStatement.executeQuery("SELECT password FROM account WHERE username=\'" + usernameIn + "\';");
            if (rs.next())
            {
                System.out.println("Unable to log in. Please double check your username and password.");
                return Context.login;
            } else {
                System.out.println("Account matching username not found. Creating a new account.");
                return Context.createUser;
            }
        }
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
