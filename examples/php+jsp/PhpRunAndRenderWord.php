<?php
header("Content-type: application/msword");
header("Content-Disposition: inline; filename=downloadedmsdoc.doc");

define ("JAVA_HOSTS", "127.0.0.1:8080");
define ("JAVA_SERVLET", "/JavaBridge/JavaBridge.phpjavabridge");

require_once("java/Java.inc");

session_start(); 

$here = getcwd();

$ctx = java_context()->getServletContext();
$birtReportEngine =        java("org.eclipse.birt.php.birtengine.BirtEngine")->getBirtEngine($ctx);
java_context()->onShutdown(java("org.eclipse.birt.php.birtengine.BirtEngine")->getShutdownHook());


try{


$report = $birtReportEngine->openReportDesign("${here}/TopNPercent.rptdesign");
$task = $birtReportEngine->createRunAndRenderTask($report);
$taskOptions = new java("org.eclipse.birt.report.engine.api.RenderOption");
$outputStream = new java("java.io.ByteArrayOutputStream");
$taskOptions->setOutputStream($outputStream);
$taskOptions->setOutputFormat("doc");

$task->setRenderOption( $taskOptions );
$task->run();
$task->close();

} catch (JavaException $e) {
	echo $e; //"Error Calling BIRT";
	
}
//echo "test";
//echo $outputStream->toString().trim();
echo java_values($outputStream->toByteArray());



?>