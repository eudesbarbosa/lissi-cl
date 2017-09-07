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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.AccessionID;
import org.biojava.nbio.core.sequence.ProteinSequence; 
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound; 
import org.biojava.nbio.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava.nbio.core.sequence.features.FeatureInterface;
import org.biojava.nbio.core.sequence.features.Qualifier;
import org.biojava.nbio.core.sequence.io.FastaWriter;
import org.biojava.nbio.core.sequence.io.GenbankReaderHelper;
import org.biojava.nbio.core.sequence.io.GenericFastaHeaderFormat;
import org.biojava.nbio.core.sequence.io.template.FastaHeaderFormatInterface;
import org.biojava.nbio.core.sequence.loader.GenbankProxySequenceReader;
import org.biojava.nbio.core.sequence.template.AbstractSequence;
import org.biojava.nbio.core.sequence.template.Sequence;

import dk.sdu.imada.methods.BrokePipelineException;
import dk.sdu.imada.methods.Parser;

/**
 * Abstract class contains methods to parse 
 * a GenBank file. 
 * 
 * @author Eudes Barbosa 
 */
public abstract class AbstractGenomeParser extends Parser {

	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(AbstractGenomeParser.class.getName());

	/** Array list with genome objects. */
	protected List<Genome> genomes = new ArrayList<Genome>();

	/** Create pattern associated with 'hypothetical proteins'. */
	static Pattern patternHypothetical = Pattern.compile("hypothetical", Pattern.CASE_INSENSITIVE);

	//------  Declaration end  ------//


	/** @return Returns list of genomes. */
	protected List<Genome> getGenomeList() {
		return this.genomes;
	}


	/**
	 * @param location		Genome accession identifier.
	 * @param lifestyle		Organism lifestyle.
	 * 
	 * @return Parses the GenBank file and returns a list 
	 * of proteins. 
	 * @throws Exception 
	 */
	protected ArrayList<ProteinSequence> parse(File file, String lifestyle) throws Exception {
		// Initialize variables
		ArrayList<ProteinSequence> fasta = new ArrayList<ProteinSequence>();
		ArrayList<Gene> genes = new ArrayList<Gene>();
		Genome genome = new Genome();		

		// Read GenBank file
		logger.debug("@@@ " + file +  "\t Can read? " + file.canRead());
		//
		LinkedHashMap<String, ProteinSequence> protSequences =
				GenbankReaderHelper.readGenbankProteinSequence(file);

		// Iterate through features
		for (ProteinSequence sequence : protSequences.values()) {
			Iterator<FeatureInterface<AbstractSequence<AminoAcidCompound>, AminoAcidCompound>> iterator = 
					sequence.getFeatures().iterator();

			// Set genome info
			genome.setFile(file.getAbsolutePath());
			genome.setLifestyle(lifestyle);
			genome.setAccession(sequence.getAccession().getID());
			genome.setTaxonID(sequence.getTaxonomy().getID());
			//
			String description = sequence.getDescription();
			String[] arrDescrip = description.split(", ");
			String organism = arrDescrip[0].replaceAll("chromosome", "")
					.replaceAll("\\s+$", "");
			String des = arrDescrip[1];
			// 
			genome.setDefinition(des);
			genome.setName(organism);

			// Iterate through qualifiers
			while (iterator.hasNext()) {

				// Get qualifiers from features
				Map<String, List<Qualifier>> map = iterator.next().getQualifiers();

				// Filter for CDS
				if (map.containsKey("translation")) {

					//---------------------------------------//
					//          Initialize variables         //
					//---------------------------------------//
					String gi = "";
					String locus = "";
					String product = "";
					String geneName = "";
					String translation = "";
					//
					int startPos = 0;
					//int endPos = 0;
					char orientation;
					//
					Gene gene = new Gene();


					//-------------------------------//
					//          Parse values         //
					//-------------------------------//

					// Orientation
					String pos = iterator.next().getSource();
					if (pos.contains("complement")) {
						orientation = '-';
						pos = pos.replaceAll("[^\\d.]", "");
					} else {
						orientation = '+';
					}
					String[] arrPos = pos.split("\\.\\.");
					String posTemp = arrPos[0].replaceAll(">", "")
							.replaceAll("<", "");
					startPos = Integer.parseInt(posTemp);
					//endPos   = Integer.parseInt(arrPos[1]);

					for(Entry<String, List<Qualifier>> entry : map.entrySet()) {
						//String key = entry.getKey();
						for (Qualifier value : entry.getValue()) {

							// GI
							if (value.toString().contains("GI:")) {
								gi = value.toString().replaceAll("\\W", "").replaceAll("[^\\d]", "");
								gene.setId(gi);
							} else if (value.getName().equals("translation")) {
								translation = value.getValue();
							} else if (value.toString().contains("product")) {
								product = value.getValue();
							} else if (value.toString().contains("locus_tag")) {
								locus = value.getValue();
							} else if (value.toString().contains("gene")) {
								geneName = value.getValue();
							}

						}
					}
					//-----------------------------//
					//          Add values         //
					//-----------------------------//

					// Amino acids
					ProteinSequence prot = new ProteinSequence(translation);
					prot.setAccession(new AccessionID(gi));
					//prot.setOriginalHeader(gi);
					logger.debug("AA sequence : " + prot.getSequenceAsString().substring(0,10)
							+ "...");
					fasta.add(prot);

					// Gene name
					gene.setName(geneName);

					// Gene Locus
					gene.setLocus(locus);

					// Product 
					Matcher matcher = patternHypothetical.matcher(product);
					if (matcher.find()) { 
						product = product +" "+ locus;   
					}
					gene.setProduct(product);

					// Position and orientation
					gene.setOrientation(orientation);
					gene.setStartPos(startPos);

					// Add organism id 
					// (redundant, but necessary without a database)
					gene.setOrganismID(sequence.getAccession().getID());


					// Add to genes array
					genes.add(gene);

					// 
					logger.debug(locus + "\t" + product + "\t" + geneName + "\t" + 
							orientation + "\t" + startPos);

				}// end-if CDS filter
			} //end-while Qualifier iteractor 			
		}//end-for GenBank features

		// Add genes to organism
		genome.setGenes(genes);
		// Add genome to list
		genomes.add(genome);
		//
		return fasta;
	}


