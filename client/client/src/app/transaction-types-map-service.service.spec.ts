import { TestBed, inject } from '@angular/core/testing';

import { TransactionTypesMapServiceService } from './transaction-types-map-service.service';

describe('TransactionTypesMapServiceService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TransactionTypesMapServiceService]
    });
  });

  it('should be created', inject([TransactionTypesMapServiceService], (service: TransactionTypesMapServiceService) => {
    expect(service).toBeTruthy();
  }));
});
