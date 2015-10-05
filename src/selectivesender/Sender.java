/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package selectivesender;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Dominik Pruss
 * @author Nicolas Moore
 *
 */

public class Sender {

    Scanner in = new Scanner(System.in);        // Initialize scanner
    public static final int size = 128;         // Set size variable for data
    int window = 0;                             // Set window base
    int sequence = Integer.MAX_VALUE;           // Initialize sequence number
    int drop[] = new int[size];                 // Initialize array for dropped packets
    
    // Class for setting up timers on packets
    class Resend extends TimerTask{
        
        int packetNumber;                       // Packet number reference
        DatagramSocket resendSocket;            // Socket to resend on
        DatagramPacket resendPkt;               // Packet to resend
        InetAddress IPAddress;                  // Destination address
        byte resendData[] = new byte[size];     // Data array to resend
        
        // Constructor for resend class, sets appropriate parameters
        public  Resend(int i, DatagramSocket socket, DatagramPacket pkt){
            packetNumber = i;
            resendSocket = socket;
            resendPkt = pkt;
            IPAddress = pkt.getAddress();
        }
        
        // Override run method in TimerTask class to resend timed out packet
        @Override
        public void run() {
            System.out.println("Packet "+ packetNumber+ " times out. resend packet "+packetNumber);     // Print packet being resent
            resendData[0] = (byte) packetNumber;                                                        // Set the sequence number 
            resendPkt = new DatagramPacket(resendData, resendData.length, IPAddress, 9876);             // Create packet to resend
            try {
                resendSocket.send(resendPkt);                                                           // Resend packet
            } catch (IOException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);                   // Catch IOExceptions
            }
            System.out.println("Packet "+packetNumber+" is resent.");                                   // Print packet is resent
        }
        
    }
    
    // Main method
    public static void main(String ARGS[]) {
        Sender start = new Sender();
    }
    
    // Constructor
    public Sender() {
        setWindow();        // Get window size
        setSequence();      // Get number of segments
        if (window * 2 > sequence) {    // If window size is to large ask for new one
            do {
                System.out.println("Your window cannot be over half the size of your max sequence number.\n");
                setWindow();
            } while (window * 2 > sequence);
        }
        setDrop();          // Get packets to be dropped
        try {
            DatagramSocket senderSocket = new DatagramSocket(9877);             // Set up sending socket
            DatagramSocket acknowledgementSocket = new DatagramSocket(9878);    // Set up ack socket
            InetAddress IPAddress = InetAddress.getByName("localhost");         // Get IP address
            byte[] sendData = new byte[size];                                   // Set up array for data
            byte[] ackData = new byte[size];                                    // Set up array for acks
            sendData = prepData(sendData);                                      // Format data for sending
            DatagramPacket sendPkt = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);    // Set up packet to send
            DatagramPacket ackPkt = new DatagramPacket(ackData, sendData.length);   // Set up ack packet
            senderSocket.send(sendPkt);                                         // Send packet

            System.out.println("Send window's size and maximum seq. number to the reciever.");  // Message for communication
            int windowStart = 0;                            // Set window base
            char windowTracker[] = new char[sequence];      // Create array to track window
            Timer windowTimer[] = new Timer[sequence];      // Create array of timers for packets
            for (int i = 0; i < sequence; i++) {            // Set packets to not sent
                windowTracker[i] = 'n';
            }
            /*  For windowTracker we will have the following 
             *   s = sent   
             *   a = Acknowledged
             *   n = not sent
             */
            int i = 0;                      // Indexing variable
            boolean finished= false;        // Looping variable
            do {
                boolean sent = false;       // Variable for sent or not
                if(i<sequence){             // As long as we arent at the end of the sequence
                if (i < window) { // if i is sending the first packets of the window
                    sendData[0] = (byte) i; // Set first spot to sequence number
                    sendPkt = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);   // Set up packet
                    senderSocket.send(sendPkt);                                                 // Send packet
                    sent = true;                                                                // Set sent to true
                } else if (windowTracker[i - window] == 'a') {                                  // If previous packets acked send next packet
                    sendData[0] = (byte) i;
                    sendPkt = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
                    senderSocket.send(sendPkt);
                    sent = true;
                }
                if (sent == true) {         // If sent print message, set timer and scheduel resend if needed then increment i
                    windowTracker[i] = 's';
                    System.out.println("Packet " + i + " is sent, window" + messageSender(windowStart, windowTracker));
                    windowTimer[i] = new Timer();
                    windowTimer[i].schedule(new Resend(i,senderSocket,sendPkt),3000,3000);
                    i++;
                }
                }
                if (sent == false) { // if we are at the edge of our window, we listen for acknowledgements
                    acknowledgementSocket.receive(ackPkt);
                    ackData = ackPkt.getData();
                    int ack = ackData[0];
                    
                    windowTracker[ack] = 'a';
                    System.out.println("Ack "+ack+" received, window"+messageSender(windowStart,windowTracker));    // Print message for acked packet
                    windowTimer[ack].cancel();                                                                      // Cancel its timer
                }
                while(windowTracker[windowStart] == 'a' && windowStart != sequence-1){                              // Loop through sequence
                    windowStart++;
                }
                finished = finish(windowTracker);
            } while (finished!= true);                                                                              // Stop when all packets sent and acked

        } catch (IOException exc) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, exc);                                  // Catch IOExceptions
        }
    }
    
    // Make sure all packets have been sent sand aked
    public boolean finish(char[] array){
        for(int i =0; i< array.length;i++){
            if(array[i] != 'a'){
                return false;
            }
        }
        return true;
    }
    /*
     This method will take in the array with the packet statuses and the current packet just sent
     the boolean is whether or not this is an acknowledgement message or not
     */
    public String messageSender(int windowStart, char array[]) {
        String message = "[";
        for(int i =0; i< window;i++){
            if( (windowStart+i) < sequence){

            switch(array[windowStart+i]){
                case 's':
                    message = message +""+(windowStart+i)+"*";
                break;
                case 'a':
                case 'n':
                    message = message +""+(windowStart+i);
                default:
                    break;
                
            }
            }
            else{
                message = message+"-";
            }
            if(i!=window-1){
                message = message+",";
            }
            else{
                message = message +"]";
            }
        }
        return message;
    }

    /*
     This method reads input from the user and sets the window size
     */
    public void setWindow() {
        do {
            System.out.print("Enter the window’s size on the sender: ");
            window = in.nextInt();
            if (window < 0) {
                System.out.println("That is an invalid entry for window size.");
            }
        } while (window < 0);
    }

    /*
     This method reads input from the user to set the sequence size
     */
    public void setSequence() {
        do {
            System.out.print("Enter the maximum sequence number on the sender: ");
            sequence = in.nextInt();
            if (sequence < 0) {
                System.out.println("That is an invalid entry for max sequence number.");
            }
        } while (sequence < 0);
    }

    /*
     This method will take input form the user to determine which packets to drop
     */
    public void setDrop() {

        System.out.print("Select the packet(s) that will be dropped:\n (seperate with spaces, enter none to not drop any packets): ");
        in.nextLine(); // catching return characters
        String input = in.nextLine();// reading in the numbers to skip
        String[] seperate = input.split("\\s+");
        
        if(input.equals("none")){
            return;
        }

        for (int i = 0; i < seperate.length; i++) {
            int value = Integer.parseInt(seperate[i]);
            if (value < 0 || value >= sequence) {
                System.out.println(value + " is an invalid packet number to drop.");
            } else {
                drop[i] = value;
            }
        }

    }

    /*
     This method will prepare the data to be sent initially, with the first spot on the array being the sequence number
     and the following bytes being the packets to drop.
     */
    public byte[] prepData(byte[] in_data) {
        in_data[0] = (byte) sequence;
        in_data[1] = (byte) window;
        int counter = 2;
        for (int i = 0; i < size; i++) {
            if (drop[i] != 0) {
                in_data[counter] = (byte) drop[i];
                counter++;
            }
        }
        return in_data;
    }
}
