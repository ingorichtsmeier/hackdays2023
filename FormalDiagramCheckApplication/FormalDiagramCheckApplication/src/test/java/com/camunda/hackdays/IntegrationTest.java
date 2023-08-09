package com.camunda.hackdays;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivateJobsResponse;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.waitForProcessInstanceCompleted;

@SpringBootTest
@ZeebeSpringTest
public class IntegrationTest {


    @Autowired
    private ZeebeClient zeebe;

    // TODO: We should probably get rid of this in Spring tests or at least hide it somewhere
    // At the moment we have two different ways of waiting: Multi-threaded waiting, and the "engine
    // run to completion"
    @Autowired private ZeebeTestEngine zeebeTestEngine;

    //@MockBean private TwitterService twitterService;


    @BeforeEach
    public void depoy(){
        zeebe.newDeployResourceCommand().addResourceFromClasspath("diagram-formal-check.bpmn").send().join();
    }
    @Test
    public void runSimpleProcess() throws InterruptedException, TimeoutException {
        ProcessInstanceEvent processInstance = zeebe.newCreateInstanceCommand()
                .bpmnProcessId("DiagramFormalCheckProcess")
                .latestVersion()
                .variables(Map.of("fileId", "bd505fca-65f2-4826-adc4-c9238c522553"))
                .send().join();

        ActivateJobsResponse jobsResponse = zeebe.newActivateJobsCommand().jobType("check-file").maxJobsToActivate(1).send().join();

        zeebe.newCompleteCommand(jobsResponse.getJobs()
                .get(0))
                .variables(Map.of("lintingResult", ";;/Users/jana/Documents/Coding/Hackdays/hackdays2023/myBPMN.bpmn; StartEvent_1 error Element is missing label/name label-required ; Activity_1dzoh12 error Element is missing label/name label-required ; Event_1v7nyg5 error Element is missing label/name label-required ; Activity_1dzoh12 error Element is an implicit end no-implicit-end ; Activity_15z57b4 error Element is an implicit start no-implicit-start;;✖ 5 problems (5 errors, 0 warnings);"))
                .send().join();

        waitForProcessInstanceCompleted(processInstance);

        assertThat(processInstance).isCompleted();
    }
}