	/**
	 * Creates Fasta file for an array of protein 
	 * sequences.
	 *  
	 * @param protSeq	Collections with proteins 
	 * 					sequences to be written. 
	 * @param fastaFile	Path to output Fasta file.
	 */
	protected void createFASTA(String fastaFile, ArrayList<ProteinSequence> protSeq) {
		// Write sequence to file
		try {			
			FileOutputStream outStream = new FileOutputStream(fastaFile);
			FastaWriter<ProteinSequence, AminoAcidCompound> fastaWriter = 
					new FastaWriter<ProteinSequence, AminoAcidCompound>(outStream, 
							protSeq, new GenericFastaHeaderFormat<ProteinSequence, AminoAcidCompound>()); // new FastaHeaderGI());
			fastaWriter.process();
			outStream.close();

		} catch (Exception e) { 
			String message = "Failed to create Fasta file for parsed genomes. ";
			logger.error(message);
			try {
				throw new BrokePipelineException(message, e);
			} catch (BrokePipelineException e1) {
				e1.printStackTrace();
			}
		}		
	}	

	
	/**
	 * Parses the GenBank file from proxy.
	 * 
	 * @param accessionID	Genome accession identifier.
	 * @param lifestyle		Organism lifestyle.
	 * 
	 * TODO: This method is incomplete. Missing parsing results 
	 * into ArrayList<ProteinSequence>. 
	 */
	protected ArrayList<ProteinSequence> parseProxy(String accessionID, String lifestyle) 
			throws IOException, InterruptedException, CompoundNotFoundException {
		// Create gbk reader 
		GenbankProxySequenceReader<AminoAcidCompound> genbankProteinReader =
				new GenbankProxySequenceReader<AminoAcidCompound>("/tmp", accessionID,
						AminoAcidCompoundSet.getAminoAcidCompoundSet());
		ProteinSequence proteinSequence = new ProteinSequence(genbankProteinReader);
		//
		genbankProteinReader.getHeaderParser().parseHeader(
				genbankProteinReader.getHeader(), proteinSequence);
		//
		return null;
	}
	
	

	/**
	 * Nested class implements FastaHeaderFormatInterface.
	 */
	@SuppressWarnings("rawtypes")
	public class FastaHeaderGI implements FastaHeaderFormatInterface {

		@Override
		public String getHeader(Sequence sequence) {
			String id = "";
			AccessionID accessionID = sequence.getAccession();
			if (accessionID != null) {
				id = sequence.getAccession().getIdentifier().toString();
			}
			//
			return id;
		}
	}
	
	

}