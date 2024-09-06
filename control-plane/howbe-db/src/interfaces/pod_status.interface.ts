import { ObjectStatus } from './object_status.interface'

export interface PodStatusDTO extends ObjectStatus{
    //metadata: MetadataDTO;
    spec?: SpecDTO;
    status?: StatusDTO;
}

// interface MetadataDTO {
//     name: string;
//     uid?: string;
// }

interface SpecDTO {
    nodeName?: string;
    containers?: ContainerDTO[];
}

interface ContainerDTO {
    image: string;
    name: string;
    resources?: ResourceRequirementsDTO;
}

interface ResourceRequirementsDTO {
    limits: ResourceDTO;
    requests: ResourceDTO;
}

interface ResourceDTO {
    cpu: string;
    memory: string;
}

interface StatusDTO {
    phase?: string;
    nodeName?: string;
    hostIp?: string;
    podIp?: string;
    conditions?: ConditionDTO[];
    containerStatuses?: ContainerStatusDTO[];
}

interface ConditionDTO {
    status: string;
    type: string;
}

interface ContainerStatusDTO {
    image: string;
    imageId: string;
    name: string;
    containerId: string;
    state?: string;
}


export function updatePodStatus(target: PodStatusDTO, source: PodStatusDTO): PodStatusDTO {
    // Deep copy to avoid modifying the original object
    const updated = JSON.parse(JSON.stringify(target)) as PodStatusDTO;

    // Update metadata
    updated.metadata = updated.metadata;
    updated.metadata.name = source.metadata.name;
    if (source.metadata?.uid) updated.metadata.uid = source.metadata.uid;

    // Update spec
    if (source?.spec) {
        updated.spec = updated.spec || {};
        if (source.spec?.nodeName) updated.spec.nodeName = source.spec.nodeName;
        if (source.spec?.containers) {
            updated.spec.containers = source.spec.containers.map(container => ({
                ...container,
                resources: container.resources ? {
                    limits: {
                        cpu: container.resources.limits.cpu,
                        memory: container.resources.limits.memory
                    },
                    requests: {
                        cpu: container.resources.requests.cpu,
                        memory: container.resources.requests.memory
                    }
                } : undefined
            }));
        }
    }

    // Update status
    if (source?.status) {
        updated.status = updated.status || {};
        if (source.status?.phase) updated.status.phase = source.status.phase;
        if (source.status?.nodeName) updated.status.nodeName = source.status.nodeName;
        if (source.status?.hostIp) updated.status.hostIp = source.status.hostIp;
        if (source.status?.podIp) updated.status.podIp = source.status.podIp;
        
        // Update conditions
        if (source.status?.conditions) {
            updated.status.conditions = source.status.conditions;
        }

        // Update containerStatuses
        if (source.status?.containerStatuses) {
            updated.status.containerStatuses = source.status.containerStatuses;
        }
    }

    return updated;
}
