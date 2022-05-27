import React, { useCallback, useEffect, useState, useMemo } from 'react';
import { useHistory } from 'umi';
import { RouteComponentProps } from 'react-router';
import { queryGlossaryRole } from '@/services/glossary';
import { Spin, Button, Input, Modal, message, Popover } from 'antd';
import { useRequest } from 'ahooks';
import { Operation } from '@/definitions/Glossary.type';
import { ExclamationCircleOutlined } from '@ant-design/icons';

import useI18n from '@/hooks/useI18n';
import useRedux from '@/hooks/useRedux';

import {
  getInitGlossaryDetail,
  GlossaryDetail as IGlossaryDetail,
  GlossaryNode,
  GlossaryChild,
} from '@/rematch/models/glossary';
import { useStickyState } from '@/hooks/useStickyState';
import ParentSearch from './components/ParentSearch/ParentSearch';
import ChildrenGlossaryList from './components/ChildrenGlossaryList/ChildrenGlossaryList';
import AssetList from './components/AssetList/AssetList';
import Editor from './components/Editor/Editor';
import { PasteContent } from './components/PasteContent/PasteContent';

import styles from './index.less';

interface MatchParams {
  glossaryId?: string;
}

interface Props extends RouteComponentProps<MatchParams> {
  currentId?: string;
  setCurrentId: (id: string) => void;
  addChild: (child: {}, parentId: string, id?: string) => void;
  editNodeName: (id: string, name: string) => void;
  deleteChild: (parentId: string, id: string) => void;
  changeParent: (preParentId: string, currentParentId: string, id: string) => void;
  onClose: () => void;
}

const { TextArea } = Input;
const { confirm } = Modal;

