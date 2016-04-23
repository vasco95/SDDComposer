/*
 * This class converts the graph elements to vmware elements
 */
package com.sddc.models;

import com.sddc.vmware.VmSpecInfo;

class Conversions {
	/**
	 * @return Returns VmSpecInfo Object for vm
	 */
	public static VmSpecInfo convertToVmSpecInfo(VmInfo vmInfo) {
		System.out.println(vmInfo.getVmOsType());
		VmSpecInfo vmSpec = new VmSpecInfo(vmInfo.getName(), vmInfo.getRam(), vmInfo.getCpuCores(), vmInfo.getVmOsType(), vmInfo.getStorage());
		return vmSpec;
	}
}
