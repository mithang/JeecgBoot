import md5 from 'md5';
//Chuỗi khóa ký(Frontend và backend phải nhất quán, khi phát hành chính thức vui lòng tự sửa)
const signatureSecret = 'dd05f1c54d63749eda95f9fa6d49v442a';

export default class signMd5Utils {
  /**
   * jsonTham số tăng dần
   * @param jsonObj Tham số gửi
   */

  static sortAsc(jsonObj) {
    let arr = new Array();
    let num = 0;
    for (let i in jsonObj) {
      arr[num] = i;
      num++;
    }
    let sortArr = arr.sort();
    let sortObj = {};
    for (let i in sortArr) {
      sortObj[sortArr[i]] = jsonObj[sortArr[i]];
    }
    return sortObj;
  }

  /**
   * @param url của yêu cầuurl,Nên chứa tham số yêu cầu(urlcủa?Tham số phía sau)
   * @param requestParams Tham số yêu cầu(@RequestParam(get)củaJSONTham số)
   * @param requestBodyParams Tham số yêu cầu(@RequestBody(post)Tham số)
   * @returns {string} Lấy chữ ký
   */
  static getSign(url, requestParams, requestBodyParams) {
    let urlParams = this.parseQueryString(url);
    let jsonObj = this.mergeObject(urlParams, requestParams);
    // Giải thích logic mã: 【QQYUN-9005]Gửi SMS kèm chữ ký---
    if(requestBodyParams){
      jsonObj = this.mergeObject(jsonObj, requestBodyParams)
    }
    let requestBody = this.sortAsc(jsonObj);
    delete requestBody._t;
    // console.log('sign requestBody:', requestBody);
    return md5(JSON.stringify(requestBody) + signatureSecret).toUpperCase();
  }

  /**
   * @param url của yêu cầuurl
   * @returns {{}} Sẽurllắp ráp tham số yêu cầu trong thànhjsonĐối tượng(urlcủa?Tham số phía sau)
   */
  static parseQueryString(url) {
    let urlReg = /^[^\?]+\?([\w\W]+)$/,
      paramReg = /([^&=]+)=([\w\W]*?)(&|$|#)/g,
      urlArray = urlReg.exec(url),
      result = {};

    // LấyURLBiến tham số cuối cùng có dấu phẩy sys/dict/getDictItems/sys_user,realname,username
    //[Ở đây điều kiện không cóencode]Ví dụ tham số có điều kiện:/sys/dict/getDictItems/sys_user,realname,id,username!='admin'%20order%20by%20create_time
    let lastpathVariable = url.substring(url.lastIndexOf('/') + 1);
    if (lastpathVariable.includes(',')) {
      if (lastpathVariable.includes('?')) {
        lastpathVariable = lastpathVariable.substring(0, lastpathVariable.indexOf('?'));
      }
      //Giải quyếtSign Kiểm tra chữ ký thất bại #2728
      //decodeURIKhông có khả năng mã hóa và giải mã ký tự đặc biệt, cần dùngdecodeURIComponent
      result['x-path-variable'] = decodeURIComponent(lastpathVariable);
    }
    if (urlArray && urlArray[1]) {
      let paramString = urlArray[1],
        paramResult;
      while ((paramResult = paramReg.exec(paramString)) != null) {
        //Chuyển giá trị số thànhstringLoại, quy tắc mã hóa frontend và backend giữ nhất quán
        if (this.myIsNaN(paramResult[2])) {
          paramResult[2] = paramResult[2].toString();
        }
        result[paramResult[1]] = paramResult[2];
      }
    }
    return result;
  }

  /**
   * @returns {*} Gộp hai đối tượng thành một
   */
  static mergeObject(objectOne, objectTwo) {
    if (objectTwo && Object.keys(objectTwo).length > 0) {
      for (let key in objectTwo) {
        if (objectTwo.hasOwnProperty(key) === true) {
          //Chuyển giá trị số thànhstringLoại, quy tắc mã hóa frontend và backend giữ nhất quán
          if (this.myIsNaN(objectTwo[key])) {
            objectTwo[key] = objectTwo[key].toString();
          }
          //Chuyển kiểu boolean thànhstringLoại, quy tắc mã hóa frontend và backend giữ nhất quán
          if (typeof objectTwo[key] === 'boolean') {
            objectTwo[key] = objectTwo[key].toString();
          }
          objectOne[key] = objectTwo[key];
        }
      }
    }
    return objectOne;
  }

  static urlEncode(param, key, encode) {
    if (param == null) return '';
    let paramStr = '';
    let t = typeof param;
    if (t == 'string' || t == 'number' || t == 'boolean') {
      paramStr += '&' + key + '=' + (encode == null || encode ? encodeURIComponent(param) : param);
    } else {
      for (let i in param) {
        let k = key == null ? i : key + (param instanceof Array ? '[' + i + ']' : '.' + i);
        paramStr += this.urlEncode(param[i], k, encode);
      }
    }
    return paramStr;
  }

  /**
   * Dùng để ký API Tạoheaderdấu thời gian trong
   * @returns {number}
   */
  static getTimestamp() {
    return new Date().getTime();
  }

  // static getDateTimeToString() {
  //   const date_ = new Date()
  //   const year = date_.getFullYear()
  //   let month = date_.getMonth() + 1
  //   let day = date_.getDate()
  //   if (month < 10) month = '0' + month
  //   if (day < 10) day = '0' + day
  //   let hours = date_.getHours()
  //   let mins = date_.getMinutes()
  //   let secs = date_.getSeconds()
  //   const msecs = date_.getMilliseconds()
  //   if (hours < 10) hours = '0' + hours
  //   if (mins < 10) mins = '0' + mins
  //   if (secs < 10) secs = '0' + secs
  //   if (msecs < 10) secs = '0' + msecs
  //   return year + '' + month + '' + day + '' + hours + '' + mins + '' + secs
  // }
  // true:kiểu số,false: Không phải kiểu số
  static myIsNaN(value) {
    return typeof value === 'number' && !isNaN(value);
  }
}
