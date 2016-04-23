/**
 * This class handles all vm related operations
 */
package com.sddc.vmware;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.AlreadyExistsFaultMsg;
import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.ConfigTarget;
import com.vmware.vim25.DuplicateNameFaultMsg;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidNameFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.OutOfBoundsFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VmConfigFaultFaultMsg;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualMachineConfigOption;

/**
 * @author vasco
 */
public class VmHandler {
	private VimPortType vimPort;
	private ServiceContent serviceContent;
	private MOREFSelector mSelector;
	private static final Logger logger = LoggerFactory.getLogger(VmHandler.class);
	
	public VmHandler(VimPortType vimPort, ServiceContent serviceContent) {
		this.vimPort = vimPort;
		this.serviceContent = serviceContent;
		this.mSelector = new MOREFSelector(vimPort, serviceContent);
	}
	
	/**
	 * Creates vm based on param info
	 * @param vmSpecInfo VmSpecObject Containing Config Info
	 * @param dcName Datacenter name
	 * @param hostIp Host on which to deploy vm
	 * @param resourcePoolName Name of the resource pool
	 * @param folderName name of the folder
	 */
	public void createVm(VmSpecInfo vmSpecInfo, String dcName, String hostIp) {
		logger.info("Creating Virtual Machine");
		ManagedObjectReference rootFolder = this.serviceContent.getRootFolder();
		//Get Datacenter MOR
		ManagedObjectReference dcMor = this.mSelector.getMOREFByName(rootFolder, "Datacenter", dcName);
		if(dcMor == null) {
			logger.error("No datacenter exists of name " + dcName);
			return;
		}
		//Get Hostsystem MOR
		ManagedObjectReference hostMor = this.mSelector.getMOREFByName(rootFolder, "HostSystem", hostIp);
		if(hostMor == null) {
			logger.error("No host exists of name " + hostIp);
			return;
		}
		//Get Compute Resources
		ManagedObjectReference crMor = (ManagedObjectReference) this.mSelector.getEntityProps(hostMor, new String[] {"parent"}).get("parent");
		if(crMor == null) {
			logger.error("No Compute resources available");
			return;
		}
		//Get resource pool mor for compute resources and get vmFolder for dcmor
		ManagedObjectReference resourcePoolMor = (ManagedObjectReference) this.mSelector.getEntityProps(crMor, new String[] {"resourcePool"}).get("resourcePool");
		if(resourcePoolMor == null) {
			logger.error("No resources pool found");
			return;
		}
		ManagedObjectReference vmFolderMor = (ManagedObjectReference) this.mSelector.getEntityProps(dcMor, new String[] {"vmFolder"}).get("vmFolder");
		if(vmFolderMor == null) {
			logger.error("No vm folder");
			return;
		}
		//Get the created vm folder for the network
		ArrayOfManagedObjectReference folderArray = (ArrayOfManagedObjectReference) this.mSelector.getEntityProps(vmFolderMor, new String[] {"childEntity"}).get("childEntity");
		List<ManagedObjectReference> folderList = folderArray.getManagedObjectReference();
		ManagedObjectReference customFolderMor = null;
		Iterator<ManagedObjectReference> i = folderList.iterator();
		while(i.hasNext()) {
			ManagedObjectReference tmpMor = i.next();
			if(tmpMor.getType().equals("Folder")) {
				String folderName = (String)this.mSelector.getEntityProps(tmpMor, new String[] {"name"}).get("name");
				if(folderName.equals("sddcNetwork")); {
					customFolderMor = tmpMor;
					break;
				}
			}
			logger.info(tmpMor.getType() + " = " + tmpMor.getValue());
		}
		if(customFolderMor == null) {
			logger.error("No vm folder");
			return;
		}
		//Now that all required MOR are avaialable we create vmconfigspec and add devices to it
		VirtualMachineConfigSpec vmConfig = vmSpecInfo.createVmConfig(this.getConfigTarget(crMor, hostMor), this.getDefaultDevices(crMor, hostMor), "datastore1");
		try {
			@SuppressWarnings("unused")
			ManagedObjectReference taskMor = this.vimPort.createVMTask(customFolderMor, vmConfig, resourcePoolMor, hostMor);
		} catch (AlreadyExistsFaultMsg | DuplicateNameFaultMsg | FileFaultFaultMsg
				| InsufficientResourcesFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidNameFaultMsg
				| InvalidStateFaultMsg | OutOfBoundsFaultMsg | RuntimeFaultFaultMsg | VmConfigFaultFaultMsg e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private ConfigTarget getConfigTarget(ManagedObjectReference crMor, ManagedObjectReference hostMor) {
		ManagedObjectReference evnBrowser = (ManagedObjectReference)this.mSelector.getEntityProps(crMor, new String[] {"environmentBrowser"}).get("environmentBrowser");
		ConfigTarget configTarget = null;
		try {
			configTarget = this.vimPort.queryConfigTarget(evnBrowser, hostMor);
			if(configTarget == null) {
				logger.debug("No config target at given compute resources");
			}
		} catch (RuntimeFaultFaultMsg e) {
			e.printStackTrace();
		}
		return configTarget;
	}
	
	private List<VirtualDevice> getDefaultDevices(ManagedObjectReference crMor, ManagedObjectReference hostMor) {
		ManagedObjectReference evnBrowser = (ManagedObjectReference)this.mSelector.getEntityProps(crMor, new String[] {"environmentBrowser"}).get("environmentBrowser");
		VirtualMachineConfigOption configOps;
		List<VirtualDevice> retList = null;
		try {
			configOps = this.vimPort.queryConfigOption(evnBrowser, null, hostMor);
			if(configOps == null) {
				return null;
			}
			else {
	            List<VirtualDevice> lvds = configOps.getDefaultDevice();
	            if (lvds == null) {
	                logger.debug("No Datastore found in ComputeResource");
	            } else {
	                retList = lvds;
	            }
			}
		} catch (RuntimeFaultFaultMsg e) {
			logger.debug("Cannot find Virtual Device Info");
		}
		return retList;
	}
}