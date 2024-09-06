export interface PodStatusDTO {
    kind: string;
    metadata: MetadataDTO;
    spec: SpecDTO;
    status?: StatusDTO;
}

interface MetadataDTO {
    name: string;
    uid?: string;
}

interface SpecDTO {
    nodeName?: string;
    containers?: ContainerDTO[];
}

interface ContainerDTO {
    image: string;
    name: string;
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
    state: string;
}


export function createPodStatusDTO(data : any): PodStatusDTO {
    const metadata: MetadataDTO = {
        name: data.metadata.name,
        uid: data.metadata?.uid
    };

    const containers: ContainerDTO[] | undefined = Array.isArray(data.spec?.containers)
        ? data.spec.containers.map((container: any) => ({
            image: container.image,
            name: container.name,
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
        }))
    : undefined;
    const spec: SpecDTO = {
        nodeName: data.spec?.nodeName,
        containers: containers
    };

    const podStatusDTO: PodStatusDTO = {
        kind: "Pod",
        metadata: metadata,
        spec: spec,
        status: undefined,
    };

    return podStatusDTO;
}