package ind.com.oracle.finalreport;

public class RESTContentPOJO {
	String restName;
	double idv_restTime;
	double FA_server_RT;
	double OHS_server_RT;
	double DCS_server_RT;
	double VBCSOverHead;
	
	public RESTContentPOJO(String restName, double idv_restTime, double fA_server_RT, double oHS_server_RT,
			double dCS_server_RT, double vBCSOverHead) {
		super();
		this.restName = restName;
		this.idv_restTime = idv_restTime;
		FA_server_RT = fA_server_RT;
		OHS_server_RT = oHS_server_RT;
		DCS_server_RT = dCS_server_RT;
		VBCSOverHead = vBCSOverHead;
	}
	
	public String getRestName() {
		return restName;
	}
	public void setRestName(String restName) {
		this.restName = restName;
	}
	public double getIdv_restTime() {
		return idv_restTime;
	}
	public void setIdv_restTime(double idv_restTime) {
		this.idv_restTime = idv_restTime;
	}
	public double getFA_server_RT() {
		return FA_server_RT;
	}
	public void setFA_server_RT(double fA_server_RT) {
		FA_server_RT = fA_server_RT;
	}
	public double getOHS_server_RT() {
		return OHS_server_RT;
	}
	public void setOHS_server_RT(double oHS_server_RT) {
		OHS_server_RT = oHS_server_RT;
	}
	public double getDCS_server_RT() {
		return DCS_server_RT;
	}
	public void setDCS_server_RT(double dCS_server_RT) {
		DCS_server_RT = dCS_server_RT;
	}
	public double getVBCSOverHead() {
		return VBCSOverHead;
	}
	public void setVBCSOverHead(double vBCSOverHead) {
		VBCSOverHead = vBCSOverHead;
	}
	
}
