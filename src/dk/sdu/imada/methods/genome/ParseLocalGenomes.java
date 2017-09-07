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


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.sdu.imada.methods.BrokePipelineException;
import dk.sdu.imada.methods.Parser;


/**
 * Class scans folders searching for GenBank files. It will 
 * report a list of genomes for each lifestyle. The information 
 * in the list contains the basic information parsed from each 
 * GenBank file.
 * 
 * @author Eudes Barbosa
 */
public class ParseLocalGenomes extends Parser {

	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(ParseLocalGenomes.class.getName());

	private final String errorMsg = "Provided path must contain at least 4 (four) genomes. "
			+ "Accepted files extensions: .gb and .gbk.";

	//------  Declaration end  ------//


	/**
	 * Scans given local folder and call method to extract header from GenBank 
	 * files. Specially designed for files stores in the local disk.
	 * 
	 * @param path	Path to directory to be scanned.
	 */
	protected ArrayList<File> browseLocalFiles(String path) {
		// Initialize
		ArrayList<File> files = new ArrayList<File>();
		//List all files in directory
		File f = new File(path);
		File[] listOfFiles = f.listFiles();
		for (int j = 0; j < listOfFiles.length; j++) {
			if (listOfFiles[j].isFile()) {
				String fileName = listOfFiles[j].getName();
				logger.debug(fileName);
				// Accept only GenBank files
				if (fileName.toLowerCase().endsWith(".gbk") || 
						fileName.toLowerCase().endsWith(".gb")) {
					// Add to array
					files.add(listOfFiles[j]);
				}
			}
		}		
		// Check for number of genomes under analysis
		if (files.size() < 4) {
			try {
				logger.error(this.errorMsg);
				throw new BrokePipelineException(this.errorMsg, new Exception());
			} catch (BrokePipelineException e) {
				e.printStackTrace();
			}
		}

		//
		return files;
	}

	/** 
	 * Extracts information from GenBank files. Specially 
	 * designed for files stores in the local disk.
	 */
	protected Object[] parseLocalGBK(File fileName) {
		// Buffer file
		BufferedReader br = getBufferedReader(fileName.getAbsolutePath());
		try {
			// Extract organism name
			String stringLine;
			String organismName;
			while ((stringLine = br.readLine()) != null) {
				if(stringLine.contains("  ORGANISM  ")){
					organismName = stringLine.replace("  ORGANISM  ", "");
					logger.debug(organismName);
					return new Object[] {organismName, fileName};
				}					
			}
			br.close();
		} catch (IOException e) {
			logger.error("Error while parsing local GenBank files. \n" +
					"File not found: " + fileName);
			e.printStackTrace();
		}		
		//
		return null;
	}
}
