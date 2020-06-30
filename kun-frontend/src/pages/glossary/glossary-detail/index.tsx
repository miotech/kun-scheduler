import React, { useCallback, useEffect, useState } from 'react';
import { useHistory } from 'umi';
import { RouteComponentProps } from 'react-router';

import { Spin, Button, Input, Modal, message } from 'antd';

import Card from '@/components/Card/Card';
import { ExclamationCircleOutlined } from '@ant-design/icons';

import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';

import {
  getInitGlossaryDetail,
  GlossaryDetail as IGlossaryDetail,
  GlossaryNode,
} from '@/rematch/models/glossary';

import GlossaryTree from '../components/GlossaryTree/GlossaryTree';
import ParentSearch from './components/ParentSearch/ParentSearch';
import ChildrenGlossaryList from './components/ChildrenGlossaryList/ChildrenGlossaryList';
import AssetList from './components/AssetList/AssetList';

import styles from './index.less';

interface MatchParams {
  glossaryId?: string;
}

interface Props extends RouteComponentProps<MatchParams> {}

const { TextArea } = Input;
const { confirm } = Modal;

export default function GlossaryDetail({ match }: Props) {
  const t = useI18n();

  const history = useHistory();

  const { selector, dispatch } = useRedux(state => state.glossary);

  const [isEditing, setIsEditing] = useState(false);

  const { currentGlossaryDetail, fetchCurrentGlossaryDetailLoading } = selector;
  const [inputtingDetail, setInputtingDetail] = useState<IGlossaryDetail>(
    getInitGlossaryDetail(),
  );
  useEffect(() => {
    if (currentGlossaryDetail) {
      setInputtingDetail(currentGlossaryDetail);
    }
    return () => {
      setInputtingDetail(getInitGlossaryDetail());
    };
  }, [currentGlossaryDetail]);

  const currentId = match.params.glossaryId;

  const [glossaryNode, setGlossaryNode] = useState<GlossaryNode | null>(null);
  useEffect(() => {
    if (currentId) {
      dispatch.glossary.fetchGlossaryDetail(currentId).then(resp => {
        if (resp) {
          const { id, name, description } = resp;
          const newGlossaryNode = { id, name, description };
          dispatch.glossary
            .fetchNodeChildAndUpdateNode({ nodeData: newGlossaryNode })
            .then(resp1 => {
              setGlossaryNode(resp1);
            });
        }
      });
    } else {
      setIsEditing(true);
    }
    return () => {
      dispatch.glossary.updateState({
        key: 'currentGlossaryDetail',
        value: null,
      });
    };
  }, [currentId, dispatch.glossary]);

  const handleClickBack = useCallback(() => {
    history.push('/data-discovery/glossary');
  }, [history]);

  const updateInputtingDetail = (key: keyof IGlossaryDetail, value: any) => {
    setInputtingDetail(detail => ({
      ...detail,
      [key]: value,
    }));
  };

  const handleChangeName = useCallback(e => {
    updateInputtingDetail('name', e.target.value);
  }, []);

  const handleChangeDesc = useCallback(e => {
    updateInputtingDetail('description', e.target.value);
  }, []);

  const handleChangeParent = useCallback(v => {
    updateInputtingDetail('parent', v);
  }, []);

  const handleChangeAssets = useCallback(v => {
    updateInputtingDetail('assets', v);
  }, []);

  const handleDeleteGlossary = useCallback(() => {
    if (currentId) {
      dispatch.glossary.deleteGlossary(currentId).then(resp => {
        if (resp) {
          message.success(t('common.operateSuccess'));
          history.push('/data-discovery/glossary');
        }
      });
    }
  }, [currentId, dispatch.glossary, history, t]);

  const showConfirm = useCallback(() => {
    confirm({
      title: t('glossary.delete.title'),
      icon: <ExclamationCircleOutlined />,
      content:
        (glossaryNode?.children?.length ?? 0) > 0
          ? t('glossary.delete.content')
          : t('glossary.delete.leafContent'),
      onOk() {
        handleDeleteGlossary();
      },
    });
  }, [glossaryNode, handleDeleteGlossary, t]);

  const handleClickCancel = useCallback(() => {
    setInputtingDetail(currentGlossaryDetail || getInitGlossaryDetail());
    setIsEditing(false);
  }, [currentGlossaryDetail]);

  const getParams = useCallback(() => {
    const { name, description, parent, assets } = inputtingDetail;
    const assetIds = assets?.filter(i => !!i).map(i => i!.id);
    const parentId = parent?.id;
    return { name, description, assetIds, parentId };
  }, [inputtingDetail]);

  const handleClickSave = useCallback(() => {
    const { id } = inputtingDetail;
    const diss = message.loading(t('common.loading'), 0);
    const params = getParams();
    dispatch.glossary.editGlossary({ id, params }).then(resp => {
      diss();
      if (resp) {
        message.success(t('common.operateSuccess'));
        setIsEditing(false);
      }
    });
  }, [dispatch.glossary, getParams, inputtingDetail, t]);

  const handleClickCreateCancel = useCallback(() => {
    history.push('/data-discovery/glossary');
  }, [history]);

  const handleClickCreate = useCallback(() => {
    const diss = message.loading(t('common.loading'), 0);
    const params = getParams();

    dispatch.glossary.addGlossary(params).then(resp => {
      diss();
      if (resp) {
        message.success(t('common.operateSuccess'));
        setIsEditing(false);
        history.replace(`/data-discovery/glossary/${resp.id}`);
      }
    });
  }, [dispatch.glossary, getParams, history, t]);

  const buttonList = () => {
    if (isEditing) {
      if (currentId) {
        return (
          <>
            <Button
              style={{ marginLeft: 'auto', marginRight: 16 }}
              size="large"
              danger
              onClick={showConfirm}
            >
              {t('common.button.delete')}
            </Button>
            <Button
              style={{ marginRight: 16 }}
              size="large"
              onClick={handleClickCancel}
            >
              {t('common.button.cancel')}
            </Button>
            <Button
              disabled={!inputtingDetail.name || !inputtingDetail.description}
              type="primary"
              size="large"
              onClick={handleClickSave}
            >
              {t('common.button.save')}
            </Button>
          </>
        );
      }
      return (
        <>
          <Button
            style={{ marginLeft: 'auto', marginRight: 16 }}
            size="large"
            onClick={handleClickCreateCancel}
          >
            {t('common.button.cancel')}
          </Button>
          <Button
            disabled={!inputtingDetail.name || !inputtingDetail.description}
            type="primary"
            size="large"
            onClick={handleClickCreate}
          >
            {t('common.button.create')}
          </Button>
        </>
      );
    }
    return (
      <Button
        style={{ marginLeft: 'auto' }}
        size="large"
        onClick={() => setIsEditing(true)}
      >
        {t('common.button.edit')}
      </Button>
    );
  };

  // 渲染的地方都用inputtingDetail 替代 currentGlossaryDetail
  return (
    <Spin
      wrapperClassName={styles.container}
      spinning={fetchCurrentGlossaryDetailLoading}
    >
      <div className={styles.backButoonRow}>
        <span className={styles.backButton} onClick={handleClickBack}>
          {`< ${t('common.back')}`}
        </span>
      </div>

      <Card className={styles.titleArea}>
        {isEditing && !currentId && (
          <span style={{ marginRight: 8 }}>{t('glossary.nameLabel')}:</span>
        )}
        {isEditing ? (
          <Input
            size="large"
            style={{ width: 384 }}
            value={inputtingDetail.name}
            onChange={handleChangeName}
          />
        ) : (
          <span className={styles.title}>{inputtingDetail.name}</span>
        )}

        {buttonList()}
      </Card>

      <div className={styles.contentArea}>
        <div className={styles.leftArea}>
          <Card className={styles.descArea}>
            <div className={styles.descLabel}>{t('glossary.desc')}</div>
            <div className={styles.descInputContainer}>
              {isEditing ? (
                <TextArea
                  className={styles.descInput}
                  value={inputtingDetail.description}
                  onChange={handleChangeDesc}
                />
              ) : (
                <div>{inputtingDetail.description}</div>
              )}
            </div>
          </Card>

          <Card className={styles.glossaryTreeContainer}>
            <GlossaryTree rootNode={glossaryNode} />
          </Card>
        </div>

        <Card className={styles.rightArea}>
          {(currentGlossaryDetail?.parent || isEditing) && (
            <div className={styles.inputBlock}>
              <div className={styles.label}>{t('glossary.parent')}</div>
              <div>
                <ParentSearch
                  isEditting={isEditing}
                  selectedParent={currentGlossaryDetail?.parent}
                  onChange={handleChangeParent}
                  disabledId={currentGlossaryDetail?.id}
                />
              </div>
            </div>
          )}

          {glossaryNode?.children && (
            <div className={styles.inputBlock}>
              <div className={styles.label} style={{ marginBottom: 14 }}>
                {t('glossary.childGlossary')}
              </div>
              <div>
                <ChildrenGlossaryList childList={glossaryNode.children} />
              </div>
            </div>
          )}

          {((inputtingDetail?.assets?.length ?? 0) > 0 || isEditing) && (
            <div className={styles.inputBlock}>
              <div className={styles.label} style={{ marginBottom: 14 }}>
                {t('glossary.assets')}
              </div>
              <div>
                <AssetList
                  isEditting={isEditing}
                  assetList={inputtingDetail?.assets || []}
                  onChange={handleChangeAssets}
                />
              </div>
            </div>
          )}
        </Card>
      </div>
    </Spin>
  );
}
