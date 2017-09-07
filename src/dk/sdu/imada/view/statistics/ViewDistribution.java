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
package dk.sdu.imada.view.statistics;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.sdu.imada.methods.Command;


/**
 * Class displays the joint distribution of either putative 
 * gene clusters (TransClust) or islands (Gecko) between 
 * the two lifestyles. 
 * 
 * @author Eudes Barbosa
 */
public class ViewDistribution implements Command {

	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(ViewDistribution.class.getName());

	/** Path to indicator matrix file. */
	protected String file = "";
	
	/** Flag to indicate if using Gecko in the analysis. */
	protected boolean gecko = false;
	
	/** Lifestyle one name. */
	protected String nameL1;

	/** Lifestyle two name. */
	protected String nameL2;
	
	/** Path to local working directory. */
	protected String localDir;
	
	//------  Declaration end  ------//


	/**
	 * Displays the joint distribution of either putative 
	 * gene clusters (TransClust) or islands (Gecko) between 
	 * the two lifestyles. 
	 * 
	 * @param file		Path to indicator matrix.
	 * @param b			True if Gecko was included in 
	 * 					the analysis; False, otherwise.
	 * @param nameL1	Name of lifestyle one.
	 * @param nameL2	Name of lifestyle two.
	 * @param localDir	Path to local working directory.
	 */
	public ViewDistribution(String file, boolean b, 
			String nameL1, String nameL2, String localDir) {
		//
		this.file = file;
		this.gecko = b;
		this.nameL1 = nameL1;
		this.nameL2 = nameL2;
		this.localDir = localDir;
	}

	@Override
	public void exec() {
		logger.info("Loading joint distribution.");
		
		// Set values
		String title = null;
		if (gecko == true)
			title = "Islands Distribution";
		else
			title = "Clusters Distribution";
		String xlab = this.nameL1;
		String ylab = this.nameL2;	

		// Pass info to histogram (occurrence, parameter)
		new SquareBlockedBin(title, xlab, ylab, file, this.localDir);		
	}
}
