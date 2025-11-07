package com.dnsrelay;

public class DNSQuestion{
	private String queryName;
	private short queryType;
	private short queryClass;

	public String getQueryName(){
		return queryName;
	}

	public void setQueryName(String qname){
		this.queryName = qname;
	}

	public short getQueryType(){
		return queryType;
	}

	public void setQueryType(short qtype){
		this.queryType = qtype;
	}

	public short getQueryClass(){
		return queryClass;
	}

	public void setQueryClass(short qclass){
		this.queryClass = qclass;
	}

	public byte[] toByteArray(){
		byte[] datagram = new byte[this.queryName.length() + 2 + 4];
		int offset = 0;
		byte[] domainByteArray = Utils.domainToByteArray(this.queryName);
		for (byte b : domainByteArray) {
			datagram[offset ++] = b;
		}
		byte[] new2Bytes = new byte[2];
		new2Bytes = Utils.shortToByteArray(this.queryType);
		for(int i = 0; i < 2; i ++) {
			datagram[offset ++] = new2Bytes[i];
		}
		new2Bytes = Utils.shortToByteArray(this.queryClass);
		for(int i = 0; i < 2; i ++) {
			datagram[offset ++] = new2Bytes[i];
		}
		return datagram;
	}
}
