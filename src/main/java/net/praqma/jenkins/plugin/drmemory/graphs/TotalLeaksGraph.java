package net.praqma.jenkins.plugin.drmemory.graphs;

import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import net.praqma.jenkins.plugin.drmemory.DrMemoryResult2;

public class TotalLeaksGraph extends AbstractGraph {
	@Override
	public float[] getNumber( DrMemoryResult2 r ) {
		float values[] = new float[] {r.getLeakCount().total};
		
		return values;
	}

	@Override
	public void addX( DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb, float[] values, NumberOnlyBuildLabel label ) {
		dsb.add( values[0], "Total Leaks", label );
	}
	
	@Override
	public String getTitle() {
		return "Total Leaks";
	}

	@Override
	public String getYAxis() {
		return "Number of leaks";
	}

}
