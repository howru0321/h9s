package utils

import (
	"fmt"
	docker "github.com/fsouza/go-dockerclient"
	"io"
	"log"
	"net/http"
	"strings"
	"sync"
)

var DockerManager_ *DockerManager

func init() {
	var err error
	DockerManager_, err = NewDockerManager()
	if err != nil {
		log.Fatalf("Error initializing DockerManager: %v", err)
	}
}

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

func (m *DockerManager) StartContainer(containerName string, imageName string, labels map[string]string) error {
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
			Image:  imageName,
			Labels: labels,
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


// RemoveAllPodContainers removes all containers from a given list of container IDs
func (m *DockerManager) RemoveAllPodContainers(containerIDs []string) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	if len(containerIDs) == 0 {
		return fmt.Errorf("no containers provided")
	}

	var errs []string

	for _, containerID := range containerIDs {
		err := m.removeContainer(containerID)
		if err != nil {
			errs = append(errs, fmt.Sprintf("failed to remove container %s: %v", containerID, err))
		}
	}

	if len(errs) > 0 {
		return fmt.Errorf("errors occurred while removing containers: %s", strings.Join(errs, "; "))
	}

	return nil
}

// removeContainer removes a single container by its ID
func (m *DockerManager) removeContainer(containerID string) error {
	// Find the container by ID
	container, err := m.client.InspectContainerWithOptions(docker.InspectContainerOptions{
		ID: containerID,
	})
	if err != nil {
		if _, ok := err.(*docker.NoSuchContainer); ok {
			return fmt.Errorf("container not found: %s", containerID)
		}
		return fmt.Errorf("failed to inspect container: %w", err)
	}

	// Stop the container if it's running
	if container.State.Running {
		timeout := uint(30) // 30 seconds timeout
		if err := m.client.StopContainer(container.ID, timeout); err != nil {
			return fmt.Errorf("failed to stop container: %w", err)
		}
		fmt.Printf("Container %s stopped.\n", containerID)
	}

	// Remove the container
	removeOptions := docker.RemoveContainerOptions{
		ID:            container.ID,
		RemoveVolumes: true,
		Force:         true,
	}

	if err := m.client.RemoveContainer(removeOptions); err != nil {
		return fmt.Errorf("failed to remove container: %w", err)
	}

	fmt.Printf("Container %s removed successfully!\n", containerID)
	return nil
}

func (m *DockerManager) GetContainersByLabel(labelKey, labelValue string) ([]docker.APIContainers, error) {
	// Define filters for listing containers based on the label
	filters := map[string][]string{
		"label": {fmt.Sprintf("%s=%s", labelKey, labelValue)},
	}

	// Set the options for listing containers, passing the filters
	listOptions := docker.ListContainersOptions{
		All:     true, // Include stopped containers
		Filters: filters,
	}

	// Get the list of containers that match the filter
	containers, err := m.client.ListContainers(listOptions)
	if err != nil {
		return nil, fmt.Errorf("failed to list containers: %w", err)
	}

	return containers, nil
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
