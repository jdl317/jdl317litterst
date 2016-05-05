/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdl317;

import java.util.Scanner;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author jefflitterst
 */
public class Purchase {
    Scanner scan = new Scanner(System.in);
    boolean exit;
    int choice;
    Connection con;
    
    
    public Purchase(Connection con)throws SQLException{
        exit = false;
        choice = -1;
        this.con = con;
        beginPurchase();
        
    }
    private void beginPurchase() throws SQLException{
        int store = 0;
        boolean validStore = false;
        boolean online = false;
        int available = 0;
        System.out.println("\nWelcome to our Purchasing Center!");
        System.out.println("\nPlease select one of our 8 store locations using the storeID to the right!");
        System.out.println("Or, for further convenience, enter 99999 to use our online resource\n");
        printStores();
        while(!validStore && !exit){
            if(scan.hasNextInt()){
                choice = scan.nextInt();
                if(choice == 0){
                    exit = true;
                    break;
                }
                else if(choice != 11111 && choice != 22222 && choice != 33333 && choice != 44444 && choice != 55555 && choice != 66666 && choice != 77777 && choice != 88888 && choice != 99999){
                    System.out.println("Please enter a valid storeid");
                }
                else if(choice == 99999){
                    validStore = true;
                    online = true;
                    store = choice;
                }
                else{
                    validStore = true;
                    store = choice;
                }
            }
            else{
                System.out.println("Please enter an integer");
            }
            scan.nextLine();
        }
        if(online){
            System.out.println("Thank you for choosing to use our website!");
        }
        else if(exit){
            return;
        }
        else{
            System.out.println("Thank you for choosing our store located at:");
            System.out.println("*************************************************************");
            try{
                Statement s = con.createStatement();
                ResultSet result = s.executeQuery("select addr_street as street, addr_city as city from physical_store where storeID = "+store);
                printResults(result);
                s.close();
                System.out.println("*************************************************************");
            }catch(Exception e){
               System.out.println(e.getMessage());
               con.close();
            }
        }
        
        System.out.println("\nPlease select one of the options below, or 0 to exit");
        System.out.println("\n\t1. Purchase Phone\n\t2. Set up Account\n\t3. Edit Account\n\t4. Restock Store");
        boolean goodchoice = false;
        while(!goodchoice && !exit){
            
            if(scan.hasNextInt()){
                choice = scan.nextInt();
                switch(choice){
                case 0: goodchoice = true; exit = true;
                break;
                case 1: goodchoice = true;
                buyPhone(store);
                break;
                case 2: goodchoice = true;
                createAccount();
                break;
                case 3: goodchoice = true;
                editAccount();
                break;
                case 4: goodchoice = true;
                restockStore(store);
                break;
                default: System.out.println("Please enter either 1, 2, 3, 4, or 0 to exit");
                }
            }
            else{
                System.out.println("Please enter an integer");
            }
            if(exit){
                break;
            }
            else{
                scan.nextLine();
            }
        }
    }

