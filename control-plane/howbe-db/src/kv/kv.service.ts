import { Injectable } from '@nestjs/common';
import { Repository, EntityManager, SelectQueryBuilder } from 'typeorm';
import { InjectRepository } from '@nestjs/typeorm';
import { ObjectStatusKV } from '../entities/objectkv.entity';
import { Key } from '../entities/key.entity';
import { ObjectStatus, updateObjectStatus } from '../interfaces/object_status.interface';
import { PodStatusDTO, updatePodStatus } from '../interfaces/pod_status.interface';
import { DbeventService } from '../dbevent/dbevent.service'
import { EventEmitter } from 'events';
import { WatchInfo } from '../interfaces/watch_info.interface'
import EventType from 'src/interfaces/eventType';

import { KV, RangeRequest, RangeResponse, PutRequest, PutResponse, DeleteRangeRequest, DeleteRangeResponse } from '../proto/rpc';
import { KeyValue, Event_EventType } from '../proto/kv'
import { json } from 'stream/consumers';

// function isKeyPlusOne(key: string, rangeEnd: string): boolean {
//     if (key.length === 0 || rangeEnd.length === 0 || key.length !== rangeEnd.length) {
//         return false;
//     }
    
//     const keyCode = key.charCodeAt(key.length - 1);
//     const rangeEndCode = rangeEnd.charCodeAt(rangeEnd.length - 1);
    
//     return key.slice(0, -1) === rangeEnd.slice(0, -1) && rangeEndCode === keyCode + 1;
// }
function compare(a: Uint8Array, b: Uint8Array): number {
    const minLength = Math.min(a.length, b.length);
    for (let i = 0; i < minLength; i++) {
        if (a[i] < b[i]) return -1;
        if (a[i] > b[i]) return 1;
    }
    return a.length - b.length;
}
function isKeyPlusOne(start: Uint8Array, end: Uint8Array): boolean {
    if (start.length === 0) return false;
    const lastByte = start[start.length - 1];
    if (lastByte < 255) {
        return compare(end, new Uint8Array([...start.slice(0, -1), lastByte + 1])) === 0;
    }
    return compare(end, new Uint8Array([...start, 0])) === 0;
}
function buildRangeQuery(query: SelectQueryBuilder<Key>, key: Uint8Array, rangeEnd: Uint8Array): SelectQueryBuilder<Key> {
    const keyStr = Buffer.from(key).toString('utf8');
    const rangeEndStr = rangeEnd !==undefined ? Buffer.from(rangeEnd).toString('utf8') : null;

    if (!rangeEndStr) {
        return query.where('key.fullKey = :key', { key: keyStr });
    } 
    // else if (isKeyPlusOne(key, rangeEnd)) {
    //     return query.where('key.fullKey LIKE :prefix', { prefix: `${keyStr}%` });
    // } 
    else if (keyStr === '\0' && rangeEndStr === '\0') {
        return query; // No where clause needed, it will select all keys
    } else if (rangeEndStr === '\0') {
        return query.where('key.fullKey >= :key', { key: keyStr });
    } else {
        return query.where('key.fullKey >= :key AND key.fullKey < :rangeEnd', { key: keyStr, rangeEnd: rangeEndStr });
    }
}
function isKeyInRange(key: Uint8Array, rangeStart: Uint8Array, rangeEnd: Uint8Array): boolean {
    // If rangeEnd is not provided or is empty, check for exact match with rangeStart
    if (rangeEnd.length === 0) {
        return compare(key, rangeStart) === 0;
    }

    // If rangeStart and rangeEnd are both empty, all keys are in range
    if (rangeStart.length === 0 && rangeEnd.length === 0) {
        return true;
    }

    // If rangeEnd is '\0', check if key is greater than or equal to rangeStart
    if (rangeEnd.length === 1 && rangeEnd[0] === 0) {
        return compare(key, rangeStart) >= 0;
    }

    // General case: check if key is within the range
    return compare(key, rangeStart) >= 0 && compare(key, rangeEnd) < 0;
}

@Injectable()
export class KvService implements KV{
    constructor(
        @InjectRepository(ObjectStatusKV, 'objectkvConnection')
        private readonly objectKvRepository: Repository<ObjectStatusKV>,
        @InjectRepository(Key, 'keyConnection')
        private readonly keyRepository: Repository<Key>,
        private readonly dbeventService: DbeventService,
    ){}

    async Range(rangeRequest: RangeRequest): Promise<RangeResponse> {
        const { key, rangeEnd, limit, keysOnly, countOnly } = rangeRequest;

        let query = this.keyRepository.createQueryBuilder('key');
        query = buildRangeQuery(query, key, rangeEnd);

        // Apply limit if specified
        if (limit > 0) {
            query = query.take(limit);
        }

        // If countOnly is true, return only the count
        if (countOnly) {
            const count = await query.getCount();
            const kvs: KeyValue[] = [];
            const rangeResponse: RangeResponse = { kvs, count };
            return rangeResponse;
        }

        const keys = await query.getMany();

        let kvs: KeyValue[] = [];
        let count = 0;

        for (const keyEntity of keys) {
            if (keysOnly) {
                kvs.push({
                    key: Buffer.from(keyEntity.fullKey),
                    value: null//new Uint8Array()
                });
            } else {
                const objectKv = await this.objectKvRepository.findOne({ where: { key: keyEntity.fullKey } });
                if (objectKv) {
                    kvs.push({
                        key: Buffer.from(keyEntity.fullKey),
                        value: Buffer.from(JSON.stringify(objectKv.value))
                    });
                }
            }
            count++;
        }

        const rangeResponse: RangeResponse = {
            kvs,
            count
        };

        return rangeResponse;
    }

