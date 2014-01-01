package com.onresolve.jira.groovy.canned.workflow.postfunctions

import com.onresolve.jira.groovy.canned.CannedScript
import com.onresolve.jira.groovy.canned.utils.CannedScriptUtils
import com.onresolve.jira.groovy.canned.utils.WorkflowUtils
import com.atlassian.jira.ComponentManager
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.util.ErrorCollection
import org.apache.log4j.Category
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.crowd.embedded.api.User

class ICCopyFieldIfNotSet implements CannedScript{
    ComponentManager componentManager = ComponentManager.getInstance()
    Category log = Category.getInstance(ICCopyFieldIfNotSet.class)
    
    String getName() {
        return "Set Account Manager to reporter"
    }

    String getDescription() {
        return """Copy the value of the <em>Reporter</em> field to the <em>Account Managers</em> field.<br>
        """
    }

    List getCategories() {
        ["Function"]
    }

    List getParameters(Map params) {
        []
    }
    
    public ErrorCollection doValidate(Map params, boolean forPreview) {
        // todo: check this is on a sub-task type
        null
    }

    Map doScript(Map params) {
        MutableIssue issue = params['issue'] as MutableIssue
        CustomField cf = componentManager.getCustomFieldManager().getCustomFieldObjectByName("Account Manager")
        
        if (issue.getCustomFieldValue(cf) == null) {
            issue.setCustomFieldValue(cf, issue.reporter);
        }    
        return params
    }
    
    String getDescription(Map params, boolean forPreview) {
        getName()
    }

    public Boolean isFinalParamsPage(Map params) {
        true
    }
}