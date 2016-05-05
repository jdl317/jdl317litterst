/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    
    public Startup(){
        selection = -1;
        exit = false;
    }
    public void login(Scanner scan){
        System.out.println("\nWelcome to Jog Wireless!\n");
        System.out.println("Please select the interface of your choice, or 0 to exit");
        System.out.println("\n\t1. Purchase\n\t2. Usage");
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
