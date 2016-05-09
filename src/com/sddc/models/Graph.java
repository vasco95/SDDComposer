package com.sddc.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

/*
 * Final Graph Class to hold all the information 
 */
@Document(collection="graphs")
public class Graph extends GraphTemplate{
	/**
	 * In this method we replace the ids of the graph with minimum ids
	 */
	public void normalize() {
		int[] vmnum = new int[this.vms.length], routernum = new int[this.routers.length], subnetnum = new int[this.subnets.length];
		int vc = 0, rc = 0, sc = 0;
		Map<Integer, Integer> indexHashMap = new HashMap<Integer, Integer>();
		for(int i = 0; i < this.nodes.length; i++) {
			if(this.nodes[i].checkNodeType(Node.NodeType.VM)) {
				vmnum[vc++] = i;
			}
			else if(this.nodes[i].checkNodeType(Node.NodeType.ROUTER)) {
				routernum[rc++] = i;
			}
			else if(this.nodes[i].checkNodeType(Node.NodeType.SUBNET)) {
				subnetnum[sc++] = i;
			}
			indexHashMap.put(this.nodes[i].id, i);
			this.nodes[i].id = i;
		}
		//modifying vms, routers and subnets arrays based on hashmap
		for(int i = 0; i < this.vms.length; i++) {
			this.vms[i].id = vmnum[i];
		}
		for(int i = 0; i < this.routers.length; i++) {
			this.routers[i].id = routernum[i];
		}
		for(int i = 0; i < this.subnets.length; i++) {
			this.subnets[i].id = subnetnum[i];
		}
		
		//Modifying the links array based on hashmap
		for(int i = 0; i < this.links.length; i++) {
			this.links[i].fromId = indexHashMap.get(this.links[i].fromId);
			this.links[i].toId = indexHashMap.get(this.links[i].toId);
		}
	}
	private Object[] getLinkedNodes(int id) {
		List<Object> retList = new ArrayList<Object>();
		for(int i = 0; i < this.links.length; i++) {
			Link link = this.links[i];
			int destId = -1;
			if(link.fromId == id) {
				destId = link.toId;
			}
			else if(link.toId == id) {
				destId = link.fromId;
			}
			if(destId != -1) {
				Node tmpNode = this.nodes[destId];
				if(tmpNode.checkNodeType(Node.NodeType.VM)) {
					for(int j = 0; j < this.vms.length; j++) {
						if(this.vms[j].id == destId) {
							retList.add(this.vms[j]);
						}
					}
				}
				else if(tmpNode.checkNodeType(Node.NodeType.ROUTER)) {
					for(int j = 0; j < this.routers.length; j++) {
						if(this.routers[j].id == destId) {
							retList.add(this.routers[j]);
						}
					}
				}
				else {
					for(int j = 0; j < this.subnets.length; j++) {
						if(this.subnets[j].id == destId) {
							retList.add(this.subnets[j]);
						}
					}
				}
			}
		}
		Object[] retListArray = new Object[retList.size()]; 
		retList.toArray(retListArray);
		return retListArray;
	}
	
	public VmInfo[] getLinkedVms(SubnetInfo subnet) {
		Object[] retList = this.getLinkedNodes(subnet.id);
		List<VmInfo> vmList = new ArrayList<VmInfo>();
		for(int i = 0; i < retList.length; i++) {
			if(retList[i].getClass() == VmInfo.class) {
				vmList.add((VmInfo)retList[i]);
			}
		}
		VmInfo[] retVmInfo = new VmInfo[vmList.size()];
		vmList.toArray(retVmInfo);
		return retVmInfo;
	}
//	public VmInfo[] getLinkedVms(SubnetInfo subnet) {
//		int id = subnet.id;
//		List<VmInfo> retList = new ArrayList<VmInfo>();
//		for(int i = 0; i < this.links.length; i++) {
//			Link link = this.links[i];
//			int destId = -1;
//			if(link.fromId == id) {
//				destId = link.toId;
//			}
//			else if(link.toId == id) {
//				destId = link.fromId;
//			}
//			if(destId != -1) {
//				Node tmpNode = this.nodes[destId];
//				if(tmpNode.checkNodeType(Node.NodeType.VM)) {
//					for(int j = 0; j < this.vms.length; j++) {
//						if(this.vms[j].id == destId) {
//							retList.add(this.vms[j]);
//						}
//					}
//				}
//			}
//		}
//		return (VmInfo[]) retList.toArray();
//	}
}