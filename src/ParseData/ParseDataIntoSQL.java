package ParseData;

/**
 * used to run methods in csv parser and PopulateDatabase class
 */
public class ParseDataIntoSQL {

    public static void main(String[] args) {
        try{

            PopulateDatabase populateDatabase = new PopulateDatabase();
            populateDatabase.connecttoDb();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
