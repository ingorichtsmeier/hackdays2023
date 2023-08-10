package com.camunda.hackdays;

import com.camunda.hackdays.services.DiagramModificationService;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.instance.TextAnnotation;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class DiagramModificationServiceTest {

  @Test
  public void testParseLinterResults() {
    String lintingResult =
        ";;/Users/jana/Documents/Coding/Hackdays/hackdays2023/myBPMN.bpmn; StartEvent_1 error Element is missing label/name label-required ; Activity_1dzoh12 error Element is missing label/name label-required ; Event_1v7nyg5 error Element is missing label/name label-required ; Activity_1dzoh12 error Element is an implicit end no-implicit-end ; Activity_15z57b4 error Element is an implicit start no-implicit-start;;✖ 5 problems (5 errors, 0 warnings);";

    Map<String, String> linterResultMap =
        new DiagramModificationService().parseLinterResults(lintingResult);
    assertThat(linterResultMap).hasSize(4);
    assertThat(linterResultMap).contains(entry("Event_1v7nyg5", "Element is missing label/name"),
        entry("Activity_1dzoh12", "Element is missing label/name; Element is an implicit end"),
        entry("StartEvent_1", "Element is missing label/name"),
        entry("Activity_15z57b4", "Element is an implicit start"));
  }

  @Test
  void testAddComment() throws IOException {
    BpmnModelInstance changedModel =
        new DiagramModificationService().addComment("hallo test comment", "Activity_1dzoh12",
            Bpmn.readModelFromStream(this.getClass().getResourceAsStream("/ugly-diagram.bpmn")),0);

    assertThat(changedModel).isNotNull();
    assertThat(changedModel.getModelElementsByType(TextAnnotation.class)).hasSize(1);
    assertThat(changedModel.getModelElementsByType(TextAnnotation.class).iterator().next().getText()
        .getTextContent()).isEqualTo("hallo test comment");

    File newModelFile = new File("target/firstTextAnnotation.bpmn");
    newModelFile.createNewFile();
    Bpmn.writeModelToFile(newModelFile, changedModel);
  }

  @Test
  void testAddComments() throws IOException {
    DiagramModificationService diagramModificationService = new DiagramModificationService();
    BpmnModelInstance changedModel =
           diagramModificationService.addComment("hallo test comment", "Activity_1dzoh12",
                    Bpmn.readModelFromStream(this.getClass().getResourceAsStream("/ugly-diagram.bpmn")),0);
    changedModel =
            diagramModificationService.addComment("hallo test comment 2", "StartEvent_1", changedModel,1);
    assertThat(changedModel).isNotNull();
    assertThat(changedModel.getModelElementsByType(TextAnnotation.class)).hasSize(2);
    assertThat(changedModel.getModelElementsByType(TextAnnotation.class).iterator().next().getText()
            .getTextContent()).isEqualTo("hallo test comment");

    File newModelFile = new File("target/secondTextAnnotation.bpmn");
    newModelFile.createNewFile();
    Bpmn.writeModelToFile(newModelFile, changedModel);
  }
}
