package ind.com.oracle.finalreport;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Stream;

public class Computation {
	
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
	
	private static Properties getProperties() {
		Properties prop = new Properties();
		try{
		System.out.println(System.getProperty("user.dir"));
        String fileName = "config.properties";
        ClassLoader classLoader = Computation.class.getClassLoader();

        URL res = Objects.requireNonNull(classLoader.getResource(fileName),"Can't find configuration file. Place it at proper place");
        
        InputStream is = new FileInputStream(res.getFile());
        
        prop.load(is);

		}
		catch(Exception e){
			System.out.print(e);
		}
		return prop;
	}
	
	public List<HashMap<String, TransactionContentPOJO>> fetchFilesSetPOJO(List<Path> files) {
		HashMap<String, TransactionContentPOJO> transRestMapping;
		List<HashMap<String, TransactionContentPOJO>> transContentList = new ArrayList<HashMap<String, TransactionContentPOJO>>();
		
		List<Path> csvfiles = new ArrayList<Path>();
		
		for(int i=0; i<files.size(); i++) {
			if(files.get(i).getFileName().toString().endsWith(".csv")) {
				csvfiles.add(files.get(i));
			}
		}
		
		for(int i=0; i<csvfiles.size(); i++) {
			
			transRestMapping = new HashMap<String, TransactionContentPOJO>();
			
			try (BufferedReader br = Files.newBufferedReader(csvfiles.get(i),
	                StandardCharsets.US_ASCII)) {
				String line = br.readLine();
				
				line = br.readLine();
				
				String transactionName = null;
				double idv_restTime;
				double FA_server_RT;
				double OHS_server_RT;
				double DCS_server_RT;
				double VBCSOverHead;
				double uiTime;
				double restTotalTime = 0;
				
				while (line != null) {
					String[] metadata = line.split("\t");
					
					List<RESTContentPOJO> restContentList = null;
					RESTContentPOJO restContent;
					TransactionContentPOJO transContent = null;
					
					String DCS_server_RT_str;
					String OHS_server_RT_str;
					String FA_server_RT_str;
					String VBCSOverHead_str;
					if(!metadata[0].equals("")){
						
						restContentList =null;
						restContent = null;
						
						if(metadata.length > 2) {
						DCS_server_RT_str = metadata[5].equals("null") ? "-999" : metadata[5];
						OHS_server_RT_str = metadata[6].equals("null") ? "-999" : metadata[6];
						FA_server_RT_str = 	metadata[7].equals("null") ? "-999" : metadata[7];
						VBCSOverHead_str =  metadata[8].equals("NA") ? "-999" : metadata[8];
						idv_restTime = new Double (metadata[4]);
						DCS_server_RT = new Double (DCS_server_RT_str);
						OHS_server_RT = new Double (OHS_server_RT_str);
						FA_server_RT = new Double (FA_server_RT_str);
						VBCSOverHead = new Double (VBCSOverHead_str);
						restContent = new RESTContentPOJO(metadata[2], idv_restTime, FA_server_RT, OHS_server_RT, DCS_server_RT, VBCSOverHead);
						restContentList = new ArrayList<RESTContentPOJO>();
						restContentList.add(restContent);
						}

						uiTime = new Double (metadata[1]);
						if(metadata.length > 9){
							restTotalTime = new Double (metadata[9]);
						} else {
							restTotalTime = -999;
						}
						transactionName = metadata[0];
						transContent = new TransactionContentPOJO(transactionName, uiTime, restTotalTime, restContentList);
						
						transRestMapping.put(transactionName, transContent);
					} else {
						DCS_server_RT_str = metadata[5].equals("null") ? "-999" : metadata[5];
						OHS_server_RT_str = metadata[6].equals("null") ? "-999" : metadata[6];
						FA_server_RT_str = 	metadata[7].equals("null") ? "-999" : metadata[7];
						VBCSOverHead_str =  metadata[8].equals("NA") ? "-999" : metadata[8];						
						idv_restTime = new Double (metadata[4]);
						DCS_server_RT = new Double (DCS_server_RT_str);
						OHS_server_RT = new Double (OHS_server_RT_str);
						FA_server_RT = new Double (FA_server_RT_str);
						VBCSOverHead = new Double (VBCSOverHead_str);
						restContent = new RESTContentPOJO(metadata[2], idv_restTime, FA_server_RT, OHS_server_RT, DCS_server_RT, VBCSOverHead);
						
						transRestMapping.get(transactionName).getRestContent().add(restContent);
					}
					
					line = br.readLine();
					
				}
			}
			catch (IOException ioe) {
	            ioe.printStackTrace();
	        }
			transContentList.add(transRestMapping);			
		}
		return transContentList;
	}
	
