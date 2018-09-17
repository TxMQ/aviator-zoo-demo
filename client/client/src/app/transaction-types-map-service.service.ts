import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ExoDistributedEndpointService } from './exo/exo-distributed-endpoint.service';
import { Subject } from 'rxjs/Subject';

@Injectable()
export class TransactionTypesMapServiceService {

  public transactionTypesMap: any;
  public ready: Subject<boolean> = new Subject<boolean>();

  constructor(private httpClient: HttpClient,
              public endpointsService: ExoDistributedEndpointService) {

    this.endpointsService.endpointsReady.subscribe((ready: boolean) => {
      if (ready) {
        let url: string = endpointsService.getBaseUrl();
        url = url.substring(0, url.indexOf('/', 8)) + '/exo/0.2.0/transactiontypes';
        this.httpClient.get(url).subscribe((result: any) => {
            this.transactionTypesMap = {};
            this.transactionTypesMap.hashes = result.payload;
            this.transactionTypesMap.names = {};
            Object.keys(result.payload).forEach( nsHash => {
              const namespace = result.payload[nsHash];
              const nsRecord = {
                namespace: namespace.namespace,
                namespaceHash: nsHash,
                transactionTypes: {}
              };

              Object.keys(namespace.transactionTypes).forEach(txnHash => {
                const txnName = namespace.transactionTypes[txnHash];
                nsRecord.transactionTypes[txnName] = txnHash;
              });

              this.transactionTypesMap.names[namespace.namespace] = nsRecord;
            });
            this.ready.next(true);
          }
        );

      }
    });
  }

  public getNamespaceFromHash(hash: number): string {
    return this.transactionTypesMap.hashes[hash].namespace;
  }

  public getHashForNamespace(namespace: string): number {
    return parseInt(this.transactionTypesMap.names[namespace].namespaceHash);
  }

  public getHashForTransacionType(namespace: string, transactionType: string): number {
    return parseInt(this.transactionTypesMap.names[namespace].transactionTypes[transactionType]);
  }

  public getTransactionTypeFromHashes(namespace: number, transactionType: number) {
    return this.transactionTypesMap.hashes[namespace].transactionTypes[transactionType];
  }
}
