import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JvmGraphComponent } from './jvm-graph.component';

describe('JvmGraphComponent', () => {
  let component: JvmGraphComponent;
  let fixture: ComponentFixture<JvmGraphComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JvmGraphComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JvmGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
