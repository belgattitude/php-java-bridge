<?php
ini_set("max_execution_time", 0);

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

java_context()->call(java_closure(new SwingApplication())) || die("must be called from java");

// The php-invocable engine automatically adds java_context()->call(java_closure()) 
// to the bottom of each invocable script. Explicitly call exit(0) to avoid calling
// the Java continuation again.
echo "terminating";
exit(0);
?>
