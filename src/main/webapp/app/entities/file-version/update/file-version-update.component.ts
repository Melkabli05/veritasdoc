import { Component, inject, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IFileVersion } from '../file-version.model';
import { FileVersionService } from '../service/file-version.service';
import { FileVersionFormService, FileVersionFormGroup } from './file-version-form.service';

@Component({
  standalone: true,
  selector: 'jhi-file-version-update',
  templateUrl: './file-version-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class FileVersionUpdateComponent implements OnInit {
  isSaving = false;
  fileVersion: IFileVersion | null = null;

  protected fileVersionService = inject(FileVersionService);
  protected fileVersionFormService = inject(FileVersionFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: FileVersionFormGroup = this.fileVersionFormService.createFileVersionFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ fileVersion }) => {
      this.fileVersion = fileVersion;
      if (fileVersion) {
        this.updateForm(fileVersion);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const fileVersion = this.fileVersionFormService.getFileVersion(this.editForm);
    if (fileVersion.id !== null) {
      this.subscribeToSaveResponse(this.fileVersionService.update(fileVersion));
    } else {
      this.subscribeToSaveResponse(this.fileVersionService.create(fileVersion));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IFileVersion>>): void {
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

  protected updateForm(fileVersion: IFileVersion): void {
    this.fileVersion = fileVersion;
    this.fileVersionFormService.resetForm(this.editForm, fileVersion);
  }
}
