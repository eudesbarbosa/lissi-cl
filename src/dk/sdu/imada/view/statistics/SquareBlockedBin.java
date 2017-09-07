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


import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



import org.jfree.chart.ChartColor;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;



/**
 * Class creates a heatmap-like graph for the
 * joint distribution of clusters/islands among 
 * the different lifestyles. It includes a slider 
 * that change the sizer o of the square bin. 
 * This ways is possible to have high resolution 
 * (smaller bins) or low resolution (bigger bins). 
 * The class implements the Command Pattern.
 *  
 * @author Eudes Barbosa
 *
 */
public class SquareBlockedBin extends JPanel {

	//------  Variable declaration  ------//

	// Set variable for GraphicsEnvironment
	static { 
		System.setProperty("java.awt.headless", "true");
		//System.out.println(java.awt.GraphicsEnvironment.isHeadless());
	}

	private static final long serialVersionUID = 788516403531582158L;

	private static final Logger logger = LogManager.getLogger(SquareBlockedBin.class.getName());

	/** JFreeChart object. */
	protected JFreeChart chart;
	
	/** JFreeChart Dataset. */
	protected XYZDataset data;
	
	/** Number of clusters. */
	protected long clusters = 0;
	
	/** Path to indicator matrix. */
	protected String matrix = "";
	
	/** X-axis label (lifestyle name). */
	protected String xlab = "";
	
	/** Y-axis label (lifestyle name). */
	protected String ylab = "";
	
	/** Graph title (if clusters or islands). */
	protected String title = "";
	
	/** Clusters distribution. */
	protected double[][] distribution; 
	
	/** Value of 15th percentile. */
	protected int per15 = -1;
	
	/** Value of 35th percentile. */
	protected int per35 = -1;
	
	/** Value of 55th percentile. */
	protected int per55 = -1;
	
	/** Value of 75th percentile. */
	protected int per75 = -1;

	/** Largest cluster. */
	protected int largestClust = -1;

	//------  Declaration end  ------//

	/**
	 * Creates a heatmap-like graph for the joint 
	 * distribution of clusters/islands among both 
	 * lifestyles.  
	 * 
	 * @param title		Graph title (clusters or islands).
	 * @param xlab		X-axis label (lifestyle name).
	 * @param ylab		Y-axis label (lifestyle name).
	 * @param matrix	Path to indicator matrix.
	 * @param localDir	Path to local working directory.
	 */
	public SquareBlockedBin(String title, String xlab, String ylab,
			String matrix, String localDir) {
		// Set axis label and configure chart
		String perc = " (%)";
		this.xlab = xlab + perc;
		this.ylab = ylab + perc;
		this.title = title;
		this.matrix = matrix;
		// Initialize and configure
		initComponents();
		// Print graph
		printGraph(localDir);
	}
	

