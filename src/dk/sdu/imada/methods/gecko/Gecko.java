/**
 * 
 */
package dk.sdu.imada.methods.gecko;

/**
 * Class defines a Gecko object. It stores all 
 * parameters and files used to run Gecko.
 * 
 * @author Eudes Barbosa (eudes@imada.sdu.dk)	
 */
public class Gecko {
	
	// Variables declaration
	protected int distance = -1;
	protected int size = -1;
	protected int genomes = -1;	
	protected String userGeckoFile = "";
	// Declaration end	
	
	/**
	 * @return 	Path to Gecko file provided 
	 * 			by the user. If none was 
	 * 			provided, it will return an 
	 * 			empty string. 
	 */
	public String getUserGeckoFile() {
		return userGeckoFile;
	}

	/**
	 * Sets path to Gecko file provided 
	 * by the user.
	 * 
	 * @param path 	Path to user's Blast file.
	 */
	public void setUserGeckoFile(String path) {
		this.userGeckoFile = path;
	}
	
	/**
	 * @return		Returns the maximum 
	 * 				allowed distance.
	 */
	public int getDistance() {
		return distance;
	}
	
	/**
	 * Sets the maximum allowed distance.
	 * @param distance	Distance.
	 */
	public void setDistance(int distance) {
		this.distance = distance;
	}
	
	/**
	 * @return 		Returns the minimum 
	 * 				cluster size.
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * Sets the minimum cluster size.
	 * @param size	Size.
	 */
	public void setSize(int size) {
		this.size = size;
	}
	
	/**
	 * @return 		Returns the minimum 
	 * 				number of covered 
	 * 				genomes.
	 */
	public int getGenomes() {
		return genomes;
	}
	
	/**
	 * Sets the minimum number of covered genomes.
	 * @param genomes		Number of genomes.
	 */
	public void setGenomes(int genomes) {
		this.genomes = genomes;
	}
	
}
