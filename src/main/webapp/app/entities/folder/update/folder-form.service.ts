import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IFolder, NewFolder } from '../folder.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IFolder for edit and NewFolderFormGroupInput for create.
 */
type FolderFormGroupInput = IFolder | PartialWithRequiredKeyOf<NewFolder>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IFolder | NewFolder> = Omit<T, 'createdAt' | 'updatedAt'> & {
  createdAt?: string | null;
  updatedAt?: string | null;
};

type FolderFormRawValue = FormValueOf<IFolder>;

type NewFolderFormRawValue = FormValueOf<NewFolder>;

type FolderFormDefaults = Pick<NewFolder, 'id' | 'createdAt' | 'updatedAt' | 'isActive' | 'files'>;

type FolderFormGroupContent = {
  id: FormControl<FolderFormRawValue['id'] | NewFolder['id']>;
  name: FormControl<FolderFormRawValue['name']>;
  parentFolderId: FormControl<FolderFormRawValue['parentFolderId']>;
  createdAt: FormControl<FolderFormRawValue['createdAt']>;
  updatedAt: FormControl<FolderFormRawValue['updatedAt']>;
  isActive: FormControl<FolderFormRawValue['isActive']>;
  file: FormControl<FolderFormRawValue['file']>;
  files: FormControl<FolderFormRawValue['files']>;
};

export type FolderFormGroup = FormGroup<FolderFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class FolderFormService {
  createFolderFormGroup(folder: FolderFormGroupInput = { id: null }): FolderFormGroup {
    const folderRawValue = this.convertFolderToFolderRawValue({
      ...this.getFormDefaults(),
      ...folder,
    });
    return new FormGroup<FolderFormGroupContent>({
      id: new FormControl(
        { value: folderRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(folderRawValue.name, {
        validators: [Validators.required],
      }),
      parentFolderId: new FormControl(folderRawValue.parentFolderId),
      createdAt: new FormControl(folderRawValue.createdAt),
      updatedAt: new FormControl(folderRawValue.updatedAt),
      isActive: new FormControl(folderRawValue.isActive),
      file: new FormControl(folderRawValue.file),
      files: new FormControl(folderRawValue.files ?? []),
    });
  }

  getFolder(form: FolderFormGroup): IFolder | NewFolder {
    return this.convertFolderRawValueToFolder(form.getRawValue() as FolderFormRawValue | NewFolderFormRawValue);
  }

  resetForm(form: FolderFormGroup, folder: FolderFormGroupInput): void {
    const folderRawValue = this.convertFolderToFolderRawValue({ ...this.getFormDefaults(), ...folder });
    form.reset(
      {
        ...folderRawValue,
        id: { value: folderRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): FolderFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdAt: currentTime,
      updatedAt: currentTime,
      isActive: false,
      files: [],
    };
  }

  private convertFolderRawValueToFolder(rawFolder: FolderFormRawValue | NewFolderFormRawValue): IFolder | NewFolder {
    return {
      ...rawFolder,
      createdAt: dayjs(rawFolder.createdAt, DATE_TIME_FORMAT),
      updatedAt: dayjs(rawFolder.updatedAt, DATE_TIME_FORMAT),
    };
  }

  private convertFolderToFolderRawValue(
    folder: IFolder | (Partial<NewFolder> & FolderFormDefaults),
  ): FolderFormRawValue | PartialWithRequiredKeyOf<NewFolderFormRawValue> {
    return {
      ...folder,
      createdAt: folder.createdAt ? folder.createdAt.format(DATE_TIME_FORMAT) : undefined,
      updatedAt: folder.updatedAt ? folder.updatedAt.format(DATE_TIME_FORMAT) : undefined,
      files: folder.files ?? [],
    };
  }
}
