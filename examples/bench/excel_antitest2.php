<?php
/*
 * This file is part of bench.php.
 *
 * Create a 200x200 excel sheet and write it to the file
 * "workbook_php.xls
 *
 * It is called "antitest" because it shows how the PHP/Java bridge
 * should _not_ be used:
 *
 * This test generates 40000 cells using more than n*40000 java reflection
 * calls (n is ~ 3..5). The code is a) interpreted and b) calls are routed
 * through the java reflection machinery and through the methods in
 * JavaBridge.java.  
 *
 * PHP code which does this will execute 10 times slower
 * than native, java JIT compiled, code.
 */

function createWorkbook2($name, $dx, $dy) {
$wb = new java("org.apache.poi.hssf.usermodel.HSSFWorkbook");
$sheet = $wb->createSheet("new sheet");
$row = $sheet->createRow(0);

//Aqua background
$style = $wb->createCellStyle();
$aqua = new java ('org.apache.poi.hssf.util.HSSFColor$AQUA');
$style->setFillBackgroundColor($aqua->index);
$style->setFillPattern($style->BIG_SPOTS);
$cell = $row->createCell(1);
$cell->setCellValue("X");
$cell->setCellStyle($style);


//Orange "foreground", foreground being the fill foreground not the font color.
$orange = new java ('org.apache.poi.hssf.util.HSSFColor$ORANGE');
$style->setFillForegroundColor($orange->index);
$style->setFillPattern($style->SOLID_FOREGROUND);

java_begin_document();
for ($x = 0; $x < $dx; $x++) {
  $row = $sheet->createRow($x);
  for ($y = 0; $y < $dy; $y++) {
    $cell = $row->createCell($y);
    $cell->setCellValue("$x . $y");
    $cell->setCellStyle($style);
  }
}
java_end_document();

// Write the output to a file
$fileOut = new java ("java.io.FileOutputStream", $name);
$wb->write($fileOut);
$fileOut->close(); 

}
?>