package com.onresolve.jira.groovy.canned.workflow.postfunctions

import com.atlassian.crowd.embedded.api.User
import com.onresolve.jira.groovy.canned.utils.ConditionUtils
import com.atlassian.jira.issue.MutableIssue
import com.onresolve.jira.groovy.canned.CannedScript
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.event.type.EventDispatchOption.*
import org.apache.log4j.Category

class ICAssignAndFasttrackTransitionParent extends FasttrackTransition implements CannedScript {
    Category log = Category.getInstance(ICAssignAndFasttrackTransitionParent.class)
    
    String getName() {
        return "Assign parent issue to current logged-in user and then fast-track transition parent issue"
    
    }
    Map doScript(Map params) {
        MutableIssue issue = params['issue'] as MutableIssue

        Boolean doIt = ConditionUtils.processCondition(params[ConditionUtils.FIELD_CONDITION] as String, issue, false, params)
        if (! doIt) {
            return [:]
        }

        if (issue.isSubTask()) {
            MutableIssue parent = issue.getParentObject() as MutableIssue
    
            params['issue'] = parent
            
            parent.assignee = componentManager.getJiraAuthenticationContext().getUser()
            componentManager.getIssueManager().updateIssue(parent.assignee, parent, EventDispatchOption.DO_NOT_DISPATCH, false)
            
            params = super.doScript (params)
            params['issue'] = issue
        } else {
            log.debug("Issue $issue is not a sub-task")
        }
        return params
    }
}