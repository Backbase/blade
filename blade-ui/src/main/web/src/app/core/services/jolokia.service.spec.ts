import { TestBed } from '@angular/core/testing';

import { JolokiaService } from './jolokia.service';

describe('JolokiaService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: JolokiaService = TestBed.get(JolokiaService);
    expect(service).toBeTruthy();
  });
});
