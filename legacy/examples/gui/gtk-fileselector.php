#!/usr/bin/php -nq

# To run the following example gtk-sharp ver. 2.10, (key# 35e10195dab3c99f)
# must be installed.

<?php
require_once ("mono/Mono.inc");
ini_set("max_execution_time", 0);

class GtkFileSelectorDemo {

  var $filew;

  function GtkFileSelectorDemo () {
    // mono_require("gtk-sharp", "2.0.0.0", "35e10195dab3c99f"); 

    // The following is equivalent to the above mono_require
    // statement. It shows how to load a library from the GAC.
    $Assembly=          mono("System.Reflection.Assembly");
    $assemblyName = new Mono("System.Reflection.AssemblyName");

    // Name is a property of AssemblyName, set_Name(...) calls the
    // setter, get_Name() calls the getter
    $assemblyName->set_Name("gtk-sharp");
    $assemblyName->set_Version(new Mono("System.Version", "2.12"));

    // pack converts the hex string into a byte array
    $assemblyName->setPublicKeyToken(pack("H16", "35e10195dab3c99f"));
    // load gtk-sharp 2.0.0.0 (35e10195dab3c99f)
    $Assembly->Load($assemblyName);
  }

  function ok($obj, $args) {
    echo "ok called\n";
    echo $this->filew->get_Filename() . "\n";
  }

  function quit($obj, $args) {
    echo "quit called\n";
    $this->Application->Quit();
  }

  function init() {
    $Application = $this->Application = mono("Gtk.Application");
    $Application->Init();

    $filew = $this->filew = new Mono("Gtk.FileSelection", "Open a file ...");
    $filew->add_DeleteEvent (new Mono("Gtk.DeleteEventHandler", mono_closure($this, "quit", mono('Gtk.DeleteEventHandler$Method'))));
    $b=$filew->get_OkButton();
    $b->add_Clicked (new Mono("System.EventHandler", mono_closure($this, "ok", mono('System.EventHandler$Method'))));
    $b=$filew->get_CancelButton();
    $b->add_Clicked (new Mono("System.EventHandler", mono_closure($this, "quit", mono('System.EventHandler$Method'))));
    $filew->set_Filename ("penguin.png");
    $filew->Show();
  }

  function run() {
    $this->init();
    $this->Application->Run();
  }
}
$demo=new GtkFileSelectorDemo();
$demo->run();

?>
