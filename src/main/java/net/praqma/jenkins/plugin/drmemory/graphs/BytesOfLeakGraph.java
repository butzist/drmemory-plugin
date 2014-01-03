package net.praqma.jenkins.plugin.drmemory.graphs;

import net.praqma.jenkins.plugin.drmemory.DrMemoryResult2;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

public class BytesOfLeakGraph extends AbstractGraph {
	@Override
	public float[] getNumber( DrMemoryResult2 r ) {
		float values[] = new float[] { r.getBytesOfLeaks().total, r.getBytesOfPossibleLeaks().total };
		
		return values;
	}

	@Override
	public void addX( DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb, float[] values, NumberOnlyBuildLabel label ) {
		dsb.add( values[0], "Total Leaks", label );
		dsb.add( values[1], "Possible Leaks", label );
	}
	
	@Override
	public String getTitle() {
		return "Bytes of Leaks";
	}

	@Override
	public String getYAxis() {
		return "Bytes";
	}

}
