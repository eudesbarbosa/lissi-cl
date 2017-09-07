package dk.sdu.imada.methods;

import java.util.Comparator;

/**
 * Class sorts a matrix based on a given column. 
 * It can be either ascending (true) or descending 
 * (false).
 * @author WEB.
 */
@SuppressWarnings("rawtypes")
public class ArrayComparator implements Comparator<Comparable[]> {

	//------  Variable declaration  ------//
	
	/** Column to be sorted. */
	protected int columnToSort;
	
	/** True for ascending and False for descending. */
	protected boolean ascending;
	
	
	//------  Declaration end  ------//

	/**
	 * Sorts a matrix based on a given column. 
	 * It can be either ascending (true) or descending 
	 * (false).
	 * @param columnToSort	Column to be sorted.
	 * @param ascending		True for ascending and False 
	 * 						for descending.
	 */
	public ArrayComparator(int columnToSort, boolean ascending) {
		this.columnToSort = columnToSort;
		this.ascending = ascending;
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Comparable[] c1, Comparable[] c2) {
		@SuppressWarnings("unchecked")
		int cmp = c1[columnToSort].compareTo(c2[columnToSort]);
		return ascending ? cmp : -cmp;
	}
}