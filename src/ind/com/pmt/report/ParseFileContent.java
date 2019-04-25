package ind.com.oracle.report;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class ParseFileContent {
	
	public void copyLogFilefromLinux(String hostname, String username, String password, String copyFrom, String copyTo) {
        JSch jsch = new JSch();
        Session session = null;
        
        System.out.println("Trying to connect the server to fetch logs.....");
        try {   	
            session = jsch.getSession(username, hostname, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel; 
            sftpChannel.get(copyFrom, copyTo);
            sftpChannel.exit();
            session.disconnect();
            System.out.println("Log files copied successfully!!");            
        } catch (JSchException e) {
            System.out.println("Server connection failed.....");        	
           // e.printStackTrace();  
        } catch (SftpException e) {
        	System.out.println("Server connection failed.....");  	
            //e.printStackTrace();
        }		
	}
	
	public List<Path> listFilesInFolder(String folder) {
		List<Path> files = new ArrayList<Path>();
		try(Stream<Path> paths = Files.walk(Paths.get(folder))) {
			
		    paths.forEach(filePath -> {
		        if (Files.isRegularFile(filePath)) {
		        	files.add(filePath);
		        }
		    });
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return files;
	}		
	
	public List<FileContentPOJO> readContentFromCSV(Path pathToFile, String logsfolder) { 
		List<FileContentPOJO> fileContentList = new ArrayList(); 
		//Path pathToFile = Paths.get(path);
		
		try (BufferedReader br = Files.newBufferedReader(pathToFile,
                StandardCharsets.US_ASCII)) {
			String line = br.readLine();
			
			line = br.readLine();
			
			while (line != null) {
				String[] attributes = line.split("\t");
				
				FileContentPOJO fileContent = createRecord(attributes, logsfolder);
				
				fileContentList.add(fileContent);
				
				line = br.readLine();
				
			} 
		}
		catch (IOException ioe) {
            ioe.printStackTrace();
        }
		
		return fileContentList;
	}
	
	private FileContentPOJO createRecord(String[] metadata, String logsfolder) {
		String urlContent = metadata[0];
		String test = null;
		String readStartDate = metadata[2].replace("Z", "+0000");
		if(readStartDate.length() > 28){
			readStartDate = readStartDate.substring(0, 26) + "" + readStartDate.substring(27,29);
        }
		
		String milliseconds = (metadata[3]).split("\\.")[0];
		int duration = Integer.parseInt(milliseconds);
		String ecid = (metadata[10]).split(",")[0];
		String method = metadata[1];
		String size = metadata[8];
		
		long urlExecTime = 0;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"); 
		Date startDateTime = null, endDatetime = null;
		try {		
			startDateTime = format.parse(readStartDate);		//2017-04-26 T 09:54:52.227+0000
			endDatetime = calculateEndDateTime(startDateTime, duration);	
			urlExecTime = endDatetime.getTime() - startDateTime.getTime();
		} 
		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		FileContentPOJO fileContent = new FileContentPOJO(urlContent, null, startDateTime, duration, ecid, endDatetime, urlExecTime, size, null, null, null, method);

		return fileContent;
			
	}

	private Date calculateEndDateTime(Date startDateTime, int duration) {
	      if (startDateTime == null) {
	          throw new IllegalArgumentException("The date must not be null");
	      }
	      Calendar cal = Calendar.getInstance();
	      cal.setTime(startDateTime);
	      cal.add(Calendar.MILLISECOND, duration);
	      return cal.getTime();
	}
	
	public void setUrlName(List<FileContentPOJO> filteredResturls) {
		  for(int i=0; i<filteredResturls.size(); i++) {
			  	String urlContent = filteredResturls.get(i).getUrl();
				String name = urlContent.substring(urlContent.lastIndexOf("/") + 1, (urlContent.indexOf("?") != -1) ? urlContent.indexOf("?") : urlContent.length());		
				Pattern pattern = Pattern.compile("([0-9])");
				Matcher match = pattern.matcher(name);
				if(match.find()){
					name = urlContent.substring((urlContent.substring(0, urlContent.lastIndexOf("/")).lastIndexOf("/")) + 1 , urlContent.lastIndexOf("/")) ; //Find the second occurrence of slash from last.
				}		  
				filteredResturls.get(i).setName(name);
		  }
	}
	
	public long totalExecutionTime(List<FileContentPOJO> fileContentList) {
		  long totalExecutionTime;
		  long minStartTimeInMills = fileContentList.get(0).getstartDateTime().getTime();
		  long maxEndTimeInMills = fileContentList.get(0).getendDateTime().getTime();
		  
		  for(int i=0; i<fileContentList.size(); i++) {
			//if(!fileContentList.get(i).getUrl().contains("catalogProductGroups")) {
			  long startTimeInMills = fileContentList.get(i).getstartDateTime().getTime();
			  if(startTimeInMills < minStartTimeInMills) {
				  minStartTimeInMills = startTimeInMills;
			  }
			  long endTimeInMills = fileContentList.get(i).getendDateTime().getTime();
			  if(endTimeInMills > maxEndTimeInMills) {
				  maxEndTimeInMills = endTimeInMills;
			  }
			//}
		  } 
		  
		  totalExecutionTime = maxEndTimeInMills - minStartTimeInMills;
		  System.out.println("The URL total execution time : " + totalExecutionTime + " ms");
		  return totalExecutionTime;
	}
	
	public List<FileContentPOJO> filteredAsyncurls(List<FileContentPOJO> fileContentList,String fileName, HashMap mapTransNamesAsynUrlsWords) {
			List<FileContentPOJO> filteredAsyncurls = new ArrayList<FileContentPOJO>();
			if(mapTransNamesAsynUrlsWords.keySet().contains(fileName)) {
			//if(mapTransNamesAsynUrlsWords.keySet().toString().startsWith(fileName)) {
				String AsynWord = (String) mapTransNamesAsynUrlsWords.get(fileName);
		mainloop: for(int i=0; i<fileContentList.size(); i++) {
					if(!AsynWord.equals("") && fileContentList.get(i).getUrl().contains(AsynWord) )	{
						filteredAsyncurls.add(fileContentList.get(i));
						break mainloop;
					}
					else {
						filteredAsyncurls.add(fileContentList.get(i));
					}
				}
			}
			else {
				filteredAsyncurls = fileContentList;
			}
			return filteredAsyncurls;
	}

	public List<FileContentPOJO> filterRestUrls(String filterRESTName, List<FileContentPOJO> filteredAsyncurls) {
		List<FileContentPOJO> filteredResturls = new ArrayList<FileContentPOJO>();
		
		for(int i=0; i<filteredAsyncurls.size(); i++) {
			if(filteredAsyncurls.get(i).getMethod().equals("DELETE")) 	{
				if(filteredAsyncurls.get(i).getUrl().contains(filterRESTName) && !filteredAsyncurls.get(i).getSize().equals("0") && !filteredAsyncurls.get(i).getUrl().endsWith(".json") ) {
					//if(!fileContentList.get(i).getUrl().contains("catalogProductGroups")) {
					// || fileContentList.get(i).getUrl().contains("/api/")
						filteredResturls.add(filteredAsyncurls.get(i));
					//}
				}
			}
			else {
				if((filteredAsyncurls.get(i).getUrl().contains(filterRESTName) || filteredAsyncurls.get(i).getUrl().contains("/api/")) && !filteredAsyncurls.get(i).getSize().equals("0") && !filteredAsyncurls.get(i).getUrl().endsWith(".json") ) {
					filteredResturls.add(filteredAsyncurls.get(i));
				}
			}
			
		}
		return filteredResturls;
	}
	
	public long calcRestRT(List<FileContentPOJO> filteredResturls) {
		long nonExecTime = 0;
		Collections.sort(filteredResturls);
		long restRT = totalExecutionTime(filteredResturls);		
		
		for(int i=0 ; i < filteredResturls.size() - 1; i++ ) {  //Last row end time cannot be compared with next row start time. TADAA as there will be no more rows
			if(filteredResturls.get(i).getendDateTime().getTime() < filteredResturls.get(i + 1).getstartDateTime().getTime()) {
				nonExecTime += filteredResturls.get(i + 1).getstartDateTime().getTime() - filteredResturls.get(i).getendDateTime().getTime();
			} 
		}

		System.out.println("The REST execution time : " + restRT + " ms");
		
		restRT -= nonExecTime;
		
		System.out.println("The total non execution Time between REST calls : " + nonExecTime  + " ms");
		
		System.out.println("The REST execution time after removing non execution time : " + restRT + " ms");
		
		return restRT;
	}
	
	public void readContentAccessLog(String filterRESTName, List<FileContentPOJO> filteredResturls, String logsfolder) { 
		
		List<Path> logfiles = listFilesInFolder(logsfolder);
		HashMap<String, FileContentPOJO> resturlMap = new HashMap<String, FileContentPOJO>();
		for (FileContentPOJO resturls: filteredResturls) { 
			resturlMap.put(resturls.getECID(), resturls); 
			}
		
		Set ecidSet = resturlMap.keySet();
		
		for(int i=0; i<logfiles.size(); i++) {
			try (BufferedReader br = Files.newBufferedReader(logfiles.get(i),
	                StandardCharsets.US_ASCII)) {
				String line = br.readLine();
				while (line != null) {
					if((line.contains(filterRESTName) || line.contains("api"))) {
						// && line.contains("SrMilestoneService")
						String[] logAttributes ={""};
						if(logfiles.get(i).getFileName().toString().startsWith("access.log.")) {
							logAttributes  = 	line.split("\t");
							String accessLogECID = logAttributes[7].replace("\"","");
							if (accessLogECID != "" && ecidSet.contains(accessLogECID)) {
								FileContentPOJO fileContPOJOObj = (FileContentPOJO)resturlMap.get(accessLogECID);
								fileContPOJOObj.setFA_server_RT(logAttributes[2]);								
							}
						} else if(logfiles.get(i).getFileName().toString().startsWith("access_log")) {
							logAttributes  = 	line.split(" ");
							String accessLogECID = logAttributes[12];
							if (accessLogECID != "" && ecidSet.contains(accessLogECID)) {
								FileContentPOJO fileContPOJOObj = (FileContentPOJO)resturlMap.get(accessLogECID);
								double calcRT = (Double.parseDouble(logAttributes[11]))/1000000;							
								fileContPOJOObj.setOHS_server_RT(String.format("%.3f", calcRT));								
							}
						} else {
							logAttributes  = 	line.split("\t");
							String accessLogECID = logAttributes[7].replace("\"","");
							if (accessLogECID != "" && ecidSet.contains(accessLogECID)) {
								FileContentPOJO fileContPOJOObj = (FileContentPOJO)resturlMap.get(accessLogECID);
								fileContPOJOObj.setDCS_server_RT(logAttributes[2]);						
							}
						}	
					}
					line = br.readLine();
				}
			}
			catch (IOException ioe) {
	            ioe.printStackTrace();
	        }
		}
	}	

}
