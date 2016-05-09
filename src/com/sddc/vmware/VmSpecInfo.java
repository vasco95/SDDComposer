package com.sddc.vmware;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.ConfigTarget;
import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.NetworkSummary;
import com.vmware.vim25.VirtualCdrom;
import com.vmware.vim25.VirtualCdromIsoBackingInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineDatastoreInfo;
import com.vmware.vim25.VirtualMachineFileInfo;
import com.vmware.vim25.VirtualMachineNetworkInfo;
import com.vmware.vim25.VirtualPCNet32;
import com.vmware.vim25.VirtualSCSISharing;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecFileOperation;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualE1000;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualIDEController;
import com.vmware.vim25.VirtualLsiLogicController;

public class VmSpecInfo {
	private String vmName;
	private Long ram;
	private int cpuCores;
	private String vmOsType = "windows7Guest";
	private int storageInGB;
	private String portgroup = "default";
	private static Logger logger = LoggerFactory.getLogger(VmSpecInfo.class);
	
	public VmSpecInfo (String vmName, Long ram, int cpuCores, String vmOsType, int storage) {
		this.vmName = vmName;
		this.ram = ram;
		this.cpuCores = cpuCores;
		this.vmOsType = vmOsType;
		this.storageInGB = storage;
	}
	
	/**
	 * Sets portgroup name for the vm
	 * @param pgname Name of the port group
	 */
	public void setPortGroup(String pgname) {
		this.portgroup = pgname;
	}

