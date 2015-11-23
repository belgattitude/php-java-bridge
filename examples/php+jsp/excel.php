<?php require_once("java/Java.inc");

header("Content-type: application/vnd.ms-excel");
header("Content-Disposition: attachment; filename=downloaded.xls");

// create a 50x40 excel sheet and return it to the client
$workbook = new java("org.apache.poi.hssf.usermodel.HSSFWorkbook");
$sheet = $workbook->createSheet("new sheet");

for($y=0; $y<40; $y++) {
  $row = $sheet->createRow($y);
  for($x=0; $x<50; $x++) {
    $cell = $row->createCell($x);
    $cell->setCellValue("cell $x/$y");
  }
}

// create and return the excel sheet to the client
$memoryStream = new java("java.io.ByteArrayOutputStream");
$workbook->write($memoryStream);
$memoryStream->close();
echo java_values($memoryStream->toByteArray());
?>
