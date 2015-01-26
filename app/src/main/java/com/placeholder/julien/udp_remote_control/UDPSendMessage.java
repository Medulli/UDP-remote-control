package com.placeholder.julien.udp_remote_control;

import android.annotation.TargetApi;
import android.widget.Toast;
import android.app.Activity;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import android.os.AsyncTask;

/**
 * UDP server: send a series of message with delay between each one.
 */

public class UDPSendMessage extends AsyncTask<Void, Void, Boolean> {
    //server settings
    private String host;
    private int port;
    private int payload;
    //messages to send
    private ArrayList<ArrayList<String>> messagesList;
    private ArrayList<ArrayList<Integer>> delaysList;
    //exception if failed
    private Exception exception;
    //parent activity, to display success or failure
    private Activity activity;
    public String test;

    public UDPSendMessage(final Activity activity,ArrayList<ArrayList<String>> messagesList, ArrayList<ArrayList<Integer>> delaysList, String host, int port, int payload) {
        this.messagesList = messagesList;
        this.delaysList = delaysList;
        this.host = host;
        this.port = port;
        this.payload=payload;
        this.activity=activity;
    }

    //send the messages (in background)
    @TargetApi(19)
    protected Boolean doInBackground(Void... params) {
        try {
            //get the internet address of the specified host
            InetAddress address = InetAddress.getByName(host);
            //create a socket
            DatagramSocket socket = new DatagramSocket();

            //go through each sequence
            for(int i=0;i<delaysList.size();i++) {
                ArrayList<String> messages = messagesList.get(i);
                ArrayList<Integer> delay = delaysList.get(i);
                //go through each message
                for (int j = 0; j < messages.size(); j++) {
                    //create message buffer
                    byte[] message = messages.get(j).getBytes(StandardCharsets.US_ASCII);
                    test=String.valueOf(message.length);
                    //sleep according to delay
                    Thread.sleep(delay.get(j));
                    //initialize a datagram packet with data and address
                    for(int k=0;k<message.length;k+=payload){
                        //split the packet
                        DatagramPacket packet = new DatagramPacket(message, k, Math.min(payload, message.length-k));
                        //send the packet
                        packet.setAddress(address);
                        packet.setPort(port);
                        socket.send(packet);
                    }
                }
            }
            //close the socket
            socket.close();
        } catch (Exception e) {
            exception = e;
            return false;
        }
        return true;
    }

    protected void onPostExecute(Boolean success) {
        //success!
        if(success){
            Toast t = Toast.makeText(activity.getApplicationContext(),
                    "Successfully sent on " + this.host + ":" + Integer.toString(this.port), Toast.LENGTH_SHORT);
            t.show();
        } else { //failed to sent the messages
            Toast t = Toast.makeText(activity.getApplicationContext(), "Failed: "+ exception.toString(), Toast.LENGTH_SHORT);
            t.show();
        }
    }
}
