import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Pod } from '../entities/pod.entity';
import { PodController } from './pod.controller';
import { PodService } from './pod.service';

@Module({
    imports: [
        TypeOrmModule.forFeature([Pod], 'podConnection'),
    ],
    controllers: [PodController],
    providers: [
        PodService,
    ]
})
export class PodModule {}
