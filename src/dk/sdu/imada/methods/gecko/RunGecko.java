/**
 *
 */
package dk.sdu.imada.methods.gecko;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import dk.sdu.imada.methods.BrokePipelineException;
import dk.sdu.imada.methods.Cancelled;
import dk.sdu.imada.methods.Command;
import dk.sdu.imada.methods.UnzipUtility;



/**
 * Class runs Gecko with user defined parameters.
 * 
 * @author Eudes Barbosa
 */
public class RunGecko extends dk.sdu.imada.methods.Run implements Command, Cancelled {

	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(RunGecko.class.getName());

	/** Thread associated with Gecko. */
	protected Thread run;

	/** Gecko object (parameters). */
	protected Gecko gecko;

	/** Path to Gecko output file. */
	protected String outFile = "";

	/** Path to local working directory. */
	protected String localDir;

	/** Flag to indicate if the process was interrupted. */
	protected boolean processInterrupted = false;

	//------  Declaration end  ------//



	/**
	 * Runs Gecko using user defined parameters.
	 * 
	 * @param g				Gecko object (parameters).
	 * @param localDir		Path to local working directory.
	 */
	public RunGecko(Gecko g, String localDir) {
		this.gecko = g;
		this.localDir = localDir;
	}
	

	@Override
	public void cancelled() {
		processInterrupted = true;
		// Interrupt thread
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
		// If file was provided, skip process
		if (!gecko.getUserGeckoFile().equals("")) {
			outFile = gecko.getUserGeckoFile();
			//
			return;

		} else {

			// Create input file
			logger.info("Creating Gecko input file.");
			GeckoInput geckoIn;
			String input = "";
			try {
				geckoIn = new GeckoInput(this.localDir);
				input = geckoIn.getInputPath();
			} catch (BrokePipelineException e) {
				logger.error("Failed to create Gecko input file.");
				e.printStackTrace();
			}
			// Get temporary directory
			String property = "java.io.tmpdir";
			String tmpDir = System.getProperty(property);

			// Update progress bar and status
			logger.info("Starting Gecko application.");

			// Run Gecko
			loadGeckoFiles(tmpDir);
			String geckoVersion = getSystemProperties();
			runGecko(input,geckoVersion, tmpDir);		
		}
	}


	/**
	 * Runs Gecko.
	 * 
	 * @param geckoVersion		Gecko version based on system 
	 * 							characteristics. 
	 * @param input				Path to Gecko input file.
	 * @param tmpDir			Path to temporary directory.
	 */
	protected void runGecko(String input, String geckoVersion, String tmpDir) {
		// Create Gecko folder
		String geckoDir =  this.localDir.concat(File.separator).concat("Gecko");
		File dir = new File(geckoDir);
		if(!dir.exists())
			dir.mkdirs();

		// Get executable path
		String execFile = tmpDir.concat(File.separator).concat("Gecko3")
				.concat(File.separator).concat("bin").concat(File.separator)
				.concat(geckoVersion);
		outFile = geckoDir.concat(File.separator).concat("GeckoOut.gck");

		// Run chmod to Gecko file
		String type = "chmod";
		List<String> process = java.util.Arrays.asList("chmod", "777", execFile);
		// Run
		if (processInterrupted == false)
			run = start(process, type);

		//-------------------------------------------------
		// Run Gecko process
		type = "Gecko";
		process = java.util.Arrays.asList(execFile,					//executable
				"-in", input,										//input file
				"-d", ""+gecko.getDistance(),						//distance
				"-q", ""+gecko.getGenomes(),						//#genomes
				"-s", ""+gecko.getSize(),							//size
				"--resultOutput", "clusterData", "showFiltered", 	//out format 
				outFile);											//out file
		// Run
		if (processInterrupted == false)
			run = start(process, type);
	}


	/**
	 * Verifies operational system and architecture (32 or 64 bit), 
	 * and sets the right version of Gecko to be executed.
	 */
	private String getSystemProperties() {
		String geckoVersion = null;

		String arch = System.getProperty("sun.arch.data.model"); //get architecture 
		String os = (System.getProperty("os.name")).toUpperCase(); //get OS

		// Verify operational system
		if (os.contains("WIN")) {	//if Windows
			if(arch.contains("32"))
				geckoVersion = "Gecko3-32bit.bat";
			else
				geckoVersion = "Gecko3.bat";
		} else if (os.contains("LINUX")) {	//if Linux
			if(arch.contains("32"))
				geckoVersion = "Gecko3-32bit";
			else
				geckoVersion = "Gecko3";			
		} else {	//assume Mac
			geckoVersion = "Gecko3";
		}
		//
		return geckoVersion;
	}


	/**
	 * Loads Gecko files into temporary directory.
	 * 
	 * @param tmpDir	Path to temporary directory.
	 */
	private void loadGeckoFiles(String tmpDir) {
		// Get file url
		ClassLoader cLoader = RunGecko.class.getClassLoader();
		URL inputUrlGecko = cLoader.getClass().getResource("/res/Gecko3.zip");
		File geckoZip;		
		try {
			// Load Gecko zip file
			geckoZip = File.createTempFile("gecko", ".zip");
			geckoZip.deleteOnExit();
			logger.debug("ZIP : " + geckoZip);
			logger.debug("URL : " + inputUrlGecko);
			FileUtils.copyURLToFile(inputUrlGecko, geckoZip);

			// Unzip file into temporary directory
			UnzipUtility unzip = new UnzipUtility();
			unzip.unzip(geckoZip.getAbsolutePath(), tmpDir);

		} catch (IOException e) {
			logger.debug("Error while loading Gecko zip file into temporary directory");
			e.printStackTrace();
		}
	}


	/** @return	Returns path to	Gecko output. */
	public String getGeckoOut(){
		return outFile;
	}
}
