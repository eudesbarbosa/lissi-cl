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


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Class contains methods to validate and 
 * parse the input provided by the user.
 * 
 * @author Eudes Barbosa
 */
public class ValidateInput {

	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(ValidateInput.class.getName());

	/** Path to configuration file. Set when application starts. */
	protected String configPath;

	/** 
	 * Object with all the parameters 
	 * required in the analysis.
	 */
	protected ParametersObject allParameters;

	/** Help message. */
	protected final String help = "SUMMARY\n"+
			"LiSSI stands for LifeStyle-Specific-Islands. It was developed " + 
			"to identify islands mainly associated with a given life-style. " + 
			"LiSSI is divided into three sequential modules: \n" + 
			"- Evolutionary Sequence Analysis; \n" + 
			"- Island Detection; \n" + 
			"- Statistical Learning Methods. \n" + 
			"Optionally, the tool can be used without the islands detection. " + 
			"In that case, it will report putative homologous genes that are " + 
			"mainly associated with a given lifestyle.\n"+
			"\nABOUT\n"+
			"\tLiSSI command line version 1.0\n"+
			"\tCopyright 2017 by Eudes Barbosa\n"+
			"\nCITATION\n" +
			"Barbosa, Eudes, et al. \"LifeStyle-Specific-Islands (LiSSI): "
			+ "Integrated Bioinformatics Platform for Genomic Island Analysis.\" "
			+ "Journal of Integrative Bioinformatics (2017).\n" +
			"\nUSAGE\n"+
			"\tjava -jar [java virtual machine options] LiSSI.jar " +
			"-c <parameter_file>";

	//------  Declaration end  ------//



	/**
	 * Class validates the command line arguments. If 
	 * all provided information is corrected it generates 
	 * an object with all the required parameters.
	 * 
	 * @param args		The command line arguments.
	 */
	public ValidateInput(String[] args) {

		// Check input arguments 
		configPath = checkInputArgs(args);

		// Create parameter object
		this.allParameters = new ParametersObject();

		// Validate configuration settings
		validateConfigFile(configPath);
	}

	/** 
	 * @return Returns an object with all the parameters 
	 * required in the analysis.
	 */
	public ParametersObject getParametersObject() {
		return this.allParameters;
	}

	/**
	 * Validates the command line arguments.
	 * 
	 * @param args		The command line arguments.
	 * @return 			Returns path to configuration 
	 * 					file. Default: empty.
	 */
	protected String checkInputArgs(String[] args) {
		// Configure options
		CommandLine commandLine;
		// Help
		Option option_Help = Option.builder("h")
				.longOpt( "help" )
				.desc( "print this message."  )
				//.hasArg()
				.argName( "HELP" )
				.build();
		// Configuration file
		Option option_Config = Option.builder("c")
				.longOpt( "config" )
				.desc( "configuration file."  )
				.hasArg()
				.argName( "CONFIG" )
				.build();
		//
		Options options = new Options();
		options.addOption(option_Help);
		options.addOption(option_Config);
		//
		CommandLineParser parser = new DefaultParser();
		try {
			// Parse command line arguments
			commandLine = parser.parse(options, args);
			// Print help
			if(commandLine.hasOption("h")){
				System.out.println(help);	
				/*
				for (Iterator<?> it=options.getOptions().iterator(); it.hasNext(); ) {
					String line = it.next().toString().replace(" option: ", "-");
					line = line.replaceAll("\\[", "").replaceAll("\\]", "").replace("ARG", "");
					line = line.replace(":", "").replaceAll("\\s+", " ").trim();
					System.out.println("   "+line);
				}
				 */
				System.exit(0);
			}
			// If no configuration file
			if(!commandLine.hasOption("c")) {
				System.out.println("Missing configuration file.");
				System.out.println("Try the option '-h' for more information.");				
				System.exit(0);
			}
			try {
				// Verify if provided string is a path to file
				FilenameUtils.getPath(commandLine.getOptionValue("c").toString());
				//
				return commandLine.getOptionValue("c").toString();
			} catch (Exception e) {
				System.out.println(commandLine.getOptionValue("c").toString());
				System.out.println("Error while reading configuration file. \nExiting...");
				//e.printStackTrace();
				System.exit(0);			
			}
		} catch (ParseException exception) {
			//logger.error("Parse error: ");
			logger.error(exception.getMessage());
			System.exit(1);
		}
		//
		return "";
	}

