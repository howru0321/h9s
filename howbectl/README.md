# howbectl

howbectl is a command-line interface for controlling h9s (howbernetes), a simplified Kubernetes-like container orchestration system.

## Getting Started

### Prerequisites

- Node.js (version 12 or later)
- npm (usually comes with Node.js)
- Access to an h9s cluster

### Installation

1. Clone the repository (if you haven't already):
   ```
   git clone https://github.com/your-username/h9s.git
   cd h9s/howbectl
   ```

2. Install dependencies:
   ```
   npm install
   ```

3. Build the project:
   ```
   npm run build
   ```

4. Link the package globally to use howbectl from anywhere:
   ```
   npm link
   ```

### Configuration

By default, howbectl connects to a master node at `localhost:8081`. If your h9s cluster is running elsewhere, you'll need to update the `masterNode` object in the `index.ts` file:

```typescript
const masterNode = {
  ip: "your-master-ip",
  port: "your-master-port"
};
```

After changing this, rebuild the project with `npm run build`.

### Basic Usage

Here are some basic commands to get you started with howbectl:

1. Get all pods:
   ```
   howbectl get pods
   ```

2. Get a specific pod:
   ```
   howbectl get pods <pod-name>
   ```

3. Delete a pod:
   ```
   howbectl delete pods <pod-name>
   ```

4. Create a resource from a YAML file:
   ```
   howbectl create -f <path-to-yaml-file>
   ```

### Creating a Pod

To create a pod, you'll need to provide a YAML file. Here's an example:

1. Create a file named `my-pod.yaml` with the following content:
   ```yaml
   kind: Pod
   metadata:
     name: my-nginx
   spec:
     containers:
     - name: nginx
       image: nginx:latest
   ```

2. Create the pod using howbectl:
   ```
   howbectl create -f my-pod.yaml
   ```

### Troubleshooting

If you encounter any issues:

1. Ensure your h9s cluster is running and accessible.
2. Check that the master node IP and port are correctly configured.
3. Verify that you have the necessary permissions to perform the operations.

For more detailed information on available commands and their usage, run:
```
howbectl --help
```