	public VirtualMachineConfigSpec createVmConfig(ConfigTarget configTarget, List<VirtualDevice> deviceList, String datastoreName) throws RuntimeException{
		VirtualMachineConfigSpec vmConfig = new VirtualMachineConfigSpec();
		//We add network info to vm here
		String networkName = null;
		if(configTarget.getNetwork() != null) {
			for(int i = 0; i < configTarget.getNetwork().size(); i++) {
				VirtualMachineNetworkInfo netInfo = configTarget.getNetwork().get(i);
				NetworkSummary netSum = netInfo.getNetwork();
				if(netSum.isAccessible()) {
					networkName = netSum.getName();
					logger.info(networkName);
					if(this.portgroup.equals(netSum.getName())){
						break;
					}
				}
			}
		}
		if(this.portgroup.equals(networkName) == false) {
			logger.error("Port Group does not exist");
		}
		networkName = this.portgroup; 
		//We add datastore info to vm here
		ManagedObjectReference dsMor = null;
		String dsName = null;
		boolean dsFlag = false;
		if(datastoreName == null) {	//We select any accessible datastore
			for(int i = 0; i < configTarget.getDatastore().size(); i++) {
				VirtualMachineDatastoreInfo vdsInfo = configTarget.getDatastore().get(i);
				DatastoreSummary dsSum = vdsInfo.getDatastore();
				if(dsSum.isAccessible()) {
					dsFlag = true;
					dsMor = dsSum.getDatastore();
					dsName = dsSum.getName();
					break;
				}
			}
		}
		else {	//we find specified datastore
			for(int i = 0; i < configTarget.getDatastore().size(); i++) {
				VirtualMachineDatastoreInfo vdsInfo = configTarget.getDatastore().get(i);
				DatastoreSummary dsSum = vdsInfo.getDatastore();
				if(dsSum.getName().equals(datastoreName)) {
					if(dsSum.isAccessible()) {
						dsFlag = true;
						dsMor = dsSum.getDatastore();
						dsName = dsSum.getName();
					}
					else {
						throw new RuntimeException("Datastore is not accessible");
					}
				}
			}
		}
		if(dsFlag == false) {
			throw new RuntimeException("Datastore not avaialable");
		}
		//Adding datastore info
		String volName = "[" + dsName + "]";
		VirtualMachineFileInfo vmFile = new VirtualMachineFileInfo();
		vmFile.setVmPathName(volName);
		vmConfig.setFiles(vmFile);
		//Adding SCSI Controller
        int diskCtlrKey = 1;
        VirtualDeviceConfigSpec scsiSpec = new VirtualDeviceConfigSpec();
        scsiSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
        VirtualLsiLogicController scsiCtrl = new VirtualLsiLogicController();
        scsiCtrl.setBusNumber(0);
        scsiSpec.setDevice(scsiCtrl);
        scsiCtrl.setKey(diskCtlrKey);
        scsiCtrl.setSharedBus(VirtualSCSISharing.NO_SHARING);
        String ctlrType = scsiCtrl.getClass().getName();
        ctlrType = ctlrType.substring(ctlrType.lastIndexOf(".") + 1);
        //Get Ide Controller
        VirtualDevice ideCtlr = null;
        for (int di = 0; di < deviceList.size(); di++) {
            if (deviceList.get(di) instanceof VirtualIDEController) {
                ideCtlr = deviceList.get(di);
                break;
            }
        }
        //Adding cdrom 
        VirtualDeviceConfigSpec cdSpec = null;
        if (ideCtlr != null) {
            cdSpec = new VirtualDeviceConfigSpec();
            cdSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
            VirtualCdrom cdrom = new VirtualCdrom();
            VirtualCdromIsoBackingInfo cdDeviceBacking = new VirtualCdromIsoBackingInfo();
            cdDeviceBacking.setDatastore(dsMor);
            cdDeviceBacking.setFileName(volName + "testcd.iso");
            cdrom.setBacking(cdDeviceBacking);
            cdrom.setKey(20);
            cdrom.setControllerKey(new Integer(ideCtlr.getKey()));
            cdrom.setUnitNumber(new Integer(0));
            cdSpec.setDevice(cdrom);
        }
        //Creating disk for the vm
        VirtualDeviceConfigSpec diskSpec = new VirtualDeviceConfigSpec();
        diskSpec.setFileOperation(VirtualDeviceConfigSpecFileOperation.CREATE);
        diskSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
        VirtualDisk virtualDisk = new VirtualDisk();
        VirtualDiskFlatVer2BackingInfo diskFileBacking = new VirtualDiskFlatVer2BackingInfo();
        diskFileBacking.setFileName(volName);
        diskFileBacking.setDiskMode("persistent");
        virtualDisk.setKey(new Integer(0));
        virtualDisk.setControllerKey(new Integer(diskCtlrKey));
        virtualDisk.setUnitNumber(new Integer(0));
        virtualDisk.setBacking(diskFileBacking);
        virtualDisk.setCapacityInKB(this.gbToKb(this.storageInGB));
        diskSpec.setDevice(virtualDisk);
        //Adding a virtual nic
        VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
        if (networkName != null) {
            nicSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
            VirtualEthernetCard nic;
            if(this.vmOsType.charAt(0) == 'w') {
            	nic = new VirtualE1000(); //VirtualE1000 for windows
            }
            else {
            	nic = new VirtualPCNet32(); //VirtualPCNet32 for linux
            }	
            VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();
            nicBacking.setDeviceName(networkName);
            nic.setAddressType("generated");
            nic.setBacking(nicBacking);
            nic.setKey(4);
            nicSpec.setDevice(nic);
        }
        //Adding all devices to vm
        List<VirtualDeviceConfigSpec> deviceConfigSpec = new ArrayList<VirtualDeviceConfigSpec>();
        deviceConfigSpec.add(scsiSpec);
        deviceConfigSpec.add(diskSpec);
        if (ideCtlr != null) {
            deviceConfigSpec.add(cdSpec);
            deviceConfigSpec.add(nicSpec);
        } else {
            deviceConfigSpec = new ArrayList<VirtualDeviceConfigSpec>();
            deviceConfigSpec.add(nicSpec);
        }
        vmConfig.getDeviceChange().addAll(deviceConfigSpec);
        //adding other info
        vmConfig.setName(this.vmName);
        vmConfig.setAnnotation("Created by SDDComposer");
        vmConfig.setMemoryMB(new Long(this.ram));
        vmConfig.setNumCPUs(this.cpuCores);
        vmConfig.setGuestId(this.vmOsType);
        return vmConfig;
	}
	
	private Long gbToKb(int gb) {
		return new Long(gb * 1024 * 1024);
	}
	
	public String getVmName() {
		return this.vmName;
	}
}
