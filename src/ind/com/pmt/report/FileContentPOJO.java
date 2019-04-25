package ind.com.oracle.report;

import java.util.Comparator;
import java.util.Date;

class FileContentPOJO implements Comparable<FileContentPOJO> {
	

	private String url;
	private String name;
	private Date startDateTime;
	private int duration;
	private Date endDateTime;
	private long urlExecTime;
	private String ecid;
	private String size;
	private String FA_server_RT;
	private String OHS_server_RT;
	private String DCS_server_RT;
	private String method;


	public FileContentPOJO(String url,String name, Date startDateTime, int duration, String ecid, Date endDateTime, long urlExecTime, String size, String FA_server_RT, String OHS_server_RT, String DCS_server_RT, String method) {

		this.url = url;
		this.name = name;
		this.startDateTime = startDateTime;
		this.duration = duration;
		this.endDateTime = endDateTime;
		this.urlExecTime = urlExecTime;
		this.ecid = ecid;
		this.size = size;
		this.FA_server_RT = FA_server_RT;
		this.OHS_server_RT = OHS_server_RT;
		this.DCS_server_RT = DCS_server_RT;
		this.method = method;
	}
	
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @return the Name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param Name to set
	 */
	public void setName(String name) {
		this.name = name;
	}	
	
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	/**
	 * @return the startDateTime
	 */
	public Date getstartDateTime() {
		return startDateTime;
	}
	/**
	 * @param startDateTime the startDateTime to set
	 */
	public void setstartDateTime(Date startDateTime) {
		this.startDateTime = startDateTime;
	}
	/**
	 * @return the duration
	 */
	public int getDuration() {
		return duration;
	}
	/**
	 * @param duration the duration to set
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}
	/**
	 * @return the endDateTime
	 */
	public Date getendDateTime() {
		return endDateTime;
	}
	/**
	 * @param endDateTime the endDateTime to set
	 */
	public void setendDateTime(Date endDateTime) {
		this.endDateTime = endDateTime;
	}
	/**
	 * @return the ecid
	 */
	public String getECID() {
		return ecid;
	}
	/**
	 * @param ecid the ecid to set
	 */
	public void setECID(String ecid) {
		this.ecid = ecid;
	}
	
	/**
	 * @return the urlExecTime
	 */
	public long getUrlExecTime() {
		return urlExecTime;
	}

	/**
	 * @param urlExecTime the urlExecTime to set
	 */
	public void setUrlExecTime(long urlExecTime) {
		this.urlExecTime = urlExecTime;
	}
	
	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}	
	
	public String getFA_server_RT() {
		return FA_server_RT;
	}

	public void setFA_server_RT(String fA_server_RT) {
		FA_server_RT = fA_server_RT;
	}

	public String getOHS_server_RT() {
		return OHS_server_RT;
	}

	public void setOHS_server_RT(String oHS_server_RT) {
		OHS_server_RT = oHS_server_RT;
	}

	public String getDCS_server_RT() {
		return DCS_server_RT;
	}

	public void setDCS_server_RT(String dCS_server_RT) {
		DCS_server_RT = dCS_server_RT;
	}
	
    public int compareTo(FileContentPOJO fileContentPOJO) {
        return this.getendDateTime().getTime() > fileContentPOJO.getendDateTime().getTime()? 1 : (this.getendDateTime().getTime() < fileContentPOJO.getendDateTime().getTime() ? -1 : 0);
    }
    
    public static Comparator<FileContentPOJO> NameComparator = new Comparator<FileContentPOJO>() {
	public int compare(FileContentPOJO fileContentpojo1, FileContentPOJO fileContentpojo2) {

	   //ascending order
	   return fileContentpojo1.getName().compareTo(fileContentpojo2.getName());

	   //descending order
	   //return URLname1.compareTo(URLname2); 
    }};
	
}