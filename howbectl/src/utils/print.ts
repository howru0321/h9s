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
    console.log("NAME                                READY   STATUS");
    
    pods.forEach(pod => {
      const name = pod.metadata.name.padEnd(36);
      const ready = getReadyStatus(pod).padEnd(8);
      const status = (pod.status?.phase || 'Unknown').padEnd(9);
  
      console.log(`${name} ${ready} ${status}`);
    });
  }
  
  function getReadyStatus(pod: PodStatusDTO): string {
    if (!pod.status?.containerStatuses) return '0/0';
    const ready = pod.status.containerStatuses.length;
    const total = pod?.spec?.containers?.length;
    return `${ready}/${total}`;
  }
  