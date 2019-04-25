package ind.com.oracle.finalreport;

import java.util.Comparator;
import java.util.List;

public class TransactionContentPOJO {
	
	String name;
	double uiTime;
	double restTotalTime;
	List<RESTContentPOJO> restContent;
	double uiStdDev;
	double restStdDev;

	public TransactionContentPOJO(String name,double uiTime, double restTotalTime, List<RESTContentPOJO> restContent) {
		super();
		this.name = name;
		this.uiTime = uiTime;
		this.restTotalTime = restTotalTime;
		this.restContent = restContent;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public double getUiTime() {
		return uiTime;
	}
	public void setUiTime(double uiTime) {
		this.uiTime = uiTime;
	}
	public double getRestTotalTime() {
		return restTotalTime;
	}
	public void setRestTotalTime(double restTotalTime) {
		this.restTotalTime = restTotalTime;
	}
	public List<RESTContentPOJO> getRestContent() {
		return restContent;
	}
	public void setRestContent(List<RESTContentPOJO> restContent) {
		this.restContent = restContent;
	}
	
	public double getUiStdDev() {
		return uiStdDev;
	}

	public void setUiStdDev(double uiStdDev) {
		this.uiStdDev = uiStdDev;
	}

	public double getRestStdDev() {
		return restStdDev;
	}

	public void setRestStdDev(double restStdDev) {
		this.restStdDev = restStdDev;
	}	
	
    public static Comparator<TransactionContentPOJO> UIResponseTimeComparator = new Comparator<TransactionContentPOJO>() {
	public int compare(TransactionContentPOJO transContentpojo1, TransactionContentPOJO transContentpojo2) {

	   //ascending order
	   return transContentpojo1.getUiTime() > transContentpojo2.getUiTime() ? 1 : (transContentpojo1.getUiTime() < transContentpojo2.getUiTime() ? -1 : 0 ) ;

	   //descending order

    }};
    
}
