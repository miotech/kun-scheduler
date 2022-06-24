/* eslint-disable react/no-unused-prop-types */
import React, { memo, useImperativeHandle, useEffect, useState, forwardRef } from 'react';
import useRedux from '@/hooks/useRedux';
import useUrlState from '@ahooksjs/use-url-state';
import GlossaryDetail from '@/pages/glossary/glossary-detail/index';
import { ReflexContainer, ReflexSplitter, ReflexElement } from 'react-reflex';
import { GlossaryDisplayType } from '@/definitions/Glossary.type';
import styles from './index.less';
import TreeList from './TreeList';
import { DisplayTypeSwitch } from '../components/DisplayTypeSwitch';
import AutosuggestInput from '../components/AutosuggestInput/AutosuggestInput';
import 'react-reflex/styles.css';

interface Props {
  glossaryDisplayType: GlossaryDisplayType;
  changeDisplayType: (displayType: GlossaryDisplayType) => void;
}

const GlossaryListView = memo(
  forwardRef((props: Props, ref) => {
    const { glossaryDisplayType, changeDisplayType } = props;
    const { selector, dispatch } = useRedux<any>(state => state.glossaryList);
    const { glossaryListData, expandedKeys } = selector;
    const [routeParams, setRouteParams] = useUrlState();
    const [visible, setVisible] = useState(false);

    const setCurrentGlossaryId = (glossaryId: string) => {
      setRouteParams({ glossaryId });
    };

    const changeParent = async (preParentId: string, currentParentId: string) => {
      await dispatch.glossaryList.fetchGlossaryChildren({ glossaryId: preParentId });
      dispatch.glossaryList.fetchGlossaryChildren({ glossaryId: currentParentId });
    };
    const addChild = async (child: {}, parentId: string, id?: string) => {
      if (parentId) {
        await dispatch.glossaryList.fetchGlossaryChildren({ glossaryId: parentId });
        if (!expandedKeys.includes(parentId)) {
          dispatch.glossaryList.updateState({
            key: 'expandedKeys',
            value: [...expandedKeys, parentId],
          });
        }
      } else {
        dispatch.glossaryList.fetchGlossaryRoot();
      }
      setRouteParams({ glossaryId: id });
    };

    const deleteChild = (parentId: string) => {
      if (parentId) {
        dispatch.glossaryList.fetchGlossaryChildren({ glossaryId: parentId });
      } else {
        dispatch.glossaryList.fetchGlossaryRoot();
      }
    };

    const editNodeName = (glossaryId: string, name: string, parentId: string) => {
      dispatch.glossaryList.fetchGlossaryChildren({ glossaryId: parentId });
    };
    const onClose = () => {
      setRouteParams({ glossaryId: undefined });
      setVisible(false);
    };

    useImperativeHandle(ref, () => ({
      create: () => {
        setVisible(true);
        setRouteParams({ glossaryId: undefined });
      },
    }));

    useEffect(() => {
      if (!glossaryListData) {
        dispatch.glossaryList.fetchGlossaryRoot();
      }
    }, [glossaryListData, dispatch.glossaryList]);

    useEffect(() => {
      if (routeParams.glossaryId) {
        setVisible(true);
      }
    }, [routeParams]);
    return (
      <ReflexContainer orientation="vertical" className={styles.container}>
        <ReflexElement className={styles.conLeft} flex={0.192} minSize={350}>
          <DisplayTypeSwitch currentType={glossaryDisplayType} onChange={changeDisplayType} />
          <div className={styles.autosuggestInputContainer}>
            <AutosuggestInput onSelect={setCurrentGlossaryId} showPath onPathClick={setCurrentGlossaryId} />
          </div>
          <div className={styles.treeListContainer}>
            <TreeList setCurrentGlossaryId={setCurrentGlossaryId} glossaryId={routeParams.glossaryId} />
          </div>
        </ReflexElement>

        <ReflexSplitter propagate />

        <ReflexElement className={styles.conRight} flex={0.847} minSize={800}>
          {visible && (
            <GlossaryDetail
              changeParent={changeParent}
              addChild={addChild}
              editNodeName={editNodeName}
              deleteChild={deleteChild}
              setCurrentId={setCurrentGlossaryId}
              onClose={onClose}
              currentId={routeParams.glossaryId}
            />
          )}
        </ReflexElement>
      </ReflexContainer>
    );
  }),
);

export default GlossaryListView;
