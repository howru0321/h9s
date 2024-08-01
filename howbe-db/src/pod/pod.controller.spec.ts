import { Test, TestingModule } from '@nestjs/testing';
import { PodController } from './pod.controller';

describe('PodController', () => {
  let controller: PodController;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [PodController],
    }).compile();

    controller = module.get<PodController>(PodController);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
