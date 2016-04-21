package com.sddc.vmware;

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
		VmSpecInfo vmSpecInfo = new VmSpecInfo("freeBuntu", new Long(1024), 2, "windows7Guest", 20);
		vmHandler.createVm(vmSpecInfo, "hostNetwork", "169.254.124.45", "Resources", "vm");
		vcon.disconnect();
	}
}
