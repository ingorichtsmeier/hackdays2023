#! /bin/bash
#fileId=$(jq -r '.fileId')
#echo "FileId: $fileId"

# TODO accessToken nicht jedes mal neu holen
#myToken=$(./getModelerAccessToken.sh | jq -r '.access_token')
#echo $myToken

#curl -X 'GET' \
#  "https://modeler.cloud.camunda.io/api/beta/files/$fileId" \
#  -H 'accept: application/json' \
#  -H "Authorization: Bearer $myToken" -s | jq -r '.content' > myBPMN.bpmn

checkResult=$(bpmnlint ./myBPMN.bpmn)
echo "{\"result\":\"test \\n test2\"}"
