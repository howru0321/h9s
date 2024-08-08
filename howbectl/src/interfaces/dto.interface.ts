import { ContainerMetadata } from '../proto/apiserveretcd';

export interface PodDTO {
    name : string;
    containerStatuses : ContainerMetadata[];
}

