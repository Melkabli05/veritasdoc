import dayjs from 'dayjs/esm';

import { IPermission, NewPermission } from './permission.model';

export const sampleWithRequiredData: IPermission = {
  id: 28295,
  fileId: 'low visible distill',
  userId: 'quarantine helpless frankly',
  permission: 'lest',
};

export const sampleWithPartialData: IPermission = {
  id: 8747,
  fileId: 'timely hydrogen verbally',
  userId: 'unrealistic instead gadzooks',
  permission: 'gleefully a',
  createdAt: dayjs('2024-06-14T15:07'),
};

export const sampleWithFullData: IPermission = {
  id: 8819,
  fileId: 'quaintly',
  userId: 'upon limply yuck',
  permission: 'endanger on',
  createdAt: dayjs('2024-06-15T04:17'),
  updatedAt: dayjs('2024-06-14T17:20'),
};

export const sampleWithNewData: NewPermission = {
  fileId: 'efficiency',
  userId: 'beside ax oeuvre',
  permission: 'bustle miskey',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
