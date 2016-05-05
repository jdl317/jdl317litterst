/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdl317;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jefflitterst
 */
public class Usage {
    Scanner scan = new Scanner(System.in);
    Connection con;
    String inputLine = " ";
    int lineCounter;
    boolean violation;
    char ch;
    int numTimestamps;
    int location;
    String outfile;
    File out;
    PrintWriter output;
    
    public Usage(Connection con)throws SQLException{
        this.con = con;
        ch = nextChar();
        location = 0;
        violation = false;
        numTimestamps = 0;
        lineCounter = 1;
        System.out.println("Welcome to the Usage Center");
        System.out.println("\nPlease enter the name of an input file for us to process, as well as an output file for any errors we may encounter.");
        System.out.println("Output File Name: ");
        outfile = scan.nextLine();
        out = new File(outfile);
        try{
            output = new PrintWriter(out);
        }catch(FileNotFoundException e){
            System.out.println("File unable to be found");
        }
        try{
            beginUsage();
        }catch(SQLException e){
            System.out.println(e.getMessage());
            this.con.close();
        }
        output.close();
    }
    
    public char nextChar(){
        if(location >= inputLine.length()){
         return ch = '$';
        }
        ch = inputLine.charAt(location);
        location++;
        return ch;
    }
    
    public boolean isDigit(char ch){
        for(int i = 0; i < 10; i++){
            if(Character.digit(ch, 10) == i){
            return true;
            }
        }
        return false;
    }
    
    public boolean isLetter(char ch){
        if(ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z'){
         return true;
        }
        else{
         return false;
        }
    }
    