	/**
	 * Validates the configuration file provided by the user. It checks: 
	 * if it has a valid email address (necessary as a password for NCBI 
	 * FTP access); if the provided local directory exists and can be used 
	 * (writable); and, if the BLAST binary files are stored in place.
	 *  
	 * @param path		Path to the user's configuration
	 * 					file. 
	 */
	protected void validateConfigFile(String path) {
		// Initialize error msg
		String errorMsg, test;
		errorMsg = test = "Please attend for the following errors in your configuration file: \n";

		// Get nodes information
		NodeList nList = getNodeList();

		// Iterate through nodes (get info)
		for (int i = 0; i < nList.getLength(); i++) {
			// Genome folder settings
			Node node = nList.item(i);
			Element eElement = (Element) node;

			// Local folder settings
			if (eElement.getAttribute("id").equals("LocalFolder")) {
				// Validate provided local folder
				// If problem with local folder, add to error message
				String localDir = eElement.getElementsByTagName("folder").item(0).getTextContent();
				File f = new File(localDir);
				boolean check = f.exists() && f.isDirectory() && f.canWrite();
				if (!check) {
					errorMsg += "- Problem with provided local folder. \n";
				} else {
					this.allParameters.setLocalDir(localDir);
				}
			}

			/*
			// Local folder settings
			if (eElement.getAttribute("id").equals("TempFolder")) {
				// Validate provided local folder
				File f = new File(eElement.getElementsByTagName("folder").item(0).getTextContent());
				try {
					if (!f.exists()) {
						f.mkdirs();
						f.deleteOnExit();
					}
				} catch (Exception e) {
					// If problem with temporary folder, add to error message
					errorMsg += "- Problem with provided temporary folder \n";
				}		
			}*/
			// Processors settings
			if (eElement.getAttribute("id").equals("CPU")) {
				// Validate
				try {
					int processors = Integer.parseInt(eElement.getElementsByTagName("cpu").item(0).getTextContent());
					this.allParameters.setProcessors(processors);
				} catch (Exception e) {
					// If problem with processors, give warning
					logger.warn("Fail to parse number of processors. Using single processor instead.");
				}		
			}
			// Blast bin folder settings
			if (eElement.getAttribute("id").equals("Blast")) {
				// Validate path to BLAST bin folder
				// If problem with blast folder, add to error message
				String blastDir = eElement.getElementsByTagName("folder").item(0).getTextContent();
				boolean check = new File(blastDir, "blastp").exists();
				if (!check){
					errorMsg += "- Problem with BLAST bin folder \n";
				} else {
					this.allParameters.setBlastDir(blastDir);
				}
			}

			// R bin folder settings
			if (eElement.getAttribute("id").equals("R")) {
				// Validate path to R bin folder
				// If problem with blast folder, add to error message
				String rDir = eElement.getElementsByTagName("folder").item(0).getTextContent();
				boolean check = new File(rDir, "Rscript").exists();
				if (!check){
					errorMsg += "- Problem with R bin folder \n";
				} else {
					this.allParameters.setRDir(rDir);
				}
			}

			// Genome folder settings
			if (eElement.getAttribute("id").equals("Genomes")) {					
				// Initialize variables
				String folderLifeOne = null;
				String nameLifeOne = null;
				String folderLifeTwo = null;
				String nameLifeTwo = null;
				// Get child nodes information
				NodeList children = eElement.getChildNodes();
				Node current = null;
				int count = children.getLength();
				for (int j = 0; j < count; j++) {
					current = children.item(j);
					if (current.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) current;
						// Extract information lifestyle one
						if (element.getNodeName().equalsIgnoreCase("LifeStyleOne")) {
							folderLifeOne = element.getAttribute("folder");
							//
							nameLifeOne = element.getAttribute("name");
						}
						// Extract information lifestyle two
						if (element.getNodeName().equalsIgnoreCase("LifeStyleTwo")) {
							folderLifeTwo = element.getAttribute("folder");
							//
							nameLifeTwo = element.getAttribute("name");
						}
					}					
				}
				logger.info("Genome parameters :" 
						+"\n\t- Lifestyle One directory : " + folderLifeOne
						+"\n\t- Lifestyle name : " + nameLifeOne
						+"\n\t- Lifestyle Two directory : " + folderLifeTwo
						+"\n\t- Lifestyle name : " + nameLifeTwo
						);
				//
				try {
					this.allParameters.setGenomeObject(folderLifeOne, nameLifeOne, folderLifeTwo, nameLifeTwo);
				} catch (BrokePipelineException e) {
					e.printStackTrace();
				}
				//
				continue;
			}
			// Homology detection settings
			if (eElement.getAttribute("id").equals("Homology")) {
				// Initialize variables
				String evalue = null;
				String blastFile = null;
				String density = null;
				String transclustFile = null;

				// Get child nodes information
				NodeList children = eElement.getChildNodes();
				Node current = null;
				int count = children.getLength();
				for (int j = 0; j < count; j++) {
					current = children.item(j);
					if (current.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) current;
						// Extract information Blast
						if (element.getNodeName().equalsIgnoreCase("Blast")) {
							evalue = element.getAttribute("evalue");
							//
							blastFile = element.getAttribute("file");
						}
						// Extract information TransClust
						if (element.getNodeName().equalsIgnoreCase("TransClust")) {
							density = element.getAttribute("density");
							//
							transclustFile = element.getAttribute("file");
						}
					}
				}
				logger.info("Homology parameters :" 
						+"\n\t- Blast Evalue : " + evalue
						+"\n\t- Provided Blast file : " + blastFile
						+"\n\t- TransClust density parameter : " + density
						+"\n\t- Provided TransClust file : " + transclustFile
						);
				// Create TransClust object
				this.allParameters.setTransClustObject(evalue, blastFile, density, transclustFile);
			}

