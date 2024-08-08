import { ContainerStatus } from '../proto/apiserveretcd';
export interface PodStatus {
  name : string;
  conditions : Conditions;
  containerStatuses : ContainerStatus[];
}

export class Conditions {
  PodScheduled : boolean;
  Initialized : boolean;
}

export class ContainerInfo {
  name: string;
  image: string;
}

export class ContainerIdInfo {
  id : string;
  metadata : ContainerInfo;
}

export interface DeploymentMetadata {
  name : string;
  replicasetid : string;
  strategyType : string;
}

export interface ReplicasetMetadata {
  name : string;
  replicas : number;
  matchlabel : Label[];
  podidlist : string[];
  podtemplate : PodTemplate;
}

export interface PodTemplate {
  name : string;
  containerlist : ContainerInfo[];
}

export class CreatePodDto {
  podName: string;
  podLabels : Label[];
  containerInfolist: ContainerInfo[];
}

export class Label {
  key: string;
  value: string;
}

export class CreateReplicasetDto {
  replicasetName: string;
  matchLabels : Label[];
  replicas: number;
  podInfo : CreatePodDto;
}