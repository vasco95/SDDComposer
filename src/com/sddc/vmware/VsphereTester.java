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
//		VmHandler vmHandler = new VmHandler(vimPort, serviceContent);
//		VmSpecInfo vmSpecInfo = new VmSpecInfo("freeBuntu", new Long(1024), 2, "ubuntuGuest", 20);
//		vmSpecInfo.setPortGroup("VM Network");
//		vmHandler.createVm(vmSpecInfo, "hostNetwork", "169.254.124.45", "vasco_TN2");
//		NetworkHandler networkHandler = new NetworkHandler(vimPort, serviceContent);
//		networkHandler.createVirtualSwitch("sddcSwitch", "169.254.124.45");
//		networkHandler.createVirtualPortGroup("sddcSwitch", "169.254.124.45", "testpg");
		RouterHandler routerHandler = new RouterHandler(vimPort, serviceContent);
		routerHandler.createVappRouter("routerTwo", "hostNetwork", "169.254.124.45", "dataStore1", "vasco_TN2", "router/halon-sr-amd64.ovf");
		vcon.disconnect();
	}
}
