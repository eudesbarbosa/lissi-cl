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
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.sdu.imada.methods.BrokePipelineException;
import dk.sdu.imada.methods.Parser;


/**
 * Class updates the information available for 
 * all the genomes. It copies the previous information 
 * and stores the cluster id for each gene. Used for 
 * analysis under 500,000 genes. Data is stored in a 
 * hashmap with concurrent access.
 * 
 * @author Eudes Barbosa
 */
public class UpdateGenomesHashMap extends Parser {

	//------  Variable declaration  ------//
	
	private static final Logger logger = LogManager.getLogger(UpdateGenomesHashMap.class.getName());

	/** HashMap with Gene Identifier as Key and cluster ID as Entry. */
	protected HashMap<Integer, Integer> clusterHash =
			new HashMap<Integer, Integer>();

	/** 
	 * Used to avoid sending multiple error messages while 
	 * using multi-thread.
	 */
	protected boolean errorMsgSent = false;

	//------  Declaration end  ------//

	
	
	/**
	 * Updates information for all available genomes. It 
	 * includes cluster id to all genes.
	 * 
	 * @param genomes		List of all genomes used in the 
	 * 						analysis.
	 */
	public List<Genome> updateGenomes(List<Genome> genomes, ArrayList<String> clusters) {
		logger.info("Loading clusters information.");

		// Create HashMap for cluster
		for(String c : clusters) {
			String[] entry = c.split("\t");
			int gi = Integer.parseInt(entry[0]);
			int cluster = Integer.parseInt(entry[1]);
			clusterHash.put(gi, cluster);
		}		
		// Update genome values
		for (Genome genome : genomes) {
			// Get gene information
			ArrayList<Gene> genes = parseGenes(genome.getGenes());
			// Add gene list to genome
			genome.setGenes(genes);
		}

		//
		return genomes;
	}

	/**
	 * @param genes	Gene array without cluster identifier.
	 * @return	Returns a Gene array with cluster identifier updated.
	 */
	protected ArrayList<Gene> parseGenes(ArrayList<Gene> genes) {
		for(Gene g : genes) {
			try {
				int id = Integer.parseInt(g.getId());
				int cluster = clusterHash.get(id);
				g.setCluster(cluster);	
			} catch (Exception e) {
				if (g.getId().equals(null)) {
					String message = "Gene has no identifier (!!!).";
					sentError(message);
				} else {
					String message = "Gene Identifier <" + g.getId() + "> from " + g.getOrganismID() 
							+ " wasn't found in the TransClust results";
					sentError(message);
				}
			}
		}			
		//
		return genes;
	}

	/**
	 * Sends and error message and indicates that 
	 * the pipeline is broken.
	 * 
	 * @param message	Error message.
	 */
	protected void sentError(String message) {
		if (errorMsgSent == false) {
			errorMsgSent = true;
			logger.error(message);			
			try {
				throw new BrokePipelineException(message, new Exception());
			} catch (BrokePipelineException e) {
				return;
			}				
		}
	}
}