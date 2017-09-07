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
package dk.sdu.imada.methods;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Class contains common methods for parsing files.
 * 
 * @author Eudes Barbosa
 */
public class Parser {

	//       Variable declaration       //

	private static final Logger logger = LogManager.getLogger(Parser.class.getName());
	
	//         Declaration end         //
	
	/**
	 * @param file	Path to file.
	 * @return	Returns a BufferedReader for the provided file.
	 */
	protected static BufferedReader getBufferedReader(String file) {
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr); 
			//
			return br;
		} catch (IOException e) {
			logger.error("Error while reading file: " + file);		
			BufferedReader br = null;
			//
			return br;
		}
	}
	
	/**
	 * @param list	List to be splitted.
	 * @param L		Maximum size of sublist. 
	 * @return  Split a list into non-view sublists of length L;
	 * and returns sublists with L size.
	 */
	protected <T> List<List<T>> chopped(List<T> list, final int L) {
	    List<List<T>> parts = new ArrayList<List<T>>();
	    final int N = list.size();
	    for (int i = 0; i < N; i += L) {
	        parts.add(new ArrayList<T>(
	            list.subList(i, Math.min(N, i + L)))
	        );
	    }
	    //
	    return parts;
	}
}