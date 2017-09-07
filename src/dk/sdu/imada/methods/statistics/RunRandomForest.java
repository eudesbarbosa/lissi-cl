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
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import dk.sdu.imada.methods.Cancelled;
import dk.sdu.imada.methods.Command;
import dk.sdu.imada.view.statistics.ViewDistribution;


/**
 * Class initiates a Random Forest analysis based 
 * on the parameters defined by the user.
 * 
 * @author Eudes Barbosa	
 */
public class RunRandomForest extends RunRscript implements Command, Cancelled {

	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(RunRandomForest.class.getName());

	/** Thread to run RScript. */
	protected Thread run;

	/** Random Forest object (parameters). */
	protected RandomForest rf;

	/** Path to Gecko output file. */
	protected String geckoOut = "";

	/** Path to R binary files. */
	protected String rBin = "";

	/** Path to local directory (store results). */
	protected String randomForestDir = "";

	/* Path to temporary directory. */
	//protected String tmpDir = "";

	/** Main RScript file. */
	protected File mainR;

	/** Packages test RScript file. */
	protected File testR;

	/** Path to indicator matrix file. */
	protected static String matrixFile = "";

	/** Path to local working directory. */
	protected String localDir;

	/** Lifestyle one name. */
	protected String nameL1;

	/** Lifestyle two name. */
	protected String nameL2;

	/** Flag to indicated that Gecko was used or not. */
	protected boolean gecko = false;

	/** Flag to indicate if the process was interrupted. */
	protected boolean processInterrupted = false;

	/** Flag to indicated error while running RScripts. */
	protected static boolean error = false;

	/** Number of threads to use. */
	protected int threads = 1;

	/** List of R resource files. */
	protected final List<String> resourceFiles = Arrays.asList("helper_methods.R", 
			"useful_with_BoxPlot.R", "method_randomForest.R", "Classification.R", 
			"Feature_Selection.R", "Decision_Tree.R");


	//------  Declaration end  ------//



	/**
	 * Starts a Random Forest (RF) analysis. It takes the 
	 * parameters as a RF object and the indicator matrix is 
	 * set as a path.
	 * 
	 * @param rf		Random Forest object with required 
	 * 					parameters.
	 * @param localDir	Path to local working directory.
	 * @param rBin		Path to R binary files.
	 * @param nameL1	Name of lifestyle one.
	 * @param nameL2	Name of lifestyle two.
	 * @param threads		Number of threads.
	 */
	public RunRandomForest(RandomForest rf, String localDir, String rBin, 
			String nameL1, String nameL2, int threads) {
		this.gecko = false;
		this.rf = rf;
		this.randomForestDir = localDir.concat(File.separator).concat("RandomForest");
		this.localDir = localDir;
		this.rBin = rBin;
		this.nameL1 = nameL1;
		this.nameL2 = nameL2;
		this.threads = threads;
	}

	/**
	 * Starts a Random Forest (RF) analysis. It takes the 
	 * parameters as a RF object and the indicator matrix is 
	 * set as a path.
	 * 
	 * @param rf			Random Forest object with required 
	 * 						parameters.
	 * @param geckoOut		Path to Gecko output file.
	 * @param localDir		Path to local working directory.
	 * @param rBin			Path to R binary files.
	 * @param nameL1	Name of lifestyle one.
	 * @param nameL2	Name of lifestyle two.
	 * @param threads		Number of threads.
	 * 	
	 */
	public RunRandomForest(RandomForest rf, String geckoOut, 
			String localDir, String rBin, 
			String nameL1, String nameL2, int threads) {
		//
		this.gecko = true;
		this.geckoOut = geckoOut;
		this.rf = rf;
		this.localDir = localDir;
		this.randomForestDir = localDir.concat(File.separator).concat("RandomForest");
		this.rBin = rBin;
		this.nameL1 = nameL1;
		this.nameL2 = nameL2;
		this.threads = threads;

	}

	@Override
	public void exec() {	
		
		//----------------------------------------------------//
		// Creates indicator matrix based either on TransClust
		// or Gecko results. Random Forest directory is created 
		// in this step.
		AbstractIndicatorMatrix im = null;
		if (this.gecko == true) {
			im = new IndicatorMatrixGecko(this.geckoOut, 
					this.localDir, this.threads);
		} else {
			im = new IndicatorMatrix(this.localDir, this.threads);
		}
		im.exec();
		matrixFile = im.getFile();

		//----------------------------------------------------//
		// Plot joint distribution
		ViewDistribution vd = new ViewDistribution(matrixFile, this.gecko, this.nameL1,
				this.nameL2, this.localDir); 
		vd.exec();

		// Load R scripts
		loadScripts();

		// Verify if system has all required libraries
		List<String> process = java.util.Arrays.asList(
				rBin.concat(File.separator).concat("Rscript"),		//R bin
				testR.getAbsolutePath());							//test script
		String type = "R packages test";
		logger.debug("@@@ " + testR.getAbsolutePath());
		// Run
		if (processInterrupted == false) {
			run = start(process, type); 
		}
		// If all packages present, run
		if (error == false) {
			// Load resource
			Path tempDir = loadResource();		
			// Send parameters
			String table = parametersTable(matrixFile, tempDir.toString());
			// Run process
			process = java.util.Arrays.asList(
					rBin.concat(File.separator).concat("Rscript"),	 		//R bin
					mainR.getAbsolutePath(),								//main script path
					table);													//file with parameters

			/*logger.info("INFO :");
			for(String s : process) 
				System.out.println(s);
			 */
			
			type = "Random Forest";
			// Run
			if (processInterrupted == false) {
				logger.debug("@@@ " + mainR.getAbsolutePath());
				run = start(process, type);
			} else {
				return;
			}

		} else if (processInterrupted == true) {
			// Report error
			showErrorPackages();
		}
	}

