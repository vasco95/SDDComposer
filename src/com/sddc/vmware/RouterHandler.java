package com.sddc.vmware;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.ConcurrentAccessFaultMsg;
import com.vmware.vim25.DuplicateNameFaultMsg;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.HttpNfcLeaseDeviceUrl;
import com.vmware.vim25.HttpNfcLeaseInfo;
import com.vmware.vim25.HttpNfcLeaseState;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidNameFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.OutOfBoundsFaultMsg;
import com.vmware.vim25.OvfCreateImportSpecParams;
import com.vmware.vim25.OvfCreateImportSpecResult;
import com.vmware.vim25.OvfFileItem;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.TimedoutFaultMsg;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VmConfigFaultFaultMsg;

public class RouterHandler {
	private ServiceContent serviceContent;
	private VimPortType vimPort;
	private ManagedObjectReference graphFolderMor = null;
	private MOREFSelector mSelector;
	
	private HttpNfcLeaseExtender leaseExtender;
	private boolean vmdkFlag = false;
	private volatile long TOTAL_BYTES = 0;
    private volatile long TOTAL_BYTES_WRITTEN = 0;
    private String cookieValue = "";
	
    private static Logger logger = LoggerFactory.getLogger(RouterHandler.class);
	
	class HttpNfcLeaseExtender implements Runnable {
        private ManagedObjectReference httpNfcLease = null;
        private VimPortType vimPort = null;
        private int progressPercent = 0;

        public HttpNfcLeaseExtender(ManagedObjectReference mor, VimPortType vimport) {
            httpNfcLease = mor;
            vimPort = vimport;
        }

