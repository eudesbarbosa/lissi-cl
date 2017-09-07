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
package dk.sdu.imada.methods.statistics;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import dk.sdu.imada.methods.BrokePipelineException;

/**
 * Class creates a Thread associated with a Rscript system call.
 * 
 * @author Eudes Barbosa
 */
public abstract class RunRscript extends dk.sdu.imada.methods.Run {

	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(RunRscript.class.getName());

	//------  Declaration end  ------//


	@Override
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
			StreamHandlearRscript errorGobbler = new 
					StreamHandlearRscript(run.getErrorStream(), "INFO");            

			// Deal with output
			StreamHandlearRscript outputGobbler = new 
					StreamHandlearRscript(run.getInputStream(), "OUT");

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
				if(!thread.isInterrupted())
					t.printStackTrace();
			}
		}
		//
		return thread;		
	}
}
