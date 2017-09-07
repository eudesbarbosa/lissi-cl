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
package dk.sdu.imada.view.statistics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import dk.sdu.imada.methods.ArrayComparator;
import dk.sdu.imada.methods.Command;



/**
 * Class gets the join distribution of either the gene clusters 
 * or islands among the two lifestyles.
 * 
 * @author Eudes Barbosa (eudes@imada.sdu.dk)
 */
public class JointDistribution implements Command {
	
	//------  Variable declaration  ------//

	private static final Logger logger = LogManager.getLogger(JointDistribution.class.getName());

	protected String file = "";
	protected String xlab = "";
	protected String ylab = "";
	protected String[][] matrixString;
	protected double[][] matrixPoints; 
	
	//------  Declaration end  ------//

	/**
	 * Gets the join distribution of either the gene clusters 
	 * or islands among the two lifestyles.
	 *   
	 * @param xlab		The lifestyle (name) occupying the X axis.
	 * @param ylab		The lifestyle (name) occupying the Y axis.
	 * @param matrix	Path to indicator matrix.
	 */
	public JointDistribution(String xlab, String ylab, String matrix) {
		this.file = matrix;
		this.xlab = xlab;
		this.ylab = ylab;
	}

	@Override
	public void exec() {
		// Parse indicator matrix
		indicatorMatrix();
		// Create joint distribution
		distribution();		
		logger.debug("Distribution matrix : " + matrixPoints.length + "\t|\t" + matrixPoints[0].length);
	}

	/**
	 * @return		Returns the joint distribution 
	 * 				of the clusters (2D).
	 */
	public double[][] getDistribution() {
		return matrixPoints;
	}	

	/**
	 * Calculate the distribution of clusters in a 
	 * 2-dimensional space.
	 */
	private void distribution() {
		// Get labels
		ArrayList<String> labels = new ArrayList<String>();
		int rows = matrixString.length;
		int cols = matrixString[0].length; 
		for(int i = 0; i < rows; i++)
			labels.add(matrixString[i][1]);

		// Comment as soon as I remember what the hell is that

		int labelA = labels.lastIndexOf(labels.get(0));
		double countA = labelA + 1;
		double countB = rows - countA;
		logger.debug("Count A = "+ countA + " and Count B = "+ countB);

		double[] labAPoints = new double[cols-2];
		double[] labBPoints = new double[cols-2];

		/* Calculate points coordinates
		 * Starts at two to ignore columns 'lifestyle' 
		 * and genome.
		 */
		for(int i = 2; i < cols; i++) { 
			// First label (alphabetically)
			int sumA = 0;
			for (int j = 0; j <= labelA; j++) {
				sumA += Integer.parseInt(matrixString[j][i]);
			}
			// Second label (alphabetically)
			int sumB = 0;
			int k = labelA + 1;
			for (;k < rows; k++) {
				sumB += Integer.parseInt(matrixString[k][i]);
			}
			// Add values to arrays
			labAPoints[i-2] = (sumA/countA)*100;
			labBPoints[i-2] = (sumB/countB)*100;	
			logger.debug("Point : " + labAPoints[i-2] + "\t|\t" + labBPoints[i-2]);
		}

		// ...
		if(labels.get(0).contains(xlab)) {
			matrixPoints = combineArrays(labAPoints,labBPoints);
		} else {
			matrixPoints = combineArrays(labBPoints,labAPoints);
		}
	}

	/**
	 * Combines two given arrays to form a 
	 * 2-dimensional matrix.
	 * 
	 * @param a0	First array (x axis).
	 * @param a1	Second array (y axis).
	 * 
	 * @return		A 2-dimensional matrix 
	 * 				with the provided arrays.
	 */
	private double[][] combineArrays(double[] a0, double[] a1) {
		double[][] combined = new double[a0.length][2];
		for(int i = 0; i< a0.length; i++){
			combined[i][0] = a0[i];
			combined[i][1] = a1[i];		
		}		
		//
		return combined;
	}

	/**
	 * Parses indicator matrix file and stores
	 * each value into a multidimensional String 
	 * array.
	 */
	private void indicatorMatrix() {
		// Initialize variable
		ArrayList<String> list = new ArrayList<String>();
		// Load indicator matrix file
		BufferedReader br;		
		try {
			// Buffer file
			br = new BufferedReader(new FileReader(file));
			String stringLine = "";
			// Read all lines
			while ((stringLine = br.readLine()) != null) {
				// Add all lines to array
				list.add(stringLine);
			}
		} catch (IOException e) {
			System.out.println("FileNotFoundException: " + e);		
		}

		// Create matrix
		int row = list.size() -1 ;
		String[] data = list.get(0).split(";");
		int col = data.length;
		matrixString = new String[row][col];
		logger.debug("Matrix : \t" + row + "\t|\t" + col);

		// Populate matrix (ignores header)
		for (int i = 0; i < list.size()-1; i++) {
			String[] cells = list.get(i+1).split(";");
			for(int j = 0; j < cells.length; j++){
				matrixString[i][j] = cells[j];
			}				
		}		
		// Sort based on lifestyle name
		Arrays.sort(matrixString, new ArrayComparator(1, true)); //true = ascending
	}

	/**
	 * Combines two matrix (bind rows).
	 * 
	 * @param m0	First matrix.
	 * @param m1	Second matrix.
	 * @return		Returns combined matrix.
	 */
	@SuppressWarnings("unused")
	private String[][] combineMatrices(String[][] m0, String[][] m1) {
		String result[][] = new String[m0.length+m1.length][];
		// Clone first matrix rows
		for (int i=0; i<m0.length; i++) {
			result[i] = m0[i].clone();
		}
		// Clone second matrix rows
		for (int i=0; i<m1.length; i++) {
			result[m0.length+i] = m1[i].clone();
		}
		return result;
	}


}
