import { Component, inject, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IFile } from 'app/entities/file/file.model';
import { FileService } from 'app/entities/file/service/file.service';
import { IMetadata } from '../metadata.model';
import { MetadataService } from '../service/metadata.service';
import { MetadataFormService, MetadataFormGroup } from './metadata-form.service';

@Component({
  standalone: true,
  selector: 'jhi-metadata-update',
  templateUrl: './metadata-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class MetadataUpdateComponent implements OnInit {
  isSaving = false;
  metadata: IMetadata | null = null;

  filesSharedCollection: IFile[] = [];

  protected metadataService = inject(MetadataService);
  protected metadataFormService = inject(MetadataFormService);
  protected fileService = inject(FileService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: MetadataFormGroup = this.metadataFormService.createMetadataFormGroup();

  compareFile = (o1: IFile | null, o2: IFile | null): boolean => this.fileService.compareFile(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ metadata }) => {
      this.metadata = metadata;
      if (metadata) {
        this.updateForm(metadata);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const metadata = this.metadataFormService.getMetadata(this.editForm);
    if (metadata.id !== null) {
      this.subscribeToSaveResponse(this.metadataService.update(metadata));
    } else {
      this.subscribeToSaveResponse(this.metadataService.create(metadata));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IMetadata>>): void {
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

  protected updateForm(metadata: IMetadata): void {
    this.metadata = metadata;
    this.metadataFormService.resetForm(this.editForm, metadata);

    this.filesSharedCollection = this.fileService.addFileToCollectionIfMissing<IFile>(this.filesSharedCollection, metadata.file);
  }

  protected loadRelationshipsOptions(): void {
    this.fileService
      .query()
      .pipe(map((res: HttpResponse<IFile[]>) => res.body ?? []))
      .pipe(map((files: IFile[]) => this.fileService.addFileToCollectionIfMissing<IFile>(files, this.metadata?.file)))
      .subscribe((files: IFile[]) => (this.filesSharedCollection = files));
  }
}
