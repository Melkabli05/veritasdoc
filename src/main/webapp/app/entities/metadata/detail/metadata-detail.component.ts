import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe } from 'app/shared/date';
import { IMetadata } from '../metadata.model';

@Component({
  standalone: true,
  selector: 'jhi-metadata-detail',
  templateUrl: './metadata-detail.component.html',
  imports: [SharedModule, RouterModule, DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe],
})
export class MetadataDetailComponent {
  metadata = input<IMetadata | null>(null);

  previousState(): void {
    window.history.back();
  }
}
