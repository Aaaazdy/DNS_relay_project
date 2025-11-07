package com.dnsrelay;

public class DNSHeader{
    private short transID;
    private short flag;
    private short qdcount;
    private short ancount;
    private short nscount;
    private short arcount;

    public DNSHeader(){}

    public DNSHeader(short transID, short flag, short qdcount, short ancount, short nscount, short arcount){
        this.transID = transID;
        this.flag = flag;
        this.qdcount = qdcount;
        this.ancount = ancount;
        this.nscount = nscount;
        this.arcount = arcount;
    }

    public short getTransID(){
        return transID;
    }

    public void setTransID(short transID){
        this.transID = transID;
    }

    public short getFlags(){
        return flag;
    }

    public void setFlags(short flag){
        this.flag = flag;
    }

    public short getQDcount(){
        return qdcount;
    }

    public void setQDcount(short qdcount){
        this.qdcount = qdcount;
    }

    public short getANcount(){
        return ancount;
    }

    public void setANcount(short ancount){
        this.ancount = ancount;
    }

    public short getNScount(){
        return nscount;
    }

    public void setNScount(short nscount){
        this.nscount = nscount;
    }

    public short getARcount(){
        return arcount;
    }

    public void setARcount(short arcount){
        this.arcount = arcount;
    }

    public void setHeader( int index, short config){
        if (index == 0)
        {//对应setTransID
            setTransID(config);
        } else if (index == 1)
        {//对应setFlags
            setFlags(config);
        } else if (index == 2)
        {//对应setQdcount
            setQDcount(config);
        } else if (index == 3)
        {//对应setAncount
            setANcount(config);
        } else if (index == 4)
        {//对应setNscount
            setNScount(config);
        } else if (index == 5)
        {//对应setArcount
            setARcount(config);
        }
    }
    public byte[] toByteArray(){
        byte[] datagram = new byte[12];
        byte[] new2Byte = new byte[2];
        int offset = 0;

        new2Byte = Utils.shortToByteArray(transID);
        for(int i = 0; i < 2; i ++){
            datagram[offset ++] = new2Byte[i];
        }
        new2Byte = Utils.shortToByteArray(flag);
        for(int i = 0; i < 2; i ++){
            datagram[offset ++] = new2Byte[i];
        }
        new2Byte = Utils.shortToByteArray(qdcount);
        for(int i = 0; i < 2; i ++){
            datagram[offset ++] = new2Byte[i];
        }
        new2Byte = Utils.shortToByteArray(ancount);
        for(int i = 0; i < 2; i ++){
            datagram[offset ++] = new2Byte[i];
        }
        new2Byte = Utils.shortToByteArray(nscount);
        for(int i = 0; i < 2; i ++){
            datagram[offset ++] = new2Byte[i];
        }
        new2Byte = Utils.shortToByteArray(arcount);
        for(int i = 0; i < 2; i ++){
            datagram[offset ++] = new2Byte[i];
        }
        return datagram;
    }
}