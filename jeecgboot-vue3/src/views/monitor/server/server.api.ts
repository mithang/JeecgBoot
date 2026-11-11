import { defHttp } from '/@/utils/http/axios';

enum Api {
  cpuCount = '/actuator/metrics/system.cpu.count',
  cpuUsage = '/actuator/metrics/system.cpu.usage',
  processStartTime = '/actuator/metrics/process.start.time',
  processUptime = '/actuator/metrics/process.uptime',
  processCpuUsage = '/actuator/metrics/process.cpu.usage',

  jvmMemoryMax = '/actuator/metrics/jvm.memory.max',
  jvmMemoryCommitted = '/actuator/metrics/jvm.memory.committed',
  jvmMemoryUsed = '/actuator/metrics/jvm.memory.used',
  jvmBufferMemoryUsed = '/actuator/metrics/jvm.buffer.memory.used',
  jvmBufferCount = '/actuator/metrics/jvm.buffer.count',
  jvmThreadsDaemon = '/actuator/metrics/jvm.threads.daemon',
  jvmThreadsLive = '/actuator/metrics/jvm.threads.live',
  jvmThreadsPeak = '/actuator/metrics/jvm.threads.peak',
  jvmClassesLoaded = '/actuator/metrics/jvm.classes.loaded',
  jvmClassesUnloaded = '/actuator/metrics/jvm.classes.unloaded',
  jvmGcMemoryAllocated = '/actuator/metrics/jvm.gc.memory.allocated',
  jvmGcMemoryPromoted = '/actuator/metrics/jvm.gc.memory.promoted',
  jvmGcMaxDataSize = '/actuator/metrics/jvm.gc.max.data.size',
  jvmGcLiveDataSize = '/actuator/metrics/jvm.gc.live.data.size',
  jvmGcPause = '/actuator/metrics/jvm.gc.pause',

  tomcatSessionsCreated = '/actuator/metrics/tomcat.sessions.created',
  tomcatSessionsExpired = '/actuator/metrics/tomcat.sessions.expired',
  tomcatSessionsActiveCurrent = '/actuator/metrics/tomcat.sessions.active.current',
  tomcatSessionsActiveMax = '/actuator/metrics/tomcat.sessions.active.max',
  tomcatSessionsRejected = '/actuator/metrics/tomcat.sessions.rejected',

  memoryInfo = '/sys/actuator/memory/info',
  // Giám sát Undertow
  undertowSessionsCreated = '/actuator/metrics/undertow.sessions.created',
  undertowSessionsExpired = '/actuator/metrics/undertow.sessions.expired',
  undertowSessionsActiveCurrent = '/actuator/metrics/undertow.sessions.active.current',
  undertowSessionsActiveMax = '/actuator/metrics/undertow.sessions.active.max',
}

/**
 * Truy vấn số lượng CPU
 */
export const getCpuCount = () => {
  return defHttp.get({ url: Api.cpuCount }, { isTransformResponse: false });
};

/**
 * Truy vấn tỷ lệ sử dụng CPU của hệ thống
 */
export const getCpuUsage = () => {
  return defHttp.get({ url: Api.cpuUsage }, { isTransformResponse: false });
};

/**
 * Truy vấn thời điểm ứng dụng khởi động
 */
export const getProcessStartTime = () => {
  return defHttp.get({ url: Api.processStartTime }, { isTransformResponse: false });
};

/**
 * Truy vấn thời gian ứng dụng đã chạy
 */
export const getProcessUptime = () => {
  return defHttp.get({ url: Api.processUptime }, { isTransformResponse: false });
};

/**
 * Truy vấn tỷ lệ sử dụng CPU của ứng dụng hiện tại
 */
export const getProcessCpuUsage = () => {
  return defHttp.get({ url: Api.processCpuUsage }, { isTransformResponse: false });
};

/**
 * Truy vấn bộ nhớ tối đa của JVM
 */
export const getJvmMemoryMax = () => {
  return defHttp.get({ url: Api.jvmMemoryMax }, { isTransformResponse: false });
};

/**
 * Bộ nhớ khả dụng của JVM
 */
export const getJvmMemoryCommitted = () => {
  return defHttp.get({ url: Api.jvmMemoryCommitted }, { isTransformResponse: false });
};

/**
 * Bộ nhớ đã dùng của JVM
 */
export const getJvmMemoryUsed = () => {
  return defHttp.get({ url: Api.jvmMemoryUsed }, { isTransformResponse: false });
};

/**
 * Bộ nhớ đệm đã dùng của JVM
 */
export const getJvmBufferMemoryUsed = () => {
  return defHttp.get({ url: Api.jvmBufferMemoryUsed }, { isTransformResponse: false });
};

