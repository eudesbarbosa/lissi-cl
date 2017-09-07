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
package dk.sdu.imada.view.transclust;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.sdu.imada.methods.Command;
import dk.sdu.imada.methods.transclust.TransClust;


/**
 * Class creates a visualization for Transitivity Clustering 
 * results. One panel will report basic information about 
 * the number of clusters and proteins in it; while the other 
 * will display an histogram with the frequency of cluster 
 * sizes.
 * 
 * @author Eudes Barbosa
 */
public class ViewResults implements Command {
	
	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(ViewResults.class.getName());

	/** 
	 * List with all clusters information as found in 
	 * TransClust output. 
	 */
	protected ArrayList<String> clusters;

	/** 
	 * TransClust object containing	information 
	 * about the used parameters and the processes runtime.
	 */
	protected TransClust tc;
	
	/** Path to local working directory. */
	protected String localDir;
	
	//------  Declaration end  ------//



	/**
	 * Creates a visualization for Transitivity Clustering 
	 * results. One panel will report basic information about 
	 * the number of clusters and proteins in it; while the other 
	 * will display an histogram with the frequency of cluster 
	 * sizes.
	 * 
	 * @param tc			TransClust object containing 
	 * 						information about the used parameters 
	 * 						and the processes runtime.
	 *  
	 * @param clusters		List with all clusters information 
	 * 						as found in TransClust output. 	
 	 * @param localDir		Path to local working directory.
	 */
	public ViewResults(TransClust tc, ArrayList<String> clusters, 
			String localDir) {
		// Set values
		this.clusters = Objects.requireNonNull(clusters, "Cluster cannont be null");
		this.tc = Objects.requireNonNull(tc, "Cluster cannont be null");
		this.localDir = localDir;
	}
	

	@Override
	public void exec() {
		// Update progress bar
		logger.info("Loading Transitivity Clustering results");

		// Check if visualizing range or single  
		// value
		if (tc.isRange() == true) {
			// TODO: Implement TC range approach
		} else {
			singleThreshold();
		} 
	}
	

	/**
	 * Visualize results for TransClust using a 
	 * single density value.
	 */
	protected void singleThreshold(){		
		// Clusters information
		double[] occurrences = getClusterInfo();
		// Pass info to histogram (occurrence, parameter)
		new HistogramPanel(this.localDir, occurrences,
				tc.getStart());		
	}
	

	/**
	 * @return Returns the occurrences of each cluster 
	 * 			in the genomes. These values will be 
	 * 			used to create a histogram of the 
	 * 			Transitivity Clustering results.  
	 */
	protected double[] getClusterInfo() {
		// Get all cluster occurrences (with duplication)
		ArrayList<String> clusterID = new ArrayList<>();
		for (String s : clusters) {
			String[] data = s.split("\t");
			clusterID.add(data[1]);
		}			
		// Get distinct clusters
		Set<String> distincClust = new LinkedHashSet<String>(clusterID);
		int size = distincClust.size();
		double[] occurrences = new double[size];
		int i = 0;
		// Count each cluster occurrences 
		for (String c : distincClust){
			int o = Collections.frequency(clusterID, c);
			occurrences[i] = o;
			i++;
		}
		//logger.info("Number of clusters = " + size);
		//
		return occurrences;
	}
	
	
	/** 
	 * [Test method] Prints occurrences. 
	 * @param occur	Occurrences.
	 */
	@SuppressWarnings("unused")
	private void printOccurances(double[] occur) {
		logger.info("Printing TransClust occurencies: ");
		for (int i = 0; i < occur.length; i++)
			System.out.println(occur[i]);		
	}
}
