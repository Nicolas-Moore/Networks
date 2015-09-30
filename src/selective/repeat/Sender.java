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
            senderSocket.send(sendPkt);  //
            System.out.println("Send window's size and maximum seq. number to the reciever.");
            char windowTracker[] = new char[sequence];
            Timer windowTimer[] = new Timer[sequence];
            for (int i = 0; i < sequence; i++) {
                windowTracker[i] = 'n';
            }
            /*  For windowTracker we will have the following 
             *   s = sent   
             *   a = Acknowledged
             *   n = not sent
             */
            int i = 0;
            do {
                boolean sent = false;
                if (i < window) { // if i is sending the first packets of the window
                    sendData[0] = (byte) i;
                    sent = true;
                } else {// we have sent up to our window, so now we wait for acknowledgements

                }
                if (sent == true) {
                    windowTracker[i] = 's';
                    String message = messageSender(windowTracker, i, false);
                    System.out.println(message);
                    
                    i++;
                }
            if(sent == false){ // if we are at the edge of our window, we listen
                senderSocket.receive(sendPkt);
                sendData = sendPkt.getData();
                System.out.println((int)sendData[0]);
            }
            } while (true);

        } catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     This method will take in the array with the packet statuses and the current packet just sent
     the boolean is whether or not this is an acknowledgement message or not
     */
    public String messageSender(char array[], int n, boolean ack) {
        if (ack == false) {
            String message = "Packet " + n + " is sent. Window["; // do his format here
            // do a ton of if statements to show what the window looks like.
            if (n < window) { // if we are at the start of the packet.

                switch (array[0]) {
                    case 's':
                        message = message + "0*";
                        break;
                    case 'a':

                    case 'n':
                        message = message + "0";
                    default:
                        break;

                }

                for (int i = 1; i < window; i++) {
                    switch (array[i]) {
                        case 's':
                            message = message + "," + i + "*";
                            break;
                        case 'a':

                        case 'n':
                            message = message + "," + i;
                        default:
                            break;
                    }
                }
            } else { // we have shifted our window at this point
                switch (array[n + 1 - window]) {
                    case 's':
                        message = message + (n+1-window);
                    case 'a':

                    case 'n':
                        message = message + (n+1-window);
                    default:
                        break;
                }

                for (int i = n + 2 - window; i <= n; i++) { // this should be shifted to our window.
                    if (i > sequence - 1) {
                        message = message + "-"; // catches outside of window errors
                    } else {
                        switch (array[i]) {
                            case 's':
                                message = message + "," + i;

                            case 'a':

                            case 'n':
                                message = message + "," + i;
                            default:
                                break;
                        }
                    }
                }
            }
            message = message + "]";
            return message;
        } else { // this is an acknowledgement message
            return "aaaah";
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
