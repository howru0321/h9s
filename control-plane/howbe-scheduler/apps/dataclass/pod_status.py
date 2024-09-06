from dataclasses import dataclass, field, asdict
from typing import List, Dict, Optional, Union, Tuple
import json
from apps.dataclass.object_status import ObjectStatus, MetadataDTO
import re

@dataclass
class ResourceDTO:
    cpu: str
    memory: str
@dataclass
class ResourceRequirementsDTO:
    limits: ResourceDTO
    requests: ResourceDTO
@dataclass
class ContainerDTO:
    image: str
    name: str
    resources: Optional[ResourceRequirementsDTO] = None

@dataclass
class SpecDTO:
    nodeName: Optional[str] = None
    containers: List[ContainerDTO] = field(default_factory=list)

@dataclass
class ConditionDTO:
    status: str
    type: str

@dataclass
class RunningStateDTO:
    startedAt: Optional[str] = None

@dataclass
class WaitingStateDTO:
    reason: Optional[str] = None
    message: Optional[str] = None

@dataclass
class TerminatedStateDTO:
    exitCode: Optional[int] = None
    reason: Optional[str] = None
    startedAt: Optional[str] = None
    finishedAt: Optional[str] = None
    containerId: Optional[str] = None

@dataclass
class ContainerStatusDTO:
    image: str
    imageId: str
    name: str
    containerId: str
    state: Union[Dict[str, RunningStateDTO], Dict[str, WaitingStateDTO], Dict[str, TerminatedStateDTO]]

@dataclass
class StatusDTO:
    phase: Optional[str] = None
    nodeName: Optional[str] = None
    hostIp: Optional[str] = None
    podIp: Optional[str] = None
    conditions: Optional[List[ConditionDTO]] = None
    containerStatuses: Optional[List[ContainerStatusDTO]] = None

@dataclass
class PodStatusDTO(ObjectStatus):
    spec: Optional[SpecDTO]
    status: Optional[StatusDTO] = None

    def to_dict(self) -> Dict:
        return {
            "kind": self.kind,
            "metadata": asdict(self.metadata),
            "spec": asdict(self.spec),
            "status": asdict(self.status) if self.status else None
        }
    
    @classmethod
    def from_json(cls, json_str: str) -> 'PodStatusDTO':
        data = json.loads(json_str)
        metadata = MetadataDTO(**data['metadata'])

        # Parse SpecDTO
        spec_data = data['spec']
        containers = [
            ContainerDTO(
                image=c['image'],
                name=c['name'],
                resources=ResourceRequirementsDTO(
                    limits=ResourceDTO(**c['resources']['limits']) if 'limits' in c['resources'] else None,
                    requests=ResourceDTO(**c['resources']['requests']) if 'requests' in c['resources'] else None
                ) if 'resources' in c else None
            ) for c in spec_data.get('containers', [])
        ]
        spec = SpecDTO(nodeName=spec_data.get('nodeName'), containers=containers)

        # Parse StatusDTO
        status_data = data.get('status')
        if status_data:
            conditions = [ConditionDTO(**c) for c in status_data.get('conditions', [])]
            container_statuses = []
            for cs in status_data.get('containerStatuses', []):
                state = {}
                if 'running' in cs['state']:
                    state['running'] = RunningStateDTO(**cs['state']['running'])
                elif 'waiting' in cs['state']:
                    state['waiting'] = WaitingStateDTO(**cs['state']['waiting'])
                elif 'terminated' in cs['state']:
                    state['terminated'] = TerminatedStateDTO(**cs['state']['terminated'])
                container_statuses.append(ContainerStatusDTO(
                    image=cs['image'],
                    imageId=cs['imageId'],
                    name=cs['name'],
                    containerId=cs['containerId'],
                    state=state
                ))
            status = StatusDTO(
                phase=status_data.get('phase'),
                nodeName=status_data.get('nodeName'),
                hostIp=status_data.get('hostIp'),
                podIp=status_data.get('podIp'),
                conditions=conditions,
                containerStatuses=container_statuses
            )
        else:
            status = None

        return cls(
            kind=data['kind'],
            metadata=metadata,
            spec=spec,
            status=status
        )
    
    @classmethod
    def parse_cpu_value(cls, cpu_value: str) -> int:
        """Convert CPU value to millicores."""
        if cpu_value.endswith('m'):
            return int(cpu_value[:-1])
        return int(float(cpu_value) * 1000)

    @classmethod
    def parse_memory_value(cls, memory_value: str) -> float:
        """Convert memory value to megabytes."""
        units = {'Ki': 1/1024, 'Mi': 1, 'Gi': 1024, 'Ti': 1024**2}
        match = re.match(r'^(\d+)(\w+)?$', memory_value)
        if match:
            value, unit = match.groups()
            return float(value) * units.get(unit, 1/1024/1024)  # Default to bytes if no unit
        return float(memory_value) / (1024 * 1024)  # Convert bytes to MB

    def get_total_requested_resources(self) -> Tuple[int, float]:
        """Calculate total requested CPU (in millicores) and memory (in MB) for the pod."""
        total_cpu = 0
        total_memory = 0.0

        for container in self.spec.containers:
            if container.resources and container.resources.requests:
                total_cpu += self.parse_cpu_value(container.resources.requests.cpu)
                total_memory += self.parse_memory_value(container.resources.requests.memory)

        return total_cpu, total_memory