export interface DropMenu {
  onClick?: Fn;
  to?: string;
  icon?: string;
  event: string | number;
  text: string;
  disabled?: boolean;
  // Có ẩn hay không
  hide?: boolean;
  divider?: boolean;
}
