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
package dk.sdu.imada.methods.transclust;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import dk.sdu.imada.methods.BrokePipelineException;
import dk.sdu.imada.methods.Command;
import dk.sdu.imada.methods.Parser;


/**
 * Class reads Transitivity Clustering (TC) results. It 
 * simply reads and returns the output as a list of 
 * strings. Each string has the same structure as TC's 
 * output: Gene Identifier \<TAB\> Cluster ID.
 * 
 * @author Eudes Barbosa
 */
public class ParseTransClustResults extends Parser implements Command {

	//------  Variable declaration  ------//
	
	private static final Logger logger = LogManager.getLogger(ParseTransClustResults.class.getCanonicalName());

	/** List with all lines from TransClust output. */
	protected ArrayList<String> list;
	
	/** Path to TransClust output. */
	protected String outFile;
	

	//------  Declaration end  ------//

	
	/**
	 * Reads Transitivity Clustering result and store 
	 * in a List. The lines structure is preserved.
	 * 
	 * @param output	Path to Transitivity Clustering 
	 * 					output file.
	 */
	public ParseTransClustResults(String output) {
		this.outFile = output;		
	}

	@Override
	public void exec() {		
		// Update progress bar
		logger.info("Parsing Transitivity Clustering results.");
		parse();		
	}

	/**
	 * Reads each line of Transitivity Clustering output 
	 * and send respective Gene Identifier and Cluster 
	 * number to the database.
	 * (database suppressed)
	 */
	protected void parse() {
		// Buffer TransClust output file
		list = new ArrayList<>();
		BufferedReader br = getBufferedReader(outFile);
		try {
			String line;
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
			//
			br.close();
		} catch (IOException e) {
			String message = "Error while parsing Transitivity Clustering output.";
			logger.debug(message);
			e.printStackTrace();
			try {
				throw new BrokePipelineException(message, e);
			} catch (BrokePipelineException e1) {
				return;
			}
		}
	}

	/**
	 * @return		Returns the list of 
	 * 				clusters genereted by 
	 * 				Transitivity Clustering.
	 */
	public ArrayList<String> getList() {
		return list;
	}

}
