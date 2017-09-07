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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import dk.sdu.imada.methods.BrokePipelineException;
import dk.sdu.imada.methods.Cancelled;
import dk.sdu.imada.methods.Command;
import dk.sdu.imada.methods.ExecutePipeline;
import dk.sdu.imada.methods.ExecutorServiceUnbounded;

import dk.sdu.imada.methods.genome.Gene;
import dk.sdu.imada.methods.genome.Genome;


/**
 * Class creates indicator matrix to be used in the 
 * machine learning process (Random Forest). The 
 * matrix will either be created based on Transitivity 
 * Clustering or Gecko results.
 * If cluster/island is present in organism, matrix receive 
 * the value one ('1'), and zero ('0') otherwise. 
 * 
 * @author Eudes Barbosa
 *
 */
public class IndicatorMatrix extends AbstractIndicatorMatrix implements  Command, Cancelled {

	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(IndicatorMatrix.class.getName());

	/** Array with all clusters (TransClust - BFG). */
	protected ArrayList<String> clusters;

	/** Distinct clusters (sorted) */
	protected ArrayList<String> uniqueClusters;
	
	/** Path to local working directory. */
	protected String localDir;

	/** Number of threads to use. */
	protected int threads = 1;
	
	//------  Declaration end  ------//


	/**
	 * Creates an indicator matrix based on 
	 * TransClust results.
	 * 
	 * @param localDir Path to local working directory.
	 * @param threads  Number of threads.
	 */ 
	public IndicatorMatrix(String localDir, int threads) {
		this.genomes  = ExecutePipeline.getGenomes();
		this.clusters = ExecutePipeline.getClusters();
		this.localDir = localDir;
		this.threads  = threads;
	}

	@Override
	public void cancelled() {
		// Cancel slowest loop
		this.processInterrupted = true;
		// Delete indicator matrix file
		FileUtils.deleteQuietly(new File(file));	
	}

	@Override
	public void exec() {	
		//
		logger.info("Creating indicator matrix based on gene clusters results.");
		createRFdir(this.localDir);
		transclustIM();
	}

	/**
	 * Generates an indicator matrix based on 
	 * Transitivity Clustering results (individual 
	 * genes).
	 */
	protected void transclustIM() {
		// Get distinct clusters (sorted)
		uniqueClusters = getUniqueClusters();
		// Associate organisms with multiple genomes (accession numbers)
		organizeGenomes();

		// Create file header			
		String row = "name;lifestyle";
		for (String c : uniqueClusters) {
			row = row + ";" + c ;
		}
		// Append the string to the file
		try {
			FileWriter fw = new FileWriter(file, true);
			fw.write(row); 	
			fw.write("\n");
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

		//--------------------------------------------------------------//
		// Check if multi-thread
		if (this.threads > 1) {
			executor = new ExecutorServiceUnbounded(this.threads);
			int perThread = (int) Math.ceil(genomes.size()/this.threads+1);
			List<List<Genome>> listPerThread = chopped(genomes, perThread);
			countDown = new CountDownLatch(listPerThread.size());
			for (List<Genome> list : listPerThread) {
				// Create Runnable
				Runnable task = createRunnable(list);
				executor.addTask(task);					
			}

			// Append to file
			try {
				countDown.await();
				verifyIndicatorMatrix(file);
			} catch (InterruptedException e) {
				if (processInterrupted == false) {
					logger.error("Error while waiting for Count Down.");
					e.printStackTrace();
				}
			}

		} else {
			// Verify if genomes have clusters
			for (Genome g : genomes) {
				// Create each row
				String name = g.getName();
				String lifestyle = g.getLifestyle();
				row = name.replaceAll(" ", "_") + ";" + lifestyle;	

				// Get all clusters present in a given organism
				HashSet<String> genomeClusters = genomeMap.get(g.getName());
				// Iterate through clusters
				for (String c : uniqueClusters) {				
					boolean contains = genomeClusters.contains(c);
					if (contains) {
						row = row + ";1";
					} else {
						row = row + ";0";
					}				
				}
				lines.add(row);
			}
			// Append to file
			appendOutput(file, lines);
		}
	}

	/**
	 * @return		Returns a set of unique clusters for a 
	 * 				given genome.
	 */
	private ArrayList<String> getAllClusters(Genome g) {
		ArrayList<String> clusters = new ArrayList<String>();
		Set<String> distinct = new LinkedHashSet<String>();
		// Iterate through all genes
		for (Gene gene : g.getGenes()) {
			distinct.add(""+gene.getCluster());
		}
		clusters.addAll(distinct);
		//
		return clusters;
	}

	/**
	 * @return		Returns set of unique clusters.
	 */
	private ArrayList<String> getUniqueClusters() {
		// Initialize variables
		ArrayList<String> uniqueClusters = new ArrayList<String>();
		Set<String> distinct = new LinkedHashSet<String>();
		// Iterate through clusters
		for(String line : clusters){
			String[] data = line.split("\t");
			distinct.add(data[1]);			
		}
		uniqueClusters.addAll(distinct);		
		// Sort
		Collections.sort(uniqueClusters);
		//
		return uniqueClusters;
	}

	@Override
	protected void printLines(List<Genome> genomes) {
		HashSet<String> lines = new HashSet<>();
		String row;
		// Verify if genomes have clusters
		for (Genome g : genomes) {
			// Create each row
			String name = g.getName();
			String lifestyle = g.getLifestyle();
			row = name.replaceAll(" ", "_") + ";" + lifestyle;
			// Get all clusters present in a given organism
			HashSet<String> genomeClusters = genomeMap.get(g.getName());
			// Iterate through clusters
			for (String c : uniqueClusters) {				
				boolean contains = genomeClusters.contains(c);
				if (contains) {
					row = row + ";1";
				} else {
					row = row + ";0";
				}				
			}
			//
			lines.add(row);
		}
		//
		appendOutput(file, lines);	
	}

	@Override
	protected void organizeGenomes() {
		HashSet<String> organismNames = new HashSet<>();
		for (Genome g : genomes) {
			String name = g.getName();
			organismNames.add(name);
		}
		//
		genomeMap = new HashMap<>();
		for (String name : organismNames) {
			HashSet<String> list = new HashSet<>();
			for (Genome g : genomes) {
				if (name.equals(g.getName())) {
					list.addAll(getAllClusters(g));
				}
			}
			//
			genomeMap.put(name, list);
		}		
	}
}
