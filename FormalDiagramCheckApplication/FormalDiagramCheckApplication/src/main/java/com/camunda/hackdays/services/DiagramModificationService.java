package com.camunda.hackdays.services;

import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.instance.*;
import io.camunda.zeebe.model.bpmn.instance.Process;
import io.camunda.zeebe.model.bpmn.instance.bpmndi.BpmnEdge;
import io.camunda.zeebe.model.bpmn.instance.bpmndi.BpmnPlane;
import io.camunda.zeebe.model.bpmn.instance.bpmndi.BpmnShape;
import io.camunda.zeebe.model.bpmn.instance.dc.Bounds;
import io.camunda.zeebe.model.bpmn.instance.di.Shape;
import io.camunda.zeebe.model.bpmn.instance.di.Waypoint;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DiagramModificationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DiagramModificationService.class);

  public BpmnModelInstance modifyDiagram(BpmnModelInstance diagram, String linterResults) {
    Map<String, String> linterResultMap = parseLinterResults(linterResults);

    int i = 0;
    for (String linterResultKey:linterResultMap.keySet()
         ) {
      addComment(linterResultMap.get(linterResultKey), linterResultKey, diagram, i);
      i++;
    }
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
      LOGGER.debug("{} Line to parse {}", i, linesToParse.get(i));
      String[] splittedLine = linesToParse.get(i).split(" ");
      LOGGER.debug("{} Splitted lines {}", i, splittedLine);
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
  
  public BpmnModelInstance addComment(String comment, String elementId, BpmnModelInstance model, int cnt) {
    
    Process process = model.getModelElementsByType(Process.class).iterator().next();
    TextAnnotation textAnnotation = createElement(process, "textannotation_" + elementId, model.getModel().getType(TextAnnotation.class));
    Text text = createElement(textAnnotation, null,model.getModel().getType(Text.class));
    text.setTextContent(comment);


    BpmnPlane plane = model.getModelElementsByType(BpmnPlane.class).iterator().next();

    BpmnShape shape = createElement(plane, "TextAnnotationShape" + elementId, model.getModel().getType(BpmnShape.class));
    shape.setBpmnElement(textAnnotation);
    Bounds bounds = createElement(shape, null,model.getModel().getType(Bounds.class));
    bounds.setX(0 + (110 * cnt));
    bounds.setY(0);
    bounds.setWidth(100);
    bounds.setHeight(50);
    shape.setBounds(bounds);
    shape.setAttributeValueNs("http://www.omg.org/spec/BPMN/non-normative/color/1.0", "border-color", "#831311");

    Association association = createAssociation(process, model.getModelElementById(elementId), textAnnotation);
    BpmnEdge edge = createElement(plane, "Association_Edge_" + elementId, model.getModel().getType(BpmnEdge.class));
    edge.setBpmnElement(association);
    Waypoint wp1 = createElement(edge, null, model.getModel().getType(Waypoint.class));
    Waypoint wp2 = createElement(edge, null, model.getModel().getType(Waypoint.class));
    wp1.setX(55 + (110*cnt));
    wp1.setY(50);
    edge.setAttributeValueNs("http://www.omg.org/spec/BPMN/non-normative/color/1.0", "border-color", "#831311");
    Iterator<BpmnShape> iterator = model.getModelElementsByType(BpmnShape.class).iterator();
    while(iterator.hasNext()){
      BpmnShape elementShape = iterator.next();
      LOGGER.info("ElementShape: {}", elementShape.getBpmnElement().getId());
      if(elementShape.getBpmnElement().getId().equals(elementId)){
        wp2.setX(elementShape.getBounds().getX() + 0.5 * elementShape.getBounds().getWidth());
        wp2.setY(elementShape.getBounds().getY());
      }
    }
    Bpmn.validateModel(model);
    return model;
  }

  protected <T extends BpmnModelElementInstance> T createElement(BpmnModelElementInstance parentElement, String id, ModelElementType elementClass) {
    T element = parentElement.getModelInstance().newInstance(elementClass, id);
    parentElement.addChildElement(element);
    return element;
  }

  public Association createAssociation(Process process, FlowNode from, BaseElement to) {
    String identifier = from.getId() + "-" + to.getId();
    process.getModelInstance().newInstance(Association.class);
    Association association = createElement(process, identifier, process.getModelInstance().getModel().getType(Association.class));
    process.addChildElement(association);
    association.setSource(from);
    association.setTarget(to);
    return association;
  }

  
}
