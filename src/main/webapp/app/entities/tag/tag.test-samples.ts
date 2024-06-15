import dayjs from 'dayjs/esm';

import { ITag, NewTag } from './tag.model';

export const sampleWithRequiredData: ITag = {
  id: 25969,
  name: 'eye kooky tee',
};

export const sampleWithPartialData: ITag = {
  id: 31112,
  name: 'pungent',
  isActive: false,
};

export const sampleWithFullData: ITag = {
  id: 18189,
  name: 'creosote ah',
  createdAt: dayjs('2024-06-14T15:43'),
  updatedAt: dayjs('2024-06-14T13:43'),
  isActive: false,
};

export const sampleWithNewData: NewTag = {
  name: 'below aboard',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
