import javax.swing.plaf.nimbus.State;
import javax.xml.transform.Result;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;

public class ConsoleApp {

    private enum Context { exit, login, createUser, loggedIn,
        collection, createCollection, listCollections, deleteCollection,
        renameCollection, editCollection, openCollection, search, follow, play, recommend, user}

    private String username;
    private Connection conn;
    public ConsoleApp(Connection conn)
    {
        this.conn = conn;
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
                        System.out.println("Please create a new user by entering your" +
                                " username, password, firstname, lastname, and email.");
                        break;
                    case loggedIn:
                        System.out.println("You are logged in. Please enter a command," +
                                " such as search, collection, follow, play, user, or recommend. " +
                                "Enter help for more information.");
                        break;
                    case collection:
                        System.out.println("Please input a collection specific command " +
                                "such as create, list, edit, delete, rename, or open.");
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
                        System.out.println("Please input the name of the " +
                                "collection you want to rename, followed by the new name.");
                        break;
                    case openCollection:
                        System.out.println("Please input the name of the collection" +
                                " you want to open and see the movies in.");
                        break;
                    case search:
                        System.out.println("Please input the terms of your search," +
                                " or nothing to see all movies.");
                        break;
                    case follow:
                        System.out.println("Please input the name of the user" +
                                " you want to follow/unfollow.");
                        break;
                    case play:
                        System.out.println("Please input the name of the movie or" +
                                " collection you wish to play, optionally followed by" +
                                " the rating you wish to give it.");
                        break;
                    case recommend:
                        System.out.println("Please input the type of recommendation you wish to receive.");
                        break;
                    case user:
                        System.out.println("Please input the name of the user you want to find information about.");
                        break;

                }
                String command = in.nextLine();
                if (command.toLowerCase().equals("quit") || command.toLowerCase().equals("exit")) {
                    current = Context.exit;
                } else {
                    if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
                    {
                        System.out.println("At any time, you may enter quit or " +
                                "exit to exit the application; cancel, stop, or back to return " +
                                "to a prior operation, or help or ? to get information " +
                                "about what to enter in a given context.");
                    }
                    switch (current) {
                        case login:
                            current = tryLogin(command);
                            break;
                        case createUser:
                            current = tryCreateUser(command);
                            break;
                        case loggedIn:
                            current = tryCommand(command);
                            break;
                        case collection:
                            current = tryCollection(command);
                            break;
                        case createCollection:
                            current = tryCreateCollection(command);
                            break;
                        case listCollections:
                            current = tryListCollections();
                            break;
                        case editCollection:
                            current = tryEditCollection(command);
                            break;
                        case deleteCollection:
                            current = tryDeleteCollection(command);
                            break;
                        case renameCollection:
                            current = tryRenameCollection(command);
                            break;
                        case openCollection:
                            current = tryOpenCollection(command);
                            break;
                        case search:
                            current = trySearch(command);
                            break;
                        case follow:
                            current = tryFollow(command);
                            break;
                        case play:
                            current = tryPlay(command);
                            break;
                        case recommend:
                            current = tryRecommend(command);
                            break;
                        case user:
                            current = tryUser(command);
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

    private Context tryUser(String command) throws SQLException
    {
        String[] input = command.split(" ");
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop")
                || command.toLowerCase().equals("back"))
        {
            return Context.loggedIn;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("Please input the username or email of the user " +
                    "you want to find information about as\n<username>");
            return Context.user;
        }
        if(input.length != 1)
        {
            System.out.println("Your input does not match format requirements. " +
                    "Please give the name you want to find information about as:\n<username>");
            return Context.user;
        }
        if(input[0].length() > 63){
            System.out.println("Please make sure the username you are typing in is " +
                    "between 1 and 63 characters in length.");
            return Context.user;
        }
        Statement findStmt = conn.createStatement();

        String userToFind = "";

        ResultSet rs21 = findStmt.executeQuery("SELECT username FROM account" +
                " WHERE email = \'"+input[0]+"\'");

        if(rs21.next()){
            userToFind = rs21.getString(1);
        }
        else
        {
            userToFind = input[0];
        }
        ResultSet rs0 = findStmt.executeQuery("SELECT username FROM account" +
                " WHERE username=\'" + userToFind + "\'");
        if (rs0.next()) {
            System.out.print("User " + userToFind);

            ResultSet rs1 = findStmt.executeQuery("SELECT COUNT(name) FROM collection\n" +
                    "WHERE username=\'"+userToFind+"\';");
            if (rs1.next()) {
                System.out.print(" has " + rs1.getInt("count") + " collections, ");
            }

            rs1 = findStmt.executeQuery("SELECT COUNT(followeduser) FROM follows\n" +
                    "WHERE followeduser=\'"+userToFind+"\';");
            if (rs1.next()) {
                System.out.print("has " + rs1.getInt("count") + " followers, ");
            }

            rs1 = findStmt.executeQuery("SELECT COUNT(followeduser) FROM follows\n" +
                    "WHERE followinguser=\'"+userToFind+"\';");
            if (rs1.next()) {
                System.out.println("follows " + rs1.getInt("count") + " other users, and their 10 most " +
                        "played movies are:");
            }
            rs1 = findStmt.executeQuery("SELECT m.name from movie as m, watches as w\n" +
                    "WHERE w.moviename = m.name AND w.username=\'"+userToFind+"\'\n" +
                    "ORDER BY w.\"watchCount\" DESC\n" +
                    "LIMIT 10;");
            while (rs1.next()) {
                System.out.println(rs1.getString("name"));
            }
            return Context.loggedIn;
        } else
        {
            System.out.println("Unable to find user " + userToFind + ".");
            return Context.user;
        }

    }

    private Context tryRecommend(String command) throws SQLException {
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop")
                || command.toLowerCase().equals("back"))
        {
            return Context.loggedIn;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("Please input the type of recommendation you wish to receive, " +
                    "either 'top', 'friends', 'new', or 'personal', as" +
                    "\n<type>");
            return Context.recommend;
        }
        String[] input = command.split(" ");
        if (input.length == 1)
        {
            if (input[0].length() < 1 || input[0].length() > 63)
            {
                System.out.println("Your input does not match format requirements." +
                        " Please make sure your collection name is" +
                        " between 1 and 63 characters in length.");
                return Context.recommend;
            } else
            {
                if (input[0].toLowerCase().equals("top"))
                {
                    Statement listStatement = conn.createStatement();
                    ResultSet rs = listStatement.executeQuery("SELECT name FROM movie\n" +
                            "WHERE current_date-releasedate <= 90\n" +
                            "ORDER BY userratingavgscore DESC\n" +
                            "LIMIT 20");
                    System.out.println("The top 20 most popular movies released in the past 90 days are:");
                    while (rs.next())
                    {
                        System.out.println(rs.getString("name"));
                    }
                    return Context.loggedIn;
                } else if (input[0].toLowerCase().equals("friends"))
                {
                    Statement listStatement = conn.createStatement();
                    ResultSet rs = listStatement.executeQuery("SELECT moviename FROM watches as w, follows as f\n" +
                            "WHERE f.followinguser=\'" + username + "\' AND w.username=f.followeduser\n" +
                            "GROUP BY moviename\n" +
                            "ORDER BY AVG(rating) DESC\n" +
                            "LIMIT 20");
                    System.out.println("The top 20 most popular movies as rated by the people you follow are:");
                    boolean flag = false;
                    while (rs.next())
                    {
                        flag = true;
                        System.out.println(rs.getString("moviename"));
                    }
                    if (!flag)
                    {
                        System.out.println("We couldn't find any movies rated by people you follow. " +
                                "You can try following more people, or encourage the people you follow to watch " +
                                "and rate more movies.");
                    }
                    return Context.loggedIn;
                } else if (input[0].toLowerCase().equals("new"))
                {
                    Statement listStatement = conn.createStatement();
                    ResultSet rs = listStatement.executeQuery("SELECT name FROM movie\n" +
                            "WHERE EXTRACT(month FROM releasedate)=EXTRACT(MONTH FROM current_date)\n" +
                            " AND EXTRACT(year FROM releasedate)=EXTRACT(YEAR FROM current_date)\n" +
                            "ORDER BY userratingavgscore DESC\n" +
                            "LIMIT 5");
                    System.out.println("The top 5 most popular movies released in the current month are:");
                    boolean flag = false;
                    while (rs.next())
                    {
                        flag = true;
                        System.out.println(rs.getString("name"));
                    }
                    if (!flag)
                    {
                        System.out.println("We couldn't find any movies released this month.");
                    }
                    return Context.loggedIn;
                } else if (input[0].toLowerCase().equals("personal"))
                {
                    Statement listStatement = conn.createStatement();
                    ResultSet rs = listStatement.executeQuery("SELECT DISTINCT rec.moviename FROM (\n" +
                            "SELECT DISTINCT other.moviename, m.userratingavgscore FROM watches as self, " +
                            "watches as other, movie as m\n" +
                            "WHERE m.name=other.moviename --We only want information about the movie to recommend\n" +
                            "AND self.username=\'"+username+"\'\n" +
                            "AND self.username!=other.username --Don't try to recommend to self\n" +
                            "AND self.moviename IN (SELECT w0.moviename FROM watches as w0\n" +
                            "WHERE w0.username=self.username ORDER BY w0.rating DESC LIMIT 5)\n" +
                            "AND other.moviename NOT IN (SELECT w0.moviename FROM watches as w0\n" +
                            "WHERE w0.username=self.username) -- Don't recommend what I've already seen.\n" +
                            "AND other.moviename IN (SELECT w0.moviename FROM watches as w0\n" +
                            "WHERE w0.username=other.username ORDER BY w0.rating DESC LIMIT 5)\n" +
                            "AND self.moviename IN (SELECT w0.moviename FROM watches as w0\n" +
                            "WHERE w0.username=other.username ORDER BY w0.rating DESC)\n" +
                            "ORDER BY m.userratingavgscore DESC) as rec\n" +
                            "LIMIT 10\n");
                    System.out.println("The top 10 most popular movies which people who share your taste in movies " +
                            "rate highly are:");
                    boolean flag = false;
                    while (rs.next())
                    {
                        flag = true;
                        System.out.println(rs.getString("moviename"));
                    }
                    if (!flag)
                    {
                        System.out.println("We couldn't find any movies you haven't watched which people who " +
                                "share your taste in movies rate highly.");
                    }
                    return Context.loggedIn;
                } else
                {
                    System.out.println("Unable to find that recommendation type.");
                    return tryRecommend("help");
                }
            }
        } else
        {
            System.out.println("Please input your collection name as:\n<collectionName>");
            return Context.openCollection;
        }
    }

