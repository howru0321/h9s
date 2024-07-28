import { Injectable } from '@nestjs/common';
import { Observable, from, concat } from 'rxjs';
import { delay, map, concatMap } from 'rxjs/operators';
import { HelloRequest, HelloReply, HelloWorldService } from '../proto/hello';

@Injectable()
export class HelloService implements HelloWorldService {
  SayHello(request: HelloRequest): Promise<HelloReply> {
    const reply: HelloReply = { message: `Hello, ${request.name}` };
    return Promise.resolve(reply);
  }

  SayHelloClientStream(request: Observable<HelloRequest>): Promise<HelloReply> {
    return new Promise((resolve) => {
      const names: string[] = [];
      request.subscribe({
        next(request: HelloRequest) {
          console.log(`Received request with name(ClientStream): ${request.name}`)
          names.push(request.name);
        },
        complete() {
          resolve({ message: `Hello, ${names.join(', ')}` });
        },
      });
    });
  }

  SayHelloServerStream(request: HelloRequest): Observable<HelloReply> {
    const names = ['Alice', 'Bob', 'Charlie'];
    const replies: HelloReply[] = names.map((name, index) => ({
        message: `Hello, ${name}! (from ${request.name})`
    }));

    return from(replies).pipe(
        concatMap((reply, index) =>
            from([reply]).pipe(
                delay(500 * index)
            )
        )
    );
  }

  SayHelloBidirectionalStream(request: Observable<HelloRequest>): Observable<HelloReply> {
    return new Observable((observer) => {
      request.subscribe({
        next(request: HelloRequest) {
          const reply: HelloReply = { message: `Hello, ${request.name}` };
          observer.next(reply);
        },
        complete() {
          observer.complete();
        },
      });
    });
  }
}
