import dayjs from 'dayjs/esm';

import { IFolder, NewFolder } from './folder.model';

export const sampleWithRequiredData: IFolder = {
  id: 8283,
  name: 'generally failing',
};

export const sampleWithPartialData: IFolder = {
  id: 18907,
  name: 'polenta',
  updatedAt: dayjs('2024-06-14T12:14'),
};

export const sampleWithFullData: IFolder = {
  id: 23895,
  name: 'shrill',
  parentFolderId: 'except',
  createdAt: dayjs('2024-06-14T23:07'),
  updatedAt: dayjs('2024-06-15T08:27'),
  isActive: false,
};

export const sampleWithNewData: NewFolder = {
  name: 'painting',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
