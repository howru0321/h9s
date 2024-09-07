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
	"strings"
	"sync"
	"time"
)

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
		apiUrl = "http://localhost:8081/api/v1/nodes"
	} else {
		apiUrl = fmt.Sprintf("http://localhost:8081/api/v1/nodes/%s", node_name)
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

		apiUrl := "http://localhost:8081/api/v1/pods/" + podName

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

func watchPods() {
	fmt.Printf("watchPods")
	url := "http://localhost:8081/api/v1/pods?watch=true&fieldSelector=spec.nodeName=node1"
	client := sse.NewClient(url)

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
	wg.Add(2)

	go func() {
		defer wg.Done()
		watchPods()
	}()

	go func() {
		defer wg.Done()
		ticker := time.NewTicker(10 * time.Second)
		defer ticker.Stop()

		node_name := "node1"
		sendNodeInfo(node_name, true)
		for range ticker.C {
			sendNodeInfo(node_name, false)
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
