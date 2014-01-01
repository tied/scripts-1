package com.onresolve.jira.groovy.canned.workflow.postfunctions

import com.atlassian.jira.config.ConstantsManager
import com.atlassian.jira.config.SubTaskManager
import com.atlassian.jira.event.issue.IssueEvent
import com.atlassian.jira.issue.*
import com.atlassian.jira.issue.issuetype.*
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.util.ErrorCollection
import com.atlassian.jira.util.SimpleErrorCollection
import com.onresolve.jira.groovy.canned.CannedScript
import com.onresolve.jira.groovy.canned.utils.CannedScriptUtils
import com.opensymphony.workflow.loader.ActionDescriptor
import com.opensymphony.workflow.loader.StepDescriptor
import com.atlassian.jira.ComponentManager
import org.ofbiz.core.entity.GenericValue
import com.atlassian.crowd.embedded.api.User
import com.atlassian.jira.util.ImportUtils
import com.atlassian.jira.security.roles.ProjectRoleManager
import com.atlassian.jira.security.roles.ProjectRole

class CreateICDesignSubtask extends CreateICSubtasks implements CannedScript {
    public final static String FIELD_ROLE_NAME = "Assigned Role"
    public final static String FIELD_UPDATE_TYPE = "Update Type"
    private def toOpen = [
            "New Installer":  [["Creative Task", "Designer", "Designers"]],
            "New DLM":        [["Creative Task", "Designer", "Designers"]],
            "New Offer":      [["Creative Task", "Designer", "Designers"]],
            "Update":         []
    ]

    protected Boolean isUpdate(Issue issue) {
        CustomField cf = componentManager.getCustomFieldManager().getCustomFieldObjectByName(FIELD_UPDATE_TYPE)
        return (issue?.getCustomFieldValue(cf) != null)
    }
    
    String getName() {
        return "Auto-creates design sub-task for installcore projects."
    }

    String getDescription() {
        return "Create design subtask used for InstallCore projects.  Only supports New projects."
    }

    List getCategories() {
        ["Function"]
    }

    List getParameters(Map params) {
        []
    }
    
    public ErrorCollection doValidate(Map params, boolean forPreview) {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection()
        return errorCollection
    }
    
    protected IssueType getIssueTypeByName(String name) {
        IssueType rv = null
        componentManager.getConstantsManager().getAllIssueTypeObjects().each() {
            type ->
                if (type.name == name) {
                    rv = type
                }
        }
        return rv
    }

    void AddIssue(MutableIssue oldIssue, IssueType issueType, User assignee, ProjectRole role, User reporter, User currentUserObj) {
        if (issueType == null) {
            return
        }
        IssueManager issueMgr = componentManager.getIssueManager()
        

        def wasIndexing = ImportUtils.indexIssues
        ImportUtils.indexIssues = true
        IssueFactory issueFactory = ComponentManager.getInstance().getIssueFactory()
        MutableIssue newIssue = issueFactory.getIssue()

        newIssue.projectObject = oldIssue.projectObject
        newIssue.issueTypeObject = issueType
        
        newIssue.priorityObject = oldIssue.priorityObject
        newIssue.summary = "AUTO-CREATED: " + issueType.name + " - " + oldIssue.summary
        if (assignee) {
            newIssue.assignee = assignee
        }
        newIssue.reporter = reporter
                
        def cf = ComponentManager.getInstance().getCustomFieldManager().getCustomFieldObjectByName(FIELD_ROLE_NAME)
        newIssue.setCustomFieldValue(cf, role.name)
                
        Issue newIssueGv = issueMgr.createIssueObject(currentUserObj, newIssue)
        indexManager.reIndex(newIssueGv);

        ImportUtils.indexIssues = wasIndexing

        SubTaskManager subTaskManager = componentManager.getSubTaskManager()
        subTaskManager.createSubTaskIssueLink(oldIssue, newIssueGv, currentUserObj)
    }
    
    Map doScript(Map params) {
        Issue issue = params['issue'] as MutableIssue
            
        if (issue.getIssueTypeObject().isSubTask()) {
            log.warn ("This issue ($issue) is already a sub-task... doing nothing.")
            return params
        }
        
        if (isUpdate(issue)) {
            log.debug("This issue ($issue) is an update... Doing nothing.")
            return params
        }
        
        User currentUserObj = getUser(params)
        def cf = ComponentManager.getInstance().getCustomFieldManager().getCustomFieldObjectByName("Account Manager")
        User reporter = issue.getCustomFieldValue(cf) ?: currentUserObj
        ProjectRoleManager projectRoleManager = (ProjectRoleManager) componentManager.getComponentInstanceOfType(ProjectRoleManager.class);
        
        String type = issue.issueTypeObject.name
        
        toOpen[type].each() {
            task ->
            String issueTypeName = task[0]
            String assigneeField = task[1]
            String roleName      = task[2]
            log.debug("Want to open a $issueTypeName for $assigneeField")
            cf = ComponentManager.getInstance().getCustomFieldManager().getCustomFieldObjectByName(assigneeField)
            log.debug("Assignee CF is " + cf.inspect())
            User assignee = issue.getCustomFieldValue(cf)
            ProjectRole role = projectRoleManager.getProjectRole(roleName)
            IssueType issueType = getIssueTypeByName(issueTypeName)
            log.debug("Really going to open a " + issueType?.name + " for user " + assignee?.name + " and role " + role?.name)
            AddIssue(issue, issueType, assignee, role, reporter, currentUserObj)
        }

        return params
    }


    String getDescription(Map params, boolean forPreview) {
        return "Will create InstallCore subtasks"
    }

    public Boolean isFinalParamsPage(Map params) {
        true
    }
}
