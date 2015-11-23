#!/usr/bin/php -nq

# To run the following example gtk-sharp ver. 2.10, (key# 35e10195dab3c99f)
# must be installed.

<?php
require_once ("mono/Mono.inc");
ini_set("max_execution_time", 0);

class GtkDemo {
  var $Application;

  function GtkDemo() {
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

  function delete($sender, $e) {
    echo "delete called\n";
    $this->Application->Quit();
  }
  function clicked($sender, $e) {
    echo "clicked\n";
    $win = new Mono("Gtk.Window", "phpinfo()");
    $win->set_DefaultWidth(640);
    $win->set_DefaultHeight(400);
    $pane = new Mono("Gtk.ScrolledWindow");

    $view = new Mono("Gtk.TextView");
    $buffer = new Mono("Gtk.TextBuffer", new Mono("Gtk.TextTagTable"));
    ob_start();
    phpinfo();
    $buffer->set_Text(ob_get_contents());
    ob_end_clean();
    $view->set_Buffer($buffer);
    $pane->add($view);
    $win->add($pane);
    $win->ShowAll();
  }
  function init() {
    $this->Application = $Application = new Mono("Gtk.Application");
    $Application->Init();

    $win = new Mono("Gtk.Window", "Hello");
    $win->add_DeleteEvent (new Mono("Gtk.DeleteEventHandler", mono_closure($this, "delete", mono('Gtk.DeleteEventHandler$Method'))));

    $btn = new Mono("Gtk.Button", "Show output from phpinfo()");

    $btn->add_Clicked(new Mono("System.EventHandler", mono_closure($this, "clicked", mono('System.EventHandler$Method'))));
    $win->Add($btn);
    $win->ShowAll();
  }

  function run() {
    $this->init();
    $this->Application->Run();
  }
}

$demo = new GtkDemo();
$demo->run();

?>
