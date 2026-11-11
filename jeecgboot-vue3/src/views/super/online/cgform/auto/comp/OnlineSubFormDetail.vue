<template>
  <detail-form :schemas="detailFormSchemas" :data="subFormData" :span="formSpan"></detail-form>
</template>

<script lang="ts">
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ref, watch } from 'vue';
  import { BasicForm } from '/@/components/Form/index';
  import { defHttp } from '/@/utils/http/axios';
  import { getRefPromise } from '../../hooks/auto/useAutoForm';
  import { Loading } from '/@/components/Loading';
  import DetailForm from '../../extend/form/DetailForm.vue';
  import { getDetailFormSchemas } from '../../hooks/auto/useAutoForm';
  import { getLinkTableColumns, transLinkTableRecord } from '../../extend/linkTable/useLinkTable';

  const baseUrl = '/online/cgform/api/subform';
  export default {
    name: 'OnlineSubFormDetail',
    components: {
      BasicForm,
      Loading,
      DetailForm,
    },
    props: {
      properties: {
        type: Object,
        required: true,
      },
      mainId: {
        type: String,
        default: '',
      },
      table: {
        type: String,
        default: '',
      },
      formTemplate: {
        type: Number,
        default: 1,
      },
    },
    emits: ['formChange'],
    setup(props) {
      // Biểu mẫu đã render xong hay chưa
      const formRendered = ref(false);
      const { createMessage: $message } = useMessage();
      const tableName = ref('');
      const subFormData = ref<any>({});
      const { detailFormSchemas, createFormSchemas, formSpan } = getDetailFormSchemas(props);

      watch(
        () => props.table,
        () => {
          tableName.value = props.table;
        },
        { immediate: true }
      );

      //Lắng nghe sự kiện thay đổi cấu hình
      watch(
        () => props.properties,
        () => {
          //Render lại biểu mẫu
          console.log('bảng chínhpropertiesThay đổi', props.properties);
          formRendered.value = false;
          createFormSchemas(props.properties);
          formRendered.value = true;
        },
        { deep: true, immediate: true }
      );

      //Lắng nghe dữ liệu bảng chínhID
      watch(
        () => props.mainId,
        () => {
          //Tải lại dữ liệu bảng con
          console.log('bảng chínhIDThay đổi', props.mainId);
          // Độ trễ tại đây100mili giây là để chopropertiesViệc lắng nghe của thực thi trước
          setTimeout(() => {
            resetSubForm();
          }, 100);
        },
        { immediate: true }
      );

      /**
       * Khi dữ liệu bảng chínhIDxảy ra thay đổi, bảng con lấy lại dữ liệu
       * @returns {Promise<void>}
       */
      async function resetSubForm() {
        await getRefPromise(formRendered);
        subFormData.value = {};
        const { table, mainId } = props;
        if (!table || !mainId) {
          return;
        }
        // update-begin--author:liaozhiyang---date:20260413---for：【QQYUN-14951】Chi tiết trường bảng khác một-một không có giá trị
        let data: any = (await loadData(table, mainId)) || {};
        await fillLinkTableFields(data);
        subFormData.value = data;
        // update-end--author:liaozhiyang---date:20260413---for：【QQYUN-14951】Chi tiết trường bảng khác một-một không có giá trị
      }
      // update-begin--author:liaozhiyang---date:20260413---for：【QQYUN-14951】Chi tiết trường bảng khác một-một không có giá trị
      /**
       * Trong bảng con một-một của trang chi tiết, căn cứ theo trường bản ghi liên kết(link_table)giá trị của để truy vấn dữ liệu liên kết, tự động điền trường của bảng khác(link_table_field)giá trị của
       */
      async function fillLinkTableFields(data) {
        const schemas = detailFormSchemas.value;
        for (const schema of schemas) {
          if (schema.view === 'link_table' && (schema as any).linkFields?.length > 0) {
            const fieldValue = data[schema.field];
            if (fieldValue) {
              const linkFields = (schema as any).linkFields as string[];
              const valueField = (schema as any).dictCode || 'id';
              const vals = String(fieldValue).split(',');
              const selectFields = [...new Set([valueField, ...linkFields.map((item) => item.split(',')[1]).filter(Boolean)])].join(',');
              const params = {
                pageSize: vals.length,
                pageNo: 1,
                superQueryMatchType: 'and',
                superQueryParams: encodeURI(JSON.stringify([{ field: valueField, rule: 'in', val: fieldValue }])),
              };
              try {
                // update-begin--author:liusq---date:20260811---for：【onlineChi tiết một-một】Trường bảng khác dùng lại logic chuyển đổi của bản ghi liên kết
                const linkTableName = (schema as any).dictTable;
                const [result, columns] = await Promise.all([
                  defHttp.get({ url: '/online/cgform/api/getData/' + linkTableName, params }),
                  getLinkTableColumns(linkTableName, selectFields),
                ]);
                const records = result?.records || [];
                records.forEach((record) => transLinkTableRecord(record, columns?.columns, columns?.dictOptions));
                for (const linkField of linkFields) {
                  const [formField, tableField] = linkField.split(',');
                  if (records.length > 0) {
                    data[formField] = records.map((r: any) => r[tableField] ?? '').join(',');
                  } else {
                    data[formField] = '';
                  }
                }
                // update-end--author:liusq---date:20260811---for：【onlineChi tiết một-một】Trường bảng khác dùng lại logic chuyển đổi của bản ghi liên kết
              } catch (e) {
                console.warn('Điền trường bảng khác thất bại:', e);
              }
            }
          }
        }
      }
      // update-end--author:liaozhiyang---date:20260413---for：【QQYUN-14951】Chi tiết trường bảng khác một-một không có giá trị
      

      async function loadData(table, mainId) {
        let url = `${baseUrl}/${table}/${mainId}`;
        return new Promise((resolve, reject) => {
          defHttp.get({ url }, { isTransformResponse: false }).then((res) => {
            console.log(res);
            if (res.success) {
              resolve(res.result);
            } else {
              reject(res.message);
            }
          });
        }).catch((e) => {
          console.warn('Bảng con lấy dữ liệu thất bại:', e);
          return Promise.resolve({});
        });
      }

      return {
        detailFormSchemas,
        subFormData,
        formSpan,
      };
    },
  };
</script>

<style scoped></style>
