/*
The MIT License (MIT)
Copyright (c) 2016 Leonardo Demartino (delek.net)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ninjablock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.MainNetParams;

public class BTC_Utils {
	static final byte[] MAGIC_ID={(byte) 0xF9, (byte) 0xBE, (byte) 0xB4, (byte) 0xD9};
	static final boolean LOG_FILE=false;
	static final boolean LOG_CONSOLE=true;
	static final boolean SEARCH=true;

	static final boolean DEBUG=false;
	static final short OP_PUSHDATA1=0x4c;
	static final short OP_PUSHDATA2=0x4d;
	static final short OP_PUSHDATA4=0x4e;
	static final short OP_DUP=0x76;
	static final short OP_HASH160=0xa9;
	static final short DER_COMPOUND_STRUCT=0x30;
	static final short DER_INTEGER=0x02;

	static final short INTEGER_TYPE_0=0xfc;
	static final short INTEGER_TYPE_2=0xfd;
	static final short INTEGER_TYPE_4=0xfe;
	static final short INTEGER_TYPE_8=0xff;
	
	static String getPublicKeyFromPoint(String point){
		byte [] final_address=new byte[25];
		byte [] raw=new byte[1+32];
		byte [] pointArray=Core_Utils.hexStringToByteArray(point);
		byte [] address=new byte[21];
		
		raw[0]=0x03;	//COMPRESSED
		System.arraycopy(pointArray, 0, raw, 1+(32-pointArray.length), pointArray.length);
	
		address[0]=0x00;	//NETWORK ID
		System.arraycopy(Utils.sha256hash160(raw), 0, address, 1, 20);

		System.arraycopy(address, 0, final_address, 0, 21);
		
		byte [] checksum=Utils.singleDigest(Utils.singleDigest(address, 0, address.length), 0, 32);
		System.arraycopy(checksum, 0, final_address, 21, 4);
		
		return Base58.encode(final_address);
	}
	
	static String getPublicKeyFromPoint(String pointA, String pointB){
		byte [] final_address=new byte[25];
		byte [] raw=new byte[1+32+32];
		byte [] pointArray_A=Core_Utils.hexStringToByteArray(pointA);
		byte [] pointArray_B=Core_Utils.hexStringToByteArray(pointB);
		byte [] address=new byte[21];
		
		raw[0]=0x04;	//NOT COMPRESSED
		System.arraycopy(pointArray_A, 0, raw, 1, pointArray_A.length);
		System.arraycopy(pointArray_B, 0, raw, 1+32, pointArray_B.length);
		
		address[0]=0x00;	//NETWORK ID
		System.arraycopy(Utils.sha256hash160(raw), 0, address, 1, 20);

		System.arraycopy(address, 0, final_address, 0, 21);
		
		byte [] checksum=Utils.singleDigest(Utils.singleDigest(address, 0, address.length), 0, 32);
		System.arraycopy(checksum, 0, final_address, 21, 4);
		
		return Base58.encode(final_address);
	}
	
	public static BTC_Address getAddressData(String publicKey, String server){
		try{			
			String data_get="", balanceString="", n_txString="";
			int balanceIndex=-1, n_txIndex=-1;
			BTC_Address addr=new BTC_Address();
			
			if(server.equals("blockchain.info")){
				data_get=Core_Utils.readUrl("https://blockchain.info/address/"+publicKey+"?format=json&limit=0");
				balanceString="\"final_balance\":";
				n_txString="\"n_tx\":";
			}
			else if(server.equals("blockcypher.com")){
				data_get=Core_Utils.readUrl("http://api.blockcypher.com/v1/btc/main/addrs/"+publicKey+"/balance");
				balanceString="\"final_balance\": ";
				n_txString="\"n_tx\": ";
			}
			else System.out.println("ERROR: server "+server+" not found.");
			
			balanceIndex=data_get.indexOf(balanceString);
			n_txIndex=data_get.indexOf(n_txString);
			if(balanceIndex==-1 || n_txIndex==-1){
				addr.balance=0;
				addr.n_tx=0;
			}
			else{
				addr.balance=	Long.parseLong(data_get.substring(balanceIndex+balanceString.length()	, data_get.indexOf(",", balanceIndex)));
				addr.n_tx=		Long.parseLong(data_get.substring(n_txIndex+n_txString.length()		, data_get.indexOf(",", n_txIndex)));
			}
			
			return addr;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	static byte[] getTransactionFromWeb(byte []txs, String server){
		byte []data_get=null;
		
		String raw_data;
		try{
			if(server.equals("blockchain.info")){
				raw_data=Core_Utils.readUrl("https://blockchain.info/tx/"+Core_Utils.bytesToHex(txs)+"?format=hex");
				data_get=new byte[raw_data.length()/2];
				data_get=Core_Utils.parseHexBinary(raw_data);
			}
			else System.out.println("ERROR: server "+server+" not found.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if(DEBUG)
			System.out.println("Data from: "+server+" txs "+Core_Utils.bytesToHex(txs)+" = "+raw_data);

		return data_get;
	}
	
	public static boolean checkIfThisPrivHaveBalance(byte [] priv, String server){
		boolean ret=false;
		try {
			if(priv.length>32)return false;
			
			byte [] privateKey = priv;
			
			for(int i=0; i<2; i++){
				String publicKey ="";
				if(i==0)publicKey = new Address(MainNetParams.get(), Utils.sha256hash160(ECKey.fromPrivate(privateKey).getPubKey())).toString();
				else publicKey = new Address(MainNetParams.get(), Utils.sha256hash160(ECKey.fromPrivate(privateKey).decompress().getPubKey())).toString();
				
				BTC_Address addr=BTC_Utils.getAddressData(publicKey, server);
				if(addr.balance>0 || addr.n_tx>0){
					File f=new File(publicKey+".txt");
					if(!f.exists())f.createNewFile();
						
					BufferedWriter output=new BufferedWriter(new FileWriter(f));
					output.write( "Pub: "+publicKey+" Priv: "+Core_Utils.bytesToHex(privateKey)+" Balance: "+addr.balance+" N_tx: "+addr.n_tx+(i==0?" Compressed":" Decompressed") );
					output.newLine();
					output.close();
					ret=true;
				}
				else System.out.println("Pub: "+publicKey+" Priv: "+Core_Utils.bytesToHex(privateKey)+(i==0?" Compressed":" Decompressed"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public static int getPushData(byte []data, int index, int pushCount[]) {
		pushCount[0]=0;
		switch(data[index]){
	        case BTC_Utils.OP_PUSHDATA1:
	        	index++;
	        	pushCount[0]=data[index];
	            return 1;
	        case BTC_Utils.OP_PUSHDATA2:
	            for(int i=0; i<2; i++){
	            	index++;
	            	pushCount[0]|=(data[index]&0xFF)<<(i*8);
	            }
	            return 2;
	        case BTC_Utils.OP_PUSHDATA4:
	            for(int i=0; i<4; i++){
	            	index++;
	            	pushCount[0]|=(data[index]&0xFF)<<(i*8);
	            }
	            return 4;
	        default:
	        	pushCount[0]=data[index];
	            return 0;
	    }
	}
	
	public static long getVariableLengthData(byte lengtho, BTC_Data block, byte[] type) {
		long out=0;
		short length=Core_Utils.toUnsigned(lengtho);

		if(length<=INTEGER_TYPE_0){
			type[0]=(byte)INTEGER_TYPE_0;
			out=length;
		}
		else if(length==INTEGER_TYPE_2){
			type[0]=(byte) INTEGER_TYPE_2;
			out|=(block.read()&0xFF);
			out|=(block.read()&0xFF)<<8;
		}
		else if(length==INTEGER_TYPE_4){
			type[0]=(byte) INTEGER_TYPE_4;
			out|=(block.read()&0xFF);
			out|=(block.read()&0xFF)<<8;
			out|=(block.read()&0xFF)<<16;
			out|=(block.read()&0xFF)<<24;
		}
		else if(length==INTEGER_TYPE_8){
			type[0]=(byte) INTEGER_TYPE_8;
			out|=(block.read()&0xFF);
			out|=(block.read()&0xFF)<<8;
			out|=(block.read()&0xFF)<<16;
			out|=(block.read()&0xFF)<<24;
			out|=(block.read()&0xFF)<<32;
			out|=(block.read()&0xFF)<<40;
			out|=(block.read()&0xFF)<<48;
			out|=(block.read()&0xFF)<<56;
		}
		else{
			if(LOG_CONSOLE)System.out.println("Error: getVariableLengthData Unknown size");
			return 0;
		}

		return out;
	}
	
	public static byte[] setVariableLengthData(short VLI, long data){
		if(VLI==BTC_Utils.INTEGER_TYPE_2){
			byte lengthFull[]=new byte[2];
			lengthFull[0]=(byte) (data&0xFF);
			lengthFull[1]=(byte) ((data>>8)&0xFF);
			return lengthFull;
		}
		else if(VLI==BTC_Utils.INTEGER_TYPE_4){
			byte lengthFull[]=new byte[4];
			lengthFull[0]=(byte) (data&0xFF);
			lengthFull[1]=(byte) ((data>>8)&0xFF);
			lengthFull[2]=(byte) ((data>>16)&0xFF);
			lengthFull[3]=(byte) ((data>>24)&0xFF);
			return lengthFull;
		}
		else if(VLI==BTC_Utils.INTEGER_TYPE_8){
			byte lengthFull[]=new byte[8];
			lengthFull[0]=(byte) (data&0xFF);
			lengthFull[1]=(byte) ((data>>8)&0xFF);
			lengthFull[2]=(byte) ((data>>16)&0xFF);
			lengthFull[3]=(byte) ((data>>24)&0xFF);
			lengthFull[4]=(byte) ((data>>32)&0xFF);
			lengthFull[5]=(byte) ((data>>40)&0xFF);
			lengthFull[6]=(byte) ((data>>48)&0xFF);
			lengthFull[7]=(byte) ((data>>56)&0xFF);
			return lengthFull;
		}
		return null;
	}
}
