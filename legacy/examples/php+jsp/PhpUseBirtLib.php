<?php

if (!(get_cfg_var('java.web_inf_dir'))) {
  define ("JAVA_HOSTS", "127.0.0.1:8080");
  define ("JAVA_SERVLET", "/JavaBridge/JavaBridge.phpjavabridge");
}
$pth = "http://" . $_SERVER["HTTP_HOST"] . $_SERVER["PHP_SELF"];
$path_parts = pathinfo($pth);
$imageURLPrefix = $path_parts['dirname'] ."/sessionChartImages/";
require_once("java/Java.inc");

session_start(); 

$here = getcwd();

$ctx = java_context()->getServletContext();
$birtReportEngine =        java("org.eclipse.birt.php.birtengine.BirtEngine")->getBirtEngine($ctx);
java_context()->onShutdown(java("org.eclipse.birt.php.birtengine.BirtEngine")->getShutdownHook());


try{
//should only be set once will affect all users of the engine
$birtReportEngine->getConfig()->setResourcePath(${here} . "/birtreportlib" );
$report = $birtReportEngine->openReportDesign("${here}/uselib.rptdesign");
$task = $birtReportEngine->createRunAndRenderTask($report);
$taskOptions = new java("org.eclipse.birt.report.engine.api.HTMLRenderOption");
$outputStream = new java("java.io.ByteArrayOutputStream");
$taskOptions->setOutputStream($outputStream);
$taskOptions->setOutputFormat("html");
$ih = new java( "org.eclipse.birt.report.engine.api.HTMLServerImageHandler");
$taskOptions->setImageHandler($ih);
$taskOptions->setEnableAgentStyleEngine(true);
$taskOptions->setBaseImageURL($imageURLPrefix . session_id());
$taskOptions->setImageDirectory($here . "/sessionChartImages/" . session_id());

$task->setRenderOption( $taskOptions );
$task->run();
$task->close();

} catch (JavaException $e) {
	echo $e; //"Error Calling BIRT";
	
}
echo $outputStream;


?>