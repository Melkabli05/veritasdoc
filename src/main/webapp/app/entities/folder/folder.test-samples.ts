import dayjs from 'dayjs/esm';

import { IFolder, NewFolder } from './folder.model';

export const sampleWithRequiredData: IFolder = {
  id: 30524,
  name: 'tiny gadzooks',
};

export const sampleWithPartialData: IFolder = {
  id: 21747,
  name: 'theft yowza',
  parentFolderId: 'jubilantly irony caramel',
  isActive: true,
};

export const sampleWithFullData: IFolder = {
  id: 6284,
  name: 'perfectly down',
  parentFolderId: 'unfinished',
  createdAt: dayjs('2024-06-14T10:04'),
  updatedAt: dayjs('2024-06-15T08:29'),
  isActive: false,
};

export const sampleWithNewData: NewFolder = {
  name: 'if',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
