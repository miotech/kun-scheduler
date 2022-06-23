kind: Pod
apiVersion: v1
metadata:
  name: ${podName}
  namespace: ${namespace}
  labels:
    kuntaskattemptid: ${kuntaskattemptid}
    kunworkflow: ''
    task_queue: ${task_queue}
spec:
  volumes:
    - name: ${pvName}
      hostPath:
        path: ${subPath}
        type: Directory
  containers:
    - name: ${containerName}
      image: >-
        ${imageName}
      imagePullPolicy: ${pullPolicy}
      command:
        <#list commandList as command>
        - ${command}
        </#list>
      env:
        <#list envList as env>
        - name: ${env.key}
          value: ${env.value}
        </#list>
      volumeMounts:
        <#list volumeList as volume>
        - name: ${volume.name}
          mountPath: ${volume.mountPath}
          subPath: ${volume.subPath}
        </#list>
      resources:
        requests:
          memory: "256Mi"
        limits:
          memory: "512Mi"
  restartPolicy: Never
  imagePullSecrets:
    - name: ${dockerSecret}