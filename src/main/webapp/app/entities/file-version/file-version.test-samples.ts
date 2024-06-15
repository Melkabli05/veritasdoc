import dayjs from 'dayjs/esm';

import { IFileVersion, NewFileVersion } from './file-version.model';

export const sampleWithRequiredData: IFileVersion = {
  id: 15561,
  fileId: 'character peel',
  versionNumber: 10109,
  objectName: 'boycott marker',
};

export const sampleWithPartialData: IFileVersion = {
  id: 20932,
  fileId: 'roster',
  versionNumber: 5824,
  objectName: 'duh multimedia celebrated',
  createdAt: dayjs('2024-06-14T15:37'),
};

export const sampleWithFullData: IFileVersion = {
  id: 517,
  fileId: 'severe',
  versionNumber: 19167,
  objectName: 'absent certainly complex',
  createdAt: dayjs('2024-06-14T10:39'),
};

export const sampleWithNewData: NewFileVersion = {
  fileId: 'yowza',
  versionNumber: 29827,
  objectName: 'bludge foray',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
