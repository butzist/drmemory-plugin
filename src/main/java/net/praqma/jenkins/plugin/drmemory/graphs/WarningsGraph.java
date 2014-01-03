package net.praqma.jenkins.plugin.drmemory.graphs;

import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import net.praqma.jenkins.plugin.drmemory.DrMemoryResult2;

public class WarningsGraph extends AbstractGraph {
	@Override
	public float[] getNumber( DrMemoryResult2 r ) {
		float values[] = new float[] { r.getWarnings().total, r.getWarnings().unique };
		
		return values;
	}

	@Override
	public void addX( DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb, float[] values, NumberOnlyBuildLabel label ) {
		dsb.add( values[0], "Total Warnings", label );
		dsb.add( values[1], "Unique Warnings", label );
	}
	
	@Override
	public String getTitle() {
		return "Warnings";
	}

	@Override
	public String getYAxis() {
		return "Warnings";
	}

}
