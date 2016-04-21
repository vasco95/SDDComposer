package com.sddc.vmware;

import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineFileInfo;

public class VmSpecInfo {
	private String vmName;
	private Long ram;
	private int cpuCores;
	private String vmOsType = "windows7Guest";
	
	public VmSpecInfo (String vmName, Long ram, int cpuCores, String vmOsType) {
		this.vmName = vmName;
		this.ram = ram;
		this.cpuCores = cpuCores;
		this.vmOsType = vmOsType;
	}
	
	public void setVmName(String vmName) {
		this.vmName = vmName;
	}
	
	public void setRam(Long ram) {
		this.ram = ram;
	}
	
	public void setCpuCores(int cpuCores) {
		this.cpuCores = cpuCores;
	}
	
	public void setVmOsType(String vmOsType) {
		this.vmOsType = vmOsType;
	}
	
	public VirtualMachineConfigSpec createVmConfig() {
		VirtualMachineConfigSpec vmSpecs = new VirtualMachineConfigSpec();
		vmSpecs.setName(this.vmName);
		vmSpecs.setAnnotation("Created by SDDComposer");
		vmSpecs.setMemoryMB(this.ram);
		vmSpecs.setNumCPUs(this.cpuCores);
		vmSpecs.setGuestId(this.vmOsType);
		VirtualMachineFileInfo vfileSpecs = new VirtualMachineFileInfo();
		vfileSpecs.setVmPathName("[datastore1]");
		vmSpecs.setFiles(vfileSpecs);
		return vmSpecs;
	}
}
