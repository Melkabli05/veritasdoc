import { Component, inject, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IFile } from 'app/entities/file/file.model';
import { FileService } from 'app/entities/file/service/file.service';
import { ITag } from '../tag.model';
import { TagService } from '../service/tag.service';
import { TagFormService, TagFormGroup } from './tag-form.service';

@Component({
  standalone: true,
  selector: 'jhi-tag-update',
  templateUrl: './tag-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class TagUpdateComponent implements OnInit {
  isSaving = false;
  tag: ITag | null = null;

  filesSharedCollection: IFile[] = [];

  protected tagService = inject(TagService);
  protected tagFormService = inject(TagFormService);
  protected fileService = inject(FileService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: TagFormGroup = this.tagFormService.createTagFormGroup();

  compareFile = (o1: IFile | null, o2: IFile | null): boolean => this.fileService.compareFile(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ tag }) => {
      this.tag = tag;
      if (tag) {
        this.updateForm(tag);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const tag = this.tagFormService.getTag(this.editForm);
    if (tag.id !== null) {
      this.subscribeToSaveResponse(this.tagService.update(tag));
    } else {
      this.subscribeToSaveResponse(this.tagService.create(tag));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ITag>>): void {
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

  protected updateForm(tag: ITag): void {
    this.tag = tag;
    this.tagFormService.resetForm(this.editForm, tag);

    this.filesSharedCollection = this.fileService.addFileToCollectionIfMissing<IFile>(
      this.filesSharedCollection,
      tag.file,
      ...(tag.files ?? []),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.fileService
      .query()
      .pipe(map((res: HttpResponse<IFile[]>) => res.body ?? []))
      .pipe(
        map((files: IFile[]) => this.fileService.addFileToCollectionIfMissing<IFile>(files, this.tag?.file, ...(this.tag?.files ?? []))),
      )
      .subscribe((files: IFile[]) => (this.filesSharedCollection = files));
  }
}
