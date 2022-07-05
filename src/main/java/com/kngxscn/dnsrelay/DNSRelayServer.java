package com.kngxscn.dnsrelay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DNSRelayServer {
    static final Object Obj = new Object();
    // ---------------------------read domainIp, and store them-------------------------------------------------------//
    private static Map<String, String> getDomainIP(String path) {
        File file = new File(path);
        Map<String, String> dAI = new HashMap<String, String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] word = line.split(" ");
                if (word.length < 2) {
                    continue;
                }
                dAI.put(word[1], word[0]);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dAI;
    }
    //-----------------------------------------the method of get socket,map and packet---------------------------------//
    private static DatagramSocket socket;
    private static Map<String, String> ourMap;


    static DatagramSocket getSocket() {
        return socket;
    }

    static DatagramSocket createSocket(int port) throws SocketException {
        DatagramSocket socket1=new DatagramSocket(port);
        return socket1;
    }

    static Map<String, String> getMap() {
        return ourMap;
    }

    static DatagramPacket createPacket(byte[] num){
        return new DatagramPacket(num, num.length);
    }
    //----------------------------------------------run the code------------------------------------------------------//
    public static void main(String[] args ) {
        //get the domain ip
        ourMap = getDomainIP("dnsrelay.txt");
        System.out.println("The local domain-IP mapping file is read successfully.In total " + ourMap.size() + " records.");
        //create packet and socket
        byte[] num = new byte[1024];
        DatagramPacket packet = createPacket(num);
        try {
            socket = createSocket(53);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        //Create a thread pool of 10 capacity
        ExecutorService Pool = Executors.newFixedThreadPool(10);
        //receive packet and execute them
        while (true) {
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Pool.execute(new QueryParser(packet));
        }
    }
}

