<?php header("Content-type: text/html; charset=UTF-8"); ?>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<title>Hello</title>
<body>
<TABLE border='1'>
<tr>
<th>English</th>
<th><?php 
  $lang=$_GET['language']; 
  if(!isset($lang)) $lang="cs_CZ";
  echo $lang?>
</th>
</tr>

<tr>
<td>Hello</td>


<td>
<?php
if(!function_exists("bindtextdomain")) die("function bindtextdomain not found. Please install language support, see http://php.net for details");
bindtextdomain("hello", "./locale");
textdomain("hello");
setlocale(LC_ALL, $lang);
echo _("hello");
?>
</td>
</tr>
</TABLE>

<form>

<p>
<select name='language'>
<option value="cs_CZ">cs_CZ (Czech)</option>
<option value="de_DE">de_DE (German)</option>
<option value="he_IL">he_IL (Hebrew)</option>
<option value="ja_JP">ja_JP (Japanese)</option>
</select>
<p>
<input type="submit" default="cs_CZ" value="Update"></input>

</form>
</body>
</html>
