import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IFileVersion } from '../file-version.model';
import { FileVersionService } from '../service/file-version.service';

@Component({
  standalone: true,
  templateUrl: './file-version-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class FileVersionDeleteDialogComponent {
  fileVersion?: IFileVersion;

  protected fileVersionService = inject(FileVersionService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.fileVersionService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
