import com.atlassian.jira.ComponentManager
import com.atlassian.jira.issue.comments.CommentManager
import com.opensymphony.workflow.WorkflowContext
import org.apache.log4j.Category
import com.atlassian.jira.config.SubTaskManager
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.issue.MutableIssue

log = Category.getInstance("com.onresolve.jira.groovy.AutoCloseChildIssues")
 
String currentUser = ((WorkflowContext) transientVars.get("context")).getCaller();
WorkflowTransitionUtil workflowTransitionUtil = ( WorkflowTransitionUtil ) JiraUtils.loadComponent( WorkflowTransitionUtilImpl.class );
 
SubTaskManager subTaskManager = ComponentManager.getInstance().getSubTaskManager();
Collection<MutableIssue> subTasks = issue.getSubTaskObjects()
if (subTaskManager.subTasksEnabled && !subTasks.empty) {
    subTasks.each {
        log.debug ("issue.statusObject.name: " + issue.statusObject.name)
        workflowTransitionUtil.setIssue(it);
        workflowTransitionUtil.setUsername(currentUser);
        workflowTransitionUtil.setAction (781)    // IC SubTask Close
 
        // validate and transition issue
        if (workflowTransitionUtil.validate()) {
            workflowTransitionUtil.progress();
        }
    }
}
