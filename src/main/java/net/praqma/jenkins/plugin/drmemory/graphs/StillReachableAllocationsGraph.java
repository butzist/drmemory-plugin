package net.praqma.jenkins.plugin.drmemory.graphs;

import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import net.praqma.jenkins.plugin.drmemory.DrMemoryResult2;

public class StillReachableAllocationsGraph extends AbstractGraph {

	@Override
	public float[] getNumber( DrMemoryResult2 r ) {
		float values[] = new float[] { r.getStillReachableAllocations().total };
		
		return values;
	}

	@Override
	public void addX( DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb, float[] values, NumberOnlyBuildLabel label ) {
		dsb.add( values[0], "Allocations", label );
	}
	@Override
	public String getTitle() {
		return "Still Reachable Allocations";
	}

	@Override
	public String getYAxis() {
		return "Allocations";
	}

}
