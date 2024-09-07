from functools import wraps
from typing import Dict, Optional
from apps.dataclass.node_status import NodeStatusDTO

class NodeStatusCache:
    def __init__(self):
        self.cache: Dict[str, NodeStatusDTO] = {}

    def get(self, uid: str) -> Optional[NodeStatusDTO]:
        return self.cache.get(uid)
    def getAll(self) -> Dict[str, NodeStatusDTO]:
        return self.cache

    def set(self, node_status: NodeStatusDTO):
        if node_status.metadata.uid:
            self.cache[node_status.metadata.uid] = node_status

    def clear(self):
        self.cache.clear()

node_status_cache = NodeStatusCache()

# def cache_node_status(func):
#     @wraps(func)
#     def wrapper(*args, **kwargs):
#         result = func(*args, **kwargs)
#         if isinstance(result, NodeStatusDTO) and result.metadata.uid:
#             node_status_cache.set(result)
#         return result
#     return wrapper

# # Example usage:
# @cache_node_status
# def get_node_status(node_id: str) -> NodeStatusDTO:
#     # This is a placeholder for your actual implementation
#     # Replace this with your logic to fetch NodeStatusDTO
#     pass

# # To use the cached version:
# def get_cached_node_status(node_id: str) -> Optional[NodeStatusDTO]:
#     cached_status = node_status_cache.get(node_id)
#     if cached_status:
#         return cached_status
#     return get_node_status(node_id)