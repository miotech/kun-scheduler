import React, { useCallback, useMemo, useState } from 'react';
import { Card } from 'antd';
import { useMount } from 'ahooks';
import { useAtom } from '@dbeining/react-atom';

import { VariablesTable } from '@/pages/settings/variable-settings/components/VariablesTable';
import { FiltersHeading } from '@/pages/settings/variable-settings/components/FiltersHeading';

import {
  CreateVariableModal,
  ModalFormValue,
} from '@/pages/settings/variable-settings/components/CreateVariableModal';
import {
  createGlobalVariable,
  deleteGlobalVariable,
  updateGlobalVariable,
} from '@/services/variables/variables.services';
import {
  viewState,
  fetchAndLoadVariablesList,
} from './atoms/VariableSettingsViewState';
import css from './index.less';

export default function VariableSettingsPage() {
  const { variableList, variableListIsLoading, searchKeyword } = useAtom(
    viewState,
  );
  const [createModalVisible, setCreateModalVisible] = useState<boolean>(false);

  useMount(function onMount() {
    fetchAndLoadVariablesList();
  });

  const filteredData = useMemo(
    () =>
      (variableList || []).filter(
        variable =>
          `${variable.key}`
            .toLocaleLowerCase()
            .indexOf(searchKeyword.trim().toLocaleLowerCase()) >= 0,
      ),
    [searchKeyword, variableList],
  );

  const existingKeys = useMemo(
    () => (variableList || []).map(variable => variable.key),
    [variableList],
  );

  const handleCreateVariable = useCallback(async function handleCreateVariable(
    formValues: ModalFormValue,
  ) {
    try {
      await createGlobalVariable({
        key: formValues.key.trim(),
        value: formValues.value ? formValues.value.trim() : '',
        encrypted: formValues.secret || false,
      });
    } finally {
      fetchAndLoadVariablesList();
    }
  },
  []);

  const handleUpdateVariableValue = useCallback(
    async function handleUpdateVariableValue(key: string, newValue: string) {
      const variableToUpdate = variableList.find(
        variable => variable.key === key,
      );
      if (!variableToUpdate) {
        return;
      }
      try {
        await updateGlobalVariable({
          key: variableToUpdate.key,
          value: newValue.trim(),
          encrypted: variableToUpdate.encrypted,
        });
      } finally {
        fetchAndLoadVariablesList();
      }
    },
    [variableList],
  );

  const handleDeleteVariable = useCallback(async function handleDeleteVariable(
    key: string,
  ) {
    try {
      await deleteGlobalVariable(key);
    } finally {
      fetchAndLoadVariablesList();
    }
  },
  []);

  return (
    <React.Fragment>
      <div id="variable-settings-view" className={css.View}>
        <FiltersHeading
          loading={variableListIsLoading}
          onClickCreateBtn={() => {
            setCreateModalVisible(true);
          }}
        />
        <Card>
          <VariablesTable
            variableList={filteredData}
            loading={variableListIsLoading}
            onUpdateValue={handleUpdateVariableValue}
            onDeleteVariable={handleDeleteVariable}
          />
        </Card>
      </div>
      <CreateVariableModal
        visible={createModalVisible}
        onCancel={() => {
          setCreateModalVisible(false);
        }}
        onConfirm={handleCreateVariable}
        existingVariableKeys={existingKeys || []}
      />
    </React.Fragment>
  );
}
