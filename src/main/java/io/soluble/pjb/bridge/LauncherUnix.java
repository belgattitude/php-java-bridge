package io.soluble.pjb.bridge;
public class LauncherUnix {
    private static final String data = "#!/bin/sh\n"+
"# php fcgi launcher\n"+
"#set -x\n"+
"\n"+
"\"$@\" 1>&2 &\n"+
"trap \"kill $! && exit 0;\" 1 2 15\n"+
"read result 1>&2\n"+
"kill $!\n"+
"";
    public static final byte[] bytes = data.getBytes(); 
}
