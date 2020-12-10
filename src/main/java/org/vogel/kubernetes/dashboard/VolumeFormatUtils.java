package org.vogel.kubernetes.dashboard;

import io.kubernetes.client.models.V1AWSElasticBlockStoreVolumeSource;
import io.kubernetes.client.models.V1AzureDiskVolumeSource;
import io.kubernetes.client.models.V1AzureFilePersistentVolumeSource;
import io.kubernetes.client.models.V1AzureFileVolumeSource;
import io.kubernetes.client.models.V1CephFSPersistentVolumeSource;
import io.kubernetes.client.models.V1CephFSVolumeSource;
import io.kubernetes.client.models.V1CinderPersistentVolumeSource;
import io.kubernetes.client.models.V1CinderVolumeSource;
import io.kubernetes.client.models.V1ConfigMapVolumeSource;
import io.kubernetes.client.models.V1DownwardAPIVolumeFile;
import io.kubernetes.client.models.V1DownwardAPIVolumeSource;
import io.kubernetes.client.models.V1EmptyDirVolumeSource;
import io.kubernetes.client.models.V1FCVolumeSource;
import io.kubernetes.client.models.V1FlexPersistentVolumeSource;
import io.kubernetes.client.models.V1FlexVolumeSource;
import io.kubernetes.client.models.V1FlockerVolumeSource;
import io.kubernetes.client.models.V1GCEPersistentDiskVolumeSource;
import io.kubernetes.client.models.V1GitRepoVolumeSource;
import io.kubernetes.client.models.V1GlusterfsVolumeSource;
import io.kubernetes.client.models.V1HostPathVolumeSource;
import io.kubernetes.client.models.V1ISCSIPersistentVolumeSource;
import io.kubernetes.client.models.V1ISCSIVolumeSource;
import io.kubernetes.client.models.V1NFSVolumeSource;
import io.kubernetes.client.models.V1PersistentVolumeClaimVolumeSource;
import io.kubernetes.client.models.V1PhotonPersistentDiskVolumeSource;
import io.kubernetes.client.models.V1PortworxVolumeSource;
import io.kubernetes.client.models.V1QuobyteVolumeSource;
import io.kubernetes.client.models.V1RBDPersistentVolumeSource;
import io.kubernetes.client.models.V1RBDVolumeSource;
import io.kubernetes.client.models.V1ScaleIOPersistentVolumeSource;
import io.kubernetes.client.models.V1ScaleIOVolumeSource;
import io.kubernetes.client.models.V1SecretVolumeSource;
import io.kubernetes.client.models.V1StorageOSPersistentVolumeSource;
import io.kubernetes.client.models.V1StorageOSVolumeSource;
import io.kubernetes.client.models.V1VsphereVirtualDiskVolumeSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.vogel.kubernetes.dashboard.FormatUtils.joinListWithCommas;

public class VolumeFormatUtils {

    public static final String TYPE = "Type:";
    public static final String FSTYPE = "FSType:";
    public static final String READ_ONLY = "ReadOnly:";

    private VolumeFormatUtils() {
    }