/**
 * Số lượng bộ đệm hiện tại của JVM
 */
export const getJvmBufferCount = () => {
  return defHttp.get({ url: Api.jvmBufferCount }, { isTransformResponse: false });
};

/**
 ** Số lượng thread daemon của JVM
 */
export const getJvmThreadsDaemon = () => {
  return defHttp.get({ url: Api.jvmThreadsDaemon }, { isTransformResponse: false });
};

/**
 * Số lượng thread đang hoạt động của JVM
 */
export const getJvmThreadsLive = () => {
  return defHttp.get({ url: Api.jvmThreadsLive }, { isTransformResponse: false });
};

/**
 * Số lượng thread cao nhất của JVM
 */
export const getJvmThreadsPeak = () => {
  return defHttp.get({ url: Api.jvmThreadsPeak }, { isTransformResponse: false });
};

/**
 * Số lượng class đã tải của JVM
 */
export const getJvmClassesLoaded = () => {
  return defHttp.get({ url: Api.jvmClassesLoaded }, { isTransformResponse: false });
};

/**
 * Số lượng class chưa tải của JVM
 */
export const getJvmClassesUnloaded = () => {
  return defHttp.get({ url: Api.jvmClassesUnloaded }, { isTransformResponse: false });
};

/**
 ** Bộ nhớ được GC phân bổ cho Young Generation
 */
export const getJvmGcMemoryAllocated = () => {
  return defHttp.get({ url: Api.jvmGcMemoryAllocated }, { isTransformResponse: false });
};

/**
 * Bộ nhớ được GC phân bổ cho Old Generation
 */
export const getJvmGcMemoryPromoted = () => {
  return defHttp.get({ url: Api.jvmGcMemoryPromoted }, { isTransformResponse: false });
};

/**
 * Bộ nhớ tối đa của Old Generation khi GC
 */
export const getJvmGcMaxDataSize = () => {
  return defHttp.get({ url: Api.jvmGcMaxDataSize }, { isTransformResponse: false });
};

/**
 * Bộ nhớ của Old Generation khi Full GC
 */
export const getJvmGcLiveDataSize = () => {
  return defHttp.get({ url: Api.jvmGcLiveDataSize }, { isTransformResponse: false });
};

/**
 * Số lần GC kể từ khi hệ thống khởi động
 */
export const getJvmGcPause = () => {
  return defHttp.get({ url: Api.jvmGcPause }, { isTransformResponse: false });
};

/**
 * Số session Tomcat đã tạo
 */
export const getTomcatSessionsCreated = () => {
  return defHttp.get({ url: Api.tomcatSessionsCreated }, { isTransformResponse: false });
};

/**
 * Số session Tomcat đã hết hạn
 */
export const getTomcatSessionsExpired = () => {
  return defHttp.get({ url: Api.tomcatSessionsExpired }, { isTransformResponse: false });
};

/**
 * Số session Tomcat đang hoạt động
 */
export const getTomcatSessionsActiveCurrent = () => {
  return defHttp.get({ url: Api.tomcatSessionsActiveCurrent }, { isTransformResponse: false });
};

/**
 * Số session Tomcat đang hoạt động tối đa
 */
export const getTomcatSessionsActiveMax = () => {
  return defHttp.get({ url: Api.tomcatSessionsActiveMax }, { isTransformResponse: false });
};

/**
 * Số session bị từ chối sau khi vượt quá số lượng session tối đa đã cấu hình
 */
export const getTomcatSessionsRejected = () => {
  return defHttp.get({ url: Api.tomcatSessionsRejected }, { isTransformResponse: false });
};

/**
 * Số session Undertow đã tạo
 */
export const getUndertowSessionsCreated = () => {
  return defHttp.get({ url: Api.undertowSessionsCreated }, { isTransformResponse: false });
};

/**
 * Số session Undertow đã hết hạn
 */
export const getUndertowSessionsExpired = () => {
  return defHttp.get({ url: Api.undertowSessionsExpired }, { isTransformResponse: false });
};

/**
 * Số session Undertow đang hoạt động
 */
export const getUndertowSessionsActiveCurrent = () => {
  return defHttp.get({ url: Api.undertowSessionsActiveCurrent }, { isTransformResponse: false });
};

/**
 * Số session Undertow đang hoạt động tối đa
 */
export const getUndertowSessionsActiveMax = () => {
  return defHttp.get({ url: Api.undertowSessionsActiveMax }, { isTransformResponse: false });
};

/**
 * Thông tin bộ nhớ
 */
export const getMemoryInfo = () => {
  return defHttp.get({ url: Api.memoryInfo }, { isTransformResponse: false });
};

