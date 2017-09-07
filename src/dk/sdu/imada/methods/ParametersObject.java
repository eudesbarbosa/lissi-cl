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

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.sdu.imada.methods.gecko.Gecko;
import dk.sdu.imada.methods.genome.GenomeInformation;
import dk.sdu.imada.methods.statistics.RandomForest;
import dk.sdu.imada.methods.transclust.TransClust;


/**
 * Class stores all objects required for 
 * the analysis.
 * 
 * @author Eudes Barbosa 
 */
public class ParametersObject {

	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(ParametersObject.class.getName());

	/** 
	 * Object stores information about the location 
	 * of the genomes and the lifestyles names.
	 */
	protected GenomeInformation genomes;

	/** TransClust object (parameters). */
	protected TransClust transclust;

	/** Gecko object (parameters).*/
	protected Gecko gecko;

	/** Random Forest object (parameters). */
	protected RandomForest randomforest;

	/** Path to R binary files. */
	protected String rDir = "/usr/bin/";

	/** Path to Blast binary files. */
	protected String blastDir = "/usr/bin/";

	/** Path to local working directory. */
	protected String localDir;

	/** Number of processors available. */
	protected int processors = 1;

	//------  Declaration end  ------//



	/** @return Returns number of available processors. */
	public int getProcessors() {
		return processors;
	}

	/**
	 * Sets number of available processors.
	 * 
	 * @param processors Number of available processors.
	 */
	public void setProcessors(int processors) {
		int cores = Runtime.getRuntime().availableProcessors();
		if (processors > cores) {
			logger.warn("Trying to use more CPUs than there are available. "
					+ "Value set to max available (" +
					cores + ").");
			this.processors = cores;
		} else if (processors <= 0) {
			logger.warn("Unable to use define number of CPUs, "
					+ "using a single processor instead.");
			this.processors = 1;
		} else {
			this.processors = processors;
		}
	}

	
	/** @return Returns path to R binary files. */
	public String getRDir() {
		return rDir;
	}
	

	/**
	 * Sets the path to R binary files.
	 * 
	 * @param rDir Path to R binary files.
	 */
	public void setRDir(String rDir) {
		this.rDir = rDir;
	}
	

	/** @return Returns path to Blast binary files. */
	public String getBlastDir() {
		return blastDir;
	}

	/**
	 * Sets the path to Blast binary files.
	 * 
	 * @param blastDir Path to Blast binary files.
	 */
	public void setBlastDir(String blastDir) {
		this.blastDir = blastDir;
	}
	

	/** @return Returns path to local working directory. */
	public String getLocalDir() {
		return localDir;
	}
	

	/**
	 * Sets path to local working directory.
	 * 
	 * @param localDir Path to local working directory.
	 */
	public void setLocalDir(String localDir) {
		this.localDir = localDir;
	}	
	

	/**
	 * @return Returns Genome Information object. 
	 * Object stores information about the location 
	 * of the genomes and the lifestyles names.
	 */
	public GenomeInformation getGenomeObject() {
		return genomes;
	}
	

	/**
	 * @return Returns TransClust object (parameters).
	 */
	public TransClust getTransClustObject() {
		return transclust;
	}


	/**
	 * @return Returns Gecko object (parameters).
	 */
	public Gecko getGeckoObject() {
		return gecko;
	}

	
	/**
	 * @return Returns Random Forest object (parameters).
	 */
	public RandomForest getRandomForestObject() {
		return randomforest;
	}


