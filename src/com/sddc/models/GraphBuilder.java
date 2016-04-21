package com.sddc.models;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GraphBuilder {
	private Gson gsonParser;
	
	public GraphBuilder() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Graph.class, new GraphDeserializer());
		gsonBuilder.registerTypeAdapter(Graph.class, new GraphSerializer());
		this.gsonParser = gsonBuilder.create();
	}

	/**
	 * @param jsonGraph json string to be converted into java object
	 * @return java Graph object created from input json with user set as "default"
	 */
	public Graph getGraphFromJson(String jsonGraph) {
		Graph newGraph = this.gsonParser.fromJson(jsonGraph, Graph.class);
		newGraph.setTimeStamp("default");
		return newGraph;
	}

	/**
	 * @param jsonGraph json string to be converted into java object
	 * @param graphUser creator of the network
	 * @return java Graph object created from input json
	 */
	public Graph getGraphFromJson(String jsonGraph, String graphUser) {
		Graph newGraph = this.gsonParser.fromJson(jsonGraph, Graph.class);
		newGraph.setTimeStamp(graphUser);
		return newGraph;
	}
	
	/**
	 * @param jsonGraph json from which Graph is to be created
	 * @param graphUser User of the Graph
	 * @param dname Name of the design
	 * @param desc Description for the design
	 * @return Returns a new graph with timestamp set
	 */
	public Graph getGraphFromJson(String jsonGraph, String graphUser, String dname, String desc) {
		Graph newGraph = this.gsonParser.fromJson(jsonGraph, Graph.class);
		newGraph.setTimeStamp(graphUser);
		newGraph.setDesignName(dname);
		newGraph.setDescription(desc);
		return newGraph;
	}
	
	/**
	 * @param graph Graph Object to be converted into json
	 * @return json string for object
	 */
	public String getJsonFromGraph(Graph graph) {
		return this.gsonParser.toJson(graph);
	}
}

/**
 * @author vasco
 * Graph Deserializer for converting json string to java object
 */
class GraphDeserializer implements JsonDeserializer<Graph>{
	/**
	 * @return returns Graph object after deserializing the input json
	 */
	@Override
	public Graph deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObject = json.getAsJsonObject();
		
		List<Node> nodes = new ArrayList<Node>();
		List<VmInfo> vmList = new ArrayList<VmInfo>();
		List<RouterInfo> routerList = new ArrayList<RouterInfo>();
		List<SubnetInfo> subnetList = new ArrayList<SubnetInfo>();
		
		JsonArray nodeArray = jsonObject.get("nodes").getAsJsonArray();
		
		Iterator<JsonElement> i = nodeArray.iterator();
		while(i.hasNext()) {
			JsonElement tmpElement = i.next();
			JsonObject tmpObject = tmpElement.getAsJsonObject();
			Node tmpNode = context.deserialize(tmpObject, Node.class);
			nodes.add(tmpNode);
			
			Node.NodeType nodeType = tmpNode.setNodeType();
			switch(nodeType) {
			case ROUTER:
				RouterInfo newRouter = context.deserialize(tmpObject, RouterInfo.class);
				routerList.add(newRouter);
				break;
			case SUBNET:
				SubnetInfo newSubnet = context.deserialize(tmpObject, SubnetInfo.class);
				subnetList.add(newSubnet);
				break;
			case VM:
				VmInfo newVm = context.deserialize(tmpObject, VmInfo.class);
				vmList.add(newVm);
				break;
			default:
				break;
			}
		}
		
		Link[] linksArray = context.deserialize(jsonObject.get("links"), Link[].class);
		
		Node[] nodesArray = new Node[nodes.size()];
		nodes.toArray(nodesArray);
		
		VmInfo[] vmArray = new VmInfo[vmList.size()];
		vmList.toArray(vmArray);
		
		RouterInfo[] routerArray = new RouterInfo[routerList.size()];
		routerList.toArray(routerArray);

		SubnetInfo[] subnetArray = new SubnetInfo[subnetList.size()];
		subnetList.toArray(subnetArray);
		
		Graph graph = new Graph();
		graph.setNodesList(nodesArray);
		graph.setLinkList(linksArray);
		graph.setRouterList(routerArray);
		graph.setSubnetList(subnetArray);
		graph.setVmList(vmArray);
		return graph;
	}
}

class GraphSerializer implements JsonSerializer<Graph> {
	@Override
	public JsonElement serialize(Graph graph, Type typeOfSrc, JsonSerializationContext context) {
		/*
		 * 1. Generate nodes array
		 * 2. Generate links array
		 * 3. add two array to JsonObject
		 * 4. We are adding three new counters for proper front end working
		 */
		int vmCount = 0, routerCount = 0, subnetCount = 0;
		
		JsonArray jsonNodesArray = new JsonArray();
		VmInfo[] vms = graph.getVmList();
		for(int i = 0; i < vms.length; i++) {
			int id = vms[i].getId();
			JsonElement jsonVm = context.serialize(vms[i]);
			JsonObject dataJson = new JsonObject();
			dataJson.add("data", jsonVm);
			dataJson.addProperty("id", id);
			jsonNodesArray.add(dataJson);
			vmCount++;
		}
		
		RouterInfo[] routers = graph.getRouterList();
		for(int i = 0; i < routers.length; i++) {
			int id = routers[i].getId();
			JsonElement jsonRouter = context.serialize(routers[i]);
			JsonObject dataJson = new JsonObject();
			dataJson.add("data", jsonRouter);
			dataJson.addProperty("id", id);
			jsonNodesArray.add(dataJson);
			routerCount++;
		}
		
		SubnetInfo[] subnets = graph.getSubnetList();
		for(int i = 0; i < subnets.length; i++) {
			int id = subnets[i].getId();
			JsonElement jsonSubnet = context.serialize(subnets[i]);
			JsonObject dataJson = new JsonObject();
			dataJson.add("data", jsonSubnet);
			dataJson.addProperty("id", id);
			jsonNodesArray.add(dataJson);
			subnetCount++;
		}
		
		JsonElement jsonLinksArray = context.serialize(graph.getLinkList());
		
		JsonObject finalJson = new JsonObject();
		finalJson.add("nodes", jsonNodesArray);
		finalJson.add("links", jsonLinksArray);
		finalJson.addProperty("vmcount", vmCount);
		finalJson.addProperty("routercount", routerCount);
		finalJson.addProperty("subnetcount", subnetCount);
		return finalJson;
	}
}