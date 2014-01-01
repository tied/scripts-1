package com.onresolve.jira.groovy.canned.workflow.postfunctions

import com.atlassian.jira.ComponentManager
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.util.ErrorCollection
import com.atlassian.jira.util.SimpleErrorCollection
import com.atlassian.jira.workflow.JiraWorkflow
import com.onresolve.jira.groovy.canned.CannedScript
import com.onresolve.jira.groovy.canned.utils.CannedScriptUtils
import com.onresolve.jira.groovy.canned.utils.WorkflowUtils
import org.apache.log4j.Category
import com.atlassian.crowd.embedded.api.User
import com.atlassian.jira.issue.index.IssueIndexManager
import com.atlassian.jira.issue.link.IssueLinkManager
import com.atlassian.jira.issue.link.IssueLinkType
import com.atlassian.jira.issue.link.IssueLinkTypeManager
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.*

class LinkToIssue implements CannedScript{

    ComponentManager componentManager = ComponentManager.getInstance()
    Category log = Category.getInstance(LinkToIssue.class)
    def projectManager = componentManager.getProjectManager()
    public final static String FIELD_ISSUE_ID = "FIELD_ISSUE_ID"
    public final static String FIELD_LINK_TYPE = "FIELD_LINK_TYPE"

    String getName() {
        return "Create a link to another issue"
    }

    String getDescription() {
        return """This will create a link to another issue<br>
        """
    }

    List getCategories() {
        ["Function"]
    }

    List getParameters(Map params) {
        [
            [
                Name:FIELD_ISSUE_ID,
                Label:"Field to link to",
                Description:"Field containing the issue key to create the link to",
                Type: "list",
                Values: getFields(true),
            ],
            [
                Name:FIELD_LINK_TYPE,
                Label:'Issue Link Type',
                Type: "list",
                Description:"What link type to use to create a link to the cloned record.",
                Values: CannedScriptUtils.getAllLinkTypes(true)
            ],
            // todo: need to allow a sub-task resolution
        ]

    }
    
    Map getFields(boolean withBlankFirstEntry) {
        Map<String,String> rt = [:] as Map<String,String>
        if (withBlankFirstEntry) {
            rt.put("", "")
        }
        
        componentManager.getCustomFieldManager().getCustomFieldObjects().each { CustomField cf ->
            rt.put(cf.getIdAsLong(), cf.getName())
        }
        rt
    }   

    public ErrorCollection doValidate(Map params, boolean forPreview) {
        ErrorCollection errorCollection = new SimpleErrorCollection()
        IssueLinkManager linkMgr = componentManager.getIssueLinkManager()
        if (!linkMgr.isLinkingEnabled()) {
            errorCollection.addError(FIELD_LINK_TYPE, "Issue linking has been disabled in JIRA.  You must enable it to use this function.")
        }
        if (!params[FIELD_ISSUE_ID]) {
            errorCollection.addError(FIELD_ISSUE_ID, "You must provide the target issue.")
        }
        if (!params[FIELD_LINK_TYPE]) {
            errorCollection.addError(FIELD_LINK_TYPE, "You must provide the target link type.")
        }
        return errorCollection
    }

    Map doScript(Map params) {
        MutableIssue issue = params['issue'] as MutableIssue
        User user = WorkflowUtils.getUser(params)
        String linkTypeId = params[FIELD_LINK_TYPE] as String
        def cf = componentManager.getCustomFieldManager().getCustomFieldObject(params[FIELD_ISSUE_ID] as Long)
        def value = issue.getCustomFieldValue(cf) as String
        Issue target = componentManager.getIssueManager().getIssueObject(value)
        
        if (!target) {
            log.debug("No issue \"$value\" found.  Aborting.")
            return params
        }
        
        // get the current list of outwards depends on links to get the sequence number
        IssueLinkManager linkMgr = componentManager.getIssueLinkManager()

        if (linkTypeId && linkMgr.isLinkingEnabled()) {
            IssueLinkTypeManager issueLinkTypeManager = (IssueLinkTypeManager) ComponentManager.getComponentInstanceOfType(IssueLinkTypeManager.class)

            IssueLinkType linkType = issueLinkTypeManager.getIssueLinkType(linkTypeId as Long)

            if (linkType) {
                linkMgr.createIssueLink (issue.id, target.getId(), linkType.id, 0, user)
            }
            else {
                log.warn ("No link type $linkTypeId found")
            }
        }
        
        return params
    }

    String getDescription(Map params, boolean forPreview) {
        getName() +  " : Create a link to <b>" + componentManager.getCustomFieldManager().getCustomFieldObject(params[FIELD_ISSUE_ID] as Long).getName() + "</b> is globally unique"
    }

    public Boolean isFinalParamsPage(Map params) {
        true
    }
}