        @Override
        public void run() {
            try {
                while (!vmdkFlag) {
                    logger.info("\n\n#####################vmdk flag: "+ vmdkFlag + "\n\n");
                    if (TOTAL_BYTES != 0)
                    	progressPercent = (int) ((TOTAL_BYTES_WRITTEN * 100) / (TOTAL_BYTES));
                    try {
                        vimPort.httpNfcLeaseProgress(httpNfcLease, progressPercent);
                        Thread.sleep(29000);
                    } catch (InterruptedException e) {
                        logger.info("********************** Thread interrupted *******************");
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	
	public RouterHandler() { }
	
	public RouterHandler(VimPortType vimPort, ServiceContent serviceContent) {
		this.serviceContent = serviceContent;
		this.vimPort = vimPort;
		this.mSelector = new MOREFSelector(vimPort, serviceContent);
	}
		
	public void createVappRouter(String routerName, String dcName, String hostIp, String dataStore, String folderName, String localPath) {
		ManagedObjectReference rootFolder = this.serviceContent.getRootFolder();
		//Get Datacenter MOR
		ManagedObjectReference dcMor = this.mSelector.getMOREFByName(rootFolder, "Datacenter", dcName);
		if(dcMor == null) {
			logger.error("No datacenter exists of name " + dcName);
			return;
		}
		//Get Hostsystem MOR
		ManagedObjectReference hostMor = this.mSelector.getMOREFByName(rootFolder, "HostSystem", hostIp);
		if(hostMor == null) {
			logger.error("No host exists of name " + hostIp);
			return;
		}
		//Get Compute Resources
		ManagedObjectReference crMor = (ManagedObjectReference) this.mSelector.getEntityProps(hostMor, new String[] {"parent"}).get("parent");
		if(crMor == null) {
			logger.error("No Compute resources available");
			return;
		}
		//Get resource pool mor for compute resources and get vmFolder for dcmor
		ManagedObjectReference resourcePoolMor = (ManagedObjectReference) this.mSelector.getEntityProps(crMor, new String[] {"resourcePool"}).get("resourcePool");
		if(resourcePoolMor == null) {
			logger.error("No resources pool found");
			return;
		}
		//get vmFolderMor for datacenter
		ManagedObjectReference vmFolderMor = (ManagedObjectReference) this.mSelector.getEntityProps(dcMor, new String[] {"vmFolder"}).get("vmFolder");
		if(vmFolderMor == null) {
			logger.error("No vm folder");
			return;
		}
		else {
			logger.info("Vm Folder Found");
		}
		//get dsMor for datastore
		Map<String, Object> hostProps = this.mSelector.getEntityProps(hostMor, new String[]{"datastore", "parent"});
		List<ManagedObjectReference> dsList = ((ArrayOfManagedObjectReference) hostProps.get("datastore")).getManagedObjectReference();
		if(dsList.isEmpty()) {
			logger.error("Datastore Error");
			return;
		}
		ManagedObjectReference dsMor = null;
		for(ManagedObjectReference mor: dsList) {
			if(dataStore.equalsIgnoreCase((String)this.mSelector.getEntityProps(mor, new String[]{"name"}).get("name"))) {
                dsMor = mor;
                break;
            }
		}
		if(dsMor == null) {
			logger.error("Datastore not found");
		}
		if(this.graphFolderMor == null) {
			ArrayOfManagedObjectReference folderArray = (ArrayOfManagedObjectReference) this.mSelector.getEntityProps(vmFolderMor, new String[] {"childEntity"}).get("childEntity");
			List<ManagedObjectReference> folderList = folderArray.getManagedObjectReference();
			ManagedObjectReference customFolderMor = null;
			Iterator<ManagedObjectReference> i = folderList.iterator();
			while(i.hasNext()) {
				ManagedObjectReference tmpMor = i.next();
				if(tmpMor.getType().equals("Folder")) {
					String tmpName = (String)this.mSelector.getEntityProps(tmpMor, new String[] {"name"}).get("name");
					if(tmpName.equals(folderName)) {
						customFolderMor = tmpMor;
						logger.info("Folder found = " + tmpName);
						break;
					}
				}
			}
			if(customFolderMor == null) {
				logger.error("Deployment folder not found");
				return;
			}
			this.graphFolderMor = customFolderMor;
		}
		String ovfDescriptor = null;
		try {
			ovfDescriptor = this.getOvfDescriptorFromLocal(localPath);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		OvfCreateImportSpecParams importSpecParams = createImportSpecParams(hostMor, routerName);
		if (ovfDescriptor == null || ovfDescriptor.isEmpty()) {
			logger.error("Unable to configure router info");
            return;
        }
		OvfCreateImportSpecResult ovfImportResult = null;
		List<OvfFileItem> fileItemArr = null;
		try {
			ovfImportResult = this.vimPort.createImportSpec(serviceContent.getOvfManager(), ovfDescriptor, resourcePoolMor, dsMor, importSpecParams);
			fileItemArr = ovfImportResult.getFileItem();
            if (fileItemArr != null) {
                for (OvfFileItem fi : fileItemArr) {
                    TOTAL_BYTES += fi.getSize();
                }
            }
            logger.info("Total Number of bytes = " + TOTAL_BYTES);
		} catch (ConcurrentAccessFaultMsg | FileFaultFaultMsg | InvalidDatastoreFaultMsg | InvalidStateFaultMsg
				| RuntimeFaultFaultMsg | TaskInProgressFaultMsg | VmConfigFaultFaultMsg e) {
			e.printStackTrace();
			return;
		}
		try {
			ManagedObjectReference httpNfcLease = vimPort.importVApp(resourcePoolMor, ovfImportResult.getImportSpec(), this.graphFolderMor, hostMor);
			if(true == this.waitForEventToComplete(httpNfcLease)) {
				logger.info("Import Successful");
			}
			else {
				logger.info("Import Failed");
				return;
			}
			HttpNfcLeaseInfo httpNfcLeaseInfo =(HttpNfcLeaseInfo)this.mSelector.getEntityProps(httpNfcLease, new String[]{"info"}).get("info");
			leaseExtender = new RouterHandler().new HttpNfcLeaseExtender(httpNfcLease, vimPort);
            Thread t = new Thread(leaseExtender);
            t.start();
            List<HttpNfcLeaseDeviceUrl> deviceUrlArr = httpNfcLeaseInfo.getDeviceUrl();
            for (HttpNfcLeaseDeviceUrl deviceUrl : deviceUrlArr) {
                String deviceKey = deviceUrl.getImportKey();
                for (OvfFileItem ovfFileItem : fileItemArr) {
                    if (deviceKey.equals(ovfFileItem.getDeviceId())) {
                        logger.info("Import key: " + deviceKey);
                        logger.info("OvfFileItem device id: " + ovfFileItem.getDeviceId());
                        logger.info("HTTP Post file: " + ovfFileItem.getPath());
                        String absoluteFile = localPath.substring(0, localPath.lastIndexOf("\\"));
                        absoluteFile = absoluteFile + "/" + ovfFileItem.getPath();
                        logger.info("Absolute path: " + absoluteFile);
                        this.getVMDKFile(ovfFileItem.isCreate(), absoluteFile, deviceUrl.getUrl().replace("*", hostIp), ovfFileItem.getSize());
                        logger.info("Completed uploading the VMDK file");
                    }
                }
            }
            try {
            	vmdkFlag = true;
                t.interrupt();
				vimPort.httpNfcLeaseProgress(httpNfcLease, 100);
				vimPort.httpNfcLeaseComplete(httpNfcLease);
			} catch (TimedoutFaultMsg e) {
				e.printStackTrace();
			} catch (InvalidStateFaultMsg e) {
				e.printStackTrace();
			}
		} catch (DuplicateNameFaultMsg | FileFaultFaultMsg | InsufficientResourcesFaultFaultMsg
				| InvalidDatastoreFaultMsg | InvalidNameFaultMsg | OutOfBoundsFaultMsg | RuntimeFaultFaultMsg
				| VmConfigFaultFaultMsg e) {
			e.printStackTrace();
			return;
		}
	}
	
	private OvfCreateImportSpecParams createImportSpecParams(ManagedObjectReference host, String newVmName) {
        OvfCreateImportSpecParams importSpecParams = new OvfCreateImportSpecParams();
        importSpecParams.setHostSystem(host);
        importSpecParams.setLocale("");
        importSpecParams.setEntityName(newVmName);
        importSpecParams.setDeploymentOption("");
        return importSpecParams;
    }
	
	private String getOvfDescriptorFromLocal(String ovfDescriptorUrl) throws IOException {
        StringBuffer strContent = new StringBuffer("");
        int x;
        try {
            InputStream fis = new FileInputStream(ovfDescriptorUrl);
            if (fis != null) {
                while ((x = fis.read()) != -1) {
                    strContent.append((char) x);
                }
            }
            fis.close();
        } catch (FileNotFoundException e) {
            System.out.println("Invalid local file path");
        }
        return strContent + "";
    }
	
	private boolean getResultsForTask(ManagedObjectReference task) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg {
        boolean retVal = false;
        WaitForValues waitForValues = new WaitForValues(this.vimPort, this.serviceContent);
        Object[] result = waitForValues.wait(task, new String[]{"state"}, new String[]{"state"}, new Object[][]{new Object[]{HttpNfcLeaseState.READY, HttpNfcLeaseState.ERROR}});
        if (result[0].equals(HttpNfcLeaseState.READY)) {
            retVal = true;
        }
        return retVal;
    }
	
	private boolean waitForEventToComplete(ManagedObjectReference task) {
		boolean ret = false;
		try {
			ret = this.getResultsForTask(task);
		} catch (InvalidPropertyFaultMsg | RuntimeFaultFaultMsg | InvalidCollectorVersionFaultMsg e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	private void getVMDKFile(boolean put, String fileName, String uri, long diskCapacity) {
	   HttpsURLConnection conn = null;
	   BufferedOutputStream bos = null;
	   int bytesRead, bytesAvailable, bufferSize;
	   byte[] buffer;
	   int maxBufferSize = 64 * 1024;
	   try {
	      System.out.println("Destination host URL: " + uri);
	      HostnameVerifier hv = new HostnameVerifier() {
	         @Override
	         public boolean verify(String urlHostName, SSLSession session) {
	            System.out.println("Warning: URL Host: " + urlHostName + " vs. "
	            + session.getPeerHost());
	            return true;
	         }
	      };
	      HttpsURLConnection.setDefaultHostnameVerifier(hv);
	      URL url = new URL(uri);
	      conn = (HttpsURLConnection) url.openConnection();
	
	      // Maintain session
	      List<String> cookies = (List<String>)conn.getHeaderFields().get("Set-cookie");
	      cookieValue = cookies.get(0);
	      StringTokenizer tokenizer = new StringTokenizer(cookieValue, ";");
	      cookieValue = tokenizer.nextToken();
	      String path = "$" + tokenizer.nextToken();
	      String cookie = "$Version=\"1\"; " + cookieValue + "; " + path;
	
	      // set the cookie in the new request header
	      Map<String, List<String>> map = new HashMap<String, List<String>>();
	      map.put("Cookie", Collections.singletonList(cookie));
	      ((BindingProvider) vimPort).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, map);
	      conn.setDoInput(true);
	      conn.setDoOutput(true);
	      conn.setUseCaches(false);
	      conn.setChunkedStreamingMode(maxBufferSize);
	      if (put) {
	         conn.setRequestMethod("PUT");
	         System.out.println("HTTP method: PUT");
	      }
	      else {
	         conn.setRequestMethod("POST");
	         System.out.println("HTTP method: POST");
	      }
	      conn.setRequestProperty("Cookie", cookie);
	      conn.setRequestProperty("Connection", "Keep-Alive");
	      conn.setRequestProperty("Content-Type", "application/x-vnd.vmware-streamVmdk");
	      conn.setRequestProperty("Content-Length", String.valueOf(diskCapacity));
	      conn.setRequestProperty("Expect", "100-continue");
	      bos = new BufferedOutputStream(conn.getOutputStream());
	      System.out.println("Local file path: " + fileName);
	      InputStream io = new FileInputStream(fileName);
	      BufferedInputStream bis = new BufferedInputStream(io);
	      bytesAvailable = bis.available();
	      System.out.println("vmdk available bytes: " + bytesAvailable);
	      bufferSize = Math.min(bytesAvailable, maxBufferSize);
	      buffer = new byte[bufferSize];
	      bytesRead = bis.read(buffer, 0, bufferSize);
	      long bytesWrote = bytesRead;
	      TOTAL_BYTES_WRITTEN += bytesRead;
	      while (bytesRead >= 0) {
	         bos.write(buffer, 0, bufferSize);
	         bos.flush();
	         System.out.println("Bytes Wrote: " + bytesWrote);
	         bytesAvailable = bis.available();
	         bufferSize = Math.min(bytesAvailable, maxBufferSize);
	         bytesWrote += bufferSize;
	         System.out.println("Total bytes written: " + TOTAL_BYTES_WRITTEN);
	         TOTAL_BYTES_WRITTEN += bufferSize;
	         buffer = null;
	         buffer = new byte[bufferSize];
	         bytesRead = bis.read(buffer, 0, bufferSize);
	         System.out.println("Bytes Read: " + bytesRead);
	         if ((bytesRead == 0) && (bytesWrote >= diskCapacity)) {
	            System.out.println("Total bytes written: " + TOTAL_BYTES_WRITTEN);
	            bytesRead = -1;
	         }
	      }
	      try {
	         DataInputStream dis = new DataInputStream(conn.getInputStream());
	         dis.close();
	      } catch (SocketTimeoutException stex) {
	         System.out.println("From (ServerResponse): " + stex);
	      } catch (IOException ioex) {
	         System.out.println("From (ServerResponse): " + ioex);
	      }
	      System.out.println("Writing vmdk to the output stream done");
	      bis.close();
	   } catch (MalformedURLException ex) {
	      ex.printStackTrace();
	   } catch (IOException ioe) {
	      ioe.printStackTrace();
	   } finally {
	      try {
	         bos.flush();
	         bos.close();
	         conn.disconnect();
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
	   }
	}

}
