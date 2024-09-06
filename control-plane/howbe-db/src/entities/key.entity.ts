import { Entity, Column, PrimaryGeneratedColumn, Index } from 'typeorm';

@Entity()
export class Key {
    @PrimaryGeneratedColumn('uuid')
    id: string;

    @Index()
    @Column()
    resourceType: string;

    @Index()
    @Column()
    name: string;

    @Column()
    fullKey: string;

    constructor(
        resourceType: string,
        name: string,
    ) {
        this.resourceType = resourceType;
        this.name = name;
        this.fullKey = this.generateFullKey();
    }

    private generateFullKey(): string {
        let key = `/registry/${this.resourceType}`;
        key += `/${this.name}`;
        return key;
    }
}