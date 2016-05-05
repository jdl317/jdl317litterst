/*
 * This is basically the Home screen for the project.  This screen allows you to choose an 
 * interface, or select 0 to exit the program.
 */
package jdl317;

import java.util.Scanner;

/**
 *
 * @author jefflitterst
 */
class Startup {
    int selection;
    boolean exit;
    
    //Startup constructor
    public Startup(){
        selection = -1;
        exit = false;
    }
    public void login(Scanner scan){
        System.out.println("\nWelcome to Jog Wireless!\n");
        System.out.println("Please select the interface of your choice");
        System.out.println("\n\t1. Purchase\n\t2. Usage\n\t0. Exit JogWireless");
        boolean goodchoice = false;
        while(!goodchoice){    
            if(scan.hasNextInt()){
                selection = scan.nextInt();
                if(selection == 0 || selection == 1 || selection == 2){
                    goodchoice = true;
                    break;
                }
                else{
                    System.out.println("Please enter either 1, 2, or 0 to exit.");
                }
            }
            else{
                System.out.println("Please enter an integer");
            }
            scan.nextLine();
        }       
        
    }
}
