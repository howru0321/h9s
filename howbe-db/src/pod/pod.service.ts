import { Injectable } from '@nestjs/common';
import { PodRequest, PodResponse, ApiserverEtcdService, ContainerStatus } from '../proto/apiserveretcd';
import { Repository } from 'typeorm';
import { InjectRepository } from '@nestjs/typeorm';
import { Pod } from '../entities/pod.entity';
import { PodStatus, Conditions } from '../interfaces/entity.interface'
import { v4 as uuidv4 } from 'uuid';

async function generateUUID(existingUUIDs: string[]): Promise<string> {
    let newUUID: string;
    do {
      newUUID = await uuidv4();
    } while (existingUUIDs.includes(newUUID));
    
    return newUUID;
  }
@Injectable()
export class PodService implements ApiserverEtcdService {
    constructor(
        @InjectRepository(Pod, 'podConnection')
        private readonly podRepository: Repository<Pod>,
    ){}

    async UpdatePodStatus(request: PodRequest): Promise<PodResponse> {
        const podId : string = request.id;
        const podName : string = request.name;
        const containerStatuses : ContainerStatus[] = request.containerStatuses;
        try{
            const conditions : Conditions = {
                PodScheduled : false,
                Initialized : false,
            }
            const value : PodStatus = {
                name : podName,
                conditions : conditions,
                containerStatuses : containerStatuses,
            }
            //const newUUID : string = await generateUUID([]);
            //const podId : string = podName + newUUID;
            const pod = this.podRepository.create({ key : podId, value : value });
            await this.podRepository.save(pod);

            const response: PodResponse = { 
                podId: podId,
                message: `Update Pod Status Successfully!`
            };
            return response;
        } catch (error) {
            console.error(`Error creating pod ${podName}:`, error);
            throw new Error(`Could not create pod ${podName}`);
            }
    }
}
