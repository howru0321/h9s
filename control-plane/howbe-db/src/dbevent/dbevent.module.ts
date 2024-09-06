import { Global, Module } from '@nestjs/common';
import { DbeventService } from './dbevent.service';

@Global()
@Module({
    providers: [DbeventService],
    exports: [DbeventService],
})
export class DbeventModule {}
