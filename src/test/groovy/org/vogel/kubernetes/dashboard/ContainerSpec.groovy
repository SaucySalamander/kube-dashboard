package org.vogel.kubernetes.dashboard

import io.kubernetes.client.custom.IntOrString
import io.kubernetes.client.custom.Quantity
import io.kubernetes.client.models.*
import spock.lang.Specification

class ContainerSpec extends Specification {
    def "create a Container mainly empty"() {
        given:
        def kubeContainer = Mock(V1Container)
        kubeContainer.resources >> Mock(V1ResourceRequirements)
        List<V1ContainerStatus> containerStatuses = new ArrayList<>()

        when:
        def container = new Container(kubeContainer, containerStatuses)

        then:
        container.name == null
        container.containerId == null
        container.image == null
        container.imageId == null
        container.ports == ""
        container.hostPorts == ""
        container.commands == null
        container.args == null
        container.state == null
        container.lastState == null
        container.ready == null
        container.restartCount == null
        container.limits.size() == 0
        container.requests.size() == 0
        container.livenessProbe == null
        container.readinessProbe == null
        container.envFrom.size() == 0
        container.env.size() == 0
        container.mounts.size() == 0
        container.containerPorts == null
    }

    def "create a Container"() {
        given:
        def kubeContainer = Mock(V1Container)
        kubeContainer.name >> "foo"
        kubeContainer.image >> "foo/bar:1.0.0"
        List<V1ContainerPort> ports = new ArrayList<>()
        V1ContainerPort port1 = new V1ContainerPort()
        port1.containerPort = 8080
        port1.protocol = "http"
        ports << port1
        kubeContainer.ports >> ports
        def resourceRequirements = Mock(V1ResourceRequirements)
        kubeContainer.resources >> resourceRequirements
        kubeContainer.command >> ["blah"]
        kubeContainer.args >> ["arg"]
        def livenessProbe = Mock(V1Probe)
        kubeContainer.livenessProbe >> livenessProbe
        def readinessProbe = Mock(V1Probe)
        kubeContainer.readinessProbe >> readinessProbe
        kubeContainer.envFrom >> []
        List<V1ContainerStatus> containerStatuses = new ArrayList<>()
        V1ContainerStatus containerStatus1 = new V1ContainerStatus()
        containerStatus1.setName("foo")
        containerStatus1.setContainerID("docker://8eaf7e1a1dd0e6e93fd7f77a87e53a410c38e59251ad1de23853dd2093859055")
        containerStatus1.setImageID("kube-dashboard:1.0.0")
        containerStatus1.setReady(true)
        containerStatus1.setRestartCount(2)
        V1ContainerState cs1State = new V1ContainerState()
        containerStatus1.setState(cs1State)
        V1ContainerState cs1LastState = new V1ContainerState()
        containerStatus1.setLastState(cs1LastState)
        containerStatuses << containerStatus1

        when:
        def container = new Container(kubeContainer, containerStatuses)

        then:
        container.name == "foo"
        container.containerId == "docker://8eaf7e1a1dd0e6e93fd7f77a87e53a410c38e59251ad1de23853dd2093859055"
        container.image == "foo/bar:1.0.0"
        container.imageId == "kube-dashboard:1.0.0"
        container.ports == "8080/http"
        container.hostPorts == "0/http"
        container.commands == ["blah"]
        container.args == ["arg"]
        container.state.state == "Waiting"
        container.lastState.state == "Waiting"
        container.ready == true
        container.restartCount == 2
        container.limits.size() == 0
        container.requests.size() == 0
        container.livenessProbe == "unknown delay=nulls timeout=nulls period=nulls #success=null #failure=null"
        container.readinessProbe == "unknown delay=nulls timeout=nulls period=nulls #success=null #failure=null"
        container.envFrom.size() == 0
        container.env.size() == 0
        container.mounts.size() == 0
        container.containerPorts == [8080]
    }

