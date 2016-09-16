/**
 * This is main interface creation of network on VMware Virtual Infrastructure
 */
package com.sddc.models;

import com.sddc.vmware.NetworkHandler;
import com.sddc.vmware.VmHandler;
import com.sddc.vmware.VmSpecInfo;
import com.sddc.vmware.VsphereConnector;
/**
 * @author vasco
 * Objectives
 * 1. Perform login to vcenter
 * 		-read credentials from xml file
 * 2. Create VMs based on graph nodes
 */
public class NetworkRealizer {
	private Graph graph;
	private NetworkHandler networkHandler;
	private VmHandler vmCreator;
	private VsphereConnector vsphereConnector;
	public NetworkRealizer(Graph graph) {
		this.graph = graph;
		init();
	}
	/**
	 * Intialize connection with vcenter server
	 */
	private void init() {
		this.vsphereConnector = new VsphereConnector();
		this.vsphereConnector.connect();
		this.networkHandler = new NetworkHandler(this.vsphereConnector.getVimPort(), this.vsphereConnector.getServiceContent());
		this.vmCreator = new VmHandler(this.vsphereConnector.getVimPort(), this.vsphereConnector.getServiceContent());
		this.vsphereConnector.printApiInfo();
	}
	
	/**
	 * Creates subnet in form of port group
	 * @param subnets
	 */
	public void createSubnet(String subnetName) {
		String pgname = this.graph.getUsername() + "_" + this.graph.getDesignName() + "_" + subnetName;
		this.networkHandler.createVirtualPortGroup("sddcSwitch", "169.254.124.45", pgname);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates vm specified by vmlist
	 * @param vmList List of type VmInfo
	 */
	private void realizeVms(final VmInfo[] vmList , String subnetName) {
		String pgname = this.graph.getUsername() + "_" + this.graph.getDesignName() + "_" + subnetName;
		String folderName = this.graph.getUsername() + "_" + this.graph.getDesignName();
		for(int i = 0; i < vmList.length; i++) {
			VmSpecInfo vmSpecInfo = Conversions.convertToVmSpecInfo(vmList[i]);
			vmSpecInfo.setPortGroup(pgname);
			this.vmCreator.createVm(vmSpecInfo, "hostNetwork", "169.254.124.45", folderName);
		}
	}
	
	public void createNetwork() {
		if(false == this.vsphereConnector.isConnected()) {
			this.init();
		}
		// 1.identify a subnet
		// 2.create a subnet (port group)
		// 3.create connected vms
		// 4.connect all vms to portgroup
		SubnetInfo[] subnets = this.graph.getSubnetList();
		for(int i = 0; i < subnets.length; i++) {
			this.createSubnet(subnets[i].getName());
			VmInfo[] vms = (VmInfo[])this.graph.getLinkedVms(subnets[i]);
			this.realizeVms(vms, subnets[i].getName());
		}
	}
}
