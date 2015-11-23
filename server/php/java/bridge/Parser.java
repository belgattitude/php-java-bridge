/*-*- mode: Java; tab-width:8 -*-*/

package php.java.bridge;

/*
 * Copyright (C) 2003-2007 Jost Boekemeier
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER(S) OR AUTHOR(S) BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.SocketException;

class Parser {
    static final int RECV_SIZE = 8192; // initial size of the receive buffer
    static final int MAX_ARGS = 100; // max # of method arguments
    static final int SLEN = 256; // initial length of the parser string

    static final short OK=0, PING=1, EOF=2, IO_ERROR=3;    // parse return codes

    IDocHandler handler;
    JavaBridge bridge;
    Parser(JavaBridge bridge, IDocHandler handler) {
        this.bridge = bridge;
	this.handler=handler;
	tag=new ParserTag[]{new ParserTag(1), new ParserTag(MAX_ARGS), new ParserTag(MAX_ARGS) };
    }
    void initOptions (byte ch) {
	bridge.options = new Options();
        if((ch&64)!=0) bridge.options.updateOptions((byte) (ch&3));
    	if((ch&128)!=0) {
    	    if(bridge.logLevel>3 && (bridge.logLevel!=((ch>>2)&7)))
	        bridge.logDebug("Client changed its request log level to: " + ((ch>>2)&7));
	            
    	    bridge.logLevel = (ch>>2)&7;
    	}
    }
    short initOptions(InputStream in, OutputStream out) throws IOException {
	if((clen=read(in)) >0) { 

	    /*
	     * Special handling if the first byte is neither a space nor "<"
	     * 
	     */
	    switch(ch=buf[c]) {
	    case '<': case '\t': case '\f': case '\n': case '\r': case ' ':
		bridge.options = new DefaultOptions();
	    	break;
	    case 0:	
		// PING
		return PING;

		// OPTIONS
	    default:
	    	if(ch==0177) { // extended header
	    	    if (++c==clen) {
	    		if((clen=read(in)) <= 0) throw new IllegalArgumentException ("Illegal header length");
	    		ch = buf[c = 0];
	    	    } else {
	    		ch = buf[c];
	    	    }
	    	} else {
	    	    throw new IllegalArgumentException ("Illegal header.");
	    	}
	    	initOptions (ch);
	    	c++;
	    }
	} else {
	    return EOF;
	}
	return OK; 
    }
     private int read(InputStream in) throws IOException {
	try {
	    return in.read(buf, 0, RECV_SIZE);
	} catch (SocketException e) {
	    // may happen if we reload the context and destroy the socket
	    if(Util.logLevel>5) Util.printStackTrace(e);
	    return -1;
	} catch (InterruptedIOException e) {
	    if(Util.logLevel>5) Util.printStackTrace(e);
	    return -1;
	} catch (IOException e) {
	    Util.printStackTrace(e);
	    throw e;
	}
    }
    private boolean response = true;
    private ParserTag tag[] = null;
    private byte buf[] = new byte[RECV_SIZE];
    private int len=SLEN;
    private byte s[]= new byte[len];
    private byte ch;
    // VOJD is VOID for f... windows (VOID is in winsock2.h)
    private static final short BEGIN=0, KEY=1, VAL=2, ENTITY=3, VOJD=5, END=6; short type=VOJD;
    private short level=0, eof=0, eor=0; boolean in_dquote, eot=false;
    private int clen=0, c=0, i=0, i0=0, e;

    void RESET() {
    	type=VOJD;
    	level=0;
    	eor=0;
    	in_dquote=false;
    	i=0;
    	i0=0;
    }
    void APPEND(byte c) {
	if(i>=len-1) {
	    int newlen=len*2;
	    byte[] s1=new byte[newlen];
	    System.arraycopy(s, 0, s1, 0, len);
	    len=newlen;
	    s=s1;
	} 
	s[i++]=c; 
    }
    void CALL_BEGIN() {
    	if(bridge.logLevel>=4) {
	    StringBuffer buf=new StringBuffer("--> <");   
	    buf.append(tag[0].strings[0].getUTF8StringValue());
	    buf.append(" ");
    		
	    for(int i=0; i<tag[1].n; i++) {
		buf.append(tag[1].strings[i].getUTF8StringValue()); buf.append("=\""); buf.append(tag[2].strings[i].getUTF8StringValue());buf.append("\" ");
	    }
	    buf.append(eot?"/>":">"); eot=false;
	    bridge.logDebug(buf.toString());
    	}
	response = handler.begin(tag);
    }
    void CALL_END() {
    	if(bridge.logLevel>=4) {
	    StringBuffer buf=new StringBuffer("--> </");   
	    buf.append(tag[0].strings[0].getUTF8StringValue());
	    buf.append(">");
	    bridge.logDebug(buf.toString());
    	}
	handler.end(tag[0].strings);
    }
    void PUSH(int t) { 
	ParserString str[] = tag[t].strings;
	short n = tag[t].n;
	s[i]=0;
	if(str[n]==null) str[n]=createParserString();
	str[n].string=s;
	str[n].off=i0;
	str[n].length=i-i0;
	++tag[t].n;
	APPEND((byte)0);
	i0=i;
    }
    short parse(final InputStream in) throws IOException {
    	if(eof!=0) return EOF;
    	
    	while(eor==0) {
	    if(c==clen) { 
	    	clen=read(in); 
		if(clen<=0) return eof=EOF;
		c=0; 

	    }
	    switch((ch=buf[c])) 
		{/* --- This block must be compilable with an ansi C compiler or javac --- */
		case '<': if(in_dquote) {APPEND(ch); break;}
		    level++;
		    type=BEGIN;
		    break;
		case '\t': case '\f': case '\n': case '\r': case ' ': if(in_dquote) {APPEND(ch); break;}
		    if(type==BEGIN) {
			PUSH(type); 
			type = KEY; 
		    }
		    break;
		case '=': if(in_dquote) {APPEND(ch); break;}
		    PUSH(type);
		    type=VAL;
		    break;
		case '/': if(in_dquote) {APPEND(ch); break;}
		    if(type==BEGIN) { type=END; level--; }
		    level--;
		    eot=true; // used for debugging only
		    break;
		case '>': if(in_dquote) {APPEND(ch); break;}
		    if(type==END){
			PUSH(BEGIN);
			CALL_END();
		    } else {
			if(type==VAL) PUSH(type);
			CALL_BEGIN();
		    }
		    tag[0].n=tag[1].n=tag[2].n=0; i0=i=0;      		/* RESET */
		    type=VOJD;
		    if(level==0 && response) eor=1; 
		    break;
		case ';':
		    if(type==ENTITY) {
			switch (s[e+1]) {
			case 'l': s[e]='<'; i=e+1; break; /* lt */
			case 'g': s[e]='>'; i=e+1; break; /* gt */
			case 'a': s[e]=(byte) (s[e+2]=='m'?'&':'\''); i=e+1; break; /* amp, apos */
			case 'q': s[e]='"'; i=e+1; break; /* quot */
			default: APPEND(ch);
			}
			type=VAL; //& escapes may only appear in values
		    } else {
		    	APPEND(ch);
		    }
		    break;
		case '&': 
		    type = ENTITY;
		    e=i;
		    APPEND(ch);
		    break;
		case '"':
		    in_dquote = !in_dquote;
		    if(!in_dquote && type==VAL) {
			PUSH(type);
			type = KEY;
		    }
		    break;
		case 0177: if(in_dquote) {APPEND(ch); break;}
		    // handled differently by socket- and JEE implementation
		    handler.parseHeader(new InputStream() {
                       public int read() throws IOException {
                	   if (c==clen) {
                	       clen = Parser.this.read(in);
                	       if(clen<=0) throw new IOException("parse header");
                	       c=0;
                	   }
                	   return buf[c++];
                       }
		    });
		    c--;
		    break;
		default:
		    APPEND(ch);
		} /* ------------------ End of ansi C block ---------------- */
	    c++;
	}
   	RESET();
  
   	return OK;
    }

    /**
     * Reset the internal state. Useful if you want to switch the
     * input stream for the next packet.
     */
    public void reset() {
	eof=0;
	clen=0;
	c=0;
	len=SLEN;
	s=new byte[len];
    }
    /**
     * Set the current bridge object
     * @param bridge The bridge
     */
    public void setBridge(JavaBridge bridge) {
	this.bridge = bridge;
    }
    
    private ParserString createParserString () {
	return new ParserString(bridge);
    }
}
