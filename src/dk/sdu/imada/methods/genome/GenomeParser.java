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
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import javax.swing.SwingWorker;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.biojava.nbio.core.sequence.ProteinSequence;

import dk.sdu.imada.methods.BrokePipelineException;
import dk.sdu.imada.methods.Cancelled;
import dk.sdu.imada.methods.Command;
import dk.sdu.imada.methods.ExecutorServiceUnbounded;


/**
 * Class contains methods to parse a GenBank file and 
 * retrieve the relevant information.
 * 
 * @author Eudes Barbosa
 */
public class GenomeParser extends AbstractGenomeParser implements Command, Cancelled {

	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(GenomeParser.class.getName());

	/** Lifestyle one genomes. */
	protected Map<String, File> genomesL1;

	/** Lifestyle two genomes. */
	protected Map<String, File> genomesL2;

	/** Lifestyle one name. */
	protected String nameL1;

	/** Lifestyle two name. */
	protected String nameL2;

	/** Local working directory. */
	protected String workingDir;

	/** Combined Fasta file. */
	protected String fastaFile;

	/** Number of treads [Default = 1]. */
	protected int threads = 1;

	/** 
	 * Flag to indicate if the analysis was 
	 * cancelled by the user.
	 */ 
	protected boolean processInterrupted = false;

	/** Executor Queue Service. */
	protected ExecutorServiceUnbounded executor;

	/** Count Down for multi-thread control. */ 
	protected CountDownLatch countDown;

	/** Hold protein sequences. Useful for concurrent access.  */
	protected Queue<ProteinSequence> concurrentSeqAA;

	//------  Declaration end  ------//


	/** 
	 * Parses a GenBank file and retrieve the relevant information. 
	 * 
	 * @param genomesL1		Map with lifestyle one genomes.
	 * @param nameL1		Name of lifestyle one.
	 * @param genomesL2 	Map with lifestyle two genomes.
	 * @param nameL2		Name of lifestyle two.
	 * @param localDir		Path to local working directory.
	 * @param processors	Number of available processors 
	 * 						(multi-thread).
	 */
	public GenomeParser(Map<String, File> genomesL1, String nameL1, 
			Map<String, File> genomesL2, String nameL2, 
			String localDir, int processors) {
		// Get working directory
		this.workingDir = localDir;

		// Set fasta file
		this.fastaFile = workingDir.concat(File.separator)
				.concat("CombinedSequence.fasta");
		try {
			new File(this.fastaFile).delete();
			new File(this.fastaFile).createNewFile();
			//fastaFile.deleteOnExit();
		} catch (IOException e) {
			logger.error("Problem while creating combined fasta file.");
			e.printStackTrace(); 
		}

		// Lifestyle One
		this.genomesL1 = genomesL1;
		this.nameL1 = nameL1;

		// Lifestyle Two
		this.genomesL2 = genomesL2;
		this.nameL2 = nameL2;

		// Number of threads
		if (processors > 1) {
			this.threads = processors;
		}
	}	

