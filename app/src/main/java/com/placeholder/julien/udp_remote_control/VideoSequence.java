package com.placeholder.julien.udp_remote_control;

import java.util.ArrayList;

/**
 * Created by Julien on 22/01/2015.
 */
public class VideoSequence {
    private String name;
    private ArrayList<Integer> delay;
    private ArrayList<String> command;
    private ArrayList<String> video;

    public ArrayList<Integer> getDelay() {
        return delay;
    }

    public ArrayList<String> getCommand() {
        return command;
    }

    public ArrayList<String> getVideo() {
        return video;
    }

    public String getName() {
        return name;
    }

    public VideoSequence(String name) {
        this.name = name;
        this.delay = new ArrayList<>();
        this.command = new ArrayList<>();
        this.video = new ArrayList<>();
    }

    public VideoSequence(String name, ArrayList<Integer> delay, ArrayList<String> command, ArrayList<String> video) {
        this.name = name;
        this.delay = delay;
        this.command = command;
        this.video = video;
    }

    //add video at the end
    public void addVideoCommand(Integer delay, String command, String video) {
        this.delay.add(delay);
        this.command.add(command);
        this.video.add(video);
    }

    //returns an ArrayList of concatenated messages (command + video)
    public ArrayList<String> getMessages(){
        ArrayList<String> result = new ArrayList<>();
        for(int i=0; i<this.command.size(); i++){
            result.add(this.command.get(i)+" "+this.video.get(i)+"\n");
        }
        return result;
    }

    //returns a String of the format [start video1, stop video1]
    public String toString(){
        String result="[";
        for(int i=0; i<this.command.size(); i++){
            if(i!=0){
                result += ",";
            }
            result += this.command.get(i);
            result += " ";
            result += this.video.get(i);
        }
        result += "]";
        return result;
    }
}
