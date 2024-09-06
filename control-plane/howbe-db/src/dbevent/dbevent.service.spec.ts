import { Test, TestingModule } from '@nestjs/testing';
import { DbeventService } from './dbevent.service';

describe('DbeventService', () => {
  let service: DbeventService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [DbeventService],
    }).compile();

    service = module.get<DbeventService>(DbeventService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
