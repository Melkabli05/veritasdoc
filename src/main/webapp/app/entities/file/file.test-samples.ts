import dayjs from 'dayjs/esm';

import { IFile, NewFile } from './file.model';

export const sampleWithRequiredData: IFile = {
  id: 18214,
  filename: 'couple',
  bucketName: 'seriously',
  objectName: 'whose what',
};

export const sampleWithPartialData: IFile = {
  id: 19378,
  filename: 'by',
  bucketName: 'smoothly repay',
  objectName: 'drat',
  contentType: 'upward during',
  uploadedBy: 'even beget',
  createdAt: dayjs('2024-06-14T14:00'),
  updatedAt: dayjs('2024-06-14T15:47'),
};

export const sampleWithFullData: IFile = {
  id: 14638,
  filename: 'well commonsense',
  bucketName: 'side while gee',
  objectName: 'gigantic',
  contentType: 'now once ricochet',
  fileSize: 10766,
  uploadedBy: 'regarding cleverly',
  createdAt: dayjs('2024-06-15T08:14'),
  updatedAt: dayjs('2024-06-14T21:37'),
  isActive: true,
};

export const sampleWithNewData: NewFile = {
  filename: 'duh authority',
  bucketName: 'flower astride',
  objectName: 'bidder',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
