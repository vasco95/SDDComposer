package com.sddc.models;

import com.google.gson.annotations.Expose;

/*
 * Class that represents basic node in network graph
 */
class Node {
	public enum NodeType {
		VM, ROUTER, SUBNET
	}
	
	protected int id;
	private String type;
	protected String name;
	
	@Expose(serialize = false)
	private NodeType nodeType; 
	
	public Node() {}
	
	/**
	 * @return Returns id of this node
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * @return Name of node
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * @param inputNodeType type of the node
	 */
	public void setNodeType(NodeType inputNodeType) {
		this.nodeType = inputNodeType;
	}
	
	/**
	 * Sets the node type according to input
	 * @return nodeType Set
	 */
	public NodeType setNodeType() {
		if(this.type.equals("vm")) {
			this.nodeType = NodeType.VM;
		}
		else if(this.type.equals("router")) {
			this.nodeType = NodeType.ROUTER;
		}
		else {
			this.nodeType = NodeType.SUBNET;
		}
		return this.nodeType;
	}
	/**
	 * @param typeCheck
	 * @return true if typeCheck matches nodeType, false otherwise
	 */
	public boolean checkNodeType(NodeType typeCheck) {
		return (typeCheck == this.nodeType);
	}

	/**
	 * @return string representation of the node
	 */
	@Override
	public String toString() {
		return new String("{id: " + this.id + ", type: " + this.type + ", name: " + this.name + "}");
	}
}

/*
 * Class that represents vm in network graph
 */
class VmInfo extends Node {
	private int cpuCores;
	private int	ram;
	private int storage;
	private String ostype;
	/**
	 * @return RAM of vm
	 */
	public Long getRam() {
		return new Long(this.ram);
	}
	/**
	 * @return Cores of CPU
	 */
	public int getCpuCores() {
		return this.cpuCores;
	}
	/**
	 * @return Storage Space
	 */
	public int getStorage() {
		return this.storage;
	}
	/**
	 * @return Os type of vm
	 */
	public String getVmOsType() {
		return this.ostype;
	}
	/**
	 * @return string representation of the vm info
	 */
	@Override
	public String toString() {
		return new String("{id: " + this.id + ", name: " + this.name + ", cpuCores: " + this.cpuCores + ", ram: " +  this.ram + " , storage: " + this.storage + "}");
	}
}

/*
 * Class that represents a router in network
 */
class RouterInfo extends Node {
}

/*
 * Class that represents a subnet in network
 */
class SubnetInfo extends Node {
}

/*
 * Class that represents link in network graph
 */
class Link {
	private int fromId;
	private int toId;
	
	public Link() {}
	
	/**
	 * @return string representation of the link
	 */
	@Override
	public String toString() {
		return new String("{fromId: " + this.fromId + ", toId: " + this.toId + "}");
	}
}