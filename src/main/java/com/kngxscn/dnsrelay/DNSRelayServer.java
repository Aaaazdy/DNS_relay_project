package com.kngxscn.dnsrelay;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class DNSRelayServer {
    static private int error=1;
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
            error=0;
        } catch (IOException e) {
            System.out.println("the format of your file is wrong!");
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
    static public void error(){
        System.out.println("your format is wrong!");
        System.out.println("you should input like this: dnsrelay [-d|-dd] [<dns-server>][<db-file>]");
        System.exit(0);
    }
    //----------------------------------------------run the code------------------------------------------------------//
    public static void main(String[] args ) throws IOException {
        //get the domain ip
        System.out.println("please input the direct of txt file");
        Scanner sc= new Scanner(System.in);
        String a=sc.next();
        if(!a.equals("dnsrelay")){
            error();
        }
        String b=sc.next();
        if (!(b.equals("-d") || b.equals("-dd"))) {
            error();
        }
        String c=sc.next();
        String[] n=c.split("\\.");
        if (n.length!=4){
            error();
        }

        String d=sc.next();
        ourMap = getDomainIP(d);
        File file = new File(d);
        long size = file.length();
        String dnsIp=c;
        int mode=0;

        System.out.println("Delayrelay ,"+System.currentTimeMillis());
        System.out.println("Usage: dnsrelay [-d|-dd] [<dns-server>][<db-file>]");
        System.out.println("");
        System.out.println("Name server "+"[-d|-dd] [<dns-server>][<db-file>]");
        if (b.equals("-d")) {
            System.out.println("Debug level " + 1);
        }
        else if (b.equals("-dd")) {
            System.out.println("Debug level " + 2);
            mode = 1;
        }
        System.out.println("Bind UDP port " + 53);
        System.out.println("Try to load table " + d + " ...ok!");
        if (b.equals("-dd")) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
        System.out.println(ourMap.size() + " names," + "occupy " + size + " bytes memory");
        System.out.println("The local domain-IP mapping file is read successfully.");
        //create packet and socket
        byte[] num = new byte[1024];
        DatagramPacket packet = createPacket(num);
        try {
            socket = createSocket(53);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        //Create a thread pool of 10 capacity
        ExecutorService Pool = Executors.newFixedThreadPool(1);
        //receive packet and execute them
        while (true) {
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Pool.execute(new QueryParser(packet,dnsIp,mode));
        }
    }
}

