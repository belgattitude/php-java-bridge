<?php
if (!(get_cfg_var('java.web_inf_dir'))) {
  define ("JAVA_HOSTS", "127.0.0.1:8080");
  define ("JAVA_SERVLET", "/JavaBridge/JavaBridge.phpjavabridge");
}

require_once("java/Java.inc");

session_start(); 
$here = getcwd();

$ctx = java_context()->getServletContext();
$birtReportEngine =        java("org.eclipse.birt.php.birtengine.BirtEngine")->getBirtEngine($ctx);
java_context()->onShutdown(java("org.eclipse.birt.php.birtengine.BirtEngine")->getShutdownHook());


try{


$report = $birtReportEngine->openReportDesign("${here}/TopNPercent.rptdesign");
$task = $birtReportEngine->createRunTask($report);
$task->run($here . "/reportDocuments/" . session_id() . "/mytopnreportdocument.rptdocument");
$task->close();

} catch (JavaException $e) {
	echo $e; //"Error Calling BIRT";
	
}
//echo "test";
//echo $outputStream->toString().trim();
echo "Document Created";



?>