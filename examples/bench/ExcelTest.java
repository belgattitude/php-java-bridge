import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.*;

/*
 * Used by bench.php
 *
 * Create a excel sheet and write it to the file "workbook.xls
 */
public class ExcelTest {
    public static void main(String s[]) {
	try {
	    createWorkbook("workbook.xls", 100, 100);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    public static void createWorkbook(String name, int dx, int dy) throws Exception {
	HSSFWorkbook wb = new HSSFWorkbook();
	HSSFSheet sheet = wb.createSheet("new sheet");
	HSSFRow row = sheet.createRow(0);
	
	//Aqua background
	HSSFCellStyle style = wb.createCellStyle();
	style.setFillBackgroundColor(HSSFColor.AQUA.index);
	style.setFillPattern(style.BIG_SPOTS);
	
	HSSFCell cell = row.createCell((short)1);
	cell.setCellValue("X");
	cell.setCellStyle(style);
	
	// Orange "foreground", foreground being the fill foreground not the font color.
	style.setFillForegroundColor(HSSFColor.ORANGE.index);
	style.setFillPattern(style.SOLID_FOREGROUND);
	
	for (int x = 0; x < dx; x++) {
	    
	    // Create a row and put some cells in it. Rows are 0 based.
	    row = sheet.createRow(x);
	    for (int y = 0; y < dy; y++) {
		cell = row.createCell((short)y);
		cell.setCellValue(String.valueOf(x) + " . " +  String.valueOf(y));
		cell.setCellStyle(style);
	    }
	}
	
	// Write the output to a file
	try {
	    java.io.FileOutputStream fileOut = new java.io.FileOutputStream(name);
	    wb.write(fileOut);
	    fileOut.close(); 
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
