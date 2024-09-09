package dto

import (
	"encoding/json"
	"fmt"
	"howbelet/utils"
	"strings"
)

type ResourceDTO struct {
	CPU    string `json:"cpu"`
	Memory string `json:"memory"`
}

type ResourceRequirementsDTO struct {
	Limits   ResourceDTO `json:"limits"`
	Requests ResourceDTO `json:"requests"`
}

type ContainerDTO struct {
	Image     string                   `json:"image"`
	Name      string                   `json:"name"`
	Resources *ResourceRequirementsDTO `json:"resources,omitempty"`
}

type SpecDTO struct {
	NodeName   string         `json:"nodeName,omitempty"`
	Containers []ContainerDTO `json:"containers"`
}

type ConditionDTO struct {
	Status string `json:"status"`
	Type   string `json:"type"`
}

type RunningStateDTO struct {
	StartedAt string `json:"startedAt,omitempty"`
}

type WaitingStateDTO struct {
	Reason  string `json:"reason,omitempty"`
	Message string `json:"message,omitempty"`
}

type TerminatedStateDTO struct {
	ExitCode    int    `json:"exitCode,omitempty"`
	Reason      string `json:"reason,omitempty"`
	StartedAt   string `json:"startedAt,omitempty"`
	FinishedAt  string `json:"finishedAt,omitempty"`
	ContainerID string `json:"containerId,omitempty"`
}

type ContainerStatusDTO struct {
	Image       string `json:"image"`
	ImageID     string `json:"imageId"`
	Name        string `json:"name"`
	ContainerID string `json:"containerId"`
	State       string `json:"state"`
}

type StatusDTO struct {
	Phase             string               `json:"phase,omitempty"`
	NodeName          string               `json:"nodeName,omitempty"`
	HostIP            string               `json:"hostIp,omitempty"`
	PodIP             string               `json:"podIp,omitempty"`
	Conditions        []ConditionDTO       `json:"conditions,omitempty"`
	ContainerStatuses []ContainerStatusDTO `json:"containerStatuses,omitempty"`
}

type MetadataDTO struct {
	Name string `json:"name"`
	UID  string `json:"uid,omitempty"`
}

type PodStatusDTO struct {
	Kind     string      `json:"kind"`
	Metadata MetadataDTO `json:"metadata"`
	Spec     SpecDTO     `json:"spec"`
	Status   StatusDTO   `json:"status,omitempty"`
}

// updatePodStatusDTO creates a PodStatusDTO with specific fields set.
func UpdatePodStatusDTO(podName string) (*PodStatusDTO, error) {
	// Step 1: Get container statuses based on the podName label
	containers, err := utils.DockerManager_.GetContainersByLabel("pod_name", podName)
	if err != nil {
		return nil, fmt.Errorf("failed to get containers by label: %w", err)
	}

	// Step 2: Create the ContainerStatuses slice for PodStatusDTO
	containerStatuses := []ContainerStatusDTO{}
	var pod_state string = "Running"
	//if len(containerStatuses) == 0 {
	//	pod_state="Pending"
	//}

	for _, container := range containers {
		// Assume we have a placeholder state in raw JSON format (for simplicity)
		// This could be parsed based on the actual state of the container if available
		containerStatus := ContainerStatusDTO{
			Image:       container.Image,
			ImageID:     "",
			Name:        container.Names[0], // Assuming Names array contains at least one element
			ContainerID: container.ID,
		}
		containerStatuses = append(containerStatuses, containerStatus)
	}

	// Step 3: Create the PodStatusDTO struct with the specified fields
	podStatus := &PodStatusDTO{
		Kind: "Pod",
		Metadata: MetadataDTO{
			Name: podName,
		},
		Status: StatusDTO{
			Phase:             pod_state,
			ContainerStatuses: containerStatuses,
		},
	}

	return podStatus, nil
}

type SSEMessage struct {
	Value struct {
		Type   string       `json:"type"`
		Object PodStatusDTO `json:"object"`
	} `json:"value"`
	PreValue PodStatusDTO `json:"pre_value"`
}

func (p *PodStatusDTO) ToJSON() ([]byte, error) {
	return json.Marshal(p)
}

func PodStatusDTOFromJSON(data []byte) (*PodStatusDTO, *PodStatusDTO, error) {
	// Strip "data: " prefix if present
	strData := string(data)
	strData = strings.TrimPrefix(strData, "data: ")

	var sseMessage SSEMessage
	err := json.Unmarshal([]byte(strData), &sseMessage)
	if err != nil {
		return nil, nil, err
	}
	return &sseMessage.Value.Object, &sseMessage.PreValue, nil
}
