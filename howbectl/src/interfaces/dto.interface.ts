import { ContainerMetadata } from '../proto/apiserveretcd';

export interface PodDTO {
    id : string;
    name : string;
    containerStatuses : ContainerMetadata[];
}

