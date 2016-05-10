/**
 *  Main controller that handles all requests and responses
 */
package com.sddc.controllers;

/**
 * Spring Support
 */
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.sddc.dbinterfaces.GraphRepository;
import com.sddc.dbinterfaces.TemplateRepository;
/**
 * Database interfaces
 */
import com.sddc.dbinterfaces.UsersRepository;

/**
 * Data Management
 */
import com.sddc.models.Graph;
import com.sddc.models.GraphBuilder;
import com.sddc.models.GraphTemplate;
import com.sddc.models.NetworkRealizer;
import com.sddc.models.datacollectors.JsonGraph;
import com.sddc.models.datacollectors.UserDesignInput;
import com.sddc.models.datacollectors.Users;
import com.sddc.vmware.VsphereTester;

@Controller
@Scope("session")
public class SddcMainController {
	//Session user info
	private Users sessionUser = null;
	
	//Allowing only one network design per session
	private Graph sessionGraph = null;

	//All required database repositories
	@Autowired
	private UsersRepository userRepo;
	@Autowired
	private GraphRepository graphRepo;
	@Autowired
	private TemplateRepository templateRepo;
	
	//Json Parser
	GraphBuilder graphBuilder = new GraphBuilder();
	
	//Logging interface
	private static final Logger logger = LoggerFactory.getLogger(SddcMainController.class);

	//Default test page
	@RequestMapping("/")
	public String hello()
	{
		VsphereTester.test();
		return "welcomepage";
	}

	//Main design page
	@RequestMapping(value = "/design", method = RequestMethod.GET)
	public String design(ModelMap model)
	{
		if(this.sessionUser == null)
			return "redirect:login";
		
		// To display all the graphs registered by the name of user to reopen
		List<Graph> list = this.graphRepo.findByUserName(this.sessionUser.getUserName());
		Map<String, String> userGraphDropDown = new HashMap<String, String>();
		Iterator<Graph> graphI = list.iterator(); 
		while(graphI.hasNext()) {
			Graph tmpGraph = graphI.next();
			userGraphDropDown.put(tmpGraph.getGraphId(), tmpGraph.getDesignName() + " : " + tmpGraph.getDescription());
		}
		model.addAttribute("userGraphDropDown", userGraphDropDown);	//drop down menu for previous design
		logger.info("Total Number of designs for " + this.sessionUser.getUserName() + " : " + list.size());
		
		// To provide the template list
		List<GraphTemplate> templateList = this.templateRepo.findAllByUserName("template");
		Map<String, String> templateDropDown = new HashMap<String, String>();
		Iterator<GraphTemplate> graphJ = templateList.iterator(); 
		while(graphJ.hasNext()) {
			GraphTemplate tmpGraph = graphJ.next();
			templateDropDown.put(tmpGraph.getGraphId(), tmpGraph.getDesignName() + " : " + tmpGraph.getDescription());
		}
		model.addAttribute("templateDropDown", templateDropDown);	//drop down menu for previous design
		logger.info("Total Number of template : " + templateList.size());
		
		model.addAttribute("userDesignInput", new UserDesignInput()); //Input for opening previous design

		//TODO - Get the latest design from database for the user and set the sessionGraph
		if(this.sessionGraph == null) {	//empty graph display previous one
			if(list.isEmpty()) {
				this.sessionGraph = new Graph();
			}
			else {
				this.sessionGraph = list.get(0);
			}
		}
		else {	//new graph has been created; use that one
		}
		model.addAttribute("jgraph", graphBuilder.getJsonFromGraph(this.sessionGraph));	//graph is built from this json
		model.addAttribute("graph", this.sessionGraph.getJsonGraph()); // This attribute is used for getting design name, description and final json
		model.addAttribute("user", this.sessionUser);
		return "vivadesign";
	}

	//Home Page
	@RequestMapping("/home")
	public String home(ModelMap model)
	{
		if(this.sessionUser == null)
			return "redirect:login";
		model.addAttribute("user", this.sessionUser);
		return "home";
	}

	//Help Documents
	@RequestMapping("/help")
	public String help(ModelMap model)
	{
		if(this.sessionUser == null)
			return "redirect:login";
		model.addAttribute("user", this.sessionUser);
		return "help";
	}

	//About this project
	@RequestMapping("/about")
	public String about(ModelMap model)
	{
		if(this.sessionUser == null)
			return "redirect:login";
		model.addAttribute("user", this.sessionUser);
		return "about";
	}

	//Handling the login requests
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(ModelMap model) {
		if(this.sessionUser != null)
			return "redirect:home";
		Users user = new Users();
		model.addAttribute("user", user);
		return "login";
	}

