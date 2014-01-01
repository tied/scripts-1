package com.onresolve.jira.groovy.canned.workflow.postfunctions

import com.onresolve.jira.groovy.canned.CannedScript
import com.onresolve.jira.groovy.canned.utils.CannedScriptUtils
import com.onresolve.jira.groovy.canned.utils.WorkflowUtils
import com.atlassian.jira.ComponentManager
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.util.ErrorCollection
import org.apache.log4j.Category
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.CustomFieldManager
import com.onresolve.jira.groovy.user.customfields.ICLastUpdatedImpl
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.event.type.EventDispatchOption.*
import com.atlassian.crowd.embedded.api.User
import com.atlassian.jira.user.util.UserUtil
import com.opensymphony.workflow.WorkflowContext

class ICCopyUpdateFields implements CannedScript{
    ComponentManager componentManager = ComponentManager.getInstance()
    Category log = Category.getInstance(ICCopyUpdateFields.class)
    private final String UPDATE_TYPE_FIELD = "Update Type"
    
    private def newFields = ["New Installer":        ["ToS", "EULA", "Privacy Policy", "Design Requirements", "InstallCore Signed", "Language(s)", "Customer EXE", "Account Name",
                                                      "Customer EXE Parameters", "Customer EXE Install Checker", "Product Description", "Offer Count", "Offer Search",
                                                      "Offer Categories", "Offer Blacklist", "Thank You Page","Important Notes"],
                             "New DLM": ["ToS", "EULA", "Privacy Policy", "Design Requirements", "InstallCore Signed", "Language(s)", "Customer EXE", "Account Name",
                                                      "DLM Programming Language", "DLM Parameters", "Offer Count", "Offer Search", "Offer Categories", "Offer Blacklist","Important Notes"],
                             "New Offer":            ["ToS", "EULA", "Privacy Policy", "Design Requirements", "InstallCore Signed", "Language(s)", "Customer EXE", "Account Name",
                                                      "Customer EXE Parameters", "Customer EXE Install Checker", "Product Description","Important Notes"]
                            ];
    
    String getName() {
        return "Copy fields from original project"
    }

    String getDescription() {
        return """Copies InstallCore-specific custom fields from the issue identified by the \"" + LAST_UPDATE_FIELD + \" custom-field<br>
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
        CustomField cf = componentManager.getCustomFieldManager().getCustomFieldObjectByName(UPDATE_TYPE_FIELD)
        
        if (issue.getCustomFieldValue(cf) == null) {
            log.debug(UPDATE_TYPE_FIELD + " was not set.  Aborting.")
            return params
        }
        
        ICLastUpdatedImpl c = new ICLastUpdatedImpl()
        String latestKey = c.getLastUpdated(issue, false)
        
        Issue latestUpdate = componentManager.getIssueManager().getIssueObject(latestKey)
        String type = issue.issueTypeObject.name

        String currentUser
        Map transientVars = params['transientVars'] as Map
        if (transientVars) {
            currentUser = ((WorkflowContext) transientVars.get("context")).getCaller();
        }
        else {
            currentUser = componentManager.getJiraAuthenticationContext().getUser().getName()
        }
        User currentUserObj = componentManager.getUserUtil().getUser(currentUser);
        
        log.debug("Usiung $type as issue type")
        log.debug("Using " + latestUpdate.key + " as original issue")
        
        newFields[type].each() {
            field ->
            log.debug("Copying value for $field")
            cf = componentManager.getCustomFieldManager().getCustomFieldObjectByName(field)
            log.debug(latestUpdate.getCustomFieldValue(cf))
            issue.setCustomFieldValue(cf, latestUpdate.getCustomFieldValue(cf))
        }
        
        componentManager.getIssueManager().updateIssue(currentUserObj, issue, EventDispatchOption.DO_NOT_DISPATCH, false)
        
        return params
    }
    
    String getDescription(Map params, boolean forPreview) {
        getName()
    }

    public Boolean isFinalParamsPage(Map params) {
        true
    }
}