<?php require_once ("java/Java.inc");

java_require("itext.jar");

try {
  $document = new java("com.lowagie.text.Document");
  $out = new java("java.io.ByteArrayOutputStream");
  $pdfWriter = java("com.lowagie.text.pdf.PdfWriter")->getInstance($document, $out);

  $document->open();
  $font = java("com.lowagie.text.FontFactory")->getFont(
	      java("com.lowagie.text.FontFactory")->HELVETICA, 
	      24, 
	      java("com.lowagie.text.Font")->BOLDITALIC, 
	      new java("java.awt.Color", 0, 0, 255));
  
  $paragraph = new java("com.lowagie.text.Paragraph", "Hello World", $font);
  $document->add($paragraph);

  $document->close();
  $pdfWriter->close();

  // print the generated document
  header("Content-type: application/pdf");
  header("Content-Disposition: attachment; filename=HelloWorld.pdf");
  echo java_values($out->toByteArray());
} catch (JavaException $e) {
  echo "Exception occured: "; echo $e; echo "<br>\n";
}
?>
