package org.example;

public class State {
	String fileName;
	Integer depth;

	public State(String fileName, Integer depth) {
		this.fileName = fileName;
		this.depth = depth;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Integer getDepth() {
		return depth;
	}

	public void setDepth(Integer depth) {
		this.depth = depth;
	}

	public Integer compare(State targetState) {
		
	}
}
