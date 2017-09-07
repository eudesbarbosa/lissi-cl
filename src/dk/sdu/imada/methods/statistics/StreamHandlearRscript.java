/*
 * File name: StreamHandlear.java
 * Copy from WEB
 * http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html
 */
package dk.sdu.imada.methods.statistics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import dk.sdu.imada.methods.ExecutePipeline;
import dk.sdu.imada.methods.StreamHandlear;

/**
 * Class deals with input stream from system calls.
 * 
 * @author Adapted from WEB.
 * http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html
 */
public class StreamHandlearRscript extends StreamHandlear {

	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(StreamHandlearRscript.class.getName());
	
	//------  Declaration end  ------//


	/**
	 * Deals with input streamm from system calls.
	 * @param is		The input stream, either error or
	 * 					the normal output of the process.
	 * 
	 * @param type		The type of input stream.
	 */
	public StreamHandlearRscript(InputStream is, String type) {
		super(is, type);
	}	
	

	/* Run thread.
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try{
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line=null;
			while ( (line = br.readLine()) != null) {
				System.out.println(type + ">" + line);
				if (line.toLowerCase().contains("execution halted")) {
					// Inform error and cancel process
					RunRandomForest.error();
					ExecutePipeline.cancelled();
				}
				// Pass R messages to log
				if (line.contains("[R]")){
					logger.info(line.replace("[1] ", "").replaceAll("\"", ""));					
				}
			}
		} catch (IOException ioe) {
			logger.info("Stream closed - process interrupted.");
		}
	}

}