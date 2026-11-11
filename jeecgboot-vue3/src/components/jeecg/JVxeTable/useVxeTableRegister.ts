// Cho trang sử dụng gốcvxe-tableđăng ký trang củavxe-tableComponent
export const useVxeTableRegister = async () => {
  const app = window['JAppRootInstance'];
  if (app._context.components.VxeTable) {
    // Đã đăng ký toàn cục
  } else {
    const { registerJVxeTable } = await import('/@/components/jeecg/JVxeTable');
    await registerJVxeTable(app);
    const { registerJVxeCustom } = await import('/@/components/JVxeCustom');
    await registerJVxeCustom();
  }
}
