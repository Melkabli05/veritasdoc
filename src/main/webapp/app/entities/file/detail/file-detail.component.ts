import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe } from 'app/shared/date';
import { IFile } from '../file.model';

@Component({
  standalone: true,
  selector: 'jhi-file-detail',
  templateUrl: './file-detail.component.html',
  imports: [SharedModule, RouterModule, DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe],
})
export class FileDetailComponent {
  file = input<IFile | null>(null);

  previousState(): void {
    window.history.back();
  }
}
