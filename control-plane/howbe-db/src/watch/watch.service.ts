import { Injectable } from '@nestjs/common';
import { Watch, WatchRequest,WatchCreateRequest, WatchCancelRequest, WatchResponse  } from '../proto/rpc'
import { DbeventService } from 'src/dbevent/dbevent.service';
import { Observable, Subscriber } from 'rxjs';
import EventType from '../interfaces/eventType'
import { EventEmitter } from 'events';
import { Event, KeyValue, Event_EventType } from '../proto/kv'
import { ObjectStatus } from '../interfaces/object_status.interface'

function generateWatchResponse(watchId: number, events : Event[]): WatchResponse {
    const response: WatchResponse = {
        watchId: watchId,
        created: true,
        canceled: false,
        cancelReason: "",
        events: events,
    };
    return response;
}

function addEventInValue(value: Uint8Array, eventType: EventType): Uint8Array {
    if (value === null) {
        value = Buffer.from('{}');
    }
    const valueStr: ObjectStatus = JSON.parse(value.toString());
    const returnValue = {
        type: eventType,
        object: valueStr,
    };
    return Buffer.from(JSON.stringify(returnValue));
}

@Injectable()
export class WatchService implements Watch{
    constructor(
        // @InjectRepository(ObjectStatusKV, 'objectkvConnection')
        // private readonly objectKvRepository: Repository<ObjectStatusKV>,
        // @InjectRepository(Key, 'keyConnection')
        // private readonly keyRepository: Repository<Key>,
        private readonly dbeventService: DbeventService,
    ){}

    Watch(request: Observable<WatchRequest>): Observable<WatchResponse> {
        return new Observable<WatchResponse>((observer: Subscriber<WatchResponse>) => {
            const subscription = request.subscribe({
                next: (watchRequest: WatchRequest) => {
                    if (watchRequest.createRequest) {
                        console.log("createRequest");
                        const eventEmitter : EventEmitter = new EventEmitter();
                        const objectEvent = (eventType : EventType) => {
                            return (watch_id: number, key : Uint8Array, value : Uint8Array, pre_key : Uint8Array, pre_value : Uint8Array) => {
                                const kv: KeyValue = {
                                    key: key,
                                    value: addEventInValue(value, eventType),
                                };
                                const preKv : KeyValue = {
                                    key: pre_key,
                                    value: pre_value,
                                };
                                const events : Event[] = [{
                                    type: Event_EventType.PUT,
                                    kv: kv,
                                    prevKv: preKv,
                                }];
                                const response: WatchResponse = generateWatchResponse(watch_id, events)
                                observer.next(response);
                            };
                        };
                        eventEmitter.on(EventType.ADDED, objectEvent(EventType.ADDED));
                        eventEmitter.on(EventType.MODIFIED, objectEvent(EventType.MODIFIED));
                        eventEmitter.on(EventType.DELETED, objectEvent(EventType.DELETED));

                        const watchId : number = watchRequest.createRequest.watchId;
                        this.dbeventService.addEventEmitter(watchRequest.createRequest, watchId, eventEmitter);
                    } else if (watchRequest.cancelRequest) {
                        console.log("cancelRequest");
                        const watchId : number = watchRequest.cancelRequest.watchId;
                        console.log("watchId: ", watchId);
                        this.dbeventService.deleteEventEmitter(watchId)
                        observer.complete();
                    }
                },
                error: (err) => observer.error(err),
                complete: () => observer.complete(),
            });
        });
    }
}
