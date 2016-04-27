/**
 * This is main interface creation of network on VMware Virtual Infrastructure
 */
package com.sddc.models;

import com.sddc.vmware.VmHandler;
import com.sddc.vmware.VmSpecInfo;
import com.sddc.vmware.VsphereConnector;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;
/**
 * @author vasco
 * Objectives
 * 1. Perform login to vcenter
 * 		-read credentials from xml file
 * 2. Create VMs based on graph nodes
 */
public class NetworkRealizer {
	private Graph graph;
	
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
		this.vsphereConnector.printApiInfo();
	}
	
	/**
	 * Creates vm specified by vmlist
	 * @param vmList List of type VmInfo
	 */
	private void realizeVms(final VmInfo[] vmList) {
		VimPortType vimPort = this.vsphereConnector.getVimPort();
		ServiceContent serviceContent = this.vsphereConnector.getServiceContent();
		VmHandler vmCreator = new VmHandler(vimPort, serviceContent);
		String folderName = this.graph.getUsername() + "_" + this.graph.getDesignName();
		for(int i = 0; i < vmList.length; i++) {
			VmSpecInfo vmSpecInfo = Conversions.convertToVmSpecInfo(vmList[i]);
			vmCreator.createVm(vmSpecInfo, "hostNetwork", "169.254.124.45", folderName);
		}
	}
	
	public void createNetwork() {
		if(false == this.vsphereConnector.isConnected()) {
			this.init();
		}
		this.realizeVms(this.graph.getVmList());
	}
}
