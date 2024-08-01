import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { ConfigModule } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { podDBConfig, replicasetDBConfig, deploymentDBConfig } from './typeorm.config';
import { HelloService } from './hello/hello.service';
import { HelloController } from './hello/hello.controller';
import { HelloModule } from './hello/hello.module';
import { PodController } from './pod/pod.controller';
import { PodService } from './pod/pod.service';
import { PodModule } from './pod/pod.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      envFilePath: ['.env'],
      isGlobal: true,
    }),
    TypeOrmModule.forRoot(podDBConfig),
    TypeOrmModule.forRoot(replicasetDBConfig),
    TypeOrmModule.forRoot(deploymentDBConfig),
    HelloModule,
    PodModule,
  ],
  controllers: [AppController, HelloController, PodController],
  providers: [AppService, HelloService, PodService],
})
export class AppModule {}
