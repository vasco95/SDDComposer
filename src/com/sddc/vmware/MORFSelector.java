package com.sddc.vmware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VimPortType;

/**
 * @author vasco
 * Service to get appropriate Managed Object References
 */
class MOREFSelector {
	private ServiceContent serviceContent;
	private VimPortType vimPort;
	private ManagedObjectReference viewMgrRef;
	private ManagedObjectReference propColl;
	
	/**
	 * @param serviceC Service Content
	 * @param vPort VimPortObject
	 */
	public MOREFSelector(VimPortType vPort, ServiceContent serviceC) {
		this.serviceContent = serviceC;
		this.vimPort = vPort;
		
		//Get View Manager and PropertyCollector from Service Content
		this.viewMgrRef = this.serviceContent.getViewManager();
		this.propColl = this.serviceContent.getPropertyCollector();
	}
	
	/**
	 * Returns Map of requried types from provided rootfolder
	 * @param rootFolder Root Folder from where to start looking
	 * @param morefType Type of the object to find
	 * @return Map of String and MOR
	 */
	public Map<String,ManagedObjectReference> getMORF(ManagedObjectReference rootFolder, String morefType) {
		Map<String,ManagedObjectReference> ret = new HashMap<String, ManagedObjectReference>();

		//Make Container view from root folder
		List<String>list = new ArrayList<String>();
		list.add(morefType);
		
		try {
			ManagedObjectReference cViewRef = this.vimPort.createContainerView(viewMgrRef, rootFolder, list, true);
			
			//Define Object Spec to declare starting point for navigation
			ObjectSpec oSpec = new ObjectSpec();
			oSpec.setObj(cViewRef);
			oSpec.setSkip(true);
			
			//Define Traversal Specification for object selection and path selection
			TraversalSpec tSpec = new TraversalSpec();
			tSpec.setName("traverseEntities");
			tSpec.setPath("view");
			tSpec.setSkip(false);
			tSpec.setType("ContainerView");
			
			//Adding to object Specs
			oSpec.getSelectSet().add(tSpec);
			
			//Define PropertySpec to select relevant results
			PropertySpec pSpec = new PropertySpec();
			pSpec.setType(morefType);
			pSpec.getPathSet().add("name");
			
			//Define filter and add pSpec to it
			PropertyFilterSpec fSpec = new PropertyFilterSpec();
			fSpec.getObjectSet().add(oSpec);
			fSpec.getPropSet().add(pSpec);
			
			//Create a list of filters
			List<PropertyFilterSpec> fSpecList = new ArrayList<PropertyFilterSpec>();
			fSpecList.add(fSpec);
			
			//Fetch data from server
			RetrieveOptions ro = new RetrieveOptions();
			RetrieveResult results = this.vimPort.retrievePropertiesEx(propColl, fSpecList, ro);
			
			while(results != null && !results.getObjects().isEmpty()) {
				List<ObjectContent> ocList = results.getObjects();
				for(ObjectContent oc: ocList) {
					String name = null;
					ManagedObjectReference moref = oc.getObj();
					List<DynamicProperty> dplist = oc.getPropSet();
					for(DynamicProperty dp: dplist) {
						name = (String)dp.getVal();
						System.out.println(morefType + " = " + name);
					}
					ret.put(name, moref);
				}
				String token = results.getToken();
				results = (token != null) ? this.vimPort.continueRetrievePropertiesEx(this.propColl, token) : null;
			}
		} catch (RuntimeFaultFaultMsg e) {
			e.printStackTrace();
		} catch (InvalidPropertyFaultMsg e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public void printAllOptionsMORF(ManagedObjectReference rootFolder, String morefType) {		
		//Make Container view from root folder
		List<String>list = new ArrayList<String>();
		list.add(morefType);
		
		try {
			ManagedObjectReference cViewRef = this.vimPort.createContainerView(viewMgrRef, rootFolder, list, true);
			
			//Define Object Spec to declare starting point for navigation
			ObjectSpec oSpec = new ObjectSpec();
			oSpec.setObj(cViewRef);
			oSpec.setSkip(true);
			
			//Define Traversal Specification for object selection and path selection
			TraversalSpec tSpec = new TraversalSpec();
			tSpec.setName("traverseEntities");
			tSpec.setPath("view");
			tSpec.setSkip(false);
			tSpec.setType("ContainerView");
			
			//Adding to object Specs
			oSpec.getSelectSet().add(tSpec);
			
			//Define PropertySpec to select relevant results
			PropertySpec pSpec = new PropertySpec();
			pSpec.setType(morefType);
			pSpec.setAll(true);
			
			//Define filter and add pSpec to it
			PropertyFilterSpec fSpec = new PropertyFilterSpec();
			fSpec.getObjectSet().add(oSpec);
			fSpec.getPropSet().add(pSpec);
			
			//Create a list of filters
			List<PropertyFilterSpec> fSpecList = new ArrayList<PropertyFilterSpec>();
			fSpecList.add(fSpec);
			
			//Fetch data from server
			RetrieveOptions ro = new RetrieveOptions();
			RetrieveResult results = this.vimPort.retrievePropertiesEx(propColl, fSpecList, ro);
			
			while(results != null && !results.getObjects().isEmpty()) {
				List<ObjectContent> ocList = results.getObjects();
				for(ObjectContent oc: ocList) {
					String propName = null;
					List<DynamicProperty> dplist = oc.getPropSet();
					for(DynamicProperty dp: dplist) {
						propName = dp.getName();
						System.out.println(propName);
					}
				}
				String token = results.getToken();
				results = (token != null) ? this.vimPort.continueRetrievePropertiesEx(this.propColl, token) : null;
			}
		} catch (RuntimeFaultFaultMsg e) {
			e.printStackTrace();
		} catch (InvalidPropertyFaultMsg e) {
			e.printStackTrace();
		}
	}
	
	public ManagedObjectReference getMOREFByName(ManagedObjectReference rootFolder, String morefType, String name) {
		Map<String, ManagedObjectReference> retmap = this.getMORF(rootFolder, morefType);
		return retmap.get(name);
	}

	public Map<String, Object> getEntityProps(ManagedObjectReference entityMor, String[] props) {
		Map<String, Object> ret = new HashMap<String, Object>();
		//create proprty specifications
		PropertySpec propSpec = new PropertySpec();
		propSpec.setType(entityMor.getType());
		propSpec.setAll(false);
		propSpec.getPathSet().addAll(Arrays.asList(props));
		//create object specification
		ObjectSpec objSpec = new ObjectSpec();
		objSpec.setObj(entityMor);
		//create property filter specification list
		List<PropertyFilterSpec> fSpecList = new ArrayList<PropertyFilterSpec>();
		PropertyFilterSpec propFilter = new PropertyFilterSpec();
		propFilter.getPropSet().add(propSpec);
		propFilter.getObjectSet().add(objSpec);
		fSpecList.add(propFilter);
		try {
			List<ObjectContent> oContent = vimPort.retrievePropertiesEx(serviceContent.getPropertyCollector(), fSpecList, new RetrieveOptions()).getObjects();
			if (oContent != null) {
	            for (ObjectContent oc : oContent) {
	                List<DynamicProperty> dps = oc.getPropSet();
	                for (DynamicProperty dp : dps) {
	                    ret.put(dp.getName(), dp.getVal());
	                }
	            }
	        }
		} catch (InvalidPropertyFaultMsg e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeFaultFaultMsg e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
}
