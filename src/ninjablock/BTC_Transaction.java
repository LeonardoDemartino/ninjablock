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

import java.io.IOException;
import java.io.RandomAccessFile;

import org.bitcoinj.core.Utils;

public class BTC_Transaction {
	byte version[]=new byte[4], lockTime[]=new byte[4];
	byte hash[]=new byte[32];
	boolean error=false;
	
	long inputsCount;
	byte inputRawLength, inputRawLengthVLI[]=new byte[1];
	BTC_Input input[];
	
	long outputsCount;
	byte outputRawLength, outputRawLengthVLI[]=new byte[1];
	BTC_Output output[];
	
	BTC_Transaction(BTC_Data data){
		this.getData(data);
	}
	
	void getData(BTC_Data block){
		try{			
			block.read(this.version);
	        
	        //INPUTS!!!!
	        this.inputRawLength=block.readByte();
	        this.inputsCount=BTC_Utils.getVariableLengthData(this.inputRawLength, block, this.inputRawLengthVLI);
	        	        
	        this.input=new BTC_Input[(int) this.inputsCount];
	        
	        for(int i=0; i<this.inputsCount; i++){
	        	this.input[i]=new BTC_Input();
	        	
	        	block.read(this.input[i].hash);
	        	this.input[i].index=(block.readByte()<<24)|(block.readByte()<<16)|(block.readByte()<<8)|block.readByte();
	
	        	//System.out.println(Core_Utils.bytesToHex(Core_Utils.swapArray(this.input[i].hash))+" "+Core_Utils.swapInt(this.input[i].index));
	            //SCRIPT
	            this.input[i].script.rawLength=block.readByte();
	            this.input[i].script.length=BTC_Utils.getVariableLengthData(this.input[i].script.rawLength, block, this.input[i].script.rawLengthVLI);
	            if(this.input[i].script.length>0){
		            this.input[i].script.data=new byte[(int) this.input[i].script.length];
		            block.read(this.input[i].script.data);
		            getScriptSig(this.input[i]);
	            }            
		            
	            //SCRIPT NUMBER (ALWAYS 0xFFFFFFFF?)
	            this.input[i].script.number=block.readInt();		//GUARDA CON ESTE?, ENDIANESS?
	        }
	        
	        //OUTPUTS!!!!
	        this.outputRawLength=block.readByte();
	        this.outputsCount=BTC_Utils.getVariableLengthData(this.outputRawLength, block, this.outputRawLengthVLI);
	        	        
	        this.output=new BTC_Output[(int) this.outputsCount];
	        
	        for(int o=0; o<this.outputsCount; o++){
	        	this.output[o]=new BTC_Output();
	        	
	        	for(int s=0; s<8; s++)
	        		this.output[o].satoshis[s]=block.readByte();

	            //SCRIPT
	        	this.output[o].script.rawLength=block.readByte();
	        	this.output[o].script.length=BTC_Utils.getVariableLengthData(this.output[o].script.rawLength, block, this.output[o].script.rawLengthVLI);
	
	        	if(this.output[o].script.length>0){
		        	this.output[o].script.data=new byte[(int) this.output[o].script.length];
		            block.read(this.output[o].script.data);
		            getScriptPubKey(this.output[o]);
	        	}
	        }

	        block.read(this.lockTime);
	        
	        calculateHash();
		}
		catch(Exception e){
			e.printStackTrace();
			this.error=true;
		}
		
		this.error=false;
	}
	
	public int calculateHash(){
		Core_Array to_hash = new Core_Array();
		short VLI=0;
		long VLI_data=0;
		
		//INIT
		to_hash.addElement(this.version);
		to_hash.addElement(this.inputRawLength);
		
		/////////////////////////////////////////////
		VLI=Core_Utils.toUnsigned(this.inputRawLengthVLI[0]);
		VLI_data=this.inputsCount;
		if(VLI!=BTC_Utils.INTEGER_TYPE_0){
			byte lengthFull[]=BTC_Utils.setVariableLengthData(VLI, VLI_data);
			to_hash.addElement(lengthFull);			
		}
		/////////////////////////////////////////////
		//INPUTS
		for(int n=0; n<this.inputsCount; n++){
			to_hash.addElement(this.input[n].hash);
			to_hash.addElement(this.input[n].index);

			to_hash.addElement(this.input[n].script.rawLength);
			/////////////////////////////////////////////
			VLI=Core_Utils.toUnsigned(this.input[n].script.rawLengthVLI[0]);
			VLI_data=this.input[n].script.length;
			if(VLI!=BTC_Utils.INTEGER_TYPE_0){
				byte lengthFull[]=BTC_Utils.setVariableLengthData(VLI, VLI_data);
				to_hash.addElement(lengthFull);
			}
			/////////////////////////////////////////////
			if(this.input[n].script.length>0)
				to_hash.addElement(this.input[n].script.data);
			to_hash.addElement(this.input[n].script.number);
		}

		//OUTPUTS
		to_hash.addElement(this.outputRawLength);
		/////////////////////////////////////////////
		VLI=Core_Utils.toUnsigned(this.outputRawLengthVLI[0]);
		VLI_data=this.outputsCount;
		if(VLI!=BTC_Utils.INTEGER_TYPE_0){
			byte lengthFull[]=BTC_Utils.setVariableLengthData(VLI, VLI_data);
			to_hash.addElement(lengthFull);			
		}
		/////////////////////////////////////////////
		for(int n=0; n<this.outputsCount; n++){
			to_hash.addElement(this.output[n].satoshis);

			to_hash.addElement(this.output[n].script.rawLength);
			/////////////////////////////////////////////
			VLI=Core_Utils.toUnsigned(this.output[n].script.rawLengthVLI[0]);
			VLI_data=this.output[n].script.length;
			if(VLI!=BTC_Utils.INTEGER_TYPE_0){
				byte lengthFull[]=BTC_Utils.setVariableLengthData(VLI, VLI_data);
				to_hash.addElement(lengthFull);				
			}
			/////////////////////////////////////////////
			if(this.output[n].script.length>0)
				to_hash.addElement(this.output[n].script.data);	
		}

		//EXTRA
		to_hash.addElement(this.lockTime);	
		
		this.hash=Utils.singleDigest(Utils.singleDigest(Core_Utils.toByteArray(to_hash), 0, to_hash.size()),0,32);

		return 0;
	}
	
