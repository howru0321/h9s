import { Controller, UseInterceptors } from '@nestjs/common';
import { HelloRequest, HelloReply, HelloWorldService } from '../proto/hello';
import { GrpcMethod, GrpcStreamMethod } from '@nestjs/microservices';
import { Observable } from 'rxjs';
import { HelloService } from './hello.service'


@Controller()
export class HelloController implements HelloWorldService  {
    constructor(private readonly helloService: HelloService) {}

    @GrpcMethod('HelloWorldService', 'SayHello')
    SayHello(request: HelloRequest): Promise<HelloReply> {
      return this.helloService.SayHello(request);
    }
  
    @GrpcStreamMethod('HelloWorldService', 'SayHelloClientStream')
    SayHelloClientStream(request: Observable<HelloRequest>): Promise<HelloReply> {
        return this.helloService.SayHelloClientStream(request);
    }

    @GrpcMethod('HelloWorldService', 'SayHelloServerStream')
    SayHelloServerStream(request: HelloRequest): Observable<HelloReply> {
        return this.helloService.SayHelloServerStream(request);
    }
  
    @GrpcStreamMethod('HelloWorldService', 'SayHelloBidirectionalStream')
    SayHelloBidirectionalStream(request: Observable<HelloRequest>): Observable<HelloReply> {
        return this.helloService.SayHelloBidirectionalStream(request);
    }
}
