import { Component, inject, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IFileVersion } from 'app/entities/file-version/file-version.model';
import { FileVersionService } from 'app/entities/file-version/service/file-version.service';
import { ITag } from 'app/entities/tag/tag.model';
import { TagService } from 'app/entities/tag/service/tag.service';
import { IFolder } from 'app/entities/folder/folder.model';
import { FolderService } from 'app/entities/folder/service/folder.service';
import { FileService } from '../service/file.service';
import { IFile } from '../file.model';
import { FileFormService, FileFormGroup } from './file-form.service';

@Component({
  standalone: true,
  selector: 'jhi-file-update',
  templateUrl: './file-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class FileUpdateComponent implements OnInit {
  isSaving = false;
  file: IFile | null = null;

  fileVersionsCollection: IFileVersion[] = [];
  tagsSharedCollection: ITag[] = [];
  foldersSharedCollection: IFolder[] = [];

  protected fileService = inject(FileService);
  protected fileFormService = inject(FileFormService);
  protected fileVersionService = inject(FileVersionService);
  protected tagService = inject(TagService);
  protected folderService = inject(FolderService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: FileFormGroup = this.fileFormService.createFileFormGroup();

  compareFileVersion = (o1: IFileVersion | null, o2: IFileVersion | null): boolean => this.fileVersionService.compareFileVersion(o1, o2);

  compareTag = (o1: ITag | null, o2: ITag | null): boolean => this.tagService.compareTag(o1, o2);

  compareFolder = (o1: IFolder | null, o2: IFolder | null): boolean => this.folderService.compareFolder(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ file }) => {
      this.file = file;
      if (file) {
        this.updateForm(file);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const file = this.fileFormService.getFile(this.editForm);
    if (file.id !== null) {
      this.subscribeToSaveResponse(this.fileService.update(file));
    } else {
      this.subscribeToSaveResponse(this.fileService.create(file));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IFile>>): void {
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

  protected updateForm(file: IFile): void {
    this.file = file;
    this.fileFormService.resetForm(this.editForm, file);

    this.fileVersionsCollection = this.fileVersionService.addFileVersionToCollectionIfMissing<IFileVersion>(
      this.fileVersionsCollection,
      file.fileVersion,
    );
    this.tagsSharedCollection = this.tagService.addTagToCollectionIfMissing<ITag>(this.tagsSharedCollection, ...(file.tags ?? []));
    this.foldersSharedCollection = this.folderService.addFolderToCollectionIfMissing<IFolder>(
      this.foldersSharedCollection,
      ...(file.folders ?? []),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.fileVersionService
      .query({ filter: 'file-is-null' })
      .pipe(map((res: HttpResponse<IFileVersion[]>) => res.body ?? []))
      .pipe(
        map((fileVersions: IFileVersion[]) =>
          this.fileVersionService.addFileVersionToCollectionIfMissing<IFileVersion>(fileVersions, this.file?.fileVersion),
        ),
      )
      .subscribe((fileVersions: IFileVersion[]) => (this.fileVersionsCollection = fileVersions));

    this.tagService
      .query()
      .pipe(map((res: HttpResponse<ITag[]>) => res.body ?? []))
      .pipe(map((tags: ITag[]) => this.tagService.addTagToCollectionIfMissing<ITag>(tags, ...(this.file?.tags ?? []))))
      .subscribe((tags: ITag[]) => (this.tagsSharedCollection = tags));

    this.folderService
      .query()
      .pipe(map((res: HttpResponse<IFolder[]>) => res.body ?? []))
      .pipe(map((folders: IFolder[]) => this.folderService.addFolderToCollectionIfMissing<IFolder>(folders, ...(this.file?.folders ?? []))))
      .subscribe((folders: IFolder[]) => (this.foldersSharedCollection = folders));
  }
}
