import path from 'path';
import fs from 'fs';

const root = path.join(__dirname, '../../');
const electronDistRoot = path.join(root, 'dist/electron');

let yamlName = 'electron-builder.yaml';
const sourcePath = fs.readFileSync(path.join(root, yamlName), 'utf-8');

try {
  // Khớp appId và productName bằng biểu thức chính quy
  const appIdMatch = sourcePath.match(/appId:\s*['"]([^'"]+)['"]/);
  const productNameMatch = sourcePath.match(/productName:\s*['"]([^'"]+)['"]/);
  if (appIdMatch && productNameMatch) {
    const fileContent = `${appIdMatch[0]}\n${productNameMatch[0]}`;
    yamlName = 'env.yaml';
    const targetPath = path.join(electronDistRoot, yamlName);
    fs.writeFileSync(targetPath, fileContent, 'utf-8');
    console.log(`✨ write dist ${yamlName} successfully.`);
  } else {
    throw new Error('appId or productName not found');
  }
} catch (e) {
  console.error(e);
  console.error(`Vui lòng kiểm tra ${yamlName} có tồn tại hay không, hoặc nội dung có đúng không`);
  process.exit(1);
}
