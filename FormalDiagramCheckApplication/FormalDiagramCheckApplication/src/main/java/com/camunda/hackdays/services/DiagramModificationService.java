package com.camunda.hackdays.services;

import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import org.springframework.stereotype.Component;

@Component
public class DiagramModificationService {

    public BpmnModelInstance modifyDiagram(BpmnModelInstance diagram, String linterResults){
        // TODO: modify
        return diagram;
    }
}
