package org.vogel.kubernetes.dashboard;

import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1ContainerState;
import io.kubernetes.client.models.V1ContainerStateTerminated;
import io.kubernetes.client.models.V1ContainerStateWaiting;
import io.kubernetes.client.models.V1ContainerStatus;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1OwnerReference;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodCondition;
import io.kubernetes.client.models.V1PodSpec;
import io.kubernetes.client.models.V1PodStatus;
import io.kubernetes.client.models.V1Toleration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.vogel.kubernetes.dashboard.FormatUtils.printMultiline;
import static org.vogel.kubernetes.dashboard.FormatUtils.translateTimestamp;

@Slf4j
@Getter
public class Pod extends Metadata {
    private String ready;
    private String reason;
    private int restarts;
    private Integer priority;
    private String priorityClassName;
    private String node;
    private String hostIp;
    private DateTime startTime;
    private DateTime deletionTimestamp;
    private String deletionDuration;
    private long deletionGracePeriodSeconds;
    private String status;
    private String describeReason;
    private String message;
    private String podIp;
    private String controlledBy;
    private List<Container> initContainers;
    private List<Container> containers;
    private Map<String, String> conditions;
    private Volumes volumes;
    private String qos;
    private List<String> nodeSelectors;
    private List<String> tolerations;

    public Pod(V1Pod pod) {
        super(pod.getMetadata());
        restarts = 0;
        V1PodSpec podSpec = pod.getSpec();
        int totalContainers = podSpec.getContainers()
                .size();
        int readyContainers = 0;

        V1PodStatus podStatus = pod.getStatus();
        reason = podStatus.getPhase();
        describeReason = podStatus.getReason();
        if (isNotBlank(describeReason)) {
            reason = describeReason;
        }

        boolean initializing = false;
        List<V1ContainerStatus> initContainerStatuses = podStatus.getInitContainerStatuses();
        if (initContainerStatuses != null) {
            initializing = processInitContainerStatuses(podSpec, initializing, initContainerStatuses);
        }

        List<V1ContainerStatus> containerStatuses = podStatus.getContainerStatuses();
        if (!initializing) {
            restarts = 0;
            boolean hasRunning = false;
            if (containerStatuses != null) {
                for (int i = containerStatuses.size() - 1; i >= 0; i--) {
                    V1ContainerStatus container = containerStatuses.get(i);

                    restarts += container.getRestartCount();
                    V1ContainerState containerState = container.getState();
                    V1ContainerStateTerminated terminated = containerState.getTerminated();
                    V1ContainerStateWaiting waiting = containerState.getWaiting();
                    if (waiting != null && isNotEmpty(waiting.getReason())) {
                        reason = waiting.getReason();
                    } else if (terminated != null && isNotEmpty(terminated.getReason())) {
                        reason = terminated.getReason();
                    } else if (terminated != null && isEmpty(terminated.getReason())) {
                        if (terminated.getSignal() != null && terminated.getSignal() != 0) {
                            reason = String.format("Signal:%d", terminated.getSignal());
                        } else {
                            reason = String.format("ExitCode:%d", terminated.getExitCode());
                        }
                    } else if (container.isReady() && containerState.getRunning() != null) {
                        hasRunning = true;
                        readyContainers++;
                    }

                    // change pod status back to "Running" if there is at least one container still reporting as "Running" status
                    if (reason.equals("Completed") && hasRunning) {
                        reason = "Running";
                    }
                }
            }
        }

        V1ObjectMeta metadata = pod.getMetadata();
        deletionTimestamp = metadata.getDeletionTimestamp();
        if (deletionTimestamp != null && StringUtils.equals("NodeLost", describeReason)) {
            reason = "Unknown";
        } else {
            reason = "Terminating";
        }

        ready = String.format("%d/%d", readyContainers, totalContainers);

        priority = podSpec.getPriority();
        if (priority != null) {
            priorityClassName = podSpec.getPriorityClassName();
        }
        node = podSpec.getNodeName();
        hostIp = podStatus.getHostIP();
        startTime = podStatus.getStartTime();

        if (deletionTimestamp != null) {
            deletionDuration = translateTimestamp(deletionTimestamp);
            deletionGracePeriodSeconds = metadata.getDeletionGracePeriodSeconds();
        }

        status = podStatus.getPhase();
        log.info("status = {}", status);
        message = podStatus.getMessage();
        podIp = podStatus.getPodIP();
        List<V1OwnerReference> ownerReferences = metadata.getOwnerReferences();
        if (ownerReferences != null) {
            Optional<V1OwnerReference> ownerReference = ownerReferences.stream()
                    .filter(V1OwnerReference::isController)
                    .findFirst();
            if (ownerReference.isPresent()) {
                V1OwnerReference ref = ownerReference.get();
                controlledBy = String.format("%s/%s", ref.getKind(), ref.getName());
            }
        }

        List<V1Container> kubeInitContainers = podSpec.getInitContainers();
        if (kubeInitContainers != null) {
            initContainers = kubeInitContainers.stream()
                    .map(container -> new Container(container, containerStatuses))
                    .collect(toList());
        }
        containers = podSpec.getContainers()
                .stream()
                .map(container -> new Container(container, containerStatuses))
                .collect(toList());

        List<V1PodCondition> podConditions = podStatus.getConditions();
        if (CollectionUtils.isNotEmpty(podConditions)) {
            conditions = podConditions.stream()
                    .collect(toMap(V1PodCondition::getType, V1PodCondition::getStatus, (p1, p2) -> p1,
                                   LinkedHashMap::new));
        }

        volumes = new Volumes(podSpec.getVolumes());

        if (isNotBlank(podStatus.getQosClass())) {
            qos = podStatus.getQosClass();
        }

        nodeSelectors = printMultiline(podSpec.getNodeSelector());
        printPodTolerations(podSpec.getTolerations());
    }