    async Put(putRequest: PutRequest): Promise<PutResponse> {
        const { key, value, prevKv } = putRequest;
        const keyString = Buffer.from(key).toString('utf8');
        const valueString: ObjectStatus = JSON.parse(Buffer.from(value).toString('utf8'));

        let prevKeyValue: KeyValue | undefined;

        // Parse the key to extract resourceType and name
        const keyParts = keyString.split('/');
        if (keyParts[1] !== 'registry') {
            throw new Error('Invalid key format');
        }

        const resourceType = keyParts[2];
        const name = keyParts[3];

        // Find or create Key entity
        let keyEntity = await this.keyRepository.findOne({ where: { fullKey: keyString } });
        if (!keyEntity) {
            keyEntity = new Key(resourceType, name);
            await this.keyRepository.save(keyEntity);
        }


        // If prev_kv is set, get the previous key-value pair
        if (prevKv) {
            const prevObjectKv = await this.objectKvRepository.findOne({ where: { key: keyString } });
            prevKeyValue = {
                key: Buffer.from(keyString),
                value: null
            };
            if (prevObjectKv) {
                prevKeyValue.value = Buffer.from(JSON.stringify(prevObjectKv.value));
            }
        }

        // Update or create ObjectStatusKV entity
        let objectKv : ObjectStatusKV = await this.objectKvRepository.findOne({ where: { key: keyString } });
        let preValue: ObjectStatus | undefined;
        let eventType : EventType;
        if (!objectKv) {
            objectKv = new ObjectStatusKV();
            objectKv.key = keyString;
            preValue = null;

            eventType= EventType.ADDED;
        } else {
            preValue = objectKv.value;
            eventType = EventType.MODIFIED;
        }
        const updateValue: ObjectStatus = updateObjectStatus(preValue, valueString);

        try {
            objectKv.value = updateValue;
        } catch (error) {
            throw new Error('Invalid JSON in value');
        }

        await this.objectKvRepository.save(objectKv);

        const watchInfos : {[key : number]: WatchInfo} = this.dbeventService.getEventEmitter(Event_EventType.PUT);
        for (const [watch_id, watchInfo] of Object.entries(watchInfos)) {
            const rangeFirst  : Uint8Array = watchInfo.key;
            const rangeEnd : Uint8Array = watchInfo.rangeEnd;
            if (isKeyInRange(key, rangeFirst, rangeEnd)) {
                // console.log("rangeFirst: ", rangeFirst.toString());
                // console.log("key: ", key.toString());
                // console.log("rangeEnd: ", rangeEnd.toString());
                const putEventEmitter : EventEmitter = watchInfo.eventEmitter;
                putEventEmitter.emit(eventType, watch_id, Buffer.from(keyString), Buffer.from(JSON.stringify(updateValue)), prevKeyValue?.key, prevKeyValue?.value);
            }
        }

        const putResponse: PutResponse = {
            prevKv: prevKeyValue
        };

        return putResponse;
    }

    async DeleteRange(deleteRangeRequest: DeleteRangeRequest): Promise<DeleteRangeResponse> {
        const { key, rangeEnd, prevKv } = deleteRangeRequest;

        let query = this.keyRepository.createQueryBuilder('key');
        query = buildRangeQuery(query, key, rangeEnd);

        let prevKvs: KeyValue[] = [];
        let keysToDelete: Key[] = [];

        let deleted = 0;

        keysToDelete = await query.getMany();
        for (const keyEntity of keysToDelete) {
            let prevKeyValue: KeyValue | undefined;
            const objectKv = await this.objectKvRepository.findOne({ where: { key: keyEntity.fullKey } });
            if (objectKv && prevKv) {
                prevKeyValue = {
                    key: Buffer.from(keyEntity.fullKey),
                    value: Buffer.from(JSON.stringify(objectKv.value))
                };
                prevKvs.push(prevKeyValue);
            }
            // Delete from Key table
            await this.keyRepository.delete(keyEntity.id);

            // Delete from ObjectStatusKV table
            await this.objectKvRepository.delete({ key: keyEntity.fullKey });
            deleted++;
            const watchInfos : {[key : number]: WatchInfo} = this.dbeventService.getEventEmitter(Event_EventType.DELETE);
            for (const [watch_id, watchInfo] of Object.entries(watchInfos)) {
                const rangeFirst  : Uint8Array = watchInfo.key;
                const rangeEnd : Uint8Array = watchInfo.rangeEnd;
                if (isKeyInRange(Buffer.from(keyEntity.fullKey), rangeFirst, rangeEnd)) {
                    // console.log("rangeFirst: ", rangeFirst.toString());
                    // console.log("key: ", key.toString());
                    // console.log("rangeEnd: ", rangeEnd.toString());
                    const putEventEmitter : EventEmitter = watchInfo.eventEmitter;
                    putEventEmitter.emit(EventType.DELETED, watch_id, Buffer.from(keyEntity.fullKey), prevKeyValue?.value, prevKeyValue?.key, prevKeyValue?.value);
                }
            }
        }

        console.log(`Deleted ${deleted} keys`);
        const deleteRangeResponse: DeleteRangeResponse = {
            deleted,
            prevKvs
        };

        return deleteRangeResponse;
    }

}
