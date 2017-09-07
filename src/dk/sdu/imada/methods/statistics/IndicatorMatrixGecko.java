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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import dk.sdu.imada.methods.BrokePipelineException;
import dk.sdu.imada.methods.Command;
import dk.sdu.imada.methods.ExecutePipeline;
import dk.sdu.imada.methods.ExecutorServiceUnbounded;
import dk.sdu.imada.methods.gecko.Island;
import dk.sdu.imada.methods.gecko.IslandGecko;
import dk.sdu.imada.methods.genome.Gene;
import dk.sdu.imada.methods.genome.Genome;


/**
 * Class parses Gecko results. It can behave in two ways: 
 * i) Create an indicator matrix based on parsed Gecko 
 * results and store in the database (Derby [not anymore]); 
 * or ii) parse the results and return a list of Island objects. 
 * For the first case, a Command Pattern was implemented.
 * (database suppressed).
 * 
 * @author Eudes Barbosa
 *
 */
// Note: this is a really shitty parser. Please improve it 
// when you find the time...
public class IndicatorMatrixGecko extends AbstractIndicatorMatrix implements Command {

	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(IndicatorMatrixGecko.class.getName());

	/** Array containing all Gecko islands. */
	protected ArrayList<IslandGecko> geckoIslands = new ArrayList<>();

	/** Path to Gecko output file. */
	protected String geckoFile = "";
	
	/** Path to local working directory. */
	protected String localDir;

	/** Number of threads to use. */
	protected int threads = 1;
	
	//------  Declaration end  ------//	


	/**
	 * Creates an indicator matrix based on the parsed Gecko 
	 * results. Besides creating an indicator matrix, it will 
	 * store the data so it can be included in database.
	 * 
	 * @param geckoFile		Path to Gecko output file.
	 * @param localDir 		Path to local working directory.
	 * @param threads  		Number of threads.
	 */
	public IndicatorMatrixGecko(String geckoFile,
			String localDir, int threads) {
		this.geckoFile = Objects.requireNonNull(geckoFile);
		this.genomes = ExecutePipeline.getGenomes();
		this.localDir = localDir;
		this.threads  = threads;
	}


	/**
	 * @return		Parsed island information as 
	 * 				a list.
	 */
	public ArrayList<IslandGecko> getIslandList() {
		return geckoIslands;
	}

	@Override
	public void exec() {
		// Start parsing
		logger.info("Creating indicator matrix based on Gecko results.");
		createRFdir(this.localDir);
		parse();
		createMatrix();
	}

	/**
	 * Generates an indicator matrix based on 
	 * Gecko results (islands). 
	 */
	protected void createMatrix() {	
		//--------------------------------------------------------------//
		// Associate organisms with multiple genomes (accession numbers)
		organizeGenomes();

		//--------------------------------------------------------------//
		// Create file header			
		String row = "name;lifestyle";
		for (IslandGecko i : geckoIslands)		
			row = row + ";" + i.getId() ;		
		// Append the string to the file
		FileWriter fw;
		try {
			fw = new FileWriter(file, true);
			fw.write(row); 	
			fw.write("\n");
			//
			fw.close();
		} catch (IOException e) {
			logger.debug("Error while writing indicator matrix into file.");
			e.printStackTrace();
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
			// Verify if genomes have islands
			for (Genome g : genomes) {
				// Create each row
				String name = g.getName();
				String lifestyle = g.getLifestyle();
				row = name.replaceAll(" ", "_") + ";" + lifestyle;		

				// Iterate through islands
				for (IslandGecko i : geckoIslands) {
					// Verify if genome is present in result
					boolean contains = verifyPresence(i.getGenomes(), name);
					//boolean contains = i.getGenomes().containsKey(name);
					if (contains) {
						row = row + ";1";
					} else {
						row = row + ";0";
					}				
				}
				lines.add(row);
			}
			//
			appendOutput(file, lines);
		}
	}

	/**
	 * @param genomeHash	Genomes in island hashmap.
	 * @param name			Organism name.
	 * @return	Returns True if any of the chromosomes of an organism 
	 * 			contains the island; False otherwise. 
	 */
	protected boolean verifyPresence(HashMap<String, String> genomeHash,
			String name) {
		for (String accession : genomeMap.get(name)) {
			if (genomeHash.containsKey(accession)) {
				return true;
			}
		}		
		return false;
	}

