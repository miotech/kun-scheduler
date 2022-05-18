import React, { useState } from 'react';
import { Radio, Space, Button, message } from 'antd';
import { GlossaryDetail as IGlossaryDetail } from '@/rematch/models/glossary';
import { copyGlossaryService } from '@/services/glossary';
import useI18n from '@/hooks/useI18n';
import { getLocale } from 'umi';
import styles from './PasteContent.less';

interface Props {
  glossarySticky: IGlossaryDetail | null;
  currentGlossaryDetail: IGlossaryDetail;
  addChild: (child: {}, parentId: string, id?: string) => void;
  setVisible: (value: boolean) => void;
}
export const PasteContent = (props: Props) => {
  const currentLocal = getLocale();

  const t = useI18n();
  const [copyOperation, setCopyOperation] = useState('ONLY_ONESELF');
  const { glossarySticky, currentGlossaryDetail, addChild, setVisible } = props;

  const pasteGlossary = async (type: string) => {
    if (glossarySticky) {
      const params = {
        parentId: type === 'child' ? currentGlossaryDetail.id : currentGlossaryDetail.parentId,
        sourceId: glossarySticky.id,
        copyOperation,
      };
      const res = await copyGlossaryService(params);
      if (res) {
        message.success(t('common.operateSuccess'));
        const copyGlossary = res.children;
        if (type === 'child') {
          addChild(copyGlossary, currentGlossaryDetail?.id, copyGlossary[0].id);
        } else {
          addChild(copyGlossary, currentGlossaryDetail?.parentId, copyGlossary[0].id);
        }
      }
    }
  };
  return (
    <div className={styles.pasteContent} style={{ width: currentLocal === 'zh-CN' ? '306px' : '356px' }}>
      <div className={styles.title}>
        {t('common.button.copy')}: {glossarySticky?.name}
      </div>
      <Radio.Group value={copyOperation} onChange={e => setCopyOperation(e.target.value)}>
        <Space direction="vertical">
          <Radio value="ONLY_ONESELF">{t('glossary.copyOperation.oneself')}</Radio>
          <Radio value="ONLY_CHILDREN">{t('glossary.copyOperation.children')}</Radio>
          <Radio value="CONTAINS_CHILDREN">{t('glossary.copyOperation.containsChildren')}</Radio>
        </Space>
      </Radio.Group>

      <div className={styles.title}>
        {t('common.button.paste')}: {currentGlossaryDetail?.name}
      </div>
      <Radio.Group>
        <Space direction="vertical">
          {/* <Radio disabled>{t('glossary.paste.cover')}</Radio> */}
          <Radio checked>{t('glossary.paste.append')}</Radio>
        </Space>
      </Radio.Group>
      <div className={styles.bottom}>
        <Button className={styles.btn} type="text" onClick={() => setVisible(false)}>
          {t('common.button.cancel')}
        </Button>
        <Button
          className={styles.btn}
          type="primary"
          ghost
          disabled={!glossarySticky}
          onClick={() => pasteGlossary('parent')}
        >
          {t('glossary.paste.brother')}
        </Button>
        <Button
          className={styles.btn}
          type="primary"
          ghost
          disabled={!glossarySticky}
          onClick={() => pasteGlossary('child')}
        >
          {t('glossary.paste.child')}
        </Button>
      </div>
    </div>
  );
};
