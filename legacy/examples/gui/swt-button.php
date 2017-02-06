#!/usr/bin/php -nq

<?php

require_once ("java/Java.inc");
ini_set("max_execution_time", 0);

class ButtonDemo {
  var $shell, $display;

  function ButtonDemo($display, $shell) {
    $this->display = $display;
    $this->shell = $shell;
  }

  function widgetSelected($arg) { $this->displayMessage($arg); }
  function widgetDefaultSelected($arg) { echo "widget default selected \n"; }

  function displayMessage($e) {
    $mb = new Java("org.eclipse.swt.widgets.MessageBox", $this->shell);
    $mb->setMessage("Thank you.");
    $mb->open();
  }


  function init() {
    $SWT = new JavaClass("org.eclipse.swt.SWT");

    $shell = $this->shell = new java("org.eclipse.swt.widgets.Shell");
    $shell->setSize(320, 200);
    $shell->setLayout(new java("org.eclipse.swt.layout.FillLayout"));

    $button = new Java("org.eclipse.swt.widgets.Button", $this->shell, $SWT->PUSH);
    $button->setText("Click here.");
    $button->addSelectionListener(java_closure($this, null, java("org.eclipse.swt.events.SelectionListener")));
    $this->shell->open();
  }

  function run() {
    $this->init();
    while (!$this->shell->isDisposed()) {
      if (!$this->display->readAndDispatch()) {
	$this->display->sleep();
      }
    }
    $this->display->dispose();
  }
}

java_require("swt.jar");
$demo = new ButtonDemo(new Java ("org.eclipse.swt.widgets.Display"), 
		       new Java ("org.eclipse.swt.widgets.Shell"));
$demo->run();

?>
