<?php require_once("java/Java.inc");

/* PHP version of numberguess.jsp */
$session = java_session();

if(java_is_null($numguess=$session->get("bean"))) {
  $session->put("bean", $numguess=new Java("num.NumberGuessBean"));
}
if($_POST['guess']) {
  $numguess->setGuess($_POST['guess']);
}
?>

<html>
<head><title>Number Guess</title></head>
<body bgcolor="white">
<font size=4>

<?php if(java_values($numguess->getSuccess())) { ?>
  Congratulations!  You got it.
  And after just <?php echo java_values($numguess->getNumGuesses()); ?> tries.<p>

  <?php $session->destroy(); ?>

  Care to <a href="numberguess.php">try again</a>?

<?php } else if (java_values($numguess->getNumGuesses()) == 0) { ?>

  Welcome to the Number Guess game.<p>

  I'm thinking of a number between 1 and 100.<p>

  <form method=post>
  What's your guess? <input type=text name=guess>
  <input type=submit value="Submit">
  </form>

<?php } else { ?>

  Good guess, but nope.  Try <b><?php echo java_cast($numguess->getHint(),"S") ?></b>.

  You have made <?php echo java_values($numguess->getNumGuesses()) ?> guesses.<p>

  I'm thinking of a number between 1 and 100.<p>

  <form method=post>
  What's your guess? <input type=text name=guess>
  <input type=submit value="Submit">
  </form>

<?php } ?>

</font>
</body>
</html>
