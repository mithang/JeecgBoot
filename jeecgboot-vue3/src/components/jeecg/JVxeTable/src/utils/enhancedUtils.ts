import { useDefaultEnhanced } from '../hooks/useJVxeComponent';
import { isFunction, isObject, isString } from '/@/utils/is';
import { JVxeTypes } from '../types';
import { JVxeComponent } from '../types/JVxeComponent';
import { componentMap } from '../componentMap';

// Phần tăng cường component đã đăng ký
const enhancedMap = new Map<JVxeTypes, JVxeComponent.Enhanced>();

/**
 * Lấy phần tăng cường của một component
 * @param type JVxeTypes
 */
export function getEnhanced(type: JVxeTypes | string): JVxeComponent.Enhanced {
  let $type: JVxeTypes = <JVxeTypes>type;
  if (!enhancedMap.has($type)) {
    let defaultEnhanced = useDefaultEnhanced();
    if (componentMap.has($type)) {
      let enhanced = componentMap.get($type)?.enhanced ?? {};
      if (isObject(enhanced)) {
        Object.keys(defaultEnhanced).forEach((key) => {
          let def = defaultEnhanced[key];
          if (enhanced.hasOwnProperty(key)) {
            // Nếu phương thức đã tồn tại thì không ghi đè
            if (!isFunction(def) && !isString(def)) {
              enhanced[key] = Object.assign({}, def, enhanced[key]);
            }
          } else {
            enhanced[key] = def;
          }
        });
        enhancedMap.set($type, <JVxeComponent.Enhanced>enhanced);
        return <JVxeComponent.Enhanced>enhanced;
      }
    } else {
      throw new Error(`[JVxeTable] ${$type} Component chưa được đăng ký, lấy tăng cường thất bại`);
    }
    enhancedMap.set($type, <JVxeComponent.Enhanced>defaultEnhanced);
  }
  return <JVxeComponent.Enhanced>enhancedMap.get($type);
}

/** Phương thức hỗ trợ: thay thế${...}Biến */
export function replaceProps(col, value) {
  if (value && typeof value === 'string') {
    let text = value;
    text = text.replace(/\${title}/g, col.title);
    text = text.replace(/\${key}/g, col.key);
    text = text.replace(/\${defaultValue}/g, col.defaultValue);
    return text;
  }
  return value;
}


