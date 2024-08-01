import { Test, TestingModule } from '@nestjs/testing';
import { PodService } from './pod.service';

describe('PodService', () => {
  let service: PodService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [PodService],
    }).compile();

    service = module.get<PodService>(PodService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