export const getMoreInfo = (infoType) => {
  if (infoType == '1') {
    return {};
  }
  if (infoType == '2') {
    return { 'jvm.gc.pause': ['.count', '.totalTime'] };
  }
  if (infoType == '3') {
    return {
      'tomcat.global.request': ['.count', '.totalTime'],
      'tomcat.servlet.request': ['.count', '.totalTime'],
    };
  }
  if (infoType == '5') {
    return {};
  }
  if (infoType == '6') {
    return {};
  }
};

export const getTextInfo = (infoType) => {
  if (infoType == '1') {
    return {
      'system.cpu.count': { color: 'green', text: 'Số lượng CPU', unit: 'lõi' },
      'system.cpu.usage': { color: 'green', text: 'Tỷ lệ sử dụng CPU hệ thống', unit: '%', valueType: 'Number' },
      'process.start.time': { color: 'purple', text: 'Thời điểm ứng dụng khởi động', unit: '', valueType: 'Date' },
      'process.uptime': { color: 'purple', text: 'Thời gian ứng dụng đã chạy', unit: 'giây' },
      'process.cpu.usage': { color: 'purple', text: 'Tỷ lệ sử dụng CPU của ứng dụng hiện tại', unit: '%', valueType: 'Number' },
    };
  }
  if (infoType == '2') {
    return {
      'jvm.memory.max': { color: 'purple', text: 'Bộ nhớ tối đa của JVM', unit: 'MB', valueType: 'RAM' },
      'jvm.memory.committed': { color: 'purple', text: 'Bộ nhớ khả dụng của JVM', unit: 'MB', valueType: 'RAM' },
      'jvm.memory.used': { color: 'purple', text: 'Bộ nhớ đã dùng của JVM', unit: 'MB', valueType: 'RAM' },
      'jvm.buffer.memory.used': { color: 'cyan', text: 'Bộ nhớ đệm đã dùng của JVM', unit: 'MB', valueType: 'RAM' },
      'jvm.buffer.count': { color: 'cyan', text: 'Số lượng bộ đệm hiện tại', unit: '' },
      'jvm.threads.daemon': { color: 'green', text: 'Số lượng thread daemon của JVM', unit: '' },
      'jvm.threads.live': { color: 'green', text: 'Số lượng thread đang hoạt động của JVM', unit: '' },
      'jvm.threads.peak': { color: 'green', text: 'Số lượng thread cao nhất của JVM', unit: '' },
      'jvm.classes.loaded': { color: 'orange', text: 'Số lượng class đã tải của JVM', unit: '' },
      'jvm.classes.unloaded': { color: 'orange', text: 'Số lượng class chưa tải của JVM', unit: '' },
      'jvm.gc.memory.allocated': { color: 'pink', text: 'Bộ nhớ được GC phân bổ cho Young Generation', unit: 'MB', valueType: 'RAM' },
      'jvm.gc.memory.promoted': { color: 'pink', text: 'Bộ nhớ được GC phân bổ cho Old Generation', unit: 'MB', valueType: 'RAM' },
      'jvm.gc.max.data.size': { color: 'pink', text: 'Bộ nhớ tối đa của Old Generation khi GC', unit: 'MB', valueType: 'RAM' },
      'jvm.gc.live.data.size': { color: 'pink', text: 'Bộ nhớ của Old Generation khi Full GC', unit: 'MB', valueType: 'RAM' },
      'jvm.gc.pause.count': { color: 'blue', text: 'Số lần GC kể từ khi hệ thống khởi động', unit: 'lần' },
      'jvm.gc.pause.totalTime': { color: 'blue', text: 'Tổng thời gian GC kể từ khi hệ thống khởi động', unit: 'giây' },
    };
  }
  if (infoType == '3') {
    return {
      'tomcat.sessions.created': { color: 'green', text: 'Số session Tomcat đã tạo', unit: '' },
      'tomcat.sessions.expired': { color: 'green', text: 'Số session Tomcat đã hết hạn', unit: '' },
      'tomcat.sessions.active.current': { color: 'green', text: 'Số session Tomcat đang hoạt động', unit: '' },
      'tomcat.sessions.active.max': { color: 'green', text: 'Số session Tomcat đang hoạt động tối đa', unit: '' },
      'tomcat.sessions.rejected': { color: 'green', text: 'Số session bị từ chối sau khi vượt quá số lượng session tối đa đã cấu hình', unit: '' },
      'tomcat.global.sent': { color: 'purple', text: 'Số byte đã gửi', unit: 'bytes' },
      'tomcat.global.request.max': { color: 'purple', text: 'Thời gian xử lý request dài nhất', unit: 'giây' },
      'tomcat.global.request.count': { color: 'purple', text: 'Tổng số lần request toàn cục', unit: 'lần' },
      'tomcat.global.request.totalTime': { color: 'purple', text: 'Tổng thời gian xử lý request toàn cục', unit: 'giây' },
      'tomcat.servlet.request.max': { color: 'cyan', text: 'Thời gian xử lý request của servlet dài nhất', unit: 'giây' },
      'tomcat.servlet.request.count': { color: 'cyan', text: 'Tổng số lần request của servlet', unit: 'lần' },
      'tomcat.servlet.request.totalTime': { color: 'cyan', text: 'Tổng thời gian xử lý request của servlet', unit: 'giây' },
      'tomcat.threads.current': { color: 'pink', text: 'Số thread hiện tại của Tomcat (bao gồm thread daemon)', unit: '' },
      'tomcat.threads.config.max': { color: 'pink', text: 'Số thread tối đa đã cấu hình của Tomcat', unit: '' },
    };
  }
  if (infoType == '5') {
    return {
      'memory.physical.total': { color: 'green', text: 'Tổng bộ nhớ vật lý', unit: 'MB', valueType: 'RAM' },
      'memory.physical.used': { color: 'green', text: 'Bộ nhớ vật lý đã dùng', unit: 'MB', valueType: 'RAM' },
      'memory.physical.free': { color: 'green', text: 'Bộ nhớ vật lý khả dụng', unit: 'MB', valueType: 'RAM' },
      'memory.physical.usage': { color: 'green', text: 'Tỷ lệ sử dụng bộ nhớ vật lý', unit: '%', valueType: 'Number' },
      'memory.runtime.total': { color: 'purple', text: 'Tổng bộ nhớ JVM', unit: 'MB', valueType: 'RAM' },
      'memory.runtime.used': { color: 'purple', text: 'Bộ nhớ JVM đã dùng', unit: 'MB', valueType: 'RAM' },
      'memory.runtime.max': { color: 'purple', text: 'Bộ nhớ tối đa của JVM', unit: 'MB', valueType: 'RAM' },
      'memory.runtime.free': { color: 'purple', text: 'Bộ nhớ khả dụng của JVM', unit: 'MB', valueType: 'RAM' },
      'memory.runtime.usage': { color: 'purple', text: 'Tỷ lệ sử dụng bộ nhớ JVM', unit: '%', valueType: 'Number' },
    };
  }
  if (infoType == '6') {
    // Giám sát Undertow
    return {
      'undertow.sessions.created': { color: 'green', text: 'Số session Undertow đã tạo', unit: '' },
      'undertow.sessions.expired': { color: 'green', text: 'Số session Undertow đã hết hạn', unit: '' },
      'undertow.sessions.active.current': { color: 'green', text: 'Số session Undertow đang hoạt động', unit: '' },
      'undertow.sessions.active.max': { color: 'green', text: 'Số session Undertow đang hoạt động tối đa', unit: '' },
      'undertow.sessions.rejected': { color: 'green', text: 'Số session bị từ chối sau khi vượt quá số lượng session tối đa đã cấu hình', unit: '' },
    };
  }
};

