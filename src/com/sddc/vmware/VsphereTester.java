package com.sddc.vmware;

import java.util.Map;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;

public class VsphereTester {
	public static void test() {
		VsphereConnector vcon = new VsphereConnector();
		vcon.connect();
		vcon.printApiInfo();
		VimPortType vimPort = vcon.getVimPort();
		ServiceContent serviceContent = vcon.getServiceContent();
		VmHandler vmHandler = new VmHandler(vimPort, serviceContent);
		VmSpecInfo vmSpecInfo = new VmSpecInfo("mytestVm3", new Long(1024), 2, "windows7Guest");
		vmHandler.createVm(vmSpecInfo, "hostNetwork", "169.254.124.45", "Resources", "vm");
		vcon.disconnect();
	}
}
