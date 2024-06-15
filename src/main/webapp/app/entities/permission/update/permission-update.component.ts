import { Component, inject, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IFolder } from 'app/entities/folder/folder.model';
import { FolderService } from 'app/entities/folder/service/folder.service';
import { IPermission } from '../permission.model';
import { PermissionService } from '../service/permission.service';
import { PermissionFormService, PermissionFormGroup } from './permission-form.service';

@Component({
  standalone: true,
  selector: 'jhi-permission-update',
  templateUrl: './permission-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class PermissionUpdateComponent implements OnInit {
  isSaving = false;
  permission: IPermission | null = null;

  foldersSharedCollection: IFolder[] = [];

  protected permissionService = inject(PermissionService);
  protected permissionFormService = inject(PermissionFormService);
  protected folderService = inject(FolderService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: PermissionFormGroup = this.permissionFormService.createPermissionFormGroup();

  compareFolder = (o1: IFolder | null, o2: IFolder | null): boolean => this.folderService.compareFolder(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ permission }) => {
      this.permission = permission;
      if (permission) {
        this.updateForm(permission);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const permission = this.permissionFormService.getPermission(this.editForm);
    if (permission.id !== null) {
      this.subscribeToSaveResponse(this.permissionService.update(permission));
    } else {
      this.subscribeToSaveResponse(this.permissionService.create(permission));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPermission>>): void {
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

  protected updateForm(permission: IPermission): void {
    this.permission = permission;
    this.permissionFormService.resetForm(this.editForm, permission);

    this.foldersSharedCollection = this.folderService.addFolderToCollectionIfMissing<IFolder>(
      this.foldersSharedCollection,
      permission.folder,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.folderService
      .query()
      .pipe(map((res: HttpResponse<IFolder[]>) => res.body ?? []))
      .pipe(map((folders: IFolder[]) => this.folderService.addFolderToCollectionIfMissing<IFolder>(folders, this.permission?.folder)))
      .subscribe((folders: IFolder[]) => (this.foldersSharedCollection = folders));
  }
}
