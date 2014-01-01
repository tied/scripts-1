package com.onresolve.jira.groovy.canned.workflow.postfunctions

import com.atlassian.jira.ComponentManager
import org.apache.log4j.Category
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.util.ErrorCollection
import com.atlassian.jira.util.SimpleErrorCollection
import com.onresolve.jira.groovy.canned.CannedScript
import com.onresolve.jira.groovy.canned.utils.CannedScriptUtils
import com.onresolve.jira.groovy.canned.utils.WorkflowUtils
import com.atlassian.crowd.embedded.api.User
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.event.type.EventDispatchOption.*
import com.atlassian.jira.config.ConstantsManager

class UpdateParentTaskField implements CannedScript {
    public static final String FIELD_CUSTOM_FIELD = 'FIELD_CUSTOM_FIELD'
    public static final String FIELD_CUSTOM_VALUE = 'FIELD_CUSTOM_VALUE'
    
    ComponentManager componentManager = ComponentManager.getInstance()
    Category log = Category.getInstance(UpdateParentTaskField.class)
    
    String getName() {
        return "Modify parent task field"
    }

    String getDescription() {
        return """Modifies a field in the parent task<br>
        """
    }

    List getCategories() {
        ["Function"]
    }

    List getParameters(Map params) {
        [
            [
                Name:FIELD_CUSTOM_FIELD,
                Label:"Target Field",
                Type: "list",
                Description:"""Custom field to modify
                    """,
                Values: getAllICFields(true),
            ],
            [
                Name:FIELD_CUSTOM_VALUE,
                Label:"Target Value",
                Description:"""Value to assign to custom field
                    """,
            ],
            
        ]

    }
    
    Map getAllICFields(boolean withBlankFirstEntry) {
        Map<String,String> rt = [:] as Map<String,String>
        if (withBlankFirstEntry) {
            rt.put("", "")
        }
        
        componentManager.getCustomFieldManager().getCustomFieldObjects(componentManager.getProjectManager().getProjectObjByKey("IC").getId(), ConstantsManager.ALL_STANDARD_ISSUE_TYPES).each { CustomField cf ->
            rt.put(cf.getIdAsLong(), cf.getName())
        }
        rt
    } 
    
    public ErrorCollection doValidate(Map params, boolean forPreview) {
        ErrorCollection errorCollection = new SimpleErrorCollection()
        if (!params[FIELD_CUSTOM_FIELD]) {
            errorCollection.addError(FIELD_CUSTOM_FIELD, "You must provide the target issue.")
        }
        if (!params[FIELD_CUSTOM_VALUE]) {
            errorCollection.addError(FIELD_CUSTOM_VALUE, "You must provide the target role.")
        }
        return errorCollection
    }


    Map doScript(Map params) {
        MutableIssue issue = params['issue'] as MutableIssue
        MutableIssue parent = issue.getParentObject()
        
        if (!parent) {
            log.warn ("No parent task for [" + issue.key + "].  Not taking any action.")
            return params
        }
        
        User currentUser = componentManager.getJiraAuthenticationContext().getUser()
        def cf = componentManager.getCustomFieldManager().getCustomFieldObject(params[FIELD_CUSTOM_FIELD] as Long)
        
        log.debug("[" + parent.key + "] Setting " + cf.name + " to " + params[FIELD_CUSTOM_VALUE])
        
        parent.setCustomFieldValue(cf, (params[FIELD_CUSTOM_VALUE]).toString())
        componentManager.getIssueManager().updateIssue(currentUser, parent, EventDispatchOption.DO_NOT_DISPATCH, false)

        return params
    }
    
    String getDescription(Map params, boolean forPreview) {
        getName() +  " : Set the value in the parent task for field <b>" + componentManager.getCustomFieldManager().getCustomFieldObject(params[FIELD_CUSTOM_FIELD] as Long).getName() + "</b> to value <b>" +
                      params[FIELD_CUSTOM_VALUE] + "</b>"
    }

    public Boolean isFinalParamsPage(Map params) {
        true
    }
}