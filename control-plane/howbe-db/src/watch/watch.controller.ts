import { Controller } from '@nestjs/common';
import { WatchRequest, WatchResponse } from '../proto/rpc';
import { GrpcMethod, GrpcStreamMethod } from '@nestjs/microservices';
import { WatchService } from './watch.service'
import { Observable } from 'rxjs';
import { Empty } from "../proto/google/protobuf/empty";

@Controller('watch')
export class WatchController {
    constructor(private readonly watchService: WatchService) {}

    @GrpcStreamMethod('Watch', 'Watch')
    Watch(request: Observable<WatchRequest>): Observable<WatchResponse> {
        return this.watchService.Watch(request);
    }
}
