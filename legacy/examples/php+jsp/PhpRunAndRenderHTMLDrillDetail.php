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


$rpt = $_REQUEST['__report'] ; 
if( substr(PHP_OS,0,3) ==  'WIN' ){
	$rpt = substr($rpt, 1);
}

$report = $birtReportEngine->openReportDesign($rpt);

$task = $birtReportEngine->createRunAndRenderTask($report);


//Match request parameters with detail report parameters
$sh = $report->getDesignHandle()->getParameters();
$parmarraysize = $sh->getCount();

$i = 0;
while ($i < $parmarraysize) {

	$parmhandle = $sh->get($i);
	$parmname = $parmhandle->getName();
	$dt = $parmhandle->getDataType();
	$passedinvalue = $_REQUEST[java_values($parmname)];
	if (isset($passedinvalue)) {
		if( strcasecmp(java_values($dt), "integer") == 0 )
		{
			$Parm = new Java("java.lang.Integer", $passedinvalue);
			$task->setParameterValue($parmname, $Parm);
		}else if( strcasecmp(java_values($dt), "string") == 0 )
		{
			$Parm = new Java("java.lang.String", $passedinvalue);
			$task->setParameterValue($parmname, $Parm);
		}
		//... add additional data types here
	}
	$i++;
}

$taskOptions = new java("org.eclipse.birt.report.engine.api.HTMLRenderOption");
$outputStream = new java("java.io.ByteArrayOutputStream");
$taskOptions->setOutputStream($outputStream);
$taskOptions->setOutputFormat("html");
$ih = new java( "org.eclipse.birt.report.engine.api.HTMLServerImageHandler");
$taskOptions->setImageHandler($ih);
$taskOptions->setBaseImageURL($imageURLPrefix . session_id());
$taskOptions->setImageDirectory($here . "/sessionChartImages/" . session_id());
$taskOptions->setEnableAgentStyleEngine(true);
$task->setRenderOption( $taskOptions );
$task->run();
$task->close();

} catch (JavaException $e) {
	echo $e;	
}
echo $outputStream;
?>
