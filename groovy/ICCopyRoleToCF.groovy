package com.onresolve.jira.groovy.canned.workflow.postfunctions

import com.atlassian.jira.ComponentManager
import com.atlassian.jira.issue.comments.CommentManager
import com.opensymphony.workflow.WorkflowContext
import org.apache.log4j.Category
import com.atlassian.jira.config.SubTaskManager
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.security.roles.ProjectRoleManager
import com.atlassian.jira.security.roles.ProjectRole
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.util.ErrorCollection
import com.atlassian.jira.util.SimpleErrorCollection
import com.onresolve.jira.groovy.canned.CannedScript
import com.onresolve.jira.groovy.canned.utils.CannedScriptUtils
import com.onresolve.jira.groovy.canned.utils.WorkflowUtils
import com.opensymphony.workflow.WorkflowContext
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.event.type.EventDispatchOption.*
import com.atlassian.crowd.embedded.api.User


class ICCopyRoleToCF implements CannedScript{
    ComponentManager componentManager = ComponentManager.getInstance()
    Category log = Category.getInstance(ICCopyRoleToCF.class)
    public final static String FIELD_ISSUE_ID = "FIELD_ISSUE_ID"
    public final static String FIELD_ROLE_ID = "FIELD_ROLE_ID"
    
    String getName() {
        return "Assign to role"
    }

    String getDescription() {
        return """Update a custom-field value with the text name of a given role<br>
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
                Name:FIELD_ROLE_ID,
                Label:"Role to set value to",
                Description:"String version of role that the field should be populated with",
                Type: "list",
                Values: getRoles(true),
            ]
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
    
    Map getRoles(boolean withBlankFirstEntry) {
        Map<String,String> rt = [:] as Map<String,String>
        if (withBlankFirstEntry) {
            rt.put("", "")
        }
        
        ProjectRoleManager projectRoleManager = (ProjectRoleManager) componentManager.getComponentInstanceOfType(ProjectRoleManager.class);
        projectRoleManager.getProjectRoles().each {
            ProjectRole role ->
            rt.put(role.id, role.name)
        }
        rt
    }  
    
    public ErrorCollection doValidate(Map params, boolean forPreview) {
        ErrorCollection errorCollection = new SimpleErrorCollection()
        if (!params[FIELD_ISSUE_ID]) {
            errorCollection.addError(FIELD_ISSUE_ID, "You must provide the target issue.")
        }
        if (!params[FIELD_ROLE_ID]) {
            errorCollection.addError(FIELD_ROLE_ID, "You must provide the target role.")
        }
        return errorCollection
    }

    Map doScript(Map params) {
        MutableIssue issue = params['issue'] as MutableIssue
        String currentUser
        Map transientVars = params['transientVars'] as Map
        if (transientVars) {
            currentUser = ((WorkflowContext) transientVars.get("context")).getCaller();
        }
        else {
            currentUser = componentManager.getJiraAuthenticationContext().getUser().getName()
        }
        User currentUserObj = componentManager.getUserUtil().getUser(currentUser);

        def cf = componentManager.getCustomFieldManager().getCustomFieldObject(params[FIELD_ISSUE_ID] as Long)
        def role = componentManager.getComponentInstanceOfType(ProjectRoleManager.class).getProjectRole(params[FIELD_ROLE_ID] as Long)
        
        log.debug("Copying " + role.name + " to field " + cf.name)
        
        issue.setCustomFieldValue(cf, role.name)
        componentManager.getIssueManager().updateIssue(currentUserObj, issue, EventDispatchOption.DO_NOT_DISPATCH, false)
        
        return params
    }
    
    String getDescription(Map params, boolean forPreview) {
        ProjectRoleManager projectRoleManager = (ProjectRoleManager) componentManager.getComponentInstanceOfType(ProjectRoleManager.class);
        getName() +  " : Copy the role <b>" + projectRoleManager.getProjectRole(params[FIELD_ROLE_ID] as Long).getName() + "</b> to custom field <b>" +
                     componentManager.getCustomFieldManager().getCustomFieldObject(params[FIELD_ISSUE_ID] as Long).getName() + "</b>"
    }

    public Boolean isFinalParamsPage(Map params) {
        true
    }
}