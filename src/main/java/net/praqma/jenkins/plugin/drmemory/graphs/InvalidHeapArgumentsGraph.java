package net.praqma.jenkins.plugin.drmemory.graphs;

import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import net.praqma.jenkins.plugin.drmemory.DrMemoryResult2;

public class InvalidHeapArgumentsGraph extends AbstractGraph {

	@Override
	public float[] getNumber( DrMemoryResult2 r ) {
		float values[] = new float[] { r.getInvalidHeapArguments().total, r.getInvalidHeapArguments().unique };
		
		return values;
	}

	@Override
	public void addX( DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb, float[] values, NumberOnlyBuildLabel label ) {
		dsb.add( values[0], "Total Invalid Heap Arguments", label );
		dsb.add( values[1], "Unique Invalid Heap Arguments", label );
	}

	@Override
	public String getTitle() {
		return "Invalid Heap Arguments";
	}

	@Override
	public String getYAxis() {
		return "Arguments";
	}

}