    private void buyPhone(int store) throws SQLException {
        List<Integer> quantity = new ArrayList<Integer>();
        List<Integer> custIDs = new ArrayList<Integer>();
        List<Double> price = new ArrayList<Double>();
        int numPhones = 0;
        int difference = 0;
        int phoneChoice = -1;
        boolean valid = false;
        int custID = -1;
        String first = "";
        String last = "";
        String street = "";
        String city = "";
        String state = "";
        ResultSet phoneInfo = null;
        while(!exit){
            valid = false;
            try{
                System.out.println("Welcome to the Phone Purchasing Area!");
                System.out.println("\nHere is our complete list of phones for you to purchase");
                System.out.println("*********************************************************************************************");
                Statement s1 = con.createStatement();
                ResultSet result = s1.executeQuery("select distinct(p_mfg) as make, p_model as model, price, quantity, phoneid\n" +
                                                    "from phone, inventory\n" +
                                                    "where inventory.ITEMID = phone.PHONEID and storeid = '"+store+"'");
                printResults(result);
                System.out.println("*********************************************************************************************");
                System.out.println("\n Please select the phone of your choice, using the phoneID, or 0 to exit");

                //Phoneids range from 201 to 207
                while(!valid && !exit){
                    if(scan.hasNextInt()){
                        phoneChoice = scan.nextInt();
                        if(phoneChoice < 201 || phoneChoice > 207){
                            if(phoneChoice == 0){
                                exit = true;
                                break;
                            }
                            else{
                                System.out.println("Please choose an integer representing your phone choice, ranging from 1 to 7");
                            }
                        }
                        else{
                            //Valid phoneChoice
                            System.out.println("You have selected:\n");
                            System.out.println("*********************************************************************************************");
                            s1 = con.createStatement();
                            phoneInfo = s1.executeQuery("select distinct(p_mfg) as make, p_model as model\n" +
                                                    "from phone\n" +
                                                    "where phoneid = '"+phoneChoice+"'");
                            printResults(phoneInfo);
                            System.out.println("*********************************************************************************************");
                            System.out.println("Is this correct (y/n)? ");
                            String agree = "";
                            while(!valid && !exit){
                                if(scan.hasNext()){
                                    agree = scan.next();
                                    if(agree.equals("y")){
                                        valid = true;
                                        break;
                                    }
                                    else if(agree.equals("n")){
                                        System.out.println("Please select a new phone");
                                        break;
                                    }
                                    else if(agree.equals("0")){
                                        exit = true;
                                        return;
                                    }
                                    else{
                                        System.out.println("Please enter a valid option (either y or n)");
                                    }
                                }
                            }
                        }
                    }
                    else{
                        System.out.println("Please select an INTEGER.  No other input will be accepted. Trust me ;)");
                        scan.next();
                    }
                }

                s1 = con.createStatement();
                result = s1.executeQuery("select quantity from inventory where itemid = '"+phoneChoice+"' and storeid = '"+store+"'");
                quantity = resultsToArrayList(result);
                result = s1.executeQuery("select price from inventory where itemid = '"+phoneChoice+"' and storeid = '"+store+"'");
                price = resultsToArrayListDouble(result);
                System.out.println("Great! How many would you like to purchase?");
                valid = false;
                while(!valid){
                    if(scan.hasNextInt()){
                        numPhones = scan.nextInt();
                        if(numPhones > quantity.get(0)){
                            System.out.println("Sorry, you've selected more than we have in stock.  Please enter a number less than the quantity shown.");
                        }
                        else if(numPhones < 1){
                            System.out.println("Please select a positive number.");
                        }
                        else{
                            valid = true;
                            difference = quantity.get(0) - numPhones;
                            s1 = con.createStatement();
                            System.out.println("Updating inventory...");
                            s1.executeQuery("update inventory set quantity = '"+difference+"' where itemid = '"+phoneChoice+"' and storeid = '"+store+"'");
                            //Essentially acts as a trigger, resetting online inventory to 1000 so it never runs out.
                            s1.executeQuery("update inventory set quantity = '1000' where itemid = '"+phoneChoice+"' and storeid = '"+store+"'");
                            s1.close();
                            System.out.println("Inventory updated!");
                        }
                    }
                    else{
                        System.out.println("Please enter an integer");
                        scan.nextLine();
                    }
                }
            }catch (Exception e){
                System.out.println(e.getMessage());
                con.close();
            }
            valid = false;
            System.out.println("Finally, we'll need you to give us an ID, name, and address.\n");
            System.out.println("Please enter a unique code of up to 5 digits.  This will be your customer ID");
            while(!valid){
                if(scan.hasNextInt()){
                    custID = scan.nextInt();
                    if(custID < 0 || custID > 99999){
                        System.out.println("Please enter a valid code");
                    }
                    else{
                        try{
                            Statement s = con.createStatement();
                            ResultSet r = s.executeQuery("select custid from customer");
                            custIDs = resultsToArrayList(r);
                            s.close();
                            System.out.println("Confirming valid ID...");
                            int bad = 0;
                            for(int i = 0; i < custIDs.size(); i++){
                                if(custID == custIDs.get(i)){
                                    System.out.println("Sorry, this ID already exists.  Please try again.");
                                    bad++;
                                    break;
                                }
                            }
                            if(bad == 0){
                                valid = true;
                            }
                        }catch(Exception e){
                            System.out.println(e.getMessage());
                            con.close();
                        }
                    }
                }
                else{
                    System.out.println("Please enter only numbers (5 digits or less) when creating your account ID");
                }
                scan.nextLine();
            }
            System.out.println("\nFirst Name: ");
            valid = false;
            while(!valid){
                if(scan.hasNext()){
                    first = scan.next();
                    if(first.length() > 20){
                        System.out.println("This name is too long. Please try again.");
                    }
                    else{
                        valid = true;
                    }
                }
                scan.nextLine();
            }
            System.out.println("\nLast Name: ");
            valid = false;
            while(!valid){
                if(scan.hasNext()){
                    last = scan.next();
                    if(last.length() > 20){
                        System.out.println("This name is too long. Please try again.");
                    }
                    else{
                        valid = true;
                    }
                }
                scan.nextLine();
            }
            System.out.println("\nStreet: ");
            valid = false;
            while(!valid){
                if(scan.hasNextLine()){
                    street = scan.nextLine();
                    if(street.length() > 40){
                        System.out.println("This name is too long. Please try again.");
                    }
                    else{
                        valid = true;
                        break;
                    }
                }
                scan.nextLine();
            }
            System.out.println("\nCity: ");
            valid = false;
            while(!valid){
                if(scan.hasNextLine()){
                    city = scan.nextLine();
                    if(city.length() > 40){
                        System.out.println("This name is too long. Please try again.");
                    }
                    else{
                        valid = true;
                        break;
                    }
                }
                scan.nextLine();
            }
            System.out.println("\nState: ");
            valid = false;
            while(!valid){
                if(scan.hasNext()){
                    last = scan.next();
                    if(last.length() > 2){
                        System.out.println("Please use the 2 letter state abbreviation (Ex: NH, FL, CA)");
                    }
                    else{
                        valid = true;
                    }
                }
                scan.nextLine();
            }
            addCustomer(custID, street, city, state);
            addPersonalCustomer(custID, last, first);
            addSale(custID, (numPhones * price.get(0)));
            System.out.println("Thank you for your purchase! Here is your sale information\n");
            System.out.println("*************************************************************");
            try{
                Statement s1 = con.createStatement();
                phoneInfo = s1.executeQuery("select distinct(p_mfg) as make, p_model as model\n" +
                                        "from phone\n" +
                                        "where phoneid = '"+phoneChoice+"'");
                printResults(phoneInfo);
                s1.close();
            }catch(SQLException e){
                System.out.println(e.getMessage());
            }
            System.out.println("*************************************************************");
            System.out.println("Number of phones purchased: "+numPhones);
            System.out.println("Price per phone: "+price.get(0));
            System.out.println("TotalCost: "+(numPhones * price.get(0)));
            System.out.println("*************************************************************");
            System.out.println("Would you like to buy another phone (y/n)? ");
            boolean answer = false;
            String another = null;
            while(!answer){
                if(scan.hasNextLine()){
                    another = scan.nextLine();
                    if(another.equals("y")){
                        answer = true;
                    }
                    else if(another.equals("n")){
                        answer = true;
                        exit = true;
                    }
                    else{
                        System.out.println("Please enter either 'y' or 'n'. ");
                    }
                }
            }

        }
        System.out.println("Goodbye");
        return;

    }
    private void editAccount() throws SQLException{
         
        List<String> phone_numbers = new ArrayList<String>();
        int phonesInAcct = 0;
        int acctID = 0;
        String plan;
        List<Integer> acctList = null;
        while(!exit){
            boolean editSchedule = false;
            boolean editMethod = false;
            boolean editNumPhones = false;
            boolean valid = false;
            System.out.println("To edit an Account, you must first enter the correct ID: ");
            while(!valid){
                if(scan.hasNextInt()){
                    acctID = scan.nextInt();
                    if(acctID < 0 || acctID > 99999){
                        System.out.println("Please enter a valid ID (5 digits or less)");
                    }
                    else{
                        try{
                            Statement s = con.createStatement();
                            ResultSet r = s.executeQuery("select acctid from account");
                            acctList = resultsToArrayList(r);
                            s.close();
                            System.out.println("Confirming valid ID...");
                            boolean match = false;
                            for(int i = 0; i < acctList.size(); i++){
                                if(acctID == acctList.get(i)){
                                    match = true;
                                    break;
                                }
                            }
                            if(match){
                                System.out.println("Valid ID");
                                valid = true;
                                break;
                            }
                            else{
                                System.out.println("Account ID could not be found in our records. Please try again");
                            }
                        }catch(Exception e){
                            System.out.println(e.getMessage());
                            con.close();
                        }
                    }
                }
                else{
                    System.out.println("Please enter only numbers (5 digits or less) when creating your account ID");
                scan.next();
                }
                
            }
            printBillingPlan(acctID);
            printPhoneNums(acctID);
            System.out.println("Getting numPhones...");
            phonesInAcct = getNumCount(acctID);
            System.out.println("Please select the option you'd like to edit.");
            System.out.println("\n1. Payment Schedule (monthly vs. annual)\n2. Payment Method (byte vs. minute vs. flat rate)\n3. Number of phones in Account (for family and business plans only)");
            valid = false;
            int edit = 0;
            while(!valid){
                if(scan.hasNextInt()){
                    edit = scan.nextInt();
                    if(edit < 1 || edit > 3){
                        System.out.println("Please enter a valid option.");
                    }
                    else{
                        switch(edit){
                            case 1: valid = true; editSchedule = true; break;
                            case 2: valid = true; editMethod = true; break;
                            case 3: valid = true; editNumPhones = true; break;
                        }
                    }
                }
                else{
                    System.out.println("Please enter an integer");
                    scan.next();
                }
            }
            if(editNumPhones){
                if(phonesInAcct == 1){
                    //Individual account
                    System.out.println("You may not edit the number of phones in your account.");
                }
                else if(phonesInAcct > 1 && phonesInAcct < 6){
                    //Family plan
                    int remaining = 5 - phonesInAcct;
                    System.out.println("You currently have "+phonesInAcct+" phone numbers in your account.");
                    System.out.println("You may add/remove up to "+remaining+"/"+(phonesInAcct-1)+" numbers. You cannot remove your primary number. How many would you like to add/remove?");
                    System.out.println("Enter positive integers for adding numbers, negative integers for removing numbers.");
                    valid = false;
                    int request = 0;
                    while(!valid){
                        if(scan.hasNextInt()){
                            request = scan.nextInt();
                            if(request > remaining){
                                System.out.println("You may only add up to "+remaining+" numbers to your account.");
                            }
                            else if(request == 0){
                                System.out.println("You have selected to add/remove 0 numbers. No problem!");
                                valid = true;
                            }
                            else if(request > 0 && request <= remaining){
                                //add phones to account
                                System.out.println("Adding "+request+" numbers to your account...");
                                phone_numbers = createNums(request, phone_numbers);
                                addPhoneNums(acctID, request, phone_numbers);
                                valid = true;
                            }
                            else if((phonesInAcct - 1) + request < 1){
                                System.out.println("You may only remove up to "+(phonesInAcct - 1)+" numbers from your account.");
                            }
                            else{
                                System.out.println("Removing "+ (-1*request)+" numbers from your account...");
                                removePhoneNums(acctID, (-1*request));
                                valid = true;
                            }
                        }
                        else{
                            System.out.println("Please enter an integer");
                        }
                    }
                }
                else if(phonesInAcct > 5){
                    //business plan
                    int remaining = 100 - phonesInAcct;
                    System.out.println("You currently have "+phonesInAcct+" phone numbers in your account.");
                    System.out.println("You may add/remove up to "+remaining+"/"+(phonesInAcct-1)+" numbers, respectively. You cannot remove your primary number. How many would you like to add/remove?");
                    System.out.println("Enter positive integers for adding numbers, negative integers for removing numbers.");
                    valid = false;
                    int request = 0;
                    while(!valid){
                        if(scan.hasNextInt()){
                            request = scan.nextInt();
                            if(request > remaining){
                                System.out.println("You may only add up to "+remaining+" numbers to your account.");
                            }
                            else if(request == 0){
                                System.out.println("You have selected to add/remove 0 numbers. No problem!");
                                valid = true;
                            }
                            else if(request > 0 && request <= remaining){
                                //add phones to account
                                System.out.println("Adding "+request+" numbers to your account...");
                                phone_numbers = createNums(request, phone_numbers);
                                addPhoneNums(acctID, request, phone_numbers);
                                valid = true;
                            }
                            else if((phonesInAcct - 1) + request < 1){
                                System.out.println("You may only remove up to "+(phonesInAcct - 1)+" numbers from your account.");
                            }
                            else{
                                System.out.println("Removing "+ (-1*request)+" numbers from your account...");
                                removePhoneNums(acctID, (-1*request));
                                valid = true;
                            }
                        }
                        else{
                            System.out.println("Please enter an integer");
                        }
                    }
                }
            }
            else if(editSchedule){
                System.out.println("Your current payment schedule is as follows:\n");
                printPlanSchedule(acctID);
                System.out.println("Please specify the payment schedule you'd like (monthly or annual):");
                valid = false;
                while(!valid){
                    String mory = "";
                    if(scan.hasNext()){
                        mory = scan.next();
                        if(mory.equals("monthly") || mory.equals("annual")){
                            editBillingPlanSchedule(acctID, mory);
                            valid = true;
                        }
                        else{
                            System.out.println("Please enter a valid payment schedule (monthly or annual).");
                            scan.next();
                        }
                    }
                }
            }
            else if(editMethod){
                System.out.println("Your current payment method is as follows:\n");
                printPayMethod(acctID);
                System.out.println("Please specify the payment method you'd like (minute, text, or flat rate:");
                valid = false;
                while(!valid){
                    String mory = "";
                    if(scan.hasNext()){
                        mory = scan.next();
                        if(mory.equals("minute") || mory.equals("text") || mory.equals("flat")){
                            mory += " rate";
                            editBillingPlanMethod(acctID, mory);
                            valid = true;
                        }
                        else{
                            System.out.println("Please enter a valid payment method (minute, text, or flat rate).");
                            scan.next();
                        }
                    }
                }
            }
            System.out.println("Would you like to edit another account (y/n)? ");
            boolean answer = false;
            String another = null;
            while(!answer){
                if(scan.hasNextLine()){
                    another = scan.nextLine();
                    if(another.equals("y")){
                        answer = true;
                    }
                    else if(another.equals("n")){
                        answer = true;
                        exit = true;
                    }
                    else{
                        System.out.println("Please enter either 'y' or 'n'. ");
                    }
                }
            }
            
        }
        System.out.println("Goodbye");
        return;
    }
    private void createAccount() throws SQLException{
        String first = null;
        String last = "";
        String street = null;
        String city = null;
        String state = null;
        int acctID = 0;
        int plan = 0;
        int maxPhones = 0;
        int numPhones = 0;
        int moy = 0;
        int payType = 0;
        List<Integer> list = null;
        List<String> phone_numbers = new ArrayList<String>();
        choice = -1;
        while(!exit){
            boolean valid = false;
            System.out.println("Please select the type of account you'd like to set up:\n\t1. Individual Plan (1 phone)\n\t2. Family Plan (up to 5 phones)\n\t3. Business Plan (up to 100 phones)\n\n\t(Select 0 to exit)");
            while(!valid && !exit){
                if(scan.hasNextInt()){
                    choice = scan.nextInt();
                    switch(choice){
                        case 0: exit = true;
                        break;
                        case 1: valid = true; plan = choice; numPhones = 1; maxPhones = 1; break;
                        case 2: valid = true; plan = choice; maxPhones = 5; break;
                        case 3: valid = true; plan = choice; maxPhones = 100; break;
                        default: System.out.println("Please enter a valid integer"); 
                        break;
                    }
                }
                else{
                    System.out.println("Please enter an integer");
                }
                scan.nextLine(); 
            }
            if(exit){
               break;
            }
            valid = false;
            System.out.println("Great! Now please enter a unique code of up to 5 digits.  This will be your account ID");
            while(!valid){
                if(scan.hasNextInt()){
                    acctID = scan.nextInt();
                    if(acctID < 0 || acctID > 99999){
                        System.out.println("Please enter a valid code");
                    }
                    else{
                        try{
                            Statement s = con.createStatement();
                            ResultSet r = s.executeQuery("select acctid from account");
                            list = resultsToArrayList(r);
                            s.close();
                            System.out.println("Confirming valid ID...");
                            int bad = 0;
                            for(int i = 0; i < list.size(); i++){
                                if(acctID == list.get(i)){
                                    System.out.println("Sorry, this account ID already exists.  Please try again.");
                                    bad++;
                                    break;
                                }
                            }
                            if(bad == 0){
                                valid = true;
                            }
                        }catch(Exception e){
                            System.out.println(e.getMessage());
                            con.close();
                        }
                    }
                }
                else{
                    System.out.println("Please enter only numbers (5 digits or less) when creating your account ID");
                }
                scan.nextLine();
            }
            //Number of phones to be created given the plan
            if(plan == 2 || plan == 3){
                System.out.println("How many phones would you like to set up for your account?\n\n(family plan max is 5, business plan max is 100)\n");
                valid = false;
                while(!valid){
                    if(scan.hasNextInt()){
                        numPhones = scan.nextInt();
                        if(numPhones < 1 || numPhones > maxPhones){
                            System.out.println("You may not exceed the maximum number of phones for your given plan.  Please enter a number within your plan maximum.");
                        }
                        else{
                            valid = true;
                        }
                    }
                    else{
                        System.out.println("Please enter an INTEGER specifying how many phones you'd like to set up");
                    }
                    scan.nextLine();
                }
            }

            //Decide payment method
            System.out.println("Would you like to pay a monthly rate or an annual rate?\n\n\t1. Monthly\n\t2. Annual");
            valid = false;
            while(!valid && !exit){
                if(scan.hasNextInt()){
                    moy = scan.nextInt();
                    if(moy < 1 || moy > 2){
                        if(moy == 0){
                            exit = true;
                            break;
                        }
                        else{
                            System.out.println("Please enter a valid integer");
                        }
                    }
                    else{
                        valid = true;
                    }
                }
                else{
                    System.out.println("Please enter an integer");
                }
                if(exit){
                    break;
                }
                scan.nextLine();
            }
            //Monthly plan
            if(moy == 1){
                //Non-business
                if(plan != 3){
                    System.out.println("How would you like to pay for your plan?\n\n\t1. Pay by call minute ($1 per minute)\n\t2. Pay by text ($0.10 per byte)\n\t3. Flat rate ($50 per phone)");
                    valid = false;
                    while(!valid){
                        if(scan.hasNextInt()){
                            payType = scan.nextInt();
                            if(payType < 1 || payType > 3){
                                System.out.println("Please enter a valid integer");
                            }
                            else{
                                valid = true;
                            }
                        }
                        else{
                            System.out.println("Please enter an integer");
                        }
                        scan.nextLine();
                    }
                    System.out.println("Finally, please enter your name and address.");
                    System.out.println("\nFirst Name: ");
                    valid = false;
                    while(!valid){
                        if(scan.hasNext()){
                            first = scan.next();
                            if(first.length() > 20){
                                System.out.println("That seems to be an unreasonably long name.  Could you please either give us a shorter name, or a nickname?");
                            }
                            else{
                                valid = true;
                            }
                        }
                        scan.nextLine();
                    }
                    System.out.println("\nLast Name: ");
                    valid = false;
                    while(!valid){
                        if(scan.hasNext()){
                            last = scan.next();
                            if(last.length() > 20){
                                System.out.println("That seems to be an unreasonably long name.  Could you please either give us a shorter name, or a nickname?");
                            }
                            else{
                                valid = true;
                            }
                        }
                        scan.nextLine();
                    }
                    System.out.println("\nStreet Address: ");
                    valid = false;
                    while(!valid){
                        if(scan.hasNextLine()){
                            street = scan.nextLine();
                            if(street.length() > 40){
                                System.out.println("That seems to be an unreasonably long address.  Please exclude unnecessary information in street address.");
                            }
                            else{
                                valid = true;
                                break;
                            }
                        }
                        scan.nextLine();
                    }
                    System.out.println("\nCity: ");
                    valid = false;
                    while(!valid){
                        if(scan.hasNextLine()){
                            city = scan.nextLine();
                            if(city.length() > 40){
                                System.out.println("That seems to be an unreasonably long city name.  Please abbreviate it.");
                            }
                            else{
                                valid = true;
                                break;
                            }
                        }
                        scan.nextLine();
                    }
                    System.out.println("\nState (Abbreviation): ");
                    valid = false;
                    while(!valid){
                        if(scan.hasNext()){
                            state = scan.next();
                            if(state.length() > 2){
                                System.out.println("Please only enter the state abbreviation (i.e. NH, CO, WY)");
                            }
                            else{
                                valid = true;
                            }
                        }
                        scan.nextLine();
                    }
                    addCustomer(acctID, street, city, state);
                    addPersonalCustomer(acctID, last, first);
                    phone_numbers = createNums(numPhones, phone_numbers);
                    addAccount(acctID, phone_numbers.get(0), plan);
                    addPhoneNums(acctID, numPhones, phone_numbers);
                    addBillingPlan(acctID, payType, "monthly");

                }
                //Monthly business plan
                else{
                    System.out.println("Entering Business Area");
                    System.out.println("How would you like to pay for your plan?\n\n\t1. Pay by call minute ($1 per minute)\n\t2. Pay by text ($0.10 per byte)\n\t3. Flat rate ($50 per phone)");
                    valid = false;
                    while(!valid){
                        if(scan.hasNextInt()){
                            payType = scan.nextInt();
                            if(payType < 1 || payType > 3){
                                System.out.println("Please enter a valid integer");
                            }
                            else{
                                valid = true;
                            }
                        }
                        else{
                            System.out.println("Please enter an integer");
                        }
                        scan.nextLine();
                    }
                    System.out.println("Finally, please enter your company name and address.");
                    System.out.println("\nCompany: ");
                    valid = false;
                    while(!valid){
                        if(scan.hasNext()){
                            first = scan.next();
                            if(first.length() > 40){
                                System.out.println("That seems to be an unreasonably long name for a company.  Could you please either give us a shorter name, or a nickname?");
                            }
                            else{
                                valid = true;
                            }
                        }
                        scan.nextLine();
                    }
                    System.out.println("\nStreet Address: ");
                    valid = false;
                    while(!valid){
                        if(scan.hasNextLine()){
                            street = scan.nextLine();
                            if(street.length() > 40){
                                System.out.println("That seems to be an unreasonably long address.  Please exclude unnecessary information in street address.");
                            }
                            else{
                                valid = true;
                                break;
                            }
                        }
                        scan.nextLine();
                    }
                    System.out.println("\nCity: ");
                    valid = false;
                    while(!valid){
                        if(scan.hasNextLine()){
                            city = scan.nextLine();
                            if(city.length() > 40){
                                System.out.println("That seems to be an unreasonably long city name.  Please abbreviate it.");
                            }
                            else{
                                valid = true;
                                break;
                            }
                        }
                        scan.nextLine();
                    }
                    System.out.println("\nState (Abbreviation): ");
                    valid = false;
                    while(!valid){
                        if(scan.hasNext()){
                            state = scan.next();
                            if(state.length() > 2){
                                System.out.println("Please only enter the state abbreviation (i.e. NH, CO, WY)");
                            }
                            else{
                                valid = true;
                            }
                        }
                        scan.nextLine();
                    }
                    addCustomer(acctID, street, city, state);
                    System.out.println("Adding bus cust");
                    addBusinessCustomer(acctID, first);
                    phone_numbers = createNums(numPhones, phone_numbers);
                    addAccount(acctID, phone_numbers.get(0), plan);
                    addPhoneNums(acctID, numPhones, phone_numbers);
                    addBillingPlan(acctID, payType, "monthly");

                }

            }
            else{
                //Annual non-business
                if(plan != 3){
                    System.out.println("How would you like to pay for your plan?\n\n\t1. Pay by call minute ($1 per minute)\n\t2. Pay by text ($0.10 per byte)\n\t3. Flat rate ($499.99 per phone)");
                    valid = false;
                    while(!valid){
                        if(scan.hasNextInt()){
                            payType = scan.nextInt();
                            if(payType < 1 || payType > 3){
                                System.out.println("Please enter a valid integer");
                            }
                            else{
                                valid = true;
                            }
                        }
                        else{
                            System.out.println("Please enter an integer");
                        }
                        scan.nextLine();
                    }
                    System.out.println("Finally, please enter your name and address.");
                    System.out.println("\nFirst Name: ");
                    valid = false;
                    while(!valid){
                        if(scan.hasNext()){
                            first = scan.next();
                            if(first.length() > 40){
                                System.out.println("That seems to be an unreasonably long name.  Could you please either give us a shorter name, or a nickname?");
                            }
                            else{
                                valid = true;
                            }
                        }
                        scan.nextLine();
                    }
                    System.out.println("\nLast Name: ");
                    valid = false;
                    while(!valid){
                        if(scan.hasNext()){
                            last = scan.next();
                            if(last.length() > 40){
                                System.out.println("That seems to be an unreasonably long name.  Could you please either give us a shorter name, or a nickname?");
                            }
                            else{
                                valid = true;
                            }
                        }
                        scan.nextLine();
                    }
                    System.out.println("\nStreet Address: ");
                    valid = false;
                    while(!valid){
                        if(scan.hasNextLine()){
                            street = scan.nextLine();
                            if(street.length() > 40){
                                System.out.println("That seems to be an unreasonably long address.  Please exclude unnecessary information in street address.");
                            }
                            else{
                                valid = true;
                                break;
                            }
                        }
                        scan.nextLine();
                    }
                    System.out.println("\nCity: ");
                    valid = false;
                    while(!valid){
                        if(scan.hasNextLine()){
                            city = scan.nextLine();
                            if(city.length() > 40){
                                System.out.println("That seems to be an unreasonably long city name.  Please abbreviate it.");
                            }
                            else{
                                valid = true;
                                break;
                            }
                        }
                        scan.nextLine();
                    }
                    System.out.println("\nState (Abbreviation): ");
                    valid = false;
                    while(!valid){
                        if(scan.hasNext()){
                            state = scan.next();
                            if(state.length() > 2){
                                System.out.println("Please only enter the state abbreviation (i.e. NH, CO, WY)");
                            }
                            else{
                                valid = true;
                            }
                        }
                        scan.nextLine();
                    }
                    addCustomer(acctID, street, city, state);
                    addPersonalCustomer(acctID, last, first);
                    phone_numbers = createNums(numPhones, phone_numbers);
                    addAccount(acctID, phone_numbers.get(0), plan);
                    addPhoneNums(acctID, numPhones, phone_numbers);
                    addBillingPlan(acctID, payType, "monthly");

                }
                //annual business plan
                else{
                    System.out.println("Entering Business Area");
                    System.out.println("How would you like to pay for your plan?\n\n\t1. Pay by call minute ($1 per minute)\n\t2. Pay by text ($0.10 per byte)\n\t3. Flat rate ($499.99 per phone)");
                    valid = false;
                    while(!valid){
                        if(scan.hasNextInt()){
                            payType = scan.nextInt();
                            if(payType < 1 || payType > 3){
                                System.out.println("Please enter a valid integer");
                            }
                            else{
                                valid = true;
                            }
                        }
                        else{
                            System.out.println("Please enter an integer");
                        }
                        scan.nextLine();
                    }
                    System.out.println("Finally, please enter your company name and address.");
                    System.out.println("\nCompany: ");
                    valid = false;
                    while(!valid){
                        if(scan.hasNext()){
                            first = scan.next();
                            if(first.length() > 40){
                                System.out.println("That seems to be an unreasonably long name for a company.  Could you please either give us a shorter name, or a nickname?");
                            }
                            else{
                                valid = true;
                            }
                        }
                        scan.nextLine();
                    }
                    System.out.println("\nStreet Address: ");
                    valid = false;
                    while(!valid){
                        if(scan.hasNextLine()){
                            street = scan.nextLine();
                            if(street.length() > 40){
                                System.out.println("That seems to be an unreasonably long address.  Please exclude unnecessary information in street address.");
                            }
                            else{
                                valid = true;
                                break;
                            }
                        }
                        scan.nextLine();
                    }
                    System.out.println("\nCity: ");
                    valid = false;
                    while(!valid){
                        if(scan.hasNextLine()){
                            city = scan.nextLine();
                            if(city.length() > 40){
                                System.out.println("That seems to be an unreasonably long city name.  Please abbreviate it.");
                            }
                            else{
                                valid = true;
                                break;
                            }
                        }
                        scan.nextLine();
                    }
                    System.out.println("\nState (Abbreviation): ");
                    valid = false;
                    while(!valid){
                        if(scan.hasNext()){
                            state = scan.next();
                            if(state.length() > 2){
                                System.out.println("Please only enter the state abbreviation (i.e. NH, CO, WY)");
                            }
                            else{
                                valid = true;
                            }
                        }
                        scan.nextLine();
                    }
                    addCustomer(acctID, street, city, state);
                    System.out.println("Adding bus cust");
                    addBusinessCustomer(acctID, first);
                    phone_numbers = createNums(numPhones, phone_numbers);
                    addAccount(acctID, phone_numbers.get(0), plan);
                    addPhoneNums(acctID, numPhones, phone_numbers);
                    addBillingPlan(acctID, payType, "annual");

                }

            }
            System.out.println("Excellent! Here is all of your account information");
            printPlanInfo(first, last, street, city, state, acctID, plan, phone_numbers, moy, payType);
            System.out.println("Would you like to create another account (y/n)? ");
            boolean answer = false;
            String another = null;
            while(!answer){
                if(scan.hasNextLine()){
                    another = scan.nextLine();
                    if(another.equals("y")){
                        answer = true;
                    }
                    else if(another.equals("n")){
                        answer = true;
                        exit = true;
                    }
                    else{
                        System.out.println("Please enter either 'y' or 'n'. ");
                    }
                }
            }

        }
        System.out.println("Goodbye");
        return;
    }
    private void restockStore(int store)throws SQLException{
        System.out.println("Here is the inventory for your current store:");
        printStoreInv(store);
        System.out.println("Would you like to choose a different store (y/n)? ");
        boolean valid = false;
        while(!exit){
            while(!valid){
                String answer = "";
                if(scan.hasNext()){
                    answer = scan.next();
                    if(answer.equals("y")){
                        valid = true;
                        System.out.println("Great, here is our list of stores.");
                        printStores();
                        System.out.println("\nPlease select a store using the storeid");
                        boolean validStore = false;
                        while(!validStore && !exit){
                            if(scan.hasNextInt()){
                                store = scan.nextInt();
                                if(store == 0){
                                    exit = true;
                                    break;
                                }
                                else if(store != 11111 && store != 22222 && store != 33333 && store != 44444 && store != 55555 && store != 66666 && store != 77777 && store != 88888){
                                    System.out.println("Please enter a valid storeid");
                                }
                                else{
                                    validStore = true;
                                    break;
                                }
                            }
                            else{
                                System.out.println("Please enter an integer");
                            }
                            scan.nextLine();
                        }
                    }
                    else if(answer.equals("n")){
                        valid = true;
                        break;
                    }
                    else{
                        System.out.println("Please enter either 'y' or 'n'");
                    }
                }
            }
            System.out.println("Here is the inventory of your selected store");
            printStoreInv(store);
            System.out.println("\nPlease select the item you'd like to restock using itemid.");
            valid = false;
            int phoneChoice = -1;
            while(!valid && !exit){
                if(scan.hasNextInt()){
                    phoneChoice = scan.nextInt();
                    if(phoneChoice < 201 || phoneChoice > 207){
                        if(phoneChoice == 0){
                            exit = true;
                            break;
                        }
                        else{
                            System.out.println("Please choose an integer representing your phone choice, ranging from 1 to 7");
                        }
                    }
                    else{
                        //Valid phoneChoice
                        System.out.println("You have selected:\n");
                        printPhone(phoneChoice);
                        System.out.println("Is this correct (y/n)? ");
                        String agree = "";
                        while(!valid && !exit){
                            if(scan.hasNext()){
                                agree = scan.next();
                                if(agree.equals("y")){
                                    valid = true;
                                    break;
                                }
                                else if(agree.equals("n")){
                                    System.out.println("Please select a new phone");
                                    break;
                                }
                                else if(agree.equals("0")){
                                    exit = true;
                                    return;
                                }
                                else{
                                    System.out.println("Please enter a valid option (either y or n)");
                                }
                            }
                        }
                    }
                }
                else{
                    System.out.println("Please select an INTEGER.  No other input will be accepted. Trust me ;)");
                    scan.next();
                }
            }
            int quantity = getPhoneInv(store, phoneChoice);
            System.out.println("You may not exceed 100 phones when restocking.\nThere are currently "+quantity+" phones in stock.\nYou may add up to "+(100 - quantity)+" phones to the store inventory.");
            System.out.println("\nPlease enter the number of phones you'd like to add to your inventory.");
            valid = false;
            int num = 0;
            while(!valid){
                if(scan.hasNextInt()){
                    num = scan.nextInt();
                    if(num + quantity > 100){
                        System.out.println("You may not exceed 100 phones when restocking. Please try again.");
                    }
                    else if(num < 1){
                        System.out.println("Please enter a positive number when restocking.");
                    }
                    else{
                        valid = true;
                        System.out.println("Adding "+num+" phones to store inventory...");
                        addPhonesToInv(store, phoneChoice, num);
                    }
                }
            }
            System.out.println("Perfect! Here is the updated inventory.");
            printStoreInv(store);
            System.out.println("Would you like to restock more items (y/n)?");
            boolean answer = false;
            String another = "";
            while(!answer){
                if(scan.hasNextLine()){
                    another = scan.nextLine();
                    if(another.equals("y")){
                        answer = true;
                    }
                    else if(another.equals("n")){
                        answer = true;
                        exit = true;
                    }
                    else{
                        System.out.println("Please enter either 'y' or 'n'. ");
                    }
                }
            }
        }
        System.out.println("Goodbye");
        return;
    }
    private void addPhonesToInv(int store, int phone, int num)throws SQLException{
        try{
            Statement s = con.createStatement();
            s.executeQuery("update inventory set quantity = quantity + "+num+" where storeid = '"+store+"' and itemid = '"+phone+"'");
            s.close();
            System.out.println("Phones Added to Inventory");
        }catch(SQLException e){
            System.out.println(e.getMessage());
            con.close();
        }
    }
    private int getPhoneInv(int storeID, int phoneID)throws SQLException{
        List<Integer> quantity = new ArrayList<Integer>();
        try{
            Statement s = con.createStatement();
            ResultSet r = s.executeQuery("select quantity from inventory where storeid = '"+storeID+"' and itemid = '"+phoneID+"'");
            quantity = resultsToArrayList(r);
            s.close();
        }catch(SQLException e){
            System.out.println(e.getMessage());
            con.close();
        }
        return quantity.get(0);
    }
    private void printStoreInv(int store)throws SQLException{
        try{
            Statement s = con.createStatement();
            ResultSet r = s.executeQuery("select i_desc as product, quantity, itemid from inventory where storeid = '"+store+"'");
            System.out.println("**************************************************");
            printResults(r);
            System.out.println("**************************************************");
            s.close();
        }catch(SQLException e){
            System.out.println(e.getMessage());
            con.close();
        }
    }
    private void printStores()throws SQLException{
        try{
            Statement s1 = con.createStatement();
            ResultSet r = s1.executeQuery("select addr_street as street, addr_city as city, addr_state as state, storeid from physical_store");
            System.out.println("***************************************************************************");
            printResults(r);
            System.out.println("***************************************************************************");
            s1.close();
        }catch(Exception e){
            System.out.println(e.getMessage());
            con.close();
        }
    }
    private void printPhone(int phone)throws SQLException{
        try{
            Statement s = con.createStatement();
            ResultSet phoneInfo = s.executeQuery("select distinct(p_mfg) as make, p_model as model\n" +
                                                "from phone\n" +
                                                "where phoneid = '"+phone+"'");
            System.out.println("**************************");
            printResults(phoneInfo);
            System.out.println("**************************");
            s.close();
        }catch(SQLException e){
            System.out.println(e.getMessage());
            con.close();
        }
    }
    public void printPlanInfo(String first, String last, String street, String city, String state, int acctID, int plan, List<String> phone_nums, int moy, int payType){
        String plan_type = null;
        String interval = null;
        String pay_type = null;
        String cost = "";
        switch(plan){
            case 1: plan_type = "Individual Plan"; break;
            case 2: plan_type = "Family Plan"; break;
            case 3: plan_type = "Business Plan"; break;
            default: break;
        }
        switch(payType){
            case 1: pay_type = "Pay by Minute"; cost = "$1 per call minute";break;
            case 2: pay_type = "Pay by Text"; cost = "$.10 per byte";break;
            case 3: pay_type = "Flat rate"; 
            if(moy == 1){
            interval = "Monthly Payment";
            cost = "$50";
            }
            else{
                interval = "Annual Payment";
                cost = "$499.99";
            }
            break;
        }
        
        System.out.println("\nName:\t\t\t"+ first + " " + last);
        System.out.println("Address:\t\t"+ street + ", " + city + " " + state);
        System.out.println("Primary Number:\t\t"+ phone_nums.get(0));
        if(phone_nums.size() > 1){
            System.out.println("Other numbers:");
            for(int i = 1; i < phone_nums.size(); i++){
            System.out.println("\t\t\t"+phone_nums.get(i));
            }
        }
        System.out.println("\n\t\tBILLING INFORMATION");
        System.out.println("Type of Plan:\t\t\t"+ plan_type);
        System.out.println("Payment Schedule:\t\t"+ interval);
        System.out.println("Payment Method:\t\t\t"+ pay_type);
        System.out.println("Cost per Payment:\t\t"+ cost);
    }
    private void printBillingPlan(int acctID)throws SQLException{
        try{
            Statement s = con.createStatement();
            ResultSet r = s.executeQuery("select timeframe as payment_schedule, pay_method, price as price_per_payment from billing_plan where acctid = '"+acctID+"'");
            System.out.println("*****************************************************************");
            printResults(r);
            s.close();
            System.out.println("*****************************************************************");
        }catch(SQLException e){
            System.out.println(e.getMessage());
            con.close();
        }
    }
    private void printPhoneNums(int acctID)throws SQLException{
        try{
            Statement s = con.createStatement();
            ResultSet r = s.executeQuery("select phone_num as account_numbers from phone_number where acctid = '"+acctID+"'");
            printResults(r);
            s.close();
            System.out.println("*****************************************************************");
        }catch(SQLException e){
            System.out.println(e.getMessage());
            con.close();
        }
    }
    private int getNumCount(int acctID)throws SQLException{
        List<String> numPhones = new ArrayList<String>();
        int num = 0;
        try{
            Statement s = con.createStatement();
            ResultSet r = s.executeQuery("select phone_num from phone_number where acctid = '"+acctID+"'");
            numPhones = resultsToArrayListString(r);
            s.close();
            for(int i = 0; i < numPhones.size(); i++){
                num++;
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
            con.close();
        }
        return num;
    }
    private void printPlanSchedule(int acctID)throws SQLException{
        List<String> plans = new ArrayList<String>();
        try{
            Statement s = con.createStatement();
            ResultSet r = s.executeQuery("select timeframe as payment_schedule from billing_plan where acctid = '"+acctID+"'");
            System.out.println("************");
            printResults(r);
            System.out.println("************");
            s.close();
        }catch(SQLException e){
            System.out.println(e.getMessage());
            con.close();
        }
    }
    private void printPayMethod(int acctID)throws SQLException{
        List<String> plans = new ArrayList<String>();
        try{
            Statement s = con.createStatement();
            ResultSet r = s.executeQuery("select pay_method as payment_method from billing_plan where acctid = '"+acctID+"'");
            System.out.println("************");
            printResults(r);
            System.out.println("************");
            s.close();
        }catch(SQLException e){
            System.out.println(e.getMessage());
            con.close();
        }
    }
    public void addCustomer(int acctID, String street, String city, String state)throws SQLException{
        try{
            Statement s = con.createStatement();
            s.executeQuery("insert into customer values('" + acctID + "','" + city + "','" + state + "','" + street + "')");
            s.close();
            System.out.println("Customer added");
        }catch(SQLException e){
            System.out.println("Customer problem");
            System.out.println(e.getMessage());
            con.close();
        }
    }
    public void addPersonalCustomer(int acctID, String last, String first)throws SQLException{
        try{
            Statement s = con.createStatement();
            s.executeQuery("insert into personal_customer values('" + acctID + "','" + last + "','" + first + "')");
            s.close();
            System.out.println("Personal Customer added");
        }catch(SQLException e){
            System.out.println("Personal Customer problem");
            System.out.println(e.getMessage());
            con.close();
        }
    }
    public void addSale(int custID, double totalCost)throws SQLException{
        try{
            Statement s = con.createStatement();
            s.executeQuery("insert into sale values(null, '" + custID + "','" + totalCost + "')");
            s.close();
            System.out.println("Sale added");
        }catch(SQLException e){
            System.out.println("Sale problem");
            System.out.println(e.getMessage());
            con.close();
        }
    }
    public void addBusinessCustomer(int acctID, String company)throws SQLException{
        try{
            Statement s = con.createStatement();
            s.executeQuery("insert into business_customer values('" + acctID + "','" + company + "')");
            s.close();
            System.out.println("Business Customer added");
        }catch(SQLException e){
            System.out.println("Business Customer problem");
            System.out.println(e.getMessage());
            con.close();
        }
    }
    public void addBillingPlan(int acctID, int payType, String moy)throws SQLException{
        double rate = 0;
        String payment = null;
        if(payType == 1){
            payment = "minute rate";
            rate = 1.00;
        }
        else if(payType == 2){
            payment = "text rate";
            rate = 0.10;
        }
        else{
            payment = "flat rate";
            rate = 50.00;
        }
        try{
            Statement s = con.createStatement();
            s.executeQuery("insert into billing_plan values('" + acctID + "','" + rate + "','" + moy + "','" + payment +"')");
            s.close();
            System.out.println("Billing Plan added");
        }catch(SQLException e){
            System.out.println("Billing Plan problem");
            System.out.println(e.getMessage());
            con.close();
        }
    }
    private void editBillingPlanSchedule(int acctID, String mory)throws SQLException{
        try{
            Statement s = con.createStatement();
            s.executeQuery("update billing_plan set timeframe = '"+mory+"' where acctid = '"+acctID+"'");
            s.close();
            System.out.println("Billing Plan updated");
        }catch(SQLException e){
            System.out.println("Update Billing Plan error");
            System.out.println(e.getMessage());
            con.close();
        }
    }
    private void editBillingPlanMethod(int acctID, String mory)throws SQLException{
        double pay = 0;
        if(mory.equals("minute rate")){
            pay = 1;
        }
        else if(mory.equals("text rate")){
            pay = .1;
        }
        else if(mory.equals("flat rate")){
            pay = 50;
        }
        try{
            Statement s = con.createStatement();
            s.executeQuery("update billing_plan set pay_method = '"+mory+"', price = '"+pay+"' where acctid = '"+acctID+"'");
            s.close();
            System.out.println("Billing Plan updated");
        }catch(SQLException e){
            System.out.println("Update Billing Plan error");
            System.out.println(e.getMessage());
            con.close();
        }
    }
    public List<String> createNums(int numPhones, List<String> phone_nums){
        Random rand = new Random();
        for(int j = 0; j < numPhones; j++){
            String number = "";
            for(int k = 0; k < 10; k++){
                number += Integer.toString(rand.nextInt(10));
            }
            phone_nums.add(number);
        }
        return phone_nums;
    }
    public void addPhoneNums(int acctID, int numPhones, List<String> phone_nums)throws SQLException{
        Random rand = new Random();
        for(int j = 0; j < numPhones; j++){
            try{
                Statement s = con.createStatement();
                s.executeQuery("insert into phone_number values('" + acctID + "', '" + phone_nums.get(j) + "')");
                s.close();
                System.out.println("Number added");
            }catch(Exception e){
                System.out.println("Number problem");
                System.out.println(e.getMessage());
                con.close();
            }
        }
    }
    private void removePhoneNums(int acctID, int request)throws SQLException{
        List<String> numbers = new ArrayList<String>();
        try{
            Statement s = con.createStatement();
            ResultSet r = s.executeQuery("select phone_num from phone_number where acctid = '"+acctID+"'");
            //String[] numbers = new String[r.getMetaData().getColumnCount()];
            numbers = resultsToArrayListString(r);
            for(int i = request; i > 0; i--){
                s.execute("delete from phone_number where acctid = '"+acctID+"' and phone_num = '"+numbers.get(i)+"'");
            }
            s.close();
            System.out.println("Numbers removed");
        }catch(Exception e){
            System.out.println("Number removal problem");
            System.out.println(e.getMessage());
            con.close();
        }
    }
    public void addAccount(int acctID, String primary, int plan)throws SQLException{
        String plan_type = null;
        if(plan == 1){
            plan_type = "individual";
        }
        if(plan == 2){
            plan_type = "family";
        }
        if(plan == 3){
            plan_type = "business";
        }
        try{
            Statement s = con.createStatement();
            s.executeQuery("insert into account values('" + acctID + "', '" + acctID + "', '" + primary + "', '" + plan_type + "')");
            s.close();
            System.out.println("Account added");
        }catch(Exception e){
            System.out.println(e.getMessage());
            con.close();
        }
    }
    private void printResults(ResultSet result) throws SQLException{
        try{
            if (!result.next()){
                System.out.println ("Empty Result");
                result.close();
            }
            else {
                // Use meta data to get column names, widths
                String f1 = "%-";
                String f2 = "s";
                String formatted = "";
                ResultSetMetaData rsmd = result.getMetaData();
                int numColumns = rsmd.getColumnCount();
                String[] colNames = new String[numColumns];
                int[] precisions = new int[numColumns];
                for (int i = 0; i < numColumns; i++){
                    colNames[i] = rsmd.getColumnName(i+1);
                    precisions[i] = rsmd.getPrecision(i+1);
                    formatted = f1+(precisions[i] + colNames[i].length())+f2;
                    System.out.printf(formatted, colNames[i]);
                }
                System.out.println();

                int numRows = 0;
                do {
                    for (int z = 0; z < numColumns; z++){
                        String currVal = result.getString(colNames[z]);
                        int currValLength = 0;
                        if (result.wasNull()){
                            currVal = "null";
                            currValLength = 4;
                        }
                        else{
                            currValLength = currVal.length();
                        }
                        formatted = f1+(precisions[z] + colNames[z].length())+f2;
                        System.out.printf(formatted, currVal);
                    }
                    System.out.println();
                    numRows++;
                } while (result.next());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            con.close();   
        }
    }

    private List<Integer> resultsToArrayList(ResultSet r) throws SQLException {
        List<Integer> list = new ArrayList<Integer>();
        int a = 0;
        while(r.next()){
            a = r.getInt(1);
            list.add(a);
        }
        return list;
    }
    private List<Double> resultsToArrayListDouble(ResultSet r) throws SQLException {
        List<Double> list = new ArrayList<Double>();
        double a = 0;
        while(r.next()){
            a = r.getDouble(1);
            list.add(a);
        }
        return list;
    }
    private List<String> resultsToArrayListString(ResultSet r) throws SQLException {
        List<String> list = new ArrayList<String>();
        String a = "";
        while(r.next()){
            System.out.println("Resultset: "+r.getString(1));
            a = r.getString(1);
            list.add(a);
        }
        return list;
    }
}