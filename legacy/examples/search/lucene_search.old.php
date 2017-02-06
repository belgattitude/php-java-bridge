<?php require_once ("java/Java.inc");

java_require("lucene.jar");


try {
  echo "indexing ... ";
  /* Create an index */
  $cwd=getcwd();
  /* create the index files in the tmp dir */
  $tmp = create_index_dir();
  $analyzer = new java("org.apache.lucene.analysis.standard.StandardAnalyzer");
  $writer = new java("org.apache.lucene.index.IndexWriter", $tmp, $analyzer, true);
  $file = new java("java.io.File", $cwd);
  $files = $file->listFiles();
  assert (!java_is_null($files));

  foreach($files as $f) {
    $doc = new java("org.apache.lucene.document.Document");
    $doc->add(new java("org.apache.lucene.document.Field",
      "name", 
       $f->getName(), 
       java('org.apache.lucene.document.Field$Store')->YES, 
       java('org.apache.lucene.document.Field$Index')->UN_TOKENIZED));
    $writer->addDocument($doc);
  }
  $writer->optimize();
  $writer->close();
  echo "done\n";

  echo "searching... ";
  /* Search */
  $searcher = new java("org.apache.lucene.search.IndexSearcher", $tmp);
  $phrase = new java("org.apache.lucene.search.MatchAllDocsQuery");
  $hits = $searcher->search($phrase);

  /* Print result */
  $iter = $hits->iterator();
  $n = java_values($hits->length());
  echo "done\n";
  echo "Hits: $n\n";

  /* Instead of retrieving the values one-by-one, we store them into a
   * LinkedList on the server side and then retrieve the list in one
   * query using java_values():
   */
  $resultList = new java("java.util.LinkedList");

				// create an XML document from the
				// following PHP code, ...
  java_begin_document();
  while($n--) {
    $next = $iter->next();
    $name = $next->get("name");
    $resultList->add($name);
  }
				//  ... execute the XML document on
				//  the server side, ...
  java_end_document();
  
				// .. retrieve the result, ...
  $result = java_values($resultList); 
				// ... print the result array
  print_r($result);

  delete_index_dir();
} catch (JavaException $e) {
  echo "Exception occured: "; echo $e; echo "<br>\n";
}

/** helper functions */
$tmp_file=null;
$tmp_dir=null;
/** create a temporary directory for the lucene index files. Make sure
 * to create the tmpdir from Java so that the directory has
 * javabridge_tmp_t Security Enhanced Linux permission. Note that PHP
 * does not have access to tempfiles with java_bridge_tmp_t: PHP
 * inherits the rights from the HTTPD, usually httpd_tmp_t.
 */
function create_index_dir() {
  global $tmp_file, $tmp_dir;
  $javaTmpdir = java("java.lang.System")->getProperty("java.io.tmpdir");
  $tmpdir = java_values($javaTmpdir);
  $tmp_file=tempnam($tmpdir, "idx");
  $tmp_dir=new java("java.io.File", "${tmp_file}.d");
  $tmp_dir->mkdir();
  return java_values($tmp_dir->toString());
}

/** delete the lucene index files */
function delete_index_dir() {
  global $tmp_file, $tmp_dir;
  $files = $tmp_dir->listFiles();
  foreach($files as $f) {
    $f->delete();
  }
  $tmp_dir->delete();
  unlink($tmp_file);
  $tmp_file=$tmp_dir=null;
}

?>
