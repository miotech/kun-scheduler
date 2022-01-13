import React, { memo, useMemo } from 'react';
import useI18n from '@/hooks/useI18n';
import moment from 'moment';
import { EditorDiff } from '@/components/CodeEditor/EditorDiff';
import { UserOutlined, LeftOutlined } from '@ant-design/icons';
import { TaskCommit } from '@/definitions/TaskDefinition.type';
import { UsernameText } from '@/components/UsernameText';
import Styles from './Diffversion.less';
import { keysMap, valuesMap, findKeys } from './consts/HistoryVersion';

interface Props {
  setView: (taskCommit: TaskCommit) => void;
  diffRows: TaskCommit[];
  goBack: (arg: string) => void;
}

export const DiffVersion: React.FC<Props> = memo(props => {
  const t = useI18n();
  const { setView, diffRows, goBack } = props;
  const leftDiff = diffRows[0];
  const rightDiff = diffRows[1];

  const objKeys = useMemo(() => {
    const keys = findKeys(leftDiff?.snapshot?.taskPayload);
    return keys;
  }, [leftDiff?.snapshot?.taskPayload]);

  const replacer = (key: string, value: string) => {
    if (valuesMap[key] && valuesMap[key][value]) {
      return t(valuesMap[key][value]);
    }
    return value;
  };
  const original = useMemo(() => {
    const taskPayload = leftDiff?.snapshot?.taskPayload;
    let res = JSON.stringify(taskPayload, replacer, 2);

    objKeys.forEach((item: string) => {
      if (keysMap[item]) {
        res = res.replace(`"${item}":`, `"${t(keysMap[item])}":`);
      }
    });
    return res;
  }, [leftDiff?.snapshot?.taskPayload]);

  const modified = useMemo(() => {
    const taskPayload = rightDiff?.snapshot?.taskPayload;
    let res = JSON.stringify(taskPayload, replacer, 2);

    objKeys.forEach((item: any) => {
      if (keysMap[item]) {
        res = res.replace(`"${item}":`, `"${t(keysMap[item])}":`);
      }
    });
    return res;
  }, [rightDiff?.snapshot?.taskPayload]);
  return (
    <div className={Styles.content}>
      <div className={Styles.back} onClick={() => goBack('history')}>
        <LeftOutlined />
        &nbsp;{t('dataDevelopment.definition.version.backVersionList')}
      </div>
      <div className={Styles.top}>
        <div className={Styles.item}>
          <div className={Styles.left}>
            <span className={Styles.version}>
              {leftDiff.version} {leftDiff.latestCommit ? `(${t('dataDevelopment.definition.version.current')})` : ''}
            </span>{' '}
            <span onClick={() => setView(leftDiff)} className={Styles.viewVersion}>
              {t('dataDevelopment.definition.version.viewVersion')}
            </span>
          </div>
          <div className={Styles.right} style={{ marginRight: '20px' }}>
            <span className={Styles.userName}>
              <UserOutlined />
              &nbsp;
              <UsernameText userId={leftDiff?.committer} />
            </span>
            <span className={Styles.time}>
              {leftDiff?.committedAt ? moment(leftDiff.committedAt).format('YYYY-MM-DD HH:mm:ss') : '...'}
            </span>
          </div>
        </div>
        <div className={Styles.item}>
          <div className={Styles.left}>
            <span className={Styles.version}>
              {rightDiff.version} {rightDiff.latestCommit ? `${t('dataDevelopment.definition.version.current')}` : ''}
            </span>{' '}
            <span onClick={() => setView(rightDiff)} className={Styles.viewVersion}>
              {t('dataDevelopment.definition.version.viewVersion')}
            </span>
          </div>
          <div className={Styles.right}>
            <span className={Styles.userName}>
              <UserOutlined />
              &nbsp;
              <UsernameText userId={rightDiff?.committer} />
            </span>
            <span className={Styles.time}>
              {rightDiff?.committedAt ? moment(rightDiff.committedAt).format('YYYY-MM-DD HH:mm:ss') : '...'}
            </span>
          </div>
        </div>{' '}
      </div>
      <EditorDiff original={original} modified={modified} />
    </div>
  );
});
