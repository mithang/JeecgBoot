// Interface data format used to return a unified format

export function resultSuccess<T = Recordable>(result: T, { message = 'ok' } = {}) {
  return {
    code: 0,
    result,
    message,
    type: 'success',
  };
}

export function resultPageSuccess<T = any>(
  pageNo: number,
  pageSize: number,
  list: T[],
  { message = 'ok' } = {}
) {
  const pageData = pagination(pageNo, pageSize, list);

  return {
    ...resultSuccess({
      records: pageData,
      total: list.length,
    }),
    message,
  };
}

export function resultError(message = 'Request failed', { code = -1, result = null } = {}) {
  return {
    code,
    result,
    message,
    type: 'error',
  };
}

export function pagination<T = any>(pageNo: number, pageSize: number, array: T[]): T[] {
  const offset = (pageNo - 1) * Number(pageSize);
  const ret =
    offset + Number(pageSize) >= array.length
      ? array.slice(offset, array.length)
      : array.slice(offset, offset + Number(pageSize));
  return ret;
}

export interface requestParams {
  method: string;
  body: any;
  headers?: { authorization?: string };
  query: any;
}

/**
 * @description Hàm này dùng để lấy token từ dữ liệu request, vui lòng sửa theo tình hình thực tế của dự án
 *
 */
export function getRequestToken({ headers }: requestParams): string | undefined {
  return headers?.authorization;
}

//TODO Đường dẫn cha của API (viết cứng nên chưa linh hoạt)
export const baseUrl = '/jeecgboot/mock';
