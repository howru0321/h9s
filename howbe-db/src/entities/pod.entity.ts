import { Entity, Column, PrimaryColumn } from 'typeorm';
import { PodStatus } from '../interfaces/entity.interface'

@Entity()
export class Pod {
    @PrimaryColumn()
    key: string;
  
    @Column('simple-json')
    value: PodStatus;
}