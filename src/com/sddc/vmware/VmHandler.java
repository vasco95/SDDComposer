/**
 * This class handles all vm related operations
 */
package com.sddc.vmware;

import com.vmware.vim25.AlreadyExistsFaultMsg;
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

/**
 * @author vasco
 */
public class VmHandler {
	private VimPortType vimPort;
	private ServiceContent serviceContent;
	private MOREFSelector mSelector;
	
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
	public void realizeVm(VmSpecInfo vmSpecInfo, String dcName, String hostIp, String resourcePoolName, String folderName) {
		ManagedObjectReference rootFolder = this.serviceContent.getRootFolder();
		//get Datacenter MOR
		ManagedObjectReference dataCenter = this.mSelector.getMOREFByName(rootFolder, "Datacenter", dcName);
		if(null == dataCenter) {
			System.out.println("No such Datcenter");
			return;
		}
		//get host MOR
		ManagedObjectReference hostSystem = this.mSelector.getMOREFByName(rootFolder, "HostSystem", hostIp);
		if(null == hostSystem) {
			System.out.println("No such host");
			return;
		}
		//get resourcePool MOR
		ManagedObjectReference resourcePool = this.mSelector.getMOREFByName(rootFolder, "ResourcePool", resourcePoolName);
		if(null == resourcePool) {
			System.out.println("No such Resource Pool");
			return;
		}
		//get folderMor
		ManagedObjectReference folder = this.mSelector.getMOREFByName(rootFolder, "Folder", folderName);
		if(null == folder) {
			System.out.println("No such Folder");
			return;
		}
		VirtualMachineConfigSpec vmSpecs = vmSpecInfo.createVmConfig();
		try {
			this.vimPort.createVMTask(folder, vmSpecs, resourcePool, hostSystem);
		} catch (AlreadyExistsFaultMsg | DuplicateNameFaultMsg | FileFaultFaultMsg | InsufficientResourcesFaultFaultMsg
				| InvalidDatastoreFaultMsg | InvalidNameFaultMsg | InvalidStateFaultMsg | OutOfBoundsFaultMsg
				| RuntimeFaultFaultMsg | VmConfigFaultFaultMsg e) {
			System.out.println("Error");
			e.printStackTrace();
		}
	}
}