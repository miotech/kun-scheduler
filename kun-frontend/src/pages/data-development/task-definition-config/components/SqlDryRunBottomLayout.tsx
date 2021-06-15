import React, { memo, useCallback, useState } from 'react';
import { SQLQueryTab } from '@/definitions/QueryResult.type';
import { Button, Spin, Tabs } from 'antd';
import { LeftOutlined, RightOutlined } from '@ant-design/icons';

import { QueryResultTable } from '@/components/QueryResultTable/QueryResultTable.component';
import css from './SqlDryRunBottomLayout.module.less';

interface OwnProps {
  onClose?: () => any;
  currentTabId?: string;
  tabs: SQLQueryTab[];
  setTabs: (nextTabsState: SQLQueryTab[]) => any;
}

type Props = OwnProps;

export const SqlDryRunBottomLayout: React.FC<Props> = memo(function SqlDryRunBottomLayout(props) {

  const { currentTabId, tabs, setTabs } = props;

  // eslint-disable-next-line no-void
  const [activeKey, setActiveKey] = useState<string | undefined>(currentTabId || tabs?.[0].id || (void 0));

  const handleEditTabs = useCallback(function handleEditTabs(targetKey: React.MouseEvent | React.KeyboardEvent | string, action: 'add' | 'remove') {
    if (action === 'remove') {
      const nextTabsState = [...tabs].filter(t => t.id !== targetKey.toString());
      if (activeKey === targetKey.toString()) {
        setActiveKey(nextTabsState?.[0]?.id || undefined);
      }
      setTabs(nextTabsState);
    }
  }, [tabs, activeKey, setTabs]);

  const renderTabData = useCallback((queryTab: SQLQueryTab) => {
    if (!queryTab.done || !queryTab.response) {
      return <Spin />;
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
    return (
      <>
        <nav className={css.QueryResultFunctionBar}>
          <Button size="small" icon={<LeftOutlined />} />
          <Button size="small" icon={<RightOutlined />} />
        </nav>
        <QueryResultTable
          columnNames={queryTab.response.columnNames}
          data={queryTab.response.records}
        />
      </>
    );
  }, []);

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
              <div className={css.QueryTabContent}>
                {renderTabData(queryTab)}
              </div>
            </Tabs.TabPane>
          );
        })}
      </Tabs>
    </section>
  );
});
