//On Click Events for the buttons
$("#addRouter").click(addRouter);
$("#addVm").click(addVm);
$("#addStorage").click(addStorage);
$("#realize").click(submitGraph);

var vmEventHandler, routerEventHandler, storageEventHandler;

function init() {
   // put all initialisations here
   $('#connectionToggle').attr('checked', false);
   routerEventHandler = routerEventHandler_1;
   vmEventHandler = vmEventHandler_1;
   storageEventHandler = storageEventHandler_1;
   console.log("Loading done");
}

function addRouter(){
   console.log("Adding new Router Router");
   var newRouter = new joint.shapes.basic.Image({
      position : {
         x : 400,
         y : 400
      },
      size : {
         width : 63,
         height : 58
      },
      attrs : {
         image : {
            "xlink:href" : "resources/images/router.png",
            width : 63,
            height : 58
         }
      },
      onClickFunction : routerOnClick
   });
   graph.addCells([newRouter]);
}

function addVm(){
   console.log("Adding new Vm");
   var newVm = new joint.shapes.basic.Image({
      position : {
         x : 100,
         y : 100
      },
      size : {
         width : 74,
         height : 70
      },
      attrs : {
         image : {
            "xlink:href" : "resources/images/vm.png",
            width : 74,
            height : 70
         }
      },
      onClickFunction : vmOnClick
   });
   graph.addCells([newVm]);
}

function addStorage(){
   console.log("Addin new Storage");
   var newStorage = new joint.shapes.basic.Image({
      position : {
         x : 100,
         y : 100
      },
      size : {
         width : 100,
         height : 67
      },
      attrs : {
         image : {
            "xlink:href" : "resources/images/storage.png",
            width : 80,
            height : 67
         }
      },
      onClickFunction : storageOnClick
   });
   graph.addCells([newStorage]);
}

//Enable/Disable Routing
function addConnection(){
   console.log("Altering Funtions");
   // addConnection.toggleValue = addConnection.toggleValue || false;
   var toggleValue = $("#connectionToggle").prop('checked');
   console.log(toggleValue);
   //we modify the function definitions here
   if(toggleValue == true) {
      routerEventHandler = routerEventHandler_2;
      vmEventHandler = vmEventHandler_2;
      storageEventHandler = storageEventHandler_2;
      addConnection.toggleValue = true;
   }
   else {
      routerEventHandler = routerEventHandler_1;
      vmEventHandler = vmEventHandler_1;
      storageEventHandler = storageEventHandler_1;
      addConnection.toggleValue = false;
   }
}

var graph = new joint.dia.Graph;
console.log($("#sketch").width());
console.log($("#sketch").height());
var paper = new joint.dia.Paper({
    el: $('#sketch'),
    width: $("#sketch").width(),
    height: $("#sketch").height(),
    model: graph,
   //  perpendicularLinks: true,
    gridSize: 1
});

paper.on('cell:pointerdblclick',function(cellView, evt, x, y) {
   cellView.model.attributes.onClickFunction();
});

//router onclick functions
function routerOnClick()
{
   routerEventHandler();
}
var routerEventHandler_1 = function() {
   console.log("r1");
}
var routerEventHandler_2 = function() {
   console.log("r2");
}

//vm onclick functions
function vmOnClick()
{
   vmEventHandler();
}
var vmEventHandler_1 = function() {
   console.log("v1");
}
var vmEventHandler_2 = function() {
   console.log("v2");
}

//storage onclick functions
function storageOnClick()
{
   storageEventHandler();
}
var storageEventHandler_1 = function() {
   console.log("s1");
}
var storageEventHandler_2 = function() {
   console.log("s2");
}

// allows additions of link in network
// 1. Create new temporary element
// 2. Add link to temp node
// 3. move temp node to Destination
// 4. remove tmp node and link and permanent link
// var sourceObj, destObj, tmpObj;

// paper.on('cell:pointerdown', function(cellView, evt, x, y) {
//    var toggleValue = $("#connectionToggle").prop('checked');
//    if (toggleValue == true) {
//       //make source node immovable
//       console.log("Source = " + cellView.model.id);
//       sourceObj = cellView.model;
//       // paper.findViewByModel(cellView.model).options.interactive = false;
//       //creating new node
//       tmpObj = new joint.shapes.basic.Rect({
//          position : { x : x, y : y },
//          size : { width : 10, height : 10 },
//          attrs : { text : { text : 'test', magnet : true }}
//       });
//       var link = new joint.dia.Link({
//           source: { id: tmpObj.id },
//           target: { id: sourceObj.id }
//       });
//       sourceObj.attr({
//          image : {
//             "xlink:href" : "images/dot.png",
//             width : 80,
//             height : 67
//          }
//       });
//       graph.addCells([tmpObj, link]);
//    }
// });
//
// paper.on('cell:pointerup', function(cellView, evt, x, y) {
//    var toggleValue = $("#connectionToggle").prop('checked');
//    if(toggleValue == true) {
//       // Find the first element below that is not a link nor the dragged element itself.
//       destObj = graph.get('cells').find(function(cell) {
//          if (cell instanceof joint.dia.Link) return false; // Not interested in links.
//          if (cell.id === cellView.model.id) return false; // The same element as the dropped one.
//          if (cell.getBBox().containsPoint(g.point(x, y))) {
//             return true;
//          }
//          return false;
//       });
//       if(destObj != undefined) {
//          console.log("Destination = " + destObj.id);
//          // paper.findViewByModel(elementBelow).options.interactive = false;
//          var link = new joint.dia.Link({
//             source: { id: sourceObj.id },
//             target: { id: destObj.id }
//          });
//          console.log(JSON.stringify(link));
//          graph.addCells([link]);
//          paper.findViewByModel(sourceObj).options.interactive = true;
//       }
//    }
// });
//

var sourceObj, destObj;
paper.on('cell:pointerdown', function(cellView, evt, x, y) {
   var toggleValue = $("#connectionToggle").prop('checked');
   if (toggleValue == true) {
      console.log("Source = " + cellView.model.id);
      sourceObj = cellView.model;
      paper.findViewByModel(cellView.model).options.interactive = false;
   }
});

paper.on('cell:pointerup', function(cellView, evt, x, y) {
   var toggleValue = $("#connectionToggle").prop('checked');
   if(toggleValue == true) {
      // Find the first element below that is not a link nor the dragged element itself.
      destObj = graph.get('cells').find(function(cell) {
         if (cell instanceof joint.dia.Link) return false; // Not interested in links.
         if (cell.id === cellView.model.id) return false; // The same element as the dropped one.
         if (cell.getBBox().containsPoint(g.point(x, y))) {
            return true;
         }
         return false;
      });
      if(destObj != undefined) {
         console.log("Destination = " + destObj.id);
         // paper.findViewByModel(elementBelow).options.interactive = false;
         var link = new joint.dia.Link({
            source: { id: sourceObj.id },
            target: { id: destObj.id }
         });
         console.log(JSON.stringify(link));
         graph.addCells([link]);
         paper.findViewByModel(sourceObj).options.interactive = true;
      }
   }
});

function submitGraph(){
	$('input[name="jsonGraph"]').val(JSON.stringify(graph.toJSON()));
	alert("Submitting");
	$("#graphForm").submit();
}