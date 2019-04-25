package ind.com.oracle.finalreport;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;

import jxl.write.WriteException;

public class FinalReport {

	public void execute(Properties prop,String buildnumber,String jobname) throws WriteException, IOException {
		
		
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd;HH.mm.ss").format(new Date());
		 String inputfolder,resultfolder;
		String JenkinsRun = prop.getProperty("IsJenkinsRun");
		Boolean isJenkinsRun = Boolean.parseBoolean(JenkinsRun);
		
		if(isJenkinsRun==true)
		{
			String FolderName = "DCS_SingleUser_Build"+buildnumber;
	        String HARFolderPath = "C:\\Jenkins\\workspace\\"+jobname+"\\"+FolderName;
	        String ReportFilePath = "C:\\Jenkins\\workspace\\"+jobname+"\\"+FolderName+"\\FinalReport";
	        File dir = new File(ReportFilePath);
	        if(!dir.exists()){
	            dir.mkdir();
	         }
	     
	         System.out.println(ReportFilePath);
			 inputfolder = HARFolderPath+"\\Output";
			 resultfolder = ReportFilePath;
		}
		else
		{
	         inputfolder = prop.getProperty("OutputFolder");	        
			 resultfolder = prop.getProperty("ResultsFolder");
			
			 File dir = new File(resultfolder);
			 if(!dir.exists()){
	            dir.mkdir();   
	         }
		
		}
		String resFileName = resultfolder+"/AutomationFinalReport-MinValues-"+ timeStamp +".xls";
	
		Computation compute = new Computation();
		
		List<Path> files = compute.listFilesInFolder(inputfolder);
		
		List<HashMap<String, TransactionContentPOJO>> transContentList = compute.fetchFilesSetPOJO(files);
		
		HashMap<String, ArrayList<TransactionContentPOJO>> combinedResults = compute.listAllTransactionValues(transContentList);
		
		TreeMap<String, ArrayList<TransactionContentPOJO>> sortResults = compute.sortAllTransactionValues(combinedResults);
		
		TreeMap<String, ArrayList<TransactionContentPOJO>> computeStdDev = compute.computeStdDev(sortResults);
		
		ExcelFileContentWriter excelFileWriterMinValue = new ExcelFileContentWriter(prop, resFileName, computeStdDev, false);
		
		resFileName = resultfolder+"/AutomationFinalReport-MedianValues-"+ timeStamp +".xls";
		
		ExcelFileContentWriter excelFileWriterMedianValue = new ExcelFileContentWriter(prop, resFileName, computeStdDev, true);
		
	}
	/*private static Properties getProperties() {
		Properties prop = new Properties();
		try{
	        File jarPath=new File(FinalReport_Demo.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	        //String propertiesPath=jarPath.getParentFile().getParent();
	        String propertiesPath=jarPath.getParent();
	        prop.load(new FileInputStream(propertiesPath+"/config.properties"));
		}
		catch(Exception e){
			System.out.print(e);
		}
		return prop;
	} */
}
