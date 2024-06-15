import dayjs from 'dayjs/esm';

import { IFile, NewFile } from './file.model';

export const sampleWithRequiredData: IFile = {
  id: 26301,
  filename: 'unused category',
  bucketName: 'little beyond',
  objectName: 'spotless shrilly who',
};

export const sampleWithPartialData: IFile = {
  id: 413,
  filename: 'including organise yahoo',
  bucketName: 'piglet optimization um',
  objectName: 'failing yahoo trustworthy',
  createdAt: dayjs('2024-06-15T07:24'),
  isActive: true,
};

export const sampleWithFullData: IFile = {
  id: 15932,
  filename: 'since',
  bucketName: 'to unwieldy',
  objectName: 'during than sweaty',
  contentType: 'incidentally why surprised',
  fileSize: 20032,
  uploadedBy: 'next certainly',
  createdAt: dayjs('2024-06-15T07:03'),
  updatedAt: dayjs('2024-06-15T02:42'),
  isActive: false,
};

export const sampleWithNewData: NewFile = {
  filename: 'consequently',
  bucketName: 'suspiciously or',
  objectName: 'mushy',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
