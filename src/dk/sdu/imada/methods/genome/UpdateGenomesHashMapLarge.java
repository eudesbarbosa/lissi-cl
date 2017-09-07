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


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import dk.sdu.imada.methods.BrokePipelineException;


/**
 * Class updates the information available for 
 * all the genomes. It copies the previous information 
 * and stores the cluster id for each gene. USed for 
 * analysis greater of equal to 300,000 genes. Data is 
 * stored in a hashmap with concurrent access.
 * 
 * @author Eudes Barbosa (eudes@imada.sdu.dk) 
 */
public class UpdateGenomesHashMapLarge extends UpdateGenomesHashMap {


	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(UpdateGenomesHashMapLarge.class.getName());

	/** ConcurrentHashMap with Gene Identifier as Key and cluster ID as Entry. */
	protected ConcurrentHashMap<Integer, Integer> clusterHash =
			new ConcurrentHashMap<Integer, Integer>();

	/** 
	 * Used to avoid sending multiple error messages while 
	 * using multi-thread.
	 */
	protected boolean errorMsgSent = false;

	/** Number of threads to use. */
	protected int threads = 1;

	//------  Declaration end  ------//



	/**
	 * Updates the information available for 
	 * all the genomes. It copies the previous information 
	 * and stores the cluster id for each gene. Preferable 
	 * used for analysis under 500,000 genes.
	 * 
	 * @param threads Number of threads to be use. 
	 * 				  Default: 1.
	 */
	public UpdateGenomesHashMapLarge(int threads) {
		super();
		if (threads > 1) {
			this.threads = threads;
		}
	}


	@Override
	public List<Genome> updateGenomes(List<Genome> genomes, ArrayList<String> clusters) {
		logger.info("Loading clusters information.");

		// Create HashMap for cluster
		clusterHashMap(clusters);
		// Update genome values
		for (Genome genome : genomes) {
			// Get gene information
			ArrayList<Gene> genes = parseGenes(genome.getGenes());
			// Add gene list to genome
			genome.setGenes(genes);
		}
		// Remove hashmap
		clusterHash = null;

		//
		return genomes;
	}


	@Override
	protected ArrayList<Gene> parseGenes(ArrayList<Gene> genes) {
		for(Gene g : genes) {
			try {
				int id = Integer.parseInt(g.getId());
				int cluster = clusterHash.get(id);
				g.setCluster(cluster);	
			} catch (Exception e) {
				if (errorMsgSent == false) {					
					if (g.getId().equals(null)) {
						String message = "Gene has no identifier (!!!).";
						sentError(message);
					} else {
						String message = "Gene Identifier <" + g.getId() + "> from " + g.getOrganismID() 
						+ " wasn't found in the TransClust results.";
						sentError(message);
					}
					errorMsgSent = true;
				}
			}
		}			
		//
		return genes;
	}			

	/**
	 * @param genes		List of Genes.
	 * @return	Returns a Callable for the given Gene Cluster list.
	 */
	protected Callable<String> createCallable(final List<String> list) {		
		Callable<String> callable = new Callable<String>() {
			@Override
			public String call() throws Exception {			
				for(String c : list) {
					String[] entry = c.split("\t");
					int gi = Integer.parseInt(entry[0]);
					int cluster = Integer.parseInt(entry[1]);
					clusterHash.put(gi, cluster);
				}				
				//
				return "Done";
			}
		};
		//
		return callable;
	}

	/**
	 * Creates a hash map using the cluster information (TransClust).
	 * @param clusters		Parsed lines of TransClust output.
	 */
	private void clusterHashMap(ArrayList<String> clusters) {
		long startTime = System.currentTimeMillis();			
		// Executor configuration	
		ExecutorService  executor = Executors.newFixedThreadPool(this.threads);
		ArrayList<Callable<String>> tasks = new ArrayList<Callable<String>>();
		// Create callables
		int perThread = (int) Math.ceil(clusters.size()/this.threads+1);
		List<List<String>> listPerThread = chopped(clusters, perThread);
		for (List<String> list : listPerThread) {
			// Create Callable
			Callable<String> callable = createCallable(list);
			tasks.add(callable);
		}
		List<Future<String>> set;
		try {
			set = executor.invokeAll(tasks);
			for (Future<String> f : set) {
				f.get().toString();
			}
		} catch (ExecutionException | InterruptedException e) {
			String message = "Fail to create cluster HashMap.";
			logger.error(message);
			try {
				throw new BrokePipelineException(message, e);
			} catch (BrokePipelineException e1) {
				return;
			}
		}
		executor.shutdown();    
		logger.debug("Creating hashmap took " + (System.currentTimeMillis() - startTime) + " ms.");
	}

	/** This is a crazy inner-class. */
	protected class GeneIdentifier {

		// Variable declaration
		private Integer id;
		private byte[] a = new byte[2];
		private byte[] b = new byte[3];
		// Declaration end

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		@Override
		public int hashCode() {		
			//final int PRIME = 1097;
			//return new HashCodeBuilder(getId()%2==0?getId()+1:getId(), PRIME).toHashCode();    
			return a[0] + powerOf52(a[1], 1) + powerOf52(b[0], 2) + powerOf52(b[1], 3) + powerOf52(b[2], 4);
		}

		private  int powerOf52(byte b, int power) {
			int result = b;
			for (int i = 0; i < power; i++) {
				result *= 52;
			}
			return result;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null)
				return false;

			if (o == this)
				return true;

			if (o.getClass() != getClass())
				return false;

			GeneIdentifier e = (GeneIdentifier) o;

			return new EqualsBuilder().
					append(getId(), e.getId()).
					isEquals();
		}
	}
}