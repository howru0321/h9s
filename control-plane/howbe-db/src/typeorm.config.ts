import { TypeOrmModuleOptions } from '@nestjs/typeorm';

export const objectkvDBConfig: TypeOrmModuleOptions = {
  name: 'objectkvConnection',
  type: 'sqlite',
  database: 'src/sqlite/objectkv.sqlite',
  entities: [__dirname + '/**/objectkv.entity{.ts,.js}'],
  synchronize: true,
};

export const keyDBConfig: TypeOrmModuleOptions = {
  name: 'keyConnection',
  type: 'sqlite',
  database: 'src/sqlite/key.sqlite',
  entities: [__dirname + '/**/key.entity{.ts,.js}'],
  synchronize: true,
};