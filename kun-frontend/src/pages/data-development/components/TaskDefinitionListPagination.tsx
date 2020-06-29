import React from 'react';
import { Pagination } from 'antd';
import useI18n from '@/hooks/useI18n';

export interface TaskDefinitionListPaginationProps {
  current?: number;
  pageSize?: number;
  total?: number;
  onChange?: (page: number, pageSize?: number) => void;
}

export const TaskDefinitionListPagination: React.FC<TaskDefinitionListPaginationProps> = props => {
  const { current, total, pageSize, onChange } = props;
  const t = useI18n();

  return (
    <div>
      <Pagination
        current={current}
        pageSize={pageSize}
        size="small"
        total={total}
        showTotal={totalNum =>
          t('common.pagination.showTotal', { total: totalNum })
        }
        onChange={onChange}
      />
    </div>
  );
};
