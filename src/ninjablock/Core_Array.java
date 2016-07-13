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

import java.util.ArrayList;

public class Core_Array extends ArrayList<Byte>{
	private static final long serialVersionUID = 1L;

	void addElement(byte []data){
		for(int i=0; i<data.length; i++)
			this.add(data[i]);
	}
	
	void addElement(byte data){
		this.add(data);
	}
	
	void addElement(int data){
		this.add((byte) (((data&0xFF000000)>>24)&0xFF));
		this.add((byte) (((data&0x00FF0000)>>16)&0xFF));
		this.add((byte) (((data&0x0000FF00)>>8)&0xFF));
		this.add((byte) (((data&0x000000FF)>>0)&0xFF));
	}
}
