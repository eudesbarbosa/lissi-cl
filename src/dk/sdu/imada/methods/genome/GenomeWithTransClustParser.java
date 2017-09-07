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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//import org.apache.logging.log4j.Logger;
//import org.apache.logging.log4j.LogManager;

import dk.sdu.imada.methods.BrokePipelineException;
import dk.sdu.imada.methods.Cancelled;
import dk.sdu.imada.methods.Command;
import dk.sdu.imada.methods.transclust.ParseTransClustResults;


/**
 * Class contains methods to parse a GenBank file and 
 * retrieve the relevant information.
 * 
 * @author Eudes Barbosa
 */
public class GenomeWithTransClustParser extends GenomeParser implements Command, Cancelled {

	
	//------  Variable declaration  ------//

	//private static final Logger logger = LogManager.getLogger(GenomeWithTransClustParser.class.getName());

	/** HashMap with Gene Identifier as Key and cluster ID as Entry. */
	protected HashMap<Integer, Integer> clusterHash =
			new HashMap<Integer, Integer>();

	/** List with clusters results as presented in TC output. */
	protected ArrayList<String> clusters = new ArrayList<String>();
	
	//------  Declaration end  ------//

	
	/** 
	 * Parses a GenBank file and retrieve the relevant information.
	 * 
	 * @param genomesL1			Genomes associated with lifestyle one.
	 * @param nameL1			Lifestyle one name.
	 * @param genomesL2			Genomes associated with lifestyle two.
	 * @param nameL2			Lifestyle two name.
	 * @param transclustFile	Path to TransClust output.
	 * @param processors		Number of available processors 
	 * @param localDir			Path to local working directory.
	 */
	public GenomeWithTransClustParser(Map<String, File> genomesL1, String nameL1, 
			Map<String, File> genomesL2, String nameL2, String transclustFile,
			String localDir, int processors) {
		//
		super(genomesL1, nameL1, genomesL2, nameL2, localDir, processors);
		//
		this.clusters = getTransClustResult(transclustFile);
		mapTranclust(clusters);		
	}
	
	/** 
	 * Parses a GenBank file and retrieve the relevant information. 
	 * 
	 * @param genomesL1			Genomes associated with lifestyle one.
	 * @param nameL1			Lifestyle one name.
	 * @param genomesL2			Genomes associated with lifestyle two.
	 * @param nameL2			Lifestyle two name.
	 * @param clusters			List of clusters genereted by 
	 * 							Transitivity Clustering.
	 * @param processors		Number of available processors 
	 * @param localDir			Path to local working directory.
	 */
	public GenomeWithTransClustParser(Map<String, File> genomesL1, String nameL1, 
			Map<String, File> genomesL2, String nameL2, ArrayList<String> clusters,
			String localDir, int processors) {
		//
		super(genomesL1, nameL1, genomesL2, nameL2, localDir, processors);
		//
		this.clusters = clusters;
		mapTranclust(clusters);		
	}
	
	
	/**
	 * For each genome under analysis, the method 
	 * sets the gene identifiers to its respective cluster
	 * identifier. 
	 */
	public void setClusters() {
		// Iterate through all genomes
		for (Genome genome : this.genomes) {
			// Initialize variable
			ArrayList<Gene> genesNew = new ArrayList<Gene>();
			// Get all genes from genome
			ArrayList<Gene> genes = genome.getGenes();
			// Set cluster id
			for (Gene g : genes) {
				int giValue = Integer.parseInt(g.getId());
				g.setCluster(getClusterIdentifier(giValue));
				//
				genesNew.add(g);
			}
			genome.setGenes(genesNew);			
		}		
	}
	
	
	/** 
	 * @return	Returs list of clusters as presented in 
	 * 			TransClust output.
	 */
	public ArrayList<String> getClusters() {		
		return clusters;
	}	
	
	
	/**
	 * @param tc	List of clusters genereted by 
	 * 				Transitivity Clustering.
	 */
	protected void mapTranclust(ArrayList<String> tc) {
		// Create HashMap for cluster
		for(String c : tc) {
			String[] entry = c.split("\t");
			int gi = Integer.parseInt(entry[0]);
			int cluster = Integer.parseInt(entry[1]);
			clusterHash.put(gi, cluster);
		}		
	}

	/**
	 * Gets TransClust result.
	 * @param transclustFile	Path to TransClust output.
	 * @return Returns the list of clusters genereted by 
	 * Transitivity Clustering.
	 */
	protected ArrayList<String> getTransClustResult(String transclustFile) {
		ParseTransClustResults parse = new ParseTransClustResults(transclustFile);
		parse.exec();
		//
		ArrayList<String> list = parse.getList();
		return list;		
	}
			

	/**
	 * @param giValue	Gene identifier as integer.
	 * @return	Returns the cluster identifier associated 
	 * with the gene. If no identifier exists, it returns -1.
	 * @throws BrokePipelineException
	 */
	protected int getClusterIdentifier(int giValue) {
		if (clusterHash.containsKey(giValue)) {
			return clusterHash.get(giValue);
		} else {
			try {
				throw new BrokePipelineException("Error while parsing GenBank files. " +
						"Fail to find TransClust identifier for gene id : " + giValue , null);
			} catch (BrokePipelineException e) {
				// do nothing
			}
		}
		//
		return -1;		
	}
}
