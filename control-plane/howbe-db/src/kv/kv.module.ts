import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ObjectStatusKV } from '../entities/objectkv.entity';
import { Key } from '../entities/key.entity';
import { KvController } from './kv.controller';
import { KvService } from './kv.service';
import { DbeventModule } from 'src/dbevent/dbevent.module';

@Module({
  imports: [
    TypeOrmModule.forFeature([ObjectStatusKV], 'objectkvConnection'),
    TypeOrmModule.forFeature([Key], 'keyConnection'),
    DbeventModule,
  ],
  controllers: [
    KvController,
  ],
  providers: [
    KvService,
  ]
})
export class KvModule {}
