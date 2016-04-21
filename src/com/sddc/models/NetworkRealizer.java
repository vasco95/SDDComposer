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
	private String userName = null;
	private String passWord = null;
	private String serverIp = null;
	
	private VsphereConnector vsphereConnector;
	public NetworkRealizer(Graph graph) {
		this.graph = graph;
	}
	/**
	 * Intialize connection with vcenter server
	 */
	private void init() {
		if(this.serverIp == null || this.userName == null || this.passWord == null) {
			this.vsphereConnector = new VsphereConnector();
		}
		else {
			this.vsphereConnector = new VsphereConnector(this.serverIp, this.userName, this.passWord);
		}
		this.vsphereConnector.connect();
		this.vsphereConnector.printApiInfo();
	}
	
	public void getLoginCredentials(String ip, String username, String password) {
		this.serverIp = ip;
		this.userName = username;
		this.passWord = password;
	}
	
	/**
	 * Creates vm specified by vmlist
	 * @param vmList List of type VmInfo
	 */
	private void realizeVms(final VmInfo[] vmList) {
		VimPortType vimPort = this.vsphereConnector.getVimPort();
		ServiceContent serviceContent = this.vsphereConnector.getServiceContent();
		VmHandler vmCreator = new VmHandler(vimPort, serviceContent);
		Conversions converter = new Conversions();
		for(int i = 0; i < 1; i++) {
			VmSpecInfo vmSpecInfo = converter.convertToVmSpecInfo(vmList[i]);
			vmCreator.realizeVm(vmSpecInfo, "hostNetwork", "169.254.145.22", "Resources", "vm");
		}
	}
	
	public void createNetwork() {
		if(false == this.vsphereConnector.isConnected()) {
			this.init();
		}
		this.realizeVms(this.graph.getVmList());
	}
}
