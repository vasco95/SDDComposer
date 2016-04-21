package com.sddc.vmware;

import javax.xml.parsers.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;

class VcenterLoginCredentials {
	private String serverip;
	private String username;
	private String password;
	
	public VcenterLoginCredentials() { }
	
	public VcenterLoginCredentials(String filePath) {
		try {
			File inputFile =  new File(filePath);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(inputFile);
			Element root = doc.getDocumentElement();
			NodeList childList = root.getChildNodes();
			for(int i = 0; i < childList.getLength(); i++) {
				Node node = childList.item(i);
				if(node.getNodeName().equals("vcenter")) {
					Element element = (Element)node;
					this.serverip = element.getElementsByTagName("serverip").item(0).getTextContent();
					this.username = element.getElementsByTagName("username").item(0).getTextContent();
					this.password =  element.getElementsByTagName("password").item(0).getTextContent();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void printCredentials() {
		System.out.println(this.serverip);
		System.out.println(this.username);
		System.out.println(this.password);
	}
	
	public String getServerIp() {
		return this.serverip;
	}
	public String getUserName() {
		return this.username;
	}
	public String getPassWord() {
		return this.password;
	}
}
