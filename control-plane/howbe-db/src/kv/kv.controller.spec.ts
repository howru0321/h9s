import { Test, TestingModule } from '@nestjs/testing';
import { KvController } from './kv.controller';

describe('KvController', () => {
  let controller: KvController;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [KvController],
    }).compile();

    controller = module.get<KvController>(KvController);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
