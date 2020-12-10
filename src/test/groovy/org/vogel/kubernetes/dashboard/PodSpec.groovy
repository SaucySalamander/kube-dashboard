package org.vogel.kubernetes.dashboard

import io.kubernetes.client.models.*
import spock.lang.Specification

class PodSpec extends Specification {
    def "creating a Pod mainly empty"() {
        given:
        def kubePod = Mock(V1Pod)
        def metadata = Mock(V1ObjectMeta)
        kubePod.metadata >> metadata
        def spec = Mock(V1PodSpec)
        List<V1Container> containers = new ArrayList<>()
        spec.containers >> containers
        kubePod.spec >> spec
        def status = Mock(V1PodStatus)
        kubePod.status >> status

        when:
        def pod = new Pod(kubePod)

        then:
        pod.ready == "0/0"
        pod.reason == "Terminating"
        pod.restarts == 0
        pod.priority == null
        pod.priorityClassName == null
        pod.node == null
        pod.hostIp == null
        pod.startTime == null
        pod.deletionTimestamp == null
        pod.deletionDuration == null
        pod.deletionGracePeriodSeconds == 0
        pod.status == null
        pod.describeReason == null
        pod.message == null
        pod.podIp == null
        pod.controlledBy == null
        pod.initContainers == null
        pod.containers.size() == 0
        pod.conditions == null
        pod.volumes != null
        pod.qos == null
        pod.nodeSelectors == null
        pod.tolerations == null
    }

    def "creating a Pod"() {
        given:
        def kubePod = Mock(V1Pod)
        def metadata = Mock(V1ObjectMeta)
        kubePod.metadata >> metadata
        def spec = Mock(V1PodSpec)
        List<V1Container> containers = new ArrayList<>()
        spec.containers >> containers
        List<V1Container> initContainers = new ArrayList<>()
        spec.initContainers >> initContainers
        kubePod.spec >> spec
        def status = Mock(V1PodStatus)
        status.reason >> "because"
        List<V1ContainerStatus> initContainerStatuses = new ArrayList<>()
        V1ContainerStatus containerStatus = new V1ContainerStatus()
        V1ContainerState containerState = new V1ContainerState()
        V1ContainerStateTerminated terminated = new V1ContainerStateTerminated()
        terminated.exitCode = 0
        containerState.terminated = terminated
        containerStatus.state = containerState
        containerStatus.restartCount = 3
        initContainerStatuses << containerStatus
        status.initContainerStatuses >> initContainerStatuses
        List<V1ContainerStatus> containerStatuses = new ArrayList<>()
        V1ContainerStatus containerStatus1 = new V1ContainerStatus()
        containerStatus1.ready = true
        V1ContainerState containerState1 = new V1ContainerState()
        containerState1.running = new V1ContainerStateRunning()
        containerStatus1.state = containerState1
        containerStatus1.restartCount = 3
        containerStatuses << containerStatus1
        status.containerStatuses >> containerStatuses
        kubePod.status >> status

        when:
        def pod = new Pod(kubePod)

        then:
        pod.ready == "1/0"
        pod.reason == "Terminating"
        pod.restarts == 3
        pod.priority == null
        pod.priorityClassName == null
        pod.node == null
        pod.hostIp == null
        pod.startTime == null
        pod.deletionTimestamp == null
        pod.deletionDuration == null
        pod.deletionGracePeriodSeconds == 0
        pod.status == null
        pod.describeReason == "because"
        pod.message == null
        pod.podIp == null
        pod.controlledBy == null
        pod.initContainers.size() == 0
        pod.containers.size() == 0
        pod.conditions == null
        pod.volumes != null
        pod.qos == null
        pod.nodeSelectors == null
        pod.tolerations == null
    }
}
