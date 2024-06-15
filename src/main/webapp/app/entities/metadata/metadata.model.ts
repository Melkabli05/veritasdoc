import dayjs from 'dayjs/esm';
import { IFile } from 'app/entities/file/file.model';

export interface IMetadata {
  id: number;
  fileId?: string | null;
  key?: string | null;
  value?: string | null;
  createdAt?: dayjs.Dayjs | null;
  updatedAt?: dayjs.Dayjs | null;
  file?: Pick<IFile, 'id'> | null;
}

export type NewMetadata = Omit<IMetadata, 'id'> & { id: null };
