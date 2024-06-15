import { Component, inject, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IFile } from 'app/entities/file/file.model';
import { FileService } from 'app/entities/file/service/file.service';
import { IFolder } from '../folder.model';
import { FolderService } from '../service/folder.service';
import { FolderFormService, FolderFormGroup } from './folder-form.service';

@Component({
  standalone: true,
  selector: 'jhi-folder-update',
  templateUrl: './folder-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class FolderUpdateComponent implements OnInit {
  isSaving = false;
  folder: IFolder | null = null;

  filesSharedCollection: IFile[] = [];

  protected folderService = inject(FolderService);
  protected folderFormService = inject(FolderFormService);
  protected fileService = inject(FileService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: FolderFormGroup = this.folderFormService.createFolderFormGroup();

  compareFile = (o1: IFile | null, o2: IFile | null): boolean => this.fileService.compareFile(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ folder }) => {
      this.folder = folder;
      if (folder) {
        this.updateForm(folder);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const folder = this.folderFormService.getFolder(this.editForm);
    if (folder.id !== null) {
      this.subscribeToSaveResponse(this.folderService.update(folder));
    } else {
      this.subscribeToSaveResponse(this.folderService.create(folder));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IFolder>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(folder: IFolder): void {
    this.folder = folder;
    this.folderFormService.resetForm(this.editForm, folder);

    this.filesSharedCollection = this.fileService.addFileToCollectionIfMissing<IFile>(
      this.filesSharedCollection,
      folder.file,
      ...(folder.files ?? []),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.fileService
      .query()
      .pipe(map((res: HttpResponse<IFile[]>) => res.body ?? []))
      .pipe(
        map((files: IFile[]) =>
          this.fileService.addFileToCollectionIfMissing<IFile>(files, this.folder?.file, ...(this.folder?.files ?? [])),
        ),
      )
      .subscribe((files: IFile[]) => (this.filesSharedCollection = files));
  }
}
