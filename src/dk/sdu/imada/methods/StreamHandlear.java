/**
 * File name: StreamHandlear.java
 * Adapted from WEB
 * http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html
 */
package dk.sdu.imada.methods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class deals with input stream from system calls.
 * 
 * @author Adapted from WEB.
 * http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html
 */
public class StreamHandlear extends Thread {
	
	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(StreamHandlear.class.getName());

	/** InputStream. */
	protected InputStream is;
	
	/** Type of process. */
	protected String type;
	//------  Declaration end  ------//

	/**
	 * Deals with input stream from system calls.
	 * 
	 * @param is		The input stream, either error or
	 * 					the normal output of the process.
	 * 
	 * @param type		The type of input stream.
	 */
	public StreamHandlear(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}


	/* Run thread.
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try{
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ( (line = br.readLine()) != null)
				System.out.println(type + ">" + line);    
		} catch (IOException ioe) {
			logger.info("Stream closed - process interrupted.");  
		}
	}

}
