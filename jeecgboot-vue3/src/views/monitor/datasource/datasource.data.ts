import { BasicColumn, FormSchema } from '/@/components/Table';

const dbDriverMap = {
  // Cơ sở dữ liệu MySQL
  '1': { dbDriver: 'com.mysql.jdbc.Driver' },
  // Cơ sở dữ liệu MySQL 5.7+
  '4': { dbDriver: 'com.mysql.cj.jdbc.Driver' },
  // Oracle
  '2': { dbDriver: 'oracle.jdbc.OracleDriver' },
  // Cơ sở dữ liệu SQLServer
  '3': { dbDriver: 'com.microsoft.sqlserver.jdbc.SQLServerDriver' },
  // Cơ sở dữ liệu MariaDB
  '5': { dbDriver: 'org.mariadb.jdbc.Driver' },
  // Cơ sở dữ liệu PostgreSQL
  '6': { dbDriver: 'org.postgresql.Driver' },
  // Cơ sở dữ liệu Dameng
  '7': { dbDriver: 'dm.jdbc.driver.DmDriver' },
  // Cơ sở dữ liệu Kingbase
  '8': { dbDriver: 'com.kingbase8.Driver' },
  // Cơ sở dữ liệu ShenTong
  '9': { dbDriver: 'com.oscar.Driver' },
  // Cơ sở dữ liệu SQLite
  '10': { dbDriver: 'org.sqlite.JDBC' },
  // Cơ sở dữ liệu DB2
  '11': { dbDriver: 'com.ibm.db2.jcc.DB2Driver' },
  // Cơ sở dữ liệu Hsqldb
  '12': { dbDriver: 'org.hsqldb.jdbc.JDBCDriver' },
  // Cơ sở dữ liệu Derby
  '13': { dbDriver: 'org.apache.derby.jdbc.ClientDriver' },
  // Cơ sở dữ liệu H2
  '14': { dbDriver: 'org.h2.Driver' },
  // Cơ sở dữ liệu khác
  '15': { dbDriver: '' },
};
const dbUrlMap = {
  // Cơ sở dữ liệu MySQL
  '1': { dbUrl: 'jdbc:mysql://127.0.0.1:3306/jeecg-boot?characterEncoding=UTF-8&useUnicode=true&useSSL=false' },
  // Cơ sở dữ liệu MySQL 5.7+
  '4': {
    dbUrl:
      'jdbc:mysql://127.0.0.1:3306/jeecg-boot?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai',
  },
  // Oracle
  '2': { dbUrl: 'jdbc:oracle:thin:@127.0.0.1:1521:ORCL' },
  // Cơ sở dữ liệu SQLServer
  '3': { dbUrl: 'jdbc:sqlserver://127.0.0.1:1433;SelectMethod=cursor;DatabaseName=jeecgboot' },
  // Cơ sở dữ liệu MariaDB
  '5': { dbUrl: 'jdbc:mariadb://127.0.0.1:3306/jeecg-boot?characterEncoding=UTF-8&useSSL=false' },
  // Cơ sở dữ liệu PostgreSQL
  '6': { dbUrl: 'jdbc:postgresql://127.0.0.1:5432/jeecg-boot' },
  // Cơ sở dữ liệu Dameng
  '7': { dbUrl: 'jdbc:dm://127.0.0.1:5236/?jeecg-boot&zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=utf-8' },
  // Cơ sở dữ liệu Kingbase
  '8': { dbUrl: 'jdbc:kingbase8://127.0.0.1:54321/jeecg-boot' },
  // Cơ sở dữ liệu ShenTong
  '9': { dbUrl: 'jdbc:oscar://192.168.1.125:2003/jeecg-boot' },
  // Cơ sở dữ liệu SQLite
  '10': { dbUrl: 'jdbc:sqlite://opt/test.db' },
  // Cơ sở dữ liệu DB2
  '11': { dbUrl: 'jdbc:db2://127.0.0.1:50000/jeecg-boot' },
  // Cơ sở dữ liệu Hsqldb
  '12': { dbUrl: 'jdbc:hsqldb:hsql://127.0.0.1/jeecg-boot' },
  // Cơ sở dữ liệu Derby
  '13': { dbUrl: 'jdbc:derby://127.0.0.1:1527/jeecg-boot' },
  // Cơ sở dữ liệu H2
  '14': { dbUrl: 'jdbc:h2:tcp://127.0.0.1:8082/jeecg-boot' },
  // Cơ sở dữ liệu khác
  '15': { dbUrl: '' },
};

export const columns: BasicColumn[] = [
  {
    title: 'Mã nguồn dữ liệu',
    dataIndex: 'code',
    width: 180,
    align: 'left',
  },
  {
    title: 'Tên nguồn dữ liệu',
    dataIndex: 'name',
    width: 200,
    align: 'left',
  },
  {
    title: 'Loại cơ sở dữ liệu',
    dataIndex: 'dbType_dictText',
    width: 200,
  },
  {
    title: 'Lớp driver',
    dataIndex: 'dbDriver',
    width: 200,
  },
  {
    title: 'Địa chỉ nguồn dữ liệu',
    dataIndex: 'dbUrl',
  },
  {
    title: 'Tên người dùng',
    dataIndex: 'dbUsername',
    width: 200,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'name',
    label: 'Tên nguồn dữ liệu',
    component: 'JInput',
    colProps: { span: 8 },
  },
  {
    field: 'dbType',
    label: 'Loại cơ sở dữ liệu',
    component: 'JDictSelectTag',
    colProps: { span: 8 },
    componentProps: () => {
      return {
        dictCode: 'database_type',
      };
    },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'code',
    label: 'Mã nguồn dữ liệu',
    component: 'Input',
    required: true,
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
  },
  {
    field: 'name',
    label: 'Tên nguồn dữ liệu',
    component: 'Input',
    required: true,
  },
  {
    field: 'dbType',
    label: 'Loại cơ sở dữ liệu',
    component: 'JDictSelectTag',
    required: true,
    componentProps: ({ formModel }) => {
      return {
        dictCode: 'database_type',
        onChange: (e: any) => {
          formModel = Object.assign(formModel, dbDriverMap[e], dbUrlMap[e]);
        },
      };
    },
  },
  {
    field: 'dbDriver',
    label: 'Lớp driver',
    required: true,
    component: 'Input',
  },
  {
    field: 'dbUrl',
    label: 'Địa chỉ nguồn dữ liệu',
    required: true,
    component: 'Input',
  },
  {
    field: 'dbUsername',
    label: 'Tên người dùng',
    required: true,
    component: 'Input',
  },
  {
    field: 'dbPassword',
    label: 'Mật khẩu',
    component: 'InputPassword',
    slot: 'pwd',
  },
  {
    field: 'remark',
    label: 'Ghi chú',
    component: 'InputTextArea',
  },
];
