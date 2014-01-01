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
import com.atlassian.jira.util.ErrorCollection
import com.atlassian.jira.util.SimpleErrorCollection
import com.onresolve.jira.groovy.canned.CannedScript
import com.onresolve.jira.groovy.canned.utils.CannedScriptUtils
import com.onresolve.jira.groovy.canned.utils.WorkflowUtils
import com.atlassian.crowd.embedded.api.User

class ICStartSubtasks implements CannedScript{
    ComponentManager componentManager = ComponentManager.getInstance()
    Category log = Category.getInstance(ICStartSubtasks.class)
    
    String getName() {
        return "Allow developers to start working on subtasks"
    }

    String getDescription() {
        return """Workflow transition "Ready To Start" on all sub-tasks, effectively allowing work to begin<br>
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
        User currentUser = componentManager.getJiraAuthenticationContext().getUser()
        WorkflowTransitionUtil workflowTransitionUtil = ( WorkflowTransitionUtil ) JiraUtils.loadComponent( WorkflowTransitionUtilImpl.class );
         
        SubTaskManager subTaskManager = ComponentManager.getInstance().getSubTaskManager();
        Collection<MutableIssue> subTasks = issue.getSubTaskObjects()
        if (subTaskManager.subTasksEnabled && !subTasks.empty) {
            subTasks.each {
                it ->
                log.debug ("issue.statusObject.name: " + issue.statusObject.name)
                workflowTransitionUtil.setIssue(it);
                workflowTransitionUtil.setUsername(currentUser.name);
                workflowTransitionUtil.setAction (711)    // IC Ready To Start
         
                // validate and transition issue
                if (workflowTransitionUtil.validate()) {
                    log.debug("Ready To Start subtask " + it.key)
                    workflowTransitionUtil.progress();
                }
            }
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