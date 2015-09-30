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

/**
 *
 * @author Nik
 */
public class Receiver {
    
    public Receiver(){
        
    try{
    DatagramSocket receiverSocket = new DatagramSocket(9876);
    byte[] rcvData = new byte[1024];
    DatagramPacket rcvPkt = new DatagramPacket(rcvData, rcvData.length);
    receiverSocket.receive (rcvPkt); // should grab all the packet info and everything
    InetAddress IPAddress = rcvPkt.getAddress();
    int port = receiverSocket.getPort();
    DatagramPacket ackPkt;
    rcvData = rcvPkt.getData();
    boolean drop[] = new boolean[(int)rcvData[0]];
    
    int a = (int)rcvData[0];
    if(drop[a] == false){
        ackPkt= new DatagramPacket(rcvData, rcvData.length);
    }
    else{
        drop[a] = false;
    }
    
     
    
    
}
    catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //public void 
    

}
