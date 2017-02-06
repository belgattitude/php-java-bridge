<?php include_once ("java/Java.inc");

java_autoload("itext.jar");

use com\lowagie\text;
use com\lowagie\text\pdf;

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
$master = new java\util\ArrayList();

while(--$argc) {
  $reader = new pdf\PdfReader($args[$f]);
  $reader->consolidateNamedDestinations();
  $n = java_values($reader->getNumberOfPages());
  $bookmarks = pdf\SimpleBookmark::type()->getBookmark($reader);
  if(!java_is_null($bookmarks)) {
    if($pageOffset!=0) {
      pdf\SimpleBookmark::type()->shiftPageNumbers($bookmarks, $pageOffset, null);
      $master->addAll($bookmarks);
    }
  }
  $pageOffset += $n;
  echo ("There are " . $n . " pages in " . $args[$f]); echo "\n";
  if($f==1) {
    $document = new text::Document($reader->getPageSizeWithRotation(1));
    $writer = new pdf::PdfCopy($document, new java\io\FileOutputStream($outfile));
    $document->open();
  }
  for($i=0; $i<$n; ) {
    ++$i;
    $page = $writer->getImportedPage($reader, $i);
    $writer->addPage($page);
    echo "Processed page: " .$i; echo "\n";
  }
  $form = $reader->getAcroForm();
  if(!java_is_null($form))
    $writer->copyAcroForm($reader);
  $f++;
}
if(java_values($master->size())>0) {
  $writer->setOutlines($master);
 }
$document->close();
      
?>