	/**
	 * Creates a Genome object with provided information.
	 * 
	 * @param folderLifeOne	Path to folder with lifestyle one genomes.
	 * @param nameLifeOne	Name lifestyle one.
	 * @param folderLifeTwo	Path to folder with lifestyle two genomes.
	 * @param nameLifeTwo	Name lifestyle two.
	 */
	protected void setGenomeObject(String folderLifeOne, String nameLifeOne,
			String folderLifeTwo, String nameLifeTwo) throws BrokePipelineException {
		// Validate provided folders
		File f = new File(folderLifeOne);
		boolean checkOne = f.exists() && f.isDirectory();
		//
		f = new File(folderLifeTwo);
		boolean checkTwo = f.exists() && f.isDirectory();
		//
		boolean checkBothDirs = (folderLifeOne.equals(folderLifeTwo));
		if (!checkOne || !checkTwo || checkBothDirs) {
			throw new BrokePipelineException("Provided path to genome folder(s) invalid.", null);
		}
		// Validate lifestyles name
		checkOne = (nameLifeOne != null) && (!nameLifeOne.equals(""));
		checkTwo = (nameLifeTwo != null) && (!nameLifeTwo.equals(""));
		boolean checkBothNames = (nameLifeOne.equals(nameLifeTwo));
		if (!checkOne || !checkTwo || checkBothNames ) {
			logger.error("Please provided (distinct) names to lifestyles. \nExiting...");
			System.exit(1);
		}
		// Create object
		logger.debug(folderLifeOne + "\t" + nameLifeOne);
		logger.debug(folderLifeTwo + "\t" + nameLifeTwo);
		this.genomes = new GenomeInformation(folderLifeOne, nameLifeOne, folderLifeTwo, nameLifeTwo);
		
	}

	
	/**
	 * Creates a Blast object.
	 * 
	 * @param evalue			E-value.
	 * @param blastFile			Path to Blast result.
	 * @param density			TransClust density parameter.
	 * @param transclustFile	Path to TransClust result.
	 */
	protected void setTransClustObject(String evalue, String blastFile,
			String density, String transclustFile) {
		this.transclust = new TransClust();
		//
		try {
			// TransClust result provided
			if (!transclustFile.equals("")) {
				File f = new File(transclustFile);
				boolean check = f.exists() && f.isFile();
				if (check) {
					this.transclust.setUserTransClustFile(transclustFile);
					this.transclust.setStart(Integer.parseInt(density));
					//
					return;
				} else {
					logger.error("Error reading provided TransClust file. \nExiting...");
					System.exit(1);
				}
			}
			// Blast result provided
			if (!blastFile.equals("")) {
				File f = new File(blastFile);
				boolean check = f.exists() && f.isFile();
				if (check) {
					this.transclust.setStart(Integer.parseInt(density));
					this.transclust.setUserBlastFile(blastFile);
					//
					return;
				} else {
					logger.error("Error reading provided Blast file. \nExiting...");
					System.exit(1);
				}

			}
			// No previous results provided
			this.transclust.setStart(Integer.parseInt(density));
			this.transclust.setEvalue(Double.parseDouble(evalue));

		} catch (Exception e) {
			logger.error("Error while parsing TransClust parameters. \nExiting...", e);
		}
	}
	

	/**
	 * Creates a Gecko object.
	 * 
	 * @param minOrganisms	Minimum number of organisms.
	 * @param minSize		Minimum island length.
	 * @param indels		Maximum number of indels.
	 * @param file			Path to previously generated result.
	 * @throws BrokePipelineException 
	 */
	protected void setGeckoObject(String minOrganisms, String minSize,
			String indels, String file) throws BrokePipelineException {
		this.gecko = new Gecko();
		try {
			// Check if it should be executed
			boolean skipGecko = (minOrganisms == null) && (minSize == null) &&
					(indels == null) && (file == null);
			if (skipGecko == true) {
				this.gecko = null;
				logger.warn("Running analysis without Island Detection!");
				//
				return;
			}		
			//
			skipGecko = (minOrganisms.equals("")) && (minSize.equals("")) &&
					(indels.equals("")) && (file.equals(""));
			if (skipGecko == true) {
				this.gecko = null;
				logger.warn("Running analysis without Island Detection!");
				//
				return;
			}			
			// Gecko result provided
			if (!file.equals("")) {
				File f = new File(file);
				boolean check = f.exists() && f.isFile();
				if (check) {
					this.gecko.setUserGeckoFile(file);
					//
					return;
				} else {
					logger.error("Error reading provided Gecko file. \nExiting...");
					System.exit(1);
				}
			}
			// No result provided
			int d = Integer.parseInt(indels);
			int g = Integer.parseInt(minOrganisms);
			int s = Integer.parseInt(minSize);
			if (d > -1 && g > 1 && s > d && s > 1) {
				this.gecko.setDistance(d);
				this.gecko.setGenomes(g);
				this.gecko.setSize(s);
			} else {
				logger.error("Error while parsing Gecko parameters. \nExiting...");
				System.exit(1);
			}
		} catch (Exception e) {
			logger.error("Error while parsing Gecko parameters", e);
		}
	}
	

	/**
	 * Creates a Random Forest object.
	 * 
	 * @param kfold	K-fold.
	 * @param runs	Number of runs.
	 * @param trees	Number of trees per run.
	 */
	protected void setRandomForestObject(String kfold, String runs,
			String trees) {
		try {
			int k = Integer.parseInt(kfold);
			int r = Integer.parseInt(runs);
			int t = Integer.parseInt(trees);
			if (k > 1 && r > 0 && t > 0) {
				this.randomforest = new RandomForest();
				this.randomforest.setKfold(k);
				this.randomforest.setRuns(r);
				this.randomforest.setTrees(t);
			}
		} catch (Exception e) {
			logger.error("Error while parsing Random Forest parameters", e);
		}
	}

}
