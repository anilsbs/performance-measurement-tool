package ind.com.oracle.finalreport;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeMap;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class ExcelFileContentWriter {
	
    private WritableCellFormat contentLabelFormat;
    private WritableCellFormat contentNumberFormat;    
    private WritableCellFormat headersFormat;
    private WritableCellFormat subHeadersFormat;
    private WritableCellFormat titleFormat;
    
    public ExcelFileContentWriter(Properties prop, String resFileName, TreeMap<String, ArrayList<TransactionContentPOJO>> List, boolean median) throws WriteException, IOException {
      
        File file = new File(resFileName);
        
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);
        workbook.createSheet("Report", 0);
        WritableSheet excelSheet = workbook.getSheet(0);  
       /* CellView cv = new CellView();
        WritableCellFormat cellFormat = new WritableCellFormat();
        cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        cv.setFormat(cellFormat); */
        writeFileTitles(excelSheet);
        writeFileSubHeaders(excelSheet, prop);
        writeFileHeaders(excelSheet);
        createContent(excelSheet, List, median);

        workbook.write();
        workbook.close();
        
        System.out.println("Please check the result file under "+resFileName );
    }
    
    private void writeFileTitles(WritableSheet sheet)
            throws WriteException {

    	cellViewTitleFormat();
    	cellViewContentLabelFormat();
    	
    	int row = 0;
    	int col = 1;
    	int mergeCells = 10;
        sheet.mergeCells(col, row, mergeCells, row);    	
        addTitle(sheet, col, row, "Single User Response");
        sheet.mergeCells(0, 7, mergeCells, 7);
        addTitle(sheet, 0, 7, "Related Scenarios");
    }
    
    private void writeFileSubHeaders(WritableSheet sheet, Properties prop)
            throws WriteException {

    	cellViewSubHeadersFormat();
    	cellViewContentLabelFormat();
    	int row = 1;
    	int col = 0;
    	
    	
    	
    	String test_date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
    	
        // Write an sub headers
        addSubHeaders(sheet, col, row++, "VBCS Build ID / Version");
    	//addSubHeaders(sheet, col, row++, prop.getProperty("InputFolder"));
        addSubHeaders(sheet, col, row++, "DCS Build ID");
        addSubHeaders(sheet, col, row++, "Fusion Build ID");
        addSubHeaders(sheet, col, row++, "P4FA");
        addSubHeaders(sheet, col, row++, "Date of Run");
        addSubHeaders(sheet, col, row++, "URL");
        
        col++;
        row=1;
        int mergeCells = 10;
        
        sheet.mergeCells(col, row, mergeCells, row);
        addLabel(sheet, col, row++, prop.getProperty("1BuildID"));
        sheet.mergeCells(col, row, mergeCells, row);
        addLabel(sheet, col, row++, prop.getProperty("2BuildID"));
        sheet.mergeCells(col, row, mergeCells, row);
        addLabel(sheet, col, row++, prop.getProperty("3BuildID"));
        sheet.mergeCells(col, row, mergeCells, row);
        addLabel(sheet, col, row++, prop.getProperty("4BuildID"));
        sheet.mergeCells(col, row, mergeCells, row);
        addLabel(sheet, col, row++, test_date);
        sheet.mergeCells(col, row, mergeCells, row);
        addLabel(sheet, col, row++, prop.getProperty("EnvURL"));
    }
    
    private void writeFileHeaders(WritableSheet sheet)
            throws WriteException {

    	cellViewHeadersFormat();
    	int row = 8;
    	int col = 0;
        // Write an headers
        addCaption(sheet, col++, row, "Name");
        addCaption(sheet, col++, row, "UI Response time");
        addCaption(sheet, col++, row, "REST Name");
        addCaption(sheet, col++, row, "REST Response Time");
        addCaption(sheet, col++, row, "WLS Log Time");
        addCaption(sheet, col++, row, "FA OB Log Time");
        addCaption(sheet, col++, row, "FA WLS Log Time");
        addCaption(sheet, col++, row, "UI Overhead %");
        addCaption(sheet, col++, row, "End to End Response time");
        addCaption(sheet, col++, row, "UI Response time StdDev");
        addCaption(sheet, col++, row, "End to End REST Response time StdDev");
        
        col = 0;
        sheet.setColumnView(col++, 45);
        sheet.setColumnView(col++, 18);
        sheet.setColumnView(col++, 30);
        sheet.setColumnView(col++, 20);
        sheet.setColumnView(col++, 19);
        sheet.setColumnView(col++, 19);
        sheet.setColumnView(col++, 19);
        sheet.setColumnView(col++, 19);
        sheet.setColumnView(col++, 19);
        sheet.setColumnView(col++, 19);
        sheet.setColumnView(col++, 19);
        
    }

    private void createContent(WritableSheet sheet, TreeMap<String, ArrayList<TransactionContentPOJO>> List,boolean median) throws WriteException,
            RowsExceededException {
    	cellViewContentLabelFormat();
    	cellViewContentNumberFormat();
		TreeMap<String, ArrayList<TransactionContentPOJO>> computeStdDev = new TreeMap<String, ArrayList<TransactionContentPOJO>>();
		ArrayList<TransactionContentPOJO> transPOJOList = null;
		
		int row = 9;
		int actualRowValue = 0;
		int printIndex = 0;
		for(String key : List.keySet()){
			
			transPOJOList = List.get(key);
			
			if(median) {
				printIndex = (int) Math.floor(( (transPOJOList.size() / 2D) > 0.5 ? (transPOJOList.size() / 2D) : 0 ));
			}
	
			int col = 0, actualColValue = 0, nextColValue = 0 ;
			
			addLabel(sheet, col++, row, transPOJOList.get(printIndex).getName());
			
			addNumber(sheet, col++, row, transPOJOList.get(printIndex).getUiTime());
			
			if(transPOJOList.get(printIndex).getRestContent() != null) {
				actualRowValue = row;
				for(RESTContentPOJO restContentPOJO: transPOJOList.get(printIndex).getRestContent()) {
				actualColValue = col;

				addLabel(sheet, actualColValue++, actualRowValue, restContentPOJO.getRestName());

				addNumber(sheet, actualColValue++, actualRowValue, restContentPOJO.getIdv_restTime());

				addNumber(sheet, actualColValue++, actualRowValue, restContentPOJO.getDCS_server_RT());

				addNumber(sheet, actualColValue++, actualRowValue, restContentPOJO.getOHS_server_RT());

				addNumber(sheet, actualColValue++, actualRowValue, restContentPOJO.getFA_server_RT());

				addNumber(sheet, actualColValue++, actualRowValue, restContentPOJO.getVBCSOverHead());
				
				nextColValue = actualColValue;
				actualColValue = col;
				actualRowValue++;
			}
				col = nextColValue;
			} else {
				col += 6; // Change the value based on columns used for REST details from for-loop above. 
			}

			addNumber(sheet, col++, row, transPOJOList.get(printIndex).getRestTotalTime());
			
			addNumber(sheet, col++, row, transPOJOList.get(printIndex).getUiStdDev());
	
			addNumber(sheet, col++, row, transPOJOList.get(printIndex).getRestStdDev());
			
			if(actualRowValue > row){
				row = actualRowValue;
			}else{
				row++;
			}
		}
    }
    
    private void cellViewTitleFormat() throws WriteException {
        // Lets create a times font
        WritableFont times12pt = new WritableFont(WritableFont.TIMES, 12, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.WHITE);
        // Define the cell format
        titleFormat = new WritableCellFormat(times12pt);
        // Lets automatically wrap the cells
        //times.setWrap(true);
        titleFormat.setBackground(Colour.OCEAN_BLUE);
        titleFormat.setAlignment(Alignment.CENTRE);
        titleFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        titleFormat.setWrap(true);    	
    }
    
    private void cellViewHeadersFormat() throws WriteException {
        // Lets create a times font
        WritableFont times11pt = new WritableFont(WritableFont.TIMES, 11, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.WHITE);
        // Define the cell format
        headersFormat = new WritableCellFormat(times11pt);
        // Lets automatically wrap the cells
        //times.setWrap(true);
        headersFormat.setBackground(Colour.OCEAN_BLUE);
        headersFormat.setAlignment(Alignment.CENTRE);
        headersFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        headersFormat.setWrap(true);    	
    }
    
    private void cellViewSubHeadersFormat() throws WriteException {
        // Lets create a times font
        WritableFont times11pt = new WritableFont(WritableFont.TIMES, 12, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.WHITE);
        // Define the cell format
        subHeadersFormat = new WritableCellFormat(times11pt);
        // Lets automatically wrap the cells
        //times.setWrap(true);
        subHeadersFormat.setBackground(Colour.GREY_40_PERCENT);
        //subHeadersFormat.setAlignment(Alignment.CENTRE);
        subHeadersFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        //subHeadersFormat.setWrap(true);    	
    }    
    
    
    private void cellViewContentLabelFormat() throws WriteException {
        // create create a bold font
        WritableFont times10ptBoldUnderline = new WritableFont(WritableFont.TIMES, 10);
        contentLabelFormat = new WritableCellFormat(times10ptBoldUnderline);
        contentLabelFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        // Lets automatically wrap the cells
        contentLabelFormat.setWrap(true);     	
    }
    
    private void cellViewContentNumberFormat() throws WriteException {
        WritableFont times10ptBoldUnderline = new WritableFont(WritableFont.TIMES, 10);
        contentNumberFormat = new WritableCellFormat(times10ptBoldUnderline, NumberFormats.FLOAT);
        contentNumberFormat.setAlignment(Alignment.CENTRE);
        contentNumberFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        contentNumberFormat.setWrap(true);     	
    }    

    private void addTitle(WritableSheet sheet, int column, int row, String s)
            throws RowsExceededException, WriteException {
        Label label;
        //times.setWrap(true);
        label = new Label(column, row, s, titleFormat);
        sheet.addCell(label);
    }
    
    private void addCaption(WritableSheet sheet, int column, int row, String s)
            throws RowsExceededException, WriteException {
        Label label;
        //times.setWrap(true);
        label = new Label(column, row, s, headersFormat);
        sheet.addCell(label);
    }
    
    private void addSubHeaders(WritableSheet sheet, int column, int row, String s)
            throws RowsExceededException, WriteException {
        Label label;
        //times.setWrap(true);
        label = new Label(column, row, s, subHeadersFormat);
        sheet.addCell(label);
    }    

    private void addNumber(WritableSheet sheet, int column, int row,
            Double dou) throws WriteException, RowsExceededException {
    	if(dou > -1) {
    	Number number;
        number = new Number(column, row, dou, contentNumberFormat);
        sheet.addCell(number);
    	} else {
    		addLabel(sheet, column, row, "");
    	}
    }

    private void addLabel(WritableSheet sheet, int column, int row, String s)
            throws WriteException, RowsExceededException {
        Label label;
        label = new Label(column, row, s, contentLabelFormat);
        sheet.addCell(label);
    }
}
