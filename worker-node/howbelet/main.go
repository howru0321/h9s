package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"github.com/r3labs/sse/v2"
	dto "howbelet/dto"
	"howbelet/utils"
	"io"
	"log"
	"net/http"
	"os"
	"strings"
	"sync"
	"time"
)

var (
	nodeName string
	hostname string
	port     string
)

func init() {
	nodeName = getEnv("NODE_NAME", "node1")
	hostname = getEnv("API_SERVER_HOST", "localhost")
	port = getEnv("API_SERVER_PORT", "8081")
}
func getEnv(key, fallback string) string {
	if value, exists := os.LookupEnv(key); exists {
		return value
	}
	return fallback
}

func sendNodeInfo(node_name string, create bool) error {
	UpdateNodeStatus, err := dto.GetUpdateNodeStatus(node_name)
	if err != nil {
		return fmt.Errorf("failed to get node status: %v", err)
	}

	jsonData, err := json.Marshal(UpdateNodeStatus)
	if err != nil {
		return fmt.Errorf("error marshaling NodeInfo: %v", err)
	}

	var apiUrl string
	if create {
		apiUrl = fmt.Sprintf("http://%s:%s/api/v1/api/v1/nodes", hostname, port)
	} else {
		apiUrl = fmt.Sprintf("http://%s:%s/api/v1/nodes/%s", hostname, port, node_name)
	}

	requestStatus(jsonData, apiUrl)

	return nil
}

func sendPodInfo() error {
	// Get all pod names that have a true value in PodList
	podNames := utils.GetTrueElements()

	for _, podName := range podNames {
		// For simplicity, using podName as UID in this example. You can replace this with actual UID.
		// Call updatePodStatusDTO to get the PodStatusDTO for each pod
		podStatus, err := dto.UpdatePodStatusDTO(podName)
		if err != nil {
			// Log the error but continue processing the next pod
			log.Printf("Error updating pod status for pod %s: %v", podName, err)
			continue
		}

		apiUrl := fmt.Sprintf("http://%s:%s/api/v1/pods/%s", hostname, port, podName)

		jsonData, err := json.Marshal(podStatus)
		if err != nil {
			return fmt.Errorf("error marshaling NodeInfo: %v", err)
		}

		requestStatus(jsonData, apiUrl)

	}

	return nil
}

func requestStatus(jsonData []byte, apiUrl string) error {
	client := &http.Client{
		Timeout: 10 * time.Second,
	}

	req, err := http.NewRequest("POST", apiUrl, bytes.NewBuffer(jsonData))
	if err != nil {
		return fmt.Errorf("error creating request: %v", err)
	}
	req.Header.Set("Content-Type", "application/json")

	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("error sending POST request: %v", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("error reading response body: %v", err)
	}

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("received non-OK response: %s, body: %s", resp.Status, string(body))
	}

	return nil
}

// ExtractContainerIDs extracts all container IDs from a PodStatusDTO
func ExtractContainerIDs(pod *dto.PodStatusDTO) []string {
	var containerIDs []string
	for _, containerStatus := range pod.Status.ContainerStatuses {
		if containerStatus.ContainerID != "" {
			// Docker container IDs usually have a prefix like "docker://". We need to remove it.
			containerID := strings.TrimPrefix(containerStatus.ContainerID, "docker://")
			containerIDs = append(containerIDs, containerID)
		}
	}
	return containerIDs
}

func watchPods(nodeName string) {
	fmt.Printf("watchPods")

	apiUrl := fmt.Sprintf("http://%s:%s/api/v1/pods?watch=true&fieldSelector=spec.nodeName=%s", hostname, port, nodeName)
	client := sse.NewClient(apiUrl)

	events := make(chan *sse.Event)
	err := client.SubscribeChan("messages", events)
	if err != nil {
		log.Fatal(err)
	}
	if err != nil {
		// Handle error
		fmt.Printf("Error creating DockerManager: %v\n", err)
		return
	}
	for event := range events {
		podStatus, prePodStatus, err := dto.PodStatusDTOFromJSON(event.Data)
		if err != nil {
			fmt.Printf("Error parsing JSON: %v\n", err)
			return
		}

		if string(event.Event) == "MODIFIED" {
			if podStatus.Status.Phase == "Pending" {
				pod_uid := podStatus.Metadata.UID
				pod_name := podStatus.Metadata.Name
				utils.PodList[pod_name] = true
				labels := map[string]string{
					"pod_uid":  pod_uid,
					"pod_name": pod_name,
				}
				for _, value := range podStatus.Spec.Containers {
					container_name := value.Name
					image_name := value.Image
					err = utils.DockerManager_.StartContainer(container_name, image_name, labels)
					if err != nil {
						// Handle error
						fmt.Printf("Error starting container: %v\n", err)
						return
					}
				}
			}
		} else if string(event.Event) == "DELETED" {
			containerIDs := ExtractContainerIDs(prePodStatus)

			// Then, remove all the containers
			err := utils.DockerManager_.RemoveAllPodContainers(containerIDs)
			if err != nil {
				log.Fatalf("Failed to remove pod containers: %v", err)
			}
		}

	}
}

func main() {

	var wg sync.WaitGroup
	wg.Add(3)

	go func() {
		defer wg.Done()
		watchPods(nodeName)
	}()

	go func() {
		defer wg.Done()
		ticker := time.NewTicker(10 * time.Second)
		defer ticker.Stop()
		sendNodeInfo(nodeName, true)
		for range ticker.C {
			sendNodeInfo(nodeName, false)
		}
	}()

	go func() {
		defer wg.Done()
		ticker := time.NewTicker(10 * time.Second)
		defer ticker.Stop()

		for range ticker.C {
			sendPodInfo()
		}
	}()

	wg.Wait()
}
