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
    TypeOrmModule.forRoot(podDBConfig),
    TypeOrmModule.forRoot(replicasetDBConfig),
    TypeOrmModule.forRoot(deploymentDBConfig),
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
