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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Class creates an Executor Queue Service. It allows 
 * tasks to be independently added and executed them according 
 * to the number of available cores/threads. 
 *  
 * @author Eudes Barbosa
 */
public class ExecutorServiceUnbounded {

	//------  Variable declaration  ------//

	/** Pool that executes a single task at a time. */
	protected ExecutorService executor;
	
	//------  Declaration end  ------//


	/** Creates an Executor that executes a single task at a time. */
	public ExecutorServiceUnbounded(int nThreads) {
		executor = Executors.newFixedThreadPool(nThreads); 
	}

	/**
	 * @param task		Runnable task.
	 * @return Returns Future after adding new runnable 
	 * task into class pool. A single task will be 
	 * executed at a time.
	 */
	public Future<?> addTask(Runnable task) {
		return executor.submit(task);
	}

	/** Shuts down the executor before leaving the program */
	public void killExecutor() {
		executor.shutdown();
	}

}