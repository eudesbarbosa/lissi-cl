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
package dk.sdu.imada.view.transclust;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import dk.sdu.imada.methods.ExecutePipeline;
import dk.sdu.imada.methods.genome.Genome;


/**
 * Class creates a new panel with a histogram reflecting the 
 * frequency of clusters. 
 * 
 * @author Eudes Barbosa
 */
public class HistogramPanel extends JPanel {


	//------  Variable declaration  ------//

	// Set variable for GraphicsEnvirment
	static { 
		System.setProperty("java.awt.headless", "true");
		//System.out.println(java.awt.GraphicsEnvironment.isHeadless());
	}

	private static final long serialVersionUID = -5160063334682121025L;

	private static final Logger logger = LogManager.getLogger(HistogramPanel.class.getName());

	/** Chart that will store the histogram. */
	protected JFreeChart chart;
	

	//------  Declaration end  ------//



	/**
	 * Creates a new panel with a histogram reflecting the 
	 * frequency of clusters. 
	 * 
	 * @param localDir		Path to local working directory.
	 * @param occurrences	Array with number of occurrences
	 * 						that all clusters were present 
	 * 						throughout the genomes.
	 * @param threshold		Density threshold. It will be used 
	 * 						to set the title of the histogram.
	 */
	public HistogramPanel(String localDir, double[] occurrences, 
			int threshold) {
		// Line chart information
		loadLineChart(occurrences, threshold);
		// Visualize chart
		setBackground(Color.WHITE);
		ChartPanel cpanel = new ChartPanel(chart);
		JScrollPane scroll = new JScrollPane(cpanel);
		add(scroll);
		// Print graph
		printGraph(localDir);		
	}

	/**
	 * Set line chart values. 
	 * 
	 * @param occur			Array with number of occurrences
	 * 						that all clusters were present 
	 * 						throughout the genomes.
	 * 
	 * @param threshold		Density threshold. It will be used 
	 * 						to set the title of the histogram. 
	 */
	protected void loadLineChart(double[] occurrences, int threshold) {
		// Get number of different organisms 
		// (currently only different species)
		//double numOrg = 0.5;
		int numSpecies = getNumberOfOrganisms();

		// Create dataset
		XYSeriesCollection dataset = createDataset(occurrences);

		// Create chart
		String title = "Homology detection - density parameter = " + threshold;
		createChart(title, numSpecies, dataset);
	}

	/**
	 * @param occur			Array with number of occurrences
	 * 						that all clusters were present 
	 * 						throughout the genomes.
	 * 
	 * @return				Returns the converted array of frequencies 
	 * 						into a set of points for the line chart.				
	 */
	private XYSeriesCollection createDataset(double[] occur) {
		// Initialize variables
		XYSeriesCollection dataset;
		List<String> asList = new ArrayList<String>();
		XYSeries serie = new XYSeries("First");
		dataset = new XYSeriesCollection();

		// Convert doubles to string
		// (Collections methods are used to get frequency)
		for (int i = 0; i < occur.length; i++) {
			asList.add(""+occur[i]);
		}
		// Get frequency and add points
		Set<String> mySet = new HashSet<String>(asList);
		for(String s: mySet) {			
			serie.add(Double.parseDouble(s), Math.log(Collections.frequency(asList,s)));
		}
		//
		dataset.addSeries(serie);
		//
		return dataset;
	}

