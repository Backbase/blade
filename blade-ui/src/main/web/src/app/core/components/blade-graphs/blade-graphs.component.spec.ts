import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BladeGraphsComponent } from './blade-graphs.component';

describe('BladeGraphsComponent', () => {
  let component: BladeGraphsComponent;
  let fixture: ComponentFixture<BladeGraphsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BladeGraphsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BladeGraphsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
