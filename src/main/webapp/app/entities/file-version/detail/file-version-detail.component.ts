import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe } from 'app/shared/date';
import { IFileVersion } from '../file-version.model';

@Component({
  standalone: true,
  selector: 'jhi-file-version-detail',
  templateUrl: './file-version-detail.component.html',
  imports: [SharedModule, RouterModule, DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe],
})
export class FileVersionDetailComponent {
  fileVersion = input<IFileVersion | null>(null);

  previousState(): void {
    window.history.back();
  }
}
