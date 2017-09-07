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

/**
 * Class contains a reprentation of a Gene as it is 
 * described in a GenBank file.
 *
 * @author Eudes Barbosa
 */
public class Gene {
	
	//------  Variable declaration  ------//

	/** Gene identifier. */
	protected String id = "";
	
	/** Gene name. */
	protected String name = "";
	
	/** Gene product description. */
	protected String product = "";
	
	/** Gene locus. */
	protected String locus = "";
	
	/** Gene orientation. */
	protected char orientation;
	
	/** Gene length (base pairs). */
	protected int length = 0;
	
	/** Organism accession number. */
	protected String organismID = "";
	
	/** Cluster identifier associated with gene. */
	protected int cluster = -1;
	
	/** Gene starting position. */
	protected int startPos = -1;
	
	/** Pseudogene.*/
	protected boolean pseudo = false;
	
	//------  Declaration end  ------//



	/**
	 * @return 	Returns pseudogene status. True if 
	 * 			pseudo. False, otherwise.
	 */
	public boolean isPseudo() {
		return pseudo;
	}

	/**
	 * Sets if a gene is a pseudogene or 
	 * not.
	 * @param pseudo	Pseudogene status. True if 
	 * 					pseudo. False, otherwise.
	 */
	public void setPseudo(boolean pseudo) {
		this.pseudo = pseudo;
	}

	/**
	 * @return	Returns gene identifier (GI). 
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the gene identifier.
	 * @param id	Gene identifier.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return Returns gene name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets gene name.
	 * @param name	Gene name.
	 */
	public void setName(String name) {
		// If the gene name is longer than 6
		// it is already wrong...
		if(name.length() < 10)
			this.name = name;		
	}

	/**
	 * @return Returns gene product description.
	 */
	public String getProduct() {
		return product;
	}

	/**
	 * Sets gene product.
	 * @param product	Gene product.
	 */
	public void setProduct(String product) {
		this.product = product;
	}

	/**
	 * @return	Returns gene locus.
	 */
	public String getLocus() {
		return locus;
	}

	/**
	 * Sets gene locus tag.
	 * @param locus	Gene locus
	 */
	public void setLocus(String locus) {
		this.locus = locus;
	}

	/**
	 * @return	Returns gene orientation.
	 */
	public char getOrientation() {
		return orientation;
	}

	/**
	 * Sets gene orientation. 
	 * @param orientation	Gene orientation. It must be either '+' 
	 * 						or '-'.
	 */
	public void setOrientation(char orientation) {
		if (orientation == '+' || orientation == '-'){
			this.orientation = orientation;
		}
	}

	/**
	 * @return	Returns gene length in base pairs.
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Sets the gene length in base pairs.
	 * @param length	Gene length (base pairs).
	 */
	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * @return	Returns organism accession number.
	 */
	public String getOrganismID() {
		return organismID;
	}

	/**
	 * Sets organism identifier.
	 * @param organismID	Organism identifier.
	 */
	public void setOrganismID(String organismID) {
		this.organismID = organismID;
	}

	/**
	 * @return	Returns cluster identifier associated 
	 * 			with gene.
	 */
	public int getCluster() {
		return cluster;
	}

	/**
	 * Sets gene cluster identifier with 
	 * Transitivity Clustering result.
	 * @param cluster	Gene cluster.
	 */
	public void setCluster(int cluster) {
		this.cluster = cluster;
	}	

	/**
	 * (Probably used only to sort the genes)
	 * @return	Returns gene starting position.
	 */
	public int getStartPos() {
		return startPos;
	}

	/**
	 * Sets gene starting position.
	 * @param startPos	Gene starting position.
	 */
	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}
}
