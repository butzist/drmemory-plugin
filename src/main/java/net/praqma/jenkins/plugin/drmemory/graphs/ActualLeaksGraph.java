package net.praqma.jenkins.plugin.drmemory.graphs;

import net.praqma.jenkins.plugin.drmemory.DrMemoryResult2;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

public class ActualLeaksGraph extends AbstractGraph {

	@Override
	public float[] getNumber( DrMemoryResult2 r ) {
		float values[] = new float[] { r.getLeakCount().total, r.getLeakCount().unique };
		return values;
	}
	
	@Override
	public void addX( DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb, float[] values, NumberOnlyBuildLabel label ) {
		dsb.add( values[0], "Total Leaks", label );
		dsb.add( values[1], "Unique Leaks", label );
	}

	@Override
	public String getTitle() {
		return "Actual Leaks";
	}

	@Override
	public String getYAxis() {
		return "Number of leaks";
	}

}
