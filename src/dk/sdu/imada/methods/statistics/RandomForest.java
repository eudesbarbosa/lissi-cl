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

/**
 * Class defines a Random Forest object. It stores all 
 * parameters and files used to run Random Forest.
 * 
 * @author Eudes Barbosa (eudes@imada.sdu.dk)	
 */
public class RandomForest {
	
	// Variables declaration
	protected int kfold = -1;
	protected int runs = -1;
	protected int trees = -1;
	protected String userMatrix = "";
	// Declaration end
	
	
	/**
	 * @return 	Path to Indicator Matrix provided 
	 * 			by the user. If none was 
	 * 			provided, it will return an 
	 * 			empty string. 
	 */
	public String getUserMatrix() {
		return userMatrix;
	}

	/**
	 * Sets path to Indicator Matrix file  
	 * provided by the user.
	 * 
	 * @param path 	Path to user's Blast file.
	 */
	public void setUserMatrix(String path) {
		this.userMatrix = path;
	}
	
	/**
	 * Gets the k number of sub-samples that the original sample 
	 * shall be partitioned into. A single sub-sample is retained as 
	 * the validation data for testing the model, and the remaining 
	 * k − 1 sub-samples are used as training data. 
	 * 
	 * @return the kfold	The k number of sub-samples.
	 */
	public int getKfold() {
		return kfold;
	}
	
	/**
	 * Sets the k number of sub-samples that the original sample 
	 * shall be partitioned into. A single sub-sample is retained as 
	 * the validation data for testing the model, and the remaining 
	 * k − 1 sub-samples are used as training data. 
	 * 
	 * @param kfold	The k number of sub-samples.
	 */
	public void setKfold(int kfold) {
		this.kfold = kfold;
	}
	
	/**
	 * Gets the number of times that the Random Forest process 
	 * shall be repeated. In each interaction, different k-folders 
	 * and randomized samples shall be used (probably).
	 * 
	 * @return	Number of Random Forest runs.
	 */
	public int getRuns() {
		return runs;
	}
	
	/**
	 * Sets the number of times that the Random Forest process 
	 * shall be repeated. In each interaction, different k-folders 
	 * and randomized samples shall be used (probably). 
	 * 
	 * @param runs	Number of Random Forest runs.
	 */
	public void setRuns(int runs) {
		this.runs = runs;
	}
	
	/**
	 * Gets the maximum number of trees to be generated in 
	 * each of the Random Forest runs. 
	 *  
	 * @return 	Maximum number of trees per run.
	 */
	public int getTrees() {
		return trees;
	}
	
	/**
	 * Sets the maximum number of trees to be generated in 
	 * each of the Random Forest runs.
	 * 
	 * @param trees	Maximum number of trees per run.
	 */
	public void setTrees(int trees) {
		this.trees = trees;
	}	
	

}
