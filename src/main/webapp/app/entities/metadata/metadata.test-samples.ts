import dayjs from 'dayjs/esm';

import { IMetadata, NewMetadata } from './metadata.model';

export const sampleWithRequiredData: IMetadata = {
  id: 8621,
  fileId: 'quizzical',
  key: 'times immunise',
  value: 'refresh',
};

export const sampleWithPartialData: IMetadata = {
  id: 11203,
  fileId: 'whose dishonor',
  key: 'wherever inside',
  value: 'austere meh',
};

export const sampleWithFullData: IMetadata = {
  id: 16206,
  fileId: 'brave imperfect',
  key: 'muscatel',
  value: 'frenetically stark longitude',
  createdAt: dayjs('2024-06-14T20:57'),
  updatedAt: dayjs('2024-06-14T23:05'),
};

export const sampleWithNewData: NewMetadata = {
  fileId: 'to',
  key: 'but whose basic',
  value: 'unless stymie',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
