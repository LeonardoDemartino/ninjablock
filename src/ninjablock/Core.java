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

import java.io.File;
import java.io.RandomAccessFile;

import org.bitcoinj.core.Utils;

public class Core {
	static String BLOCKCHAIN_PATH="C:\\blockchain\\";
	
	public static void main(String[] args) throws Exception {		
		//Get address data from a web service
		BTC_Address testAddress=BTC_Utils.getAddressData("1FrcEEdvdrzBbzCwt3LfrtFuvUcVeNsWZ2", "blockchain.info");
		System.out.println("The address have "+testAddress.balance+" satoshis");
		
		//Checks if a private key have a balance in his compressed and uncompressed address forms from a web service
		boolean haveBalance=BTC_Utils.checkIfThisPrivHaveBalance(Core_Utils.hexStringToByteArray("EFC253C8E3924B6AB0EB2AF377D6F3ECC7A5FB4D09B1678C22E91DD30F375D08"), "blockchain.info");
		if(haveBalance)System.out.println("One of the addresses of this private key have balance! A [ADDRESS].txt file was created.");
		else System.out.println("None of the addresses of this private key have balance");
		
		//Get a particular transaction from a web service
		byte[] trans=BTC_Utils.getTransactionFromWeb(Core_Utils.hexStringToByteArray("0b132058b3936f906c32214330b4c18d72c90bc39cbc27ac789bd94353f02775"), "blockchain.info");
		System.out.println("The transaction data is: "+Core_Utils.bytesToHex(trans));
				
		//Gets a address (BASE58+RIEPMD+SHA) from points in the plane
		String publicKey=BTC_Utils.getPublicKeyFromPoint("2f4d61dacb24debfe221e7de748f9667c373b717c1bed5bd14f4d0c129aa33ef", "e3dfb2eeabe9a3d17808e1c6f5de197914e8a62f551acb0244965b8ffb3313e6");
		System.out.println("Address from random public points: "+publicKey);
		
		//Process from blkxxxx.dat files
		ninja_readBlocksFromDisk(0,156, BLOCKCHAIN_PATH);

		System.out.println("Done...");
	}
	
	public static void ninja_readBlocksFromDisk(int startBlock, int endBlock, String path){
		if(startBlock>endBlock)return;
		//From disk
		try {
			for(int n_to_open=startBlock; n_to_open<=endBlock; n_to_open++){
				String blockPath=path+"blk"+String.format("%05d", n_to_open)+".dat";
				File check2=new File(blockPath);
				if(check2.exists()){
					String infoPath="blk"+String.format("%05d", n_to_open)+".txt";
					File check=new File(infoPath);
					if(check.exists())check.delete();
					RandomAccessFile out=new RandomAccessFile(infoPath, "rw");
					
					RandomAccessFile blockFile;
					blockFile = new RandomAccessFile(blockPath, "r");
					BTC_Data block=new BTC_Data(blockFile);
					boolean end=false;
			        
					while(!end){       
			            while(!end){
			            	byte id[]=new byte[4]; 
			            	int cRead=block.read(id);
			            	long cpos=block.getFilePointer();
				            if(cRead==-1)end=true;
				            if(!Core_Utils.equalsBytes(id, BTC_Utils.MAGIC_ID) && !end)block.seek(cpos+1);
				            else break;
			            }
			            
			            if(!end){
			            	//BLOCK START
			            	Core_Array head_to_hash = new Core_Array();
			                byte block_hash[]=new byte[32];
			                byte headerLength[]=new byte[4], versionNumber[]=new byte[4], timeStamp[]=new byte[4], difficulty[]=new byte[4], nonce[]=new byte[4];
			                byte hashPrev[]=new byte[32], hashMerkle[]=new byte[32], transLength=0, transIntegerType[]=new byte[1];
			                
			                block.read(headerLength);
			                block.read(versionNumber);
			                block.read(hashPrev);
			                block.read(hashMerkle);
			                block.read(timeStamp);
			                block.read(difficulty);
			                block.read(nonce);
			                
			                head_to_hash.addElement(versionNumber);
			                head_to_hash.addElement(hashPrev);
			                head_to_hash.addElement(hashMerkle);
			                head_to_hash.addElement(timeStamp);
			                head_to_hash.addElement(difficulty);
			                head_to_hash.addElement(nonce);
			                
			                //Dual SHA256
			                block_hash=Utils.singleDigest(Utils.singleDigest(Core_Utils.toByteArray(head_to_hash), 0, head_to_hash.size()),0,32);
			                
			        		BTC_Utils.checkIfThisPrivHaveBalance(Core_Utils.swapArray(block_hash), "blockchain.info");
			        		
			                if(BTC_Utils.LOG_FILE){
			                	out.writeBytes("Prev Block Hash: ");
			                	out.writeBytes(Core_Utils.bytesToHex(Core_Utils.swapArray(hashPrev))+Core_Utils.newline);
			                	
			                	out.writeBytes("Merkle Hash: ");
			                	out.writeBytes(Core_Utils.bytesToHex(Core_Utils.swapArray(hashMerkle))+Core_Utils.newline);
			                	
			                	out.writeBytes("Block Hash: ");
			                	out.writeBytes(Core_Utils.bytesToHex(Core_Utils.swapArray(block_hash))+Core_Utils.newline+Core_Utils.newline);
			                }
			                
			                //if(BTC_Utils.LOG_CONSOLE)System.out.println("Block "+Core_Utils.bytesToHex(Core_Utils.swapArray(block_hash))+" found @ block "+n_to_open);
			                
							/////////////////////////////////////////////////////////
							/////////////////////////////////////////////////////////
							///////////////////TRANSACTIONS//////////////////////////
							/////////////////////////////////////////////////////////
			                transLength=block.readByte();
							long transactionsCount=BTC_Utils.getVariableLengthData(transLength, block, transIntegerType);
			
			                for(long t=0; t<transactionsCount; t++){
			                	BTC_Transaction trans=new BTC_Transaction(block);
			                    if(trans.error){
			                    	end=true;
			                    	break;
			                    }
			                    //if(BTC_Utils.LOG_CONSOLE)System.out.println("- New Transaction found: "+Core_Utils.bytesToHex(Core_Utils.swapArray(trans.hash)));
			                    if(BTC_Utils.LOG_FILE)trans.printData(out);
			                }
			                if(BTC_Utils.LOG_FILE)out.writeBytes("End of block..."+Core_Utils.newline+Core_Utils.newline);
			            }
			        }
					out.close();
					block.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
