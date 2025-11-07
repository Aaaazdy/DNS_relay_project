package com.dnsrelay;

public class DNSRR {
	/**
	 * Answer/Authority/Additional
	 0  1  2  3  4  5  6  7  0  1  2  3  4  5  6  7
	 +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 |					   ... 						  |
	 |                    NAME                       |
	 |                    ...                        |
	 +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 |                    TYPE                       |
	 +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 |                    CLASS                      |
	 +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 |                    TTL                        |
	 |                                               |
	 +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 |                    RDLENGTH                   |
	 +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 |                    ...                        |
	 |                    RDATA                      |
	 |                    ...                        |
	 +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 */
	
	/*NAME (2 bytes using message compression),TYPE (2 bytes), CLASS (2 bytes), TTL (4 bytes), LENGTH (2 bytes),
	RDATA IPv4 is 4 bytes*/
	private short name;
	private short type;
	private short class1;
	private int ttl;
	private short length;
	private String rd;
	private byte[] result;
	private int offset = 0;
	byte[] b2 = new byte[2];
	byte[] b4 = new byte[4];

	public DNSRR() {}

	public DNSRR(short name, short type, short class1, int ttl, short length, String rd) {
		this.name = name;
		this.type = type;
		this.class1 = class1;
		this.ttl = ttl;
		this.length = length;
		this.rd = rd;
		this.result = new byte[12 + length];
	}

	public short getAname() {
		return name;
	}
	//set and get information from this class
	public void setAname(short name) {
		this.name = name;
	}

	public short getAtype() {
		return type;
	}

	public void setAtype(short type) {
		this.type = type;
	}

	public short getAclass() {return class1;}

	public void setAclass(short class1) {this.class1 = class1;}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public short getRdlength() {
		return length;
	}

	public void setRdlength(short length) {
		this.length = length;
	}

	public String getRdata() {
		return rd;
	}

	public void setRdata(String rd) {
		this.rd = rd;
	}

	//load the information
	public byte[] toByteArray() {
		//byte[] result = new byte[12 + length];
		//int offset = 0;

		//store information in result and return
		store1(name);
		store1(type);
		store1(class1);
		store2(ttl);
		store1(length);
		if (length == 4) {
			store3(rd);
		}

		//store information into result
	/*	for (int i=0; i<2; i++) {
			result[offset++] = Utils.shortToByteArray(name)[i];
		}

        for (int i=0; i<2; i++) {
            result[offset++] = Utils.shortToByteArray(type)[i];
        }

        for (int i=0; i<2; i++) {
            result[offset++] = Utils.shortToByteArray(class1)[i];
        }

        for (int i=0; i<4; i++) {
            result[offset++] = Utils.intToByteArray(ttl)[i];
        }

        for (int i=0; i<2; i++) {
            result[offset++] = Utils.shortToByteArray(length)[i];
        }

        if (length == 4) {
            for (int i=0; i<4; i++) {
                result[offset++] = Utils.ipv4ToByteArray(rd)[i];
            }
        }*/

		return result;

	}
	// method of store short format
	private void store1(short a){
		b2 = Utils.shortToByteArray(a);
		for (int i=0; i<2; i++) {
			result[offset++] = b2[i];
		}
	}

	// method of store int format
	private void store2(int a){
		b4 = Utils.intToByteArray(a);
		for (int i=0; i<4; i++) {
			result[offset++] = b4[i];
		}
	}

	//method of store string format
	private void store3(String a){
		b4=Utils.ipv4ToByteArray(a);
		for (int i=0; i<4; i++) {
			result[offset++] = b4[i];
		}
	}

}