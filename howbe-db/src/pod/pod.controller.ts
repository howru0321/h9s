import { Controller } from '@nestjs/common';
import { PodRequest, PodResponse, ApiserverEtcdService} from '../proto/apiserveretcd';
import { GrpcMethod, GrpcStreamMethod } from '@nestjs/microservices';
import { PodService } from './pod.service'

@Controller()
export class PodController implements ApiserverEtcdService {
    constructor(private readonly podService: PodService) {}

    @GrpcMethod('ApiserverEtcdService', 'CreatePod')
    CreatePod(request: PodRequest): Promise<PodResponse> {
      return this.podService.CreatePod(request);
    }
}
