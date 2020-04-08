import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { WebappControlsComponent } from './webapp-controls.component';

describe('WebappControlsComponent', () => {
  let component: WebappControlsComponent;
  let fixture: ComponentFixture<WebappControlsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WebappControlsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WebappControlsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
