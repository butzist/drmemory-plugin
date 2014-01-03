package net.praqma.jenkins.plugin.drmemory.graphs;

import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import net.praqma.jenkins.plugin.drmemory.DrMemoryResult2;

public class UninitializedAccessesGraph extends AbstractGraph {

	@Override
	public float[] getNumber( DrMemoryResult2 r ) {
		float values[] = new float[] { r.getUninitializedAccess().total, r.getUninitializedAccess().unique };
		
		return values;
	}

	@Override
	public void addX( DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb, float[] values, NumberOnlyBuildLabel label ) {
		dsb.add( values[0], "Total Uninitialized accesses", label );
		dsb.add( values[1], "Unique Uninitialized accesses", label );
	}
	
	@Override
	public String getTitle() {
		return "Uninitialized accesses";
	}

	@Override
	public String getYAxis() {
		return "Accesses";
	}

}
