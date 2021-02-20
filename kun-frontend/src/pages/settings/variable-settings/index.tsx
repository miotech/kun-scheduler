import React from 'react';
import { Card } from 'antd';
import { useMount } from 'ahooks';
import { useAtom } from '@dbeining/react-atom';

import { VariablesTable } from '@/pages/settings/variable-settings/components/VariablesTable';
import {
  viewState,
  fetchAndLoadVariablesList,
} from './atoms/VariableSettingsViewState';
import css from './index.less';

export default function VariableSettingsPage() {
  const { variableList } = useAtom(viewState);

  useMount(function onMount() {
    fetchAndLoadVariablesList();
  });

  return (
    <div id="variable-settings-view" className={css.View}>
      <Card>
        <VariablesTable variableList={variableList} />
      </Card>
    </div>
  );
}