	/**
	 * Reads each line of Gecko output 
	 * and extract the relevant information. 
	 */
	protected void parse() {
		// Initialize variables
		Pattern pattern = Pattern.compile("^\\d+:\t");
		IslandGecko islandInfo = new IslandGecko();
		HashMap<String, String> genomes = new HashMap<>();
		ArrayList<String> geneIdentifiers = new ArrayList<>();
		boolean genomeList = false;
		boolean geneList = false;
		long id;
		String pValue;
		String refSeq;

		// Read Gecko output file	
		String line;
		Scanner scan;
		try {
			scan = new Scanner(new File(geckoFile)).useDelimiter("\n");
			while(scan.hasNextLine()) {
				line = scan.nextLine();
				//System.out.println(line);

				if (line.contains("new cluster: ")) {
					// Parse
					String[] data = line.split(",");
					id = Integer.parseInt(data[0].replace("new cluster: ID = ", ""));
					pValue = data[1].replace(" pValue = ", "");
					refSeq = data[2].replace(" refSeq = ", "");
					logger.debug("@@@@ Island "+id + "\tp-value " + pValue);

					// Add to island
					islandInfo.setId(id);
					islandInfo.setpValue(pValue);
					islandInfo.setRefGenome(refSeq);
					continue;
				}

				// Identify which part of the file 
				// we are parsing.
				if (line.contains("in chromosomes:")) {
					genomeList = true;
					continue;
				}
				if (line.contains("Occurrences:")) {
					genomeList = false;
					geneList = true;
					continue;
				}				

				// Genome information
				Matcher matcher = pattern.matcher(line);
				if (genomeList && matcher.find()) {
					// Parse Gecko reference number
					String[] data = line.split(":");
					String ref = data[0].replaceAll("\\.\\d", "");

					// Parse Genome information
					data = line.split(" - ");
					String genome = data[0].replaceAll("\\d+:\t", "");
					logger.debug("#### " + genome + "\t" + ref);
					genomes.put(genome , ref);
					continue;
				}

				// Gene information
				if (geneList == true && !line.trim().equals("")) {
					//System.out.println("**** " + line);
					if (line.substring(line.length()-1).equals("]")) {
						String[] data = line.split("\\[");
						int size = data.length - 1;
						String g = data[size].replaceAll("\\]", "");
						//logger.info("@@@@ "+g);
						geneIdentifiers.add(g);
						continue;
					}
				}

				// End of parsing, add island info
				if (line.length() == 0) {
					islandInfo.setGenesIdentifiers(geneIdentifiers);
					islandInfo.setGenomes(genomes);
					geckoIslands.add(islandInfo);
					// Clean
					genomeList = false;
					geneList = false;
					islandInfo = new IslandGecko();
					genomes = new HashMap<String, String>();
					geneIdentifiers = new ArrayList<>();
					//
					continue;
				}				
			}
			// Close
			scan.close();

		} catch (FileNotFoundException e) {
			String message = "Error while reading Gecko output file.";
			logger.debug(message);
			try {
				throw new BrokePipelineException(message, e);
			} catch (BrokePipelineException e1) {
				return;
			}
		}
	}

	/**
	 * Updates the parsed results by adding the structure  
	 * of each individual island (Island object) associated 
	 * with a given Gecko ID (IslandGecko). It requires that 
	 * the method 'parse()' have being previously executed.
	 * 
	 * NOTE: Probably the most inefficient piece 
	 * of code I ever wrote.
	 */
	protected void setGeneOrder() {
		// Get all genomes
		List<Genome> genomes = ExecutePipeline.getGenomes();
		HashMap<String, Genome> genomesMap = new HashMap<>();
		for (Genome g : genomes){
			genomesMap.put(g.getAccession(), g);
		}
		// Gene map
		HashMap<String, Gene> geneHashMap = getGenesMap(genomes);

		// Iterate throw Gecko output
		ArrayList<IslandGecko> newGeckoIslands = new ArrayList<>();

		for (IslandGecko gIsland : this.geckoIslands) {
			// Store individual islands
			ArrayList<Island> islands = new ArrayList<>();
			logger.debug("Genome length : " + gIsland.getGenomes().size());
			logger.debug("Genes length : " + gIsland.getGenesIdentifier().size());

			Iterator<Entry<String, String>> it = gIsland.getGenomes().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> pair = (Map.Entry<String, String>)it.next();
				Genome g = genomesMap.get(pair.getKey());
				String accession = g.getAccession();
				//
				SortedMap<Integer, Gene> sortedGenes = new TreeMap<Integer, Gene>();				
				for (String id : gIsland.getGenesIdentifier()) {
					Gene gene = geneHashMap.get(id);
					if (gene.getOrganismID().equals(accession)) {
						sortedGenes.put(gene.getStartPos(), gene);
						continue;
					}
				}
				//
				if (sortedGenes.size() > 0) {
					// Create new island
					Island is = new Island();
					// Add sorted genes
					is.setSortedGenes(sortedGenes);
					// Get genome reference number
					String ref = gIsland.getGenomes().get(g.getAccession());
					g.setGeckoChromosomeNr(ref);
					is.setGenome(g);
					islands.add(is);					
				}					
			}
			if (islands.size() > 0) {
				gIsland.setIslands(islands);
				newGeckoIslands.add(gIsland);
			} else {
				logger.info("Why the f*#% not??! Real problem...");
			}
		}
		//logger.info("[Gecko Parser] Are the old Gecko islands the same as the new one ? " 
		//		+ newGeckoIslands.equals(this.geckoIslands));
		this.geckoIslands = newGeckoIslands;
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
					list.add(g.getAccession());
				}
			}
			//
			genomeMap.put(name, list);
		}
	}

	/**
	 * @param genomes	List of all genomes being analyzed.  
	 * @return 			Returns a HashMap associating each Gene object 
	 * 					(value) with its gene identifier (key).
	 */
	private HashMap<String, Gene> getGenesMap(List<Genome> genomes) {
		HashMap<String, Gene> geneHashMap = new HashMap<>();
		//
		for (Genome g: genomes)
			for (Gene gene : g.getGenes())
				geneHashMap.put(gene.getId(), gene);
		//
		return geneHashMap;
	}

	@Override
	protected void printLines(List<Genome> genomes) {
		HashSet<String> lines = new HashSet<>();
		// Verify if genomes have islands
		String row;
		for (Genome g : genomes) {
			// Create each row
			String name = g.getName();
			String lifestyle = g.getLifestyle();
			row = name.replaceAll(" ", "_") + ";" + lifestyle;		

			// Iterate through islands
			for (IslandGecko i : geckoIslands) {
				// Verify if genome is present in result
				boolean contains = verifyPresence(i.getGenomes(), name);
				//boolean contains = i.getGenomes().containsKey(name);
				if (contains) {
					row = row + ";1";
				} else {
					row = row + ";0";
				}				
			}
			lines.add(row);
		}
		//
		appendOutput(file, lines);
	}
}