    def "create a Container with more"() {
        given:
        def kubeContainer = Mock(V1Container)
        kubeContainer.name >> "foo"
        kubeContainer.image >> "foo/bar:1.0.0"
        List<V1ContainerPort> ports = new ArrayList<>()
        V1ContainerPort port1 = new V1ContainerPort()
        port1.containerPort = 8080
        port1.protocol = "http"
        ports << port1
        kubeContainer.ports >> ports
        def resourceRequirements = Mock(V1ResourceRequirements)
        Map<String, Quantity> limits = new HashMap<>()
        limits.put("cpu", new Quantity("1"))
        limits.put("memory", new Quantity("1Gi"))
        limits.put("ephemeral-storage", new Quantity("1Gi"))
        resourceRequirements.limits >> limits
        Map<String, Quantity> requests = new HashMap<>()
        requests.put("cpu", new Quantity("1"))
        requests.put("memory", new Quantity("1Gi"))
        requests.put("ephemeral-storage", new Quantity("1Gi"))
        resourceRequirements.requests >> requests
        kubeContainer.resources >> resourceRequirements
        kubeContainer.command >> ["blah"]
        kubeContainer.args >> ["arg"]
        def httpProbe = new V1HTTPGetAction().host("localhost").port(new IntOrString(80))
        def livenessProbe = new V1Probe().httpGet(httpProbe)
        kubeContainer.livenessProbe >> livenessProbe
        def tcpProbe = new V1TCPSocketAction().host("localhost").port(new IntOrString(80))
        def readinessProbe = new V1Probe().tcpSocket(tcpProbe)
        kubeContainer.readinessProbe >> readinessProbe
        V1EnvFromSource fromConfigMap = new V1EnvFromSource()
        fromConfigMap.prefix = "foo"
        V1ConfigMapEnvSource configMapRef = new V1ConfigMapEnvSource()
        configMapRef.name = "config-map"
        configMapRef.optional = false
        fromConfigMap.configMapRef = configMapRef
        V1EnvFromSource fromSecret = new V1EnvFromSource()
        fromSecret.prefix = "bar"
        V1SecretEnvSource secretRef = new V1SecretEnvSource()
        secretRef.name("secret")
        secretRef.optional = true
        fromSecret.secretRef = secretRef
        V1EnvFromSource blank = new V1EnvFromSource()
        kubeContainer.envFrom >> [fromConfigMap, fromSecret, blank]
        V1EnvVar envVar = new V1EnvVar()
        envVar.name = "var1"
        envVar.value = "value1"
        V1EnvVar envVarBlankSource = new V1EnvVar()
        envVarBlankSource.name = "var2"
        envVarBlankSource.value = "value2"
        V1EnvVarSource blankSource = new V1EnvVarSource()
        envVarBlankSource.valueFrom = blankSource
        V1EnvVar envVarFieldRef = new V1EnvVar()
        envVarFieldRef.name = "var3"
        envVarFieldRef.value = "value3"
        V1EnvVarSource fieldRefSource = new V1EnvVarSource()
        V1ObjectFieldSelector fieldRef = new V1ObjectFieldSelector()
        fieldRefSource.fieldRef = fieldRef
        envVarFieldRef.valueFrom = fieldRefSource
        V1EnvVar envVarResourceFieldRef = new V1EnvVar()
        envVarResourceFieldRef.name = "var4"
        V1EnvVarSource resourceFieldRefSource = new V1EnvVarSource()
        V1ResourceFieldSelector resourceFieldSelector = new V1ResourceFieldSelector()
        resourceFieldSelector.divisor = "1234"
        resourceFieldSelector.resource = "limits.cpu"
        resourceFieldRefSource.resourceFieldRef = resourceFieldSelector
        envVarResourceFieldRef.valueFrom = resourceFieldRefSource
        V1EnvVar envVarSecretKeyRef = new V1EnvVar()
        envVarSecretKeyRef.name = "var5"
        V1EnvVarSource secretRefSource = new V1EnvVarSource()
        V1SecretKeySelector secretKeySelector = new V1SecretKeySelector()
        secretRefSource.secretKeyRef(secretKeySelector)
        envVarSecretKeyRef.valueFrom = secretRefSource
        V1EnvVar envVarConfigMapKeyRef = new V1EnvVar()
        envVarConfigMapKeyRef.name = "var6"
        V1EnvVarSource configMapKeySource = new V1EnvVarSource()
        V1ConfigMapKeySelector configMapKeySelector = new V1ConfigMapKeySelector()
        configMapKeySource.configMapKeyRef = configMapKeySelector
        envVarConfigMapKeyRef.setValueFrom(configMapKeySource)
        kubeContainer.env >> [envVar, envVarBlankSource, envVarFieldRef, envVarResourceFieldRef, envVarSecretKeyRef, envVarConfigMapKeyRef]
        V1VolumeMount mount1 = new V1VolumeMount()
        mount1.name = "blah"
        mount1.mountPath = "/foo"
        mount1.readOnly = false
        mount1.subPath = "/bar"
        V1VolumeMount mount2 = new V1VolumeMount()
        mount2.name = "bah"
        mount2.mountPath = "/hum"
        mount2.readOnly = true
        kubeContainer.volumeMounts >> [mount1, mount2]
        List<V1ContainerStatus> containerStatuses = new ArrayList<>()
        V1ContainerStatus containerStatus1 = new V1ContainerStatus()
        containerStatus1.name = "foo"
        containerStatus1.containerID = "docker://8eaf7e1a1dd0e6e93fd7f77a87e53a410c38e59251ad1de23853dd2093859055"
        containerStatus1.imageID = "kube-dashboard:1.0.0"
        containerStatus1.ready = true
        containerStatus1.restartCount = 2
        V1ContainerState cs1State = new V1ContainerState()
        containerStatus1.state = cs1State
        V1ContainerState cs1LastState = new V1ContainerState()
        containerStatus1.lastState = cs1LastState
        containerStatuses << containerStatus1

        when:
        def container = new Container(kubeContainer, containerStatuses)

        then:
        container.name == "foo"
        container.containerId == "docker://8eaf7e1a1dd0e6e93fd7f77a87e53a410c38e59251ad1de23853dd2093859055"
        container.image == "foo/bar:1.0.0"
        container.imageId == "kube-dashboard:1.0.0"
        container.ports == "8080/http"
        container.hostPorts == "0/http"
        container.commands == ["blah"]
        container.args == ["arg"]
        container.state.state == "Waiting"
        container.lastState.state == "Waiting"
        container.ready == true
        container.restartCount == 2
        container.limits.size() == 3
        container.requests.size() == 3
        container.livenessProbe == "http-get null://localhost:80/null delay=nulls timeout=nulls period=nulls #success=null #failure=null"
        container.readinessProbe == "tcp-socket localhost:80 delay=nulls timeout=nulls period=nulls #success=null #failure=null"
        container.envFrom.size() == 2
        container.envFrom[0].name == "config-map"
        container.envFrom[0].from == "ConfigMap"
        container.envFrom[0].optional == false
        container.envFrom[0].prefix == "foo"
        container.envFrom[1].name == "secret"
        container.envFrom[1].from == "Secret"
        container.envFrom[1].optional == true
        container.envFrom[1].prefix == "bar"
        container.env.size() == 5
        container.env[0].name == "var1"
        container.env[0].value == "value1"
        container.env[0].optional == null
        container.env[1].name == "var3"
        container.env[1].value == " (null:null)"
        container.env[1].optional == null
        container.env[2].name == "var4"
        container.env[2].value == "1 (limits.cpu)"
        container.env[2].optional == null
        container.env[3].name == "var5"
        container.env[3].value == "&lt;set to the key 'null' in secret 'null'&gt;"
        container.env[3].optional == false
        container.env[4].name == "var6"
        container.env[4].value == "&lt;set to the key 'null' in config map 'null'&gt;"
        container.env[4].optional == false
        container.mounts.size() == 2
        container.mounts[0] == "/foo from blah (rw,path=/bar)"
        container.mounts[1] == "/hum from bah (ro)"
        container.containerPorts == [8080]
    }

