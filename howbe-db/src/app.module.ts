import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { ConfigModule } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { podDBConfig, replicasetDBConfig, deploymentDBConfig } from './typeorm.config';

@Module({
  imports: [
    ConfigModule.forRoot({
      envFilePath: ['.env'],
      isGlobal: true,
    }),
    TypeOrmModule.forRoot(podDBConfig),//pod.sqlite 연결
    TypeOrmModule.forRoot(replicasetDBConfig),//replicaset.sqlite 연결
    TypeOrmModule.forRoot(deploymentDBConfig),//deploymentDBConfig 연결
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
