/*
* Copyright 2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.util.Hashtable;
import java.io.Writer;
import java.io.IOException;

/**
 * Example1: the simplest tag
 * Collect attributes and call into some actions
 *
 * <foo att1="..." att2="...." att3="...." />
 */

/*
* Copyright 2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

abstract class ExampleTagBase extends BodyTagSupport {

    public void setParent(Tag parent) {
        this.parent = parent;
    }

    public void setBodyContent(BodyContent bodyOut) {
        this.bodyOut = bodyOut;
    }

    public void setPageContext(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    public Tag getParent() {
        return this.parent;
    }
    
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }
    

    // Default implementations for BodyTag methods as well
    // just in case a tag decides to implement BodyTag.
    public void doInitBody() throws JspException {
    }

    public int doAfterBody() throws JspException {
        return SKIP_BODY;
    }

    public void release() {
        bodyOut = null;
        pageContext = null;
        parent = null;
    }
    
    protected BodyContent bodyOut;
    protected PageContext pageContext;
    protected Tag parent;
}

public class FooTag 
    extends ExampleTagBase 
{
    private String atts[] = new String[3];
    int i = 0;
    
    private final void setAtt(int index, String value) {
        atts[index] = value;
    }
    
    public void setAtt1(String value) {
        setAtt(0, value);
    }
    
    public void setAtt2(String value) {
        setAtt(1, value);
    }

    public void setAtt3(String value) {
        setAtt(2, value);
    }
    
    /**
     * Process start tag
     *
     * @return EVAL_BODY_INCLUDE
     */
    public int doStartTag() throws JspException {
        i = 0;
	return EVAL_BODY_INCLUDE;
    }

    public void doInitBody() throws JspException {
        pageContext.setAttribute("member", atts[i]);
        i++;
    }
    
    public int doAfterBody() throws JspException {
        try {
            if (i == 3) {
                bodyOut.writeOut(bodyOut.getEnclosingWriter());
                return SKIP_BODY;
            } else
                pageContext.setAttribute("member", atts[i]);
            i++;
            return EVAL_BODY_AGAIN;
        } catch (IOException ex) {
            throw new JspTagException(ex.toString());
        }
    }
}
