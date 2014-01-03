package net.praqma.jenkins.plugin.drmemory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.praqma.drmemory.DrMemoryError;
import net.praqma.drmemory.DrMemoryResult;
import net.praqma.drmemory.DrMemoryResult.ErrorSummary;
import net.praqma.drmemory.exceptions.InvalidInputException;

public class DrMemoryResult2 {
	protected DrMemoryResult result;
	protected String command;
	
	protected DrMemoryResult2() {
	}
	
	public DrMemoryResult2(DrMemoryResult result) {
		this.result = result;
		this.command = result.getCmd();
	}
	
	public String getVersion() {
		return result.getVersion();
	}
	
	public String getDate() {
		return result.getDate();
	}
	
	public String getCmd() {
		return command;
	}
	
	public void setCmd(String cmd) {
		this.command = cmd;
	}
	
	public List<String> getElements() {
		return result.getElements();
	}
	
	public Map<Integer, DrMemoryError> getErrors() {
		return result.getErrors();
	}
	
	public ErrorSummary getUnaddressableAccesses() {
		return result.getUnaddressableAccesses();
	}

	public ErrorSummary getUninitializedAccess() {
		return result.getUninitializedAccess();
	}

	public ErrorSummary getInvalidHeapArguments() {
		return result.getInvalidHeapArguments();
	}

	public ErrorSummary getWarnings() {
		return result.getWarnings();
	}

	public ErrorSummary getBytesOfLeaks() {
		return result.getBytesOfLeaks();
	}
	
	public ErrorSummary getLeakCount() {
		return result.getLeakCount();
	}

	public ErrorSummary getBytesOfPossibleLeaks() {
		return result.getBytesOfPossibleLeaks();
	}
	
	public ErrorSummary getPossibleLeakCount() {
		return result.getPossibleLeakCount();
	}

	public ErrorSummary getStillReachableAllocations() {
		return result.getStillReachableAllocations();
	}

	public List<ErrorSummary> getSummaries() {
		return result.getSummaries();
	}

	public static DrMemoryResult2 parse(File file) throws IOException, InvalidInputException {
		return new DrMemoryResult2(DrMemoryResult.parse(file));
	}
	
	public static void getErrorSummary(DrMemoryResult2 result, String summary) {
		DrMemoryResult.getErrorSummary(result.result, summary);
	}
	
	public static void getDuplicates(DrMemoryResult2 result, String duplicates) {
		DrMemoryResult.getDuplicates(result.result, duplicates);
	}
}