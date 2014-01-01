package com.onresolve.jira.groovy.canned.workflow.postfunctions

import com.atlassian.jira.ComponentManager
import com.atlassian.jira.util.ErrorCollection
import com.atlassian.jira.util.SimpleErrorCollection
import com.onresolve.jira.groovy.canned.CannedScript
import org.apache.log4j.Category
import com.atlassian.jira.config.ConstantsManager
import com.atlassian.jira.config.SubTaskManager
import com.atlassian.jira.event.issue.IssueEvent
import com.atlassian.jira.issue.*
import com.atlassian.jira.issue.fields.CustomField

class UpdateSubTaskCount implements CannedScript{
    public static final String FIELD_SUBTASK_CUSTOM_FIELD = 'FIELD_SUBTASK_CUSTOM_FIELD'

    ComponentManager componentManager = ComponentManager.getInstance()
    Category log = Category.getInstance(UpdateSubTaskCount.class)
    CustomFieldManager customFieldManager = componentManager.getCustomFieldManager()

    String getName() {
        return "Update subtask count"
    }

    String getDescription() {
        return "A postfunction used for storing a snapshot of the current sub-task count to be compared against later"
    }

    List getCategories() {
        ["Function"]
    }


    List getParameters(Map params) {
        [
            [
                Name:FIELD_SUBTASK_CUSTOM_FIELD,
                Label:"Counter Field",
                Type: "list",
                Description:"""Use this custom field to store the current number of subtasks
                    """,
                Values: getAllICFields(true),
            ],
        ]

    }

    Map getAllICFields(boolean withBlankFirstEntry) {
        Map<String,String> rt = [:] as Map<String,String>
        if (withBlankFirstEntry) {
            rt.put("", "")
        }
        
        customFieldManager.getCustomFieldObjects(componentManager.getProjectManager().getProjectObjByKey("IC").getId(), ConstantsManager.ALL_STANDARD_ISSUE_TYPES).each { CustomField cf ->
            rt.put(cf.getIdAsLong(), cf.getName())
        }
        rt
    }  
    
    public ErrorCollection doValidate(Map params, boolean forPreview) {
        ErrorCollection errorCollection = new SimpleErrorCollection()
        if (!params[FIELD_SUBTASK_CUSTOM_FIELD]) {
            errorCollection.addError(FIELD_SUBTASK_CUSTOM_FIELD, "You must provide the target custom field.")
        }
        
        // To-do: ensure that the subtask is a compatible (numeric, text?) field type
        return errorCollection
    }

    Map doScript(Map params) {
        log.debug ("UpdateSubTaskCount.doScript with params: ${params}")
        Issue issue = params['issue'] as Issue
        def cf = ComponentManager.getInstance().getCustomFieldManager().getCustomFieldObject(params[FIELD_SUBTASK_CUSTOM_FIELD] as Long)
        log.debug ("Setting " + cf.getName() + " to " + issue.subTasks.size())
        issue.setCustomFieldValue(cf, issue.subTasks.size() as Double)

        return params
    }

    String getDescription(Map params, boolean forPreview) {
        return getName() +  " :  A snapshot number of sub-tasks for the issue will be stored in <b>" + customFieldManager.getCustomFieldObject(params[FIELD_SUBTASK_CUSTOM_FIELD] as Long).getName() + "</b>"
    }

    public Boolean isFinalParamsPage(Map params) {
        true
    }
}
