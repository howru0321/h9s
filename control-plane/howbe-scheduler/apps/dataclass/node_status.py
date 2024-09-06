from dataclasses import dataclass, field, asdict
from typing import List, Dict, Optional, Any
import json
from apps.dataclass.object_status import ObjectStatus, MetadataDTO

@dataclass
class SpecDTO:
    podCIDR: Optional[str] = None

@dataclass
class ConditionDTO:
    type: str
    status: str
    reason: str
    message: str

@dataclass
class AddressDTO:
    type: str
    address: str

@dataclass
class NodeInfoDTO:
    containerRuntimeVersion: str
    operatingSystem: str
    architecture: str

@dataclass
class ImageDTO:
    names: List[str]
    sizeBytes: int

@dataclass
class ResourceDTO:
    cpu: str = None
    memory: str = None

@dataclass
class StatusDTO:
    capacity: Optional[ResourceDTO] = None
    allocatable: Optional[ResourceDTO] = None
    conditions: List[ConditionDTO] = field(default_factory=list)
    addresses: List[AddressDTO] = field(default_factory=list)
    nodeInfo: Optional[NodeInfoDTO] = None
    images: List[ImageDTO] = field(default_factory=list)

@dataclass
class NodeStatusDTO(ObjectStatus):
    spec: SpecDTO
    status: StatusDTO

    def to_dict(self) -> Dict:
        return {
            "kind": self.kind,
            "metadata": asdict(self.metadata),
            "spec": asdict(self.spec),
            "status": asdict(self.status)
        }
    
    @classmethod
    def from_json(cls, json_str: str) -> 'NodeStatusDTO':
        data = json.loads(json_str)
        metadata = MetadataDTO(**data['metadata'])
        spec = SpecDTO(**data['spec'])
        
        status_data = data['status']
        conditions = [ConditionDTO(**c) for c in status_data.get('conditions', [])]
        addresses = [AddressDTO(**a) for a in status_data.get('addresses', [])]
        node_info = NodeInfoDTO(**status_data['nodeInfo']) if status_data.get('nodeInfo') else None
        images = [ImageDTO(**i) for i in status_data.get('images', [])]
        
        capacity_data = status_data.get('capacity')
        capacity = ResourceDTO(**capacity_data) if capacity_data else None
        
        allocatable_data = status_data.get('allocatable')
        allocatable = ResourceDTO(**allocatable_data) if allocatable_data else None
        
        status = StatusDTO(
            capacity=capacity,
            allocatable=allocatable,
            conditions=conditions,
            addresses=addresses,
            nodeInfo=node_info,
            images=images
        )
        
        return cls(
            kind=data['kind'],
            metadata=metadata,
            spec=spec,
            status=status
        )
        