/**
 * Truy vấn số lượng CPU
 * @param params
 */
export const getServerInfo = (infoType) => {
  if (infoType == '1') {
    return Promise.all([getCpuCount(), getCpuUsage(), getProcessStartTime(), getProcessUptime(), getProcessCpuUsage()]);
  }
  if (infoType == '2') {
    return Promise.all([
      getJvmMemoryMax(),
      getJvmMemoryCommitted(),
      getJvmMemoryUsed(),
      getJvmBufferCount(),
      getJvmBufferMemoryUsed(),
      getJvmThreadsDaemon(),
      getJvmThreadsLive(),
      getJvmThreadsPeak(),
      getJvmClassesLoaded(),
      getJvmClassesUnloaded(),
      getJvmGcLiveDataSize(),
      getJvmGcMaxDataSize(),
      getJvmGcMemoryAllocated(),
      getJvmGcMemoryPromoted(),
      getJvmGcPause(),
    ]);
  }
  if (infoType == '3') {
    return Promise.all([
      getTomcatSessionsActiveCurrent(),
      getTomcatSessionsActiveMax(),
      getTomcatSessionsCreated(),
      getTomcatSessionsExpired(),
      getTomcatSessionsRejected(),
    ]);
  }
  if (infoType == '5') {
    return Promise.all([getMemoryInfo()]);
  }
  // Giám sát Undertow
  if (infoType == '6') {
    return Promise.all([
      getUndertowSessionsActiveCurrent(),
      getUndertowSessionsActiveMax(),
      getUndertowSessionsCreated(),
      getUndertowSessionsExpired(),
    ]);
  }
};
