import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { ConfigModule } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { objectkvDBConfig, keyDBConfig } from './typeorm.config';
import { DbeventService } from './dbevent/dbevent.service';
import { DbeventModule } from './dbevent/dbevent.module';
import { KvModule } from './kv/kv.module';
import { WatchModule } from './watch/watch.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      envFilePath: ['.env'],
      isGlobal: true,
    }),
    TypeOrmModule.forRoot(objectkvDBConfig),
    TypeOrmModule.forRoot(keyDBConfig),
    DbeventModule,
    KvModule,
    WatchModule,
  ],
  controllers: [
    AppController,
  ],
  providers: [
    AppService,
    DbeventService,
  ],
})
export class AppModule {}
