
;; PHP/Java Bridge test client written in Chicken/SCHEME.
;; Compile/run this example with: csc -lxslt getProperties.scm; ./getProperties >out.html
;; Then visit out.html with a web browser

(declare (uses tcp posix))

;; wrapper for libxslt
(define transform 
  (lambda (buf style-sheet)
    ;; declare the foreign methods ..
    (define xmlSubstituteEntitiesDefault 
      (foreign-lambda void "xmlSubstituteEntitiesDefault" int))
    (define xsltParseStylesheetFile 
      (foreign-lambda c-pointer "xsltParseStylesheetFile" c-string))
    (define  xmlParseMemory
      (foreign-lambda c-pointer "xmlParseMemory" c-string int))
    (define  xsltApplyStylesheet
      (foreign-lambda c-pointer "xsltApplyStylesheet" c-pointer c-pointer c-pointer))
    (define xsltSaveResultTo 
      (foreign-lambda void "xsltSaveResultTo" c-pointer c-pointer c-pointer))
    (define xmlOutputBufferCreateFd
      (foreign-lambda c-pointer "xmlOutputBufferCreateFd" int c-pointer))

    ;; .. and call them
    (define default (xmlSubstituteEntitiesDefault 1))
    (define transform (xsltParseStylesheetFile style-sheet))
    (define doc (xmlParseMemory buf (string-length buf)))
    (define res (xsltApplyStylesheet transform doc #f))
    (define stdout (xmlOutputBufferCreateFd 1 #f))
    (xsltSaveResultTo stdout res transform)))



(define HOST "127.0.0.1")
(define PORT 9267)

(define-values (inp outp) (tcp-connect HOST PORT))
(define buf (make-string 65535))
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

;; access java.lang.System
(display "<C value=\"java.lang.System\" p=\"C\"></C>" outp)
(read-document buf inp)

;; ... and ask for getProperties
(display "<I value=\"1\" m=\"getProperties\" p=\"Invoke\"></I>" outp)
(read-document buf inp)
(display "<I value=\"0\" m=\"getValues\" p=\"Invoke\"><Object v=\"2\"/></I>" outp)
(let ((count (read-document buf inp)))
  ;; format the result
  (transform (substring buf 0 count) "formatArray.xsl") 
  (newline))