	/**
	 * Creates a simple chart.
	 * 
	 * @param title			Title of the chart.
	 * @param numSpecies	Number of species under analysis.
	 * @param dataset 		Set of points for the line chart.
	 */
	private void createChart(String title, double numSpecies, XYSeriesCollection dataset) {
		chart = ChartFactory.createXYLineChart(
				title, 							//title
				"Clusters size",				//x-lab
				"log(Frequency)",				//y-lab
				dataset, 						//dataset
				PlotOrientation.VERTICAL, 		//orientation
				false, 							//legend
				false, 							//tooltips
				false							//urls
				);

		// Configure chart
		chart.setBackgroundPaint(new Color(230,230,230));
		XYPlot xyplot = (XYPlot)chart.getPlot();
		xyplot.setForegroundAlpha(0.7F);
		xyplot.setBackgroundPaint(Color.WHITE);
		xyplot.setDomainGridlinePaint(new Color(150,150,150));
		xyplot.setRangeGridlinePaint(new Color(150,150,150));
		/*
		XYBarRenderer xybarrenderer = (XYBarRenderer)xyplot.getRenderer();
		xybarrenderer.setShadowVisible(false);
		xybarrenderer.setBarPainter(new StandardXYBarPainter()); 
		 */
		// Set axis labels as integers
		ValueAxis axis = xyplot.getDomainAxis();
		axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		//
		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesLinesVisible(0, true);
		renderer.setSeriesPaint(0, Color.BLUE);
		xyplot.setRenderer(renderer);

		// Add line (number of organisms)
		ValueMarker marker = new ValueMarker(numSpecies);  //position is the value on the axis
		marker.setPaint(Color.black);
		marker.setLabel("Species");
		marker.setLabelOffset(new RectangleInsets(10,10,10,20));        
		marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
		marker.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
		//
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.addDomainMarker(marker);

	}

	/**
	 * So far, this method is fairly simple. It can identify 
	 * the difference between organisms only at species 
	 * level. It compares the first two names of the organism 
	 * definition (name) and return if they are the same.
	 * 
	 * @return		Returns the number of organisms used  
	 * 				under analysis. 
	 */
	private int getNumberOfOrganisms() {
		// TODO: Species under analysis - make it less naïve 
		// Get all organisms definition (names)
		List<Genome> organism = ExecutePipeline.getGenomes();
		Set<String> distincSpecies = new LinkedHashSet<String>();
		// Verify unique species (equal first two names)
		for(Genome org : organism){
			String[] data = org.getName().split("\\s");
			distincSpecies.add(data[0]+" "+data[1]);
		}	
		//
		return distincSpecies.size();
	}

	//--------------------------------//
	// HISTOGRAM METHODS (UNUSED)

	/**
	 * Set histogram values. 
	 * 
	 * @param occur			Array with number of occurrences
	 * 						that all clusters were present 
	 * 						throughout the genomes.
	 * 
	 * @param threshold		Density threshold. It will be used 
	 * 						to set the title of the histogram. 
	 */
	@SuppressWarnings("unused")
	private void loadHistogram(double[] occur, int threshold) {
		// Get number of different organisms 
		// (currently only different species)
		//double numOrg = 0.5;
		//double maxValue = 1;	
		int numOrg = getNumberOfOrganisms();
		int maxValue = (int) Math.round(numOrg + numOrg*0.125); //arbitrary value, don't ask why

		// Calculate bin size
		int bin = setBin(occur);

		/* Create histogram data set
		 * key - the series key (null not permitted).
		 * values - the raw observations.
		 * bins - the number of bins (must be at least 1).
		 * minimum - the lower bound of the bin range.
		 * maximum - the upper bound of the bin range. 
		 */
		HistogramDataset dataset = new HistogramDataset();
		dataset.addSeries("Clusters", occur, bin, 0, maxValue);

		// Create chart
		String title = "Transitivity Clustering - density parameter = " + threshold;
		createDataset(occur);
		//createChart(title, numOrg, dataset);
	}

	/**
	 * Calculates bin width based on Freedman-Diaconisi
	 * rule.
	 * Bin = diff(range(x)) / (2 * IQR(x) / length(x)^(1/3)))
	 * 
	 * @param occur		Occurrences of clusters throughout
	 * 					the genomes.
	 * 
	 * @return			Bin width.
	 */
	private int setBin(double[] doubOccur) {
		int bin = 0;
		// Get interquartile range (IQR)		
		DescriptiveStatistics da = new DescriptiveStatistics(doubOccur);
		double iqr = 1;
		if (da.getPercentile(75) != da.getPercentile(25)){
			iqr = da.getPercentile(75) - da.getPercentile(25);
		}
		// Get minimum value
		double max = da.getMax(); 
		// Get maxim value 
		double  min = da.getMin();
		// Calculate bin
		bin = (int) Math.round(((Math.abs(max - min))/((2 * iqr)/(da.getN()^(1/3))))); 
		//		
		return bin;
	}

