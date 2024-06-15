import dayjs from 'dayjs/esm';

import { IFileVersion, NewFileVersion } from './file-version.model';

export const sampleWithRequiredData: IFileVersion = {
  id: 1899,
  fileId: 'superintend plagiarize',
  versionNumber: 21133,
  objectName: 'incredible yowza',
};

export const sampleWithPartialData: IFileVersion = {
  id: 20208,
  fileId: 'supposing',
  versionNumber: 14212,
  objectName: 'and bid',
  createdAt: dayjs('2024-06-14T11:05'),
};

export const sampleWithFullData: IFileVersion = {
  id: 23049,
  fileId: 'harsh youthful',
  versionNumber: 16977,
  objectName: 'stimulating now',
  createdAt: dayjs('2024-06-15T04:21'),
};

export const sampleWithNewData: NewFileVersion = {
  fileId: 'um',
  versionNumber: 21993,
  objectName: 'oh blah',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
