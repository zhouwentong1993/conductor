调试步骤
1. 启动 Server 服务
2. 创建 task 和 Workflow，脚本如下：
<br>创建 task
 ```shell script
curl -X POST \
  http://localhost:8080/api/metadata/taskdefs \
  -H 'Content-Type: application/json' \
  -d '[
    {
      "name": "verify_if_idents_are_added",
      "retryCount": 3,
      "retryLogic": "FIXED",
      "retryDelaySeconds": 10,
      "timeoutSeconds": 300,
      "timeoutPolicy": "TIME_OUT_WF",
      "responseTimeoutSeconds": 180,
      "ownerEmail": "819878569@qq.com"
    },
    {
      "name": "add_idents",
      "retryCount": 3,
      "retryLogic": "FIXED",
      "retryDelaySeconds": 10,
      "timeoutSeconds": 300,
      "timeoutPolicy": "TIME_OUT_WF",
      "responseTimeoutSeconds": 180,
      "ownerEmail": "819878569@qq.com"
    }
]'
```
<br> 创建 Workflow

```shell script
curl -X POST \
  http://localhost:8080/api/metadata/workflow \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "add_netflix_identation",
    "description": "Adds Netflix Identation to video files.",
    "version": 2,
    "schemaVersion": 2,
    "ownerEmail": "819878569@qq.com",
    "tasks": [
        {
            "name": "verify_if_idents_are_added",
            "taskReferenceName": "ident_verification",
            "inputParameters": {
                "contentId": "${workflow.input.contentId}"
            },
            "type": "SIMPLE"
        },
        {
            "name": "decide_task",
            "taskReferenceName": "is_idents_added",
            "inputParameters": {
                "case_value_param": "${ident_verification.output.is_idents_added}"
            },
            "type": "DECISION",
            "caseValueParam": "case_value_param",
            "decisionCases": {
                "false": [
                    {
                        "name": "add_idents",
                        "taskReferenceName": "add_idents_by_type",
                        "inputParameters": {
                            "identType": "${workflow.input.identType}",
                            "contentId": "${workflow.input.contentId}"
                        },
                        "type": "SIMPLE"
                    }
                ]
            }
        }
    ]
}'
```
<br>
启动workflow

```shell script
curl -X POST \
  http://localhost:8080/api/workflow/add_netflix_identation \
  -H 'Content-Type: application/json' \
  -d '{
    "identType": "animation",
    "contentId": "my_unique_content_id"
}'
```
