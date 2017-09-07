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
package dk.sdu.imada.methods.statistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import dk.sdu.imada.methods.BrokePipelineException;
import dk.sdu.imada.methods.Cancelled;
import dk.sdu.imada.methods.Command;
import dk.sdu.imada.methods.ExecutorServiceUnbounded;
import dk.sdu.imada.methods.Parser;
import dk.sdu.imada.methods.genome.Genome;


/**
 * Abstract class contains basic methods to create 
 * an indicator matrix. The matrix can based either 
 * on gene clusters (BFG pipeline) or on islands 
 * (LiSSI pipeline).
 * 
 * @author Eudes Barbosa	
 */
public abstract class AbstractIndicatorMatrix extends Parser implements Command, Cancelled {

	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(AbstractIndicatorMatrix.class.getName());

	/** Unbound executor (multi-thread). */
	protected ExecutorServiceUnbounded executor;

	/** Counter associated with executor. */
	protected CountDownLatch countDown;

	/** List with all genomes used in the analysis. */
	protected List<Genome> genomes;
	
	/** Path to indicator matrix file. */
	protected String file;

	/** File lines. */
	protected HashSet<String> lines = new HashSet<>();

	/** 
	 * Map each organism with its (pontentially) multiply genome 
	 * accession numbers.
	 */
	protected HashMap<String, HashSet<String>> genomeMap;

	/** Flag to indicate if the process was interrupted. */
	protected boolean processInterrupted = false;


	//------  Declaration end  ------//	
	


	/**
	 * Creates Random Forest directory. Also, it removes 
	 * previous results if necessary.
	 * 
	 * @param localDir Path to local working directory.
	 */
	protected void createRFdir(String localDir) {
		// Create RF directory
		String path = localDir.concat(File.separator).concat("RandomForest");		
		// Remove previous Random Forest results
		File dir = new File(path);
		if (dir.exists()) {
			try {
				FileUtils.deleteDirectory(dir);
				dir.mkdirs();
			} catch (IOException e) {
				logger.error("Failed to remove prIslandevious Random Forest results directory.");
			}
		} else {
			dir.mkdirs();
		}
		//-----------------------------------------------------------------------//
		// Set indicator matrix file 
		this.file = path.concat(File.separator).concat("IndicatorMatrix.csv");
		FileUtils.deleteQuietly(new File(file));
		try {
			new File(file).createNewFile();
		} catch (IOException e) {
			logger.debug("Failed to create indicator matrix file.");
			e.printStackTrace();
		}
	}


	@Override
	public void cancelled() {
		// Cancel slowest loop
		this.processInterrupted = true;
		// Delete indicator matrix file
		FileUtils.deleteQuietly(new File(file));	
	}

	/**
	 * @return	Returns path to indicator matrix.
	 */
	public String getFile() {		
		return file;
	}


	/** Associates organisms with multiple genomes (accession numbers). */
	abstract protected void organizeGenomes();

	/**
	 * Appends the string to the file.
	 * @param file	Output file.
	 * @param line	File lines.
	 */
	protected void appendOutput(final String file, final HashSet<String> lines) {
		try {
			FileWriter fw = new FileWriter(file, true);
			for (String l : lines) {
				fw.write(l); 	
				fw.write("\n");
			}
			//
			fw.close();
		} catch (IOException e) {
			String message = "Error while writing indicator matrix into file.";
			logger.error(message);
			try {
				throw new BrokePipelineException(message, e);
			} catch (BrokePipelineException e1) {
				return;
			}
		}
	}

	/** 
	 * Verifies if there are no duplicated lines in file.
	 * @param file	Path to indicator matrix.
	 */
	protected void verifyIndicatorMatrix(String file) {
		LinkedHashSet<String> uniquelines = new LinkedHashSet<>();
		BufferedReader br = getBufferedReader(file);
		String line = null;  
		try {
			while ((line = br.readLine()) != null) {
				uniquelines.add(line);
			}
			FileWriter fw = new FileWriter(file);
			for (String l : uniquelines) {
				fw.write(l); 	
				fw.write("\n");
			}
			//
			fw.close();
		} catch (IOException e) {
			String message = "Error while writing indicator matrix into file.";
			logger.error(message);
			try {
				throw new BrokePipelineException(message, e);
			} catch (BrokePipelineException e1) {
				return;
			}
		}		
	}


	/** 
	 * Parses list of genomes into indicator matrix format.
	 * @param genomes	List of genomes.
	 */
	protected abstract void printLines(List<Genome> genomes);

	/**
	 * @param genomes	List of genomes and lifestyle name.
	 * @return	Returns a Runnable for the given genome list.
	 */
	protected Runnable createRunnable(final List<Genome> listgenomes) {		
		Runnable runnable = new Runnable() {	    	
			@Override
			public void run() {
				// Create inner class to run tasks in the background
				@SuppressWarnings("rawtypes")
				SwingWorker worker = new SwingWorker() {
					@Override
					protected Object doInBackground() throws Exception {
						//HashSet<String> parsedLines = 
						printLines(listgenomes);
						//lines.addAll(parsedLines);
						//
						return null;
					}
					protected void done() {
						countDown.countDown();
					}
				};
				worker.execute();			
			}
		};
		//
		return runnable;
	}
}