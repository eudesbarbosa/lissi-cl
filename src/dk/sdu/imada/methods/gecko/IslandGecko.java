/**
 * 
 */
package dk.sdu.imada.methods.gecko;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class defines an Island object as it is 
 * presented in the Gecko output. It stores 
 * all relevant information such as: identifier, 
 * p-values, organisms that present the island, 
 * among others.
 * 
 * @author Eudes Barbosa	
 */
public class IslandGecko {

	//------  Variable declaration  ------//

	/** 
	 * The identifier as provided in the
	 * Gecko output. Several islands can and 
	 * most likely will share the same identifier. 
	 */
	protected long id = -1;

	/** P-value associated with the island. */
	protected String pValue = null;	
	
	/** Reference Sequence (Genome). */
	protected String refGenome = null;

	/**
	 * The genomes of islands that are associated with a 
	 * single Gecko ID. ArrayList with genomes accession 
	 * numbers plus Gecko reference number, format: accession 
	 * number \<TAB\> refence number.
	 */ 
	protected HashMap<String, String> genomes = new HashMap<>();
	
	/**
	 * The genes of islands that are associated with a 
	 * single Gecko ID. ArrayList of gene identifiers (GI). 
	 * The GI's are not sorted or grouped in any particular order. 
	 * It is still necessary to parse them to have some 
	 * meaningful result.
	 */ 
	protected ArrayList<String> genesIdentifiers = new ArrayList<>();

	/**
	 * The set of islands that are associated with a 
	 * single Gecko ID. ArrayList of Island objects.
	 */ 
	protected ArrayList<Island> islands = new ArrayList<>();

	//------  Declaration end  ------//

	
	/**
	 * @return 	Returns Gecko reference 
	 * 			genome.
	 */
	public String getRefGenome() {
		return refGenome;
	}

	/**
	 * Sets which genome was used as reference 
	 * during Gecko analysis.
	 * 
	 * @param refGenome	Reference genome.
	 */
	public void setRefGenome(String refGenome) {
		this.refGenome = refGenome;
	}	
	
	/**
	 * @return	Returns the set of islands 
	 * 			that are associated with a 
	 * 			single Gecko ID.
	 */
	public ArrayList<Island> getIslands() {
		return islands;
	}

	/**
	 * Sets the group of islands associated 
	 * with a single Gecko ID.
	 * 
	 * @param islands 	Set of islands that are 
	 * 					associated with a single 
	 * 					Gecko ID.
	 */
	public void setIslands(ArrayList<Island> islands) {
		this.islands = islands;
	}	

	/**
	 * @return	Returns list of all gene identifiers 
	 * 			(GIs) present in the island.
	 */
	public ArrayList<String> getGenesIdentifier() {
		return genesIdentifiers;
	}	

	/**
	 * Sets list of all gene identifiers 
	 * (GIs) present in the island.
	 * 
	 * @param genes	List of gene identifiers (GIs).
	 */
	public void setGenesIdentifiers(ArrayList<String> genes) {
		this.genesIdentifiers = genes;
	}

	/**
	 * @return	Returns the island identifier 
	 * 			as provided by Gecko.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the island identifier (provided 
	 * by Gecko).
	 * 
	 * @param id	Island identifier.
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return 	Returns island p-value.
	 */
	public String getpValue() {
		return pValue;
	}

	/**
	 * Set island p-value.
	 * 
	 * @param pValue	Island p-value.
	 */
	public void setpValue(String pValue) {
		this.pValue = pValue;
	}

	/**
	 * @return	Returns list of genomes 
	 * 			(accession number and Gecko 
	 * 			reference number) that are
	 * 			present in the island.
	 */
	public HashMap<String, String> getGenomes() {
		return genomes;
	}

	/**
	 * Sets list of genomes (accession number 
	 * and Gecko reference number) that	are 
	 * present in the island.
	 * 
	 * @param genomes 	List of genes (accession numbers)
	 */
	public void setGenomes(HashMap<String, String> genomes) {
		this.genomes = genomes;
	}

	/**
	 * @return	Returns list of Gene objects that 
	 * 			are present in the island. Refer 
	 * 			to Gene class.

	public ArrayList<Gene> getGenes() {
		return genes;
	}
	 */

	/**
	 * Sets the list of Gene objects that 
	 * are present in the island. Refer 
	 * to Gene class.
	 * 
	 * @param	List of Gene objects.
	public void setGenes(ArrayList<Gene> genes) {
		this.genes = genes;
	}
	 */
}
