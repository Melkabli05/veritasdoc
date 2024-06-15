import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IFolder } from '../folder.model';
import { FolderService } from '../service/folder.service';

@Component({
  standalone: true,
  templateUrl: './folder-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class FolderDeleteDialogComponent {
  folder?: IFolder;

  protected folderService = inject(FolderService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.folderService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
