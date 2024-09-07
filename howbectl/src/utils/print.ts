import { PodStatusDTO } from '../interfaces/pod_status.interface';

export function isValidJSON(str: string): boolean {
  try {
    JSON.parse(str);
    return true;
  } catch (e) {
    return false;
  }
}

export function printPodStatus(pods: PodStatusDTO[]) {
    console.log("NAME                                READY   STATUS    RESTARTS   AGE");
    
    pods.forEach(pod => {
      const name = pod.metadata.name.padEnd(36);
      const ready = getReadyStatus(pod).padEnd(8);
      const status = (pod.status?.phase || 'Unknown').padEnd(9);
      const restarts = getRestarts(pod).toString().padEnd(10);
      const age = getAge(pod);
  
      console.log(`${name} ${ready} ${status} ${restarts} ${age}`);
    });
  }
  
  function getReadyStatus(pod: PodStatusDTO): string {
    if (!pod.status?.containerStatuses) return '0/0';
    const ready = pod.status.containerStatuses.length;
    const total = pod?.spec?.containers?.length;
    return `${ready}/${total}`;
  }
  
  function getRestarts(pod: PodStatusDTO): number {
    if (!pod.status?.containerStatuses) return 0;
    return pod.status.containerStatuses.reduce((sum, cs) => sum + (cs.state === 'Running' ? 0 : 1), 0);
  }
  
  function getAge(pod: PodStatusDTO): string {
    // This is a placeholder. In a real scenario, you'd calculate the age based on creation timestamp
    return '1d';  // Example age
  }