    private void beginUsage()throws SQLException{
        String infile = "";
        boolean valid = false;
        File in = null;
        System.out.println("Input File: ");
        while(!valid){
                infile = scan.nextLine();
                try{
                    in = new File(infile);
                    scan = new Scanner(in);
                }catch(FileNotFoundException e){
                    
                }
            if(in.isFile()){
                System.out.println("File has been found...");
                valid = true;
            }
            else{
                System.out.println("The filename you entered didn't show up in our records.  Please try again.");
            }
        }
        
        //Read data in from file
        List<String> tokens;
        System.out.println("Printing file data");
        while(scan.hasNextLine()){
            location = 0;
            ch = ' ';
            violation = false;
            numTimestamps = 0;
            inputLine = scan.nextLine();
            tokens = new ArrayList<String>();
            tokens = parseLine(inputLine);
            if(violation){
                //out = new File(outfile);
                //output = new PrintWriter(out);
                System.out.println("Printing error to file");
                //Send to error file
                output.println("Error at Line "+lineCounter+": Does not follow correct format");
                violation = false;
            }
            else{
                System.out.println("Checking Tokens for validity");
                try {
                    checkTokens(tokens);
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                    con.close();
                }
            }
            lineCounter++;
            //System.out.println("Tokens: "+tokens.get(0)+ " "+tokens.get(1)+" "+tokens.get(2));
        }
        deleteExtra();
    }
    private void checkTokens(List<String> tokens)throws SQLException{
        List<String> list = null;
        boolean call = false;
        boolean text = false;
        boolean web = false;
        //First token must be source phone num.
        if(tokens.get(0).length() == 10){
            if(tokens.size() == 3 && tokens.get(2).length() < 9){
                //Web Usage
                System.out.println("Line "+lineCounter+" is web usage");
                web = true;
            }
            else if(tokens.get(1).length() == 10 && numTimestamps == 2){
                //Call usage
                System.out.println("Line "+lineCounter+" is call usage");
                call = true;
            }
            else if(tokens.get(1).length() == 10 && numTimestamps == 1 && tokens.get(3).length() < 9){
                //text usage
                System.out.println("Line "+lineCounter+" is text usage");
                text = true;
            }
            else{
                //Error
                output.println("Error at Line "+lineCounter+": Does not follow correct format");
            }
        }
        else{
            output.println("Error at Line "+lineCounter+": Does not follow correct format");
        }
        //For call and text, one number must be in database, otherwise information is irrelevant
        if(call || text){
            String knownNum1 = "";
            String knownNum2 = "";
            boolean known = false;
            try{
                Statement s = con.createStatement();
                ResultSet r = s.executeQuery("select phone_num from phone_number");
                list = resultsToArrayList(r);
                s.close();
                //Confirms that numbers are in database, otherwise the info isn't relevant
                for(int i = 0; i < list.size(); i++){
                    if(tokens.get(0).equals(list.get(i))){
                        known = true;
                        knownNum1 = tokens.get(0);
                    }
                    if(tokens.get(1).equals(list.get(i))){
                        known = true;
                        knownNum2 = tokens.get(1);
                    }
                    
                }
                if(!known){
                    //Send to error file
                    output.println("Error at line "+lineCounter+": Numbers given are not in Jog Wireless Database");
                    
                }
                else{
                    if(call){
                        updateCall(tokens, knownNum1, knownNum2);
                    }
                    if(text){
                        updateText(tokens, knownNum1, knownNum2);
                    }
                }
            }catch(SQLException e){
                System.out.println(e.getMessage());
                con.close();
            }
        }
        else{
            //Web
            boolean known = false;
            try{
                Statement s = con.createStatement();
                ResultSet r = s.executeQuery("select phone_num from phone_number");
                list = resultsToArrayList(r);
                s.close();
                //Confirms that numbers are in database, otherwise the info isn't relevant
                for(int i = 0; i < list.size(); i++){
                    if(tokens.get(0).equals(list.get(i))){
                        known = true;
                    }
                }
                if(!known){
                    //Send to error file
                    output.println("Error at line "+lineCounter+": Numbers given are not in Jog Wireless Database");
                    
                }
                else{
                    updateWeb(tokens);
                }
            }catch(SQLException e){
                System.out.println(e.getMessage());
                con.close();
            }
        }
    }
    private void updateCall(List<String> tokens, String known1, String known2)throws SQLException{
        String duration = calculateTimeDiff(tokens);
        try{
            if(!known1.equals("")){
                Statement s = con.createStatement();
                ResultSet r = s.executeQuery("select account.acctid from account, phone_number where phone_number.acctid = account.acctid and phone_num = '"+known1+"'");
                List<String> list = resultsToArrayList(r);
                s.executeQuery("insert into usage values(null,'"+list.get(0)+"')");
                s.executeQuery("update call set source_phone = '"+tokens.get(0)+"', dest_phone = '"+tokens.get(1)+"', start_time = '"+tokens.get(2)+"', end_time = '"+tokens.get(3)+"', duration = '"+duration+"' where useid = (select max(useid) from usage where acctid = '"+list.get(0)+"')");
                s.close();
            }
            if(!known2.equals("")){
                Statement s = con.createStatement();
                ResultSet r = s.executeQuery("select account.acctid from account, phone_number where phone_number.acctid = account.acctid and phone_num = '"+known2+"'");
                List<String> list = resultsToArrayList(r);
                s.executeQuery("insert into usage values(null,'"+list.get(0)+"')");
                s.executeQuery("update call set source_phone = '"+tokens.get(0)+"', dest_phone = '"+tokens.get(1)+"', start_time = '"+tokens.get(2)+"', end_time = '"+tokens.get(3)+"', duration = '"+duration+"' where useid = (select max(useid) from usage where acctid = '"+list.get(0)+"')");
                s.close();
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
            con.close();
        }
        //deleteExtra();
    }
    private void updateText(List<String> tokens, String known1, String known2)throws SQLException{
        try{
            if(!known1.equals("")){
                Statement s = con.createStatement();
                ResultSet r = s.executeQuery("select account.acctid from account, phone_number where phone_number.acctid = account.acctid and phone_num = '"+known1+"'");
                List<String> list = resultsToArrayList(r);
                s.executeQuery("insert into usage values(null,'"+list.get(0)+"')");
                s.executeQuery("update text set source_phone = '"+tokens.get(0)+"', dest_phone = '"+tokens.get(1)+"', t_time = '"+tokens.get(2)+"', t_size = '"+tokens.get(3)+"' where useid = (select max(useid) from usage where acctid = '"+list.get(0)+"')");
                s.close();
            }
            if(!known2.equals("")){
                Statement s = con.createStatement();
                ResultSet r = s.executeQuery("select account.acctid from account, phone_number where phone_number.acctid = account.acctid and phone_num = '"+known2+"'");
                List<String> list = resultsToArrayList(r);
                s.executeQuery("insert into usage values(null,'"+list.get(0)+"')");
                s.executeQuery("update text set source_phone = '"+tokens.get(0)+"', dest_phone = '"+tokens.get(1)+"', t_time = '"+tokens.get(2)+"', t_size = '"+tokens.get(3)+"' where useid = (select max(useid) from usage where acctid = '"+list.get(0)+"')");
                s.close();
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
            con.close();
        }
        //deleteExtra();
    }
    private void updateWeb(List<String> tokens)throws SQLException{
        try{
            Statement s = con.createStatement();
            ResultSet r = s.executeQuery("select account.acctid from account, phone_number where phone_number.acctid = account.acctid and phone_num = '"+tokens.get(0)+"'");
            List<String> list = resultsToArrayList(r);
            s.executeQuery("insert into usage values(null,'"+list.get(0)+"')");
            s.executeQuery("update web set access_type = '"+tokens.get(1)+"', web_bytes = '"+tokens.get(2)+"' where useid = (select max(useid) from usage where acctid = '"+list.get(0)+"')");
            s.close();
        }catch(SQLException e){
            System.out.println(e.getMessage());
            con.close();
        }
        //deleteExtra();
    }
    private void deleteExtra()throws SQLException{
        int delete;
        try{
            Statement s = con.createStatement();
            s.execute("delete from call where source_phone is null");
            s.execute("delete from text where source_phone is null");
            s.execute("delete from web where access_type is null");
            s.close();
        }catch(SQLException e){
            System.out.println(e.getMessage());
            con.close();
        }
    }
    private List<String> parseLine(String inputLine) {
        String token = "";
        List <String> tokens = new ArrayList<String>();
        while(ch != '$'){
            if(ch == ','){
                //Create token, move to next one.
                tokens.add(token);
                token = "";
                ch = nextChar();
            }
            //Skips all spaces
            if(ch == ' '){
                while(ch == ' '){
                    ch = nextChar();
                }
            }
            //Source number first
            //ch = nextChar();
            if(isDigit(ch)){
                while(isDigit(ch)){
                    token += ch;
                    ch = nextChar();
                }
                if(ch == '-'){
                    //Timestamp
                    token += ch;
                    token = handleTimestamp(token);
                }
            }
            if(isLetter(ch)){
                while(isLetter(ch)){
                    token += ch;
                    ch = nextChar();
                }
                if(!token.equals("upload") && !token.equals("download")){
                    violation = true;
                    break;
                }
            }
            if(ch == '$'){
                tokens.add(token);
                token = "";
                location = 0;
                ch = nextChar();
                for(int i = 0; i < tokens.size(); i++){
                }
                break;
            }
        }
        return tokens;
    }
    //Moves through each character of timestamp very carefully, making sure that format is exactly what it must be.
    private String handleTimestamp(String token){
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        numTimestamps++;
        ch = nextChar();
        //3 letter month abbreviation check
        //while(true){
            if(isLetter(ch)){
                while(isLetter(ch)){
                    token += ch;
                    ch = nextChar();
                }
                if(ch == '-'){
                    token += ch;
                    ch = nextChar();
                    //2 digit year check
                    if(isDigit(ch)){
                        while(isDigit(ch)){
                            token += ch;
                            ch = nextChar();
                        }
                        if(ch == ' '){
                            token += ch;
                            ch = nextChar();
                            //hours check
                            if(isDigit(ch)){
                                String hour = "";
                                while(isDigit(ch)){
                                    hour += ch;
                                    token += ch;
                                    ch = nextChar();
                                }
                                System.out.println("Hour length: "+hour.length());
                                if(hour.length() != 2){
                                    System.out.println("VIOLATION");
                                    violation = true;
                                }
                                //else{
                                    hours = Integer.parseInt(hour);
                                    if(hours > 12){
                                        violation = true;
                                    }
                                    if(ch == '.'){
                                        token += ch;
                                        ch = nextChar();
                                        //minutes check
                                        if(isDigit(ch)){
                                            String minute = "";
                                            while(isDigit(ch)){
                                                minute += ch;
                                                token += ch;
                                                ch = nextChar();
                                            }
                                            
                                            if(minute.length() != 2){
                                                violation = true;
                                            }
                                            //else{
                                                minutes = Integer.parseInt(minute);
                                                if(minutes > 59){
                                                    violation = true;
                                                }
                                                if(ch == '.'){
                                                    token += ch;
                                                    ch = nextChar();
                                                    //seconds check
                                                    if(isDigit(ch)){
                                                        String second = "";
                                                        while(isDigit(ch)){
                                                            second += ch;
                                                            token += ch;
                                                            ch = nextChar();
                                                        }
                                                        if(second.length() != 2){
                                                            violation = true;
                                                        }
                                                        //else{
                                                            seconds = Integer.parseInt(second);
                                                            if(seconds > 59){
                                                                violation = true;
                                                            }
                                                            if(ch == '.'){
                                                                token += ch;
                                                                ch = nextChar();
                                                                //milliseconds check
                                                                int milliCount = 0;
                                                                if(isDigit(ch)){
                                                                    while(isDigit(ch)){
                                                                        milliCount++;
                                                                        token += ch;
                                                                        ch = nextChar();
                                                                    }
                                                                    if(milliCount < 1 || milliCount > 6){
                                                                        violation = true;
                                                                    }
                                                                    //else{
                                                                        if(ch == ' '){
                                                                            token += ch;
                                                                            ch = nextChar();
                                                                            //AM or PM check
                                                                            if(isLetter(ch)){
                                                                                String ampm = "";
                                                                                while(isLetter(ch)){
                                                                                    ampm += ch;
                                                                                    token += ch;
                                                                                    ch = nextChar();
                                                                                }
                                                                                if(!ampm.equals("AM") && !ampm.equals("PM") && !ampm.equals("am") && !ampm.equals("pm")){
                                                                                    violation = true;
                                                                                }
                                                                                if(ampm.equals("PM") || ampm.equals("pm")){
                                                                                    hours += 12;
                                                                                }
                                                                            }
                                                                            else{
                                                                                violation = true;
                                                                                //break;
                                                                            }
                                                                        }
                                                                        else{
                                                                            violation = true;
                                                                            //break;
                                                                        }
                                                                    //}
                                                                }
                                                                else{
                                                                    violation = true;
                                                                    //break;
                                                                }
                                                            }
                                                            else{
                                                                violation = true;
                                                                //break;
                                                            }
                                                        //}
                                                    }
                                                    else{
                                                        violation = true;
                                                        //break;
                                                    }
                                                }
                                                else{
                                                    violation = true;
                                                    //break;
                                                }
                                            //}
                                        }
                                        else{
                                            violation = true;
                                            //break;
                                        }
                                    }
                                    else{
                                        violation = true;
                                        //break;
                                    }
                                //}
                            }
                            else{
                                violation = true;
                                //break;
                            }
                        }
                        else{
                            violation = true;
                            //break;
                        }
                    }
                    else{
                        violation = true;
                        //break;
                    }
                }
                else{
                    violation = true;
                    //break;
                }
            }
            else{
                violation = true;
                //break;
            }
        //}
        return token;
    }
    private String calculateTimeDiff(List<String> tokens){
        int duration = 0;
        int dur_hrs = 0;
        int dur_mins = 0;
        int dur_secs = 0;
        
        String start_hours = ""+tokens.get(2).charAt(10)+tokens.get(2).charAt(11);
        String start_minutes = ""+tokens.get(2).charAt(13)+tokens.get(2).charAt(14);
        String start_seconds = ""+tokens.get(2).charAt(16)+tokens.get(2).charAt(17);
        
        String end_hours = ""+tokens.get(3).charAt(10)+tokens.get(3).charAt(11);
        String end_minutes = ""+tokens.get(3).charAt(13)+tokens.get(3).charAt(14);
        String end_seconds = ""+tokens.get(3).charAt(16)+tokens.get(3).charAt(17);
        
        int start_hrs = Integer.parseInt(start_hours);
        int start_mins = Integer.parseInt(start_minutes);
        int start_secs = Integer.parseInt(start_seconds);
        
        int end_hrs = Integer.parseInt(end_hours);
        int end_mins = Integer.parseInt(end_minutes);
        int end_secs = Integer.parseInt(end_seconds);
        
        start_secs += (3600*start_hrs) + (60*start_mins);
        end_secs += (3600*end_hrs) + (60*end_mins);
        
        duration = end_secs - start_secs;
        while(duration >= 3600){
            duration -= 3600;
            dur_hrs++;
        }
        while(duration >= 60){
            duration -= 60;
            dur_mins++;
        }
        String time = ""+dur_hrs+":"+dur_mins+"."+duration;
        System.out.println("Time interval: "+time);
        return time;
    }
    
    private List<String> resultsToArrayList(ResultSet r) throws SQLException {
        List<String> list = new ArrayList<String>();
        String a = "";
        while(r.next()){
            a = r.getString(1);
            list.add(a);
        }
    return list;
    }
}