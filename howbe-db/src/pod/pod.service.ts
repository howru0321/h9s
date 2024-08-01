import { Injectable } from '@nestjs/common';
import { PodRequest, PodResponse, ApiserverEtcdService, ContainerMetadata } from '../proto/apiserveretcd';
import { Repository } from 'typeorm';
import { InjectRepository } from '@nestjs/typeorm';
import { Pod } from '../entities/pod.entity';
import { PodMetadata, ContainerMetadatawithId } from '../interfaces/entity.interface'

@Injectable()
export class PodService implements ApiserverEtcdService {
    constructor(
        @InjectRepository(Pod, 'podConnection')
        private readonly podRepository: Repository<Pod>,
    ){}

    async CreatePod(request: PodRequest): Promise<PodResponse> {
        const response: PodResponse = { 
            podId: "podIdhow",
            message: `Hello, ${request.name}, ${request.containers[0].name}, ${request.containers[1].image}`
        };
        return Promise.resolve(response);
        // try{
        //     const id : string = null;
        //     const containers : ContainerMetadatawithId[] = [];
        //     for(const container of request.containers){
        //         let containerMetadata : ContainerMetadatawithId;
        //         containerMetadata.id=null;
        //         containerMetadata.name=container.name;
        //         containerMetadata.image=container.image;

        //         containers.push(containerMetadata);
        //     }
            
        //     const value : PodMetadata = {
        //         name : request.name,
        //         bind : false,
        //         containers : containers,
        //     }
        //     const pod = this.podRepository.create({ key : id, value : value });
        //     await this.podRepository.save(pod);
        //     const response: PodResponse = { 
        //         podId: null,
        //         message: `Create Pod Successfully!`
        //     };
        //     return response;
        // } catch (error) {
        //     console.error('Error saving key-value pair:', error);
        //     throw new Error('Could not save key-value pair');
        //     }
    }
}
