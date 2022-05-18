// @ts-nocheck
/* eslint-disable no-underscore-dangle, no-inner-declarations, no-param-reassign */
import { useHistory, formatMessage } from 'umi';
import React, { memo, useEffect, useCallback, useState, forwardRef, useImperativeHandle } from 'react';
import { useUpdateEffect, useMount } from 'ahooks';
import * as d3 from 'd3';
import cloneDeep from 'lodash/cloneDeep';
import { PlusOutlined, MinusOutlined } from '@ant-design/icons';
import { Drawer } from 'antd';
import { GlossaryNode } from '@/rematch/models/glossary';
import useRedux from '@/hooks/useRedux';
import { fetchGlossariesService } from '@/services/glossary';
import GlossaryDetail from '@/pages/glossary/glossary-detail/index';
import useUrlState from '@ahooksjs/use-url-state';
import { Tree } from './Tree';
import styles from './GlossaryTree.less';

export interface Props {
  rootNode: GlossaryNode | null;
}

let currentIdCache = null;
let tree = null;

export default memo(
  forwardRef(function GlossaryTree({ rootNode }: Props, ref) {
    const history = useHistory();
    const { dispatch } = useRedux(() => {});

    const [visible, setVisible] = useState(false);
    const [currentId, setCurrentId] = useState();
    const [routeParams, setRouteParams] = useUrlState();
    const setCurrentIdCache = id => {
      setCurrentId(id);
      currentIdCache = id;
    };
    const onClose = useCallback(() => {
      setVisible(false);
      setCurrentIdCache();
    }, [setVisible]);
    useImperativeHandle(ref, () => ({
      create: () => {
        setVisible(true);
        setCurrentIdCache();
      },
      setCurrentId: (id: string) => {
        setVisible(true);
        setCurrentIdCache(id);
      },
    }));

    useMount(() => {
      if (routeParams.glossaryId) {
        setCurrentId(routeParams.glossaryId);
        setVisible(true);
      }
    });

    useUpdateEffect(() => {
      const glossaryId = currentId || undefined;
      setRouteParams({ glossaryId });
    }, [currentId]);

    const updateTreeCache = useCallback(() => {
      dispatch.glossary.updateState({
        key: 'glossaryData',
        value: cloneDeep(tree.root),
      });
    }, [dispatch]);

    const fetchChildren = useCallback(async (node, id) => {
      const resp = await fetchGlossariesService(id);
      if (resp) {
        const { children } = resp;
        if (node.childrenCache && node.childrenCache.length === children.length) {
          node.children = node.childrenCache;
        } else {
          node.children = children.map(child => ({
            ...child,
            parentId: id,
          }));
        }
      }
    }, []);

    const click = useCallback(
      async d => {
        if (currentIdCache === d.data.id && d.data.children) {
          d.data.childrenCache = d.data.children;
          d.data.children = null;
          tree.updateTree(d);
          updateTreeCache();
          onClose();
          return;
        }
        setVisible(true);
        setCurrentIdCache(d.data.id);
        if (!d.data.children) {
          await fetchChildren(d.data, d.data.id);
        }
        tree.updateTree(d);
        updateTreeCache();
      },
      [updateTreeCache, onClose],
    );
    useEffect(() => {
      async function fetchRootNode() {
        if (rootNode) {
          tree = new Tree(cloneDeep(rootNode), click);
        } else {
          const resp = await fetchGlossariesService();
          if (resp) {
            const { children } = resp;
            const rootGlossary: GlossaryNode = {
              id: 'root',
              name: formatMessage({
                id: 'glossary.title',
              }),
              description: '',
              childrenCount: children.length,
              children,
            };
            tree = new Tree(cloneDeep(rootGlossary), click);
          }
        }
        updateTreeCache();
      }
      fetchRootNode();
      return () => {
        const treeElement = document.getElementById('tree');
        if (treeElement) {
          treeElement.innerHTML = '';
        }
      };
    }, [dispatch.glossary, history, click, fetchChildren, onClose]);

    async function editNodeName(id, newName) {
      d3.select(`[id=text${id}]`).text(newName);
    }

    async function addChild(child, parentId, id) {
      if (!parentId) {
        tree.addChildNode({ data: tree.root }, child);
        setCurrentIdCache(id);
        updateTreeCache();
      } else {
        d3.selectAll('rect').each(async function textfunc(d) {
          if (d && d.data.id === parentId) {
            if (!d.data.children) {
              await fetchChildren(d.data, d.data.id);
              console.log('1');
            } else {
              tree.addChildNode(d, child);
            }
            console.log('2');

            d3.select(`[id=count${parentId}]`).text(d.data.children.length);
            tree.updateTree(d);
            setCurrentIdCache(id);
            updateTreeCache();
          }
        });
      }
    }
    function deleteChild(parentId, id) {
      if (!parentId) {
        tree.deleteChildNode({ data: tree.root }, id);
      }
      d3.selectAll('rect').each(function textfunc(d) {
        if (d && d.data.id === parentId) {
          tree.deleteChildNode(d, id);
          d3.select(`[id=count${parentId}]`).text(d.data.children.length);
        }
      });
      updateTreeCache();
    }

    async function changeParent(preParentId, currentParentId, id) {
      const node = d3.select(`[id=node${id}]`).data();
      addChild(node[0].data, currentParentId, id);
      deleteChild(preParentId, id);
    }

    useUpdateEffect(() => {
      d3.selectAll('rect').each(function textfunc(d) {
        if (d) {
          const canNode = d3.select(this);
          canNode.attr('stroke', 'rgba(224, 224, 224, 0.8)');
          if (d.data.id === currentId) {
            canNode.attr('stroke', '#1a73e8');
          }
        }
      });
    }, [currentId]);
    const handleClickAdd = useCallback(() => {
      tree.zoomIn();
    }, []);

    const handleClickSub = useCallback(() => {
      tree.zoomOut();
    }, []);

    return (
      <div className={styles.glossaryTree}>
        <div className={styles.scaleButtonRow}>
          <div className={styles.scaleButton} onClick={handleClickAdd}>
            <PlusOutlined className={styles.scaleButtonIcon} />
          </div>
          <div className={styles.scaleButton} onClick={handleClickSub}>
            <MinusOutlined className={styles.scaleButtonIcon} />
          </div>
        </div>
        <Drawer title="" width={800} placement="right" destroyOnClose onClose={onClose} mask={false} visible={visible}>
          <GlossaryDetail
            changeParent={changeParent}
            addChild={addChild}
            editNodeName={editNodeName}
            deleteChild={deleteChild}
            setCurrentId={setCurrentIdCache}
            onClose={onClose}
            currentId={currentId}
          />
        </Drawer>
        <div id="tree" className={styles.treeContainer} />
      </div>
    );
  }),
);
