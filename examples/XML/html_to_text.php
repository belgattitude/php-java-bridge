#!/usr/bin/php

<?php

require_once ("java/Java.inc");

class HandlerBase {
  function resolveEntity ($publicId, $systemId) 
  {
    return null;
  }
    
  function notationDecl ($name, $publicId, $systemId)
  {
  }
    
  function unparsedEntityDecl ($name, $publicId, $systemId, $notationName)
  {
  }
    
  function setDocumentLocator ($locator)
  {
  }
    
  function startDocument ()
  {
    echo "\n";
  }

  function endDocument ()
  {
    echo "\n";
  }
  function startElement ($name, $attributes) 
  {
    echo "[";
  }

  function endElement ($name)
  {
    echo "]";
  }
    
  function characters ($chars, $start, $length)
  {
    $s = new java("java.lang.String", $chars, $start, $length);
    echo java_values($s);
  }
  function ignorableWhitespace ($chars, $start, $length)
  {
    $s = new java("java.lang.String", $chars, $start, $length);
    echo java_values($s);
  }

  function processingInstruction ($target, $data)
  {
  }
    
    
  function warning ($e)
  {
    echo "callback warning called with args $e<br>\n";
  }
    
  function error ($e)
  {
    echo "callback error called with args $e<br>\n";
  }
    
  function fatalError ($e)
  {
    echo "callback fatalError called with args $e<br>\n";
    //throw $e;
  }
}

// The interfaces that our HandlerBase implements
function getInterfaces() {
  return array(new JavaClass("org.xml.sax.EntityResolver"),
	       new JavaClass("org.xml.sax.DTDHandler"),
	       new JavaClass("org.xml.sax.DocumentHandler"),
	       new JavaClass("org.xml.sax.ErrorHandler"));
}

// Create an instance of HandlerBase which implements the above
// interfaces.
function createHandler() {
  return java_closure(new HandlerBase(), null, getInterfaces());
}

// Standard SAX handling
$ParserFactory=new JavaClass("javax.xml.parsers.SAXParserFactory");
$parser=$ParserFactory->newInstance()->newSaxParser()->getParser();

$handler=createHandler();
$parser->setDocumentHandler($handler);
$parser->setErrorHandler($handler);

$parser->setFeature("http://apache.org/xml/features/validation/schema", false);
$parser->setFeature("http://xml.org/sax/features/namespaces", false);
$parser->setFeature("http://xml.org/sax/features/validation", false);
$parser->setFeature("http://xml.org/sax/features/namespace-prefixes", false);
$parser->setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);

// capture the HTML output of phpinfo ..
// ob_start();
// phpinfo();
// $in = new java("java.io.ByteArrayInputStream", ob_get_contents());
// ob_end_clean();
$here=getcwd();
$in = new java("java.io.FileInputStream", "$here/phpinfo.xml");

// and filter it through the above callbacks
$inputSource=new java("org.xml.sax.InputSource", "$here/DTD");
$inputSource->setByteStream($in);
$parser->parse($inputSource);

?>
