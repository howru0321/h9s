import { Controller, UseInterceptors } from '@nestjs/common';
import { Observable } from 'rxjs';

import { HelloRequest, HelloReply, HelloWorldService } from '../proto/hello'
import { GrpcMethod } from '@nestjs/microservices';

@Controller()
export class HelloController implements HelloWorldService {
    @GrpcMethod('HelloWorldService', 'SayHello')
    SayHello(request: HelloRequest): Promise<HelloReply> {
        const name : string = request.name;
        const message : string = `Hello ${name}!`
        
        const reply: HelloReply = { message };
        return Promise.resolve(reply);
    }
}
