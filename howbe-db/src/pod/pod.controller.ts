import { Controller } from '@nestjs/common';
import { PodRequest, PodResponse, ApiserverEtcdService} from '../proto/apiserveretcd';
import { GrpcMethod, GrpcStreamMethod } from '@nestjs/microservices';
import { PodService } from './pod.service'

@Controller()
export class PodController implements ApiserverEtcdService {
    constructor(private readonly podService: PodService) {}

    @GrpcMethod('ApiserverEtcdService', 'UpdatePodStatus')
    UpdatePodStatus(request: PodRequest): Promise<PodResponse> {
      return this.podService.UpdatePodStatus(request);
    }
}
