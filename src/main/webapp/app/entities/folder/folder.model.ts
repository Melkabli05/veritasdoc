import dayjs from 'dayjs/esm';
import { IFile } from 'app/entities/file/file.model';

export interface IFolder {
  id: number;
  name?: string | null;
  parentFolderId?: string | null;
  createdAt?: dayjs.Dayjs | null;
  updatedAt?: dayjs.Dayjs | null;
  isActive?: boolean | null;
  file?: Pick<IFile, 'id'> | null;
  files?: Pick<IFile, 'id'>[] | null;
}

export type NewFolder = Omit<IFolder, 'id'> & { id: null };
