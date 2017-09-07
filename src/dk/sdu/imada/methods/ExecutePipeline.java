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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.sdu.imada.methods.gecko.RunGecko;
import dk.sdu.imada.methods.genome.Genome;
import dk.sdu.imada.methods.genome.GenomeParser;
import dk.sdu.imada.methods.genome.GenomeWithTransClustParser;
import dk.sdu.imada.methods.transclust.RunTransClust;
import dk.sdu.imada.methods.statistics.RunRandomForest;


/**
 * Class synchronizes the analysis pipeline. It can be 
 * based either on putative groups of homology proteins 
 * (clusters) or on putative genomic islands. 
 * <br>
 * > Clusters (as described in the Briefings and 
 * Functional Genomics paper): it simply cluster the genes 
 * into potential homologous groups and attempts to find 
 * the most discriminant clusters for each lifestyle.
 * <br>
 * > Islands:<br>
 * i) cluster the genes into potential homologous 
 * groups; ii) finds regions with conserved gene clusters 
 * order (islands); and iii), attempts to find the most 
 * discriminant islands for each lifestyle.
 * 
 * @author Eudes Barbosa
 */
public class ExecutePipeline implements Command {


	//------  Variable declaration  ------//
	

	private static final Logger logger = LogManager.getLogger(ExecutePipeline.class.getName());

	/** Flag to indicate if the process was interrupted. */
	protected static boolean processInterrupted = false;

	/** Parameter object. */
	protected ParametersObject par = null;

	/* Number of threads to use. */
	//protected int threads = 1;

	/* Path to local working directory. */
	//protected String workingDir;

	/** Genome Parser. */
	protected static GenomeParser parseG = null;

	/** Run TransClust object. */
	protected static RunTransClust clust = null;

	/** Run Gecko object. */	
	protected static RunGecko gecko = null;

	/** Run Random Forest object. */
	protected static RunRandomForest learning = null;

	/** List with genome objects under analysis. */
	protected static List<Genome> genomes = new ArrayList<Genome>();
	
	/** Array list with cluster information. */
	protected static ArrayList<String> clusters = new ArrayList<String>();
	

	//------  Declaration end  ------//


	/** 
	 * Sets the pipeline to execute with the provided 
	 * arguments.
	 * @param par 	Object that stores all parameters 
	 * 				required for the analysis. 
	 */
	public ExecutePipeline(ParametersObject par) {
		// Check provided parameters
		this.par = Objects.requireNonNull(par, "Parameters cannot be null.");
		Objects.requireNonNull(par.getGenomeObject(), 
				"Genome Information cannot be null.");
		Objects.requireNonNull(par.getTransClustObject(),
				"TransClust parameters cannot be null.");
		Objects.requireNonNull(par.getRandomForestObject(),
				"Random Forest parameters cannot be null.");
		Objects.requireNonNull(par.getRandomForestObject());

		/* Number of threads
		if (par.getProcessors() > 0) {
			this.threads = par.getProcessors();
		}*/

		// Get working directory
		//this.workingDir = par.getLocalDir();
	}


