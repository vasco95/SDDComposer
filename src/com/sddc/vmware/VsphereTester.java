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
		MOREFSelector mSelector = new MOREFSelector(vimPort, serviceContent);
		ManagedObjectReference dc = mSelector.getMOREFByName(serviceContent.getRootFolder(), "Datacenter", "hostNetwork");
		Map<String, Object> ret = mSelector.getEntityProps(dc, new String[] {"parent"});
		if(ret.isEmpty()) {
			System.out.println("Empty");
		}
		else {
			System.out.println("NE ");
			ManagedObjectReference parent = (ManagedObjectReference)ret.get("parent");
			System.out.println(parent.getValue());
		}
		vcon.disconnect();
	}
}
