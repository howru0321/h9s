import { EventEmitter } from 'events';

export interface WatchInfo {
    eventEmitter: EventEmitter;
    key : Uint8Array;
    rangeEnd : Uint8Array;
}