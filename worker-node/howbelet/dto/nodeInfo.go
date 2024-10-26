package dto

import (
	"fmt"
	"net"
	"os/exec"
	"runtime"
	"strings"
	"time"

	"github.com/shirou/gopsutil/cpu"
	"github.com/shirou/gopsutil/mem"
)

type Node struct {
	Kind     string   `json:"kind"`
	Metadata Metadata `json:"metadata"`
	Spec     Spec     `json:"spec"`
	Status   Status   `json:"status"`
}

type Metadata struct {
	Name string `json:"name"`
	UID  string `json:"uid"`
}

type Spec struct {
	PodCIDR string `json:"podCIDR"`
}

type Status struct {
	Capacity    ResourceList `json:"capacity"`
	Allocatable ResourceList `json:"allocatable"`
	Conditions  []Condition  `json:"conditions"`
	Addresses   []Address    `json:"addresses"`
	NodeInfo    NodeInfo     `json:"nodeInfo"`
	Images      []Image      `json:"images"`
}

type ResourceList map[string]string

type Condition struct {
	Type    string `json:"type"`
	Status  string `json:"status"`
	Reason  string `json:"reason"`
	Message string `json:"message"`
}

type Address struct {
	Type    string `json:"type"`
	Address string `json:"address"`
}

type NodeInfo struct {
	ContainerRuntimeVersion string `json:"containerRuntimeVersion"`
	OperatingSystem         string `json:"operatingSystem"`
	Architecture            string `json:"architecture"`
}

type Image struct {
	Names     []string `json:"names"`
	SizeBytes int64    `json:"sizeBytes"`
}

func GetUpdateNodeStatus(name string) (*Node, error) {
	cpuCount, err := cpu.Counts(true)
	if err != nil {
		return nil, fmt.Errorf("Fail to get CPU info: %v", err)
	}
	allocatablecpuCount, err := getAllocatableMilliCPU()

	vmStat, err := mem.VirtualMemory()
	if err != nil {
		return nil, fmt.Errorf("Fail to get Memory info: %v", err)
	}

	nodeIP, err := getOutboundIP()
	if err != nil {
		return nil, fmt.Errorf("Fail to get Node IP info: %v", err)
	}

	containerRuntime, containerVersion, err := getContainerRuntimeInfo()
	if err != nil {
		return nil, fmt.Errorf("Fail to get container runtime: %v", err)
	}

	return &Node{
		Kind: "Node",
		Metadata: Metadata{
			Name: name,
		},
		Spec: Spec{
			PodCIDR: "10.244.1.0/24",
		},
		Status: Status{
			Capacity: ResourceList{
				"cpu":    fmt.Sprintf("%dm", cpuCount*1000),
				"memory": fmt.Sprintf("%.2fMi", float64(vmStat.Total)/(1024*1024)),
			},
			Allocatable: ResourceList{
				"cpu":    fmt.Sprintf("%dm", allocatablecpuCount),
				"memory": fmt.Sprintf("%.2fMi", float64(vmStat.Available)/(1024*1024)),
			},
			Conditions: []Condition{
				{
					Type:    "Ready",
					Status:  "True",
					Reason:  "KubeletReady",
					Message: "kubelet is posting ready status",
				},
			},
			Addresses: []Address{
				{Type: "InternalIP", Address: nodeIP.String()},
				{Type: "Hostname", Address: name},
			},
			NodeInfo: NodeInfo{
				OperatingSystem:         runtime.GOOS,
				Architecture:            runtime.GOARCH,
				ContainerRuntimeVersion: fmt.Sprintf("%s://%s", containerRuntime, containerVersion),
			},
		},
	}, nil
}

func getOutboundIP() (net.IP, error) {
	conn, err := net.Dial("udp", "8.8.8.8:80")
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	localAddr := conn.LocalAddr().(*net.UDPAddr)
	return localAddr.IP, nil
}

func getContainerRuntimeInfo() (string, string, error) {
	out, err := exec.Command("docker", "version", "--format", "{{.Server.Version}}").Output()
	if err == nil {
		return "docker", strings.TrimSpace(string(out)), nil
	}

	return "", "", fmt.Errorf("Cannot get container runtime info: %v", err)
}

func getAllocatableMilliCPU() (int64, error) {
	cpuPercent, err := cpu.Percent(time.Second, true)
	if err != nil {
		return 0, fmt.Errorf("Fail to get CPU percent: %v", err)
	}

	if len(cpuPercent) == 0 {
		return 0, fmt.Errorf("Fail to get CPU percent: %v", err)

	var totalAllocatableMilliCPU int64 = 0

	for _, corePercent := range cpuPercent {
		allocatableMilliCPU := int64((100 - corePercent) * 10)
		totalAllocatableMilliCPU += allocatableMilliCPU
	}

	return totalAllocatableMilliCPU, nil
}
