import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe } from 'app/shared/date';
import { IFolder } from '../folder.model';

@Component({
  standalone: true,
  selector: 'jhi-folder-detail',
  templateUrl: './folder-detail.component.html',
  imports: [SharedModule, RouterModule, DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe],
})
export class FolderDetailComponent {
  folder = input<IFolder | null>(null);

  previousState(): void {
    window.history.back();
  }
}