	protected void initComponents() {
		// Get clusters joint distribution (among lifestyles)
		JointDistribution jd = new JointDistribution(xlab, ylab, matrix);
		jd.exec();
		distribution = jd.getDistribution();
		logger.debug("Distribution matrix : " + distribution.length + "\t|\t" + distribution[0].length);
		// Configure chart
		createChart(createDataset(), 2.0);
	}

	
	/**
	 * Creates the graph.
	 * @param dataset	Dataset.
	 */
	protected void createChart(XYZDataset dataset, double size) {
		// Configure axis
		NumberAxis xAxis = new NumberAxis(xlab);
		xAxis.setLowerMargin(0.0);
		xAxis.setUpperMargin(0.0);
		NumberAxis yAxis = new NumberAxis(ylab);
		yAxis.setUpperMargin(0.0);
		yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		// Configure block size and scale
		XYBlockRenderer renderer = new XYBlockRenderer();
		renderer.setBlockWidth(size);
		renderer.setBlockHeight(size);
		// Configure scale and add colors
		LookupPaintScale scale = new LookupPaintScale(0, largestClust, ChartColor.VERY_LIGHT_BLUE);
		scale.add(per15, ChartColor.LIGHT_BLUE);
		scale.add(per35, ChartColor.BLUE);
		scale.add(per55, ChartColor.DARK_BLUE);
		scale.add(per75, ChartColor.VERY_DARK_BLUE);
		scale.add(largestClust, ChartColor.BLACK);
		renderer.setPaintScale(scale); 			
		// Configure plot
		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
		plot.setOrientation(PlotOrientation.HORIZONTAL);
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinesVisible(false);
		plot.setRangeGridlinesVisible(false);
		// Adjust axis range
		ValueAxis domainAxis = plot.getDomainAxis();
		ValueAxis rangeAxis = plot.getRangeAxis();			
		domainAxis.setRange(-1, 100.5);
		rangeAxis.setRange(-1, 100.5);
		// Create legend
		NumberAxis scaleAxis = new NumberAxis("Scale");
		scaleAxis.setAxisLinePaint(Color.WHITE);
		scaleAxis.setTickMarkPaint(Color.WHITE);
		scaleAxis.setTickLabelFont(new Font("Dialog", 0, 7));
		LinePaintScaleLegend paintscalelegend = new LinePaintScaleLegend(renderer.getPaintScale(), scaleAxis);
		paintscalelegend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		paintscalelegend.setAxisOffset(5D);
		paintscalelegend.setMargin(new RectangleInsets(5D, 5D, 5D, 5D));
		paintscalelegend.setFrame(new BlockBorder(Color.WHITE));
		paintscalelegend.setPadding(new RectangleInsets(10D, 10D, 10D, 10D));
		paintscalelegend.setStripWidth(10D);
		paintscalelegend.setPosition(RectangleEdge.RIGHT);
		paintscalelegend.setBackgroundPaint(new Color(255, 255, 255));
		// Configure tool tip
		plot.getRenderer().setBaseToolTipGenerator(new XYToolTipGenerator() {
			@Override
			public String generateToolTip(XYDataset dataset, int series, int item) {
				XYZDataset xyzDataset = (XYZDataset)dataset;
				double z = xyzDataset.getZValue(series, item);
				//System.out.println("x =" + x + " | " + y + " | " + z);
				return (title.replace("Distribution", "") + Math.round(z) );
			}
		});
		//
		chart = new JFreeChart(title, plot);
		chart.removeLegend();
		chart.addSubtitle(paintscalelegend);
		chart.setBackgroundPaint(Color.white);
	} 


	/** @return Returns a sample dataset. */
	protected XYZDataset createDataset() {
		Set<String> map = new LinkedHashSet<String>();
		ArrayList<String> full = new ArrayList<>();
		// Copy distribution array
		for(int i = 0; i < distribution.length; i++) {
			full.add(distribution[i][0]+"\t"+distribution[i][1]);
			map.add(distribution[i][0]+"\t"+distribution[i][1]);
		}
		// Create heatmap arrays
		double[] xvalues = new double[map.size()];
		double[] yvalues = new double[map.size()];
		double[] zvalues = new double[map.size()];

		// Populate arrays
		int i = 0;
		double x;
		double y;
		double z;			
		for (String s : map) {
			// Get values
			z = Collections.frequency(full,s);
			String[] data = s.split("\t");
			x = Double.parseDouble(data[0]);
			y = Double.parseDouble(data[1]);
			// Add to array
			xvalues[i] = x;
			yvalues[i] = y;
			zvalues[i] = z;
			//
			i++;
		}
		// Create data set
		DefaultXYZDataset dataset = new DefaultXYZDataset();
		dataset.addSeries("Only", 
				new double[][] { xvalues, yvalues, zvalues });

		// Configure color percentil
		setColorPercentil(zvalues);

		//
		data = dataset;
		return dataset;
	}


	/**
	 * Divides the available colors according to the distributions 
	 * of clusters in the two dimensional space.
	 * 
	 * @param freq		Occurrences of all clusters throughout the 
	 * 					genomes. It will divide color set according 
	 * 					to the distributions percentiles. 
	 */
	private void setColorPercentil(double[] freq) {
		// Get percentile information	
		DescriptiveStatistics da = new DescriptiveStatistics(freq);
		per15 = (int) Math.abs(da.getPercentile(15));
		per35 = (int) Math.abs(da.getPercentile(35));
		per55 = (int) Math.abs(da.getPercentile(55));
		per75 = (int) Math.abs(da.getPercentile(75));
		largestClust = (int) Math.abs(da.getMax());
	}


	/** 
	 * Save Distribution graph to file. 
	 * @param localDir Path to local working directory.
	 */
	private void printGraph(String localDir) {
		// File name
		String file = localDir.concat(File.separator).concat("RandomForest")
				.concat(File.separator).concat("Distribution.png");
		// Buffer image and save to file
		BufferedImage img = chart.createBufferedImage(800, 600);
		try {
			ImageIO.write(img, "PNG", new File(file));
		} catch (IOException e) {
			logger.error("Failed to save Distribution graph to file.");
			e.printStackTrace();
		}	
	}

}