import { getAuthCache, setAuthCache } from '/@/utils/auth';
/**
 * Nhóm một yêu cầu
 *
 * @param getPromise Truyền vào một phương thức có thể lấy được đối tượng Promise
 * @param groupId ID nhóm, nếu không truyền hoặc trống thì không nhóm
 * @param expire Thời gian hết hạn, mặc định là nửa phút
 */
export function httpGroupRequest(getPromise, groupId, expire = 1000 * 30) {
  if (groupId == null || groupId === '') {
    console.log('--------popup----------getFrom  DB-------with---no--groupId ');
    return getPromise();
  }

  if (getAuthCache(groupId)) {
    console.log('---------popup--------getFrom  Cache--------groupId = ' + groupId);
    return Promise.resolve(getAuthCache(groupId));
  } else {
    console.log('--------popup----------getFrom  DB---------groupId = ' + groupId);
  }

  // Chưa gửi yêu cầu, thì gửi yêu cầu đầu tiên
  return getPromise().then((res) => {
    setAuthCache(groupId, res);
    return Promise.resolve(res);
  });
}
