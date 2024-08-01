import { Injectable } from '@nestjs/common';
import { PodRequest, PodResponse, ApiserverEtcdService} from '../proto/apiserveretcd';

@Injectable()
export class PodService implements ApiserverEtcdService {
    CreatePod(request: PodRequest): Promise<PodResponse> {
        const response: PodResponse = { 
            podId: "podIdhow",
            message: `Hello, ${request.name}, ${request.containers[0].name}, ${request.containers[1].image}`
        };
        return Promise.resolve(response);
      }
}
