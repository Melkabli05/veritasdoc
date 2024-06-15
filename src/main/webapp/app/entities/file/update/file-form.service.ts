import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IFile, NewFile } from '../file.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IFile for edit and NewFileFormGroupInput for create.
 */
type FileFormGroupInput = IFile | PartialWithRequiredKeyOf<NewFile>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IFile | NewFile> = Omit<T, 'createdAt' | 'updatedAt'> & {
  createdAt?: string | null;
  updatedAt?: string | null;
};

type FileFormRawValue = FormValueOf<IFile>;

type NewFileFormRawValue = FormValueOf<NewFile>;

type FileFormDefaults = Pick<NewFile, 'id' | 'createdAt' | 'updatedAt' | 'isActive' | 'tags' | 'folders'>;

type FileFormGroupContent = {
  id: FormControl<FileFormRawValue['id'] | NewFile['id']>;
  filename: FormControl<FileFormRawValue['filename']>;
  bucketName: FormControl<FileFormRawValue['bucketName']>;
  objectName: FormControl<FileFormRawValue['objectName']>;
  contentType: FormControl<FileFormRawValue['contentType']>;
  fileSize: FormControl<FileFormRawValue['fileSize']>;
  uploadedBy: FormControl<FileFormRawValue['uploadedBy']>;
  createdAt: FormControl<FileFormRawValue['createdAt']>;
  updatedAt: FormControl<FileFormRawValue['updatedAt']>;
  isActive: FormControl<FileFormRawValue['isActive']>;
  fileVersion: FormControl<FileFormRawValue['fileVersion']>;
  tags: FormControl<FileFormRawValue['tags']>;
  folders: FormControl<FileFormRawValue['folders']>;
};

export type FileFormGroup = FormGroup<FileFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class FileFormService {
  createFileFormGroup(file: FileFormGroupInput = { id: null }): FileFormGroup {
    const fileRawValue = this.convertFileToFileRawValue({
      ...this.getFormDefaults(),
      ...file,
    });
    return new FormGroup<FileFormGroupContent>({
      id: new FormControl(
        { value: fileRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      filename: new FormControl(fileRawValue.filename, {
        validators: [Validators.required],
      }),
      bucketName: new FormControl(fileRawValue.bucketName, {
        validators: [Validators.required],
      }),
      objectName: new FormControl(fileRawValue.objectName, {
        validators: [Validators.required],
      }),
      contentType: new FormControl(fileRawValue.contentType),
      fileSize: new FormControl(fileRawValue.fileSize),
      uploadedBy: new FormControl(fileRawValue.uploadedBy),
      createdAt: new FormControl(fileRawValue.createdAt),
      updatedAt: new FormControl(fileRawValue.updatedAt),
      isActive: new FormControl(fileRawValue.isActive),
      fileVersion: new FormControl(fileRawValue.fileVersion),
      tags: new FormControl(fileRawValue.tags ?? []),
      folders: new FormControl(fileRawValue.folders ?? []),
    });
  }

  getFile(form: FileFormGroup): IFile | NewFile {
    return this.convertFileRawValueToFile(form.getRawValue() as FileFormRawValue | NewFileFormRawValue);
  }

  resetForm(form: FileFormGroup, file: FileFormGroupInput): void {
    const fileRawValue = this.convertFileToFileRawValue({ ...this.getFormDefaults(), ...file });
    form.reset(
      {
        ...fileRawValue,
        id: { value: fileRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): FileFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdAt: currentTime,
      updatedAt: currentTime,
      isActive: false,
      tags: [],
      folders: [],
    };
  }

  private convertFileRawValueToFile(rawFile: FileFormRawValue | NewFileFormRawValue): IFile | NewFile {
    return {
      ...rawFile,
      createdAt: dayjs(rawFile.createdAt, DATE_TIME_FORMAT),
      updatedAt: dayjs(rawFile.updatedAt, DATE_TIME_FORMAT),
    };
  }

  private convertFileToFileRawValue(
    file: IFile | (Partial<NewFile> & FileFormDefaults),
  ): FileFormRawValue | PartialWithRequiredKeyOf<NewFileFormRawValue> {
    return {
      ...file,
      createdAt: file.createdAt ? file.createdAt.format(DATE_TIME_FORMAT) : undefined,
      updatedAt: file.updatedAt ? file.updatedAt.format(DATE_TIME_FORMAT) : undefined,
      tags: file.tags ?? [],
      folders: file.folders ?? [],
    };
  }
}
