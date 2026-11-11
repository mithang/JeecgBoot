import {add} from "/@/components/Form/src/componentMap";
import LinkTableSelect from '../../extend/linkTable/LinkTableSelect.vue';
import LinkTableCard from '../../extend/linkTable/LinkTableCard.vue';
import OnlineSelectCascade from '../../auto/comp/OnlineSelectCascade.vue';

const componentKeyMap = {};

/**
 * Dùng để sangformthêm component trong đó
 */
export function useExtendComponent() {

  addComponent('OnlineSelectCascade', OnlineSelectCascade);
  addComponent('LinkTableSelect', LinkTableSelect);
  addComponent('LinkTableCard', LinkTableCard);

  /**
   * Tránh thêm trùng lặp
   */
  function addComponent(key, comp) {
    if(!componentKeyMap[key]){
      add(key, comp)
      componentKeyMap[key] = 1;
    }
  }

  /**
   * Control truy vấn của bản ghi liên kết không nên dùng chế độ thẻ Thống nhất sử dụng chế độ dropdown 
   */
  function linkTableCard2Select(schema) {
    if("LinkTableCard"==schema.component){
      schema.component = 'LinkTableSelect';
      schema.componentProps.popContainer = 'body';
    }
  }

  return {
    addComponent,
    linkTableCard2Select
  }
}