import React, { useCallback, useState, useEffect } from 'react';
import { Button, Form, Input, Select } from 'antd';
import { BasicInformation } from './components/basicInformation';
import { ConnectConfig } from './components/connectConfig';

import styles from './index.less';

export default function DataSettingDetail() {
  const [form] = Form.useForm();

  return (
    <div className={styles.container}>
      <BasicInformation form={form} />
      <ConnectConfig form={form} />
      <div className={styles.tips}>

        <div className={styles.title}>
          温馨提示
        </div>

        <div className={styles.content}>
          1.仅管理员和数据源创建者可对基本信息做修改；<br />

          2.仅数据连接创建者本人可对连接信息做修改；；<br />

          3.授权使用选择“所有人”时，新添加的kun用户默认被被授权使用
        </div>
      </div>
    </div>
  );
};