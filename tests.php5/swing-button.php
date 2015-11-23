#!/usr/bin/php -nq

<?php 

// PLEASE DO NOT USE SWING TO CREATE A JAVA GUI. If you want to
// create a GUI application, please use SWT or GTK, please see
// the gtk-button.inc, gtk-fileselector.php and swt-button.php
// examples.

// Please see the server/tests/SwingTest.java and
// server/tests/SwingTest.php for a better swing example. It uses java
// as an "execution environment" for php scripts and uses System.exit()
// to avoid issues with swing's background threads.

include_once ("java/Java.inc");
ini_set("max_execution_time", 0);
if($argc<2) {
  echo "No automatic test. Use php swing-button.php --force to run this test.\n";
  exit(0);
 }

/**
 * Swing's native peers are switched off by defaul t (we don't want to
 * run in one of the many swing related bugs or crashes).
 * Switch it on for this test.
 */
$System=new java("java.lang.System");
$System->setProperty("java.awt.headless", "false");

class SwingApplication {
  var $labelPrefix = "Button clicks: "; 
  var $numClicks = 0; 
  var $label;
  var $frame;
  
  function actionPerformed($e) {
    echo "action performed called\n";
    $this->numClicks++; 
    $this->label->setText($this->labelPrefix . $this->numClicks);
  } 

  function createComponents() { 
    $button = new java("javax.swing.JButton", "I'm a Swing button!"); 

    // set the label before we close over $this
    $this->label = new java("javax.swing.JLabel");
    $button->addActionListener(java_closure($this, null, java("java.awt.event.ActionListener")));

    $this->label->setLabelFor($button); 
    $pane = new java("javax.swing.JPanel", new java("java.awt.GridLayout", 0, 1)); 
    $pane->add($button); 
    $pane->add($this->label);
    $BorderFactory = new JavaClass("javax.swing.BorderFactory");
    $pane->setBorder($BorderFactory->createEmptyBorder(30,30,10,30)); 
    return $pane; 
  } 

  function init() { 
    $this->frame = $frame = new java("javax.swing.JFrame", "SwingApplication");
    $frame->setDefaultcloseOperation($frame->EXIT_ON_CLOSE);
    $contents = $this->createComponents();
    $contentPane = $frame->getContentPane();
    $BorderLayout = new JavaClass("java.awt.BorderLayout");
    $contentPane->add($contents, $BorderLayout->CENTER);
    $frame->pack(); 
  } 

  function run() {
    $this->frame->setVisible(true); 
 } 
} 

$swing = new SwingApplication();
$swing->init();
$SwingUtilities = new JavaClass("javax.swing.SwingUtilities");
$SwingUtilities->invokeAndWait(java_closure($swing, null, java("java.lang.Runnable")));

// Due to swings insane design we don't know when the UI thread
// terminates. It may even be that the thread and therefore the VM
// never terminates, for example if a PrinterJob has been created on
// solaris, see the extensive number of related swing bugs.  The only
// reliable way to terminate a swing application is to call
// System.exit(..) which terminates all threads at once. So while we
// must terminate the whole server by calling System.exit(), we can
// wait here forever until the communication channel breaks.  If this
// happens the low-level php protocol code automatically calls
// exit(6). (If use use a php version with debug symbols, it will
// abort and dump core instead, but that's what we expect). -- To
// repeate the above statement: PLEASE DO NOT USE SWING TO CREATE A
// GUI, please use SWT, GTK or any other toolkit instead!
$Thread = new JavaClass("java.lang.Thread");
// don't forget to make the current thread a daemon thread, otherwise
// the VM will not exit because it waits for the thread to terminate,
// see discussion above.
while(true) {
  $Thread->sleep(99999);
}
?>
