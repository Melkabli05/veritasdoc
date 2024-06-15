import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { FolderDetailComponent } from './folder-detail.component';

describe('Folder Management Detail Component', () => {
  let comp: FolderDetailComponent;
  let fixture: ComponentFixture<FolderDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FolderDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: FolderDetailComponent,
              resolve: { folder: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(FolderDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FolderDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load folder on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', FolderDetailComponent);

      // THEN
      expect(instance.folder()).toEqual(expect.objectContaining({ id: 123 }));
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
