package com23;
import java.sql.*;
import java.text.ParseException;
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class LibSystem23 {

    private static Connection con = LibSystem23.connect();

    static boolean end = false;
    public static Connection connect(){
        String dbAddress = "jdbc:mysql://projgw.cse.cuhk.edu.hk:2633/db23";
        String dbUsername = "Group23";
        String dbPassword = "hipaas";

        Connection con = null;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(dbAddress, dbUsername, dbPassword);
        } catch (ClassNotFoundException e){
            System.out.println("[Error]: Java MySQL DB Driver not found!" + e);
            System.exit(0);
        } catch (SQLException e){
            System.out.println(e);
        }
        return con;
    }

    public static void main(String[] args) throws Exception, Throwable{
        System.out.println("Welcome to the Library Inquiry System!\n");
        do {
            String opening = "-----Main menu-----\n";
            opening += "What kinds of operations would you like to perform?\n";
            opening += "1. Operations for Administrator\n";
            opening += "2. Operations for Library User\n";
            opening += "3. Operations for Librarian\n";
            opening += "4. Exit this program\n";
            opening += "Enter Your Choice: ";

            try {

                System.out.print(opening);
                Scanner scanner = new Scanner(System.in);
                int choice = scanner.nextInt();
                /* change for case switch */
                switch (choice) {
                    case 1:
                        admin();
                        break;
                    case 2:
                        user();
                        break;
                    case 3:
                        librarian();
                        break;
                    case 4:
                        end = true;
                        break;
                }
            } catch (Exception e) {
                System.out.println("[ERROR]: Type in your choice again.");
                main(null);
            }
        } while (!end);


    }

    //5.1 ADMIN
    public static void admin() throws Exception, Throwable {
        do {
            String display = "-----Operations for administrator menu-----\n";
            display += "What kind of operation would you like to perform?\n";
            display += "1. Create all tables\n";
            display += "2. Delete all tables\n";
            display += "3. Load from datafile\n";
            display += "4. Show number of records in each table\n";
            display += "5. Return to the main menu\n";
            display += "Enter Your Choice: ";
            System.out.print(display);

            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            while (choice < 1 || choice > 5) {
                System.out.println("[Error]: Invalid Input.");
                choice = scanner.nextInt();
            }
            switch (choice) {
                case 1:
                    createTable();
                    break;
                case 2:
                    deleteTable();
                    break;
                case 3:
                    loadData();
                    break;
                case 4:
                    ShowRecords();
                    break;
                case 5:
                    end = true;
                    main(null);
                    break;
                default:
                    System.out.println("[Error]: Invalid Input.");
            }
        } while (!end);
    }

    // Create table schemas in the database
    private static void createTable() {
        String user_category = "CREATE TABLE IF NOT EXISTS user_category(" +
                "ucid INT   NOT NULL," +
                "max INT    NOT NULL," +
                "period INT NOT NULL," +
                "PRIMARY KEY (ucid))";

        String libuser = "CREATE TABLE IF NOT EXISTS libuser(" +
                "libuid CHAR(10)      NOT NULL," +
                "name VARCHAR(25)     NOT NULL," +
                "age INT              NOT NULL," +
                "address VARCHAR(100) NOT NULL," +
                "ucid INT             NOT NULL," +
                "PRIMARY KEY (libuid)," +
                "FOREIGN KEY (ucid) REFERENCES user_category (ucid))";

        String book_category = "CREATE TABLE IF NOT EXISTS book_category(" +
                "bcid INT           NOT NULL," +
                "bcname VARCHAR(30) NOT NULL," +
                "PRIMARY KEY (bcid))";

        String book = "CREATE TABLE IF NOT EXISTS book(" +
                "callnum CHAR(8)   NOT NULL," +
                "title VARCHAR(30) NOT NULL," +
                "publish Date," +
                "rating FLOAT," +
                "tborrowed INT     NOT NULL," +
                "bcid INT          NOT NULL," +
                "PRIMARY KEY (callnum)," +
                "FOREIGN KEY (bcid) REFERENCES book_category (bcid))";

        String copy = "CREATE TABLE IF NOT EXISTS copy(" +
                "copynum INT     NOT NULL," +
                "callnum CHAR(8) NOT NULL," +
                "PRIMARY KEY (copynum, callnum)," +
                "FOREIGN KEY (callnum) REFERENCES book (callnum))";

        String borrow = "CREATE TABLE IF NOT EXISTS borrow(" +
                "libuid CHAR(10) NOT NULL," +
                "callnum CHAR(8) NOT NULL," +
                "copynum INT     NOT NULL," +
                "checkout_date Date," +
                "return_date Date," +
                "PRIMARY KEY (libuid, callnum, checkout_date, copynum)," +
                "FOREIGN KEY (libuid) REFERENCES libuser (libuid)," +
                "FOREIGN KEY (callnum) REFERENCES book (callnum)," +
                "FOREIGN KEY (copynum) REFERENCES copy (copynum))";

        String authorship = "CREATE TABLE IF NOT EXISTS authorship(" +
                "aname VARCHAR(25) NOT NULL," +
                "callnum CHAR(8)   NOT NULL," +
                "PRIMARY KEY (aname, callnum)," +
                "FOREIGN KEY (callnum) REFERENCES book(callnum))";

        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate(user_category);
            stmt.executeUpdate(libuser);
            stmt.executeUpdate(book_category);
            stmt.executeUpdate(book);
            stmt.executeUpdate(copy);
            stmt.executeUpdate(borrow);
            stmt.executeUpdate(authorship);


            System.out.println("Processing...Done. Database is initialized.");
        } catch (SQLException e) {
            System.out.println(e);
        }

    }

    // Delete table schemas in the database
    private static void deleteTable() {
        String user_category = "DROP TABLE IF EXISTS user_category";
        String libuser = "DROP TABLE IF EXISTS libuser";
        String book_category = "DROP TABLE IF EXISTS book_category";
        String book = "DROP TABLE IF EXISTS book";
        String copy = "DROP TABLE IF EXISTS copy";
        String borrow = "DROP TABLE IF EXISTS borrow";
        String authorship = "DROP TABLE IF EXISTS authorship";

        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate(borrow);
            stmt.executeUpdate(libuser);
            stmt.executeUpdate(user_category);
            stmt.executeUpdate(copy);
            stmt.executeUpdate(authorship);
            stmt.executeUpdate(book);
            stmt.executeUpdate(book_category);

            System.out.println("Processing...Done! Database is removed.");
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    // Load data from a dataset
    private static void loadData() throws IOException {
        SimpleDateFormat s_d = new SimpleDateFormat("dd/MM/yyyy");
        System.out.print("Type in the Source Data Folder Path: ");
        Scanner scan = new Scanner(System.in);
        String path = scan.nextLine();

        // Insert data into user_category table
        scan = new Scanner(new File(path + "/user_category.txt"));
        while (scan.hasNextLine()) {
            String[] data = scan.nextLine().split("\t");
            try {
                Statement stmt = con.createStatement();
                String insertQuery = "INSERT INTO user_category VALUES('" + data[0] + "'," + "'" + data[1] + "','" + data[2] + "')";
                stmt.executeUpdate(insertQuery);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Insert data into libuser table
        scan = new Scanner(new File(path + "/user.txt"));
        while (scan.hasNextLine()) {
            String line = scan.nextLine().replace("'", "+");
            String[] data = line.split("\t");
            try {
                Statement stmt = con.createStatement();
                String insertQuery = "INSERT INTO libuser VALUES('" + data[0] + "','" + data[1] + "','" + data[2] + "','"
                        + data[3] + "','" + data[4] + "')";
                stmt.executeUpdate(insertQuery);
            } catch (SQLException e){
                e.printStackTrace();
            }
        }

        // Insert data into book_category table
        scan = new Scanner(new File(path + "/book_category.txt"));
        while (scan.hasNextLine()){
            String[] data = scan.nextLine().split("\t");
            String insertQuery = "";
            try {
                Statement stmt = con.createStatement();
                insertQuery = "INSERT INTO book_category VALUES('" + data[0] + "','" + data[1] + "')";
                stmt.executeUpdate(insertQuery);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Insert data into book table // no.5 null value for rating
        scan = new Scanner(new File(path + "/book.txt"));
        while (scan.hasNextLine()) {
            String[] data = scan.nextLine().split("\t");
            String insertQuery = "";
            try {
                Statement stmt = con.createStatement();
                java.sql.Date publish = new java.sql.Date(s_d.parse(data[4]).getTime());
                if (data[5].equals("null")) {
                    insertQuery = "INSERT INTO book VALUES('" + data[0] + "','" + data[2] + "','" + publish + "',NULL,'" + data[6] + "','" + data[7] + "')";
                } else {
                    insertQuery = "INSERT INTO book VALUES('" + data[0] + "','" + data[2] + "','" + publish + "','" + data[5] + "','" + data[6] + "','" + data[7] + "')";
                }
                stmt.executeUpdate(insertQuery);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Insert data into copy table
        scan = new Scanner(new File(path + "/book.txt"));
        while (scan.hasNextLine()) {
            String[] data = scan.nextLine().split("\t");
            String Query5 = "";
            try {
                Statement stmt = con.createStatement();
                for (int i = 1; i <= Integer.valueOf(data[1]) ; i++){
                    Query5 = "INSERT INTO copy VALUES('" + i + "','" + data[0] + "')";
                    stmt.executeUpdate(Query5);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Insert data into borrow table
        scan = new Scanner(new File(path + "/check_out.txt"));
        while (scan.hasNextLine()){
            String[] data = scan.nextLine().split("\t");
            String insertQuery = "";
            try {
                Statement stmt = con.createStatement();
                java.sql.Date c_date = new java.sql.Date(s_d.parse(data[3]).getTime());
                if (data[4].equals("null")) {
                    insertQuery = "INSERT INTO borrow VALUES('" + data[2] + "','" + data[0] + "','" + data[1] + "','" + c_date + "',NULL)";
                } else {
                    java.sql.Date r_date = new java.sql.Date(s_d.parse(data[4]).getTime());
                    insertQuery = "INSERT INTO borrow VALUES('" + data[2] + "','" + data[0] + "','" + data[1] + "','" + c_date + "','" + r_date + "')";
                }
                stmt.executeUpdate(insertQuery);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Insert data into authorship table
        scan = new Scanner(new File(path + "/book.txt"));
        while (scan.hasNextLine()){
            String[] data = scan.nextLine().split("\t");
            String[] author = data[3].split("\\,");
            String Query7 = "";
            try {
                Statement stmt = con.createStatement();
                for (int j=0; j<author.length; j++) {
                    Query7 = "INSERT INTO authorship VALUES('" + author[j]+ "','" + data[0] + "')";
                    stmt.executeUpdate(Query7);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        System.out.println("Processing...Done. Data is inputted to the database.");
    }

    // Show the number of records in each table
    private static void ShowRecords() {
        System.out.println("Number of records in each table:");

        String user_category = "SELECT COUNT(*) AS sum FROM user_category";
        String libuser = "SELECT COUNT(*) AS sum FROM libuser";
        String book_category = "SELECT COUNT(*) AS sum FROM book_category";
        String book = "SELECT COUNT(*) AS sum FROM book";
        String copy = "SELECT COUNT(*) AS sum FROM copy";
        String borrow = "SELECT COUNT(*) AS sum FROM borrow";
        String authorship = "SELECT COUNT(*) AS sum FROM authorship";

        try{
            // No of records in user category table
            Statement stmt = con.createStatement();
            ResultSet rs1 = stmt.executeQuery(user_category);
            rs1.next();
            System.out.println("user_category: " + rs1.getInt("sum"));

            // No of records in user table
            ResultSet rs2 = stmt.executeQuery(libuser);
            rs2.next();
            System.out.println("libuser: " + rs2.getInt("sum"));

            // No of records in book category table
            ResultSet rs3 = stmt.executeQuery(book_category);
            rs3.next();
            System.out.println("book_category: " + rs3.getInt("sum"));

            // No of records in book table
            ResultSet rs4 = stmt.executeQuery(book);
            rs4.next();
            System.out.println("book: " + rs4.getInt("sum"));

            // No of records in copy table
            ResultSet rs5 = stmt.executeQuery(copy);
            rs5.next();
            System.out.println("copy: " + rs5.getInt("sum"));

            // No of records in borrow table
            ResultSet rs6 = stmt.executeQuery(borrow);
            rs6.next();
            System.out.println("borrow: " + rs6.getInt("sum"));

            // No of records in authorship table
            ResultSet rs7 = stmt.executeQuery(authorship);
            rs7.next();
            System.out.println("authorship: " + rs7.getInt("sum"));

        }catch (SQLException e) {
            System.out.println("[Error]: No tables existed in the database.");
        }
    }

    //5.2 LIB-USER
    public static void user() throws Exception, Throwable {
        do {
            String display = "-----Operations for library user menu-----\n";
            display += "What kind of operation would you like to perform?\n";
            display += "1. Search for Books\n";
            display += "2. Show loan record of a user\n";
            display += "3. Return to the main menu\n";
            display += "Enter Your Choice: ";
            System.out.print(display);

            @SuppressWarnings("resource")
            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            while (choice < 1 || choice > 5) {
                System.out.println("[Error]: Invalid Input.");
                choice = scanner.nextInt();
            }
            switch (choice) {
                case 1:
                    search();
                    break;
                case 2:
                    show_check_out();
                    break;
                case 3:
                    main(null);
                    break;
                default:
                    System.out.println("[Error]: No tables existed in the database.");
            }
        } while (!end);
    }

    private static void search() throws Exception, Throwable {
        System.out.println("Choose the Search criterion:");
        System.out.println("1. call number");
        System.out.println("2. title");
        System.out.println("3. author");
        System.out.print("Choose the Search criterion: ");
        Scanner scan = new Scanner(System.in);
        int choice = scan.nextInt();
        System.out.print("Type in the Search Keyword:");
        scan = new Scanner(System.in);
        String keyword = scan.nextLine();
        switch(choice){
            case 1: search_by_callnum(keyword);
            case 2: search_by_title(keyword);
            case 3: search_by_author(keyword);
        }
        return;
    }

    @SuppressWarnings("finally")
    private static void search_by_callnum(String a) throws Exception, Throwable {
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://projgw.cse.cuhk.edu.hk:2633/db23",
                    "Group23", "hipaas");
            Statement stmt = conn.createStatement();

            ResultSet rs1 = stmt.executeQuery("SELECT book.callnum, book.title, book_category.bcname, authorship.aname, book.rating, copy.copynum "
                    + "FROM book, authorship, book_category, copy "
                    + "WHERE book.callnum=authorship.callnum AND book.callnum='" + a + "' "
                    + "GROUP BY book.callnum");

            System.out.println("|Callnum|Title|Book Category|Author|Rating|Available Copy Num|");

            while (rs1.next()){
                System.out.print("|" +rs1.getString(1) + "|" + rs1.getString(2) + "|" + rs1.getString(3) + "|"+  rs1.getString(4)+"|"+rs1.getString(5)+"|"+rs1.getInt(6)+"|"+"\n");
            }

            System.out.println("End of Query");
            rs1.close();
            stmt.close();
            conn.close();

        }catch(Exception e){
            System.out.println("[Error]: No tables existed in the database.");

        }
        finally{
            user(); //back to the main menu
            return;
        }

    }


    @SuppressWarnings("finally")
    private static void search_by_title(String b) throws Exception, Throwable {
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://projgw.cse.cuhk.edu.hk:2633/db23",
                    "Group23", "hipaas");


            Statement stmt = conn.createStatement();
            ResultSet rs2 = stmt.executeQuery("SELECT B.callnum, B.title, BC.bcname, A.aname, B.rating, C.copynum  "
                    + "FROM book B, authorship A,book_category BC,copy C "
                    + "WHERE B.callnum = A.callnum AND B.bcid = BC.bcid AND B.callnum=C.callnum "
                    + "AND (B.title LIKE '%"+b+"%' OR B.title LIKE '"+b+"%' OR B.title LIKE '%"+b+"') "
                    + "ORDER BY callnum ASC");


            System.out.println("|Callnum|Title|Book Category|Author|Rating|Available Copy Num|");

            while (rs2.next()){
                System.out.print("|"+rs2.getString(1)+"|"+rs2.getString(2)+ "|"+rs2.getString(3)+ "|"+rs2.getString(4)+ "|"+rs2.getString(5)+ "|"+rs2.getInt(6)+ "|"+"\n");
                stmt = conn.createStatement();
            }

            System.out.println("End of Query");
            rs2.close();
            stmt.close();
            conn.close();

        }catch(Exception e){
            System.out.println(e);

        }
        finally{
            user(); //back to the main menu
            return;
        }
    }


    @SuppressWarnings("finally")
    private static void search_by_author(String c) throws Exception, Throwable {
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://projgw.cse.cuhk.edu.hk:2633/db23",
                    "Group23", "hipaas");
            Statement stmt = conn.createStatement();
            ResultSet rs3 = stmt.executeQuery("SELECT book.callnum, book.title, book_category.bcname, authorship.aname, book.rating, copy.copynum FROM book, authorship, book_category, copy "
                    + "WHERE book.callnum=authorship.callnum AND book.callnum=copy.callnum "
                    + "AND book_category.bcid = book.bcid "
                    + "AND (aname LIKE '%"+c+"%' OR aname LIKE '"+c+"%' OR aname LIKE '%"+c+"') "
                    + "ORDER BY book.callnum ASC");

            System.out.println("|Callnum|Title|Book Category|Author|Rating|Available Copy Num|");

            while (rs3.next()){
                System.out.print("|" + rs3.getString(1)+ "|"+rs3.getString(2)+ "|"+rs3.getString(3)+ "|"+rs3.getString(4)+ "|"+ rs3.getString(5)+ "|"+rs3.getInt(6)+ "|"+"\n");
                stmt = conn.createStatement();
            }
            System.out.println("End of Query");
            rs3.close();
            stmt.close();
            conn.close();

        }catch(Exception e){
            System.out.println(e);
        }
        finally{
            user(); //back to the main menu
            return;
        }
    }



    @SuppressWarnings("finally")
    private static void show_check_out() throws Exception, Throwable {
        System.out.print("Enter The User ID: ");
        Scanner scanner = new Scanner(System.in);
        String userid = scanner.nextLine();
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://projgw.cse.cuhk.edu.hk:2633/db23",
                    "Group23", "hipaas");
            Statement stmt = conn.createStatement();
            ResultSet rs4 = stmt.executeQuery("SELECT B.callnum, C.copynum, B.title, C.checkout_date, C.return_date, C.libuid, U.libuid "
                    + "FROM  borrow C,libuser U,  book B "
                    + "WHERE C.callnum = B.callnum  AND C.libuid='" + userid + "' "
                    //+ "GROUP BY B.callnum "
                    + "ORDER BY C.checkout_date DESC");

            rs4.next();
            System.out.println("Loan Record:");
            System.out.println("|Callnum|Available Copy Num|Title|Date|Return?|");

            while (rs4.next())
                System.out.println("|" + rs4.getString(1) + "|" + rs4.getInt(2) + "|" + rs4.getString(3) +  "|" + rs4.getDate(4) + "|" + ((rs4.getDate(5)==null)?"No":"Yes")+ "|");

            System.out.println("End of Query");

            rs4.close();
            stmt.close();
            conn.close();

        }catch(Exception e){
            System.out.println(e);
        }
        finally{
            user(); //back to the main menu
            return;
        }
    }


    // 5.3 LIBRARIAN
    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd");

    public static void librarian() throws IOException, Exception, Throwable {

        String opening = "-----Operations for librarian menu-----\n";
        opening += "What kinds of operations would you like to perform?\n";
        opening += "1. Book Borrowing\n";
        opening += "2. Book Returning\n";
        opening += "3. List all un-returned which are checked-out within a period\n";
        opening += "4. Return to the Main Menu\n";
        opening += "Enter Your Choice:";

        System.out.println();
        System.out.print(opening);

        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        while (choice < 1 || choice > 5){
            System.out.println("Error");
            choice = scanner.nextInt();
        }

        switch (choice) {
            case 1:
                borrowBook();
                break;
            case 2:
                returnBook();
                break;
            case 3:
                unreturnedBooks();
                break;
            case 4:
                main(null);
                break;
        }
    }

    private static void borrowBook() throws IOException, Exception, Throwable {
        System.out.println("Enter the User-ID: ");
        Scanner scanner = new Scanner(System.in);
        String userID = scanner.next();

        System.out.println("Enter the Call Number: ");
        String callNumber = scanner.next();

        System.out.println("Enter the Copy Number: ");
        String copyNumber = scanner.next();

        String checkUser = "SELECT COUNT(*) FROM libuser WHERE libuid = '" + userID + "'";
        String checkBook = "SELECT COUNT(*) FROM copy WHERE callnum ='"+ callNumber + "' AND copynum = "+ copyNumber;
        String checkAvailability = "SELECT COUNT(*) FROM borrow WHERE (callnum ='"+ callNumber + "' AND copynum = "+ copyNumber + " AND (return_date IS NULL))";
        Date d = new Date();
        String insert = "INSERT INTO borrow (callnum, copynum, libuid, checkout_date)"
                + "VALUES ('"+ callNumber +"','"+ copyNumber +"','"+ userID + "','"+ dateFormat.format(d) + "')";



        try {
            Statement st1 = con.createStatement();
            Statement st2 = con.createStatement();
            Statement st3 = con.createStatement();
            Statement st4 = con.createStatement();
            Statement st5 = con.createStatement();

            ResultSet r1 = st1.executeQuery(checkUser);
            ResultSet r2 = st2.executeQuery(checkBook);
            ResultSet r3 = st3.executeQuery(checkAvailability);

            if (r1.next() && r2.next() && r3.next()) {

                if ((r1.getInt(1) == 1 && r2.getInt(1) == 1 && r3.getInt(1) == 0)) {
                    st4.executeUpdate(insert);

                    r1.close();
                    r2.close();
                    r3.close();
                    st1.close();
                    st2.close();
                    st3.close();
                    st4.close();
                    st5.close();
                    System.out.println("Book borrowing performed successfully.");
                    librarian();


                } else {
                    System.out.println("Invalid information or book not available.");
                    r1.close();
                    r2.close();
                    r3.close();
                    st1.close();
                    st2.close();
                    st3.close();
                    st4.close();
                    st5.close();
                    librarian();

                }
            } else {
                System.out.println("Invalid information or book not available.");
                r1.close();
                r2.close();
                r3.close();
                st1.close();
                st2.close();
                st3.close();
                st4.close();
                st5.close();
                librarian();
            }


        } catch (SQLException e) {
            System.out.println("Book not found/unavailable.");
            e.printStackTrace();

        }

    }

    private static void returnBook() throws IOException, Exception, Throwable {
        System.out.println("Enter the User-ID: ");
        Scanner scanner = new Scanner(System.in);
        String userID = scanner.next();

        System.out.println("Enter the Call Number: ");
        String callNumber = scanner.next();

        System.out.println("Enter the Copy Number: ");
        String copyNumber = scanner.next();

        String checkUser = "SELECT COUNT(*) FROM libuser WHERE libuid = '" + userID + "'";
        String checkBook = "SELECT COUNT(*) FROM copy WHERE callnum ='"+ callNumber + "' AND copynum = "+ copyNumber;
        String checkAvailability = "SELECT COUNT(*) FROM borrow WHERE ((return_date IS NULL) AND (callnum ='"+ callNumber + "') AND (copynum = "+ copyNumber + ") AND (libuid = '"+ userID +"'))";

        Date d = new Date();



        try {
            Statement st1 = con.createStatement();
            Statement st2 = con.createStatement();
            Statement st3 = con.createStatement();
            Statement st4 = con.createStatement();
            Statement st5 = con.createStatement();
            Statement st6 = con.createStatement();
            Statement st7 = con.createStatement();

            ResultSet r1 = st1.executeQuery(checkUser);
            ResultSet r2 = st2.executeQuery(checkBook);
            ResultSet r3 = st3.executeQuery(checkAvailability);

            if(r1.next() && r2.next() && r3.next()) {
                if ((r1.getInt(1) == 1 && r2.getInt(1) == 1 && r3.getInt(1) == 1)) {

                    System.out.println("Enter your rating of the book (Integer from 1-10): ");
                    String user_rating = scanner.next();
                    ResultSet r4 = st4.executeQuery("SELECT rating FROM book WHERE callnum = '" + callNumber+ "'");
                    ResultSet r5= st5.executeQuery("SELECT tborrowed FROM book WHERE callnum = '" + callNumber + "'");

                    if (r4.next() && r5.next()) {
                        double new_rating = (r4.getDouble(1)* r5.getInt(1)+ Double.parseDouble(user_rating))/(r5.getInt(1)+1);

                        String query = "UPDATE borrow "
                                + "SET return_date = '" + dateFormat.format(d)+ "' "
                                + "WHERE ((return_date IS NULL) AND (callnum ='"+ callNumber + "') AND (copynum = "+ copyNumber + ") AND (libuid = '"+ userID +"'))";
                        String updatebook = "UPDATE book SET tborrowed = " + (r5.getInt(1)+1) +", rating = " + new_rating + " WHERE callnum = '"+ callNumber +"'";



                        st6.executeUpdate(query);
                        st7.executeUpdate(updatebook);
                        System.out.println("Book returning performed successfully.");
                        librarian();
                    }
                    r1.close();
                    r2.close();
                    r3.close();
                    r4.close();
                    r5.close();
                    st1.close();
                    st2.close();
                    st3.close();
                    st4.close();
                    st5.close();
                    st6.close();
                } else {
                    System.out.println("Invalid information. Book ist already returned or wrong input information.");

                    r1.close();
                    r2.close();
                    r3.close();
                    st1.close();
                    st2.close();
                    st3.close();
                    st4.close();
                    st5.close();
                    st6.close();
                    librarian();
                }
            } else {
                System.out.println("Invalid information. Book ist already returned or wrong input information.");
                r1.close();
                r2.close();
                r3.close();
                st1.close();
                st2.close();
                st3.close();
                st4.close();
                st5.close();
                st6.close();
                librarian();
            }
        } catch (SQLException e) {
            e.printStackTrace();

        } catch (NumberFormatException e) {
            System.out.println("Rate your book with integers from 1 to 10!");

        }

    }

    private static void unreturnedBooks() throws IOException, Exception, Throwable {
        System.out.println("Type in starting date [dd/mm/yyyy]: ");
        Scanner scanner = new Scanner(System.in);
        String startDate = scanner.next();
        System.out.println("Type in ending date [dd/mm/yyyy]: ");
        String endDate = scanner.next();



        final String OLD_FORMAT = "dd/MM/yyyy";
        final String NEW_FORMAT = "yyyy/MM/dd";

        String newStartDate = " ";
        String newEndDate = " ";

        SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
        try {
            Date d = sdf.parse(startDate);
            Date d2 = sdf.parse(endDate);

            sdf.applyPattern(NEW_FORMAT);
            newStartDate = sdf.format(d);
            newEndDate = sdf.format(d2);

        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        String unreturnedBooks = "SELECT * FROM borrow "
                + "WHERE ((return_date IS NULL) AND (checkout_date BETWEEN CAST('"+ newStartDate + "' as date) AND CAST('"+ newEndDate + "' as date)))"
                + "ORDER BY checkout_date DESC";

        Statement st;
        try {
            st = con.createStatement();
            ResultSet r1 = st.executeQuery(unreturnedBooks);
            System.out.println("USER-ID | Call Number | Copy Number | Check-out date | Return Date");
            ResultSetMetaData rsmd = r1.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            while (r1.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(" | ");
                    System.out.print(r1.getString(i));
                }
                System.out.println("");
            }
            System.out.println("End of query.");
            r1.close();
            st.close();
            librarian();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

