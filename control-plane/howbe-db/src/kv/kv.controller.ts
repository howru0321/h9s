import { Controller } from '@nestjs/common';
import { GrpcMethod, GrpcStreamMethod } from '@nestjs/microservices';
import { KvService } from './kv.service'
import { DeleteRangeRequest, DeleteRangeResponse, PutRequest, PutResponse, RangeRequest, RangeResponse } from 'src/proto/rpc';

@Controller('kv')
export class KvController {
    constructor(private readonly kvService: KvService) {}

    @GrpcMethod('KV', 'Range')
    async Range(data: RangeRequest): Promise<RangeResponse> {
        return this.kvService.Range(data);
    }

    @GrpcMethod('KV', 'Put')
    async Put(data: PutRequest): Promise<PutResponse> {
        return this.kvService.Put(data);
    }

    @GrpcMethod('KV', 'DeleteRange')
    async DeleteRange(data: DeleteRangeRequest): Promise<DeleteRangeResponse> {
        return this.kvService.DeleteRange(data);
    }
}
