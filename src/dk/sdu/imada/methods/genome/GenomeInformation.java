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
import java.util.Objects;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class stores all information about the 
 * genomes under analysis in a HashMap.
 * 
 * @author Eudes Barbosa 
 */
public class GenomeInformation extends ParseLocalGenomes {
	
	//------  Variable declaration  ------//
	
	private static final Logger logger = LogManager.getLogger(GenomeInformation.class.getName());

	/** Lifestyle one name. */
	protected String lifestyleOne = null;

	/** Lifestyle one genomes. */
	protected Map<String, File> genomeLifestyleOne = 
			new HashMap<String, File>();

	/** Lifestyle two name. */
	protected String lifestyleTwo = null;

	/** Lifestyle two genomes. */
	protected Map<String, File> genomeLifestyleTwo = 
			new HashMap<String, File>();

	//------  Declaration end  ------//


	/**
	 * Creates a object to store all information about the 
	 * genomes under analysis.
	 * 
	 * @param folderLifeOne		Path to directory with 
	 * 							lifestyle one genomes.
	 * @param nameLifeOne		Lifestyle one name.
	 * @param folderLifeTwo		Path to directory with 
	 * 							lifestyle two genomes.
	 * @param nameLifeTwo		Lifestyle two name.
	 */
	public GenomeInformation(String folderLifeOne, String nameLifeOne,
			String folderLifeTwo, String nameLifeTwo) {
		// Set names
		this.lifestyleOne = Objects.requireNonNull(nameLifeOne, "Lifestyle one name cannot be null.");
		this.lifestyleTwo = Objects.requireNonNull(nameLifeTwo, "Lifestyle two name cannot be null.");
		//
		Objects.requireNonNull(folderLifeOne, "Path to lifestyle one directory cannot be null.");
		Objects.requireNonNull(folderLifeTwo, "Path to lifestyle two directory  cannot be null.");
		
		// Add organisms to LifestyleOne table
		ArrayList<File> list = browseLocalFiles(folderLifeOne);
		for (File s : list) {
			logger.debug(s);
			addRowLifestyleOne(parseLocalGBK(s));
		}

		// Add organisms to LifestyleTwo table
		list = browseLocalFiles(folderLifeTwo);
		for (File s : list) {
			logger.debug(s);
			addRowLifestyleTwo(parseLocalGBK(s));
		}
	}

	/**
	 * Adds a new row to lifestyle One table. It first verify if the value
	 * wasn't already added to one of the two lifestyles tables.
	 * 
	 * @param row		Object array that contains information about the
	 * 					genome, namely: organism name, checked (boolean),
	 * 					accession number and project identifier.
	 */
	protected void addRowLifestyleOne(Object[] row) {
		String organismName = row[0].toString();
		//fileName     = row[1].toString();
		//
		genomeLifestyleOne.put(organismName, (File) row[1]);		
	}

	/**
	 * Adds a new row to lifestyle Two table. It first verify if the value
	 * wasn't already added to one of the two lifestyles tables.
	 * 
	 * @param row		Object array that contains information about the
	 * 					genome, namely: organism name, checked (boolean),
	 * 					accession number and project identifier.
	 */
	protected void addRowLifestyleTwo(Object[] row) {
		String organismName = row[0].toString();
		//fileName     = row[1].toString();
		
		// Check if not already in the other map		
		if (!genomeLifestyleOne.containsKey(organismName)) {  
			genomeLifestyleTwo.put(organismName, (File) row[1]);	
		} else {
			logger.info("Attemped to include organisms multiple times: " + organismName);
		}
	}

	/** @return	Returns lifestyle one table. */
	public Map<String, File> getLifestyleOneTable() {
		return genomeLifestyleOne;
	}

	/** @return	Returns lifestyle two table. */
	public Map<String, File> getLifestyleTwoTable() {
		return genomeLifestyleTwo;
	}

	/** @return	Returns lifestyle one name. */
	public String getNameLifestyleOne() {
		return lifestyleOne;
	}
	
	/** @return	Returns lifestyle two name. */
	public String getNameLifestyleTwo() {
		return lifestyleTwo;
	}	
}