    def "create a Container http probe with empty port and no port"() {
        given:
        def kubeContainer = Mock(V1Container)
        def httpProbe = new V1HTTPGetAction().host("localhost").port(new IntOrString(""))
        def livenessProbe = new V1Probe().httpGet(httpProbe)
        kubeContainer.livenessProbe >> livenessProbe
        def httpProbe2 = new V1HTTPGetAction().host("localhost")
        def readinessProbe = new V1Probe().httpGet(httpProbe2)
        kubeContainer.readinessProbe >> readinessProbe
        kubeContainer.resources >> Mock(V1ResourceRequirements)
        List<V1ContainerStatus> containerStatuses = new ArrayList<>()

        when:
        def container = new Container(kubeContainer, containerStatuses)

        then:
        container.name == null
        container.containerId == null
        container.image == null
        container.imageId == null
        container.ports == ""
        container.hostPorts == ""
        container.commands == null
        container.args == null
        container.state == null
        container.lastState == null
        container.ready == null
        container.restartCount == null
        container.limits.size() == 0
        container.requests.size() == 0
        container.livenessProbe == "http-get null://localhost/null delay=nulls timeout=nulls period=nulls #success=null #failure=null"
        container.readinessProbe == "http-get null://localhost/null delay=nulls timeout=nulls period=nulls #success=null #failure=null"
        container.envFrom.size() == 0
        container.env.size() == 0
        container.mounts.size() == 0
        container.containerPorts == null
    }

