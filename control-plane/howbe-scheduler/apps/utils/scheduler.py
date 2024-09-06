from apps.dataclass.node_status import NodeStatusDTO
from apps.dataclass.pod_status import PodStatusDTO
from typing import Dict, List, Optional

from apps.cache import node_status_cache
from apps.dataclass.node_status import NodeStatusDTO
from apps.dataclass.pod_status import PodStatusDTO
from typing import Dict, List
from apps.cache import node_status_cache

def filtering_nodes(pod: PodStatusDTO) -> Dict[str, NodeStatusDTO]:
    total_request_cpu, total_request_memory = pod.get_total_requested_resources()
    print(f"Total requested CPU: {total_request_cpu}, Total requested memory: {total_request_memory}")
    nodes: Dict[str, NodeStatusDTO] = node_status_cache.getAll()
    filtered_nodes: Dict[str, NodeStatusDTO] = {}
    count=0
    for node_uid, node in nodes.items():
        print(f"Node {count} has {node.status.allocatable.cpu} CPU and {node.status.allocatable.memory} memory")
        count+=1
        node_allocatable_cpu = int(node.status.allocatable.cpu[:-1])  # Extract the numeric value of CPU
        node_allocatable_memory = float(node.status.allocatable.memory[:-2])  # Extract the numeric value of memory

        if node_allocatable_cpu >= total_request_cpu and node_allocatable_memory >= total_request_memory:
            filtered_nodes[node_uid] = node
    return filtered_nodes

def scoring_nodes(pod: PodStatusDTO, nodes: Dict[str, NodeStatusDTO]) -> Dict[str, float]:
    scores: Dict[str, float] = {}
    total_request_cpu, total_request_memory = pod.get_total_requested_resources()

    for node_uid, node in nodes.items():
        node_allocatable_cpu = int(node.status.allocatable.cpu[:-1])
        node_allocatable_memory = float(node.status.allocatable.memory[:-2])

        cpu_utilization = total_request_cpu / node_allocatable_cpu if node_allocatable_cpu > 0 else 1
        memory_utilization = total_request_memory / node_allocatable_memory if node_allocatable_memory > 0 else 1

        cpu_score = 1 - cpu_utilization
        memory_score = 1 - memory_utilization
        
        final_score = (cpu_score + memory_score) / 2
        
        print(f"Node {node_uid} has a final score of {final_score}")

        scores[node_uid] = final_score

    return scores

def schedule_pod(pod: PodStatusDTO) -> Optional[str]:
    """Schedule a pod on the best-fit node."""
    filtered_nodes : Dict[str, NodeStatusDTO] = filtering_nodes(pod)
    if not filtered_nodes:
        return None
    
    scored_nodes  : Dict[str, NodeStatusDTO] = scoring_nodes(pod, filtered_nodes)
    best_node_uid = max(scored_nodes, key=lambda k: scored_nodes[k])
    print(f"Scheduling pod {pod.metadata.name} on node {best_node_uid}")
    return filtered_nodes[best_node_uid].metadata.name