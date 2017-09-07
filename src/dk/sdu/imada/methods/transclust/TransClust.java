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
package dk.sdu.imada.methods.transclust;

/**
 * Class defines a Transitivity Cluster (TC) object. It will hold 
 * all necessary parameters to run the clustering process, namely: 
 * the range of density values (or a single value) and the number of 
 * steps within the range. While running TC with for a single threshold 
 * only the 'start' value should be taken into consideration. 
 * 
 * @author Eudes Barbosa (eudes@imada.sdu.dk)
 */
public class TransClust {

	// Variable associated with parameters //
	
	/** Density parameter: single/range start. */
	protected int start = 0;
	
	/** Density parameter: range end. */
	protected int end = -1;
	
	/** Number of steps in the range. */
	protected int steps = -1;
	
	/** Blast e-value. */
	protected double evalue = -1;
	
	/** Path to Blast file provided by the user. */
	protected String userBlastFile = "";
	
	/** Path to TransClust file provided by the user. */
	protected String userTransClustFile = "";
	
	/** True if using a range of values. False, otherwise. */
	protected boolean range = false;
	
	// Variable associated with process //
	
	/** Estimated time to run Blast (miliseconds). */
	protected long runtimeBlastEstimated = 0;
	
	/** Actual time to run Blast (miliseconds). */
	protected long runtimeBlastReal = 0;
	
	/** Actual time to run TransClust (miliseconds). */
	protected long runtimeTransClust = 0;


	/**
	 * @return 	Returns path to Blast file provided 
	 * 			by the user. If none was 
	 * 			provided, it will return an 
	 * 			empty string. 
	 */
	public String getUserBlastFile() {
		return userBlastFile;
	}

	/**
	 * Sets path to Blast file provided 
	 * by the user.
	 * 
	 * @param path 	Path to user's Blast file.
	 */
	public void setUserBlastFile(String path) {
		this.userBlastFile = path;
	}

	/**
	 * @return 	Returns path to TransClust file 
	 * 			provided by the user.
	 * 			 If none was provided, 
	 * 			it will return an 
	 * 			empty string. 
	 */
	public String getUserTransClustFile() {
		return userTransClustFile;
	}

	/**
	 * Sets path to TransClust file provided 
	 * by the user.
	 * 
	 * @param path 	Path to user's TransClust file.
	 */
	public void setUserTransClustFile(String path) {
		this.userTransClustFile = path;
	}

	/**
	 * @return		Returns Blast's 
	 * 				E-value cut-off
	 * 				set by the user. 				
	 */
	public double getEvalue() {
		return evalue;
	}

	/**
	 * Sets Blast E-value.
	 * 
	 * @param evalue	E-value
	 */
	public void setEvalue(double evalue) {
		this.evalue = evalue;
	}

	/**
	 * Gets the initial estimation for running 
	 * the Blast process. The estimation is based 
	 * on the number of inputed sequences, used 
	 * processors and shared eleven sized seeds.
	 * 
	 * @return		Returns Blast predicted runtime 
	 * 				(milliseconds).
	 */
	public long getRuntimeBlastEstimated() {
		return runtimeBlastEstimated;
	}

	/**
	 * Sets the initial estimation for running 
	 * the Blast process. The estimation was based 
	 * on the number of inputed sequences, used 
	 * processors and shared eleven sized seeds 
	 * (Blast class).
	 * 
	 * @param runtimeBlastEstimated		Blast predicted runtime 
	 * 									(milliseconds).
	 */
	public void setRuntimeBlastEstimated(long runtimeBlastEstimated) {
		this.runtimeBlastEstimated = runtimeBlastEstimated;
	}

	/**
	 * Gets the actual time required to run the 
	 * whole Blast process.
	 * 
	 * @return		Returns Blast runtime (milliseconds).
	 */
	public long getRuntimeBlastReal() {
		return runtimeBlastReal;
	}

	/**
	 * Sets the actual time required to run the 
	 * whole Blast process.
	 * 
	 * @param runtimeBlastReal		Blast runtime (milliseconds).
	 */
	public void setRuntimeBlastReal(long runtimeBlastReal) {
		this.runtimeBlastReal = runtimeBlastReal;
	}

	/**
	 * Gets the actual time required to run the 
	 * whole Transitivity Clustering process.
	 * 
	 * @return		Returns Transitivity Clustering runtime 
	 * 				(milliseconds).
	 */
	public long getRuntimeTransClust() {
		return runtimeTransClust;
	}

	/**
	 * Sets the actual time required to run the 
	 * whole Transitivity Clustering process.
	 * 
	 * @param runtimeTransClust		Transitivity Clustering runtime 
	 * 								(milliseconds).
	 */
	public void setRuntimeTransClust(long runtimeTransClust) {
		this.runtimeTransClust = runtimeTransClust;
	}

	/**
	 * Gets the start of the analysis range 
	 * for the multiple threshold; or simply 
	 * a unique value in case of single 
	 * threshold analysis.
	 * 
	 * @return Returns threshold value.
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Sets the start of the analysis range 
	 * for the multiple threshold; or simply 
	 * a unique value in case of single 
	 * threshold analysis.
	 * 
	 * @param start		Threshold value (range start 
	 * 					or unique value).
	 */
	public void setStart(int start) {
		this.start = start;
	}

	/**
	 * Gets the end of the analysis range 
	 * for the multiple threshold.
	 * 
	 * @return	Returns threshold value (range end).
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * Sets the end of the analysis range 
	 * for the multiple threshold.
	 * 
	 * @param end	Threshold value (range end).
	 */
	public void setEnd(int end) {
		this.end = end;
	}

	/**
	 * Gets the number of steps in the
	 * analysis.
	 * 
	 * @return	Returns number of steps.
	 */
	public int getSteps() {
		return steps;
	}

	/**
	 * Sets the number of steps in the
	 * analysis.
	 * 
	 * @param steps	Number of steps.
	 */
	public void setSteps(int steps) {
		this.steps = steps;
	}

	/**
	 * Verifies if it is a single or 
	 * multiple threshold analysis.
	 * 
	 * @return	Returns True if it is a multiple 
	 * 			threshold analysis; false 
	 * 			otherwise.
	 */
	public boolean isRange() {
		return range;
	}

	/**
	 * Verifies if it is a single or 
	 * multiple threshold analysis.
	 * 
	 * @param range		True if it is a multiple 
	 * 					threshold analysis; false 
	 * 					otherwise.
	 */
	public void setRange(boolean range) {
		this.range = range;
	}
}