    def "create a Container exec probe"() {
        given:
        def kubeContainer = Mock(V1Container)
        List<String> command = new ArrayList()
        command.add("command")
        command.add("param1")
        def execProbe = new V1ExecAction().command(command)
        def livenessProbe = new V1Probe().exec(execProbe)
        kubeContainer.livenessProbe >> livenessProbe
        kubeContainer.resources >> Mock(V1ResourceRequirements)
        List<V1ContainerStatus> containerStatuses = new ArrayList<>()

        when:
        def container = new Container(kubeContainer, containerStatuses)

        then:
        container.name == null
        container.containerId == null
        container.image == null
        container.imageId == null
        container.ports == ""
        container.hostPorts == ""
        container.commands == null
        container.args == null
        container.state == null
        container.lastState == null
        container.ready == null
        container.restartCount == null
        container.limits.size() == 0
        container.requests.size() == 0
        container.livenessProbe == "exec command,param1 delay=nulls timeout=nulls period=nulls #success=null #failure=null"
        container.readinessProbe == null
        container.envFrom.size() == 0
        container.env.size() == 0
        container.mounts.size() == 0
        container.containerPorts == null
    }

    def "create a Container node allocated memory env var"() {
        given:
        def kubeContainer = Mock(V1Container)
        V1EnvVar envVarLimitMemory = createEnvVarResourceField("var4", "0", "limits.memory")
        V1EnvVar envVarLimitStorage = createEnvVarResourceField("var5", "0", "limits.ephemeral-storage")
        V1EnvVar envVarRequestCpu = createEnvVarResourceField("var6", "1234", "requests.cpu")
        V1EnvVar envVarRequestMemory = createEnvVarResourceField("var7", "1073742000", "requests.memory")
        V1EnvVar envVarRequestStorage = createEnvVarResourceField("var8", "1073742000", "requests.ephemeral-storage")
        V1EnvVar envVarRequestUnknown = createEnvVarResourceField("var9", "1073742000", "requests.foobar")
        kubeContainer.env >> [envVarLimitMemory, envVarLimitStorage, envVarRequestCpu, envVarRequestMemory, envVarRequestStorage, envVarRequestUnknown]
        def resourceRequirements = Mock(V1ResourceRequirements)
        Map<String, Quantity> limits = new HashMap<>()
        limits.put("cpu", new Quantity("1"))
        limits.put("memory", new Quantity("0"))
        limits.put("ephemeral-storage", new Quantity("0"))
        resourceRequirements.limits >> limits
        Map<String, Quantity> requests = new HashMap<>()
        requests.put("cpu", new Quantity("1"))
        requests.put("memory", new Quantity("1Gi"))
        requests.put("ephemeral-storage", new Quantity("1Gi"))
        resourceRequirements.requests >> requests
        kubeContainer.resources >> resourceRequirements
        List<V1ContainerStatus> containerStatuses = new ArrayList<>()

        when:
        def container = new Container(kubeContainer, containerStatuses)

        then:
        container.name == null
        container.containerId == null
        container.image == null
        container.imageId == null
        container.ports == ""
        container.hostPorts == ""
        container.commands == null
        container.args == null
        container.state == null
        container.lastState == null
        container.ready == null
        container.restartCount == null
        container.limits.size() == 3
        container.requests.size() == 3
        container.livenessProbe == null
        container.readinessProbe == null
        container.envFrom.size() == 0
        container.env.size() == 6
        container.env[0].name == "var4"
        container.env[0].value == "node allocatable (limits.memory)"
        container.env[0].optional == null
        container.env[1].name == "var5"
        container.env[1].value == "0 (limits.ephemeral-storage)"
        container.env[1].optional == null
        container.env[2].name == "var6"
        container.env[2].value == "1 (requests.cpu)"
        container.env[2].optional == null
        container.env[3].name == "var7"
        container.env[3].value == "1 (requests.memory)"
        container.env[3].optional == null
        container.env[4].name == "var8"
        container.env[4].value == "1 (requests.ephemeral-storage)"
        container.env[4].optional == null
        container.env[5].name == "var9"
        container.env[5].value == " (requests.foobar)"
        container.env[5].optional == null
        container.mounts.size() == 0
        container.containerPorts == null
    }

    private createEnvVarResourceField(def name, def divisor, def resource) {
        V1EnvVar envVar = new V1EnvVar()
        envVar.setName(name)
        V1EnvVarSource envVarSource = new V1EnvVarSource()
        V1ResourceFieldSelector resourceFieldSelector = new V1ResourceFieldSelector()
        resourceFieldSelector.setDivisor(divisor)
        resourceFieldSelector.setResource(resource)
        envVarSource.setResourceFieldRef(resourceFieldSelector)
        envVar.setValueFrom(envVarSource)

        return envVar
    }
}
