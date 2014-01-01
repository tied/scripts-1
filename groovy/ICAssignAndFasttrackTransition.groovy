package com.onresolve.jira.groovy.canned.workflow.postfunctions

import com.atlassian.crowd.embedded.api.User
import com.onresolve.jira.groovy.canned.utils.ConditionUtils
import com.atlassian.jira.issue.MutableIssue
import com.onresolve.jira.groovy.canned.CannedScript
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.event.type.EventDispatchOption.*

class ICAssignAndFasttrackTransition extends FasttrackTransition implements CannedScript {
    String getName() {
        return "Assign issue to current logged-in user and then fast-track transition an issue"
    
    }
    Map doScript(Map params) {
        MutableIssue issue = params['issue'] as MutableIssue

        Boolean doIt = ConditionUtils.processCondition(params[ConditionUtils.FIELD_CONDITION] as String, issue, false, params)
        if (! doIt) {
            return [:]
        }

        issue.assignee = componentManager.getJiraAuthenticationContext().getUser()
        componentManager.getIssueManager().updateIssue(issue.assignee, issue, EventDispatchOption.DO_NOT_DISPATCH, false)
        
        params = super.doScript (params)

        return params
    }
}