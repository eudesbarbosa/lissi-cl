/*
 *            	Life-Style-Specific-Islands
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public License.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *      
 * This material was developed as part of a research project at 
 * the University of Southern Denmark (SDU - Odense, Denmark) 
 * and the Federal University of Minas Gerais (UFMG - Belo 
 * Horizonte, Brazil). For more information please access:
 * 
 *      	https://lissi.compbio.sdu.dk/ 
*/
package dk.sdu.imada.methods;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.sdu.imada.methods.StreamHandlear;


/**
 * Class creates a generic Thread associated with a system call.
 * 
 * @author Eudes Barbosa
 */
public abstract class Run extends Thread {

	//------  Variable declaration  ------//
	
	private static final Logger logger = LogManager.getLogger(Run.class.getName());

	/** Java Lang Thread. */
	protected Thread thread = null;

	/** Exit value. Default: -111. */
	protected int exitValue = -111;
	
	/** Java Lang Process. */
	protected Process run;
	
	//------  Declaration end  ------//

	/**
	 * Configure thread.
	 * 
	 * @param process		Command line.
	 * @param name			Process name. It will be used 
	 * 						in the logger.
	 */
	protected Thread start(List<String> process, String name) {
		try {
			// Create process
			ProcessBuilder pb = new ProcessBuilder(process);					
			run = pb.start();

			// Add shutdown hook
			thread = new Thread() {
				@Override
				public void run() {
					run.destroy();
				}
			};
			Runtime.getRuntime().addShutdownHook(thread);

			// Deal with error
			StreamHandlear errorGobbler = new 
					StreamHandlear(run.getErrorStream(), "INFO");            

			// Deal with output
			StreamHandlear outputGobbler = new 
					StreamHandlear(run.getInputStream(), "OUT");

			// Kick them off
			errorGobbler.start();
			outputGobbler.start();

			// Check the exit value
			if(!thread.isInterrupted()) {
				exitValue = run.waitFor();			
				logger.info(name + " had exit value = " + exitValue);
				if (exitValue != 0) { 
					throw new BrokePipelineException(name, new Exception());
				}
			}
		} catch (InterruptedException | IOException | BrokePipelineException e) {
			logger.error("Unable to finish running " + name);
			try {
				run.destroy();
				thread.interrupt();
			} catch (Throwable t) {
				if (!thread.isInterrupted())
					t.printStackTrace();
			}
		}
		//
		return thread;		
	}

	/**
	 * @return	Returns thread exit value. Default 
	 * 			value: -111.
	 */
	public int getExitValue() {
		return exitValue;
	}
}
