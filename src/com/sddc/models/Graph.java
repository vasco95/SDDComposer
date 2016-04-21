package com.sddc.models;

import java.text.SimpleDateFormat;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.sddc.models.datacollectors.JsonGraph;

/*
 * Final Graph Class to hold all the information 
 */
@Document(collection="graphs")
public class Graph {
	@Id
	private String graphId;
	private String userName;
	private String designName;
	private String description;
	

	private Node[] nodes = new Node[0];
	private Link[] links = new Link[0];
	private VmInfo[] vms = new VmInfo[0];
	private RouterInfo[] routers = new RouterInfo[0];
	private SubnetInfo[] subnets = new SubnetInfo[0];
	
	public Graph() {
		this.designName = "blank_design";
		this.description = "empty_description";
		this.graphId = null;
	}
	
	/**
	 * @return Returns graphId for the graph
	 */
	public String getGraphId() {
		return this.graphId;
	}

	/**
	 * @param id Id for the graph
	 */
	public void setGraphId(String id) {
		this.graphId = id;
	}
	
	/**
	 * @param dname Name of the design
	 */
	public void setDesignName(String dname) {
		this.designName = dname;
	}
	
	/**
	 * @return Name of the design
	 */
	public String getDesignName() {
		return this.designName;
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
	
	/**
	 * @param graphUser creator of the graph
	 */
	public void setUserName(String graphUser) {
		this.userName = graphUser;
	}
	
	public String getUsername() {
		return this.userName;
	}
	/**
	 * Sets up the time stamp and id
	 */
	public void setTimeStamp() {
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
		this.graphId = this.userName + "_" + timeStamp;
	}
	
	/**
	 * @param graphUser creater of graph
	 * Sets up graph user as well as time stamp
	 */
	public void setTimeStamp(String graphUser) {
		this.userName = graphUser;
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
		this.graphId = this.userName + "_" + timeStamp;
	}

	/**
	 * @param nodeList Array of nodes
	 */
	public void setNodesList(Node[] nodeList) {
		this.nodes = nodeList;
	}
	
	/**
	 * @param links Array of links
	 */
	public void setLinkList(Link[] linkList) {
		this.links = linkList;
	}
	
	/**
	 * @param vmList Array of vms
	 */
	public void setVmList(VmInfo[] vmList) {
		this.vms = vmList;
	}
	
	/**
	 * @return Returns List of vms
	 */
	public VmInfo[] getVmList() {
		return this.vms;
	}
	
	/**
	 * @param routerList Array of routers
	 */
	public void setRouterList(RouterInfo[] routerList) {
		this.routers = routerList;
	}
	
	/**
	 * @return Returns list of routers
	 */
	public RouterInfo[] getRouterList() {
		return this.routers;
	}
	
	/**
	 * @param subnetList Array of subnets
	 */
	public void setSubnetList(SubnetInfo[] subnetList) {
		this.subnets = subnetList;
	}
	
	/**
	 * @return Returns subnets list
	 */
	public SubnetInfo[] getSubnetList() {
		return this.subnets;
	}
	
	/**
	 * @return Returns links list
	 */
	public Link[] getLinkList() {
		return this.links;
	}
	
	public JsonGraph getJsonGraph() {
		JsonGraph jgraph = new JsonGraph(this.designName, this.description);
		//TODO - Set jgraph.jsonGraph with appropriate value here
		return jgraph;
	}
	
	/**
	 * Print all information about the graph
	 */
	@Override
	public String toString() {
		StringBuilder retString = new StringBuilder();
		retString.append("name: " + this.designName + "\n");
		retString.append("description: " + this.description + "\n");
		
		retString.append("{id: " + this.graphId + ",");
		retString.append("user: " + this.userName + ",");
		
		retString.append("nodes:[");
		for(int j = 0; j < this.nodes.length - 1; j++) {
			retString.append(this.nodes[j].toString() + ",\n");
		}
		if(this.nodes.length == 0)
			retString.append("],\n");
		else
			retString.append(this.nodes[this.nodes.length - 1].toString() + "],\n");
		
		retString.append("links:[");
		for(int j = 0; j < this.links.length - 1; j++) {
			retString.append(this.links[j].toString() + ",\n");
		}
		if(this.links.length == 0)
			retString.append("],\n");
		else
			retString.append(this.links[this.links.length - 1] + "],\n");
		
		retString.append("vms:[");
		for(int j = 0; j < this.vms.length - 1; j++) {
			retString.append(this.vms[j].toString() + ",\n");
		}
		if(this.vms.length == 0)
			retString.append("],\n");
		else
			retString.append(this.vms[this.vms.length - 1] + "],\n");
		
		retString.append("routers:[");
		for(int j = 0; j < this.routers.length - 1; j++) {
			retString.append(this.routers[j].toString() + ",\n");
		}
		if(this.routers.length == 0)
			retString.append("],\n");
		else
			retString.append(this.routers[this.routers.length - 1] + "],\n");
		
		retString.append("subnets:[");
		for(int j = 0; j < this.subnets.length - 1; j++) {
			retString.append(this.subnets[j].toString() + ",\n");
		}
		if(this.subnets.length == 0)
			retString.append("],\n");
		else
			retString.append(this.subnets[this.subnets.length - 1] + "]}\n");
		return retString.toString();
	}
}