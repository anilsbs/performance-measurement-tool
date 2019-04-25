package ind.com.oracle.report;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.wink.json4j.JSONObject;

public class FileContentWriter {

	//Delimiter used in CSV file
	private final String TAB_DELIMITER = "\t";
	private final String NEW_LINE_SEPARATOR = "\r\n";
	
	FileWriter fileWriter = null;

	public void writeFileHeaders(String fileName) {
		//CSV file header
		String FILE_HEADER = "Name\tUI Response Time\tREST Name\tECID\tREST Response Time\tDCS_server_RT\tOHS_server_RT\tFA_server_RT\tVBCS Overhead in %\tREST E2E Time";
		try {
			fileWriter = new FileWriter(fileName);

			//Write the CSV file header
			fileWriter.append(FILE_HEADER.toString());
			
			//Add a new line separator after the header
			fileWriter.append(NEW_LINE_SEPARATOR);	
			
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		}
	}

	public void writeCSVFile(Properties prop, String fileName, long totalExecutionTime, long restRT, List<FileContentPOJO> filteredResturls,String buildnumber,String jobname) {
		double uiTime = (double) totalExecutionTime/1000;
		double restTotalTime = (double) restRT/1000;		
		double abcOverHead = new BigDecimal((uiTime - restTotalTime) * 100 / uiTime).round(new MathContext(4)).doubleValue(); 
	
		try {
			//Write a new URL object list to the CSV file

			fileWriter.append(fileName);
			fileWriter.append(TAB_DELIMITER);
			fileWriter.append(String.valueOf(uiTime));
			fileWriter.append(TAB_DELIMITER);
			
			String MUID = null, RUID = null;
			if(Boolean.parseBoolean(prop.getProperty("SaveToApex"))) {
				//setRunInfoInApex(prop, buildnumber, jobname);		
				MUID = getUniqueID(fileName, true);
				setUIValuesInApex(buildnumber,jobname,MUID,uiTime,restTotalTime);
			}
			
			if(!filteredResturls.isEmpty()) {
				for(int i=0; i<filteredResturls.size(); i++) {
					String name = filteredResturls.get(i).getName();
					String ecid = filteredResturls.get(i).getECID();
					String method = filteredResturls.get(i).getMethod();
					double idv_restTime = (double) filteredResturls.get(i).getUrlExecTime()/1000;
					String FA_server_RT = filteredResturls.get(i).getFA_server_RT();
					String OHS_server_RT = filteredResturls.get(i).getOHS_server_RT();
					String DCS_server_RT = filteredResturls.get(i).getDCS_server_RT();
					String VBCSOverHead;
					if(DCS_server_RT==null || OHS_server_RT==null)
					{ VBCSOverHead	= "NA"; }
					else {
					VBCSOverHead = String.valueOf(new BigDecimal((Double.parseDouble(DCS_server_RT) - Double.parseDouble(OHS_server_RT))/Double.parseDouble(DCS_server_RT)*100).round(new MathContext(4)).doubleValue()); 
					}
					
					if(Boolean.parseBoolean(prop.getProperty("SaveToApex"))) {
						RUID = getUniqueID(name, false);
						setRESTValuesInApex(buildnumber,jobname,MUID,RUID,ecid,DCS_server_RT,OHS_server_RT,FA_server_RT,VBCSOverHead,method);
					}
					
					if(!method.equals("GET")){
						name = name + "-" + method;
					}

					if(i == 0) {
						fileWriter.append(String.valueOf(name));
						fileWriter.append(TAB_DELIMITER);
						fileWriter.append(String.valueOf(ecid));
						fileWriter.append(TAB_DELIMITER);
						fileWriter.append(String.valueOf(idv_restTime));
						fileWriter.append(TAB_DELIMITER);
						fileWriter.append(String.valueOf(DCS_server_RT));
						fileWriter.append(TAB_DELIMITER);
						fileWriter.append(String.valueOf(OHS_server_RT));
						fileWriter.append(TAB_DELIMITER);
						fileWriter.append(String.valueOf(FA_server_RT));
						fileWriter.append(TAB_DELIMITER);
						fileWriter.append(String.valueOf(VBCSOverHead));
						fileWriter.append(TAB_DELIMITER);
						fileWriter.append(String.valueOf(restTotalTime));
					} else  {
						fileWriter.append(TAB_DELIMITER);
						fileWriter.append(TAB_DELIMITER);
						fileWriter.append(String.valueOf(name));
						fileWriter.append(TAB_DELIMITER);
						fileWriter.append(String.valueOf(ecid));
						fileWriter.append(TAB_DELIMITER);
						fileWriter.append(String.valueOf(idv_restTime));
						fileWriter.append(TAB_DELIMITER);
						fileWriter.append(String.valueOf(DCS_server_RT));
						fileWriter.append(TAB_DELIMITER);
						fileWriter.append(String.valueOf(OHS_server_RT));
						fileWriter.append(TAB_DELIMITER);
						fileWriter.append(String.valueOf(FA_server_RT));	
						fileWriter.append(TAB_DELIMITER);
						fileWriter.append(String.valueOf(VBCSOverHead));
					}
					
					fileWriter.append(NEW_LINE_SEPARATOR);
					
				}
			} else {
				fileWriter.append(NEW_LINE_SEPARATOR);
			}
			
			System.out.println("contents are written in CSV file successfully !!!");
				
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		}
	}
	
