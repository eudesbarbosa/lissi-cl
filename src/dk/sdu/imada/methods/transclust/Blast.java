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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import dk.sdu.imada.methods.Cancelled;
import dk.sdu.imada.methods.Command;


/**
 * Class contains methods do run Blast.
 * 
 * @author Eudes Barbosa
 */
public class Blast extends dk.sdu.imada.methods.Run implements Command, Cancelled {

	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(Blast.class.getName());

	/** Flag to indicate if Blast is done. */
	protected boolean blastingDone = false;

	/** Path to Blast bin files. */
	protected String bin;

	/** Path to local working directory.*/
	protected String localDir;

	/** Path to combined Fasta file. */
	protected String fasta;

	/** Path to output file. */
	protected String output;

	/** Blast parameter DB name. */
	protected String dbName;

	/** Blast parameter e-value. */
	protected String evalue;	

	/** Number of threads to use. */
	protected int threads = 1;

	/** Thread. */
	protected Thread run;

	/** Flag to indicate if the process was interrupted. */
	protected boolean processInterrupted;


	//------  Declaration end  ------//

	/**
	 * Creates a Blast database and runs an all vs. all analysis 
	 * using the provided fasta file.
	 * 
	 * @param bin		Path to Blast binary files.
	 * @param localDir	Path to working directory.
	 * @param fasta		Path to fasta file that shall be analyzed.
	 * @param evalue 	E-value.
	 * @param threads	Number of threads.
	 */
	public Blast(String bin, String localDir,
			String fasta, Double evalue, int threads) {

		// Path to Blast binary files
		this.bin = bin;

		// Create folder for Blast results
		this.localDir = localDir.concat(File.separator).concat("TransClust");
		File dir = new File(this.localDir);
		dir.mkdirs();

		// Create directory for Blast temporary files
		String property = "java.io.tmpdir";
		String tempDir = System.getProperty(property);
		//
		String tmpDir = tempDir.concat(File.separator).concat("DB");
		dir = new File(tmpDir);
		dir.mkdirs();

		// Set path to combined fasta file
		this.fasta = fasta;

		// Set E-value
		this.evalue = ""+evalue;

		// Set path to output
		this.output = localDir.concat(File.separator).concat("Blast.out");
		FileUtils.deleteQuietly(new File(this.output));

		// Set database name
		this.dbName = tmpDir.concat(File.separator).concat("DB");

		// Get number of CPUs to be used
		this.threads = threads;

		// Start 'cancel'
		processInterrupted = false;
	}

	@Override
	public void cancelled() {
		// Change status
		processInterrupted = true;		

		// Kill processes
		if (run != null && run.isAlive()) {
			run.interrupt(); 
			try {
				run.join();
			} catch (InterruptedException e) {
				logger.error("Error while waiting for thread to die...gracefully.");
				e.printStackTrace();
			} catch (NullPointerException ne) {
				return; //do nothing...
			}
		}

	}

	@Override
	public void exec() {

		// Run MakeBlastDB
		String bin = this.bin.concat(File.separator).concat("makeblastdb");
		List<String> makeDB = java.util.Arrays.asList(bin,
				"-in", fasta,		//fasta file 
				"-dbtype","prot",	//db type
				"-out", dbName);	//db name
		if (processInterrupted == false)
			run = start(makeDB, "MakeDB");

		// Run Blast
		bin = this.bin.concat(File.separator).concat("blastp");
		List<String> b = java.util.Arrays.asList(bin,
				"-db", dbName,					//db name
				"-query", fasta,				//fasta query 
				"-out", output,					//output name
				"-evalue", evalue,				//evalue 
				"-outfmt", "6",					//out format
				"-num_threads", ""+this.threads);	//#processors
		if (processInterrupted == false)
			run = start(b, "Blast All-vs.-All");
		//
		blastingDone = true;
	}	

	/** @return	Returns path to Blast output file. */
	public String getOutput() {
		return output;
	}
}
