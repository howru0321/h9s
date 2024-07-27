package main

import (
	"context"
	pb "howbelet/proto"
	"io"
	"log"
	"strconv"
	"time"
)

func unaryExample(c pb.HelloWorldServiceClient) {
	ctx, cancel := context.WithTimeout(context.Background(), time.Second)
	defer cancel()
	const name string = "unaryExample"
	r, err := c.SayHello(ctx, &pb.HelloRequest{Name: name})
	if err != nil {
		log.Fatalf("could not greet: %v", err)
	}
	log.Printf("Greeting: %s", r.GetMessage())
}

func clientStreamExample(c pb.HelloWorldServiceClient) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	stream, err := c.SayHelloClientStream(ctx)
	if err != nil {
		log.Fatalf("could not create stream: %v", err)
	}

	const name string = "clientStreamExample"
	for i := 0; i < 5; i++ {
		requestName := name + strconv.Itoa(i)
		if err := stream.Send(&pb.HelloRequest{Name: requestName}); err != nil {
			log.Fatalf("could not send request: %v", err)
		}
		time.Sleep(time.Millisecond * 500)
	}

	reply, err := stream.CloseAndRecv()
	if err != nil {
		log.Fatalf("could not receive reply: %v", err)
	}
	log.Printf("Client Stream Greeting: %s", reply.GetMessage())
}

func serverStreamExample(c pb.HelloWorldServiceClient) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	const name string = "serverStreamExample"
	stream, err := c.SayHelloServerStream(ctx, &pb.HelloRequest{Name: name})
	if err != nil {
		log.Fatalf("could not create stream: %v", err)
	}

	for {
		reply, err := stream.Recv()
		if err == io.EOF {
			break
		}
		if err != nil {
			log.Fatalf("could not receive: %v", err)
		}
		log.Printf("Server Stream Greeting: %s", reply.GetMessage())
	}
}

func bidirectionalStreamExample(c pb.HelloWorldServiceClient) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	stream, err := c.SayHelloBidirectionalStream(ctx)
	if err != nil {
		log.Fatalf("could not create stream: %v", err)
	}

	waitc := make(chan struct{})
	go func() {
		for {
			reply, err := stream.Recv()
			if err == io.EOF {
				close(waitc)
				return
			}
			if err != nil {
				log.Fatalf("could not receive: %v", err)
			}
			log.Printf("Bidirectional Stream Greeting: %s", reply.GetMessage())
		}
	}()

	const name string = "bidirectionalStreamExample"
	for i := 0; i < 5; i++ {
		requestName := name + strconv.Itoa(i)
		if err := stream.Send(&pb.HelloRequest{Name: requestName}); err != nil {
			log.Fatalf("could not send request: %v", err)
		}
		time.Sleep(time.Millisecond * 500)
	}
	stream.CloseSend()
	<-waitc
}
