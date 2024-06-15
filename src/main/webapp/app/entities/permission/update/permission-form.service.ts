import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IPermission, NewPermission } from '../permission.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IPermission for edit and NewPermissionFormGroupInput for create.
 */
type PermissionFormGroupInput = IPermission | PartialWithRequiredKeyOf<NewPermission>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IPermission | NewPermission> = Omit<T, 'createdAt' | 'updatedAt'> & {
  createdAt?: string | null;
  updatedAt?: string | null;
};

type PermissionFormRawValue = FormValueOf<IPermission>;

type NewPermissionFormRawValue = FormValueOf<NewPermission>;

type PermissionFormDefaults = Pick<NewPermission, 'id' | 'createdAt' | 'updatedAt'>;

type PermissionFormGroupContent = {
  id: FormControl<PermissionFormRawValue['id'] | NewPermission['id']>;
  fileId: FormControl<PermissionFormRawValue['fileId']>;
  userId: FormControl<PermissionFormRawValue['userId']>;
  permission: FormControl<PermissionFormRawValue['permission']>;
  createdAt: FormControl<PermissionFormRawValue['createdAt']>;
  updatedAt: FormControl<PermissionFormRawValue['updatedAt']>;
  folder: FormControl<PermissionFormRawValue['folder']>;
};

export type PermissionFormGroup = FormGroup<PermissionFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class PermissionFormService {
  createPermissionFormGroup(permission: PermissionFormGroupInput = { id: null }): PermissionFormGroup {
    const permissionRawValue = this.convertPermissionToPermissionRawValue({
      ...this.getFormDefaults(),
      ...permission,
    });
    return new FormGroup<PermissionFormGroupContent>({
      id: new FormControl(
        { value: permissionRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      fileId: new FormControl(permissionRawValue.fileId, {
        validators: [Validators.required],
      }),
      userId: new FormControl(permissionRawValue.userId, {
        validators: [Validators.required],
      }),
      permission: new FormControl(permissionRawValue.permission, {
        validators: [Validators.required],
      }),
      createdAt: new FormControl(permissionRawValue.createdAt),
      updatedAt: new FormControl(permissionRawValue.updatedAt),
      folder: new FormControl(permissionRawValue.folder),
    });
  }

  getPermission(form: PermissionFormGroup): IPermission | NewPermission {
    return this.convertPermissionRawValueToPermission(form.getRawValue() as PermissionFormRawValue | NewPermissionFormRawValue);
  }

  resetForm(form: PermissionFormGroup, permission: PermissionFormGroupInput): void {
    const permissionRawValue = this.convertPermissionToPermissionRawValue({ ...this.getFormDefaults(), ...permission });
    form.reset(
      {
        ...permissionRawValue,
        id: { value: permissionRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): PermissionFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdAt: currentTime,
      updatedAt: currentTime,
    };
  }

  private convertPermissionRawValueToPermission(
    rawPermission: PermissionFormRawValue | NewPermissionFormRawValue,
  ): IPermission | NewPermission {
    return {
      ...rawPermission,
      createdAt: dayjs(rawPermission.createdAt, DATE_TIME_FORMAT),
      updatedAt: dayjs(rawPermission.updatedAt, DATE_TIME_FORMAT),
    };
  }

  private convertPermissionToPermissionRawValue(
    permission: IPermission | (Partial<NewPermission> & PermissionFormDefaults),
  ): PermissionFormRawValue | PartialWithRequiredKeyOf<NewPermissionFormRawValue> {
    return {
      ...permission,
      createdAt: permission.createdAt ? permission.createdAt.format(DATE_TIME_FORMAT) : undefined,
      updatedAt: permission.updatedAt ? permission.updatedAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
