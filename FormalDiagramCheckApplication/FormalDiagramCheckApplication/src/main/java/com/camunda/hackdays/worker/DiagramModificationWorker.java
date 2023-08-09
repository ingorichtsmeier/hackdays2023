package com.camunda.hackdays.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DiagramModificationWorker implements JobHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiagramModificationWorker.class);
    @Override
    @JobWorker(type = "modify-file")
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        LOGGER.info("handling job {}", job.getKey());
        LOGGER.info("Variables: {}", job.getVariablesAsMap().toString());

        var fileId = job.getVariablesAsMap().get("fileId");
        var lintingResult = job.getVariablesAsMap().get("lintingResult");

        
    }
}
