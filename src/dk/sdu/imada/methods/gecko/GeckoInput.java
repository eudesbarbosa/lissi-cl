/**
 * 
 */
package dk.sdu.imada.methods.gecko;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import dk.sdu.imada.methods.BrokePipelineException;
import dk.sdu.imada.methods.ExecutePipeline;
import dk.sdu.imada.methods.genome.Gene;
import dk.sdu.imada.methods.genome.Genome;

/**
 * Class parses the list of genome objects 
 * and create the input for Gecko.
 * 
 * @author Eudes Barbosa
 *
 */
public class GeckoInput {

	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(GeckoInput.class.getName());

	/** Path to Gecko input file. */
	protected String fileName = "";	

	
	//------  Declaration end  ------//


	/**
	 * Parses the list of Genome objects  
	 * and create the input for Gecko.
	 */
	public GeckoInput(String localDir) throws BrokePipelineException {
		// Create Gecko folder
		String geckoDir =  localDir.concat(File.separator).concat("Gecko");
		File dir = new File(geckoDir);
		if(!dir.exists())
			dir.mkdirs();

		// Create input file
		this.fileName = geckoDir.concat(File.separator).concat("GeckoInput.cog");
		File f = new File(fileName);
		try {
			if(!f.exists()) {
				f.createNewFile();
			} else {
				f.delete();
				f.createNewFile();
			}
		} catch (IOException e) {
			logger.error("Error while creating Gecko input file.");
			e.printStackTrace();
		}		

		// Print information into file
		List<Genome> genomes = ExecutePipeline.getGenomes();
		if (genomes.size() == 0)
			throw new BrokePipelineException("Zero genomes present in the analysis.", null);
		toGecko(genomes);
	}
	

	/**
	 * Parses information into Gecko input file. 
	 * 
	 * @param clusterHash	List of Genome objects.
	 */
	private void toGecko(List<Genome> genomes) {	
		try {
			// Write to file
			FileWriter fw = new FileWriter(new File(fileName));
			// Iterate through all organisms
			for (Genome org : genomes) {
				// Get genome definition
				String orgDefinition = org.getAccession().concat(" - ")
						.concat(org.getDefinition()).concat("\n");
				// Get genes
				ArrayList<Gene> genes = org.getGenes();
				if (genes.size() == 0)
					throw new BrokePipelineException("Zero genes under analysis.", null);

				// Print header and number of proteins
				fw.write(orgDefinition); 	
				fw.write(org.getGenes().size()+" proteins\n");
				// Print each gene information
				String line;
				for (Gene g : genes) {
					String clusterID = "" + g.getCluster();
					if (g.getName().equals("")) {
						line = clusterID.concat("\t").concat(""+g.getOrientation())
								.concat("\t-\t---\t").concat(g.getProduct()).concat(" ["+g.getId()+"]");
					} else {
						line = clusterID.concat("\t").concat(""+g.getOrientation())
								.concat("\t-\t").concat(g.getName()).concat("\t")
								.concat(g.getProduct()).concat(" ["+g.getId()+"]");
					}
					logger.debug(line);
					// Print line
					fw.write(line + "\n");
				}
				fw.write("\n");	
			}
			// Close file
			fw.close();				
		} catch (IOException | BrokePipelineException e) {
			logger.debug("Error while writing Gecko input file.");
			e.printStackTrace();
		}
	}
	

	/**
	 * @return	Returns path to Gecko's 
	 * 			input file.
	 */
	public String getInputPath() {
		return fileName;
	}

}
