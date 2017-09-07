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


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.sdu.imada.methods.BrokePipelineException;
import dk.sdu.imada.methods.Cancelled;
import dk.sdu.imada.methods.Command;
import dk.sdu.imada.view.transclust.ViewResults;


/**
 * Class runs BLAST followed by Transitivity Clustering. 
 * 
 * @author Eudes Barbosa
 *
 */
public class RunTransClust extends dk.sdu.imada.methods.Run implements Command, Cancelled {

	
	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(RunTransClust.class.getName());

	/** Flag to indicate if the process was interrupted. */
	protected boolean processInterrupted = false;

	/** Actual time required to run Blast. */
	protected long runtimeBlastReal = 0;

	/** Estimated time required to run Blast.  */
	protected long runtimeBlastEstimated = 0;

	/** Actual time required to run TransClust. */
	protected long runtimeTransClust = 0;

	/** TransClust object. */
	protected TransClust tc;

	/** Blast object. */
	protected Blast b;

	/** Path to Blast bin directory. */
	protected String blastBin;

	/** Thread.*/
	protected Thread run;

	/** Path to TransClust directory. */
	protected String transClustDir;

	/** Path to local working directory. */
	protected String localDir;

	/** Path to TransClust jar file. */
	protected String jar;

	/** 
	 *  Path to Similarity matrix file.
	 *  Required for TransClust. 
	 */
	protected String simFile;

	/** Path to combined fasta file. */
	protected String fasta;

	/** Path to Blast output. */
	protected String blastOutFile;

	/** Number of threads to use. */
	protected int threads = 1;

	/** Path to TransClust output. */
	protected String output = "";
	
	/** Array list with cluster information. */
	protected ArrayList<String> clusters = null;


	//------  Declaration end  ------//

	/**
	 * Runs BLAST and Transitivity Clustering using 
	 * system calls.
	 * 
	 * @param transclust	Transitivity Clustering 
	 * 						object (parameters).
	 * @param localDir		Path to local working directory.
	 * @param blastBin		Path to Blast binary files.
	 * @param threads		Number of threads.	
	 */
	public RunTransClust(TransClust transclust, String localDir, 
			String blastBin, int threads) {
		// Get variables
		this.tc = transclust;
		this.localDir = localDir;
		this.blastBin = blastBin;
		this.transClustDir = localDir.concat(File.separator).concat("TransClust");		
		this.fasta = localDir.concat(File.separator).concat("CombinedSequence.fasta");
		this.threads = threads;
	}

	
	@Override
	public void cancelled() {
		// Change status
		processInterrupted = true;
		// Clean Blast process
		if(b != null)
			b.cancelled();
		// Interrupt TC thread
		if(run != null && run.isAlive()) {
			run.interrupt();
			try {
				run.join();
			} catch (InterruptedException e) {
				logger.error("Error while waiting for thread to die...gracefully.");
				e.printStackTrace();
			}
		}
	}

	
	@Override
	public void exec() {	
		//
		//logger.info("Running Transitivity Clustering.");
		// TransClust diretory
		File dir = new File(transClustDir);
		if (dir.exists()) {
			try {
				FileUtils.deleteDirectory(dir);
			} catch (IOException e) {
				logger.error("Failed to remove previous TransClust results directory.");
			}
		}

		// Run Blast on the sequences
		if (tc.getUserBlastFile().equals("") 
				&& tc.getUserTransClustFile().equals("")) {
			dir.mkdirs();
			blast();
		} else {
			blastOutFile = tc.getUserBlastFile();
		}
		// Verify if single or multiple run
		if (tc.isRange() == false && processInterrupted == false) {
			// Run TransClust
			TransClust tc = new TransClust();
			tc.setStart(this.tc.getStart());
			tc.setRange(false);
			if (this.tc.getUserTransClustFile().equals("")) {
				// Create folder if necessary
				if(!dir.exists())
					dir.mkdirs();
				// Prepare TC required files
				loadTCFiles();
				// Get path to output
				output = clusterSingle(tc.getStart());	
				tc.setRuntimeTransClust(runtimeTransClust);
				if (b != null) {
					tc.setRuntimeBlastEstimated(runtimeBlastEstimated);
					tc.setRuntimeBlastReal(runtimeBlastReal);						
				}
				if (processInterrupted == false)
					logger.info("Transitivity Clusterig output saved at: " + output);				 

			} else {
				output = this.tc.getUserTransClustFile();					
				logger.info("Transitivity Clusterig file provided by the user: " + output);
			}
			// Store results
			if (!output.equals("Not provided.") && processInterrupted ==  false) {
				ParseTransClustResults parseTC = new ParseTransClustResults(output);
				if (processInterrupted == false) {
					parseTC.exec();					
					clusters = parseTC.getList();
					
					// View results - send TransClust object
					ViewResults view = new ViewResults(tc, clusters, localDir);
					view.exec();	
					
				}

			}
		}
	}
	

