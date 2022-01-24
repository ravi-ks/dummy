import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NoTaskCardComponent } from './no-task-card.component';

describe('NoTaskCardComponent', () => {
  let component: NoTaskCardComponent;
  let fixture: ComponentFixture<NoTaskCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NoTaskCardComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NoTaskCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
