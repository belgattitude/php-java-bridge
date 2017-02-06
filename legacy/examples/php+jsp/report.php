<?php require_once("java/Java.inc");
header("Content-type: text/html");


/**
 * This example demonstrates how to use a complex library such as
 * Eclipse BIRT. This library starts a number of threads, which must
 * be terminated before the library is unloaded. This is usually done
 * in the servlet's destroy() method or in the destroy() method of
 * an associated ServletContextListener.
 * 
 * To allow startup and shutdown of such libraries, the PHP/Java
 * Bridge provides two convenience procedures, which allow one to run
 * a synchronized init() and to register a close() hook with the
 * servlet or the VM. Please see the API documentation of
 * java_context()->init() and java_context()->onShutdown() for
 * details.
 * 
 * To use this sample, copy "report.php", "test.rptdesign" (and
 * "Java.inc", if needed) to some directory, start the Java back- end
 * (tomcat, or any other J2EE server) and type:
 *
 * php report.php >helloBirt.html
 *
 */

// the report file to render
$myReport = "test.rptdesign";

// load resources, .rpt files and images from the current working dir
$here = getcwd();

$ctx = java_context()->getServletContext();
$birtReportEngine =        java("org.eclipse.birt.php.birtengine.BirtEngine")->getBirtEngine($ctx);
java_context()->onShutdown(java("org.eclipse.birt.php.birtengine.BirtEngine")->getShutdownHook());


// Create a HTML render context
$renderContext = new java ("org.eclipse.birt.report.engine.api.HTMLRenderContext");
$renderContext->setBaseImageURL("$here/images");
$contextMap = new java("java.util.HashMap");
$CONTEXT = java("org.eclipse.birt.report.engine.api.EngineConstants")->APPCONTEXT_HTML_RENDER_CONTEXT;
$contextMap->put($CONTEXT, $renderContext );


// Load the report design
$design = $birtReportEngine->openReportDesign("${here}/${myReport}");
$task = $birtReportEngine->createRunAndRenderTask( $design );  
$task->setAppContext( $contextMap );

// Add HTML render options
$options = new java("org.eclipse.birt.report.engine.api.HTMLRenderOption");
$options->setOutputFormat($options->OUTPUT_FORMAT_HTML);

// Create the output
$out = new java("java.io.ByteArrayOutputStream");
$options->setOutputStream($out);
$task->setRenderOption($options);
$task->run ();
$task->close();

// Return the generated output to the client
echo java_values($out);

?>
