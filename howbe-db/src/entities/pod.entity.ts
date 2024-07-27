import { Entity, Column, PrimaryColumn } from 'typeorm';
import { PodMetadata } from '../interfaces/entity.interface'

@Entity()
export class Pod {
    @PrimaryColumn()
    key: string;
  
    @Column('simple-json')
    value: PodMetadata;
}