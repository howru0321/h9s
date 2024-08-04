import { Injectable } from '@nestjs/common';
import { PodRequest, PodResponse, ApiserverEtcdService, ContainerMetadata } from '../proto/apiserveretcd';
import { Repository } from 'typeorm';
import { InjectRepository } from '@nestjs/typeorm';
import { Pod } from '../entities/pod.entity';
import { PodMetadata } from '../interfaces/entity.interface'
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

    async CreatePod(request: PodRequest): Promise<PodResponse> {
        // const response: PodResponse = { 
        //     podId: "podIdhow",
        //     message: `Hello, ${request.name}, ${request.containers[0].name}, ${request.containers[1].image}`
        // };
        // return Promise.resolve(response);
        const podName : string = request.name;
        const containers : ContainerMetadata[] = request.containers;
        try{
            // const id : string = null;
            // const containers : ContainerMetadata[] = request.containers;
            // for(const container of request.containers){
            //     let containerMetadata : ContainerMetadatawithId;
            //     containerMetadata.id=null;
            //     containerMetadata.name=container.name;
            //     containerMetadata.image=container.image;

            //     containers.push(containerMetadata);
            // }
            
            const value : PodMetadata = {
                name : podName,
                bind : false,
                containers : containers,
            }
            const newUUID : string = await generateUUID([]);
            const podId : string = podName + newUUID;
            const pod = this.podRepository.create({ key : podId, value : value });
            await this.podRepository.save(pod);

            const response: PodResponse = { 
                podId: podId,
                message: `Create Pod Successfully!`
            };
            return response;
        } catch (error) {
            console.error(`Error creating pod ${podName}:`, error);
            throw new Error(`Could not create pod ${podName}`);
            }
    }
}