    private Context trySearch(String command) throws SQLException {
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop")
                || command.toLowerCase().equals("back"))
        {
            return Context.loggedIn;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("Please input any number of search or sort terms. " +
                    "Terms must be either [name/genre/release/studio/director/actor] <desired_value>" +
                    " or sort [name/studio/genre/release] [ascending/descending]." +
                    "Later sorts take precedence over earlier ones." +
                    "Names with spaces in them should have the spaces input as '_'.");
            return Context.search;
        }
        String[] input = command.split(" ");
        String whereClause = " WHERE ";
        boolean wherePresent = false;
        String orderClause = " ORDER BY ";
        String sortClause = "name ASC, releasedate ASC";
        for (int i = 0; i < input.length; i++)
        {
            if (input[i].equals("") || input[i].equals(" ") || input[i].equals("\n"))
            {
                continue;
            } else if (input.length - i <= 1)
            {
                System.out.println("Dangling search term " + input[i] +
                        ". Terms must be either [name/genre/release/studio/director/actor] <desired_value>" +
                        " or sort [name/studio/genre/release] [ascending/descending]. " +
                        "Names with spaces in them should have the spaces input as '_'.");
                return Context.search;
            } else if (input[i].toLowerCase().equals("name"))
            {
                if (wherePresent)
                {
                    whereClause += " AND ";
                } else
                {
                    wherePresent = true;
                }
                i++;
                String desiredName = underToSpace(input[i]);
                whereClause += "name LIKE \'%" + desiredName + "%\'";
            } else if (input[i].toLowerCase().equals("genre"))
            {
                if (wherePresent)
                {
                    whereClause += " AND ";
                } else
                {
                    wherePresent = true;
                }
                i++;
                String desiredGenre = input[i];
                whereClause += "genre LIKE \'%" + desiredGenre + "%\'";
            } else if (input[i].toLowerCase().equals("release"))
            {
                if (wherePresent)
                {
                    whereClause += " AND ";
                } else
                {
                    wherePresent = true;
                }
                i++;
                String desiredDate = input[i];
                try {
                    java.sql.Date date = Date.valueOf(desiredDate);
                    whereClause += "releasedate=\'" + date + "\'";
                } catch (IllegalArgumentException e)
                {
                    System.out.println(desiredDate + " is not a valid date. " +
                            "Please format release date terms as:\nrelease <yyyy>-<mm>-<dd>");
                    return Context.search;
                }
            } else if (input[i].toLowerCase().equals("studio"))
            {
                if (wherePresent)
                {
                    whereClause += " AND ";
                } else
                {
                    wherePresent = true;
                }
                i++;
                String desiredStudio = underToSpace(input[i]);
                whereClause += "studio LIKE \'%" + desiredStudio + "%\'";
            } else if (input[i].toLowerCase().equals("director"))
            {
                if (wherePresent)
                {
                    whereClause += " AND ";
                } else
                {
                    wherePresent = true;
                }
                i++;
                String desiredDirector = underToSpace(input[i]);
                Statement directorStatement = conn.createStatement();
                ResultSet ds = directorStatement.executeQuery("SELECT * FROM person" +
                        " WHERE name=\'" + desiredDirector + "\';");
                if (ds.next())
                {
                    int directorID = ds.getInt("personid");
                    whereClause += "directorid=" + directorID;
                } else
                {
                    System.out.println("Unable to find director " + desiredDirector + ".");
                    return Context.search;
                }
            } else if (input[i].toLowerCase().equals("actor"))
            {
                if (wherePresent)
                {
                    whereClause += " AND ";
                } else
                {
                    wherePresent = true;
                }
                i++;
                String desiredActor = underToSpace(input[i]);
                Statement actorStatement = conn.createStatement();
                ResultSet as = actorStatement.executeQuery("SELECT * FROM person" +
                        " WHERE name=\'" + desiredActor + "\';");
                if (as.next())
                {
                    int personID = as.getInt("personid");
                    whereClause += "name IN (SELECT moviename FROM actsin WHERE personid=" + personID + ")";
                }
                else {
                    System.out.println("Unable to find actor " + desiredActor + ".");
                    return Context.search;
                }
            } else if (input[i].toLowerCase().equals("sort"))
            {
                if (input.length - i <= 2)
                {
                    System.out.println("Incorrect number of terms for sort. Sort terms must be" +
                            " structured as:\nsort <type> [ascending/descending]");
                    return Context.search;
                }
                i++;
                String type = input[i].toLowerCase();
                i++;
                String ad = input[i].toLowerCase();
                String term = "";

                if (type.equals("name"))
                {
                    term += "name ";
                } else if (type.equals("studio"))
                {
                    term += "studio ";
                } else if (type.equals("genre"))
                {
                    term += "genre ";
                } else if (type.equals("release"))
                {
                    term += "releasedate ";
                } else
                {
                    System.out.println("Unknown sort term " + term + ".");
                    return Context.search;
                }

                if (ad.equals("ascending"))
                {
                    term += "ASC";
                } else if (ad.equals("descending"))
                {
                    term += "DESC";
                } else
                {
                    System.out.println("Please input the sort direction as" +
                            " either ascending or descending, not " + ad + ".");
                    return Context.search;
                }

                term += ", ";
                sortClause = term + sortClause;
            }
            else
            {
                System.out.println("Unknown search term " + input[i] + ".");
                return Context.search;
            }
        }

