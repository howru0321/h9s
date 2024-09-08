# h9s Worker Node

## Overview

The worker node is a crucial component of the h9s (howbernetes) system, our simplified Kubernetes-like orchestration platform. It's responsible for running and managing containers as instructed by the control plane.

The main component of the worker node is `howbelet`, which is implemented in Go.

## Prerequisites

- Go 1.16 or later
- Docker
- Access to the h9s control plane

## Getting Started

1. Clone the repository (if you haven't already):
   ```
   git clone https://github.com/your-username/h9s.git
   cd h9s/worker-node/howbelet
   ```

2. Install dependencies:
   ```
   go mod tidy
   ```

## Configuration

The howbelet uses environment variables for configuration. Set the following variables before running:

- `NODE_NAME`: The name of this worker node (e.g., "node1")
- `API_SERVER_HOST`: The hostname or IP address of the h9s API server
- `API_SERVER_PORT`: The port number of the h9s API server

Example (PowerShell):
```powershell
$env:NODE_NAME = "node1"
$env:API_SERVER_HOST = "127.0.0.1"
$env:API_SERVER_PORT = "8081"
```

Example (Bash):
```bash
export NODE_NAME=node1
export API_SERVER_HOST=127.0.0.1
export API_SERVER_PORT=8081
```

## Running the Worker Node

After setting the environment variables, run the howbelet:

```
go run main.go
```

## Verifying Operation

When running correctly, the howbelet will:

1. Connect to the API server
2. Start watching for pod assignments
3. Periodically send node status updates
4. Manage containers(Create, Delete) as instructed by the control plane
