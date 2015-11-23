
;; PHP/Java Bridge test client written in Chicken/SCHEME.
;; Run this example with: csi listToArray

(load-library 'tcp)
(load-library 'posix)

(define HOST "127.0.0.1")
(define PORT 9267)

(define-values (inp outp) (tcp-connect HOST PORT))
(define buf (make-string 65535))

;; read chars until the top-level <.*/> was received 
(define read-document(lambda (buf inp)
    (let* ((last #\  )
	   (level 0)
	   (quote #f)
	   (more
	    (lambda (c)
	      (let ((more-chars 
		     (lambda (c)
		       (cond 
			( [char=? #\/ c] [set! level (- level (if (char=? #\< last) 2 1))] )
			( [char=? #\< c] [set! level (+ 1 level)] ))
		       (set! last c)
		       (and (char=? #\> c) (= 0 level)))))

		(if quote 
		    (begin (if (char=? #\" c) (begin (set! quote #f) #f) #f)) 
		    (begin (if (char=? #\" c) (begin (set! quote #t) #f) (more-chars c))) )))))
      
      (do ((i 0 (+ 1 i)) (c (read-char inp) (read-char inp)))
	  ((more c) (begin (string-set! buf i c) (+ i 1)))
	(begin (string-set! buf i c))))))

;; real work starts here

;; header: prefer values, not base 64 encoded
(display (integer->char #o177) outp) 
(display (integer->char #o101) outp)

;; create a java.util.ArrayList, add 3 entries to it ...
(display "<C value=\"java.util.ArrayList\" p=\"I\"></C>" outp)
(read-document buf inp) ;discard received document
(display "<I value=\"1\" method=\"add\" p=\"I\"><String v=\"ENTRY 1\"/></I>" outp)
(read-document buf inp)
(display "<I value=\"1\" method=\"add\" p=\"I\"><String v=\"ENTRY 2\"/></I>" outp)
(read-document buf inp)
(display "<I value=\"1\" method=\"add\" p=\"I\"><String v=\"LAST ENTRY\"/></I>" outp)
(read-document buf inp)

;; ... and ask for the array
(display "<I value=\"1\" m=\"toArray\" p=\"Invoke\"></I>" outp)
(read-document buf inp)
(display "<I value=\"0\" m=\"getValues\" p=\"Invoke\"><Object v=\"2\"/></I>" outp)
(let ((count (read-document buf inp)))
  (display "Received:") (newline)
  ;; should have received an array of three values
  (display (substring buf 0 count))
  (newline))

(exit 0)

