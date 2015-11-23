<?php
if (!(get_cfg_var('java.web_inf_dir'))) {
  define ("JAVA_HOSTS", "127.0.0.1:8080");
  define ("JAVA_SERVLET", "/JavaBridge/JavaBridge.phpjavabridge");
}
require_once("java/Java.inc");

$pth = "http://" . $_SERVER["HTTP_HOST"] . $_SERVER["PHP_SELF"];
$path_parts = pathinfo($pth);
$filePrefix = $path_parts['dirname'] ."/myreport.xls";

session_start(); 
$here = getcwd();

$ctx = java_context()->getServletContext();
$birtReportEngine =        java("org.eclipse.birt.php.birtengine.BirtEngine")->getBirtEngine($ctx);
java_context()->onShutdown(java("org.eclipse.birt.php.birtengine.BirtEngine")->getShutdownHook());


try{


$report = $birtReportEngine->openReportDesign("${here}/TopNPercent.rptdesign");
$task = $birtReportEngine->createRunAndRenderTask($report);
$taskOptions = new java("org.eclipse.birt.report.engine.api.RenderOption");
$taskOptions->setOutputFileName($here . "/myreport.xls");
$taskOptions->setOutputFormat("xls");

$task->setRenderOption( $taskOptions );
$task->run();
$task->close();

} catch (JavaException $e) {
	echo $e; //"Error Calling BIRT";
	
}
echo 'Click <a href='. $filePrefix . '>Here</a> to download your report';
?>