	/**
	 * Runs Transitivity Clustering using a single threshold. 
	 * @param 	Density parameter (threshold).
	 */
	protected String clusterSingle(int threshold) {
		// Update progress bar and status
		logger.info("Clustering gene products (threshold = "+threshold+")");

		// Create Cost Matrix
		List<String> process = java.util.Arrays.asList(
				"java", "-Xss512M",		//VM configuration
				"-cp", jar,					//TransClust jar file
				"de.costmatrixcreation.main.CostMatrixCreator", "-gui",
				"-f", fasta,				//Fasta file
				"-b", blastOutFile,				//Blast output
				"-c", transClustDir,		//Output directory
				"-s", simFile,				//Similarity matrix
				"-t", ""+threshold);		//Threshold
		String type = "Transitivity Clustering - creating Cost Matrices";
		// Run
		long time = System.currentTimeMillis();
		if (processInterrupted == false)
			run = start(process, type);

		//---------------------------------------------------------------
		// Create cluster
		String output = transClustDir.concat(File.separator)
				.concat("TransClust_"+threshold+".cls");
		FileUtils.deleteQuietly(new File(output));	//delete file
		//
		process = java.util.Arrays.asList(  
				"java", "-jar", jar,		//TransClust jar file
				"-i", transClustDir,		//Output directory
				"-o", output);				//Output file
		//
		type = "Transitivity Clustering ("+threshold+")";
		// Run
		if (processInterrupted == false) 
			run = start(process, type);

		// Get required time
		runtimeTransClust = System.currentTimeMillis() - time;

		//
		return output;
	}
	

	/**
	 * Loads Transitivity Clustering (TC) required files. It 
	 * loads TC jar into the system's temporary directory; and 
	 * creates a file to store the similarity matrix (if file isn't 
	 * created TC crashes).
	 */
	protected void loadTCFiles() {
		//
		ClassLoader cLoader = RunTransClust.class.getClassLoader();
		try {
			// Load Transitivity Cluster jar into temporary folder
			URL inputUrl = cLoader.getClass().getResource("/res/TransClust.jar");
			File tempTCjar = File.createTempFile("TransClust", ".jar");
			tempTCjar.deleteOnExit();
			FileUtils.copyURLToFile(inputUrl, tempTCjar);
			this.jar = tempTCjar.getAbsolutePath().toString();

			// Create SimMatrix file
			this.simFile = transClustDir.concat(File.separator).concat("SimilarityMatrix.txt");
			File f = new File(this.simFile);
			FileUtils.deleteQuietly(f);
			f.createNewFile();

		} catch (IOException e) {
			String message = "Error while loding Transitivity Clustering files. ";
			logger.error(message);
			try {
				throw new BrokePipelineException(message, e);
			} catch (BrokePipelineException e1) {
				e1.printStackTrace();
			}
		}
	}
	

	/** Calls a BLAST All vs. All on the sequences. */
	protected void blast() {
		// Set parameters
		Double evalue = tc.getEvalue();
		// Execute BLAST
		long time = System.currentTimeMillis();
		b = new Blast(this.blastBin, this.localDir, 
				this.fasta, evalue, this.threads);
		b.exec();	
		this.blastOutFile = b.getOutput();
		// Get required time
		runtimeBlastReal = System.currentTimeMillis() - time;
	}
	

	/**
	 * @return		Returns the clusters generated by 
	 * 				TransClust as an array list. Format: 
	 * 				Gene Identifier <\TAB\> Cluster Identifier. 
	 */
	public ArrayList<String> getClusters() {
		return clusters;
	}	
	

	/** Runs Transitivity Clustering using a range of threshold. 
	protected void clusterRange() {
		// TODO Auto-generated method stub
	}*/
	
}
