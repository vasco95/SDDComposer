//Viva graph elements
var graph, renderer;

//element sizes
var widths = {"vm" : 50, "router" : 50, "subnet" : 100};
var heights = {"vm" : 50, "router" : 50, "subnet" : 100};
var linkLength = 130;

//Event Handlers
var newLink;

//Node Display Utilities
function nodeDisplay(node) {
   var ui = Viva.Graph.svg('g');
   var nodeObj = node.data;
   var text = Viva.Graph.svg('text').text(nodeObj.name).attr('width', nodeObj.name.size).attr('height', nodeObj.name.size).attr('id', 'nodeText_'+nodeObj.id),
   img = Viva.Graph.svg('image').attr('width', widths[nodeObj.type]).attr('height', heights[nodeObj.type]).attr('id', 'nodeImg_'+nodeObj.id).link('resources/images/' + nodeObj.type + '.png');
   ui.append(img);
   ui.append(text);
   ui.type = nodeObj.type;
   $(ui).click(function() {
      newLink(this.node);
   });
   return ui;
}
function nodePlacement(nodeUI, pos) {
   // nodeUI.attr('x', pos.x - 12).attr('y', pos.y - 12);
   nodeUI.attr('transform', 'translate(' + (pos.x - widths[nodeUI.type]/2) + ',' + (pos.y - heights[nodeUI.type]/2) + ')');
}

//Link Display Utilities
function linkDisplay(link) {
   return Viva.Graph.svg('path')
      .attr('stroke', 'gray')
      .attr('file', 'none');
}
function linkPlacement(linkUI, fromPos, toPos) {
   // linkUI - is the object returned from link() callback above.
   var data = 'M' + fromPos.x + ',' + fromPos.y +
   'L' + toPos.x + ',' + toPos.y;

   // 'Path data' (http://www.w3.org/TR/SVG/paths.html#DAttribute )
   // is a common way of rendering paths in SVG:
   linkUI.attr("d", data);
}

function init() {
   // put all initializations here
   //1.Managing Event Handlers
   $("#addRouter").click(addRouter);
   $("#addVm").click(addVm);
   $("#addSubnet").click(addSubnet);
   $('#connectionToggle').change(addConnection);
   $("#done").click(printJSON);
   $("#realize").click(submitGraph);

   //Initializing functions
   $('input[name="connectionToggle"]').attr('checked', false);
   addNewLink.sourceFlag = false;
   newLink = disableNewLink;
   //Creating network graph
   graph = Viva.Graph.graph();
   var graphics = Viva.Graph.View.svgGraphics();
   graphics.node(nodeDisplay).placeNode(nodePlacement);

   // Step 4. Customize link appearance:
   //   As you might have guessed already the link()/placeLink()
   //   functions complement the node()/placeNode() functions
   //   and let us override default presentation of edges:
   graphics.link(linkDisplay).placeLink(linkPlacement);

   var layout = Viva.Graph.Layout.forceDirected(graph, {
      springLength: linkLength,
      springCoeff : 0.0008,
      gravity : -0.3,
      springTransform: function (link, spring) {
        spring.length = linkLength;
      }
   });
   // Step 5. Render the graph with our customized graphics object:
   var renderer = Viva.Graph.View.renderer(graph, {
      container : document.getElementById('sketch'),
      layout : layout,
      graphics : graphics
   });
   renderer.run();
   // renderer.pause();
   console.log("Loading done");
}

