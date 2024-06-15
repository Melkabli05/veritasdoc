import dayjs from 'dayjs/esm';

import { IPermission, NewPermission } from './permission.model';

export const sampleWithRequiredData: IPermission = {
  id: 19444,
  fileId: 'below',
  userId: 'analyst boo',
  permission: 'whether',
};

export const sampleWithPartialData: IPermission = {
  id: 371,
  fileId: 'instead commercial joshingly',
  userId: 'supposing obliterate lest',
  permission: 'commonly',
  updatedAt: dayjs('2024-06-15T00:24'),
};

export const sampleWithFullData: IPermission = {
  id: 15390,
  fileId: 'boo',
  userId: 'yowza wisely',
  permission: 'gosh sometimes alarmed',
  createdAt: dayjs('2024-06-15T01:16'),
  updatedAt: dayjs('2024-06-14T20:57'),
};

export const sampleWithNewData: NewPermission = {
  fileId: 'gee',
  userId: 'boo wise',
  permission: 'physically how whack',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
