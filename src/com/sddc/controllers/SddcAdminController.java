/**
 *  Main controller that handles all requests and responses
 */
package com.sddc.controllers;

/**
 * Spring Support
 */
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sddc.dbinterfaces.TemplateRepository;
import com.sddc.models.Graph;
import com.sddc.models.GraphBuilder;
import com.sddc.models.GraphTemplate;
import com.sddc.models.datacollectors.JsonGraph;
import com.sddc.models.datacollectors.UserDesignInput;
import com.sddc.models.datacollectors.Users;

@Controller
@RequestMapping("/admin")
@Scope("session")
public class SddcAdminController {
	private static Logger logger = LoggerFactory.getLogger(SddcAdminController.class);
	private static Users rootUser = null;
	
	private Users sessionUser = null;
	private Graph sessionGraph = null;
	private GraphBuilder graphBuilder = new GraphBuilder();
	
	@Autowired
	private TemplateRepository templateRepo;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String defaultPage() {
		return "redirect:/admin/login";
	}
	
	//Handling the login requests
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(ModelMap model) {
		if(rootUser != null)
			return "/admin/reloginerror";
		Users user = new Users();
		model.addAttribute("user", user);
		return "/admin/login";
	}
	
	//Login authentication and redirection
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String loginValidate(@ModelAttribute Users user, ModelMap model) {
		if(user.getUserName().equals("root") && user.getPassWord().equals("root") && rootUser == null) {
			this.sessionUser = user;
			rootUser = user;
			logger.info("First Time user login successful");
			return "redirect:/admin/home";
		}
		return "redirect:/admin/login";
	}
	
	//Admin home
	@RequestMapping(value="/home", method=RequestMethod.GET)
	public String home(ModelMap model) {
		if(this.sessionUser == null) {
			return "redirect:/admin/login";
		}
		model.addAttribute("user", this.sessionUser);
		return "/admin/home";
	}
	
	//Admin help
	@RequestMapping(value="/help", method=RequestMethod.GET)
	public String help(ModelMap model) {
		if(this.sessionUser == null) {
			return "redirect:/admin/login";
		}
		model.addAttribute("user", this.sessionUser);
		return "/admin/help";
	}
	
	//Admin about
	@RequestMapping(value="/about", method=RequestMethod.GET)
	public String about(ModelMap model) {
		if(this.sessionUser == null) {
			return "redirect:/admin/login";
		}
		model.addAttribute("user", this.sessionUser);
		return "/admin/about";
	}
	
	//Admin Logout
	@RequestMapping(value="/logout", method=RequestMethod.GET)
	public String logout() {
		if(this.sessionUser == null) {
			return "redirect: /admin/login";
		}
		this.sessionUser = null;
		this.sessionGraph = null;
		rootUser = null;
		return "redirect:/admin/login";
	}
	
	//Admin template addition page
	@RequestMapping(value="/template", method=RequestMethod.GET)
	public String template(ModelMap model) {
		if(this.sessionUser == null) {
			logger.info("User");
			return "redirect:/admin/login";
		}
		
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
		
		if(this.sessionGraph == null) {
			this.sessionGraph = new Graph();
		}	
		model.addAttribute("jgraph", graphBuilder.getJsonFromGraph(this.sessionGraph));	//graph is built from this json
		model.addAttribute("graph", this.sessionGraph.getJsonGraph()); // This attribute is used for getting design name, description and final json
		model.addAttribute("user", this.sessionUser);
		return "/admin/template";
	}
	
	@RequestMapping(value="/save", method=RequestMethod.POST) 
	public String saveTemplate(@ModelAttribute JsonGraph graph, ModelMap model) {
		Graph newGraph = graphBuilder.getGraphFromJson(graph.getJsonGraph(), this.sessionUser.getUserName(), this.sessionGraph.getDesignName(), this.sessionGraph.getDescription());
		newGraph.setUserName("template");
		this.templateRepo.save((GraphTemplate)newGraph);
		this.sessionGraph = newGraph;
		logger.info("Saving Template " + this.sessionGraph.getDesignName() + " : " + this.sessionGraph.getDescription());
		model.addAttribute("result", graphBuilder.getJsonFromGraph(this.sessionGraph));
		return "test_view/result";
	}
	
	//Facilitate new network
	@RequestMapping(value="/newdesign", method=RequestMethod.POST)
	public String newDesign(@ModelAttribute JsonGraph jgraph, ModelMap model) {
		this.sessionGraph = new Graph();
		this.sessionGraph.setDesignName(jgraph.getName());
		this.sessionGraph.setDescription(jgraph.getDescription());
		return "redirect:template";
	}
	
	//Loading of the selected template
	@RequestMapping(value = "/templatedesign", method = RequestMethod.POST)
	public String templateDesign(@ModelAttribute UserDesignInput uinput, ModelMap model){
		this.sessionGraph = (Graph) this.templateRepo.findByGraphId(uinput.getDesignId());
		this.sessionGraph.setUserName(this.sessionUser.getUserName());
		this.sessionGraph.setDesignName(uinput.getDname());
		return "redirect:template";
	}
	
	//terminate session
	@RequestMapping(value = "/terminate", method = RequestMethod.POST)
	public String terminate() {
		this.sessionUser = null;
		this.sessionGraph = null;
		this.graphBuilder = null;
		SddcAdminController.rootUser = null;
		logger.info("Destroyed");
		return "empty";
	}
}