//Creates a new network element with unique id
function getNewElement(type) {
   if(getNewElement.counter == undefined){
      getNewElement.counter = 0;
   }
   if(type == "vm"){
      if(getNewElement.vmCount == undefined){
         getNewElement.vmCount = 0;
      }
      //create a new id
      var id = getNewElement.counter;
      getNewElement.counter++;

      //create a new object
      var tmpVm = new Object();
      tmpVm.type = "vm";
      //tmpVm.typeId = 0;
      tmpVm.id = id;
      tmpVm.name = "vm_" + getNewElement.vmCount.toString();
      tmpVm.cpuCores = 1;  //in number
      tmpVm.ram = 1024; //in mb
      tmpVm.storage = 16;  //in gb

      getNewElement.vmCount++;
      return tmpVm;
   }
   else if (type == "router") {
      if(getNewElement.routerCount == undefined){
         getNewElement.routerCount = 0;
      }
      //create a new id
      var id = getNewElement.counter;
      getNewElement.counter++;

      //create a new object
      var tmpRouter = new Object();
      tmpRouter.type = "router";
      //tmpRouter.typeId = 1;
      tmpRouter.id = id;
      tmpRouter.name = "router_" + getNewElement.routerCount.toString();

      getNewElement.routerCount++;
      return tmpRouter;
   }
   else if(type == "subnet"){
      if(getNewElement.subnetCount == undefined){
         getNewElement.subnetCount = 0;
      }
      //create a new id
      var id = getNewElement.counter;
      getNewElement.counter++;

      //create a new object
      var tmpSubnet = new Object();
      tmpSubnet.type = "subnet";
      //tmpSubnet.typeId = 2;
      tmpSubnet.id = id;
      tmpSubnet.name = "subnet_" + getNewElement.subnetCount.toString();

      getNewElement.subnetCount++;
      return tmpSubnet;
   }
   return null;
}

function addVm(){
   var newVm = getNewElement("vm");
   graph.addNode(newVm.id, newVm);
   console.log("Adding new Vm");
}

function addRouter(){
   var newRouter = getNewElement("router");
   graph.addNode(newRouter.id, newRouter);
   console.log("Adding new Router Router");
}
function addSubnet(){
   var newSubnet = getNewElement("subnet");
   graph.addNode(newSubnet.id, newSubnet);
   console.log("Addin new Subnet");
}

//Enable/Disable Routing
function addConnection(){
   console.log("Altering Funtions");
   // addConnection.toggleValue = addConnection.toggleValue || false;
   var toggleValue = $("#connectionToggle").prop('checked');
   console.log(toggleValue);
   //we modify the function definitions uehere
   if(toggleValue == true) {
      newLink = addNewLink;
   }
   else {
      newLink = disableNewLink;
   }
}

function addNewLink(node) {
   if(addNewLink.sourceFlag == false) {
      addNewLink.sourceNode = node;
      addNewLink.sourceFlag = true;
   }
   else {
      destNode = node;
      graph.addLink(addNewLink.sourceNode.id, destNode.id);
      addNewLink.sourceFlag = false;
   }
}

function disableNewLink(node) {
}

function printJSON(){
   var ser = Viva.Graph.serializer();
   var jgraph = ser.storeToJSON(graph);
   console.log(jgraph);
}

function submitGraph(){
	var ser = Viva.Graph.serializer();
	var jgraph = ser.storeToJSON(graph);
	var newObj = JSON.parse(jgraph);

	var finalNodes = new Array();

	var nodeArray = new Array();
	nodeArray = newObj.nodes;
	var nodeLen = nodeArray.length;
	for(var i = 0; i < nodeLen; i++) {
	   var tmpObj = nodeArray[i].data;
	   finalNodes.push(tmpObj);
	}

	var linkArray = newObj.links;

	var finalObj = new Object();
	finalObj["nodes"] = finalNodes;
	finalObj["links"] = linkArray;
	$('input[name="jsonGraph"]').val(JSON.stringify(finalObj));
	alert("Submitting");
	$("#graphForm").submit();
}

function printJSON(){
//	var ser = Viva.Graph.serializer();
//	var jgraph = ser.storeToJSON(graph);
//	console.log(JSON.stringify(graph));
	var ser = Viva.Graph.serializer();
	var jgraph = ser.storeToJSON(graph);
	var newObj = JSON.parse(jgraph);

	var finalNodes = new Array();

	var nodeArray = new Array();
	nodeArray = newObj.nodes;
	var nodeLen = nodeArray.length;
	for(var i = 0; i < nodeLen; i++) {
	   var tmpObj = nodeArray[i].data;
	   finalNodes.push(tmpObj);
	}

	var linkArray = newObj.links;

	var finalObj = new Object();
	finalObj["nodes"] = finalNodes;
	finalObj["links"] = linkArray;
	console.log(JSON.stringify(finalObj));
}