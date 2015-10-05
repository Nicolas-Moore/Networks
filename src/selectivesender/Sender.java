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

    Scanner in = new Scanner(System.in);
    public static final int size = 128;
    int window = 0;
    int sequence = Integer.MAX_VALUE;
    int drop[] = new int[size];
    
    class Resend extends TimerTask{
        
        int packetNumber;
        DatagramSocket resendSocket;
        DatagramPacket resendPkt;
        InetAddress IPAddress;
        byte resendData[] = new byte[size];
        
        
        public  Resend(int i, DatagramSocket socket, DatagramPacket pkt){
            packetNumber = i;
            resendSocket = socket;
            resendPkt = pkt;
            IPAddress = pkt.getAddress();
        }
        
        @Override
        public void run() {
            System.out.println("Packet "+ packetNumber+ " times out. resend packet "+packetNumber);
            resendData[0] = (byte) packetNumber;
            resendPkt = new DatagramPacket(resendData, resendData.length, IPAddress, 9876);
            try {
                resendSocket.send(resendPkt);
            } catch (IOException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Packet "+packetNumber+" is resent.");
        }
        
        
        
    }

    public static void main(String ARGS[]) {
        Sender start = new Sender();
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
            DatagramSocket acknowledgementSocket = new DatagramSocket(9878);
            InetAddress IPAddress = InetAddress.getByName("localhost");
            byte[] sendData = new byte[size];
            byte[] ackData = new byte[size];
            sendData = prepData(sendData);
            DatagramPacket sendPkt = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
            DatagramPacket ackPkt = new DatagramPacket(ackData, sendData.length);
            senderSocket.send(sendPkt);  // sending sequence, window and drop packet information
            System.out.println("Send window's size and maximum seq. number to the reciever.");
            acknowledgementSocket.receive(ackPkt);
            ackData = ackPkt.getData();
            if(ackData[0] == (byte) sequence){
                System.out.println("Received Confirmation from the receiver.");
            }

            int windowStart = 0;
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
            boolean finished= false;
            do {
                boolean sent = false;
                if(i<sequence){
                if (i < window) { // if i is sending the first packets of the window
                    sendData[0] = (byte) i;
                    sendPkt = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
                    senderSocket.send(sendPkt);
                    sent = true;
                } else if (windowTracker[i - window] == 'a') {
                    sendData[0] = (byte) i;
                    sendPkt = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
                    senderSocket.send(sendPkt);
                    sent = true;
                }
                if (sent == true) {
                    windowTracker[i] = 's';
                    System.out.println("Packet " + i + " is sent, window" + messageSender(windowStart, windowTracker));
                    windowTimer[i] = new Timer();
                    windowTimer[i].schedule(new Resend(i,senderSocket,sendPkt),3000,3000);
                    i++;
                }
                }
                if (sent == false) { // if we are at the edge of our window, we listen
                    acknowledgementSocket.receive(ackPkt);
                    ackData = ackPkt.getData();
                    int ack = ackData[0];
                    
                    windowTracker[ack] = 'a';
                    System.out.println("Ack "+ack+" received, window"+messageSender(windowStart,windowTracker));
                    windowTimer[ack].cancel();
                }
                while(windowTracker[windowStart] == 'a' && windowStart != sequence-1){
                    windowStart++;
                }
                finished = finish(windowTracker);
            } while (finished!= true);

        } catch (IOException exc) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, exc);
        }
    }

    
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
