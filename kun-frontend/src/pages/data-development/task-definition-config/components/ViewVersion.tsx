import React, { useState, memo, useMemo } from 'react';
import { useParams, Link } from 'umi';
import MonacoEditor from '@monaco-editor/react';
import { UserOutlined, LeftOutlined, RightOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import useI18n from '@/hooks/useI18n';
import moment from 'moment';
import { Descriptions } from 'antd';
import { TaskCommit } from '@/definitions/TaskDefinition.type';
import { UsernameText } from '@/components/UsernameText';
import Styles from './ViewVersion.less';
import { keysMap, valuesMap, findKeys } from './consts/HistoryVersion';

interface Props {
  setView: (taskCommit: TaskCommit) => void;
  setDiff: (taskCommits: TaskCommit[]) => void;
  viewTaskCommit: TaskCommit | null;
  taskCommits: TaskCommit[];
  goBack: () => void;
}
export const ViewVersion: React.FC<Props> = memo(props => {
  const params = useParams<{ taskDefId: string }>();
  const { setDiff, taskCommits, viewTaskCommit, goBack } = props;
  const [taskCommit, setTaskCommit] = useState<TaskCommit | null>(viewTaskCommit);
  const t = useI18n();
  const snapshot = taskCommit?.snapshot;
  const latestCommit = taskCommits.find((item: TaskCommit) => item.latestCommit === true) || null;
  const currentIndex = taskCommits.findIndex((item: TaskCommit) => item.version === taskCommit?.version);
  const setCommit = (index: number) => {
    if (index < 0 || index >= taskCommits.length) {
      return;
    }
    setTaskCommit(taskCommits[index]);
  };
  const replacer = useMemo(
    () => (key: string, value: string) => {
      if (valuesMap[key] && valuesMap[key][value]) {
        return t(valuesMap[key][value]);
      }
      return value;
    },
    [t],
  );
  const objKeys = useMemo(() => {
    const keys = findKeys(snapshot);
    return keys;
  }, [snapshot]);

  const value = useMemo(() => {
    const taskPayload = snapshot?.taskPayload;
    let res = JSON.stringify(taskPayload, replacer, 2);

    objKeys.forEach((item: string) => {
      if (keysMap[item]) {
        res = res.replace(`"${item}":`, `"${t(keysMap[item])}":`);
      }
    });
    return res;
  }, [snapshot, objKeys, replacer, t]);
  return (
    <div className={Styles.content}>
      <div style={{ backgroundColor: '#fff', padding: '20px 30px' }}>
        <div className={Styles.header}>
          <div className={Styles.back} onClick={goBack}>
            <LeftOutlined />
            &nbsp;{t('dataDevelopment.definition.version.backVersionList')}
          </div>
          {!taskCommit?.latestCommit && (
            <div>
              <ExclamationCircleOutlined />
              <span className={Styles.txt}>
                {t('dataDevelopment.definition.version.versionNotice')}
                <span onClick={() => setTaskCommit(latestCommit)} className={Styles.link}>
                  {t('dataDevelopment.definition.version.currentVersion')}
                </span>
                。 &nbsp;&nbsp;
                <span
                  className={Styles.link}
                  onClick={() => {
                    setDiff([latestCommit as TaskCommit, taskCommit as TaskCommit]);
                  }}
                >
                  {t('dataDevelopment.definition.version.compareCurrentVersion')}
                </span>
              </span>
            </div>
          )}
        </div>
        <div className={Styles.con}>
          <div className={Styles.between} onClick={() => setCommit(currentIndex + 1)}>
            <LeftOutlined />
          </div>
          <div className={Styles.center}>
            <div className={Styles.version}>{taskCommit?.version}</div>
            <div className={Styles.name}>
              <UserOutlined /> <UsernameText owner={taskCommit?.committer} />{' '}
            </div>
            <div className={Styles.tiem}>
              {taskCommit?.committedAt ? moment(taskCommit.committedAt).format('YYYY-MM-DD HH:mm:ss') : '...'}
            </div>
            <div className={Styles.line}> </div>
            {!taskCommit?.latestCommit && (
              <Link
                to={`/data-development/task-definition/${params.taskDefId}?taskPayload=${encodeURIComponent(
                  JSON.stringify(snapshot?.taskPayload),
                )}`}
                target="_blank"
                className={Styles.rollback}
              >
                {t('dataDevelopment.definition.version.rollbackVersion')}
              </Link>
            )}
          </div>

          <div
            className={Styles.between}
            onClick={e => {
              e.preventDefault();
              setCommit(currentIndex - 1);
            }}
          >
            <RightOutlined />
          </div>
        </div>
      </div>
      <div className={Styles.detail}>
        <div className={Styles.TaskDefMetas}>
          <Descriptions column={2}>
            {/* Task template name */}
            <Descriptions.Item label={t('dataDevelopment.definition.property.taskTemplateName')}>
              {taskCommit?.snapshot?.taskTemplateName || '-'}
            </Descriptions.Item>
            {/* committedAt */}
            <Descriptions.Item label={t('dataDevelopment.definition.committedAt')}>
              {taskCommit?.committedAt ? moment(taskCommit.committedAt).format('YYYY-MM-DD HH:mm:ss') : '...'}
            </Descriptions.Item>
            {/* Owner */}
            <Descriptions.Item label={t('dataDevelopment.definition.property.owner')}>
              {taskCommit?.snapshot?.owner ? <UsernameText owner={taskCommit?.snapshot?.owner} /> : '...'}
            </Descriptions.Item>
            {/* Last committer */}
            <Descriptions.Item label={t('dataDevelopment.definition.committer')}>
              <UsernameText owner={taskCommit?.committer} />
            </Descriptions.Item>
          </Descriptions>
        </div>
      </div>
      <div style={{ padding: '10px 30px', backgroundColor: '#fff' }}>
        <MonacoEditor height="500px" language="json" value={value} theme="vs-dark" />
      </div>
    </div>
  );
});
