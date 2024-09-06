import { Injectable } from '@nestjs/common';
import { EventEmitter } from 'events';
import { WatchCreateRequest, WatchCreateRequest_FilterType } from '../proto/rpc'
import { Event_EventType } from '../proto/kv'
import { WatchInfo } from '../interfaces/watch_info.interface'

type EventMap = {
    [key in Event_EventType]: {
      [key: string]: WatchInfo
    }
  };

@Injectable()
export class DbeventService {
    private eventEmitterList: EventMap = {
        [Event_EventType.PUT]: {},
        [Event_EventType.DELETE]: {},
        [Event_EventType.UNRECOGNIZED]: {},
    };



    addEventEmitter(WatchCreateRequest: WatchCreateRequest, watch_id: number, eventEmitter: EventEmitter) {
        const filterTypes = WatchCreateRequest.filters;
        const filterSet = new Set(filterTypes);
        const watchInfo : WatchInfo = {
            eventEmitter: eventEmitter,
            key: WatchCreateRequest.key,
            rangeEnd: WatchCreateRequest.rangeEnd,
        }
        
        if (filterSet.has(WatchCreateRequest_FilterType.NOPUT) && filterSet.has(WatchCreateRequest_FilterType.NODELETE)) {
            this.eventEmitterList[Event_EventType.UNRECOGNIZED][watch_id] = watchInfo;
        } else if (filterSet.has(WatchCreateRequest_FilterType.NOPUT)) {
            this.eventEmitterList[Event_EventType.DELETE][watch_id] = watchInfo;
        } else if (filterSet.has(WatchCreateRequest_FilterType.NODELETE)) {
            this.eventEmitterList[Event_EventType.PUT][watch_id] = watchInfo;
        } else {
            this.eventEmitterList[Event_EventType.PUT][watch_id] = watchInfo;
            this.eventEmitterList[Event_EventType.DELETE][watch_id] = watchInfo;
        }
    }

    deleteEventEmitter(watch_id: number) {
        if (this.eventEmitterList[Event_EventType.PUT][watch_id]) {
            delete this.eventEmitterList[Event_EventType.PUT][watch_id];
        }
        if (this.eventEmitterList[Event_EventType.DELETE][watch_id]) {
            delete this.eventEmitterList[Event_EventType.DELETE][watch_id];
        }
        if (this.eventEmitterList[Event_EventType.UNRECOGNIZED][watch_id]) {
            delete this.eventEmitterList[Event_EventType.UNRECOGNIZED][watch_id];
        }
    }

    getEventEmitter(eventType : Event_EventType): {[key : number]: WatchInfo} {
        return this.eventEmitterList[eventType];
    }
}
