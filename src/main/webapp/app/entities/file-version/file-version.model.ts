import dayjs from 'dayjs/esm';

export interface IFileVersion {
  id: number;
  fileId?: string | null;
  versionNumber?: number | null;
  objectName?: string | null;
  createdAt?: dayjs.Dayjs | null;
}

export type NewFileVersion = Omit<IFileVersion, 'id'> & { id: null };
