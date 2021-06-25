import React, { memo, useCallback, useEffect, useState } from 'react';
import isEqual from 'lodash/isEqual';
import cloneDeep from 'lodash/cloneDeep';
import { SQLQueryResult, SQLQueryTab } from '@/definitions/QueryResult.type';
import { Button, Spin, Tabs } from 'antd';
import { LeftOutlined, RightOutlined } from '@ant-design/icons';

import { QueryResultTable } from '@/components/QueryResultTable/QueryResultTable.component';
import css from './SqlDryRunBottomLayout.module.less';

interface OwnProps {
  onClose?: () => any;
  currentTabId?: string;
  tabs: SQLQueryTab[];
  setTabs: (nextTabsState: SQLQueryTab[]) => any;
  doSqlExecuteAndFetchPage: (sql: string, pageNum: number, pageSize: number) => Promise<SQLQueryResult>;
}

type Props = OwnProps;

export const SqlDryRunBottomLayout: React.FC<Props> = memo(function SqlDryRunBottomLayout(props) {

  const { currentTabId, tabs, setTabs, doSqlExecuteAndFetchPage } = props;

  const [ prevTabsState, setPrevTabsState ] = useState<SQLQueryTab[]>(tabs || []);
  // eslint-disable-next-line no-void
  const [ activeKey, setActiveKey ] = useState<string | undefined>(currentTabId || tabs?.[0].id || (void 0));
  const [ loadingState, setLoadingState ] = useState<Record<string, boolean>>({});

  // When a new tab joins, auto switch to that latest tab
  useEffect(() => {
    if (!isEqual(prevTabsState, tabs)) {
      // it should not be removing a tab
      if (prevTabsState.length < tabs.length) {
        setActiveKey(tabs[tabs.length - 1].id);
      }
      setPrevTabsState(cloneDeep(tabs));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tabs]);

  const handleEditTabs = useCallback(function handleEditTabs(targetKey: React.MouseEvent | React.KeyboardEvent | string, action: 'add' | 'remove') {
    if (action === 'remove') {
      const nextTabsState = [...tabs].filter(t => t.id !== targetKey.toString());
      if (activeKey === targetKey.toString()) {
        setActiveKey(nextTabsState?.[0]?.id || undefined);
      }
      setTabs(nextTabsState);
    }
  }, [tabs, activeKey, setTabs]);

  const tabGoToNextPage = useCallback((tab: SQLQueryTab) => {
    const newTabs = cloneDeep(tabs);
    const targetIndex = newTabs.findIndex(tb => tb.id === tab.id);
    setLoadingState(s => ({ ...s, [tab.id]: true }));
    doSqlExecuteAndFetchPage(tab.sql, tab.pageNum + 1, tab.pageSize)
      .then(result => {
        newTabs[targetIndex].pageNum += 1;
        newTabs[targetIndex].response = result;
        setTabs(newTabs);
      })
      .catch(() => {
        newTabs[targetIndex].response = null;
      })
      .finally(() => {
        setLoadingState(s => ({ ...s, [tab.id]: false }));
        setTabs(newTabs);
      });
  }, [doSqlExecuteAndFetchPage, setTabs, tabs]);

  const tabGoToPrevPage = useCallback((tab: SQLQueryTab) => {
    const newTabs = cloneDeep(tabs);
    const targetIndex = newTabs.findIndex(tb => tb.id === tab.id);
    setLoadingState(s => ({ ...s, [tab.id]: true }));
    doSqlExecuteAndFetchPage(tab.sql, tab.pageNum - 1, tab.pageSize)
      .then(result => {
        newTabs[targetIndex].pageNum -= 1;
        newTabs[targetIndex].response = result;
      })
      .catch(() => {
        newTabs[targetIndex].response = null;
      })
      .finally(() => {
        setLoadingState(s => ({ ...s, [tab.id]: false }));
        setTabs(newTabs);
      });
  }, [doSqlExecuteAndFetchPage, setTabs, tabs]);

  const renderTabData = useCallback((queryTab: SQLQueryTab) => {
    if (!queryTab.done || !queryTab.response) {
      return <div className={css.NotLoaded}>
        <Spin />
      </div>;
    }
    // else
    if (queryTab.response.errorMsg) {
      return (
        <div className={css.ErrorMsg}>
          <pre>{queryTab.response.errorMsg}</pre>
        </div>
      );
    }
    // else
    const rowCountStart = (queryTab.pageNum - 1) * queryTab.pageSize + 1;
    return (
      <>
        <nav className={css.QueryResultFunctionBar}>
          <span className={css.Pagination}>
            {/* Button prev page */}
            <Button
              size="small"
              icon={<LeftOutlined />}
              disabled={loadingState[queryTab.id] || queryTab.pageNum === 1}
              onClick={() => tabGoToPrevPage(queryTab)}
            />
            {/* Row range text */}
            <span className={css.PaginationDisplayText}>
              Rows: {rowCountStart} - {rowCountStart + (queryTab.response.records?.length || 0) - 1}
            </span>
            {/* Button next page */}
            <Button
              size="small"
              icon={<RightOutlined />}
              disabled={loadingState[queryTab.id] || (!queryTab.response?.hasNext)}
              onClick={() => tabGoToNextPage(queryTab)}
            />
          </span>
        </nav>
        <QueryResultTable
          columnNames={queryTab.response.columnNames}
          data={queryTab.response.records}
        />
      </>
    );
  }, [loadingState, tabGoToNextPage, tabGoToPrevPage]);

  return (
    <section className={css.SqlDryRunBottomLayout}>
      <Tabs
        type="editable-card"
        hideAdd
        activeKey={activeKey}
        onChange={(k: string) => setActiveKey(k)}
        onEdit={handleEditTabs}
      >
        {(tabs || []).map((queryTab) => {
          return (
            <Tabs.TabPane
              tab={`Query ${queryTab.ordinal}`}
              key={queryTab.id}
              closable
            >
              <Spin spinning={loadingState[queryTab.id] || false}>
                <div className={css.QueryTabContent}>
                  {renderTabData(queryTab)}
                </div>
              </Spin>
            </Tabs.TabPane>
          );
        })}
      </Tabs>
    </section>
  );
});
