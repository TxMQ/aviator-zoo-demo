import { Component, OnInit, OnDestroy } from '@angular/core';
import { ZooWebsocketService } from '../zoo-websocket.service';
import { Subscription } from '../../../node_modules/rxjs/Subscription';
import { Guid } from 'guid-typescript';
import { MatDialog, MatDialogRef } from '../../../node_modules/@angular/material';
import { WebSocketMessageDialogComponent } from '../web-socket-message-dialog/web-socket-message-dialog.component';
import { Router } from '../../../node_modules/@angular/router';
import { TransactionTypesMapServiceService } from '../transaction-types-map-service.service';

@Component({
  selector: 'app-websocket',
  templateUrl: './websocket.component.html',
  styleUrls: ['./websocket.component.css']
})
export class WebsocketComponent implements OnInit, OnDestroy {

  public messages: Array<any> = [];
  private MAX_MESSAGES = 100;
  
  private animalSpecies: string;
  private animalName = '';
  private currentTransactionID: string;

  private zoo: any;
  private subscription: Subscription;

  private getZooTimeout: number;
  private paused = false;

  private namespaceHash: number;
  private getZooHash: number;
  private addAnimalHash: number;

  constructor(private zooWebSocketService: ZooWebsocketService,
              private transactionTypesService:TransactionTypesMapServiceService,
              private dialogService: MatDialog,
              private router: Router ) {
              }

  ngOnInit() {
    this.subscription = 
    this.zooWebSocketService.zooSubject.subscribe(data => this.onWebSocketMessage(data));
    
    this.transactionTypesService.ready.subscribe(isReady => {
      if (isReady) {
        this.namespaceHash = this.transactionTypesService.getHashForNamespace('ZooDemoTransactionTypes');
        this.getZooHash = this.transactionTypesService.getHashForTransacionType('ZooDemoTransactionTypes', 'GET_ZOO');
        this.addAnimalHash = this.transactionTypesService.getHashForTransacionType('ZooDemoTransactionTypes', 'ADD_ANIMAL');
        setTimeout(() => this.getZoo(), 250);
      }
    });
  }

  ngOnDestroy() {
    if (this.getZooTimeout) {
      clearTimeout(this.getZooTimeout);
    }
  }

  private onWebSocketMessage(data: any): void {
    if (!this.checkNamespaces()) {
      return;
    }

    const item: any = JSON.parse(data.data);
    item.localTimestamp = Date.now();

    if (item.event === 'transactionComplete') {
      if (item.transactionType.ns === this.namespaceHash) {
        switch (item.transactionType.value) {
          case this.getZooHash:
            this.zoo = item.payload;
            break;

          case this.addAnimalHash:
            if (item.triggeringMessage.uuid == this.currentTransactionID) {
              this.reset();
              this.getZoo();
            }
            break;
        }
      }
    }
    this.messages.unshift(item);
    while (this.messages.length > this.MAX_MESSAGES) {
      this.messages.pop();
    }
  }

  private getZoo(): void {
    if (this.getZooTimeout) {
      clearTimeout(this.getZooTimeout);
    }

    if (!this.checkNamespaces()) {
      return;
    }
    
    const getZooRequest = {
      transactionType: {
        ns: this.namespaceHash,
        value: this.getZooHash
      },
      payload: null,
      uuid: Guid.raw()
    };

    console.log(getZooRequest);
    this.zooWebSocketService.zooSubject.next(getZooRequest);

    if (!this.paused) {
      this.getZooTimeout = setTimeout(_ => this.getZoo(), 2000);
    }
  }

  public addAnimal() {
    const addAnimalRequest = {
      transactionType: {
        ns: 'ZooDemoTransactionTypes',
        value: 'ADD_ANIMAL'
      },
      payload: {
        name: this.animalName,
        species: this.animalSpecies
      },
      uuid: Guid.raw()
    };
    this.currentTransactionID = addAnimalRequest.uuid;
    this.zooWebSocketService.zooSubject.next(addAnimalRequest);
  }

  public viewMessage(message: any) {
    const dialog: MatDialogRef<WebSocketMessageDialogComponent> = this.dialogService.open(
      WebSocketMessageDialogComponent,
      {
        data: {
          message: message
        }
      }
    );
  }

  public toggle() {
    this.paused = !this.paused;
    if (this.paused) {
      if (this.getZooTimeout) {
        clearInterval(this.getZooTimeout);
        this.getZooTimeout = null;
      }
    } else {
      this.getZoo();
    }
  }

  private reset(): void {
    this.currentTransactionID = null;
    this.animalName = '';
    this.animalSpecies = null;
  }

  private goToREST() {
    this.router.navigate(['home']);
  }

  private checkNamespaces(): boolean {
    if (this.namespaceHash === undefined) {
      if (this.transactionTypesService.transactionTypesMap) {
        this.namespaceHash = this.transactionTypesService.getHashForNamespace('ZooDemoTransactionTypes');
        this.getZooHash = this.transactionTypesService.getHashForTransacionType('ZooDemoTransactionTypes', 'GET_ZOO');
        this.addAnimalHash = this.transactionTypesService.getHashForTransacionType('ZooDemoTransactionTypes', 'ADD_ANIMAL');
      } else {
        return false;
      }
    } 

    return true;
  }
}
