package main

import (
	"fmt"
	"github.com/fsouza/go-dockerclient"
	"io"
	"log"
	"net/http"
	"sync"
	"time"
)

type DockerManager struct {
	client *docker.Client
	mu     sync.Mutex
}

func NewDockerManager() (*DockerManager, error) {
	client, err := docker.NewClientFromEnv()
	if err != nil {
		return nil, fmt.Errorf("failed to create Docker client: %w", err)
	}
	return &DockerManager{
		client: client,
	}, nil
}

func (m *DockerManager) StartContainer(containerName string, imageName string) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	pullOptions := docker.PullImageOptions{
		Repository: imageName,
		Tag:        "latest",
	}
	authOptions := docker.AuthConfiguration{}
	if err := m.client.PullImage(pullOptions, authOptions); err != nil {
		return fmt.Errorf("failed to pull image: %w", err)
	}

	fmt.Printf("Successfully pulled image: %s\n", imageName)

	createOptions := docker.CreateContainerOptions{
		Name: containerName,
		Config: &docker.Config{
			Image: imageName,
		},
	}

	container, err := m.client.CreateContainer(createOptions)
	if err != nil {
		return fmt.Errorf("failed to create container: %w", err)
	}

	startOptions := docker.HostConfig{
		PortBindings: map[docker.Port][]docker.PortBinding{
			"80/tcp": {{HostPort: "8080"}},
		},
	}

	if err := m.client.StartContainer(container.ID, &startOptions); err != nil {
		return fmt.Errorf("failed to start container: %w", err)
	}

	fmt.Printf("Container %s started successfully!\n", containerName)
	return nil
}

func sendRequest(url string) {
	resp, err := http.Get(url)
	if err != nil {
		fmt.Printf("Failed to send request: %v\n", err)
		return
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		fmt.Printf("Failed to read response body: %v\n", err)
		return
	}

	fmt.Printf("Response status: %s\n", resp.Status)
	fmt.Printf("Response body: %s\n", string(body))

}

func main() {
	manager, err := NewDockerManager()
	if err != nil {
		log.Fatalf("Error initializing Docker manager: %s", err)
	}

	containerName := "my-container"
	imageName := "nginx"

	if err := manager.StartContainer(containerName, imageName); err != nil {
		log.Fatalf("Error: %s", err)
	}
	url := "http://localhost:8080"

	ticker := time.NewTicker(1 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-ticker.C:
			sendRequest(url)
		}
	}
}
