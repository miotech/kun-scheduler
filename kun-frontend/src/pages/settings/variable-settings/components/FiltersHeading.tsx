import React, { memo } from 'react';
import { Button, Col, Input, Row, Space } from 'antd';

import useI18n from '@/hooks/useI18n';
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';

import { useAtom } from '@dbeining/react-atom';
import {
  viewState,
  setSearchKeyword,
  fetchAndLoadVariablesList,
} from '@/pages/settings/variable-settings/atoms/VariableSettingsViewState';

import css from '../index.less';

interface OwnProps {
  loading?: boolean;
  onClickCreateBtn?: () => any;
}

type Props = OwnProps;

export const FiltersHeading: React.FC<Props> = memo(function FiltersHeading(
  props,
) {
  const { searchKeyword } = useAtom(viewState);

  const { loading = false, onClickCreateBtn } = props;

  const t = useI18n();

  return (
    <nav className={css.FilterHeading}>
      <Row gutter={[16, 0]}>
        <Col flex="1 1 0">
          {/* Search input */}
          <Input.Search
            value={searchKeyword}
            onChange={ev => setSearchKeyword(ev.target.value)}
            placeholder={t('settings.variableSettings.searchByKeyName')}
          />
        </Col>
        <Col flex="0 0 220px">
          <Space>
            {/* Create new variable button */}
            <Button
              onClick={onClickCreateBtn}
              icon={<PlusOutlined />}
              type="primary"
            >
              {t('settings.variableSettings.create')}
            </Button>
            {/* Refresh button */}
            <Button
              loading={loading}
              icon={<ReloadOutlined />}
              onClick={() => {
                fetchAndLoadVariablesList();
              }}
            >
              {t('common.refresh')}
            </Button>
          </Space>
        </Col>
      </Row>
    </nav>
  );
});