    private boolean processInitContainerStatuses(V1PodSpec podSpec, boolean initializing,
                                                 List<V1ContainerStatus> initContainerStatuses) {
        for (int i = 0; i < initContainerStatuses.size(); i++) {
            V1ContainerStatus container = initContainerStatuses.get(i);
            restarts += container.getRestartCount();

            V1ContainerState containerState = container.getState();
            V1ContainerStateTerminated terminated = containerState.getTerminated();
            V1ContainerStateWaiting waiting = containerState.getWaiting();
            if (terminated != null && terminated.getExitCode() == 0) {
                continue;
            } else if (terminated != null) {
                // initialization is failed
                if (isBlank(terminated.getReason())) {
                    if (terminated.getSignal() != 0) {
                        reason = String.format("Init:Signal:%d", terminated.getSignal());
                    } else {
                        reason = String.format("Init:ExitCode:%d", terminated.getExitCode());
                    }
                } else {
                    reason = "Init:" + terminated.getReason();
                }
                initializing = true;
            } else if (waiting != null && isNotBlank(waiting.getReason()) && !waiting.getReason()
                    .equals("PodInitializing")) {
                reason = "Init:" + waiting.getReason();
                initializing = true;
            } else {
                reason = String.format("Init:%d/%d", i, podSpec.getInitContainers()
                        .size());
                initializing = true;
            }
            break;
        }
        return initializing;
    }

    private void printPodTolerations(List<V1Toleration> podSpecTolerations) {
        if (CollectionUtils.isNotEmpty(podSpecTolerations)) {
            tolerations = new ArrayList<>();
            for (V1Toleration podToleration : podSpecTolerations) {
                StringBuilder tol = new StringBuilder();
                tol.append(podToleration.getKey());
                if (isNotBlank(podToleration.getValue())) {
                    tol.append("=")
                            .append(podToleration.getValue());
                }
                if (isNotBlank(podToleration.getEffect())) {
                    tol.append(":")
                            .append(podToleration.getEffect());
                }
                if (podToleration.getTolerationSeconds() != null) {
                    tol.append(" for ")
                            .append(podToleration.getTolerationSeconds())
                            .append("s");
                }
                tolerations.add(tol.toString());
            }
        }
    }
}
