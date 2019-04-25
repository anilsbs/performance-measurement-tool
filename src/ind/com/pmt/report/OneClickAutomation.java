package ind.com.oracle.report;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import ind.com.oracle.finalreport.FinalReport;
import ind.com.oracle.hartools.har2csv;
import jxl.write.WritableSheet;
import jxl.write.WriteException;


public class OneClickAutomation {
	
	//private static  FileContentWriter fileContentWriter;

	public static void main(String[] args) throws IOException, WriteException {
		// TODO Auto-generated method stub
		
		
		Properties prop = getProperties();
		
		String JenkinsRun = prop.getProperty("IsJenkinsRun");
		Boolean isJenkinsRun = Boolean.parseBoolean(JenkinsRun);
		
		if(isJenkinsRun==true)
		{
			int iarglenth = args.length;
			System.out.println("No of arguments : "+iarglenth);
			
			String buildnumber = args[0];
			System.out.println("Build number : "+buildnumber);
			
			String jobname = args[1];
			System.out.println("Jenkins Job Name : "+jobname);
			
		
			String FolderName = "DCS_SingleUser_Build"+buildnumber;
			String HARFolderPath = "C:\\hudson\\workspace\\"+jobname+"\\"+FolderName;
	     
	        
	        System.out.println(HARFolderPath);
	        
	        File folder = new File(HARFolderPath);
	        File[] listOfFiles = folder.listFiles();
	        
	       String OutputFilePath = "C:\\hudson\\workspace\\"+jobname+"\\"+FolderName+"\\Output";
	        File dir = new File(OutputFilePath);
	        if(!dir.exists()){

	            dir.mkdir();
	          
	         }
	      
	        
	        String LogFilePath = "C:\\hudson\\workspace\\"+jobname+"\\"+FolderName+"\\Logs";
	        File dir1 = new File(LogFilePath);
	        dir1.mkdir();
	        
	        for (int f = 0; f < listOfFiles.length; f++) {

	  		  if (!listOfFiles[f].isFile() && listOfFiles[f].getName().startsWith("Iteration")) {
	  	
	  			  try {
	  				String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
	  				
	  				
	  				String inputfolder = HARFolderPath+"\\"+listOfFiles[f].getName();
	  				
	  				
	  				String outputfolder = OutputFilePath;
	  				
	  				
	  				String logsfolder = LogFilePath;
	  				ExecuteMainCode(inputfolder,outputfolder,logsfolder,buildnumber, listOfFiles[f].getName());
	  				
	  			  }
	  			 catch (Exception e)
	      	        {
	        			  //fileContentWriter.closeCSVFile();
	      	        	continue;
	      	        	
	      	        }
	  		  }
	        }
	        FinalReport finalReport = new FinalReport();
			
			finalReport.execute(prop,buildnumber,jobname);       
			
		}
		else
		{
		String inputfolder = prop.getProperty("InputFolder");
		
		
		String outputfolder = prop.getProperty("OutputFolder");
		
		
		String logsfolder = prop.getProperty("LogsFolder");
		
		//String buildnumber = "0";
		//String jobname = "No_Job";
		
		String buildnumber = "DEMO01";
		String jobname = "IT02";
		
		ExecuteMainCode(inputfolder,outputfolder,logsfolder,buildnumber,jobname);
		
        FinalReport finalReport = new FinalReport();
		
		finalReport.execute(prop,buildnumber,jobname);
		
		}
        
		
	}
	 private static void ExecuteMainCode(String inputfolder,String outputfolder,String logsfolder,String buildnumber,String jobname) throws  IOException, WriteException
	 {

		Properties prop = getProperties();
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd;HH.mm.ss").format(new Date());
		
		if(Boolean.parseBoolean(prop.getProperty("SaveToApex"))) {
			FileContentWriter.setRunInfoInApex(prop, buildnumber, jobname);
		}
		
		String[] transNames = prop.getProperty("TransactionNames").split(",");
		
		String[] AsynUrlsWords = prop.getProperty("AsyncWords").split(",");
		
		String filterRESTName = prop.getProperty("FilterRESTName");
		
		HashMap mapTransNamesAsynUrlsWords = new HashMap();
		
		if(transNames.length != AsynUrlsWords.length){
			System.out.println("WARNING : Number of Transaction Names and Number of Async URL Words are not matching");
			System.exit(0);
		} else {
			for(int j=0; j<transNames.length; j++) {
				mapTransNamesAsynUrlsWords.put(transNames[j].trim(), AsynUrlsWords[j].trim());
			}
		}
		
		String resFileName = outputfolder+"/Results."+ timeStamp +".csv";
		String fileName = null;
		long restRT = 0;		
		
		ParseFileContent parseFileContent = new ParseFileContent();
		
		String hostname = prop.getProperty("HostName");		
		String copyFrom = prop.getProperty("AccessLogFolder");
        String username = prop.getProperty("UserName");
        String password = prop.getProperty("Password");

		parseFileContent.copyLogFilefromLinux(hostname,username,password,copyFrom,logsfolder);
        File folder = new File(inputfolder);
        File[] listOfFiles = folder.listFiles();
		List<Path> files = parseFileContent.listFilesInFolder(inputfolder);
		List<String> harfiles = new ArrayList<String>();
		
		for(int i=0; i<files.size(); i++) {
			if(files.get(i).getFileName().toString().endsWith(".har")) {
				harfiles.add(files.get(i).getFileName().toString());
			}
		}
		
		har2csv har2csv = new har2csv();
		
		String har2csvArgs [] = {"HAR2CSV","--in","InputFileName","--out","OutputFileName"};
		
		for(int i=0; i<harfiles.size(); i++) {

		har2csvArgs[2] = inputfolder+"/"+harfiles.get(i).toString();
	
		har2csvArgs[4] = inputfolder+"/"+harfiles.get(i).toString().replace(".har", ".csv");
		
		har2csv.gethar2csv(har2csvArgs);
		
		}

		parseFileContent = new ParseFileContent();
        
		files = parseFileContent.listFilesInFolder(inputfolder);		
		List<Path> csvfiles = new ArrayList<Path>();
		
		for(int i=0; i<files.size(); i++) {
			if(files.get(i).getFileName().toString().endsWith(".csv")) {
				csvfiles.add(files.get(i));
			}
		}

		FileContentWriter fileContentWriter = new FileContentWriter();
		fileContentWriter.writeFileHeaders(resFileName);
		
		for(int i=0; i<csvfiles.size(); i++) {
		fileName = (csvfiles.get(i).getFileName().toString()).split(".csv")[0];
		
		//fileName = fileName.substring(0, fileName.indexOf("."));
		
		List<FileContentPOJO> fileContentList = parseFileContent.readContentFromCSV(csvfiles.get(i), logsfolder);
		
		List<FileContentPOJO> filteredAsyncurls = parseFileContent.filteredAsyncurls(fileContentList, fileName, mapTransNamesAsynUrlsWords);

		long totalExecutionTime = parseFileContent.totalExecutionTime(filteredAsyncurls);
		
		List<FileContentPOJO> filteredResturls = parseFileContent.filterRestUrls(filterRESTName, filteredAsyncurls);
		
		if(!filteredResturls.isEmpty()) {
		restRT = parseFileContent.calcRestRT(filteredResturls);
		}
		
		parseFileContent.setUrlName(filteredResturls);
		if(new Boolean(prop.getProperty("SortResults"))){
			Collections.sort(filteredResturls,FileContentPOJO.NameComparator);
		}
		parseFileContent.readContentAccessLog(filterRESTName, filteredResturls, logsfolder);
		
		fileContentWriter.writeCSVFile(prop, fileName, totalExecutionTime, restRT, filteredResturls, buildnumber, jobname);
		}
		
		fileContentWriter.closeCSVFile();

	}	
	
	private static Properties getProperties() {
		Properties prop = new Properties();
		try{
	        File jarPath=new File(OneClickAutomation.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	        String propertiesPath=jarPath.getParent();
	        prop.load(new FileInputStream(propertiesPath+"/config.properties"));
		}
		catch(Exception e){
			System.out.print(e);
		}
		return prop;
	}
}
