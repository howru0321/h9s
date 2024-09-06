import { Module } from '@nestjs/common';
import { WatchService } from './watch.service';
import { WatchController } from './watch.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ObjectStatusKV } from '../entities/objectkv.entity';
import { Key } from '../entities/key.entity';

@Module({
    imports : [
        TypeOrmModule.forFeature([ObjectStatusKV], 'objectkvConnection'),
        TypeOrmModule.forFeature([Key], 'keyConnection'),
      ],
      providers: [
        WatchService,
      ],
      controllers: [
        WatchController,
      ]
})
export class WatchModule {}
