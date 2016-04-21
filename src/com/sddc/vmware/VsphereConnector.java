package com.sddc.vmware;

import java.util.Map;
import javax.xml.ws.BindingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.InvalidLocaleFaultMsg;
import com.vmware.vim25.InvalidLoginFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.UserSession;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;


	
public class VsphereConnector {
	//Login Credentials
	private String vcenterServer;
	private String userName;
	private String passWord;
	private String url;
	
	//API Info
	private UserSession userSession = null;
	private String apiVersion = "6.0.0.0";
	private String servType = "VirtualCenter";
	
	//ManagVsphereTester.test();ement Objects
	VimService vimService;
	VimPortType vimPort;
	ServiceContent serviceContent;
	ManagedObjectReference serviceInstance = null;

	//Logger
	private static final Logger logger = LoggerFactory.getLogger(VsphereConnector.class);
	
	public VsphereConnector() {
		VcenterLoginCredentials fileLogin = new VcenterLoginCredentials("./SddcConfig.xml");
		this.vcenterServer = fileLogin.getServerIp();
		this.userName = fileLogin.getUserName();
		this.passWord = fileLogin.getPassWord();
		this.url = "https://" + this.vcenterServer + "/sdk/vimService";
		logger.info(this.vcenterServer);
		logger.info(this.userName);
		logger.info(this.passWord);
	}
	
	/**
	 * @param server Server ip
	 * @param user Username for vcenter server
	 * @param pass Password for vcenter server
	 */
	public VsphereConnector(String server, String user, String pass) {
		this.vcenterServer = server;
		this.userName = user;
		this.passWord = pass;
		this.url = "https://" + this.vcenterServer + "/sdk/vimService";
	}
	
	/**
	 * @return Returns UserSession Info
	 */
	public UserSession getUserSession() {
		return this.userSession;
	}
	
	/**
	 * @return Returns api version of Vcenter
	 */
	public String getApiVersion() {
		return this.apiVersion;
	}
	
	/**
	 * @return Returns type of the Vcenter Server
	 */
	public String getServType() {
		return this.servType;
	}
	
	/**
	 * @return ServiceInstance object
	 */
	private ManagedObjectReference getServiceInstance() {
		if(this.serviceInstance == null){
			ManagedObjectReference ref = new ManagedObjectReference();
			ref.setType("ServiceInstance");
			ref.setValue("ServiceInstance");
			this.serviceInstance = ref;
		}
		return this.serviceInstance;
	}
	
	/**
	 * @return Returns VimPort
	 */
	public VimPortType getVimPort() {
		return this.vimPort;
	}
	
	public ServiceContent getServiceContent() {
		return this.serviceContent;
	}
	
	public void printApiInfo(){
		 // print out the product name, server type, and product version
        System.out.println(this.serviceContent.getAbout().getFullName());
        System.out.println("Server type is " + this.serviceContent.getAbout().getApiType());
        System.out.println("API version is " + this.serviceContent.getAbout().getVersion());
        this.servType = this.serviceContent.getAbout().getApiType();
        this.apiVersion = this.serviceContent.getAbout().getVersion();
	}
	
	public void connect() {
		this.vimService = new VimService();
        this.vimPort = vimService.getVimPort();

        Map<String, Object> ctxt = ((BindingProvider) vimPort).getRequestContext();
        ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.url);
        ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

        ManagedObjectReference serviceInstance = this.getServiceInstance();

        try {
            this.serviceContent = vimPort.retrieveServiceContent(serviceInstance);
            this.userSession = vimPort.login(this.serviceContent.getSessionManager(), this.userName, this.passWord, null);
        } catch (RuntimeFaultFaultMsg e) {
            e.printStackTrace();
        } catch (InvalidLocaleFaultMsg e) {
            e.printStackTrace();
        } catch (InvalidLoginFaultMsg e) {
            e.printStackTrace();
        }
	}
	
	public boolean isConnected() {
		if(userSession == null)
			return false;
		return true;
	}
	
	public void disconnect() {
		try {
			this.vimPort.logout(this.serviceContent.getSessionManager());
		} catch (RuntimeFaultFaultMsg e) {
			e.printStackTrace();
		}
	}
}
