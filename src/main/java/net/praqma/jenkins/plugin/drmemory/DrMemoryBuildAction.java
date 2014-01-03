package net.praqma.jenkins.plugin.drmemory;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import net.praqma.jenkins.plugin.drmemory.graphs.AbstractGraph;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.util.ChartUtil;
import hudson.util.ColorPalette;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;

public class DrMemoryBuildAction implements Action {

    private static final Logger logger = Logger.getLogger(DrMemoryBuildAction.class.getName());

    public enum GraphType {

        TOTAL_LEAK
    }
    private final AbstractBuild<?, ?> build;
    private List<DrMemoryPublisher> publishers;
    private List<DrMemoryBuilder> builders;
    private List<DrMemoryResult2> results;

    protected DrMemoryBuildAction(AbstractBuild<?, ?> build) {
        this.build = build;
        this.builders = new ArrayList<DrMemoryBuilder>();
        this.results = new ArrayList<DrMemoryResult2>();
        this.publishers = new ArrayList<DrMemoryPublisher>();
    }


    public void addPublisher(DrMemoryPublisher publisher) {
        this.publishers.add(publisher);
    }

    public void addBuilder(DrMemoryBuilder builder) {
        this.builders.add(builder);
    }

    public List<DrMemoryPublisher> getPublishers() {
        return publishers;
    }

    public void addResult(DrMemoryResult2 result) {
        this.results.add(result);
    }

    public List<DrMemoryBuilder> getBuilders() {
        return builders;
    }

    @Override
    public String getDisplayName() {
        return "DrMemory";
    }

    @Override
    public String getIconFileName() {
        return "graph.gif";
    }

    @Override
    public String getUrlName() {
        return "drmemory";
    }

    public List<DrMemoryResult2> getResults() {
        return results;
    }
    
    public static DrMemoryBuildAction getActionForBuild(AbstractBuild<?, ?> build) {
        DrMemoryBuildAction action = build.getAction(DrMemoryBuildAction.class);
        if (action == null) {
        	action = new DrMemoryBuildAction(build);
        	build.addAction(action);
        }
    	
    	return action;
    }

    public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        File dm = new File(build.getRootDir(), DrMemoryPublisher.__OUTPUT);
        Calendar t = build.getTimestamp();
        if (dm.exists()) {
            rsp.serveFile(req, FileUtils.openInputStream(dm), t.getTimeInMillis(), dm.getTotalSpace(), "drmemory.txt");
        } else {
            rsp.sendError(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    static DrMemoryBuildAction getPreviousResult(AbstractBuild<?, ?> start) {
        AbstractBuild<?, ?> b = start;
        while ((b = b.getPreviousBuild()) != null) {
            DrMemoryBuildAction r = b.getAction(DrMemoryBuildAction.class);
            if (r == null || r.getResults() == null || r.getResults().isEmpty()) {
                continue;
            }

            return r;
        }
        return null;
    }

    public DrMemoryBuildAction getPreviousResult() {
        return getPreviousResult(build);
    }

    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        String type = req.getParameter("type");

        logger.fine("Graphing " + type);

        int width = 500, height = 200;
        String w = req.getParameter("width");
        String h = req.getParameter("height");
        if (w != null && w.length() > 0) {
            width = Integer.parseInt(w);
        }

        if (h != null && h.length() > 0) {
            height = Integer.parseInt(h);
        }

        if (type == null) {
            throw new IOException("No type given");
        }

        if (ChartUtil.awtProblemCause != null) {
            // not available. send out error message
            rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
            return;
        }

        Calendar t = build.getTimestamp();

        if (req.checkIfModified(t, rsp)) {
            return; // up to date
        }

        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

        boolean latest = true;
        String yaxis = "???";
        float max = 0;

        AbstractGraph g = DrMemoryPublisher.getGraphTypes().get(type);
        if (g == null) {
            rsp.sendError(1);
            return;
        }

        /* For each build, moving backwards */
        for (DrMemoryBuildAction a = this; a != null; a = a.getPreviousResult()) {
            logger.finest("Build " + a.getDisplayName());

            /* Make the x-axis label */
            ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(a.build);

            if(a.getResults().size() == 0)
            	return;
            
        	int fields = g.getNumber(a.getResults().get(0)).length;
            float[] sums = new float[fields];
            
            /* Sum all results */
            for(DrMemoryResult2 result : a.getResults()) {
	            float[] ns = g.getNumber(result);
	            for(int i=0; i < ns.length; ++i) {
	            	sums[i] += ns[i];      	
	            	max = Math.max(max,sums[i]);
	            }
            }
            
            g.addX(dsb, sums, label);

            if (latest) {
                yaxis = g.getYAxis();
                latest = false;
            }
        }

        ChartUtil.generateGraph(req, rsp, createChart(dsb.build(), g.getTitle(), yaxis, (int) max), width, height);
    }

    private JFreeChart createChart(CategoryDataset dataset, String title, String yaxis, int max) {

        final JFreeChart chart = ChartFactory.createLineChart(title, // chart
                // title
                null, // unused
                yaxis, // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips
                false // urls
                );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

        chart.setBackgroundPaint(Color.white);

        final CategoryPlot plot = chart.getCategoryPlot();

        // plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setUpperBound(max);
        rangeAxis.setLowerBound(0);

        final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseStroke(new BasicStroke(2.0f));
        ColorPalette.apply(renderer);

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(5.0, 0, 0, 5.0));

        return chart;
    }
}