	public void closeCSVFile() {
		try {
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			System.out.println("Error while flushing/closing fileWriter !!!");
            e.printStackTrace();
		}
	}
	
	public String getUniqueID(String transactionName, boolean UItransactionType){
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String url = null, id = null;
		try {
		 if(UItransactionType)
			 url = "https://apex.oraclecorp.com/pls/apex/svcpsr/pmt/ui/master/" + transactionName;
		 else
		 	 url = "https://apex.oraclecorp.com/pls/apex/svcpsr/pmt/rest/master/" + transactionName;

			HttpGet getRequest = new HttpGet(url);
			getRequest.addHeader("accept", "application/json");
			HttpResponse response = httpClient.execute(getRequest);

			if (response.getStatusLine().getStatusCode() != 200) {
				id = "UNK001";
				System.out.println("Failed to retrive ID value "+ transactionName +" : HTTP error code : " + response.getStatusLine().getStatusCode());
			} else {
				BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
				JSONObject json = new JSONObject(br.readLine());
				 if(UItransactionType)
					 id = json.getString("muid");
				 else
					 id = json.getString("ruid");
			}
			httpClient.getConnectionManager().shutdown();

		  } catch (Exception e) {
		
			e.printStackTrace();
		  }		
		
		return id;
		
	}
	
