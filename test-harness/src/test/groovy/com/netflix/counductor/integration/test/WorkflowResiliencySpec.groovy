package com.netflix.counductor.integration.test

import com.netflix.conductor.common.metadata.tasks.Task
import com.netflix.conductor.common.run.Workflow
import com.netflix.conductor.core.execution.WorkflowExecutor
import com.netflix.conductor.dao.QueueDAO
import com.netflix.conductor.service.ExecutionService
import com.netflix.conductor.service.MetadataService
import com.netflix.conductor.test.util.WorkflowTestUtil
import com.netflix.conductor.tests.utils.TestModule
import com.netflix.governator.guice.test.ModulesForTesting
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@ModulesForTesting([TestModule.class])
class WorkflowResiliencySpec extends Specification {

    @Inject
    ExecutionService workflowExecutionService

    @Inject
    MetadataService metadataService

    @Inject
    WorkflowExecutor workflowExecutor

    @Inject
    WorkflowTestUtil workflowTestUtil

    @Inject
    QueueDAO queueDAO

    @Shared
    def LINEAR_WORKFLOW_T1_T2 = 'integration_test_wf'

    @Shared
    def INTEGRATION_TEST_WF_NON_RESTARTABLE = "integration_test_wf_non_restartable"


    def setup() {
        //Register LINEAR_WORKFLOW_T1_T2,  RTOWF, WORKFLOW_WITH_OPTIONAL_TASK
        workflowTestUtil.registerWorkflows('simple_workflow_1_integration_test.json',
                'simple_workflow_with_resp_time_out_integration_test.json')
    }

    def cleanup() {
        workflowTestUtil.clearWorkflows()
    }

    def "Test workflow push to decider queue on start"() {

        given: "An existing simple workflow definition"
        metadataService.getWorkflowDef(LINEAR_WORKFLOW_T1_T2, 1)

        and: "input required to start the workflow execution"
        String correlationId = 'unit_test_1'
        def input = new HashMap()
        String inputParam1 = 'p1 value'
        input['param1'] = inputParam1
        input['param2'] = 'p2 value'

        when: "Start a workflow based on the registered simple workflow"
        def workflowInstanceId = workflowExecutor.startWorkflow(LINEAR_WORKFLOW_T1_T2, 1,
                correlationId, input,
                null, null, null)

        then: "verify that the workflow is in a running state"
        with(workflowExecutionService.getExecutionStatus(workflowInstanceId, true)) {
            status == Workflow.WorkflowStatus.RUNNING
            tasks.size() == 1
            tasks[0].taskType == 'integration_task_1'
            tasks[0].status == Task.Status.SCHEDULED
        }

        then: "verify that the decider queue contains this workflow message"
        queueDAO.containsMessage("_deciderQueue", workflowInstanceId) == true
        queueDAO.containsMessage("_deciderQueue", "some_non_existent_wf_id+2345524") == false
    }
}
