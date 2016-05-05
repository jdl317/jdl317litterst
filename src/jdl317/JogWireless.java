/*
 * This class contains the main method of the program.  The JogWireless class establishes a connection
 * With the database, and then brings the user to the Startup screen.
 */
package jdl317;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;


public class JogWireless {
    
    public static void main(String [] args) throws SQLException{
        
        ResultSet result = null;
        Connection con = null;
        Statement s = null;
        Scanner scan = new Scanner(System.in);
        boolean connected = false;
        do{
            try{
                Class.forName("oracle.jdbc.driver.OracleDriver");
                //Username and Password Request
                System.out.print("Enter Oracle user id: ");
                String username = scan.nextLine();
                System.out.print("Enter Oracle password for " + username + ": ");
                String password = scan.nextLine();
                con=DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", username, password);
                connected = true;
                //s = con.createStatement();
                System.out.println("Connection successfully made...");
                //Series of catch blocks for ClassNotFoundExceptions, SQLExceptions, and other general exceptions.
            } catch (ClassNotFoundException e) {
            System.out.println("Could not find 'oracle.jdbc.driver.OracleDriver' class");
            } catch (SQLException sql) {
            System.out.println("Invalid username or password. Try again.");
            } catch (Exception e) {
            System.out.println("Error");
            }
        } while(!connected);
        Startup start = new Startup();
        while(!start.exit){
            start.login(scan);
            switch(start.selection){
                case 0:
                    start.exit = true; break;
                case 1: 
                    System.out.println("Purchase");
                    Purchase purchase = new Purchase(con);
                    break;
                case 2:
                    System.out.println("Usage");
                    Usage usage = new Usage(con);
                    break;
                default:
                    System.out.println("Please enter a valid number");
            }
        }
        con.close();
        System.out.println("You have successfully exited the system... Thanks for using Jog Wireless!!");
        
    }
}
