package com.camunda.hackdays.services;

import com.camunda.hackdays.worker.DiagramModificationWorker;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Component
public class DiagramModificationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DiagramModificationService.class);

  public BpmnModelInstance modifyDiagram(BpmnModelInstance diagram, String linterResults) {
    Map<String, String> linterResultMap = parseLinterResults(linterResults);



    return diagram;
  }

  public Map<String, String> parseLinterResults(String linterResults) {
    // elementId, Comment
    Map<String, String> linterResultMap = new HashMap<>();

    String[] resultLines = linterResults.split(";");

    List<String> linesToParse = new ArrayList<>();

    for (String line : resultLines) {
      line = line.trim();
      if (line.length() != 0) {
        linesToParse.add(line);
      }
    }

    String elementID;
    String warning;
    for (int i = 1; i < linesToParse.size() - 1; i++) {
      LOGGER.info("{} Line to parse {}", i, linesToParse.get(i));
      String[] splittedLine = linesToParse.get(i).split(" ");
      LOGGER.info("{} Splitted lines {}", i, splittedLine);
      elementID = splittedLine[0];
      warning = "";
      for (int j = 2; j < splittedLine.length - 1; j++) {
        warning += splittedLine[j] + " ";
      }
      if (linterResultMap.containsKey(elementID)) {
        linterResultMap.put(elementID, linterResultMap.get(elementID) + "; " + warning.trim());
      } else {
        linterResultMap.put(elementID, warning.trim());
      }
    }
    LOGGER.info("Splitted lines {}", linterResultMap);
    return linterResultMap;
  }
}
