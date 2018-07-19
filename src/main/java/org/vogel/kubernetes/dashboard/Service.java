package org.vogel.kubernetes.dashboard;

import io.kubernetes.client.models.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;
import static org.vogel.kubernetes.dashboard.FormatUtils.joinListWithCommas;

@Getter
public class Service extends Metadata {
    private String svcType;
    private String clusterIp;
    private String externalIp;
    private String ports;
    private String selector;
    private String specExternalIp;
    private String loadBalancerIp;
    private String externalName;
    private String loadBalancerIngress;
    private List<ServicePort> servicePorts;
    private String sessionAffinity;
    private String externalTrafficPolicy;
    private int healthCheckNodePort;
    private String loadBalancerSourceRanges;

    public Service(V1Service service) {
        super(service.getMetadata());
        V1ServiceSpec serviceSpec = service.getSpec();
        V1ServiceStatus serviceStatus = service.getStatus();
        svcType = serviceSpec.getType();
        clusterIp = serviceSpec.getClusterIP();
        if (isBlank(clusterIp)) {
            clusterIp = "<none>";
        }
        externalIp = getServiceExternalIP(service);
        ports = makePortString(serviceSpec.getPorts());

        if (serviceSpec.getSelector() != null) {
            selector = serviceSpec.getSelector()
                    .entrySet()
                    .stream()
                    .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                    .sorted()
                    .collect(joining(","));
        }
        if (serviceSpec.getExternalIPs() != null && serviceSpec.getExternalIPs()
                .size() > 0) {
            specExternalIp = joinListWithCommas(serviceSpec.getExternalIPs());
        }
        loadBalancerIp = serviceSpec.getLoadBalancerIP();
        externalName = serviceSpec.getExternalName();
        List<V1LoadBalancerIngress> ingressList = serviceStatus.getLoadBalancer()
                .getIngress();
        if (ingressList != null && ingressList.size() > 0) {
            loadBalancerIngress = loadBalancerStatusStringer(serviceStatus.getLoadBalancer());
        }
        sessionAffinity = serviceSpec.getSessionAffinity();
        externalTrafficPolicy = serviceSpec.getExternalTrafficPolicy();
        if (serviceSpec.getHealthCheckNodePort() != null) {
            healthCheckNodePort = serviceSpec.getHealthCheckNodePort();
        }
        if (serviceSpec.getLoadBalancerSourceRanges() != null && serviceSpec.getLoadBalancerSourceRanges()
                .size() > 0) {
            loadBalancerSourceRanges = joinListWithCommas(serviceSpec.getLoadBalancerSourceRanges());
        }
    }

    public Service(V1Service service, V1Endpoints endpointsList) {
        this(service);
        V1ServiceSpec serviceSpec = service.getSpec();
        servicePorts = serviceSpec.getPorts()
                .stream()
                .map(sp -> new ServicePort(sp, endpointsList))
                .collect(toList());
    }

    private String getServiceExternalIP(V1Service service) {
        V1ServiceSpec spec = service.getSpec();
        if (equalsAny(svcType, "ClusterIP", "NodePort")) {
            List<String> externalIPs = spec.getExternalIPs();
            if (externalIPs != null && externalIPs.size() > 0) {
                return joinListWithCommas(externalIPs);
            }
            return "<none>";
        } else if (svcType.equals("LoadBalancer")) {
            String lbIps = loadBalancerStatusStringer(service.getStatus()
                                                              .getLoadBalancer());
            List<String> externalIPs = spec.getExternalIPs();
            if (externalIPs.size() > 0) {
                List<String> results = new ArrayList<>();
                results.add(lbIps);
                results.addAll(externalIPs);
                return joinListWithCommas(results);
            }

            if (isNotBlank(lbIps)) {
                return lbIps;
            }

            return "<pending>";
        } else if (svcType.equals("ExternalName")) {
            return spec.getExternalName();
        }

        return "<unknown>";
    }

    private String loadBalancerStatusStringer(V1LoadBalancerStatus loadBalancer) {
        List<V1LoadBalancerIngress> ingress = loadBalancer.getIngress();

        return ingress.stream()
                .map(this::ingressToString)
                .collect(joining(","));
    }

    private String ingressToString(V1LoadBalancerIngress ing) {
        if (isNotBlank(ing.getIp())) {
            return ing.getIp();
        } else if (isNotBlank(ing.getHostname())) {
            return ing.getHostname();
        }

        return null;
    }

    private String makePortString(List<V1ServicePort> ports) {
        if (ports.size() == 0) {
            return "<none>";
        } else {
            return ports.stream()
                    .map(this::portToString)
                    .collect(joining(","));
        }
    }

    private String portToString(V1ServicePort port) {
        if (port.getNodePort() != null && port.getNodePort() > 0) {
            return String.format("%d:%d/%s", port.getPort(), port.getNodePort(), port.getProtocol());
        } else {
            return String.format("%d/%s", port.getPort(), port.getProtocol());
        }
    }
}
