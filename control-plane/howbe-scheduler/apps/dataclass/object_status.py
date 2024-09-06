from dataclasses import dataclass
from typing import Optional, Dict
import json
from abc import abstractmethod

@dataclass
class MetadataDTO:
    name: str
    uid: Optional[str] = None

@dataclass
class ObjectStatus:
    kind: str
    metadata: MetadataDTO
    
    @abstractmethod
    def to_dict(self) -> Dict:
        pass

    def to_json(self) -> str:
        return json.dumps(self.to_dict())
    