	//Login authentication and redirection
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String loginValidate(@ModelAttribute Users user, ModelMap model) {
		this.sessionUser = userRepo.findByUserName(user.getUserName());
		if(this.sessionUser == null)
		{
			// User does not exists. Redirecting to login page
			return "redirect:login";
		}
		String sessionPassWord = this.sessionUser.getPassWord();
		if(sessionPassWord.equals(user.getPassWord()))
		{
			//Authenticate the user and redirect to home
			logger.info("Successful Login for " + this.sessionUser.getUserName());
			return "redirect:home";
		}
		//Authentication failed. Redirecting to login page
		this.sessionUser = null;
		return "redirect:login";
	}

	// Logging out and clearing session variables
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logout(ModelMap model) {
		this.sessionUser = null;
		this.sessionGraph = null;
		return "redirect:login";
	}
	
	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public String testModule1(ModelMap model)
	{
		JsonGraph graph = new JsonGraph();
		model.addAttribute("graph", graph);
		return "test_view/test";
	}

	@RequestMapping(value = "/test", method = RequestMethod.POST)
	public String testModule2(@ModelAttribute JsonGraph graph, ModelMap model) {
		Graph newGraph = graphBuilder.getGraphFromJson(graph.getJsonGraph(), this.sessionUser.getUserName(), this.sessionGraph.getDesignName(), this.sessionGraph.getDescription());
		newGraph.setGraphId(this.sessionGraph.getGraphId());
		this.graphRepo.save(newGraph);
		this.sessionGraph = newGraph;
		logger.info(this.sessionGraph.toString());
		model.addAttribute("result", graphBuilder.getJsonFromGraph(this.sessionGraph));
		return "test_view/result";
	}
	
	@RequestMapping(value = "/newuser", method = RequestMethod.POST)
	public String newUser(@ModelAttribute Users user, ModelMap model) {
		this.userRepo.save(user);
		return "redirect:login";
	}
	
	@RequestMapping(value = "/newdesign", method = RequestMethod.POST)
	public String newDesign(@ModelAttribute JsonGraph jgraph, ModelMap model) {
		this.sessionGraph = new Graph();
		this.sessionGraph.setDesignName(jgraph.getName());
		this.sessionGraph.setDescription(jgraph.getDescription());
		return "redirect:design";
	}
	
	@RequestMapping(value = "/userdesign", method = RequestMethod.POST)
	public String userDesign(@ModelAttribute UserDesignInput uinput, ModelMap model){
		this.sessionGraph = this.graphRepo.findByGraphId(uinput.getDesignId());
		return "redirect:design";
	}
	
	@RequestMapping(value = "/templatedesign", method = RequestMethod.POST)
	public String templateDesign(@ModelAttribute UserDesignInput uinput, ModelMap model){
		this.sessionGraph = (Graph) this.templateRepo.findByGraphId(uinput.getDesignId());
		this.sessionGraph.setUserName(this.sessionUser.getUserName());
		this.sessionGraph.setDesignName(uinput.getDname());
		return "redirect:design";
	}
			
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String saveGraph(@ModelAttribute JsonGraph graph, ModelMap model) {
		Graph newGraph = graphBuilder.getGraphFromJson(graph.getJsonGraph(), this.sessionUser.getUserName(), this.sessionGraph.getDesignName(), this.sessionGraph.getDescription());
//		newGraph.setGraphId(this.sessionGraph.getGraphId());
		newGraph.setUserName("template");
		this.templateRepo.save((GraphTemplate)newGraph);
		this.sessionGraph = newGraph;
//		logger.info(this.sessionGraph.toString());
		model.addAttribute("result", graphBuilder.getJsonFromGraph(this.sessionGraph));
		return "test_view/result";
	}
	
	@RequestMapping(value = "/realize", method = RequestMethod.POST)
	public String realizeGraph(@ModelAttribute JsonGraph graph, ModelMap model) {
		Graph newGraph = graphBuilder.getGraphFromJson(graph.getJsonGraph(), this.sessionUser.getUserName(), this.sessionGraph.getDesignName(), this.sessionGraph.getDescription());
		newGraph.setGraphId(this.sessionGraph.getGraphId());
		this.graphRepo.save(newGraph);
		this.sessionGraph = newGraph;
		logger.info("Realizing network " + this.sessionGraph.getDesignName() + " for user " + this.sessionGraph.getUsername());
		this.sessionGraph.normalize();
		NetworkRealizer netRealizer = new NetworkRealizer(this.sessionGraph);
		netRealizer.createNetwork();
		logger.info(this.sessionGraph.toString());
		model.addAttribute("result", graphBuilder.getJsonFromGraph(this.sessionGraph));
		return "test_view/result";	
	}
}
