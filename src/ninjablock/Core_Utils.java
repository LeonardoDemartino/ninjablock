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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Core_Utils {
	static String newline = "\r\n";

	public static boolean deleteFolder(File dir){
		if(dir.exists()){
			if(dir.isDirectory()){
				String[] children = dir.list();
				for (int i=0; i<children.length; i++) {
					boolean success = deleteFolder(new File(dir, children[i]));

					if (!success) {
						return false;
					}

				}

			}

			return dir.delete();
		}
		return true;
	}
    
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	static byte[] swapArray(byte s[]){
		byte[] aux = new byte[s.length];

		for(int i=0; i<s.length; i++){
			aux[i]=s[s.length-i-1];
		}
		return aux;
	}

	public static int swapInt(int value) {
        return
            ( ( ( value >> 0 ) & 0xff ) << 24 ) +
            ( ( ( value >> 8 ) & 0xff ) << 16 ) +
            ( ( ( value >> 16 ) & 0xff ) << 8 ) +
            ( ( ( value >> 24 ) & 0xff ) << 0 );
    }
	
	public static byte[] toByteArray(List<Byte> in) {
	    final int n = in.size();
	    byte ret[] = new byte[n];
	    for (int i = 0; i < n; i++) {
	        ret[i] = in.get(i);
	    }
	    return ret;
	}

	static boolean equalsFirstBytes(byte a[], byte b[], int length){
		if(a==null || b==null)return false;

		for(int i=0; i<length; i++){
			if(a[i]!=b[i])return false;
		}
		return true;
	}

	static boolean equalsBytes(byte a[], byte b[]){
		if(a==null || b==null || a.length!=b.length)return false;

		for(int i=0; i<a.length; i++){
			if(a[i]!=b[i])return false;
		}
		return true;
	}
	
	static int containsByteArray(ArrayList<byte[]> list, byte[] elem){
		for (int i=0; i<list.size(); i++)
	        if (equalsBytes(list.get(i), elem)) return i;
	    return -1;
	}
	
	static long arrayToLongLittleEndian(byte by[]){
		long value=0;
		for (int i = 0; i < by.length; i++)
		{
		   value += ((long) by[i] & 0xffL) << (8 * i);
		}
		return value;
	}
	
	static long arrayToLong(byte by[]){
		long value=0;
		for (int i = 0; i < by.length; i++)
		{
		   value = (value << 8) + (by[i] & 0xff);
		}
		return value;
	}
	
	static int byteCopy(byte source[], byte destiny[], int pos_source, int pos_destiny, int length){
		System.arraycopy(source, pos_source, destiny, pos_destiny,length);
		return source.length;
	}
	
	static int byteCopy(byte source, byte destiny[], int pos_source, int pos_destiny, int length){
		byte sourcea[]={source};
		return byteCopy(sourcea, destiny, pos_source, pos_destiny, length);
	}
	
	static int byteCopy(int source, byte destiny[], int pos_source, int pos_destiny, int length){
		byte sourcea[]={(byte) ((source&0xFF000000)>>24), (byte) ((source&0x00FF0000)>>16), (byte) ((source&0x0000FF00)>>8), (byte) ((source&0x000000FF)>>0)};
		return byteCopy(sourcea, destiny, pos_source, pos_destiny, length);
	}

	public static String bytesToHex(byte[] bytes) {
		final char[] hexArray = "0123456789abcdef".toCharArray();
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j*2+0] = hexArray[(v&0xF0)>>>4];
			hexChars[j*2+1] = hexArray[(v&0x0F)>>>0];
		}
		return new String(hexChars);
	}
	
	public static String bytesToHex(byte[] bytes, int length) {
		final char[] hexArray = "0123456789abcdef".toCharArray();
		char[] hexChars = new char[length * 2];
		for ( int j = 0; j < length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j*2+0] = hexArray[(v&0xF0)>>>4];
			hexChars[j*2+1] = hexArray[(v&0x0F)>>>0];
		}
		return new String(hexChars);
	}
	
	public static byte[] hexToBytes(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	public static void fileString(RandomAccessFile out, String str){
		try {
			for(int i=0; i<str.length(); i++)
				out.writeChar(str.charAt(i));
		} catch (IOException e) {
		}
	}

	public static short toUnsigned(byte b) { 
		return (short)(b & 0xff);
	}
	
	static String readUrl(String urlString) throws Exception {
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    }
	    catch (Exception e){
	    	System.out.println("Error getting: "+urlString+"     Retrying...");
	    	return readUrl(urlString);
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	}
	
	private static int hexToBin( char ch ) {
	    if( '0'<=ch && ch<='9' )    return ch-'0';
	    if( 'A'<=ch && ch<='F' )    return ch-'A'+10;
	    if( 'a'<=ch && ch<='f' )    return ch-'a'+10;
	    return -1;
	}
	
	public static byte[] parseHexBinary(String s) {
	    final int len = s.length();

	    // "111" is not a valid hex encoding.
	    if( len%2 != 0 )
	        throw new IllegalArgumentException("Error: the hex string needs to be even in length: "+s);

	    byte[] out = new byte[len/2];

	    for( int i=0; i<len; i+=2 ) {
	        int h = hexToBin(s.charAt(i  ));
	        int l = hexToBin(s.charAt(i+1));
	        if( h==-1 || l==-1 )
	            throw new IllegalArgumentException("Error: the hex string contains illegal characters: "+s);

	        out[i/2] = (byte)(h*16+l);
	    }

	    return out;
	}
}