        Statement listStatement = conn.createStatement();
        String select = "SELECT * FROM movie";
        if (wherePresent)
        {
            select += whereClause;
        }
        select += orderClause + sortClause;
        System.out.println(select);
        ResultSet rs = listStatement.executeQuery(select);
        System.out.println("The movies matching your search are:");
        while (rs.next())
        {
            String nameIn = rs.getString("name");
            String name = "";
            for (int i = 0; i < nameIn.length(); i++)
            {
                name += nameIn.charAt(i);
                if (nameIn.charAt(i) == '\'')
                {
                    name += nameIn.charAt(i);
                }
            }
            System.out.print(nameIn);

            System.out.print(" is directed by ");
            int directorID = rs.getInt("directorid");
            Statement directorStatement = conn.createStatement();
            ResultSet ds = directorStatement.executeQuery("SELECT * FROM person" +
                    " WHERE personid=" + directorID);
            ds.next();
            System.out.print(ds.getString("name"));

            System.out.print(", is acted by ");
            Statement actIdsStatement = conn.createStatement();
            ResultSet ais = actIdsStatement.executeQuery("SELECT * FROM actsin" +
                    " WHERE moviename=\'" + name + "\';");
            boolean isActed = false;
            while (ais.next())
            {
                isActed = true;
                Statement actorStatment = conn.createStatement();
                ResultSet as = actorStatment.executeQuery("SELECT * FROM person" +
                        " WHERE personid=" + ais.getInt("personid"));
                as.next();
                System.out.print(as.getString("name") + ", ");
            }
            if (!isActed)
            {
                System.out.print("no known actors, ");
            }

            System.out.print("is " + rs.getInt("length") + " minutes long,");

            System.out.print(" is rated " + rs.getString("mpaarating") + ", and");

            System.out.print(" has an average user score of "
                    + rs.getFloat("userratingavgscore") + " stars.");

            System.out.println();
        }
        return Context.search;
    }

    private Context tryOpenCollection(String command) throws SQLException {
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop")
                || command.toLowerCase().equals("back"))
        {
            return Context.collection;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("Please input your collection name" +
                    " as:\n<collectionName>.");
            return Context.openCollection;
        }
        String[] input = command.split(" ");
        if (input.length == 1)
        {
            if (input[0].length() < 1 || input[0].length() > 63)
            {
                System.out.println("Your input does not match format requirements." +
                        " Please make sure your collection name is" +
                        " between 1 and 63 characters in length.");
                return Context.openCollection;
            } else
            {
                Statement foundStatement = conn.createStatement();
                ResultSet rs = foundStatement.executeQuery("SELECT username FROM collection" +
                        " WHERE username=\'" + username + "\' AND name=\'" + input[0] + "\';");
                if (rs.next())
                {
                    Statement listStatement = conn.createStatement();
                    rs = listStatement.executeQuery("SELECT * FROM contains WHERE username=\'"
                            + username + "\' AND collectionname=\'" + input[0]+ "\';");
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

    private Context tryEditCollection(String command) throws SQLException {
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop")
                || command.toLowerCase().equals("back"))
        {
            return Context.collection;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("Please input your collection name, add or remove, " +
                    "and the movie name as:\n<collectionName> [add/remove] <movie name>");
            return Context.renameCollection;
        }
        String[] input = command.split(" ");
        if (input.length == 3)
        {
            if (input[0].length() < 1 || input[0].length() > 63)
            {
                System.out.println("Your input does not match format requirements." +
                        " Please make sure your collection name is between" +
                        " 1 and 63 characters in length.");
                return Context.editCollection;
            } else if (!(input[1].toLowerCase().equals("add") || input[1].toLowerCase().equals("remove")))
            {
                System.out.println("Your input does not match format requirements. " +
                        "Please make sure you input whether you want to add or remove the movie.");
                return Context.editCollection;
            } else if (input[2].length() < 1 || input[1].length() > 63)
            {
                System.out.println("Your input does not match format requirements." +
                        " Please make sure your movie name is between 1 and 63 characters in length.");
                return Context.editCollection;
            } else
            {
                String moviename = underToSpace(input[2]);
                System.out.println(moviename);
                Statement foundCStatement = conn.createStatement();
                ResultSet cs = foundCStatement.executeQuery("SELECT username FROM" +
                        " collection WHERE username=\'"
                        + username + "\' AND name=\'" + input[0] + "\';");
                if (cs.next())
                {
                    Statement foundMStatement = conn.createStatement();
                    ResultSet ms = foundMStatement.executeQuery("SELECT * FROM movie " +
                            "WHERE name=\'" + moviename + "\';");
                    if (ms.next()) {
                        Statement foundRStatement = conn.createStatement();
                        ResultSet rs = foundRStatement.executeQuery("SELECT * FROM contains" +
                                " WHERE username=\'" + username +
                                "\' AND collectionname=\'" + input[0]+
                                "\' AND moviename=\'" + moviename + "\';");
                        if (input[1].toLowerCase().equals("add"))
                        {
                            if (rs.next())
                            {
                                System.out.println("The collection already contains that movie");
                                return Context.editCollection;
                            } else
                            {
                                int length = ms.getInt("length");
                                foundCStatement.execute("UPDATE collection " +
                                        "SET totallength=totallength+" + length +
                                        " WHERE username=\'" + username + "\' AND name=\'"
                                        + input[0]+ "\';");
                                foundCStatement.execute("UPDATE collection SET " +
                                        "movienumber=movienumber+1 WHERE username=\'"
                                        + username + "\' AND name=\'" + input[0]+ "\';");

                                Statement addContainsStatement = conn.createStatement();
                                String values = formatForInsert(new String[] {input[0], username, moviename});
                                addContainsStatement.execute("INSERT INTO contains VALUES " + values);
                                return Context.collection;
                            }

                        } else
                        {
                            if (rs.next())
                            {
                                int length = ms.getInt("length");
                                foundCStatement.execute("UPDATE collection SET" +
                                        " totallength=totallength-" + length +
                                        " WHERE username=\'" + username + "\' AND name=\'"
                                        + input[0]+ "\';");
                                foundCStatement.execute("UPDATE collection SET " +
                                        "movienumber=movienumber-1 WHERE username=\'" + username +
                                        "\' AND name=\'" + input[0]+ "\';");

                                Statement deleteContainsStatement = conn.createStatement();
                                deleteContainsStatement.execute("DELETE FROM contains WHERE username=\'"
                                        + username + "\' AND collectionname=\'" + input[0]+
                                        "\' AND moviename=\'" + moviename + "\';");
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
            System.out.println("Please input your collection name, add or remove, and the " +
                    "movie name as:\n<collectionName> [add/remove] <movie name>");
            return Context.editCollection;
        }

    }

    private String underToSpace(String s) {
        String output = "";
        for (int j = 0; j < s.length(); j++)
        {
            if (s.charAt(j) == '_')
            {
                output += ' ';
            } else {
                output += s.charAt(j);
            }
        }
        return output;
    }

    private Context tryRenameCollection(String command) throws SQLException {
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop")
                || command.toLowerCase().equals("back"))
        {
            return Context.collection;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("Please input your collection name and new name " +
                    "as:\n<collectionName> <new name>");
            return Context.renameCollection;
        }
        String[] input = command.split(" ");
        if (input.length == 2)
        {
            if (input[0].length() < 1 || input[0].length() > 63)
            {
                System.out.println("Your input does not match format requirements." +
                        " Please make sure your collection name is between 1 and 63 characters in length.");
                return Context.deleteCollection;
            } else if (input[1].length() < 1 || input[1].length() > 63)
            {
                System.out.println("Your input does not match format requirements." +
                        " Please make sure your new collection name is between 1 and 63 characters in length.");
                return Context.deleteCollection;
            } else
            {
                Statement foundStatement = conn.createStatement();
                ResultSet rs = foundStatement.executeQuery("SELECT * FROM " +
                        "collection WHERE username=\'" + username +
                        "\' AND name=\'" + input[0] + "\';");
                if (rs.next())
                {
                    // We cannot simply update the name, as that would break the foreign key constraint in our
                    // contains table.
                    Statement createDuplicateStatement = conn.createStatement();
                    String values = formatForInsert(new String[] {input[1], username, "" +
                            rs.getInt("movienumber"), "" + rs.getInt("totallength")});
                    createDuplicateStatement.execute("INSERT INTO collection VALUES " + values);

                    Statement transferContentsStatement = conn.createStatement();
                    transferContentsStatement.execute("UPDATE contains SET collectionname=\'" + input[1] +
                            "\' WHERE username=\'" + username +
                            "\' AND collectionname=\'" + input[0] + "\';");

                    Statement deleteCollectionStatement = conn.createStatement();
                    deleteCollectionStatement.execute("DELETE FROM collection WHERE username=\'" + username +
                            "\' AND name=\'" + input[0] + "\';");
                    return Context.collection;
                } else
                {
                    System.out.println("Unable to find a collection to rename with the given name.");
                    return Context.renameCollection;
                }
            }
        } else
        {
            System.out.println("Please input your collection name and new name " +
                    "as:\n<collectionName> <new name>");
            return Context.renameCollection;
        }
    }

    private Context tryDeleteCollection(String command) throws SQLException {
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop")
                || command.toLowerCase().equals("back"))
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
                System.out.println("Your input does not match format requirements. " +
                        "Please make sure your collection name is between 1 and 63 characters in length.");
                return Context.deleteCollection;
            } else
            {
                Statement foundStatement = conn.createStatement();
                ResultSet rs = foundStatement.executeQuery("SELECT username FROM collection" +
                        " WHERE username=\'" + username + "\' AND name=\'" + input[0] + "\';");
                if (rs.next())
                {
                    Statement deleteContainsStatement = conn.createStatement();
                    deleteContainsStatement.execute("DELETE FROM contains WHERE username=\'" + username + "\' " +
                            "AND collectionname=\'" + input[0] + "\';");
                    Statement deleteCollectionStatement = conn.createStatement();
                    deleteCollectionStatement.execute("DELETE FROM collection " +
                            "WHERE username=\'" + username + "\' AND name=\'" + input[0] + "\';");
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

    private Context tryListCollections() throws SQLException {
        Statement listStatement = conn.createStatement();
        ResultSet rs = listStatement.executeQuery("SELECT * FROM collection " +
                "WHERE username=\'" + username + "\' ORDER BY name ASC;");
        while (rs.next())
        {
            int totalLength = rs.getInt("totalLength");
            int hours = totalLength/60;
            int minutes = totalLength%60;
            String formatMin =String.format(":%d",minutes);
            if(minutes<10)
                formatMin = String.format(":0%d",minutes);

            System.out.println("Collection " + rs.getString("name") +
                    " contains " + rs.getInt("movieNumber") +
                    " movies, with a total length of "
                    +hours+ formatMin);
        }
        return Context.collection;
    }

    private Context tryCreateCollection(String command) throws SQLException {
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop")
                || command.toLowerCase().equals("back"))
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
                System.out.println("Your input does not match format requirements." +
                        " Please make sure your collection name is between 1 and 63" +
                        " characters in length.");
                return Context.createCollection;
            } else
            {
                Statement foundStatement = conn.createStatement();
                ResultSet rs = foundStatement.executeQuery("SELECT username FROM " +
                        "collection WHERE username=\'" + username + "\' AND name=\'" + input[0] + "\';");
                if (rs.next())
                {
                    System.out.println("You may only have one collection with a given name." +
                            " You may want to edit instead, or create a collection with a different name.");
                    return Context.createCollection;
                }

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

    private Context tryCollection(String command) throws SQLException {
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop")
                || command.toLowerCase().equals("back"))
        {
            return Context.loggedIn;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("Please input a collection specific command " +
                    "such as create, list, edit, delete, rename, or open.");
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
                tryListCollections();
                return Context.collection;
            } else if (input[0].toLowerCase().equals("edit"))
            {
                return Context.editCollection;
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
            if (input[0].toLowerCase().equals("create"))
            {
                return tryCreateCollection(recreate);
            } else if (input[0].toLowerCase().equals("edit"))
            {
                return tryEditCollection(recreate);
            } else if (input[0].toLowerCase().equals("delete"))
            {
                return tryDeleteCollection(recreate);
            } else if (input[0].toLowerCase().equals("rename"))
            {
                return tryRenameCollection(recreate);
            } else if (input[0].toLowerCase().equals("open"))
            {
                return tryOpenCollection(recreate);
            }
            return Context.collection;
        }
        return Context.collection;
    }

    private Context tryCommand(String command) throws SQLException {
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop") ||
                command.toLowerCase().equals("back") || command.toLowerCase().equals("logout")
                || command.toLowerCase().equals("log out"))
        {
            System.out.println("Logging out.");
            return Context.login;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("You can type collection to access collection commands, " +
                    "search to access search commands, follow to access follow commands, " +
                    "play to access play commands, user to access information on a specific user," +
                    "or recommend to access recommendation commands.");
            return Context.loggedIn;
        }
        String[] input = command.split(" ");
        if (input.length == 1)
        {
            if (input[0].toLowerCase().equals("collection"))
            {
                return Context.collection;
            } else if (input[0].toLowerCase().equals("search"))
            {
                return Context.search;
            }
            else if(input[0].toLowerCase().equals("follow")){
                return Context.follow;
            }
            else if(input[0].toLowerCase().equals("play")){
                return Context.play;
            }
            else if(input[0].toLowerCase().equals("recommend")){
                return Context.recommend;
            }
            else if(input[0].toLowerCase().equals("user")){
                return Context.user;
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
                return tryCollection(recreate);
            } else if (input[0].toLowerCase().equals("search"))
            {
                return trySearch(recreate);
            }
            else if(input[0].toLowerCase().equals("follow")){
                return tryFollow(recreate);
            }
            else if(input[0].toLowerCase().equals("play")){
                return tryPlay(recreate);
            }
            else if(input[0].toLowerCase().equals("recommend")){
                return tryRecommend(recreate);
            }
            else if(input[0].toLowerCase().equals("user")){
                return tryUser(recreate);
            }

        }

        System.out.println("Your input does not match any commands. " +
                "Please input your command as:\n<command>," +
                " from 'collection', 'search', 'follow', 'play', 'user', 'recommend'");
        return Context.loggedIn;
    }

    private Context tryPlay(String command) throws SQLException {
        String[] input = command.split(" ");
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop")
                || command.toLowerCase().equals("back"))
        {
            return Context.loggedIn;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("Please input the name of the movie or collection you wish to play," +
                    " optionally followed by the rating you wish to give it." +
                    " Please replace any spaces in names with underscores. " +
                    "Format as:\n<movie_name>/<collectionName> (rating 1-5)");
            return Context.play;
        }
        if(input.length < 1 || input.length > 2)
        {
            System.out.println("Your input does not match format requirements. " +
                    "Please input the name of the movie or collection you wish to play, " +
                    "optionally followed by the rating you wish to give it. " +
                    "Please replace any spaces in names with underscores." +
                    " Format as:\n<movie_name>/<collectionName> (rating 1-5)");
            return Context.play;
        }
        int rating = 0;
        String name = underToSpace(input[0]);
        if (input.length > 1) {
            try {
                rating = Integer.valueOf(input[1]);
            } catch (NumberFormatException e) {
                System.out.println("Unable to parse " + input[1] + " into an integer.");
                return Context.play;
            }
            if (rating != 0 && (rating < 1 || rating > 5)) {
                System.out.println("Ratings must be between 1 and 5, or 0 for no rating.");
                return Context.play;
            }
        }

        Statement findStatement = conn.createStatement();

        ResultSet mS = findStatement.executeQuery("SELECT * FROM movie WHERE name=\'" + name + "\';");
        ArrayList<String> movieNames = new ArrayList<>();

        if (mS.next())
        {
            movieNames.add(mS.getString("name"));
        } else
        {
            ResultSet cs = findStatement.executeQuery("SELECT * FROM collection " +
                    "WHERE name=\'" + name + "\' AND username=\'" + username + "\';");
            if (cs.next())
            {
                Statement listStatement = conn.createStatement();
                ResultSet rs = listStatement.executeQuery("SELECT * FROM contains " +
                        "WHERE username=\'" + username + "\' AND collectionname=\'" + input[0]+ "\';");
                System.out.println("Playing from collection " + input[0] + ".");
                while (rs.next())
                {
                    movieNames.add(rs.getString("moviename"));
                }
            } else
            {
                System.out.println("Unable to find movie or collection " + name + ".");
                return Context.play;
            }
        }


        for (String movie: movieNames)
        {
            System.out.println("Playing " + movie);

            Statement watchesStatement = conn.createStatement();
            ResultSet ws = watchesStatement.executeQuery("SELECT * FROM watches" +
                    " WHERE username=\'" + username + "\' AND moviename=\'" + movie + "\';");

            if (ws.next())
            {
                Statement updateStatement = conn.createStatement();
                updateStatement.execute("UPDATE watches SET \"watchCount\"=\"watchCount\"+1 WHERE " +
                        "username=\'" + username + "\' AND moviename=\'" + movie + "\';");

                if (rating != 0 && ws.getInt("rating") != 0)
                {
                    Statement oldRatings = conn.createStatement();
                    ResultSet oldSet = oldRatings.executeQuery("SELECT * from movie " +
                            "WHERE name=\'" + movie + "\';");
                    oldSet.next();
                    Statement removeOld = conn.createStatement();
                    if (oldSet.getInt("userratingcount") == 1)
                    {
                        removeOld.execute("UPDATE movie SET userratingcount=0, " +
                                "userratingavgscore=0.0 WHERE name=\'" + movie + "\';");
                    } else
                    {
                        removeOld.execute("UPDATE movie SET userratingcount=userratingcount-1," +
                                " userratingavgscore=((userratingavgscore*userratingcount)-"
                                + oldSet.getInt("userratingcount") +
                                ")/(userratingcount-1) WHERE name=\'" + movie + "\';");
                    }
                }
            } else
            {
                Statement updateStatement = conn.createStatement();
                String values = formatForInsert(new String[] {username, movie, "0", "1"});
                updateStatement.execute("INSERT INTO watches VALUES" + values);
            }

            if (rating != 0)
            {
                Statement newRating = conn.createStatement();
                newRating.execute("UPDATE movie SET userratingcount=userratingcount+1, " +
                        "userratingavgscore=((userratingavgscore*userratingcount)+"
                        + rating + ")/(userratingcount+1) WHERE name=\'" + movie + "\';");
                newRating.execute("UPDATE watches SET rating=" + rating +
                        "WHERE username=\'" + username + "\' AND moviename=\'" + movie + "\';");
            }
        }

        return Context.play;
    }

    private Context tryCreateUser(String command) throws SQLException {
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop")
                || command.toLowerCase().equals("back"))
        {
            return Context.login;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("Please input your new user info " +
                    "as:\n<username> <password> <firstname> <lastname> <email>");
            return Context.createUser;
        }
        String[] input = command.split(" ");
        if (input.length != 5)
        {
            System.out.println("Your input does not match format requirements. " +
                    "Please input your user info as:\n<username> <password> <firstname> <lastname> <email>");
            return Context.createUser;
        }
        if (input[0].length() <= 0 || input[0].length() > 63)
        {
            System.out.println("Your input does not match format requirements. " +
                    "Please make sure your username is between 1 and 63 characters in length.");
            return Context.createUser;
        }
        if (input[1].length() <= 0 || input[1].length() > 63)
        {
            System.out.println("Your input does not match format requirements." +
                    " Please make sure your password is between 1 and 63 characters in length.");
            return Context.createUser;
        }
        if (input[2].length() <= 0 || input[2].length() > 63)
        {
            System.out.println("Your input does not match format requirements." +
                    " Please make sure your first name is between 1 and 63 characters in length.");
            return Context.createUser;
        }
        if (input[3].length() <= 0 || input[3].length() > 63)
        {
            System.out.println("Your input does not match format requirements." +
                    " Please make sure your last name is between 1 and 63 characters in length.");
            return Context.createUser;
        }
        if (input[4].length() <= 0 || input[4].length() > 63)
        {
            System.out.println("Your input does not match format requirements." +
                    " Please make sure your email is between 1 and 63 characters in length.");
            return Context.createUser;
        }

        Statement foundStatement = conn.createStatement();
        ResultSet rs = foundStatement.executeQuery("SELECT password FROM account" +
                " WHERE username=\'" + username + "\';");
        if (rs.next()) {
            System.out.println("An account with this username already exists." +
                    " Please select a different username.");
            return Context.createUser;
        }


        java.sql.Timestamp sqlDate = new java.sql.Timestamp(Instant.now().toEpochMilli());

        username = input[0];

        Statement createAccountStatement = conn.createStatement();
        String values = formatForInsert(new String[] {input[0], input[1], input[2],
                input[3], sqlDate.toString(), input[4]});
        System.out.println(values);
        createAccountStatement.execute("INSERT INTO account VALUES " + values);

        createAccountStatement.execute("INSERT INTO logintime(username, logindatetime) " +
                "VALUES ('" +username+"', '"+sqlDate+"')");

        return Context.loggedIn;
    }

    private Context tryLogin(String command) throws SQLException
    {
        if (command.toLowerCase().equals("create") || command.toLowerCase().equals("new"))
        {
            return Context.createUser;
        }
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop")
                || command.toLowerCase().equals("back"))
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
            System.out.println("Your input does not match format requirements." +
                    " Please input your username and password as:\n<username> <password>");
            return Context.login;
        }
        if (input[0].length() <= 0 || input[0].length() > 63)
        {
            System.out.println("Your input does not match format requirements." +
                    " Please make sure your username is between 1 and 63 characters in length.");
            return Context.login;
        }
        if (input[1].length() <= 0 || input[1].length() > 63)
        {
            System.out.println("Your input does not match format requirements." +
                    " Please make sure your password is between 1 and 63 characters in length.");
            return Context.login;
        }

        String usernameIn = input[0];
        String password = input[1];
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(Instant.now().toEpochMilli());
        Statement loginStatement = conn.createStatement();
        ResultSet rs = loginStatement.executeQuery("SELECT password FROM account" +
                " WHERE username=\'" + usernameIn + "\' AND password=\'" + password + "\';");
        if (rs.next())
        {
            this.username = input[0];
            Statement updateTimeStatement = conn.createStatement();
            //updateTimeStatement.execute("UPDATE account SET
            // lastaccessdate=current_date WHERE username='" + username + "';");

            updateTimeStatement.execute("INSERT INTO logintime(username, logindatetime) " +
                    "VALUES ('" +username+"', '"+sqlDate+"')");

            return Context.loggedIn;
        }
        else
        {
            Statement foundStatement = conn.createStatement();
            rs = foundStatement.executeQuery("SELECT password FROM " +
                    "account WHERE username=\'" + usernameIn + "\';");
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
    private Context tryFollow(String command) throws SQLException
    {
        String[] input = command.split(" ");
        if (command.toLowerCase().equals("cancel") || command.toLowerCase().equals("stop")
                || command.toLowerCase().equals("back"))
        {
            return Context.loggedIn;
        }
        if (command.toLowerCase().equals("help") || command.toLowerCase().equals("?"))
        {
            System.out.println("Please input the username or email of the user " +
                    "you want to follow or unfollow as\n<username>");
            return Context.follow;
        }
        if(input.length != 1)
        {
            System.out.println("Your input does not match format requirements. " +
                    "Please give the name you want to follow:\n<username>");
            return Context.follow;
        }
        if(input[0].length() > 63){
            System.out.println("Please make sure the username you are typing in is " +
                    "between 1 and 63 characters in length.");
            return Context.follow;
        }
        Statement followStmt = conn.createStatement();

        String userToFollow = "";

        ResultSet rs21 = followStmt.executeQuery("SELECT username FROM account" +
                " WHERE email = \'"+input[0]+"\'");

        if(rs21.next()){
             userToFollow = rs21.getString(1);
        }
        else
        {
            userToFollow = input[0];
        }
        ResultSet rs0 = followStmt.executeQuery("SELECT username FROM account" +
                " WHERE username=\'" + userToFollow + "\'");
        if (rs0.next()) {
            ResultSet rs1 = followStmt.executeQuery("SELECT followinguser FROM follows" +
                    " WHERE followeduser =\'" + userToFollow +
                    "\' AND followinguser = \'" + username + "\'");
            if (rs1.next()) {
                followStmt.execute("DELETE FROM follows WHERE followeduser =\'" + userToFollow +
                        "\' AND followinguser = \'" + username + "\'");

                System.out.println("You have unfollowed " + userToFollow);
                return Context.loggedIn;
            } else {
                followStmt.execute("INSERT INTO follows VALUES(\'"
                        + userToFollow + "\',\'" + username + "\')");
                System.out.println("You are now following " + userToFollow);
                return Context.loggedIn;
            }
        } else
        {
            System.out.println("Unable to find user " + userToFollow + ".");
            return Context.follow;
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