	@Override
	public void exec() {

		// Check if multi-thread
		if (this.threads > 1) {

			// Prepare tasks
			concurrentSeqAA = new ConcurrentLinkedQueue<ProteinSequence>();
			executor = new ExecutorServiceUnbounded(threads);			
			
			// Get GenBank files location //
			// ... for lifestyle one			
			Set<Entry<String, File>> set = genomesL1.entrySet();
			List<Entry<File, String>> listFiles = new ArrayList<Entry<File, String>>();
			for (Entry<String, File> entry : set) {
				listFiles.add(new AbstractMap.SimpleEntry<File, String>(entry.getValue(), nameL1));
			}
			// ... for lifestyle two
			set = genomesL2.entrySet();
			for (Entry<String, File> entry : set) {
				listFiles.add(new AbstractMap.SimpleEntry<File, String>(entry.getValue(), nameL2));
			}
			
			// Number of genomes
			int numGenomes = listFiles.size();
			int perThread  = (int) Math.ceil(numGenomes/threads+1);
			List<List<Entry<File, String>>> listPerThread = chopped(listFiles, perThread);

			// Create Runnable
			countDown = new CountDownLatch(listPerThread.size());
			logger.debug("@@@ CountDown : " + listPerThread.size());
			for (List<Entry<File, String>> list : listPerThread) {
				// Create Runnable
				Runnable task = createRunnable(list);
				executor.addTask(task);					
			}
			
		} else {
			ArrayList<ProteinSequence> seqAA = new ArrayList<>();

			// ... for lifestyle one
			Iterator<Entry<String, File>> it = genomesL1.entrySet().iterator();
			while (it.hasNext()) {
				//String line = "";
				//
				Map.Entry<String, File> pair = (Map.Entry<String, File>)it.next();
				//line = pair.getValue() + " \t " + nameL1;
				try {
					seqAA.addAll(parse( pair.getValue(), this.nameL1));
				} catch (Exception e) {
					String errorMsg = "Error while parsing GenBank files.";
					logger.error(errorMsg);
					try {
						throw new BrokePipelineException(errorMsg, e);
					} catch (BrokePipelineException e1) {
						e1.printStackTrace();
					}	
				}
				//		        
				it.remove(); // avoids a ConcurrentModificationException
			}

			// ... for lifestyle two
			it = genomesL2.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, File> pair = (Map.Entry<String, File>)it.next();
				//line = pair.getValue() + " \t " + nameL1;
				try {
					seqAA.addAll(parse(pair.getValue(), this.nameL2));
				} catch (Exception e) {
					String errorMsg = "Error while parsing GenBank files.";
					logger.error(errorMsg);
					try {
						throw new BrokePipelineException(errorMsg, e);
					} catch (BrokePipelineException e1) {
						e1.printStackTrace();
					}	
				}
				//		        
				it.remove(); // avoids a ConcurrentModificationException
			}
			// Create FASTA file based on GenBank files
			logger.debug("@@@ Fasta file : " + fastaFile);
			logger.debug("@@@ #Sequences : " + seqAA.size());
			//
			createFASTA(fastaFile, seqAA);
			return;
		}

		try {
			//
			countDown.await();
			// Create FASTA file based on GenBank files
			ArrayList<ProteinSequence> seqAA = new ArrayList<>();
			seqAA.addAll(concurrentSeqAA);
			logger.debug("@@@ Fasta file : " + fastaFile);
			logger.debug("@@@ #Sequences : " + seqAA.size());
			//
			createFASTA(fastaFile, seqAA);	
		} catch (InterruptedException e) {
			if (processInterrupted == false) {
				logger.error("Error while waiting for Count Down.");
				e.printStackTrace();
			}
		}
	}

	@Override
	public void cancelled() {
		// Change cancel status
		processInterrupted = true;
		// Shutdown executor
		if (executor != null)
			executor.killExecutor();
		// Remove fasta file
		//if (fastaFile.exists())
		//	fastaFile.delete();
	}

	/**
	 * @return		Returns list of Genome objects. The 
	 * 				objects are expected to be inserted 
	 * 				in the database.
	 */
	public List<Genome> getGenomes(){
		return genomes;
	}	

	/**
	 * @param genomes	List of genomes and lifestyle name.
	 * @return	Returns a Runnable for the given genome list.
	 */
	protected Runnable createRunnable(final List<Entry<File, String>> genomes) {		
		Runnable runnable = new Runnable() {	    	
			@Override
			public void run() {
				// Create inner class to run tasks in the background
				@SuppressWarnings("rawtypes")
				SwingWorker worker = new SwingWorker() {
					@Override
					protected Object doInBackground() throws Exception {
						for (Entry<File, String> s : genomes) {
							// Parse GenBank file
							concurrentSeqAA.addAll(parse(s.getKey(), s.getValue()));
						}
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
