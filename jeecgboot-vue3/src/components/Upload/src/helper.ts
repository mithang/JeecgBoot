export function checkFileType(file: File, accepts: string[]) {
  // Giải thích logic mã: 【issues/7954]Component BasicUpload tải tệp lên, giới hạn định dạng tải lên bị lỗi kiểm chứng
  const mimePatterns: string[] = [];
  const suffixList: string[] = [];
  // Xử lý theo loại accepts
  for (const item of accepts) {
    if (item.includes('/')) {
      mimePatterns.push(item);
    } else {
      // Hỗ trợ.png hoặc png (có hậu tố dấu chấm hoặc không có hậu tố dấu chấm)
      const suffix = item.startsWith('.') ? item.slice(1) : item;
      suffixList.push(suffix);
    }
  }
  // Logic khớp hậu tố
  let suffixMatch = false;
  if (suffixList.length > 0) {
    const suffixRegex = new RegExp(`\\.(${suffixList.join('|')})$`, 'i');
    suffixMatch = suffixRegex.test(file.name);
  }
  // MIMELogic khớp loại
  let mimeMatch = false;
  if (mimePatterns.length > 0 && file.type) {
    mimeMatch = mimePatterns.some((pattern) => {
      // Thoát ký tự đặc biệt trước, rồi xử lý ký tự đại diện
      const regexPattern = pattern
        .replace(/[.+?^${}()|[\]\\]/g, '\\$&') // Thoát ký tự đặc biệt trước
        .replace(/\*/g, '.*'); // Thay thế ký tự đại diện lần nữa
      const regex = new RegExp(`^${regexPattern}$`, 'i');
      return regex.test(file.type);
    });
  }
  if (mimePatterns.length && suffixList.length) {
    return suffixMatch || mimeMatch;
  } else if (mimePatterns.length) {
    return mimeMatch;
  } else if (suffixList.length) {
    return suffixMatch;
  }
}

export function checkImgType(file: File) {
  return isImgTypeByName(file.name);
}

export function isImgTypeByName(name: string) {
  return /\.(jpg|jpeg|png|gif)$/i.test(name);
}

export function getBase64WithFile(file: File) {
  return new Promise<{
    result: string;
    file: File;
  }>((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve({ result: reader.result as string, file });
    reader.onerror = (error) => reject(error);
  });
}
