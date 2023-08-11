package com.camunda.hackdays.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.List;

@Component
public class GetFilesWorker  implements JobHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagramModificationWorker.class);

    @Value("${token}")
    String token;

    @Override
    @JobWorker(type = "get-files", autoComplete = false)
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        String folderId = (String) job.getVariablesAsMap().get("folderId");

        String url = "https://modeler.cloud.camunda.io/api/beta/folders/" + folderId;

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", "Bearer " + token);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(header);

        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        Map<String, Object> folderResponse = new JacksonJsonParser().parseMap(responseEntity.getBody());
        Map<String, Object>  content = (Map<String, Object>) folderResponse.get("content");
        List<Map<String, Object>> fileList = (List<Map<String, Object>>) content.get("files");

        LOGGER.info("Filelist: {}", fileList);


        ObjectMapper objectMapper = new ObjectMapper();

        ObjectNode resultNode = objectMapper.createObjectNode();
        ArrayNode fileResultList = objectMapper.createArrayNode();

        for (Map<String, Object> file:fileList
             ) {
            LOGGER.info("file: {} {}", file.get("name"), file.get("id"));
            ObjectNode fileResult = objectMapper.createObjectNode()
                    .put("quizId", ((String) file.get("name")).split("_")[0])
                    .put("id", (String) file.get("id"));
            fileResultList.add(fileResult);
        }
        resultNode.put("files", fileResultList);

        LOGGER.info("fileResultList: {} ", resultNode);
        client.newCompleteCommand(job).variables(resultNode).send().join();


    }
}
