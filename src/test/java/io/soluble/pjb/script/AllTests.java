package io.soluble.pjb.script;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
    
    public static TestSuite suite;

    public static Test suite() {
        suite = new TestSuite("Test for io.soluble.pjb.test");
        //$JUnit-BEGIN$
        suite.addTestSuite(TestException.class);
        suite.addTestSuite(TestGetResult.class);
        suite.addTestSuite(TestSimpleCompileable.class);
        suite.addTestSuite(TestGetInterface.class);
        suite.addTestSuite(TestScript.class);
        suite.addTestSuite(TestInvocablePhpScriptEngine.class);
        suite.addTestSuite(TestExceptionInvocable.class);
        suite.addTestSuite(TestSetWriter.class);
        suite.addTestSuite(TestInteractiveRequestAbort.class);
        suite.addTestSuite(TestExceptionInvocable2.class);
        suite.addTestSuite(TestSimpleInvocation.class);
        suite.addTestSuite(TestInvocable.class);
        suite.addTestSuite(TestURLReader.class);
        suite.addTestSuite(TestCli.class);
        suite.addTestSuite(TestPhpScriptEngine.class);
        suite.addTestSuite(TestDiscovery.class);
        suite.addTestSuite(TestBindings.class);
        //$JUnit-END$
        return suite;
    }

}