			// Island detection settings
			if (eElement.getAttribute("id").equals("Island")) {
				// Initialize variables
				String minOrganisms = null;
				String minSize = null;
				String indels = null;
				String file = null;
				// Get child nodes information
				NodeList children = eElement.getChildNodes();
				Node current = null;
				int count = children.getLength();
				for (int j = 0; j < count; j++) {
					current = children.item(j);
					if (current.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) current;
						if (element.getNodeName().equalsIgnoreCase("Gecko")) {
							minOrganisms = element.getAttribute("minOrganisms");
							minSize = element.getAttribute("minSize");
							indels = element.getAttribute("indels");
							file = element.getAttribute("file");
							//
							logger.info("Gecko parameters :" 
									+"\n\t- Minimum number of organisms : " + minOrganisms
									+"\n\t- Minimum island size : " + minSize
									+"\n\t- Maximum accepted indels : " + indels
									+"\n\t- Provided file : " + file
									);
						}
					}
				}
				// Create Gecko object
				try {
					this.allParameters.setGeckoObject(minOrganisms, minSize, indels, file);
				} catch (BrokePipelineException e) {
					logger.warn("Running analysis without Island Detection!!!");
				}
			}
			// Island detection settings
			if (eElement.getAttribute("id").equals("Statistics")) {
				// Initalize variables
				String kfold = null;
				String runs = null;
				String trees = null;
				// Get child nodes information
				NodeList children = eElement.getChildNodes();
				Node current = null;
				int count = children.getLength();
				for (int j = 0; j < count; j++) {
					current = children.item(j);
					if (current.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) current;
						if (element.getNodeName().equalsIgnoreCase("RandomForest")) {
							kfold= element.getAttribute("k");
							runs = element.getAttribute("runs");
							trees = element.getAttribute("trees");
							//
							logger.info("Random Forest parameters :" 
									+"\n\t- K-fold : " + kfold
									+"\n\t- Number of runs : " + runs
									+"\n\t- Number of trees per run : " + trees
									);
						}
					}
				}
				// Create RandomForest object
				this.allParameters.setRandomForestObject(kfold, runs, trees);
			}
		}
		//
		if (!test.equals(errorMsg)) {
			logger.error("Error while reading configuration file.\n" + errorMsg + "\nExiting...\n");
			System.exit(1);
		} else {

		}
	}

	/**
	 * Reads the XML configuration file and search 
	 * for the provided number of processors. It doesn't 
	 * re-validate the path since it assumes that there 
	 * were no changes after the program started.
	 * 
	 * @return			Returns number of processors set 
	 * 					by the user. If none was provided, 
	 * 					it returns '1'.
	 */
	protected int getProcessors() {
		// Get nodes information
		NodeList nList = getNodeList();
		// Iterate through nodes (get info)
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			Element eElement = (Element) node;
			if (eElement.getAttribute("id").equals("Cores")) {
				// Return local folder
				String value = eElement.getElementsByTagName("processors").item(0).getTextContent(); 
				return Integer.parseInt(value);
			}
		}
		//
		return 1;
	}

	/**
	 * Reads the XML configuration file and search 
	 * for the provided R bin directory. It doesn't re-
	 * validate the path since it assumes that there were 
	 * no changes after the program started.
	 * 
	 * @return			Return string containing the path to 
	 * 					R binary directory.
	 */
	protected String getRBin() {
		// Get nodes information
		NodeList nList = getNodeList();
		// Iterate through nodes (get info)
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			Element eElement = (Element) node;
			if (eElement.getAttribute("id").equals("R")) {
				// Return Blast bin folder
				return eElement.getElementsByTagName("folder").item(0).getTextContent();
			}
		}
		//
		return null;
	}

	/**
	 * Reads the XML configuration file and search 
	 * for the provided BLAST bin directory. It doesn't re-
	 * validate the path since it assumes that there were 
	 * no changes after the program started.
	 * 
	 * @return			String containing the path to 
	 * 					BLAST bin directory.
	 */
	protected String getBlastBin() {
		// Get nodes information
		NodeList nList = getNodeList();
		// Iterate through nodes (get info)
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			Element eElement = (Element) node;
			if (eElement.getAttribute("id").equals("Blast")) {
				// Return Blast bin folder
				return eElement.getElementsByTagName("folder").item(0).getTextContent();
			}
		}
		//
		return null;
	}

	/**
	 * Reads the XML configuration file and search 
	 * for the provided local directory. It doesn't re-
	 * validate the path since it assumes that there were 
	 * no changes after the program started.
	 * 
	 * @return			String containing the path the user's 
	 * 					selected temporary directory.
	 */
	protected String getLocalDirectory() {
		// Get nodes information
		NodeList nList = getNodeList();
		// Iterate through nodes (get info)
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			Element eElement = (Element) node;
			if (eElement.getAttribute("id").equals("LocalFolder")) {
				// Return local folder
				return eElement.getElementsByTagName("folder").item(0).getTextContent();
			}
		}
		//
		return null;
	}

	/**
	 * Reads the XML configuration file and search 
	 * for the provided local directory. It doesn't re-
	 * validate the path since it assumes that there were 
	 * no changes after the program started.
	 * 
	 * @return			String containing the path the user's 
	 * 					selected local directory.
	 */
	protected String getTemporaryDirectory() {
		// Get nodes information
		NodeList nList = getNodeList();
		// Iterate through nodes (get info)
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			Element eElement = (Element) node;
			if (eElement.getAttribute("id").equals("TempFolder")) {
				// Return local folder
				return eElement.getElementsByTagName("folder").item(0).getTextContent();
			}
		}
		//
		return null;
	}

	/**
	 * Reads the configuration file and gets the node information,
	 * i.e., the user's settings. 
	 * @return	List of nodes found in the configuration file.
	 */
	protected NodeList getNodeList() {
		//
		NodeList nList = null;
		// Read XML file
		try {			
			File xmlFile = new File(configPath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			// Remove spaces and line breaks from fields
			doc.getDocumentElement().normalize();
			// Get nodes information
			nList = doc.getElementsByTagName("set");
		} catch (Exception e) {
			logger.error("Failed to read configuration file (xml). ");
			System.out.println("Failed to read configuration file (xml). ");
			System.out.println("Try the option '-h' for more information.");
		}
		//
		return nList;
	}


}