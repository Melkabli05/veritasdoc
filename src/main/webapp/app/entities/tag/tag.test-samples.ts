import dayjs from 'dayjs/esm';

import { ITag, NewTag } from './tag.model';

export const sampleWithRequiredData: ITag = {
  id: 14125,
  name: 'pith unnecessarily',
};

export const sampleWithPartialData: ITag = {
  id: 375,
  name: 'fatigues livid',
};

export const sampleWithFullData: ITag = {
  id: 14920,
  name: 'meanwhile',
  createdAt: dayjs('2024-06-14T12:43'),
  updatedAt: dayjs('2024-06-15T04:38'),
  isActive: true,
};

export const sampleWithNewData: NewTag = {
  name: 'echo',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