	/**
	 *  Save Distribution graph to file.
	 * 
	 *  @param localDir	Path to local working directory.
	 */
	private void printGraph(String localDir) {
		// File name
		String file = localDir.concat(File.separator).concat("Cluster_Size_Freq.png");
		// Buffer image and save to file
		BufferedImage img = chart.createBufferedImage(800, 600);
		logger.info("Saving histogram at : " + file);
		try {
			ImageIO.write(img, "PNG", new File(file));
		} catch (IOException e) {
			logger.error("Failed to save Distribution graph to file.");
			e.printStackTrace();
		}	
	}

	/**
	 * @return	[Test Method] Returns random range of values.
	 */
	@SuppressWarnings("unused")
	private double[] testvalues() {
		double[] v1 = {
				0.2029, 0.2056, 0.2072, 0.2104, 0.2106, 0.2116, 0.2119, 
				0.2135, 0.2146, 0.2149, 0.2154, 0.2165, 0.2203, 0.2234, 0.2237,
				0.2242, 0.2264, 0.2265, 0.2266, 0.2267, 0.2274, 0.2276, 0.2288, 
				0.2291, 0.2292, 0.2314, 0.2351, 0.2369, 0.2429, 0.2456, 0.2482, 
				0.2485, 0.2515, 0.2567, 0.2583, 0.2663, 0.2682, 0.2690, 0.2708, 
				0.2710, 0.2714, 0.2722, 0.2733, 0.2764, 0.2765, 0.2789, 0.2790, 
				0.2800, 0.2856, 0.2859, 0.2880, 0.2897, 0.2901, 0.2932, 0.2933, 
				0.2952, 0.2962, 0.2980, 0.2991, 0.3027, 0.3034, 0.3049, 0.3050, 
				0.3056, 0.3059, 0.3068, 0.3068, 0.3078, 0.3084, 0.3104, 0.3153, 
				0.3159, 0.3188, 0.3189, 0.3273, 0.3283, 0.3283, 0.3284, 0.3286, 
				0.3288, 0.3291, 0.3315, 0.3323, 0.3352, 0.3373, 0.3383, 0.3399, 
				0.3401, 0.3415, 0.3442, 0.3467, 0.3485, 0.3516, 0.3520, 0.3524, 
				0.3526, 0.3655, 0.3681, 0.3753, 0.3852, 0.3881, 0.3909, 0.3917, 
				0.3918, 0.3951, 0.3959, 0.3963, 0.3970, 0.3977, 0.3978, 0.4110, 
				0.4110, 0.4112, 0.4131, 0.4139, 0.4140, 0.4164, 0.4167, 0.4213, 
				0.4217, 0.4258, 0.4266, 0.4269, 0.4287, 0.4311, 0.4441, 0.4475, 
				0.4554, 0.4563, 0.4571, 0.4613, 0.4654, 0.4674, 0.4727, 0.4784, 
				0.4800, 0.4831, 0.4852, 0.4926, 0.4944, 0.5000, 0.5000, 0.5000, 
				0.5058, 0.5065, 0.5074, 0.5125, 0.5144, 0.5226, 0.5229, 0.5290, 
				0.5306, 0.5328, 0.5345, 0.5359, 0.5464, 0.5475, 0.5686, 0.5730, 
				0.5731, 0.5747, 0.5769, 0.5953, 0.6057, 0.6203, 0.6203, 0.6268, 
				0.6579, 0.6648, 0.6682, 0.6870, 0.6924, 0.6950, 0.6990, 0.7033, 
				0.7207, 0.7260, 0.7317, 0.7334, 0.7353, 0.7355, 0.7374, 0.7383, 
				0.7405, 0.7425, 0.7879, 0.8013, 0.8283, 0.8307, 0.8349, 0.8466, 
				0.8502, 0.8541, 0.8583, 0.8605, 0.8628, 0.8763, 0.8768, 0.8920, 
				0.8930, 0.9045, 0.9093, 0.9317, 0.9324, 0.9629, 0.9819
		};
		//
		return v1;
	}
}
