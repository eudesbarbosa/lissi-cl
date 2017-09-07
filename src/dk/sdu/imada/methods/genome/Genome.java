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

/**
 * Class defines a Genome object. It stores 
 * relevant information about the genome, such 
 * as: Accesion Number, definition, organism 
 * name, among others. 
 * 
 * @author Eudes Barbosa
 */
public class Genome {

	//------  Variable declaration  ------//

	/** Genome accession number. */
	protected String accession = "";
	
	/** Organism name (including strain). */
	protected String name = "";
	
	/** Organismn taxonomic identifier. */
	protected String taxonID = "";
	
	/** Path to Genbank file. */
	protected String file = "";
	
	/** Organism lifestyle. */
	protected String lifestyle = "";
	
	/** 
	 * Definition of the genome, for example: 
	 * 'complete', 'choromosome one', etc.
	 */
	protected String definition = "";	
	
	/** Chromosome number as found in Gecko's output. */
	protected String geckoChromosomeNr = null;
	
	/** List of all genes present in the Genome/Chromosome. */
	protected ArrayList<Gene> genes = new ArrayList<Gene>();
	
	//------  Declaration end  ------//


	/**
	 * @return 		Gecko's reference number for 
	 * 				this genome (see Gecko's 
	 * 				output)
	 */
	public String getGeckoChromosomeNr() {
		return geckoChromosomeNr;
	}

	/**
	 * Sets the reference number for this genome as 
	 * found in Gecko output file.
	 * 
	 * @param geckoChromosomeNr		Gecko's reference number.
	 */
	public void setGeckoChromosomeNr(String geckoChromosomeNr) {
		this.geckoChromosomeNr = geckoChromosomeNr;
	}	

	/**
	 * Gets the organism's definition, i.e., if 
	 * it is the complete genome or if it has more 
	 * than one chromosome.
	 * @return	Organism definition.
	 */
	public String getDefinition() {
		return definition;
	}

	/**
	 * Sets the organism's definition, i.e., if 
	 * it is the complete genome or if it has more 
	 * than one chromosome.
	 * @param definition	Organism definition.
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	/**
	 * Gets the lifestyle associated with a 
	 * genome.
	 * @return	Organism lifestyle.
	 */
	public String getLifestyle() {
		return lifestyle;
	}

	/**
	 * Sets the lifestyle of a organism.
	 * @param lifestyle	Organism lifestyle.
	 */
	public void setLifestyle(String lifestyle) {
		this.lifestyle = lifestyle;
	}	

	/**
	 * Gets genome ID.
	 * @return 	The genome ID.
	 */
	public String getAccession() {
		return accession;
	}

	/**
	 * Sets genome ID.
	 * @param id 	The genome ID.
	 */
	public void setAccession(String id) {
		this.accession = id;
	}

	/**
	 * Gets genome name.
	 * @return 	The organism name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets genome name.
	 * @param name	The organism name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets organism taxonomic ID.
	 * @return	The organism taxonomic ID.
	 */
	public String getTaxonID() {
		return taxonID;
	}

	/**
	 * Sets organism taxonomic ID.
	 * @param taxonID	The organism taxonomic ID.
	 */
	public void setTaxonID(String taxonID) {
		this.taxonID = taxonID;
	}
	
	/**
	 * Gets path to local copy of the genome, 
	 * the .gbk file. 
	 * @return	String containing path to genome.
	 */
	public String getFile() {
		return file;
	}

	/**
	 * Sets path to local copy of the genome, 
	 * the .gbk file. 
	 * @param file	String containing path to genome.
	 */
	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * Gets a list with the genomic content 
	 * of the organism (all genes).
	 * @return List of all genes.
	 */
	public ArrayList<Gene> getGenes() {
		return genes;
	}

	/**
	 * Sets the list with the genomic content 
	 * of the organism (all genes).
	 * @param genes	List of all genes.
	 */
	public void setGenes(ArrayList<Gene> genes) {
		this.genes = genes;
	}	

}
