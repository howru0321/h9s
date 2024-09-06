import { PodStatusDTO, updatePodStatus } from "./pod_status.interface";
import { NodeStatusDTO, updateNodeStatus } from "./node_status.interface";

export interface ObjectStatus{
  kind: string;
  metadata: MetadataDTO;
}

export interface MetadataDTO {
  name: string;
  uid?: string;
}

export function updateObjectStatus(target: ObjectStatus, source: ObjectStatus): ObjectStatus {
  if(!target){
    return source;
  }

  if (target.kind !== source.kind) {
    throw new Error("Cannot update objects of different kinds");
  }

  switch (target.kind) {
    case "Pod":
      return updatePodStatus(target as PodStatusDTO, source as PodStatusDTO);
    case "Node":
      return updateNodeStatus(target as NodeStatusDTO, source as NodeStatusDTO);
    default:
      throw new Error(`Unsupported object kind: ${target.kind}`);
  }
}