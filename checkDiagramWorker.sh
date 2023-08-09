#! /bin/bash

#{"fileId": "bd505fca-65f2-4826-adc4-c9238c522553"}
source CamundaCloudMgmtAPI-Client-hackdays.sh

zbctl create worker check-file --handler "./checkDiagram.sh"
