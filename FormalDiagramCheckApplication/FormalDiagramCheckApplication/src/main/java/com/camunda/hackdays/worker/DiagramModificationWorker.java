package com.camunda.hackdays.worker;

import com.camunda.hackdays.services.DiagramModificationService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.instance.bpmndi.BpmnDiagram;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DiagramModificationWorker implements JobHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiagramModificationWorker.class);

    DiagramModificationService diagramModificationService;

    @Autowired
    public DiagramModificationWorker(DiagramModificationService diagramModificationService) {
        this.diagramModificationService = diagramModificationService;
    }


    @Override
    @JobWorker(type = "modify-file")
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        LOGGER.info("handling job {}", job.getKey());
        LOGGER.info("Variables: {}", job.getVariablesAsMap().toString());

        String fileId = (String) job.getVariablesAsMap().get("fileId");
        String lintingResult = (String) job.getVariablesAsMap().get("lintingResult");

        // get file from Web Modeler
        // (https://modeler.cloud.camunda.io/api/beta/files/bd505fca-65f2-4826-adc4-c9238c522553)

        // convert to BPMN diagram

        // modify diagram
        diagramModificationService.modifyDiagram(null, lintingResult);

        // upload modified diagram



    }
}
