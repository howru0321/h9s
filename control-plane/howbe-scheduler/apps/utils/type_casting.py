from apps.dataclass.object_status import ObjectStatus, MetadataDTO
from apps.dataclass.pod_status import PodStatusDTO
from apps.dataclass.pod_status import SpecDTO as PSpecDTO, ContainerDTO as PContainerDTO, ResourceDTO as PResourceDTO, ResourceRequirementsDTO as PResourceRequirementsDTO, ConditionDTO as PConditionDTO, RunningStateDTO as PRunningStateDTO, WaitingStateDTO as PWaitingStateDTO, TerminatedStateDTO as PTerminatedStateDTO, ContainerStatusDTO as PContainerStatusDTO, StatusDTO as PStatusDTO
from apps.dataclass.node_status import NodeStatusDTO
from apps.dataclass.node_status import SpecDTO as NSpecDTO, ConditionDTO as NConditionDTO, AddressDTO as NAddressDTO, NodeInfoDTO as NNodeInfoDTO, ImageDTO as NImageDTO, StatusDTO as NStatusDTO
import json
from typing import Dict, Any

def parse_sse_message(msg):
    print("event:", msg.event)
    if msg.data:
        try:
            return json.loads(msg.data)
        except json.JSONDecodeError:
            print(f"Failed to parse JSON: {msg.data}")
    return None

def parse_json_to_object_status(data: Dict[str, Any]) -> ObjectStatus:
    try:
        object_data = data['value']['object']
        kind = object_data['kind']
        metadata = MetadataDTO(**object_data['metadata'])
    except KeyError as e:
        raise ValueError(f"Missing required field: {e}")
    
    if kind == 'Pod':
        spec_data = object_data['spec']
        #containers = [PContainerDTO(**container) for container in spec_data.get('containers', [])]
        #spec = PSpecDTO(nodeName=spec_data.get('nodeName'), containers=containers)
        containers = []
        for container in spec_data.get('containers', []):
            resources = None
            if 'resources' in container:
                limits = PResourceDTO(**container['resources'].get('limits', {}))
                requests = PResourceDTO(**container['resources'].get('requests', {}))
                resources = PResourceRequirementsDTO(limits=limits, requests=requests)
            containers.append(PContainerDTO(
                image=container['image'],
                name=container['name'],
                resources=resources
            ))
            spec = PSpecDTO(nodeName=spec_data.get('nodeName'), containers=containers)
        
        status_data = object_data.get('status', {})
        conditions = [PConditionDTO(**condition) for condition in status_data.get('conditions', [])]
        
        container_statuses = []
        for container_status in status_data.get('containerStatuses', []):
            state = container_status.get('state', {})
            state_instance = None
            if 'running' in state:
                state_instance = {'running': PRunningStateDTO(**state['running'])}
            elif 'waiting' in state:
                state_instance = {'waiting': PWaitingStateDTO(**state['waiting'])}
            elif 'terminated' in state:
                state_instance = {'terminated': PTerminatedStateDTO(**state['terminated'])}
            
            container_statuses.append(PContainerStatusDTO(
                image=container_status['image'],
                imageId=container_status['imageId'],
                name=container_status['name'],
                containerId=container_status['containerId'],
                state=state_instance
            ))
        
        status = PStatusDTO(
            phase=status_data.get('phase'),
            nodeName=status_data.get('nodeName'),
            hostIp=status_data.get('hostIp'),
            podIp=status_data.get('podIp'),
            conditions=conditions,
            containerStatuses=container_statuses
        )
        
        return PodStatusDTO(kind=kind, metadata=metadata, spec=spec, status=status)
    elif kind == 'Node':
        spec_data = object_data['spec']
        spec = NSpecDTO(podCIDR=spec_data.get('podCIDR'))
        
        status_data = object_data['status']
        conditions = [NConditionDTO(**condition) for condition in status_data.get('conditions', [])]
        addresses = [NAddressDTO(**address) for address in status_data.get('addresses', [])]
        node_info = NNodeInfoDTO(**status_data['nodeInfo']) if 'nodeInfo' in status_data else None
        images = [NImageDTO(**image) for image in status_data.get('images', [])]
        
        status = NStatusDTO(
            capacity=status_data.get('capacity'),
            allocatable=status_data.get('allocatable'),
            conditions=conditions,
            addresses=addresses,
            nodeInfo=node_info,
            images=images
        )
        
        return NodeStatusDTO(kind=kind, metadata=metadata, spec=spec, status=status)
    else:
        raise ValueError(f"Unsupported kind: {kind}")