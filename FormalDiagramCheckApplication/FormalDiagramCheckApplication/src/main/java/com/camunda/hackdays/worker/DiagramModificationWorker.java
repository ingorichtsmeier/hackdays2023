package com.camunda.hackdays.worker;

import com.camunda.hackdays.services.DiagramModificationService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.instance.bpmndi.BpmnDiagram;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

@Component
public class DiagramModificationWorker implements JobHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiagramModificationWorker.class);

    DiagramModificationService diagramModificationService;

    @Value("${token}")
    String token;

    @Autowired
    public DiagramModificationWorker(DiagramModificationService diagramModificationService) {
        this.diagramModificationService = diagramModificationService;
    }

    @Override
    @JobWorker(type = "modify-file")
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        LOGGER.info("handling job {}", job.getKey());
        LOGGER.info("Variables: {}", job.getVariablesAsMap().toString());

        LOGGER.info("Token = {}", token);

        String fileId = (String) job.getVariablesAsMap().get("fileId");
        String lintingResult = (String) job.getVariablesAsMap().get("lintingResult");

        String url = "https://modeler.cloud.camunda.io/api/beta/files/" + fileId;

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", "Bearer " + token);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(header);

        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        //LOGGER.info("Response: {}", responseEntity.getBody());

        String fileContent = (String) new JacksonJsonParser().parseMap(responseEntity.getBody()).get("content");
        LOGGER.info("BPMN File: {}", fileContent);

        // convert to BPMN diagram
        InputStream targetStream = new ByteArrayInputStream(fileContent.getBytes());
        BpmnModelInstance modelInstance = Bpmn.readModelFromStream(targetStream);

        LOGGER.info("Piece of the model: {}", modelInstance.getDefinitions().getExporter());

        // modify diagram
        BpmnModelInstance modifiedModel =  diagramModificationService.modifyDiagram(modelInstance, lintingResult);
        //LOGGER.info("BPMN File: {}", Bpmn.convertToString(modifiedModel));

        File newModelFile = new File("/Users/jana/Documents/Coding/Hackdays/processModifiedByWorker.bpmn");
        newModelFile.createNewFile();
        Bpmn.writeModelToFile(newModelFile, modifiedModel);
        // TODO upload modified diagram



    }
}
