import { TestBed } from '@angular/core/testing';

import { MotelMock } from './motel-mock';

describe('MotelMock', () => {
  let service: MotelMock;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MotelMock);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
