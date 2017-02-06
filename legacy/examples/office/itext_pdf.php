<?php include_once ("java/Java.inc");

java_autoload("itext.jar");

use com\lowagie\text;
use com\lowagie\text\pdf;

try {
  $document = new text\Document();
  $out = new java\io\ByteArrayOutputStream();
  $pdfWriter = pdf\PdfWriter::type()->getInstance($document, $out);

  $document->open();
  $font = text\FontFactory::type()->getFont(
	      text\FontFactory::type()->HELVETICA, 
	      24, 
	      text\Font::type()->BOLDITALIC, 
	      new java\awt\Color(0, 0, 255));
  
  $paragraph = new text\Paragraph("Hello World", $font);
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