	/**
	 * Creates a file and stores R required parameters in it. 
	 * @param matrixFile 	Path to indicator matrix file.
	 * @param tempDir	Path to temporary directory.
	 */
	protected String parametersTable(String matrixFile, String tempDir) {
		// Create file to store parameters
		String table = randomForestDir.concat(File.separator).concat("parameters.tab");
		File file = new File(table);
		try {
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			fw.write("parameters;value\n");							//header
			fw.write("cores;" + this.threads +"\n");						//cores
			fw.write("kfold;" + rf.getKfold() +"\n");				//kfold
			fw.write("trees;" + rf.getTrees() +"\n");				//trees
			fw.write("runs;" + rf.getRuns() +"\n");					//runs
			fw.write("matrix;" + matrixFile +"\n");					//indicator matrix
			fw.write("dir;" + randomForestDir
					.concat(File.separator) +"\n");					//save directory
			fw.write("src;" + tempDir.concat(File.separator));		//source files
			fw.close();
		} catch (IOException e) {
			logger.debug("Error while creating R parameters table (file).");
			e.printStackTrace();
		}
		//
		return table;
	}

	/** 
	 * @return Loads R source files from jar file into temporary directory,
	 * and returns Path to temporary directory. 
	 */
	protected Path loadResource() {
		/* Create path to all files
		File baseDir = new File(tempDir);
		if (baseDir.exists()) {			
			FileUtils.deleteQuietly(baseDir);			
		}
		baseDir.mkdirs();
		//baseDir.deleteOnExit();
		 */
		Path tempDir;
		try {
			tempDir = Files.createTempDirectory("res");
			// Load files from jar to disk
			//String Sourcefolder = baseDir.getAbsolutePath().toString();		
			for (String res : this.resourceFiles) {
				// Create file
				String fileName = tempDir.toAbsolutePath().toString()
						.concat(File.separator).concat(res);
				File file = new File(fileName);// Stop visualization
				file.deleteOnExit();

				// Set path to folder within jar
				ClassLoader cLoader = RunRandomForest.class.getClassLoader();
				String input = File.separator.concat("res").concat(File.separator).concat(res);
				InputStream link = (cLoader.getClass().getResourceAsStream(input));
				// Copy files to disk
				try {
					if (!file.exists()) {
						Files.copy(link, file.getAbsoluteFile().toPath());
					}
				} catch (IOException e) { 
					logger.debug("Error while loading R source files into temporary directory.");
					e.printStackTrace(); 
				}
			}	
			//
			return tempDir;
			
		} catch (IOException e1) {
			logger.debug("Error while creating temporary directory.");
			e1.printStackTrace(); 
		}
		//
		return null;
	}

	/** Loads R scripts from jar file into system's temporary directory. */
	protected void loadScripts() {
		// Get R scripts urls
		ClassLoader cLoader = RunRandomForest.class.getClassLoader();
		URL inputUrlTest= cLoader.getClass().getResource("/res/test.R");
		URL inputUrlMain = cLoader.getClass().getResource("/res/main.R");
		//
		try {
			// Load Test script (verify all libraries)
			testR = File.createTempFile("test", ".R");
			testR.deleteOnExit();
			FileUtils.copyURLToFile(inputUrlTest, testR);
			// Load Main script (coordinator)
			mainR = File.createTempFile("main", ".R");
			mainR.deleteOnExit();
			FileUtils.copyURLToFile(inputUrlMain, mainR);			

		} catch (IOException e) {
			logger.debug("Error while loading R scripts into temporary directory");
			e.printStackTrace();
		}
	}

	/** @return	Returns path to indicator matrix file. */
	public static String indicatorMatrixPath() {
		return matrixFile;
	}

	//-----------------------------------//
	// 	     CANCELLATION ROUTINES       //
	//-----------------------------------//


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

	/** Indicates error with required R packages. */
	public static void error(){
		error = true;
	}

	/** Displays error associated with libraries. */
	private void showErrorPackages() {
		logger.error("Error while executing R test script (libraries)");
		// Send error message
		String errorMessage = "Please verify if all of the following R packages are installed:\n" 
				+ "- foreach;\n"
				+ "- doMC;\n"
				+ "- rpart;\n"
				+ "- XML;\n"
				+ "- pmml;\n"
				+ "- rattle;\n"
				+ "- snow;\n"
				+ "- stringr;\n"
				+ "- varSelRF;\n"
				+ "- randomForest;\n"
				+ "- ggplot2;\n"
				+ "- caret;\n"
				+ "- gdata;\n"
				+ "- ROCR;\n"
				+ "- pROC;\n"
				+ "- graphics;\n";
		logger.error( errorMessage);
	}

	/** Display error while running R. */
	protected void showErrorRunning() {
		// Send error message
		String errorMessage = "An error occurred while running Random Forest. Execution halted...\n" ;
		logger.error(errorMessage);
	}

}