	public HashMap<String, ArrayList<TransactionContentPOJO>> listAllTransactionValues(List<HashMap<String, TransactionContentPOJO>> transContentList) {
		HashMap<String, ArrayList<TransactionContentPOJO>> combined = new HashMap<String, ArrayList<TransactionContentPOJO>>();
		ArrayList<TransactionContentPOJO> al = null;
		for(int i=0; i<transContentList.size(); i++) {
			HashMap<String, TransactionContentPOJO> iterateEachResult = transContentList.get(i);
			for(String key : iterateEachResult.keySet()){
				
				if(combined.keySet().contains(key)){
					al = combined.get(key);
				}else {					
					al = new ArrayList<TransactionContentPOJO>();
				}
				
				al.add(iterateEachResult.get(key));
				combined.put(key, al);
			}

		}
		return combined;
	}
	
	public TreeMap<String, ArrayList<TransactionContentPOJO>> sortAllTransactionValues(HashMap<String, ArrayList<TransactionContentPOJO>> combinedResults) {
		HashMap<String, ArrayList<TransactionContentPOJO>> sorted = new HashMap<String, ArrayList<TransactionContentPOJO>>();
		ArrayList<TransactionContentPOJO> transPOJOList = null;
		for(String key : combinedResults.keySet()){
			transPOJOList = combinedResults.get(key);
			 Collections.sort(transPOJOList, TransactionContentPOJO.UIResponseTimeComparator);
			 sorted.put(key, transPOJOList);
		}
		TreeMap<String, ArrayList<TransactionContentPOJO>> transSorted = new TreeMap<>(sorted);
		return transSorted;
	}
	
	public TreeMap<String, ArrayList<TransactionContentPOJO>> computeStdDev(TreeMap<String, ArrayList<TransactionContentPOJO>> transSorted){
		TreeMap<String, ArrayList<TransactionContentPOJO>> computeStdDev = new TreeMap<String, ArrayList<TransactionContentPOJO>>();
		ArrayList<TransactionContentPOJO> transPOJOList = null;
		for(String key : transSorted.keySet()){
			transPOJOList = transSorted.get(key);
			double uiTime = 0, uiStdDev = 0;
			double restTime = 0, restStdDev = 0;
			double uiTimeMean = 0, restTimeMean;
			double uiTimeMeanSquare, restTimeMeanSquare;
			double uiTimeSumMeanSquare = 0, restTimeSumMeanSquare = 0;
			double uiTimeVariance = 0, restTimeVariance = 0;
			int sampleCount;
			
			for(TransactionContentPOJO transPOJO : transPOJOList){
				uiTime += transPOJO.getUiTime();
				restTime += transPOJO.getRestTotalTime();
			}
			
			sampleCount = transPOJOList.size();
			
			uiTimeMean = (double) uiTime/sampleCount;
			
			restTimeMean = (double) restTime/sampleCount;
			
			for(TransactionContentPOJO transPOJO : transPOJOList){
				uiTimeMeanSquare = Math.pow((transPOJO.getUiTime() - uiTimeMean), 2);
				uiTimeSumMeanSquare += uiTimeMeanSquare;
				
				restTimeMeanSquare = Math.pow((transPOJO.getRestTotalTime() - restTimeMean), 2);
				restTimeSumMeanSquare += restTimeMeanSquare;
			}			
			
			uiTimeVariance = uiTimeSumMeanSquare/(sampleCount - 1);
			
			restTimeVariance = restTimeSumMeanSquare/(sampleCount - 1);
			
			uiStdDev = Math.sqrt(uiTimeVariance);
			
			restStdDev =  Math.sqrt(restTimeVariance);
			
			for(int i=0; i<transPOJOList.size(); i++){		
			transPOJOList.get(i).setUiStdDev(uiStdDev);
			
			transPOJOList.get(i).setRestStdDev(restStdDev);
			}
			
			computeStdDev.put(key, transPOJOList);
		}
		
		return computeStdDev;
	}

}