package com.kngxscn.dnsrelay;

import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

// 实现Java runnable接口, 以满足多线程需求
public class QueryParser implements Runnable
{
    private byte[] packetData;
    private int packetLength;
    private int mode;
    private String DNSIP;
    private InetAddress clientAddress;
    private int clientPort;

    QueryParser(DatagramPacket packet, String DNSIP, int mode)
    {
        packetLength = packet.getLength();
        this.DNSIP = DNSIP;
        this.mode = mode;
        packetData = new byte[packetLength];
        System.arraycopy(packet.getData(), 0, packetData, 0, packet.getLength());
        // 保存packet中的数据
        clientAddress = packet.getAddress();
        // 返回机器的ip地址
        clientPort = packet.getPort();
        // 返回机器端口
    }

    //java多线程实现
    public void run()
    {

//读取头文件和域名
//--------------------------------------------------------------------

        ArrayBlockingQueue<Byte> buffer = new ArrayBlockingQueue(12, true);
        // 为了提高服务器性能, 使用阻塞队列实现对DNS Header的buffer
        //创建字节类ArrayBlockingQueue队列, 用于实现buffer
        //offer来向队列中存储元素时，存储成功时会返回true，失败时会返回false；
        //利用poll来从队列中取出元素，若队列中元素不足时，会返回null。
        DNSHeader dnsHeader = new DNSHeader();
        DNSQuestion dnsQuestion = new DNSQuestion();
        // 处理请求，返回结果
        if (mode == 1)
        {
            System.out.println("-------------------------------------------------------------------------");
            System.out.println("RECEIVED from" + " 127.0.0.1:" + clientPort + " (" + packetLength + "Bytes) " + "[ID:" + packetData[0] + packetData[1] + "]");
            System.out.println(Utils.byteArrayToHexString(packetData));
            System.out.println("ID:" + dnsHeader.getTransID() + " " + "QR:" + (((((int) (dnsHeader.getFlags())) << 16) >>> 31)) + " " + "opcode:" + (((((int) (dnsHeader.getFlags())) << 17) >>> 28)) +
                    " " + "AA:" + ((((int) (dnsHeader.getFlags()) << 21) >>> 31)) + " " + "TC:" + ((((int) (dnsHeader.getFlags()) << 22) >>> 31)) + " " + "RD:" + ((((int) (dnsHeader.getFlags()) << 23) >>> 31) +
                    " " + "RA:" + (((((int) (dnsHeader.getFlags())) << 24) >>> 31)) + " " + "RCODE:" + ((((int) (dnsHeader.getFlags())) << 28) >> 28)));
        }
        try
        {
            // 读取DNS header
            /**
             DNS header 结构
             0  1  2  3  4  5  6  7  0  1  2  3  4  5  6  7
             +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
             |                      ID                       |
             +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
             |QR|  opcode   |AA|TC|RD|RA|   Z    |   RCODE   |
             +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
             |                    QDCOUNT                    |
             +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
             |                    ANCOUNT                    |
             +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
             |                    NSCOUNT                    |
             +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
             |                    ARCOUNT                    |
             +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
             */
            //故每2字节读取一次
            //先读取ID
            boolean ifOfferSucceed = false;
            int i = 0;
            int index = 0;  //buffer 的索引
            byte[] bytes = new byte[2];
            while (i < 12)
            {
                do
                {
                    ifOfferSucceed = buffer.offer(packetData[i]);
                    if (ifOfferSucceed)
                        i++;
                }
                while (ifOfferSucceed && i < 12);
                //buffer是否已满, 若未满, 则继续写入buffer

                //若i>=12,则header已经全部写入buffer
                while ((buffer.size() > 0) && (buffer.size() % 2 == 0))
                //若buffer队列还有Byte且为偶数个,则进行读取. 否则跳出循环, 继续读取packetData
                {
                    bytes[0] = buffer.poll();
                    bytes[1] = buffer.poll();
                    dnsHeader.setHeader(index, Utils.byteArrayToShort(bytes));
                    index++;
                }

            }

            /**
             DNS header 后为 Queries
             */
            // 获取查询的域名
            if (dnsHeader.getQDcount() > 0)
            // Qdcount为1, 则读取Queries负载
            {
                String domainName = Utils.extractDomain(packetData, 0x00);
                //域名以字节0x00结尾
                dnsQuestion.setQueryName(domainName);
                i = 12 + domainName.length() + 2;
                //加上最低级域名的一字节表示长度的字符和结束字符0x00
                // queries去除域名后剩下的type 和 class
                for (int j = 0; j < 2; j++)
                {
                    bytes[j] = packetData[j + i];
                }
                i += 2;
                dnsQuestion.setQueryType(Utils.byteArrayToShort(bytes));
                for (int j = 0; j < 2; j++)
                {
                    bytes[j] = packetData[j + i];
                }
                i += 2;
                dnsQuestion.setQueryClass(Utils.byteArrayToShort(bytes));
            } else
            {
                System.out.println(Thread.currentThread().getName() + " DNS数据长度不匹配, Packet length fault");
            }
        } catch (ArrayIndexOutOfBoundsException e)
        {
            System.out.println(Thread.currentThread().getName() + " Packet length fault");
        }

// 查询本地域名-IP映射
//--------------------------------------------------------------------


        String ip = DNSRelayServer.getMap().getOrDefault(dnsQuestion.getQueryName(), "");
        System.out.println(Thread.currentThread().getName() + " " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()) + " Client 127.0.0.1 " + "info:" + dnsQuestion.getQueryName() + "  TYPE:" + dnsQuestion.getQueryType() + "  CLASS:" + dnsQuestion.getQueryClass());

