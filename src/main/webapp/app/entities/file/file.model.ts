import dayjs from 'dayjs/esm';
import { IFileVersion } from 'app/entities/file-version/file-version.model';
import { ITag } from 'app/entities/tag/tag.model';
import { IFolder } from 'app/entities/folder/folder.model';

export interface IFile {
  id: number;
  filename?: string | null;
  bucketName?: string | null;
  objectName?: string | null;
  contentType?: string | null;
  fileSize?: number | null;
  uploadedBy?: string | null;
  createdAt?: dayjs.Dayjs | null;
  updatedAt?: dayjs.Dayjs | null;
  isActive?: boolean | null;
  fileVersion?: Pick<IFileVersion, 'id'> | null;
  tags?: Pick<ITag, 'id'>[] | null;
  folders?: Pick<IFolder, 'id'>[] | null;
}

export type NewFile = Omit<IFile, 'id'> & { id: null };
