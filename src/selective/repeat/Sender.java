/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package selective.repeat;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;
import java.util.Timer;

/**
 *
 * @author Dominik Pruss
 * @author Nicolas Moore
 *
 */
public class Sender {

    Scanner in = new Scanner(System.in);
    int window = 0;
    int sequence = Integer.MAX_VALUE;
    int drop = -1;

    public static void main(String ARGS[]) {
        Sender driver = new Sender();
    }

    public Sender() {

        setWindow();
        setSequence();

        if (window * 2 > sequence) {
            do {
                System.out.println("Your window cannot be over half the size of your max sequence number.\n");
                setWindow();
            } while (window * 2 > sequence);
        }

        setDrop();

        try {
            DatagramSocket senderSocket = new DatagramSocket(9877);
            InetAddress IPAddress = InetAddress.getByName("localhost");
            byte[] sendData = new byte[1024];
            DatagramPacket sendPkt = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
            senderSocket.send(sendPkt);
            System.out.println("Send window's size and maximum seq. number to the reciever.");
            char windowTracker[] = new char[window];
            Timer windowTimer[] = new Timer[window];
            /*  For windowTracker we will have the following 
             *   S = sent   
             *   A = Acknowledged
             *   N = not sent
             */
            int i = 0;
            do {
                sendData[0] = (byte)i;
                sendPkt = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
                senderSocket.send(sendPkt);
                System.out.println("");
                if( i < window){
                    
                    
                }

            } while (true);

        } catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    

    public void setWindow() {
        do {
            System.out.print("Enter the window’s size on the sender: ");
            window = in.nextInt();
            if (window < 0) {
                System.out.println("That is an invalid entry for window size.");
            }
        } while (window < 0);
    }

    public void setSequence() {
        do {
            System.out.print("Enter the maximum sequence number on the sender: ");
            sequence = in.nextInt();
            if (sequence < 0) {
                System.out.println("That is an invalid entry for max sequence number.");
            }
        } while (sequence < 0);
    }

    public void setDrop() {
        do {
            System.out.print("Select the packet(s) that will be dropped:");
            drop = in.nextInt();
            if (drop < 0 || drop >= sequence) {
                System.out.println("That is an invalid packet number to drop.");
            }
        } while (drop < 0 || drop >= sequence);
    }

}