        if ((!ip.equals("")) && dnsQuestion.getQueryType() == 1)
        { // 在本地域名-IP映射文件中找到结果且查询类型为A(Host Address)，构造回答的数据包
            // Header
            short flags = 0;
            if (ip.equals("0.0.0.0"))
            // 若ip等于0.0.0.0 则rcode为0011（名字差错）
            {
                flags = (short) 0x8583;
            } else
            {
                // 否则rcode设置为0000 (无差错)
                flags = (short) 0x8580;
            }
            DNSHeader dnsHeaderResponse = new DNSHeader(dnsHeader.getTransID(), flags, dnsHeader.getQDcount(), (short) 1, (short) 0, (short) 0);
            //获取刚刚创建的header
            byte[] headerBytes = dnsHeaderResponse.toByteArray();

            // 获取之前的Questions
            byte[] questionBytes = dnsQuestion.toByteArray();

            // Answers
            DNSRR answerDNSRR = new DNSRR((short) 0xc00c, dnsQuestion.getQueryType(), dnsQuestion.getQueryClass(), 3600 * 24, (short) 4, ip);
            byte[] answerBytes = answerDNSRR.toByteArray();

            // Authoritative nameservers，只是模拟了包格式，nameserver实际指向了查询的域名
            DNSRR authorityDNSRR = new DNSRR((short) 0xc00c, (short) 6, dnsQuestion.getQueryClass(), 3600 * 24, (short) 0, null);
            byte[] authorityBytes = authorityDNSRR.toByteArray();

            byte[] response_data = new byte[headerBytes.length + questionBytes.length + answerBytes.length];
            int responseOffset = 0;
            for (int i = 0; i < headerBytes.length; i++)
            {
                response_data[responseOffset++] = headerBytes[i];
            }
            for (int i = 0; i < questionBytes.length; i++)
            {
                response_data[responseOffset++] = questionBytes[i];
            }
            if (!ip.equals("0.0.0.0"))
            {
                for (int i = 0; i < answerBytes.length; i++)
                {
                    response_data[responseOffset++] = answerBytes[i];
                }
            }
//            for (int i = 0; i < authorityBytes.length; i++)
//            {
//                response_data[responseOffset++] = authorityBytes[i];
//            }

            if (mode == 1)
                System.out.println(Thread.currentThread().getName() + " " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()) + " Client 127.0.0.1 " + " DNS回复帧内容:0x" + Utils.byteArrayToHexString(answerBytes));
            // 回复响应数据包
            DatagramPacket responsePacket = new DatagramPacket(response_data, response_data.length, clientAddress, clientPort);
            synchronized (DNSRelayServer.Obj)
            // 使用synchronized关键字实现代码块的进程同步
            {
                try
                {
                    if (mode == 1)
                    {
                        packetLength = responsePacket.getLength();
                        packetData = new byte[packetLength];
                        System.arraycopy(responsePacket.getData(), 0, packetData, 0, responsePacket.getLength());
                        // 保存packet中的数据
                        clientAddress = responsePacket.getAddress();
                        // 返回机器的ip地址
                        clientPort = responsePacket.getPort();
                        // 返回机器端口
                        int i = 0;
                        while (i < 12)
                        {
                            if (i % 2 == 0)
                            {
                                dnsHeader.setHeader(i / 2, Utils.byteArrayToShort(packetData, i));

                            }

                            i++;
                        }
                        System.out.println("SEND to" + " 127.0.0.1:" + clientPort + " (" + responsePacket.getLength() + "Bytes) " + "[ID:" + dnsHeaderResponse.getTransID() + "]");
                        System.out.println(Utils.byteArrayToHexString(packetData));
                        System.out.println("ID:" + dnsHeader.getTransID() + " " + "QR:" + (((((int) (dnsHeader.getFlags())) << 16) >>> 31)) + " " + "opcode:" + (((((int) (dnsHeader.getFlags())) << 17) >>> 28)) +
                                " " + "AA:" + ((((int) (dnsHeader.getFlags()) << 21) >>> 31)) + " " + "TC:" + ((((int) (dnsHeader.getFlags()) << 22) >>> 31)) + " " + "RD:" + ((((int) (dnsHeader.getFlags()) << 23) >>> 31) +
                                " " + "RA:" + (((((int) (dnsHeader.getFlags())) << 24) >>> 31)) + " " + "RCODE:" + ((((int) (dnsHeader.getFlags())) << 28) >> 28)));
                    }
                    System.out.println(Thread.currentThread().getName() + " " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()) + " Client 127.0.0.1 " + "获得socket，响应DNS请求:" + dnsQuestion.getQueryName() + "  DNS查询结果:" + ip);
                    DNSRelayServer.getSocket().send(responsePacket);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        } else
        { // 本地未检索到，请求因特网DNS服务器
            System.out.println(Thread.currentThread().getName() + " " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()) + " Client 127.0.0.1 " + "Domain not found locally-→forward query");

