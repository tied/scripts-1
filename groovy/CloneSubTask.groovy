package com.onresolve.jira.groovy.canned.workflow.postfunctions

import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.config.SubTaskManager
import org.apache.log4j.Category
import com.atlassian.crowd.embedded.api.User
import com.onresolve.jira.groovy.canned.utils.WorkflowUtils
import com.onresolve.jira.groovy.canned.CannedScript
import com.onresolve.jira.groovy.canned.utils.CannedScriptUtils
import com.onresolve.jira.groovy.canned.utils.ConditionUtils

class CloneSubTask extends CloneIssue implements CannedScript {
    Category log = Category.getInstance(CloneSubTask.class)
    
    String getName() {
        return "Clones a sub-task and link."
    }

    String getDescription() {
        return """Clones this sub-task and preserves link to parent task, optioninally in another project, and optionally a different issue type.  Can also link to original task.
        """
    }

    List getParameters(Map params) {
        [
            ConditionUtils.getConditionParameter(),
            [
                Name:FIELD_TARGET_PROJECT,
                Label:"Target Project",
                Type: "list",
                Description:"Target project. Leave blank for the same project as the source issue.",
                Values: CannedScriptUtils.getProjectOptions(true),
            ],
            [
                Name:FIELD_TARGET_ISSUE_TYPE,
                Label:"Target Issue Type",
                Type: "list",
                Description:"""Target issue type. Leave blank for the same issue type as the source issue.
                    <br>NOTE: This issue type must be valid for the target project""",
                Values: CannedScriptUtils.getAllSubTaskIssueTypes(true),
            ],
            getOverridesParam(),
            [
                Name:FIELD_LINK_TYPE,
                Label:'Issue Link Type',
                Type: "list",
                Description:"What link type to use to create a link to the cloned record.",
                Values: CannedScriptUtils.getAllLinkTypes(true)
            ],
        ]
    }
       
    Map doScript(Map params) {
        MutableIssue issue = params['issue'] as MutableIssue

        Boolean doIt = ConditionUtils.processCondition(params[ConditionUtils.FIELD_CONDITION] as String, issue, false, params)
        if (! doIt) {
            return [:]
        }

        
        
        params = super.doScript (params)

        if (issue.isSubTask()) {
            User user = WorkflowUtils.getUser(params)
            MutableIssue parent = issue.getParentObject() as MutableIssue
            Issue newIssue = params['newIssue'] as Issue
            
            componentManager.getSubTaskManager().createSubTaskIssueLink(parent, newIssue, user)
        }
        
        params
    }

}