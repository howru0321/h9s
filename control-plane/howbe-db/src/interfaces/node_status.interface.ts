import { ObjectStatus } from './object_status.interface';

export interface NodeStatusDTO extends ObjectStatus {
  spec: SpecDTO;
  status: StatusDTO;
}

export interface SpecDTO {
  podCIDR?: string;
}

export interface ResourceDTO {
  cpu: string;
  memory: string;
}

export interface StatusDTO {
  capacity?: ResourceDTO;
  allocatable?: ResourceDTO;
  conditions?: ConditionDTO[];
  addresses?: AddressDTO[];
  nodeInfo?: NodeInfoDTO;
  images?: ImageDTO[];
}

export interface ConditionDTO {
  type: string;
  status: string;
  reason: string;
  message: string;
}

export interface AddressDTO {
  type: string;
  address: string;
}

export interface NodeInfoDTO {
  containerRuntimeVersion: string;
  operatingSystem: string;
  architecture: string;
}

export interface ImageDTO {
  names: string[];
  sizeBytes: number;
}

export function updateNodeStatus(target: NodeStatusDTO, source: NodeStatusDTO): NodeStatusDTO {
    // Deep copy to avoid modifying the original object
    const updated = JSON.parse(JSON.stringify(target)) as NodeStatusDTO;

    // Update metadata
    updated.metadata = updated.metadata;
    updated.metadata.name = source.metadata.name;
    if (source.metadata?.uid) updated.metadata.uid = source.metadata.uid;

    // Update spec
    if (source?.spec) {
        updated.spec = updated.spec || {};
        if (source.spec?.podCIDR) updated.spec.podCIDR = source.spec.podCIDR;
    }

    // Update status
    if (source?.status) {
        updated.status = updated.status || {};
        
        // Update capacity and allocatable
        if (source.status?.capacity) updated.status.capacity = source.status.capacity;
        if (source.status?.allocatable) updated.status.allocatable = source.status.allocatable;

        // Update conditions
        if (source.status?.conditions) {
            updated.status.conditions = source.status.conditions;
        }

        // Update addresses
        if (source.status?.addresses) {
            updated.status.addresses = source.status.addresses;
        }

        // Update nodeInfo
        if (source.status?.nodeInfo) {
            updated.status.nodeInfo = {
                ...updated.status.nodeInfo,
                ...source.status.nodeInfo
            };
        }

        // Update images
        if (source.status?.images) {
            updated.status.images = source.status.images;
        }
    }

    return updated;
}