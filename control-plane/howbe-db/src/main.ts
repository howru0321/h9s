import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { ConfigService } from '@nestjs/config'
import { Transport, MicroserviceOptions } from '@nestjs/microservices';
import { join } from 'path';


async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  const configService = app.get(ConfigService);

  const port = configService.get<number>('PORT', 3000);
  await app.listen(port);

  app.connectMicroservice<MicroserviceOptions>({
    transport: Transport.GRPC,
    options: {
      package: 'etcdserverpb',
      protoPath: join(__dirname, './proto/rpc.proto'),
      url: '0.0.0.0:50054',
    },
  });

  await app.startAllMicroservices();
}
bootstrap();
