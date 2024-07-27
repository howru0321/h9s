package main

import (
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
	pb "howbelet/proto"
	"log"
)

const (
	address = "localhost:50051"
)

func main() {
	conn, err := grpc.NewClient(address, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Fatalf("did not connect: %v", err)
	}
	defer conn.Close()
	c := pb.NewHelloWorldServiceClient(conn)

	// Unary call
	unaryExample(c)

	// Client Streaming call
	clientStreamExample(c)

	// Server Streaming call
	serverStreamExample(c)

	// Bidirectional Streaming call
	bidirectionalStreamExample(c)
}