    public static Map<String, String> printHostPathVolumeSource(V1HostPathVolumeSource hostPath) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "HostPath (bare host directory volume)");
        info.put("Path", hostPath.getPath());
        String type = defaultIfBlank(hostPath.getType(), "none");
        info.put("HostPathType:", type);

        return info;
    }

    public static Map<String, String> printEmptyDirVolumeSource(V1EmptyDirVolumeSource emptyDir) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "EmptyDir (a temporary directory that shares a pod's lifetime)");
        info.put("Medium:", emptyDir.getMedium());

        return info;
    }

    public static Map<String, String> printGCEPersistentDiskVolumeSource(V1GCEPersistentDiskVolumeSource gce) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "GCEPersistentDisk (a Persistent Disk resource in Google Compute Engine)");
        info.put("PDName:", gce.getPdName());
        info.put(FSTYPE, gce.getFsType());
        info.put("Partition:", gce.getPartition()
                .toString());
        info.put(READ_ONLY, Boolean.toString(Boolean.TRUE.equals(gce.isReadOnly())));

        return info;
    }

    public static Map<String, String> printAWSElasticBlockStoreVolumeSource(V1AWSElasticBlockStoreVolumeSource aws) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "AWSElasticBlockStore (a Persistent Disk resource in AWS)");
        info.put("VolumeID:", aws.getVolumeID());
        info.put(FSTYPE, aws.getFsType());
        info.put("Partition:", aws.getPartition()
                .toString());
        info.put(READ_ONLY, Boolean.toString(Boolean.TRUE.equals(aws.isReadOnly())));

        return info;
    }

    public static Map<String, String> printGitRepoVolumeSource(V1GitRepoVolumeSource git) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "GitRepo (a volume that is pulled from git when the pod is created)");
        info.put("Repository:", git.getRepository());
        info.put("Revision:", git.getRevision());

        return info;
    }

    public static Map<String, String> printSecretVolumeSource(V1SecretVolumeSource secret) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "Secret (a volume populated by a Secret)");
        info.put("SecretName", secret.getSecretName());
        info.put("Optional:", Boolean.toString(Boolean.TRUE.equals(secret.isOptional())));

        return info;
    }

    public static Map<String, String> printConfigMapVolumeSource(V1ConfigMapVolumeSource configMap) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "ConfigMap (a volume populated by a ConfigMap)");
        info.put("Name:", configMap.getName());
        info.put("Optional:", Boolean.toString(Boolean.TRUE.equals(configMap.isOptional())));

        return info;
    }

    public static Map<String, String> printNFSVolumeSource(V1NFSVolumeSource nfs) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "NFS (an NFS mount that lasts the lifetime of a pod)");
        info.put("Server:", nfs.getServer());
        info.put("Path:", nfs.getPath());
        info.put(READ_ONLY, Boolean.toString(Boolean.TRUE.equals(nfs.isReadOnly())));

        return info;
    }

    public static Map<String, String> printISCSIVolumeSource(V1ISCSIVolumeSource iscsi) {
        return printISCSI(iscsi.getTargetPortal(), iscsi.getIqn(), iscsi.getLun(), iscsi.getIscsiInterface(),
                          iscsi.getFsType(), iscsi.isReadOnly(), iscsi.getPortals(),
                          iscsi.isChapAuthDiscovery(), iscsi.isChapAuthSession(), iscsi.getSecretRef()
                                  .getName(), iscsi.getInitiatorName());
    }

    public static Map<String, String> printISCSIPersistentVolumeSource(V1ISCSIPersistentVolumeSource iscsi) {
        return printISCSI(iscsi.getTargetPortal(), iscsi.getIqn(), iscsi.getLun(), iscsi.getIscsiInterface(),
                          iscsi.getFsType(), iscsi.isReadOnly(), iscsi.getPortals(),
                          iscsi.isChapAuthDiscovery(),
                          iscsi.isChapAuthSession(), iscsi.getSecretRef()
                                  .getName(), iscsi.getInitiatorName());
    }

    private static Map<String, String> printISCSI(String targetPortal, String iqn, Integer lun,
                                                  String iscsiInterface, String fsType, Boolean readOnly,
                                                  List<String> portals2, Boolean chapAuthDiscovery,
                                                  Boolean chapAuthSession, String name, String initiatorName) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE,
                 "ISCSI (an ISCSI Disk resource that is attached to a kubelet's host machine and then exposed to the pod)");
        info.put("TargetPortal:", targetPortal);
        info.put("IQN:", iqn);
        info.put("Lun:", lun
                .toString());
        info.put("ISCSIInterface:", iscsiInterface);
        info.put(FSTYPE, fsType);
        info.put(READ_ONLY, Boolean.toString(Boolean.TRUE.equals(readOnly)));
        String portals = joinListWithCommas(portals2);
        info.put("Portals:", portals);
        info.put("DiscoveryCHAPAuth:", Boolean.toString(Boolean.TRUE.equals(chapAuthDiscovery)));
        info.put("SessionCHAPAuth:", Boolean.toString(Boolean.TRUE.equals(chapAuthSession)));
        info.put("SecretRef:", name);
        info.put("InitiatorName:", initiatorName);

        return info;
    }

    public static Map<String, String> printGlusterfsVolumeSource(V1GlusterfsVolumeSource glusterfs) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "Glusterfs (a Glusterfs mount on the host that shares a pod's lifetime)");
        info.put("EndpointsName:", glusterfs.getEndpoints());
        info.put("Path:", glusterfs.getPath());
        info.put(READ_ONLY, Boolean.toString(Boolean.TRUE.equals(glusterfs.isReadOnly())));

        return info;
    }

    public static Map<String, String> printPersistentVolumeClaimVolumeSource(
            V1PersistentVolumeClaimVolumeSource claim) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "PersistentVolumeClaim (a reference to a PersistentVolumeClaim in the same namespace)");
        info.put("ClaimName:", claim.getClaimName());
        info.put(READ_ONLY, Boolean.toString(Boolean.TRUE.equals(claim.isReadOnly())));

        return info;
    }

    public static Map<String, String> printRBDVolumeSource(V1RBDVolumeSource rbd) {
        return printRBD(rbd.getMonitors(), rbd.getImage(), rbd.getFsType(), rbd.getPool(), rbd.getUser(),
                        rbd.getKeyring(), rbd.getSecretRef()
                                .getName(), rbd.isReadOnly());
    }

    public static Map<String, String> printRBDPersistentVolumeSource(V1RBDPersistentVolumeSource rbd) {
        return printRBD(rbd.getMonitors(), rbd.getImage(), rbd.getFsType(), rbd.getPool(), rbd.getUser(),
                        rbd.getKeyring(),
                        rbd.getSecretRef()
                                .getName(), rbd.isReadOnly());
    }

    private static Map<String, String> printRBD(List<String> monitors2, String image, String fsType,
                                                String pool, String user, String keyring, String name,
                                                Boolean readOnly) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "RBD (a Rados Block Device mount on the host that shares a pod's lifetime)");
        String monitors = joinListWithCommas(monitors2);
        info.put("CephMonitors:", monitors);
        info.put("RBDImage:", image);
        info.put(FSTYPE, fsType);
        info.put("RBDPool:", pool);
        info.put("RadosUser:", user);
        info.put("Keyring:", keyring);
        info.put("SecretRef:", name);
        info.put(READ_ONLY, Boolean.toString(Boolean.TRUE.equals(readOnly)));

        return info;
    }

    public static Map<String, String> printQuobyteVolumeSource(V1QuobyteVolumeSource quobyte) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "Quobyte (a Quobyte mount on the host that shares a pod's lifetime)");
        info.put("Registry:", quobyte.getRegistry());
        info.put("Volume:", quobyte.getVolume());
        info.put(READ_ONLY, Boolean.toString(Boolean.TRUE.equals(quobyte.isReadOnly())));

        return info;
    }

    public static Map<String, String> printDownwardAPIVolumeSource(V1DownwardAPIVolumeSource d) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "DownwardAPI (a volume populated by information about the pod)");
        for (V1DownwardAPIVolumeFile mapping : d.getItems()) {
            if (mapping.getFieldRef() != null) {
                info.put(mapping.getFieldRef()
                                 .getFieldPath(), mapping.getPath());
            }
            if (mapping.getResourceFieldRef() != null) {
                info.put(mapping.getResourceFieldRef()
                                 .getResource(), mapping.getPath());
            }
        }

        return info;
    }

    public static Map<String, String> printAzureDiskVolumeSource(V1AzureDiskVolumeSource d) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "AzureDisk (an Azure Data Disk mount on the host and bind mount to the pod)");
        info.put("DiskName:", d.getDiskName());
        info.put("DiskURI:", d.getDiskURI());
        info.put("Kind:", d.getKind());
        info.put(FSTYPE, d.getFsType());
        info.put("CachingMode:", d.getCachingMode());
        info.put(READ_ONLY, Boolean.toString(Boolean.TRUE.equals(d.isReadOnly())));

        return info;
    }

    public static Map<String, String> printVsphereVolumeSource(V1VsphereVirtualDiskVolumeSource vsphere) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "vSphereVolume (a Persistent Disk resource in vSphere)");
        info.put("VolumePath:", vsphere.getVolumePath());
        info.put(FSTYPE, vsphere.getFsType());
        info.put("StoragePolicyName:", vsphere.getStoragePolicyName());

        return info;
    }

    public static Map<String, String> printCinderVolumeSource(V1CinderVolumeSource cinder) {
        return printCinder(cinder.getVolumeID(), cinder.getFsType(), cinder.isReadOnly());
    }

    public static Map<String, String> printCinderPersistentVolumeSource(V1CinderPersistentVolumeSource cinder) {
        return printCinder(cinder.getVolumeID(), cinder.getFsType(), cinder.isReadOnly());
    }

    private static Map<String, String> printCinder(String volumeID, String fsType, Boolean readOnly) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "Cinder (a Persistent Disk resource in OpenStack)");
        info.put("VolumeID:", volumeID);
        info.put(FSTYPE, fsType);
        info.put(READ_ONLY, Boolean.toString(Boolean.TRUE.equals(readOnly)));

        return info;
    }

    public static Map<String, String> printPhotonPersistentDiskVolumeSource(V1PhotonPersistentDiskVolumeSource photon) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "PhotonPersistentDisk (a Persistent Disk resource in photon platform)");
        info.put("PdID:", photon.getPdID());
        info.put(FSTYPE, photon.getFsType());

        return info;
    }

    public static Map<String, String> printPortworxVolumeSource(V1PortworxVolumeSource pwxVolume) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "PortworxVolume (a Portworx Volume resource)");
        info.put("VolumeID:", pwxVolume.getVolumeID());

        return info;
    }

    public static Map<String, String> printScaleIOVolumeSource(V1ScaleIOVolumeSource sio) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "ScaleIO (a persistent volume backed by a block device in ScaleIO)");
        info.put("Gateway:", sio.getGateway());
        info.put("System:", sio.getSystem());
        info.put("Protection Domain:", sio.getProtectionDomain());
        info.put("Storage Pool:", sio.getStoragePool());
        info.put("Storage Mode:", sio.getStorageMode());
        info.put("VolumeName:", sio.getVolumeName());
        info.put(FSTYPE, sio.getFsType());
        info.put(READ_ONLY, Boolean.toString(Boolean.TRUE.equals(sio.isReadOnly())));

        return info;
    }

    public static Map<String, String> printScaleIOPersistentVolumeSource(V1ScaleIOPersistentVolumeSource sio) {
        String secretNs = "";
        String secretName = "";

        if (sio.getSecretRef() != null) {
            secretNs = sio.getSecretRef()
                    .getNamespace();
            secretName = sio.getSecretRef()
                    .getName();
        }

        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "ScaleIO (a persistent volume backed by a block device in ScaleIO)");
        info.put("Gateway:", sio.getGateway());
        info.put("System:", sio.getSystem());
        info.put("Protection Domain:", sio.getProtectionDomain());
        info.put("Storage Pool:", sio.getStoragePool());
        info.put("Storage Mode:", sio.getStorageMode());
        info.put("VolumeName:", sio.getVolumeName());
        info.put("SecretName:", secretName);
        info.put("SecretNamespace:", secretNs);
        info.put(FSTYPE, sio.getFsType());
        info.put(READ_ONLY, Boolean.toString(Boolean.TRUE.equals(sio.isReadOnly())));

        return info;
    }

    public static Map<String, String> printCephFSVolumeSource(V1CephFSVolumeSource cephfs) {
        return printCephFS(cephfs.getMonitors(), cephfs.getPath(), cephfs.getUser(), cephfs.getSecretFile(),
                           cephfs.getSecretRef()
                                   .getName(), cephfs.isReadOnly());
    }

    public static Map<String, String> printCephFSPersistentVolumeSource(V1CephFSPersistentVolumeSource cephfs) {
        return printCephFS(cephfs.getMonitors(), cephfs.getPath(), cephfs.getUser(), cephfs.getSecretFile(),
                           cephfs.getSecretRef()
                                   .getName(), cephfs.isReadOnly());
    }

    private static Map<String, String> printCephFS(List<String> cephMonitors, String path, String user,
                                                   String secretFile, String name, Boolean readOnly) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "CephFS (a CephFS mount on the host that shares a pod's lifetime)");
        String monitors = joinListWithCommas(cephMonitors);
        info.put("Monitors:", monitors);
        info.put("Path:", path);
        info.put("User:", user);
        info.put("SecretFile:", secretFile);
        info.put("SecretRef:", name);
        info.put(READ_ONLY, Boolean.toString(Boolean.TRUE.equals(readOnly)));

        return info;
    }

    public static Map<String, String> printStorageOSVolumeSource(V1StorageOSVolumeSource storageos) {
        return printStorageOS(storageos.getVolumeName(), storageos.getVolumeNamespace(), storageos.getFsType(),
                              storageos.isReadOnly());
    }

    public static Map<String, String> printStorageOSPersistentVolumeSource(
            V1StorageOSPersistentVolumeSource storageos) {
        return printStorageOS(storageos.getVolumeName(), storageos.getVolumeNamespace(), storageos.getFsType(),
                              storageos.isReadOnly());
    }

    private static Map<String, String> printStorageOS(String volumeName, String volumeNamespace, String fsType,
                                                      Boolean readOnly) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "StorageOS (a StorageOS Persistent Disk resource)");
        info.put("VolumeName:", volumeName);
        info.put("VolumeNamespace:", volumeNamespace);
        info.put(FSTYPE, fsType);
        info.put(READ_ONLY, Boolean.toString(Boolean.TRUE.equals(readOnly)));

        return info;
    }

    public static Map<String, String> printFCVolumeSource(V1FCVolumeSource fc) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "FC (a Fibre Channel disk)");
        String targetWwns = joinListWithCommas(fc.getTargetWWNs());
        info.put("TargetWWNs:", targetWwns);
        String lun = null;
        if (fc.getLun() != null) {
            lun = fc.getLun()
                    .toString();
        }
        info.put("LUN:", lun);
        info.put(FSTYPE, fc.getFsType());
        info.put(READ_ONLY, Boolean.toString(Boolean.TRUE.equals(fc.isReadOnly())));

        return info;
    }

    public static Map<String, String> printAzureFileVolumeSource(V1AzureFileVolumeSource azureFile) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "AzureFile (an Azure File Service mount on the host and bind mount to the pod)");
        info.put("SecretName:", azureFile.getSecretName());
        info.put("ShareName:", azureFile.getShareName());
        info.put(READ_ONLY, Boolean.toString(Boolean.TRUE.equals(azureFile.isReadOnly())));

        return info;
    }

    public static Map<String, String> printAzureFilePersistentVolumeSource(
            V1AzureFilePersistentVolumeSource azureFile) {
        Map<String, String> info = new LinkedHashMap<>();
        String ns = "";
        if (azureFile.getSecretNamespace() != null) {
            ns = azureFile.getSecretNamespace();
        }
        info.put(TYPE, "AzureFile (an Azure File Service mount on the host and bind mount to the pod)");
        info.put("SecretNamespace:", ns);
        info.put("SecretName:", azureFile.getSecretName());
        info.put("ShareName:", azureFile.getShareName());
        info.put(READ_ONLY, Boolean.toString(Boolean.TRUE.equals(azureFile.isReadOnly())));

        return info;
    }

    public static Map<String, String> printFlexVolumeSource(V1FlexVolumeSource flex) {
        return printFlex(flex.getDriver(), flex.getFsType(), flex.getSecretRef()
                .getName(), flex.isReadOnly(), flex.getOptions());
    }

    public static Map<String, String> printFlexPersistentVolumeSource(V1FlexPersistentVolumeSource flex) {
        return printFlex(flex.getDriver(), flex.getFsType(), flex.getSecretRef()
                .getName(), flex.isReadOnly(), flex.getOptions());
    }

    private static Map<String, String> printFlex(String driver, String fsType, String name, Boolean readOnly,
                                                 Map<String, String> options) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE,
                 "FlexVolume (a generic volume resource that is provisioned/attached using an exec based plugin)");
        info.put("Driver:", driver);
        info.put(FSTYPE, fsType);
        info.put("SecretRef:", name);
        info.put(READ_ONLY, Boolean.toString(Boolean.TRUE.equals(readOnly)));
        info.put("Options:", options
                .toString());

        return info;
    }

    public static Map<String, String> printFlockerVolumeSource(V1FlockerVolumeSource flocker) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put(TYPE, "Flocker (a Flocker volume mounted by the Flocker agent)");
        info.put("DatasetName:", flocker.getDatasetName());
        info.put("DatasetUUID:", flocker.getDatasetUUID());

        return info;
    }
}
