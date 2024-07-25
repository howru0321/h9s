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
      package: 'hello',
      protoPath: join(__dirname, './proto/hello.proto'),
      url: 'localhost:50051',
    },
  });

  await app.startAllMicroservices();

  console.log(`howbe-db Application is running on: ${await app.getUrl()}`);
}
bootstrap();