            try
            {
                InetAddress dnsServerAddress = InetAddress.getByName(DNSIP);
                DatagramPacket internetSendPacket = new DatagramPacket(packetData, packetLength, dnsServerAddress, 53);
                DatagramSocket internetSocket = new DatagramSocket();
                internetSocket.send(internetSendPacket);
                byte[] receivedData = new byte[1024];
                DatagramPacket internetReceivedPacket = new DatagramPacket(receivedData, receivedData.length);
                internetSocket.receive(internetReceivedPacket);
                if (mode == 1)
                {
                    System.out.println("SEND to " + DNSIP + ":53" + " (" + packetLength + "Bytes) " + "[ID:" + dnsHeader.getTransID() + "]");
                    System.out.println(Utils.byteArrayToHexString(packetData));
                }
                // 回复响应数据包
                DatagramPacket responsePacket = new DatagramPacket(receivedData, internetReceivedPacket.getLength(), clientAddress, clientPort);
                internetSocket.close();
                synchronized (DNSRelayServer.Obj)
                // 使用synchronized关键字实现代码块的进程同步
                {
                    try
                    {
                        if (mode == 1)
                        {
                            packetLength = responsePacket.getLength();
                            packetData = new byte[packetLength];
                            System.arraycopy(responsePacket.getData(), 0, packetData, 0, responsePacket.getLength());
                            // 保存packet中的数据
                            clientAddress = responsePacket.getAddress();
                            // 返回机器的ip地址
                            clientPort = responsePacket.getPort();
                            // 返回机器端口
                            int i = 0;
                            while (i < 12)
                            {
                                if (i % 2 == 0)
                                {
                                    dnsHeader.setHeader(i / 2, Utils.byteArrayToShort(packetData, i));

                                }

                                i++;
                            }
                            System.out.println("RECEIVED from " + DNSIP + ":" + clientPort + " (" + responsePacket.getLength() + "Bytes) " + "[ID:" + receivedData[0] + receivedData[1] + "]");
                            System.out.println(Utils.byteArrayToHexString(packetData));
                            System.out.println("ID:" + dnsHeader.getTransID() + " " + "QR:" + (((((int) (dnsHeader.getFlags())) << 16) >>> 31)) + " " + "opcode:" + (((((int) (dnsHeader.getFlags())) << 17) >>> 28)) +
                                    " " + "AA:" + ((((int) (dnsHeader.getFlags()) << 21) >>> 31)) + " " + "TC:" + ((((int) (dnsHeader.getFlags()) << 22) >>> 31)) + " " + "RD:" + ((((int) (dnsHeader.getFlags()) << 23) >>> 31) +
                                    " " + "RA:" + (((((int) (dnsHeader.getFlags())) << 24) >>> 31)) + " " + "RCODE:" + ((((int) (dnsHeader.getFlags())) << 28) >> 28)));
                        }
                        System.out.println(Thread.currentThread().getName() + " " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()) + " Client 127.0.0.1 " + "获得socket，响应DNS请求:" + dnsQuestion.getQueryName());
                        DNSRelayServer.getSocket().send(responsePacket);
                        if (mode == 1)
                        {
                            System.out.println("SEND to" + " 127.0.0.1:" + responsePacket.getPort() + " (" + responsePacket.getLength() + "Bytes) " + "[ID:" + receivedData[0] + receivedData[1] + "]");
                        }
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }
}