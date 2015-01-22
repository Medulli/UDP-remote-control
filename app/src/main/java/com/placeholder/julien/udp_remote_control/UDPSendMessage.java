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
    //messages to send
    private ArrayList<String> messages;
    private ArrayList<Integer> delay;
    //exception if failed
    private Exception exception;
    //parent activity, to display success or failure
    private Activity activity;

    public UDPSendMessage(final Activity activity,ArrayList<String> messages, ArrayList<Integer> delay, String host, int port) {
        this.messages = messages;
        this.delay = delay;
        this.host = host;
        this.port = port;
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

            //go through every message
            for(int i = 0; i<messages.size(); i++){
                //create message buffer
                byte[] message = messages.get(i).getBytes(StandardCharsets.US_ASCII);
                //initialize a datagram packet with data and address
                DatagramPacket packet = new DatagramPacket(message, message.length,
                        address, port);
                //sleep according to delay
                Thread.sleep(delay.get(i));
                //send the packet
                socket.send(packet);
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