	@Override
	public void exec() {
		//-------------------------------------//
		// Parse genomes and insert info in DB //
		//-------------------------------------//	
		logger.info("Parsing GenBank files.");
		// Lifestyle One
		Map<String, File> genomesL1 = this.par.getGenomeObject().getLifestyleOneTable();
		String nameL1 = this.par.getGenomeObject().getNameLifestyleOne();
		// Lifestyle Two
		Map<String, File> genomesL2 = this.par.getGenomeObject().getLifestyleTwoTable();
		String nameL2 = this.par.getGenomeObject().getNameLifestyleTwo();
		//
		if (this.par.getTransClustObject().getUserTransClustFile().equals("")) {
			parseG = new GenomeParser(genomesL1, nameL1, 
					genomesL2, nameL2,
					this.par.getLocalDir(), this.par.getProcessors());
			parseG.exec();					
		} else {
			parseG = new GenomeWithTransClustParser(genomesL1, nameL1, 
					genomesL2, nameL2, this.par.getTransClustObject().getUserTransClustFile(),
					this.par.getLocalDir(), this.par.getProcessors());
			parseG.exec();
			((GenomeWithTransClustParser) parseG).setClusters();
			clusters =  ((GenomeWithTransClustParser) parseG).getClusters();
		}
		genomes = parseG.getGenomes();


		//-----------------------------//
		// Run Transitivity Clustering //
		//-----------------------------//	

		if (par.getTransClustObject().getUserTransClustFile().equals("")) {
			
			//---------------------------------------//
			// Run TransClust
			logger.info("Running Transitivity Clustering.");
			//
			clust = new RunTransClust(par.getTransClustObject(), par.getLocalDir(),
					par.getBlastDir(), par.getProcessors());
			clust.exec();
					
			//---------------------------------------//
			// Update genome information with cluster id's					
			clusters = clust.getClusters();
			// Parse
			parseG = new GenomeWithTransClustParser(genomesL1, nameL1, 
					genomesL2, nameL2, clusters, par.localDir, par.getProcessors());
			parseG.exec();
			((GenomeWithTransClustParser) parseG).setClusters();
			genomes = parseG.getGenomes();
			/*
			long genesAnalyzed = clusters.size();
			if (genesAnalyzed < 300000) { 
				UpdateGenomesHashMap updated = new UpdateGenomesHashMap();
				genomes = updated.updateGenomes(genomes, clusters);
			} else {
				UpdateGenomesHashMap updated = new UpdateGenomesHashMapLarge();
				genomes = updated.updateGenomes(genomes, clusters);
			}
			 */
		}

		//-----------//
		// Run Gecko //
		//-----------//	

		/* Check if it is necessary to include Gecko  
		 * in the pipeline. 
		 */

		if (this.par.getGeckoObject() != null) {
			// Run Gecko
			logger.info("Running Gecko.");
			gecko = new RunGecko(this.par.getGeckoObject(), 
					this.par.getLocalDir());
			gecko.exec();
		}

		//-------------------//
		// Run Random Forest
		//-------------------//
		logger.info("Running Statistical Methods.");

		// Run random forest
		if (this.par.getGeckoObject()  == null) {
			learning = new RunRandomForest(par.getRandomForestObject(), 
					par.getLocalDir(), par.getRDir(), 
					par.getGenomeObject().getNameLifestyleOne(),
					par.getGenomeObject().getNameLifestyleTwo(),
					par.getProcessors());

		} else {
			learning = new RunRandomForest(par.getRandomForestObject(), 
					gecko.getGeckoOut(), par.getLocalDir(), par.getRDir(), 
					par.getGenomeObject().getNameLifestyleOne(),
					par.getGenomeObject().getNameLifestyleTwo(),
					par.getProcessors());
		}
		learning.exec();

		// End
		System.exit(0);
	}
	

	/**
	 * @return		Returns a list of all genomes 
	 * 				used in the analysis.
	 */
	public static List<Genome> getGenomes() {
		return genomes;
	}
	

	/**
	 * @return		Returns a list of all clusters 
	 * 				(TransClust output).
	 */
	public static ArrayList<String> getClusters() {
		return clusters;
	}
	

	//---------------------------//
	//   CANCELLATION ROUTINES   //
	//---------------------------//

	
	/** Cancel all processes. */
	public static void cancelled() {
		logger.info("Process interrupted. ");
		// Cancel all processes and threads
		cancellAllWorkers();
		//
		//System.exit(2);
	}
	

	/** Cancels all SwingWorkers that are not done. */
	protected static void cancellAllWorkers() {
		// Cancel Random Forest
		if (learning != null)
			learning.cancelled();
		// Cancel Gecko
		if (gecko != null)
			gecko.cancelled();
		// Cancel clustering
		if (clust != null)
			clust.cancelled();
		// Cancel parsing
		if(parseG != null)
			parseG.cancelled();			
	}
	
	
}