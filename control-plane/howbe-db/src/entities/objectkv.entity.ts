import { Entity, Column, PrimaryColumn } from 'typeorm';
import { ObjectStatus } from '../interfaces/object_status.interface';

@Entity()
export class ObjectStatusKV {
    @PrimaryColumn()
    key: string;
  
    @Column('simple-json')
    value: ObjectStatus;
}