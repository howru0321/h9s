# h9s Control Plane

## Overview

The control plane is the brain of h9s (howbernetes), our simplified Kubernetes-like system. It's responsible for making global decisions about the cluster and detecting and responding to cluster events. The h9s control plane consists of three main components:

1. **howbe-apiserver**: This is the front-end of the control plane, exposing the h9s API. It's designed to scale horizontally and serves as the primary interface for users and other components to interact with the cluster state.

2. **howbe-scheduler**: This component watches for newly created pods with no assigned node and selects a node for them to run on. It makes scheduling decisions based on resource availability.

3. **howbe-db**: This is our simplified version of etcd in Kubernetes. It acts as the cluster's key-value store, used for all cluster data storage.

These components work together to manage the state of the cluster, schedule workloads, and maintain the desired state of the system.

## Getting Started

Follow these simple steps to set up and run the h9s control plane:

1. Clone the repository (if you haven't already):
   ```
   git clone https://github.com/your-username/h9s.git
   cd h9s/control-plane
   ```

2. Start the control plane:
   ```
   docker compose up
   ```

   This single command will start all the control plane components (howbe-db, howbe-apiserver, and howbe-scheduler) in the correct order.

## Important Notes

- The `docker-compose.yml` file is configured to handle all dependencies and start the components in the correct order automatically.
- You don't need to worry about the order of starting components or performing any additional steps.
- To stop the control plane, you can use Ctrl+C in the terminal where you ran `docker compose up`.
- If you want to run the control plane in the background, you can use:
  ```
  docker compose up -d
  ```
  To stop it when running in detached mode, use:
  ```
  docker compose down
  ```

For more detailed information about each component, refer to their respective documentation in this directory.