import dayjs from 'dayjs/esm';

import { IMetadata, NewMetadata } from './metadata.model';

export const sampleWithRequiredData: IMetadata = {
  id: 26933,
  fileId: 'forenenst',
  key: 'atop resolution merrily',
  value: 'times',
};

export const sampleWithPartialData: IMetadata = {
  id: 9770,
  fileId: 'tattered forenenst',
  key: 'until',
  value: 'scribble coaxingly minute',
  createdAt: dayjs('2024-06-15T06:50'),
  updatedAt: dayjs('2024-06-15T07:24'),
};

export const sampleWithFullData: IMetadata = {
  id: 395,
  fileId: 'ouch iron ouch',
  key: 'ick spectacle',
  value: 'instead dizzy radio',
  createdAt: dayjs('2024-06-14T20:55'),
  updatedAt: dayjs('2024-06-14T18:53'),
};

export const sampleWithNewData: NewMetadata = {
  fileId: 'kindly infinite',
  key: 'finished foolishly',
  value: 'properly plagiarize malnutrition',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