export default function GlossaryDetail({
  currentId,
  addChild,
  editNodeName,
  deleteChild,
  setCurrentId,
  changeParent,
  onClose,
}: Props) {
  const t = useI18n();
  const history = useHistory();

  const query = useMemo(() => (history.location as any)?.query ?? {}, [history.location]);
  const [visible, setVisible] = useState(false);
  const { selector, dispatch } = useRedux(state => state.glossary);
  const [glossarySticky, setGlossarySticky] = useStickyState<IGlossaryDetail | null>(null, 'glossary');
  const [isEditing, setIsEditing] = useState(false);
  const [preId, setPreId] = useState<string>();
  const [pretName, setPreName] = useState<string>();

  const { currentGlossaryDetail, fetchCurrentGlossaryDetailLoading } = selector;
  const [inputtingDetail, setInputtingDetail] = useState<IGlossaryDetail>(getInitGlossaryDetail());

  const queryGlossaryRoleRequest = useRequest(queryGlossaryRole, {
    manual: true,
  });

  const GLossaryRole = useMemo(() => {
    return queryGlossaryRoleRequest?.data?.operations;
  }, [queryGlossaryRoleRequest.data]);

  useEffect(() => {
    if (currentGlossaryDetail) {
      setInputtingDetail(currentGlossaryDetail);
    } else if (preId && pretName) {
      setInputtingDetail(i => ({
        ...i,
        parent: {
          id: preId,
          name: pretName,
        },
      }));
    }

    return () => {
      setInputtingDetail(getInitGlossaryDetail());
    };
  }, [currentGlossaryDetail, pretName, preId, query]);

  const [glossaryNode, setGlossaryNode] = useState<GlossaryNode | null>(null);
  useEffect(() => {
    setIsEditing(false);

    if (currentId) {
      dispatch.glossary.fetchGlossaryDetail(currentId).then(resp => {
        if (resp) {
          const { id, name, description } = resp;
          const newGlossaryNode = { id, name, description };
          dispatch.glossary.fetchNodeChildAndUpdateNode({ nodeData: newGlossaryNode }).then(resp1 => {
            setGlossaryNode(resp1);
          });
        }
      });

      queryGlossaryRoleRequest.runAsync(currentId);
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

  const copyGlossary = () => {
    if (currentGlossaryDetail) {
      setGlossarySticky(currentGlossaryDetail);
      message.success(t('common.operateSuccess'));
    }
  };

  const createChild = (name: string, id: string) => {
    setIsEditing(false);
    setCurrentId('');
    setPreId(id);
    setPreName(name);
    setGlossaryNode(null);
  };
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
          deleteChild(resp.parentId, resp.id);
          message.success(t('common.operateSuccess'));
          onClose();
        }
      });
    }
  }, [currentId, deleteChild, dispatch.glossary, onClose, t]);

  const showConfirm = useCallback(() => {
    confirm({
      title: t('glossary.delete.title'),
      icon: <ExclamationCircleOutlined />,
      content:
        (glossaryNode?.children?.length ?? 0) > 0 ? t('glossary.delete.content') : t('glossary.delete.leafContent'),
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
    const assetIds = assets?.filter(i => !!i).map(i => i.id);
    const parentId = parent?.id;
    return { name, description, assetIds, parentId };
  }, [inputtingDetail]);

  const saveFunc = useCallback(
    (id, params) => {
      const diss = message.loading(t('common.loading'), 0);

      dispatch.glossary.editGlossary({ id, params }).then(resp => {
        diss();
        if (resp) {
          const preParentId = currentGlossaryDetail?.parent?.id;
          if (preParentId !== params.parentId) {
            changeParent(preParentId, params.parentId, id);
          }
          editNodeName(id, params.name);
          message.success(t('common.operateSuccess'));
          setIsEditing(false);
        }
      });
    },
    [dispatch.glossary, t, currentGlossaryDetail, changeParent, editNodeName],
  );

  const handleClickSave = useCallback(() => {
    const { id } = inputtingDetail;
    const params = getParams();
    saveFunc(id, params);
  }, [getParams, inputtingDetail, saveFunc]);

  const handleClickCreateCancel = useCallback(() => {
    onClose();
  }, [onClose]);

  const handleClickCreate = useCallback(() => {
    const diss = message.loading(t('common.loading'), 0);
    const params = getParams();

    dispatch.glossary.addGlossary(params).then(resp => {
      diss();
      if (resp) {
        setIsEditing(true);
        addChild(resp, resp?.parent?.id, resp.id);
        message.success(t('common.operateSuccess'));
      }
    });
  }, [dispatch.glossary, addChild, getParams, t]);

  const buttonList = () => {
    if (isEditing) {
      if (currentId) {
        return (
          <>
            {GLossaryRole && GLossaryRole.includes(Operation.REMOVE_GLOSSARY) && (
              <Button style={{ marginLeft: 'auto', marginRight: 16 }} size="large" danger onClick={showConfirm}>
                {t('common.button.delete')}
              </Button>
            )}
            <Button style={{ marginRight: 16 }} size="large" onClick={handleClickCancel}>
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
          <Button style={{ marginLeft: 'auto', marginRight: 16 }} size="large" onClick={handleClickCreateCancel}>
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
      <div style={{ marginLeft: 'auto' }}>
        {GLossaryRole && GLossaryRole.includes(Operation.EDIT_GLOSSARY) && (
          <Button size="large" onClick={() => setIsEditing(true)} type="primary" ghost>
            {t('common.button.edit')}
          </Button>
        )}
        {GLossaryRole && GLossaryRole.includes(Operation.COPY_GLOSSARY) && (
          <Button style={{ marginLeft: 10 }} size="large" type="primary" ghost onClick={copyGlossary}>
            {t('common.button.copy')}
          </Button>
        )}
        {GLossaryRole && GLossaryRole.includes(Operation.PASTE_GLOSSARY) && (
          <Popover
            visible={visible}
            onVisibleChange={(value: boolean) => setVisible(value)}
            placement="bottom"
            title={null}
            content={
              <PasteContent
                glossarySticky={glossarySticky}
                addChild={addChild}
                setVisible={setVisible}
                currentGlossaryDetail={currentGlossaryDetail}
              />
            }
            trigger="click"
          >
            <Button style={{ marginLeft: 10 }} size="large" type="primary" ghost>
              {t('common.button.paste')}
            </Button>
          </Popover>
        )}
      </div>
    );
  };

  const handleDeleteSingleAsset = useCallback(
    assetId => {
      const { id, name, description, parent, assets } = inputtingDetail;
      const newAssets = assets?.filter(asset => asset.id !== assetId) ?? [];
      const parentId = parent?.id;
      const assetIds = newAssets?.filter(i => !!i).map(i => i!.id);
      const params = { name, description, assetIds, parentId };
      saveFunc(id, params);
    },
    [inputtingDetail, saveFunc],
  );
  const handleAddSingleAsset = useCallback(
    asset => {
      const { id, name, description, parent, assets } = inputtingDetail;
      const newAssets = assets ? [...assets, asset] : [asset];
      const parentId = parent?.id;
      const assetIds = newAssets?.filter(i => !!i).map(i => i!.id);
      const params = { name, description, assetIds, parentId };
      saveFunc(id, params);
    },
    [inputtingDetail, saveFunc],
  );

  // 渲染的地方都用 inputtingDetail 替代 currentGlossaryDetail
  return (
    <Spin wrapperClassName={styles.container} spinning={fetchCurrentGlossaryDetailLoading}>
      {inputtingDetail.ancestryGlossaryList && (
        <div className={styles.path}>
          {inputtingDetail.ancestryGlossaryList.map((item: GlossaryChild, index: number) => {
            return (
              <span className={styles.pathName} onClick={() => setCurrentId(item.id)}>
                {' '}
                {index !== 0 && '->'} {item.name}
              </span>
            );
          })}
        </div>
      )}
      <div className={styles.titleArea}>
        {isEditing && !currentId && <span style={{ marginRight: 8 }}>{t('glossary.nameLabel')}:</span>}
        {isEditing ? (
          <Input size="large" style={{ width: 384 }} value={inputtingDetail.name} onChange={handleChangeName} />
        ) : (
          <span className={styles.title}>{inputtingDetail.name}</span>
        )}

        {buttonList()}
      </div>
      <div className={styles.descArea}>
        <div className={styles.descLabel}>{t('glossary.desc')}</div>
        <div className={styles.descInputContainer}>
          {isEditing ? (
            <TextArea className={styles.descInput} value={inputtingDetail.description} onChange={handleChangeDesc} />
          ) : (
            <div>{inputtingDetail.description}</div>
          )}
        </div>
      </div>
      <div className={styles.contentArea}>
        {/* <div className={styles.leftArea}>


        </div> */}

        <div className={styles.leftArea}>
          {(inputtingDetail?.parent || isEditing) && (
            <div className={styles.inputBlock}>
              <div className={styles.label}>{t('glossary.parent')}</div>
              <div>
                <ParentSearch
                  currentId={currentId}
                  setCurrentId={setCurrentId}
                  isEditting={isEditing}
                  selectedParent={inputtingDetail?.parent}
                  onChange={handleChangeParent}
                  disabledId={inputtingDetail?.id}
                />
              </div>
            </div>
          )}

          <div className={styles.inputBlock}>
            <div className={styles.funcTitleRow}>
              <div className={styles.funcTitleRowlabel}>
                {t('glossary.childGlossary')}
                {(glossaryNode?.children ?? []).length > 0 && (
                  <span style={{ marginLeft: 4 }}>({glossaryNode?.children?.length})</span>
                )}
              </div>

              {!isEditing && GLossaryRole && GLossaryRole.includes(Operation.EDIT_GLOSSARY_CHILD) && (
                <Button size="small" onClick={() => createChild(inputtingDetail.name, inputtingDetail.id)}>
                  {t('glossary.childGlossary.create')}
                </Button>
              )}
            </div>
            <div>
              <ChildrenGlossaryList setCurrentId={setCurrentId} childList={glossaryNode?.children ?? []} />
            </div>
          </div>

          <Editor
            hasPermission={GLossaryRole && GLossaryRole.includes(Operation.EDIT_GLOSSARY_EDITOR)}
            id={currentId}
          />
        </div>
        <div className={styles.rightArea}>
          <div className={styles.inputBlock}>
            <div className={styles.label} style={{ marginBottom: isEditing ? 14 : 0 }}>
              {t('glossary.assets')}{' '}
              {(inputtingDetail?.assets || []).length > 0 && (
                <span style={{ marginLeft: 4 }}>({(inputtingDetail?.assets || []).length})</span>
              )}
            </div>
            <div>
              <AssetList
                isEditting={isEditing && GLossaryRole && GLossaryRole.includes(Operation.EDIT_GLOSSARY_RESOURCE)}
                assetList={inputtingDetail?.assets || []}
                hasPermission={GLossaryRole && GLossaryRole.includes(Operation.EDIT_GLOSSARY_RESOURCE)}
                onChange={handleChangeAssets}
                onDeleteSingleAsset={handleDeleteSingleAsset}
                onAddSingleAsset={handleAddSingleAsset}
              />
            </div>
          </div>
        </div>
      </div>
    </Spin>
  );
}
