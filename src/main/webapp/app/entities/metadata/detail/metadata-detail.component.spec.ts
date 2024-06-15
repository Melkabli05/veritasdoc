import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { MetadataDetailComponent } from './metadata-detail.component';

describe('Metadata Management Detail Component', () => {
  let comp: MetadataDetailComponent;
  let fixture: ComponentFixture<MetadataDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MetadataDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: MetadataDetailComponent,
              resolve: { metadata: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(MetadataDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MetadataDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load metadata on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', MetadataDetailComponent);

      // THEN
      expect(instance.metadata()).toEqual(expect.objectContaining({ id: 123 }));
    });
  });

  describe('PreviousState', () => {
    it('Should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
