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
 * @author Nicolas
 */
public class Sender {

    Scanner in = new Scanner(System.in);
    int window = 0;
    int sequence = Integer.MAX_VALUE;
    int drop = -1;
    
    public static void main(String ARGS[]){
        Sender driver = new Sender();
    }

    public Sender() {

        setWindow();
        setSequence();
        

        if (window * 2 > sequence) {
            do {
                System.out.print("Your window cannot be over half the size of your sequence.\n");
                setWindow();
            } while (window * 2 > sequence);
        }
        setDrop();

        try {

            DatagramSocket senderSocket = new DatagramSocket(9877);
            InetAddress IPAddress = InetAddress.getByName("192.168.10.116");
            byte[] sendData = new byte[1024];
            DatagramPacket sendPkt = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);

            senderSocket.send(sendPkt);
        } catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setWindow() {
        do {
            System.out.print("Enter the window’s size on the sender:");
            window = in.nextInt();
            if (window < 0) {
                System.out.print("That is an invalid window size.\n");
            }
        } while (window < 0);
    }

    public void setSequence() {
        do {
            System.out.print("Enter the maximum sequence number on the sender:");
            sequence = in.nextInt();
            if (window < 0) {
                System.out.print("That is an invalid sequence number.\n");
            }
        } while (sequence < 0);
    }

    public void setDrop() {
        do {
            System.out.print("Select the packet(s) that will be dropped:");
            drop = in.nextInt();
            if (drop < 0 || drop >= sequence) {
                System.out.print("That is an invalid packet to drop.\n");
            }
        } while (drop < 0 || drop >= sequence);
    }

}
