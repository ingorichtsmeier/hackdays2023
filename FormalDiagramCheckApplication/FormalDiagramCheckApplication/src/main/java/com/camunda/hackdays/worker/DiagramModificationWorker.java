package com.camunda.hackdays.worker;

import com.camunda.hackdays.services.DiagramModificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

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

        Map<String, Object> responseMap = new JacksonJsonParser().parseMap(responseEntity.getBody());
        String fileContent = (String) responseMap.get("content");
        Map<?, ?> metadata = (Map<?, ?>) responseMap.get("metadata");
        String folderId = (String) metadata.get("folderId");
        String projectId = (String) metadata.get("projectId");
        String parentId = (String) metadata.get("folderId");
        int revision = (int) metadata.get("revision");
        String filename = (String) metadata.get("name");
        LOGGER.info("BPMN File: {}", fileContent);
        LOGGER.info("FolderId: {}, ProjectId: {}, ParentId: {}, next revision: {}, name: {}", folderId, projectId, parentId, revision, filename);

        // convert to BPMN diagram
        InputStream targetStream = new ByteArrayInputStream(fileContent.getBytes());
        BpmnModelInstance modelInstance = Bpmn.readModelFromStream(targetStream);

        LOGGER.info("Piece of the model: {}", modelInstance.getDefinitions().getExporter());

        // modify diagram
        BpmnModelInstance modifiedModel =  diagramModificationService.modifyDiagram(modelInstance, lintingResult);
        //LOGGER.info("BPMN File: {}", Bpmn.convertToString(modifiedModel));

        File newModelFile = new File("target/processModifiedByWorker.bpmn");
        newModelFile.createNewFile();
        Bpmn.writeModelToFile(newModelFile, modifiedModel);
        
        // upload modified diagram
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        restTemplate.setRequestFactory(requestFactory);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode()
            .put("name", filename)
            .put("content", Bpmn.convertToString(modifiedModel))
            .put("revision", revision)
            .put("projectId", projectId)
            .put("parentId", parentId);
        String request = objectMapper.writer().writeValueAsString(objectNode);
        LOGGER.debug("PATCH request: {}", request);
        HttpEntity<String> entityRequest = new HttpEntity<String>(request, header);
        Object patchResponse = restTemplate.exchange(url, HttpMethod.PATCH, entityRequest, Void.class);
        LOGGER.debug("Resosnse from patch: {}", patchResponse);

    }
}
