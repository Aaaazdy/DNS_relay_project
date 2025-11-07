package com.dnsrelay;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        System.out.println("you should input like this: dnsrelay [-d|-dd] [<dns-server>] [<db-file>]");
        System.exit(0);
    }
    //----------------------------------------------run the code------------------------------------------------------//
    public static void main(String[] args ) throws IOException {
        Scanner input = new Scanner(System.in);
        String debugLevel;
        String dnsIp;
        String fileName;
        String cmd;
        System.out.println("Welcome to the DNS Relay Server!");
        System.out.println("To use the server, please input following the format below:");
        // System.out.println("please input: dnsrelay [-d|-dd] [<dns-server>] [<db-file>]");
        while (true) {
            System.out.println("please input: dnsrelay [-d|-dd] [<dns-server>] [<db-file>]");
            cmd = input.nextLine().trim();

            // \s represent whitespace character
            String[] args2 = cmd.split("\\s+");

            if (args2.length != 4) {
                System.out.println("wrong argument count!");
                continue;
            }

            // first of the args must be "dnsrelay"
            if (!args2[0].equals("dnsrelay")) {
                System.out.println("first arg must be dnsrelay!");
                continue;
            }

            // check if the second parameter is -d or -dd
            if (!args2[1].equals("-d") && !args2[1].equals("-dd")) {
                System.out.println("arg2 must be -d or -dd!");
                continue;
            }

            // check if the third parameter is a valid ip address
            String[] ip = args2[2].split("\\.");
            if (ip.length != 4) {
                System.out.println("dns-server format error!");
                continue;
            }

            debugLevel = args2[1];
            dnsIp    = args2[2];
            fileName = args2[3];
            // if the arguments are in correct format → break
            break;
        }

        ourMap = getDomainIP(fileName);
        File file = new File(fileName);
        long size = file.length();
        int mode=0;
        // SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        // Date date = new Date(System.currentTimeMillis());
        // System.out.println("Delayrelay ,"+formatter.format(date));
        // System.out.println("Usage: dnsrelay [-d|-dd] [<dns-server>][<db-file>]");
        // System.out.println("");
        // System.out.println("Name server "+"[-d|-dd] [<dns-server>][<db-file>]");
        if (debugLevel.equals("-d")) {
            System.out.println("Debug level " + 1);
        }
        else if (debugLevel.equals("-dd")) {
            System.out.println("Debug level " + 2);
            mode = 1;
        }
        // told user the UDP port 53 will be binded
        System.out.println("Bind UDP port " + 53);
        System.out.println("Try to load table " + fileName + " ...ok!");
        if (debugLevel.equals("-dd")) {
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
        ExecutorService Pool = Executors.newFixedThreadPool(10);
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

