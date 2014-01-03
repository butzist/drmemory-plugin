package net.praqma.jenkins.plugin.drmemory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.drmemory.exceptions.InvalidInputException;
import net.praqma.jenkins.plugin.drmemory.graphs.AbstractGraph;
import net.praqma.jenkins.plugin.drmemory.graphs.ActualLeaksGraph;
import net.praqma.jenkins.plugin.drmemory.graphs.AllLeaksGraph;
import net.praqma.jenkins.plugin.drmemory.graphs.BytesOfLeakGraph;
import net.praqma.jenkins.plugin.drmemory.graphs.InvalidHeapArgumentsGraph;
import net.praqma.jenkins.plugin.drmemory.graphs.StillReachableAllocationsGraph;
import net.praqma.jenkins.plugin.drmemory.graphs.TotalLeaksGraph;
import net.praqma.jenkins.plugin.drmemory.graphs.UnaddressableAccessesGraph;
import net.praqma.jenkins.plugin.drmemory.graphs.UninitializedAccessesGraph;
import net.praqma.jenkins.plugin.drmemory.graphs.WarningsGraph;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

public class DrMemoryPublisher extends Recorder {

    private static final Logger logger = Logger.getLogger(DrMemoryPublisher.class.getName());
    public static final String __OUTPUT = "drmemory.txt";
    private String logPath;
    public static Map<String, AbstractGraph> graphTypes = new HashMap<String, AbstractGraph>();

    static {
        graphTypes.put("total-leaks", new TotalLeaksGraph());
        graphTypes.put("all-leaks", new AllLeaksGraph());
        graphTypes.put("actual-leaks", new ActualLeaksGraph());
        graphTypes.put("bytes-of-leak", new BytesOfLeakGraph());
        graphTypes.put("allocations", new StillReachableAllocationsGraph());
        graphTypes.put("uninitialized-accesses", new UninitializedAccessesGraph());
        graphTypes.put("unaddressable-accesses", new UnaddressableAccessesGraph());
        graphTypes.put("warnings", new WarningsGraph());
        graphTypes.put("invalid-heap-arguments", new InvalidHeapArgumentsGraph());
    }
    
	private static final Pattern directory_pattern = Pattern.compile("DrMemory\\-(.*)\\.(.*)\\.(.*)");

    @DataBoundConstructor
    public DrMemoryPublisher(String logPath) {
    	this.logPath = logPath;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        File rootDir = build.getRootDir();
    	File resultOutput = new File(rootDir, __OUTPUT);
        PrintStream out = listener.getLogger();
        DrMemoryBuildAction action = DrMemoryBuildAction.getActionForBuild(build);

        out.println("My workspace is " + build.getWorkspace());

        assert(logPath != null);
        FilePath resultPath = new FilePath(build.getWorkspace(), logPath);
        
        List<FilePath> resultFiles = new ArrayList<FilePath>();
    	try {
    		FilePath[] rr = resultPath.list("**/results.txt");
    		for (FilePath r : rr) {
    			resultFiles.add(r);
    		}
    	}
    	catch(Exception e) {
    		out.println("Error reading results: " + e.toString());        		
    	}

        if (resultFiles.isEmpty()) {
            out.println("No results to parse");
            return true;
        }
        
        /* Concatenate results */
        try {
        	StringBuffer buffer = new StringBuffer();
        	for (FilePath result : resultFiles) {
        		buffer.append(result.readToString());
        	}
        	FilePath output = new FilePath(resultOutput);
        	output.write(buffer.toString(), null);
        } catch (IOException e) {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener.fatalError("Unable to concatenate results to " + resultOutput));
            return false;
        }

        /* Read all results */
        for (FilePath result : resultFiles) {
        	try {
	            DrMemoryResult2 dresult = DrMemoryResult2.parse(new File(result.toString()));
	            String directory_name = result.getParent().getName();
	            Matcher match = directory_pattern.matcher(directory_name);
	            
	            if(match.find()) {
		            String name = match.group(1);
		            //int pid = Integer.parseInt(match.group(2));
		            dresult.setCmd(name);
		        }
	            action.addResult(dresult);
	        } catch (InvalidInputException e) {
	            out.println("Invalid input: " + e.getMessage() + ", file: " + result);
	        }
        }
        
        action.addPublisher(this);

        return true;
    }
    private List<Graph> graphs;

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new DrMemoryProjectAction(project);
    }

    public static Map<String, AbstractGraph> getGraphTypes() {
        return graphTypes;
    }

    public void setGraphs(List<Graph> graphs) {
        this.graphs = graphs;
    }

    public AbstractGraph getGraph(String type) {
        return graphTypes.get(type);
    }

    public List<Graph> getGraphs() {
        return graphs;
    }
    
    public String getLogPath() {
    	return logPath;
    }
    
    public void setLogPath(String logDir) {
    	this.logPath = logDir;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }
 
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        
        @Override
        public String getDisplayName() {
            return "Dr. Memory Report";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> arg0) {
            return true;
        }

        @Override
        public DrMemoryPublisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            DrMemoryPublisher instance = req.bindJSON(DrMemoryPublisher.class, formData);

            List<Graph> graphs = req.bindParametersToList(Graph.class, "drmemory.graph.");
            instance.setGraphs(graphs);
            save();
            return instance;
        }

        public List<Graph> getGraphs(DrMemoryPublisher instance) {
            if (instance == null) {
                return new ArrayList<Graph>();
            } else {
                return instance.getGraphs();
            }
        }

        public AbstractGraph getGraph(String type) {
            return graphTypes.get(type);
        }

        public Set<String> getGraphTypes() {
            return graphTypes.keySet();
        }
    }
}
