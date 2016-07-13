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

public class BTC_Data {
	RandomAccessFile disk=null;
	Core_Array memory = new Core_Array();
	int index=0;
	boolean onDisk=false;
	
	BTC_Data(RandomAccessFile disk){
		this.disk=disk;
		onDisk=true;
	}
	
	BTC_Data(Core_Array memory){
		this.memory=memory;
		onDisk=false;
	}
	
	long getFilePointer(){
		if(onDisk)
			try {
				return disk.getFilePointer();
			} catch (IOException e) {
				e.printStackTrace();
				return 0;
			}
		else return 0;
	}
	
	int read(byte r[]){
		if(onDisk){
			try {
				return disk.read(r);
			} catch (IOException e) {
				e.printStackTrace();
				return 0;
			}
		}
		else{
			for(int i=0; i<r.length; i++)
				r[i]=memory.get(index+i);
			index+=r.length;
			
			return 0;
		}
	}
	
	int read(){
		if(onDisk){
			try {
				return disk.read();
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
		}
		else{
			int ret=memory.get(index);
			index++;
			return ret;
		}
	}

	byte readByte(){
		if(onDisk){
			try {
				return disk.readByte();
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
		}
		else{
			byte ret=memory.get(index);
			index++;
			return ret;
		}
	}
	
	int readInt(){
		if(onDisk){
			try {
				return disk.readInt();
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
		}
		else{
			int ret=(memory.get(index)<<24)|(memory.get(index+1)<<16)|(memory.get(index+2)<<8)|(memory.get(index)<<0);
			index+=4;
			return ret;
		}
	}
	
	long readLong(){
		if(onDisk){
			try {
				return disk.readLong();
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
		}
		else{
			long ret=(memory.get(index)<<56)|(memory.get(index+1)<<48)|(memory.get(index+2)<<40)|(memory.get(index+3)<<32)|(memory.get(index+4)<<24)|(memory.get(index+5)<<16)|(memory.get(index+6)<<8)|(memory.get(index+7)<<0);
			index+=8;
			return ret;
		}
	}

	public void seek(long l) {
		if(onDisk)
			try {
				disk.seek(l);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public void close() {
		if(onDisk)
			try {
				disk.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
	}
}