	public static void setRunInfoInApex(Properties prop, String JRUNID, String ITERATIONID) {
		DefaultHttpClient httpClient = new DefaultHttpClient();		
		try {
			StringEntity input = new StringEntity("{\"JRUNID\":\""+ JRUNID +"\",\"ITERATIONID\":\""+ ITERATIONID +"\",\"P4FA_LABEL\":\""+ prop.getProperty("P4FA") +"\",\"VBCSBUILDID\":\""+ prop.getProperty("VBCSBuildID") +"\",\"DCSBUILDID\":\""+ prop.getProperty("DCSBuildID") +"\",\"FUSIONBUILDID\":\""+ prop.getProperty("FusionBuildID") +"\",\"DCS_URL\":\""+ prop.getProperty("URL") +"\",\"COMMENTS\": \"Commented from REST\"}");
			HttpPost postRequest = new HttpPost("https://apex.oraclecorp.com/pls/apex/svcpsr/pmt/run/info");
			postRequest.addHeader("content-type", "application/json");
			
			postRequest.setEntity(input);
			
			HttpResponse response = httpClient.execute(postRequest);
			
			if (response.getStatusLine().getStatusCode() != 200) {
				System.out.println("Failed to insert Run info values : HTTP error code : "+ response.getStatusLine().getStatusCode());
			}
			httpClient.getConnectionManager().shutdown();			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}
	
	public void setUIValuesInApex(String JRUNID, String ITERATIONID, String MUID, double UI_RESPONSETIME, double restTotalTime) {
		DefaultHttpClient httpClient = new DefaultHttpClient();		
		try {
			StringEntity input = new StringEntity("{\"JRUNID\":\""+ JRUNID +"\",\"ITERATIONID\":\""+ ITERATIONID +"\",\"MUID\":\""+ MUID +"\",\"UI_RESPONSETIME\": "+ UI_RESPONSETIME +",\"END_TO_END_REST_RESPONSETIME\": "+ restTotalTime +"}");
			
			//String s = new String("{\"JRUNID\":\""+ JRUNID +"\",\"ITERATIONID\":\""+ ITERATIONID +"\",\"MUID\":\""+ MUID +"\",\"UI_RESPONSETIME\": "+ UI_RESPONSETIME +",\"END_TO_END_REST_RESPONSETIME\": "+ restTotalTime +"}");
			//System.out.println(s);
			
			HttpPost postRequest = new HttpPost("https://apex.oraclecorp.com/pls/apex/svcpsr/pmt/ui/rundata/");
			postRequest.addHeader("content-type", "application/json");
			
			postRequest.setEntity(input);
			
			HttpResponse response = httpClient.execute(postRequest);
			
			if (response.getStatusLine().getStatusCode() != 200) {
				System.out.println("Failed to insert UI trans values : HTTP error code : "+ response.getStatusLine().getStatusCode());
			}
			httpClient.getConnectionManager().shutdown();			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setRESTValuesInApex(String JRUNID, String ITERATIONID, String MUID, String RUID, String ECID, String DCS_LOG_RESPONSETIME, String OHS_LOG_RESPONSETIME, String FA_LOG_RESPONSETIME, String VBCS_OVERHEAD, String METHOD_NAME) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		try {
			StringEntity input = new StringEntity("{\"JRUNID\":\""+ JRUNID +"\",\"ITERATIONID\":\""+ ITERATIONID +"\",\"MUID\":\""+ MUID +"\",\"RUID\":\""+ RUID +"\",\"ECID\":\""+ ECID +"\",\"DCS_LOG_RESPONSETIME\": "+ DCS_LOG_RESPONSETIME +",\"OHS_LOG_RESPONSETIME\": "+ OHS_LOG_RESPONSETIME +",\"FA_LOG_RESPONSETIME\": "+ FA_LOG_RESPONSETIME +",\"VBCS_OVERHEAD\": \""+ VBCS_OVERHEAD +"\",\"METHOD_NAME\":\""+ METHOD_NAME +"\"}");
			
			//String s = new String("{\"JRUNID\":\""+ JRUNID +"\",\"ITERATIONID\":\""+ ITERATIONID +"\",\"MUID\":\""+ MUID +"\",\"RUID\":\""+ RUID +"\",\"ECID\":\""+ ECID +"\",\"DCS_LOG_RESPONSETIME\": "+ DCS_LOG_RESPONSETIME +",\"OHS_LOG_RESPONSETIME\": "+ OHS_LOG_RESPONSETIME +",\"FA_LOG_RESPONSETIME\": "+ FA_LOG_RESPONSETIME +",\"VBCS_OVERHEAD\": \""+ VBCS_OVERHEAD +"\",\"METHOD_NAME\":\""+ METHOD_NAME +"\"}");
			//System.out.println(s);
			
			HttpPost postRequest = new HttpPost("https://apex.oraclecorp.com/pls/apex/svcpsr/pmt/rest/rundata/");
			postRequest.addHeader("content-type", "application/json");
			postRequest.setEntity(input);
			HttpResponse response = httpClient.execute(postRequest);
			
			if (response.getStatusLine().getStatusCode() != 200) {
				System.out.println("Failed to insert REST trans values : HTTP error code : "+ response.getStatusLine().getStatusCode());
			}
			httpClient.getConnectionManager().shutdown();			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
}
