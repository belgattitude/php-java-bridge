<?php

  /**
   * These classes allow one to call JSP tags from PHP.
   * Example:
   * <code>
   * $tag = new java_Tag($ctx=new java_PageContext(), "FooTag, null);<br>
   * if($tag-&gt;start()) do { ... } while($tag-&gt;repeat()); <br>
   * $tag-&gt;end();<br>
   * </code>
   */
if(!class_exists("java_Tag")) {
  class java_PageContext {
    var $servlet = null;
    var $response = null;
    var $request = null;
    var $out = null;
    var $pc = null;

    function java_PageContext($java_session) {
      if (java_is_null($java_session)) throw new Exception ("session is null");
      $ctx = java_context();
      $this->servlet = $ctx->getAttribute("php.java.servlet.Servlet");
      $this->response = $ctx->getAttribute("php.java.servlet.HttpServletResponse");
      $this->request = $ctx->getAttribute("php.java.servlet.HttpServletRequest");

      $factory = java("javax.servlet.jsp.JspFactory")->getDefaultFactory();
      $this->pc = $factory->getPageContext($this->servlet, $this->request, $this->response, null, true, 8192, false);
      $this->out = $this->pc->out;

    }

    function getPageContext () {
      return $this->pc;
    }
  }
  class java_Tag {
    var $clazz = null;
    var $pc = null;
    var $noend = false;
    var $evalTag = null;

    /**
     * Create a java tag proxy.
     * @param $pageContext, the java_PageContext object
     * @param $tagname The name of the tag
     * @param $params The parameters
     */
    function java_Tag($pageContext, $tagname, $params = null) {
      $this->pc = $pageContext;
      $this->clazz = new Java($tagname);
      $this->clazz->setPageContext($pageContext->pc);
      if(null != $params) {
	foreach($params as $k => $v) {
	  $method = "set";
	  $s = substr($k, 0, 1);
	  $method .= $s.substr($k, 1);
	  $this->clazz->$method($v);
	}
      }
    }

    /**
     * Must be called to open the tag.
     * @return true, if the body should be evaluated.
     */
    function start() {
      $this->evalTag = java_values($this->clazz->doStartTag());
      if($this->evalTag != java_values(Java("javax.servlet.jsp.tagext.Tag")->SKIP_BODY)) {
	$this->noend=true;
	if($this->evalTag == java_values(Java("javax.servlet.jsp.tagext.Tag")->EVAL_BODY_INCLUDE)) {
	  $out = $this->pc->pc->pushBody();
	  $this->clazz->setBodyContent($out);
	  $this->clazz->doInitBody();
	}
	return true;
      }
      return false;
    }

    /**
     * Must be called at the end of the body.
     * @return true if the body should be evaluated again.
     */
    function repeat() {
      $evalDoAfterBody = java_values($this->clazz->doAfterBody());
      if ($evalDoAfterBody != java_values(Java("javax.servlet.jsp.tagext.BodyTag")->EVAL_BODY_AGAIN)) {
	return false;
      }

      return true;
    }

    /**
     * Must be called at the end.
     */
    function end($autoflush = true) {
      if(!$this->noend) {
	if ($this->evalTag != java_values(Java("javax.servlet.jsp.tagext.Tag")->EVAL_BODY_INCLUDE)) {
	  $this->pc->pc->popBody();
	}
      }
      $this->clazz->doEndTag();
      if($autoflush ) {
	$this->pc->out->flush();
	echo java_values($this->pc->response->getBufferContents());
      }
      return true;
    }
  }
 }

?>