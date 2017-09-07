package dk.sdu.imada.methods.gecko;

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import dk.sdu.imada.methods.genome.Gene;
import dk.sdu.imada.methods.genome.Genome;


/** 
 * Class defines an Island object as it is found 
 * in a bacterial genome. It stores a version of 
 * the Genome object (with the set of all genes), 
 * and a sorted map based on gene start position (the 
 * value is the Gene object itself).
 * 
 * @author Eudes Barbosa
 */
public class Island {

	
	//------  Variable declaration  ------//

	/** 
	 * Genome object where the islands 
	 * was found. Remember not to include 
	 * the list of all genes present, they 
	 * most likely won't be necessary.
	 */
	protected Genome genome = null;

	/** 
	 * A SortedMap representing the order of the genes 
	 * in the island. The key is an integer (gene start  
	 * position) and the value is a Gene object. 
	 */
	protected SortedMap<Integer, Gene> sortedGenes = new TreeMap<Integer, Gene>();
	
	//------  Declaration end  ------//



	/**
	 * @return	Returns a Genome object 
	 * 			where the island was 
	 * 			found.
	 */
	public Genome getGenome() {
		return genome;
	}

	/**
	 * Sets the genome where the island was 
	 * found.
	 * @param genome	Genome object.
	 */
	public void setGenome(Genome genome) {
		this.genome = Objects.requireNonNull(genome, "Genome object cannot be null.");
	}

	/**
	 * @return	A SortedMap representing the order of 
	 * 			the genes in the island.
	 */
	public SortedMap<Integer, Gene> getSortedGenes() {
		return sortedGenes;
	}

	/**
	 * Sets the representation of the gene island order.
	 * 
	 * @param sortedGenes	A SortedMap representing the gene order 
	 * 						in the island.
	 */
	public void setSortedGenes(SortedMap<Integer, Gene> sortedGenes) {
		this.sortedGenes = sortedGenes;
	}

}