	static void getScriptPubKey(BTC_Output out){
	    int index=0, pushCount[]={0};
	    if(out.script.data.length==0)return;
	    if(out.script.data[index]==BTC_Utils.OP_DUP){
	    	index++;
	    	if(index>=out.script.length)return;
	        if(out.script.data[index]==BTC_Utils.OP_HASH160){
	        	index++;
	        	out.script.pushType1=out.script.data[index];
	        	
				/////////////////////////////////////////////////////////
				index+=BTC_Utils.getPushData(out.script.data, index, pushCount);
				////////////////////////////////////////////////////////
				
	            index++;

	            out.script.pubKeySize=pushCount[0];
	            out.script.pubKey=new byte[out.script.pubKeySize];
	            Core_Utils.byteCopy(out.script.data,out.script.pubKey,index,0, out.script.pubKeySize);
	        }
	    }
	}
	
	static void getScriptSig(BTC_Input in){
	    int index=0, pushCount[]={0};
	    byte rsSize=0;
	    in.script.pushType1=in.script.data[index];
	    
		/////////////////////////////////////////////////////////
		index+=BTC_Utils.getPushData(in.script.data, index, pushCount);
		////////////////////////////////////////////////////////
		
	    index++;
	    if(index>=in.script.data.length)return;
	    if(in.script.data[index]==BTC_Utils.DER_COMPOUND_STRUCT){
	    	index++;
	    	if(index>=in.script.data.length)return;
	    	
	        rsSize=in.script.data[index];
	        if(BTC_Utils.DEBUG)System.out.println("rsSize "+rsSize);
	        index++; 
	        if(index>=in.script.data.length)return;
	        
	        if(in.script.data[index]==BTC_Utils.DER_INTEGER){ //r
	        	index++;
	        	in.rSize=in.script.data[index];
	            in.r=new byte[in.rSize];
	            index++;	            
	            Core_Utils.byteCopy(in.script.data, in.r, index, 0, in.rSize);
	            //////////////////////
	            index+=in.rSize;
	            if(in.script.data[index]==BTC_Utils.DER_INTEGER){ //s
	            	index++;
	                in.sSize=in.script.data[index];
	                in.s=new byte[in.sSize];
	                index++;
	                Core_Utils.byteCopy(in.script.data,in.s,index, 0, in.sSize);
	                //////////////////////	                
	                index+=rsSize-(1+1+in.rSize+1+1);
	                in.script.hashType=in.script.data[index];
	                index++;
	                //PUBKEY
	                if(index>=in.script.length)return;
	                in.script.pushType2=in.script.data[index];
	                
	                /////////////////////////////////////////////////////////
	                index+=BTC_Utils.getPushData(in.script.data, index, pushCount);
	                 ////////////////////////////////////////////////////////
	                
	                in.script.pubKeySize=pushCount[0];
	                in.script.pubKey=new byte[in.script.pubKeySize];
	                
	                index++;
	                if(index>=in.script.length)return;
	                if(index+in.script.pubKeySize<=in.script.data.length)
	                	Core_Utils.byteCopy(in.script.data,in.script.pubKey,index, 0, in.script.pubKeySize);
	                else System.out.println("Error!, pubkeysize too big for script! "+in.script.pubKeySize);
	            }
	        }
	    }
	}
	
	public void printData(RandomAccessFile out){
		try {
			///PRINT!!!
			out.writeBytes(" - Transaction "+Core_Utils.newline);
			
			for(int i=0; i<this.inputsCount; i++)
				out.writeBytes(" - - - Input index "+Core_Utils.swapInt(this.input[i].index)+" Prev tx: "+Core_Utils.bytesToHex(Core_Utils.swapArray(this.input[i].hash))+" from output "+Core_Utils.swapInt(this.input[i].index)+" Script: "+Core_Utils.bytesToHex(this.input[i].script.data)+Core_Utils.newline);
			
			for(int o=0; o<this.outputsCount; o++){
	        	//To BigInteger endianess swap
	        	long satoshis= Core_Utils.arrayToLong(Core_Utils.swapArray(this.output[o].satoshis));
            	out.writeBytes(" - - - Output "+o+" Sathoshis "+satoshis+" Script: "+Core_Utils.bytesToHex(this.output[o].script.data)+Core_Utils.newline);
			}
			
			out.writeBytes(Core_Utils.newline);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}