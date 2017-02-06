<?php require_once ("java/Java.inc");

java_require("itext.jar");

/*
 * Concat: merge pdf files
 *
 * Usage: concat.php outfile.pdf file1.pdf file2.pdf ...
 */

array_shift($argv); $argc--;

$outfile=$argv[0];
$args=$argv;
$pageOffset=0;
$f=1;
$master = new java("java.util.ArrayList");

while(--$argc) {
  $reader = new java("com.lowagie.text.pdf.PdfReader", $args[$f]);
  $reader->consolidateNamedDestinations();
  $n = java_values($reader->getNumberOfPages());
  $bookmarks = java("com.lowagie.text.pdf.SimpleBookmark")->getBookmark($reader);
  if(java_values($bookmarks)!=null) {
    if($pageOffset!=0) {
      java("com.lowagie.text.pdf.SimpleBookmark")->shiftPageNumbers($bookmarks, $pageOffset, null);
      $master->addAll($bookmarks);
    }
  }
  $pageOffset += $n;
  echo ("There are " . $n . " pages in " . $args[$f]); echo "\n";
  if($f==1) {
    $document = new java("com.lowagie.text.Document", $reader->getPageSizeWithRotation(1));
    $writer = new java("com.lowagie.text.pdf.PdfCopy", 
		       $document, 
		       new java("java.io.FileOutputStream", $outfile));
    $document->open();
  }
  for($i=0; $i<$n; ) {
    ++$i;
    $page = $writer->getImportedPage($reader, $i);
    $writer->addPage($page);
    echo "Processed page: " .$i; echo "\n";
  }
  $form = $reader->getAcroForm();
  if(java_values($form)!=null)
    $writer->copyAcroForm($reader);
  $f++;
}
if(java_values($master->size())>0) {
  $writer->setOutlines($master);
 }
$document->close();
      
?>

