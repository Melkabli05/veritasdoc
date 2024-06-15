import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IFileVersion, NewFileVersion } from '../file-version.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IFileVersion for edit and NewFileVersionFormGroupInput for create.
 */
type FileVersionFormGroupInput = IFileVersion | PartialWithRequiredKeyOf<NewFileVersion>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IFileVersion | NewFileVersion> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

type FileVersionFormRawValue = FormValueOf<IFileVersion>;

type NewFileVersionFormRawValue = FormValueOf<NewFileVersion>;

type FileVersionFormDefaults = Pick<NewFileVersion, 'id' | 'createdAt'>;

type FileVersionFormGroupContent = {
  id: FormControl<FileVersionFormRawValue['id'] | NewFileVersion['id']>;
  fileId: FormControl<FileVersionFormRawValue['fileId']>;
  versionNumber: FormControl<FileVersionFormRawValue['versionNumber']>;
  objectName: FormControl<FileVersionFormRawValue['objectName']>;
  createdAt: FormControl<FileVersionFormRawValue['createdAt']>;
};

export type FileVersionFormGroup = FormGroup<FileVersionFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class FileVersionFormService {
  createFileVersionFormGroup(fileVersion: FileVersionFormGroupInput = { id: null }): FileVersionFormGroup {
    const fileVersionRawValue = this.convertFileVersionToFileVersionRawValue({
      ...this.getFormDefaults(),
      ...fileVersion,
    });
    return new FormGroup<FileVersionFormGroupContent>({
      id: new FormControl(
        { value: fileVersionRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      fileId: new FormControl(fileVersionRawValue.fileId, {
        validators: [Validators.required],
      }),
      versionNumber: new FormControl(fileVersionRawValue.versionNumber, {
        validators: [Validators.required],
      }),
      objectName: new FormControl(fileVersionRawValue.objectName, {
        validators: [Validators.required],
      }),
      createdAt: new FormControl(fileVersionRawValue.createdAt),
    });
  }

  getFileVersion(form: FileVersionFormGroup): IFileVersion | NewFileVersion {
    return this.convertFileVersionRawValueToFileVersion(form.getRawValue() as FileVersionFormRawValue | NewFileVersionFormRawValue);
  }

  resetForm(form: FileVersionFormGroup, fileVersion: FileVersionFormGroupInput): void {
    const fileVersionRawValue = this.convertFileVersionToFileVersionRawValue({ ...this.getFormDefaults(), ...fileVersion });
    form.reset(
      {
        ...fileVersionRawValue,
        id: { value: fileVersionRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): FileVersionFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdAt: currentTime,
    };
  }

  private convertFileVersionRawValueToFileVersion(
    rawFileVersion: FileVersionFormRawValue | NewFileVersionFormRawValue,
  ): IFileVersion | NewFileVersion {
    return {
      ...rawFileVersion,
      createdAt: dayjs(rawFileVersion.createdAt, DATE_TIME_FORMAT),
    };
  }

  private convertFileVersionToFileVersionRawValue(
    fileVersion: IFileVersion | (Partial<NewFileVersion> & FileVersionFormDefaults),
  ): FileVersionFormRawValue | PartialWithRequiredKeyOf<NewFileVersionFormRawValue> {
    return {
      ...fileVersion,
      createdAt: fileVersion.createdAt ? fileVersion.createdAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
