package com.sddc.vmware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.AlreadyExistsFaultMsg;
import com.vmware.vim25.HostConfigFaultFaultMsg;
import com.vmware.vim25.HostConfigManager;
import com.vmware.vim25.HostNetworkPolicy;
import com.vmware.vim25.HostPortGroupSpec;
import com.vmware.vim25.HostVirtualSwitchSpec;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.NotFoundFaultMsg;
import com.vmware.vim25.ResourceInUseFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;

public class NetworkHandler {
	private MOREFSelector mSelector;
	private VimPortType vimPort;
	private ServiceContent serviceContent;
	private static Logger logger = LoggerFactory.getLogger(NetworkHandler.class);

	public NetworkHandler(VimPortType vimPort, ServiceContent serviceContent) {
		this.vimPort = vimPort;
		this.serviceContent = serviceContent;
		this.mSelector = new MOREFSelector(vimPort, serviceContent);
	}

	public void createVirtualSwitch(String switchName, String hostName) {
		ManagedObjectReference rootFolder = this.serviceContent.getRootFolder();
		//Get Hostsystem MOR
		ManagedObjectReference hostMor = this.mSelector.getMOREFByName(rootFolder, "HostSystem", hostName);
		if(hostMor == null) {
			logger.error("No host exists of name " + hostName);
			return;
		}
		HostConfigManager hostCfgMgr = (HostConfigManager) this.mSelector.getEntityProps(hostMor, new String[] {"configManager"}).get("configManager");
		ManagedObjectReference nwSystem = hostCfgMgr.getNetworkSystem();
        HostVirtualSwitchSpec spec = new HostVirtualSwitchSpec();
        spec.setNumPorts(8);
        try {
			vimPort.addVirtualSwitch(nwSystem, switchName, spec);
		} catch (AlreadyExistsFaultMsg | HostConfigFaultFaultMsg | ResourceInUseFaultMsg | RuntimeFaultFaultMsg e) {
			e.printStackTrace();
		}
	}

	public void createVirtualPortGroup(String switchName, String hostName, String portGroupName) {
		ManagedObjectReference rootFolder = this.serviceContent.getRootFolder();
		//Get Hostsystem MOR
		ManagedObjectReference hostMor = this.mSelector.getMOREFByName(rootFolder, "HostSystem", hostName);
		if(hostMor == null) {
			logger.error("No host exists of name " + hostName);
			return;
		}
		HostConfigManager hostCfgMgr = (HostConfigManager) this.mSelector.getEntityProps(hostMor, new String[] {"configManager"}).get("configManager");
		ManagedObjectReference nwSystem = hostCfgMgr.getNetworkSystem();
		HostPortGroupSpec portgrp = new HostPortGroupSpec();
		portgrp.setName(portGroupName);
		portgrp.setVswitchName(switchName);
		portgrp.setPolicy(new HostNetworkPolicy());

    	try {
			vimPort.addPortGroup(nwSystem, portgrp);
		} catch (AlreadyExistsFaultMsg e) {
			e.printStackTrace();
		} catch (HostConfigFaultFaultMsg e) {
			e.printStackTrace();
		} catch (NotFoundFaultMsg e) {
			e.printStackTrace();
		} catch (RuntimeFaultFaultMsg e) {
			e.printStackTrace();
		}
	}
}
