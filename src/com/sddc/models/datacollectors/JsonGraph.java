package com.sddc.models.datacollectors;

/*
 * Final class used to hold user designs and used to perform realization operations
 */
public class JsonGraph {
	// Hold json from the front end
	private String jsonGraph;
	private String name;
	private String description;
	
	public JsonGraph() {
		this.name = "blank_design";
		this.description = "empty_description";
		this.jsonGraph = "{}";
	}
	
	public JsonGraph(String json) {
		this.jsonGraph = json;
	}
	
	/**
	 * @param dname Name of the design
	 * @param desc Description for the design
	 */
	public JsonGraph(String dname, String desc) {
		this.name = dname;
		this.description = desc;
	}
	
	/**
	 * @return Returns json string for graph
	 */
	public String getJsonGraph() {
		return this.jsonGraph;
	}
	/**
	 * @param json Sets up json string of graph
	 */
	public void setJsonGraph(String json) {
		this.jsonGraph = json;
	}
	/**
	 * @param designName Name of the design
	 */
	public void setName(String designName) {
		this.name = designName;
	}
	
	/**
	 * @return Name of the design
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * @param desc Description for new design
	 */
	public void setDescription(String desc) {
		this.description = desc;
	}
	
	/** 
	 * @return Description of the design
	 */
	public String getDescription() {
		return this.description;
	}
}