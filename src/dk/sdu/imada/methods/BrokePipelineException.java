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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Class contains generic exception procedure for 
 * when a crucial part of the pipeline fails.
 * 
 * @author Eudes Barbosa
 */
public class BrokePipelineException extends Exception {

	//------  Variable declaration  ------//
	
	private static final long serialVersionUID = -1136696416171363364L;
	
	private static final Logger logger = LogManager.getLogger(BrokePipelineException.class.getName());

	//------  Declaration end  ------//

	
	/** 
	 * Creates generic exception procedure for 
	 * when a crucial part of the pipeline fails.
	 * 
	 * @param message	Message to be display on 
	 *  				progress panel.
	 * @param e			The original exception.
	 */
	public BrokePipelineException(String message, Exception e) {
		// Cancel pipeline
		ExecutePipeline.cancelled();
		
		// Change message
		String error = "Process failed : " + message + ". Execution halted...";
		logger.error(error);
		e.printStackTrace();
		System.exit(2);
	}
	

}