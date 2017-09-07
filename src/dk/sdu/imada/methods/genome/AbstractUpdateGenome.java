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
package dk.sdu.imada.methods.genome;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.sdu.imada.methods.BrokePipelineException;
import dk.sdu.imada.methods.Parser;


/**
 * Abstract class defines the methods required to 
 * update the gene information from all Genomes. It 
 * has routines to add a cluster identifiers to each 
 * gene.
 * 
 * @author Eudes Barbosa
 */
public abstract class AbstractUpdateGenome extends Parser {
	
	private static final Logger logger = LogManager.getLogger(AbstractUpdateGenome.class.getName());

	
	/** 
	 * Used to avoid sending multiple error messages while 
	 * using multi-thread.
	 */
	protected boolean errorMsgSent = false;
	
	
	/**
	 * Updates information for all available genomes. It 
	 * includes cluster id to all genes.
	 * 
	 * @param genomes	List of all genomes used in the 
	 * 					analysis.
	 */
	public abstract List<Genome> updateGenomes(List<Genome> genomes);
	
	
	/**
	 * @param oldGenes	Gene array without cluster identifier.
	 * @return	Returns a Gene array with cluster identifier updated.
	 */
	protected abstract ArrayList<Gene> parseGenes(ArrayList<Gene> oldGenes);
	
	
	/**
	 * @param genes			List of Genes.
	 * @param clusterHash 
	 * @return	Returns a Callable for the given Gene list.
	 */
	protected abstract Callable<String> createCallable(final List<String> list);
	
	
	/**
	 * Sends and error message and indicates that 
	 * the pipeline is broken.
	 * 
	 * @param message	Error message.
	 */
	protected void sentError(String message) {
		// Avoids sending multiple messages
		if (errorMsgSent == false ) { 			
			logger.error(message);
			try {
				throw new BrokePipelineException(message, new Exception());
			} catch (BrokePipelineException e) {
				return;
			}
		}		
	}
	
	
}
