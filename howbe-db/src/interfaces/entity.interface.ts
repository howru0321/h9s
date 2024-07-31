export interface PodMetadata {
  name : string;
  bind : boolean;
  containers : ContainerMetadata[];
}

export class ContainerMetadata {
  id : string;
  name : string;
  image : string;
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