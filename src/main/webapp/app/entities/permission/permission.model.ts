import dayjs from 'dayjs/esm';
import { IFolder } from 'app/entities/folder/folder.model';

export interface IPermission {
  id: number;
  fileId?: string | null;
  userId?: string | null;
  permission?: string | null;
  createdAt?: dayjs.Dayjs | null;
  updatedAt?: dayjs.Dayjs | null;
  folder?: Pick<IFolder, 'id'> | null;
}

export type NewPermission = Omit<IPermission, 'id'> & { id: null };
